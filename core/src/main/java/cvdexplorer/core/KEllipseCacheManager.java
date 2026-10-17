package cvdexplorer.core;

import cvdexplorer.field.DistanceGrid2D;
import cvdexplorer.field.DistanceGridBuilder;
import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.field.DistanceGridSpec;
import cvdexplorer.geometry.Vector;
import cvdexplorer.kellipse.KEllipseBoundaryBuilder;
import cvdexplorer.kellipse.KEllipsePreparationException;
import cvdexplorer.kellipse.PreparedKEllipseBoundary;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Boundary and distance-grid caches keyed by the k-ellipse instance, not by cluster position.
 * Entries are immutable once stored. A failed boundary build is not stored.
 */
public final class KEllipseCacheManager {
    private final KEllipseBoundaryBuilder boundaryBuilder;
    private final DistanceGridLimits limits;
    private final IdentityHashMap<KEllipseMember, Entry> entries = new IdentityHashMap<>();
    private int lastBoundaryBuilds;
    private int lastGridBuilds;
    private int lastEvictions;
    private long totalSampleBytes;

    public KEllipseCacheManager(KEllipseBoundaryBuilder boundaryBuilder) {
        this(boundaryBuilder, DistanceGridLimits.DEFAULT);
    }

    public KEllipseCacheManager(KEllipseBoundaryBuilder boundaryBuilder, DistanceGridLimits limits) {
        if (boundaryBuilder == null || limits == null) {
            throw new IllegalArgumentException("K-ellipse cache requires a boundary builder and limits");
        }
        this.boundaryBuilder = boundaryBuilder;
        this.limits = limits;
    }

    public PreparedKEllipseBoundary prepareBoundary(KEllipseMember member) throws KEllipsePreparationException {
        if (member == null) {
            throw new IllegalArgumentException("KEllipseMember is required");
        }
        Entry existing = entries.get(member);
        if (existing != null) {
            return existing.boundary;
        }
        PreparedKEllipseBoundary boundary = boundaryBuilder.build(member);
        entries.put(member, new Entry(boundary, null));
        return boundary;
    }

    public Map<KEllipseMember, PreparedKEllipseMember> prepareAll(
            List<ClusterSite> clusters,
            DistanceGridSpec spec
    ) throws KEllipsePreparationException {
        if (spec == null) {
            throw new IllegalArgumentException("Distance grid spec is required");
        }
        lastBoundaryBuilds = 0;
        lastGridBuilds = 0;
        lastEvictions = 0;
        List<KEllipseMember> live = liveMembers(clusters);
        IdentityHashMap<KEllipseMember, Boolean> liveSet = new IdentityHashMap<>();
        for (KEllipseMember member : live) {
            liveSet.put(member, Boolean.TRUE);
        }
        evictAbsent(liveSet);
        long neededBytes = sampleBytesFor(spec, live.size());
        if (neededBytes > limits.maxTotalSampleBytes()) {
            throw new DomainLimitExceededException(
                    "K-ellipse distance grids need " + neededBytes
                            + " bytes (limit " + limits.maxTotalSampleBytes() + ")."
            );
        }
        IdentityHashMap<KEllipseMember, PreparedKEllipseMember> prepared = new IdentityHashMap<>();
        for (KEllipseMember member : live) {
            try {
                prepared.put(member, prepareMember(member, spec));
            } catch (KEllipsePreparationException failure) {
                throw preparationFailure(clusters, member, failure);
            }
        }
        totalSampleBytes = recountSampleBytes();
        return prepared;
    }

    /** Drops caches for k-ellipses that are no longer in the scene. Builds nothing. */
    public void evictAbsent(List<ClusterSite> clusters) {
        lastBoundaryBuilds = 0;
        lastGridBuilds = 0;
        lastEvictions = 0;
        IdentityHashMap<KEllipseMember, Boolean> liveSet = new IdentityHashMap<>();
        for (KEllipseMember member : liveMembers(clusters)) {
            liveSet.put(member, Boolean.TRUE);
        }
        evictAbsent(liveSet);
        totalSampleBytes = recountSampleBytes();
    }

    public List<Vector> outlineFor(KEllipseMember member) {
        Entry entry = entries.get(member);
        if (entry == null) {
            return null;
        }
        return entry.boundary.polyline().vertices();
    }

    PreparedKEllipseBoundary cachedBoundary(KEllipseMember member) {
        Entry entry = entries.get(member);
        return entry == null ? null : entry.boundary;
    }

    public CacheStatistics statistics() {
        return new CacheStatistics(lastBoundaryBuilds, lastGridBuilds, lastEvictions, totalSampleBytes, entries.size());
    }

    private PreparedKEllipseMember prepareMember(KEllipseMember member, DistanceGridSpec spec)
            throws KEllipsePreparationException {
        Entry entry = entries.get(member);
        PreparedKEllipseBoundary boundary;
        if (entry == null) {
            boundary = boundaryBuilder.build(member);
            lastBoundaryBuilds++;
            entry = new Entry(boundary, null);
            entries.put(member, entry);
        } else {
            boundary = entry.boundary;
        }
        if (entry.prepared != null && entry.prepared.grid().spec().equals(spec)) {
            return entry.prepared;
        }
        DistanceGrid2D grid = DistanceGridBuilder.build(spec, boundary.polyline(), boundary.index());
        lastGridBuilds++;
        PreparedKEllipseMember prepared = new PreparedKEllipseMember(member, grid);
        entries.put(member, new Entry(boundary, prepared));
        return prepared;
    }

    private static long sampleBytesFor(DistanceGridSpec spec, int members) {
        if (members <= 0) {
            return 0L;
        }
        long sampleBytes = spec.sampleBytes();
        if (sampleBytes > Long.MAX_VALUE / members) {
            return Long.MAX_VALUE;
        }
        return sampleBytes * members;
    }

    private static KEllipsePreparationException preparationFailure(
            List<ClusterSite> clusters,
            KEllipseMember member,
            KEllipsePreparationException failure
    ) {
        if (clusters != null) {
            for (ClusterSite cluster : clusters) {
                int memberIndex = 0;
                for (ClusterMember candidate : cluster.members()) {
                    if (candidate == member) {
                        return new KEllipsePreparationException(
                                failure.reason(),
                                "K_ELLIPSE member #" + memberIndex + " in cluster " + cluster.name()
                                        + " cannot be prepared: " + failure.getMessage()
                        );
                    }
                    memberIndex++;
                }
            }
        }
        return failure;
    }

    private void evictAbsent(IdentityHashMap<KEllipseMember, Boolean> liveSet) {
        int before = entries.size();
        entries.keySet().removeIf(member -> !liveSet.containsKey(member));
        lastEvictions += before - entries.size();
    }

    private long recountSampleBytes() {
        long bytes = 0L;
        for (Entry entry : entries.values()) {
            if (entry.prepared != null) {
                bytes += entry.prepared.grid().spec().sampleBytes();
            }
        }
        return bytes;
    }

    private static List<KEllipseMember> liveMembers(List<ClusterSite> clusters) {
        IdentityHashMap<KEllipseMember, Boolean> seen = new IdentityHashMap<>();
        List<KEllipseMember> live = new ArrayList<>();
        if (clusters == null) {
            return live;
        }
        for (ClusterSite cluster : clusters) {
            for (ClusterMember member : cluster.members()) {
                if (member instanceof KEllipseMember kEllipse && !seen.containsKey(kEllipse)) {
                    seen.put(kEllipse, Boolean.TRUE);
                    live.add(kEllipse);
                }
            }
        }
        return live;
    }

    public record CacheStatistics(
            int lastBoundaryBuilds,
            int lastGridBuilds,
            int lastEvictions,
            long totalSampleBytes,
            int cachedMembers
    ) {
    }

    private record Entry(
            PreparedKEllipseBoundary boundary,
            PreparedKEllipseMember prepared
    ) {
    }
}
