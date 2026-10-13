package cvdexplorer.core;

import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.field.DistanceGridSpec;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import cvdexplorer.kellipse.KEllipseApproximationSettings;
import cvdexplorer.kellipse.KEllipseBoundaryBuilder;
import cvdexplorer.kellipse.KEllipsePreparationException;
import cvdexplorer.kellipse.PreparedKEllipseBoundary;
import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.NeighborOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prepares one render. K-ellipse distances come from grids built before any query.
 * Use this from the drawing thread only; the cache is not synchronized.
 */
public final class ScenePreparer {
    private final KEllipseCacheManager cache;
    private final QueryDomainPolicy policy;
    private DistanceGridSpec currentDomain;

    public ScenePreparer() {
        this(new QueryDomainPolicy(), new KEllipseCacheManager(
                new KEllipseBoundaryBuilder(KEllipseApproximationSettings.DEFAULT)
        ));
    }

    public ScenePreparer(QueryDomainPolicy policy, KEllipseCacheManager cache) {
        if (policy == null || cache == null) {
            throw new IllegalArgumentException("Scene preparer requires a domain policy and a cache");
        }
        this.policy = policy;
        this.cache = cache;
    }

    public ScenePreparation.PreparedScene prepare(
            List<ClusterSite> clusters,
            MetricKind metricKind,
            NeighborOrder neighborOrder,
            int metricParameter,
            Box requiredWorldBounds
    ) throws KEllipsePreparationException {
        if (!ScenePreparation.containsKEllipse(clusters)) {
            cache.evictAbsent(clusters);
            currentDomain = null;
            return ScenePreparation.prepare(clusters, metricKind, neighborOrder, metricParameter);
        }
        BudgetedPreparation budgeted = prepareWithinBudget(clusters, requiredWorldBounds);
        DistanceGridSpec spec = budgeted.spec();
        Map<KEllipseMember, PreparedKEllipseMember> prepared = budgeted.prepared();
        currentDomain = spec;
        List<ClusterSite> queryClusters = new ArrayList<>(clusters.size());
        for (ClusterSite cluster : clusters) {
            List<ClusterMember> members = new ArrayList<>(cluster.size());
            for (ClusterMember member : cluster.members()) {
                if (member instanceof KEllipseMember kEllipse) {
                    members.add(prepared.get(kEllipse));
                } else {
                    members.add(member);
                }
            }
            queryClusters.add(new ClusterSite(cluster.name(), cluster.color(), members));
        }
        ClusterMetric metric = ScenePreparation.metricFor(clusters, metricKind, metricParameter);
        return new ScenePreparation.PreparedScene(
                List.copyOf(queryClusters),
                metric,
                ScenePreparation.ownershipSelectorFor(neighborOrder),
                spec
        );
    }

    /**
     * Builds grids for {@code clusters} on a new preparer. Does not touch any other cache.
     * Throws {@link DomainLimitExceededException} when the grids do not fit.
     */
    public static ScenePreparer prepareNew(
            List<ClusterSite> clusters,
            MetricKind metricKind,
            NeighborOrder neighborOrder,
            int metricParameter,
            Box requiredWorldBounds,
            DistanceGridLimits limits
    ) throws KEllipsePreparationException {
        ScenePreparer trial = new ScenePreparer(
                new QueryDomainPolicy(
                        QueryDomainPolicy.DEFAULT_SPACING,
                        QueryDomainPolicy.DEFAULT_MARGIN_FRACTION,
                        limits
                ),
                new KEllipseCacheManager(
                        new KEllipseBoundaryBuilder(KEllipseApproximationSettings.DEFAULT),
                        limits
                )
        );
        trial.prepare(clusters, metricKind, neighborOrder, metricParameter, requiredWorldBounds);
        return trial;
    }

    public void evictAbsent(List<ClusterSite> clusters) {
        cache.evictAbsent(clusters);
    }

    /**
     * Reuses the current domain when it covers the viewport. If that retained domain no longer
     * fits the cache budget, builds a fresh domain for this viewport at the same spacing.
     */
    private BudgetedPreparation prepareWithinBudget(List<ClusterSite> clusters, Box requiredWorldBounds)
            throws KEllipsePreparationException {
        DistanceGridSpec spec = policy.ensureCovers(currentDomain, requiredWorldBounds);
        try {
            return new BudgetedPreparation(spec, cache.prepareAll(clusters, spec));
        } catch (DomainLimitExceededException budget) {
            if (spec != currentDomain) {
                throw budget;
            }
            DistanceGridSpec fresh = policy.ensureCovers(null, requiredWorldBounds);
            if (sameExtent(fresh, spec)) {
                throw budget;
            }
            return new BudgetedPreparation(fresh, cache.prepareAll(clusters, fresh));
        }
    }

    private static boolean sameExtent(DistanceGridSpec a, DistanceGridSpec b) {
        return a.originX() == b.originX()
                && a.originY() == b.originY()
                && a.spacing() == b.spacing()
                && a.nodesX() == b.nodesX()
                && a.nodesY() == b.nodesY();
    }

    private record BudgetedPreparation(
            DistanceGridSpec spec,
            Map<KEllipseMember, PreparedKEllipseMember> prepared
    ) {
    }

    public PreparedKEllipseBoundary validate(KEllipseMember member) throws KEllipsePreparationException {
        return cache.prepareBoundary(member);
    }

    public List<Vector> outlineFor(KEllipseMember member) {
        return cache.outlineFor(member);
    }

    PreparedKEllipseBoundary cachedBoundary(KEllipseMember member) {
        return cache.cachedBoundary(member);
    }

    public KEllipseCacheManager.CacheStatistics statistics() {
        return cache.statistics();
    }

    public DistanceGridSpec currentDomain() {
        return currentDomain;
    }
}
