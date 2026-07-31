package cvdexplorer.model;

import cvdexplorer.metric.MetricMemberCompatibility;
import cvdexplorer.metric.MetricKind;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetBoolean;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetEnum;
import xyz.marsavic.drawingfx.gadgets.annotations.GadgetInteger;
import xyz.marsavic.drawingfx.gadgets.annotations.Properties;
import cvdexplorer.geometry.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SceneState {
    public static final int MAX_CLUSTERS = SceneLimits.MAX_CLUSTERS;
    public static final int MAX_MEMBERS_PER_CLUSTER = SceneLimits.MAX_MEMBERS_PER_CLUSTER;

    @GadgetEnum(enumClass = GalleryExample.class)
    @Properties(name = "Examples")
    public GalleryExample galleryExample = GalleryExample.CURRENT;

    @GadgetInteger(min = 1, max = MAX_CLUSTERS)
    @Properties(name = "Number of clusters (Shift+a/d)")
    public int numberOfClusters = 1;

    @GadgetInteger(min = 1, max = MAX_MEMBERS_PER_CLUSTER)
    @Properties(name = "Members in selected cluster (a/d)")
    public int targetPointCountForActiveCluster = 1;

    @GadgetEnum(enumClass = SiteMemberKind.class)
    @Properties(name = "New member type")
    public SiteMemberKind siteMemberKind = SiteMemberKind.POINT;

    @GadgetEnum(enumClass = MetricKind.class)
    @Properties(name = "Metric")
    public MetricKind metricKind = MetricKind.MINIMUM_DISTANCE;

    @GadgetEnum(enumClass = NeighborOrder.class)
    @Properties(name = "Neighbor order")
    public NeighborOrder neighborOrder = NeighborOrder.NEAREST;

    @GadgetInteger(min = 1, max = MAX_MEMBERS_PER_CLUSTER)
    @Properties(name = "Metric parameter k")
    public int nearestNeighborK = 1;

    @GadgetBoolean
    @Properties(name = "Show colored regions (c)")
    public boolean showDiagram = true;

    /** Whether to draw the boundaries between regions controlled by different members within the same cluster. */
    @GadgetBoolean
    @Properties(name = "Show region subdivision (v)")
    public boolean showRegionSubdivision = false;

    @GadgetBoolean
    @Properties(name = "Show members (m)")
    public boolean showMembers = true;

    @GadgetBoolean
    @Properties(name = "Show skeleton (k)")
    public boolean showSkeleton = true;

    @GadgetBoolean
    @Properties(name = "Show help (h)")
    public boolean showHelp = false;

    @GadgetBoolean
    @Properties(name = "Snap to grid (g)")
    public boolean snapToGrid = false;

    @GadgetBoolean
    @Properties(name = "Snap to handles (f)")
    public boolean snapToHandles = false;

    @GadgetBoolean
    @Properties(name = "Distance shading (s)")
    public boolean showShading = false;

    @GadgetBoolean
    @Properties(name = "Fast draw preview")
    public boolean fastDrawPreview = true;

    private final List<ClusterSite> clusters = new ArrayList<>();
    private final Rgba backgroundColor = Rgba.gray(0.92);

    private int lastMemberSyncClusterIndex = -1;

    public static SceneState demo() {
        SceneSnapshot snap = DemoScenes.defaultSnapshot();
        SceneState state = new SceneState();
        state.applyLoadedScene(
                snap.metricKind(),
                snap.neighborOrder(),
                snap.siteMemberKind(),
                snap.clusters(),
                snap.nearestNeighborK()
        );
        state.targetPointCountForActiveCluster = state.clusters.get(0).size();
        state.lastMemberSyncClusterIndex = -1;
        return state;
    }

    public List<ClusterSite> clusters() {
        return clusters;
    }

    public Rgba backgroundColor() {
        return backgroundColor;
    }

    public int clusterCount() {
        return clusters.size();
    }

    /** Minimum {@link ClusterSite#size()} across clusters; 0 if there are no clusters. */
    public int minMemberCountAcrossClusters() {
        return clusters.stream().mapToInt(ClusterSite::size).min().orElse(0);
    }

    /** Keeps {@link #nearestNeighborK} in {@code [1, min cluster size]} (and gadget max). */
    public void clampNearestNeighborK() {
        int minSize = minMemberCountAcrossClusters();
        if (minSize < 1) {
            nearestNeighborK = 1;
            return;
        }
        nearestNeighborK = Math.max(1, Math.min(nearestNeighborK, Math.min(minSize, MAX_MEMBERS_PER_CLUSTER)));
    }

    /** Authoring fields + clusters for JSON (no UI gadgets / view toggles). */
    public SceneSnapshot toSnapshot() {
        SceneSnapshot snapshot = new SceneSnapshot();
        snapshot.setMetricKind(metricKind);
        snapshot.setNeighborOrder(neighborOrder);
        snapshot.setSiteMemberKind(siteMemberKind);
        snapshot.setNearestNeighborK(nearestNeighborK);
        snapshot.setClusters(clusters);
        return snapshot;
    }

    /**
     * Replaces all clusters and metric authoring fields from a loaded file.
     * View toggles and camera are unchanged. Gadget fields are normalized to the new cluster list.
     */
    public void applyLoadedScene(
            MetricKind newMetricKind,
            NeighborOrder newNeighborOrder,
            SiteMemberKind newSiteMemberKind,
            List<ClusterSite> newClusters,
            int loadedNearestNeighborK
    ) {
        if (newClusters.isEmpty() || newClusters.size() > MAX_CLUSTERS) {
            throw new IllegalArgumentException("Cluster list must be non-empty and at most " + MAX_CLUSTERS);
        }
        metricKind = newMetricKind;
        neighborOrder = newNeighborOrder;
        siteMemberKind = newSiteMemberKind;
        nearestNeighborK = loadedNearestNeighborK;
        clusters.clear();
        for (ClusterSite site : newClusters) {
            clusters.add(site);
        }
        numberOfClusters = clusters.size();
        targetPointCountForActiveCluster = clusters.get(0).size();
        lastMemberSyncClusterIndex = -1;
        clampNearestNeighborK();
    }

    public void copyFrom(SceneState other) {
        metricKind = other.metricKind;
        neighborOrder = other.neighborOrder;
        nearestNeighborK = other.nearestNeighborK;
        galleryExample = other.galleryExample;
        showDiagram = other.showDiagram;
        showRegionSubdivision = other.showRegionSubdivision;
        showMembers = other.showMembers;
        showSkeleton = other.showSkeleton;
        showHelp = other.showHelp;
        snapToGrid = other.snapToGrid;
        snapToHandles = other.snapToHandles;
        showShading = other.showShading;
        fastDrawPreview = other.fastDrawPreview;
        siteMemberKind = other.siteMemberKind;
        numberOfClusters = other.numberOfClusters;
        targetPointCountForActiveCluster = other.targetPointCountForActiveCluster;
        lastMemberSyncClusterIndex = -1;

        clusters.clear();
        for (ClusterSite cluster : other.clusters) {
            clusters.add(new ClusterSite(cluster.name(), cluster.color(), cluster.members()));
        }
        if (numberOfClusters != clusters.size()) {
            numberOfClusters = clusters.size();
        }
    }

    /**
     * Removes the cluster at {@code index}. Updates {@link #numberOfClusters}.
     * No-op (returns {@code false}) if fewer than two clusters or {@code index} is out of range.
     */
    public boolean removeClusterAt(int index) {
        if (clusters.size() <= 1 || index < 0 || index >= clusters.size()) {
            return false;
        }
        clusters.remove(index);
        numberOfClusters = clusters.size();
        lastMemberSyncClusterIndex = -1;
        return true;
    }

    /**
     * Grows the cluster list to match {@link #numberOfClusters}. Does not shrink;
     * the app removes a selected cluster when the count decreases.
     */
    public Optional<String> ensureClusterCountMatchesGadget() {
        numberOfClusters = Math.max(1, Math.min(MAX_CLUSTERS, numberOfClusters));
        while (clusters.size() < numberOfClusters) {
            Optional<String> invalidNewMember = MetricMemberCompatibility.invalidNewMemberMessage(metricKind, siteMemberKind);
            if (invalidNewMember.isPresent()) {
                numberOfClusters = clusters.size();
                return invalidNewMember;
            }
            clusters.add(defaultCluster(clusters.size()));
        }
        if (numberOfClusters > clusters.size()) {
            numberOfClusters = clusters.size();
        }
        return Optional.empty();
    }

    /**
     * Grows member count for the given cluster to match {@link #targetPointCountForActiveCluster}.
     * Does not shrink; the app removes the selected member when the count decreases.
     */
    public Optional<String> ensureMemberCountForCluster(int clusterIndex) {
        if (clusters.isEmpty() || clusterIndex < 0 || clusterIndex >= clusters.size()) {
            return Optional.empty();
        }

        targetPointCountForActiveCluster = Math.max(
                1,
                Math.min(MAX_MEMBERS_PER_CLUSTER, targetPointCountForActiveCluster)
        );

        if (clusterIndex != lastMemberSyncClusterIndex) {
            // Switching clusters adopts that cluster's size; first sync after reset keeps the gadget value.
            if (lastMemberSyncClusterIndex >= 0) {
                targetPointCountForActiveCluster = clusters.get(clusterIndex).size();
            }
            lastMemberSyncClusterIndex = clusterIndex;
        }

        ClusterSite cluster = clusters.get(clusterIndex);

        while (cluster.size() < targetPointCountForActiveCluster) {
            Optional<String> invalidNewMember = MetricMemberCompatibility.invalidNewMemberMessage(metricKind, siteMemberKind);
            if (invalidNewMember.isPresent()) {
                targetPointCountForActiveCluster = cluster.size();
                return invalidNewMember;
            }
            Vector hint = jitteredNewMemberHint(cluster, clusterIndex, cluster.size());
            cluster.addMember(SiteMemberFactory.createDefault(siteMemberKind, clusterIndex, cluster.size(), hint));
        }
        return Optional.empty();
    }

    /** Resets member-count gadget sync so the next selection adopts the cluster's real size. */
    public void resetMemberCountGadgetSync() {
        lastMemberSyncClusterIndex = -1;
    }

    private static Vector centroidOfMembers(List<ClusterMember> members) {
        double sx = 0.0;
        double sy = 0.0;
        for (ClusterMember m : members) {
            Vector c = m.placementCentroid();
            sx += c.x();
            sy += c.y();
        }
        int n = members.size();
        return Vector.xy(sx / n, sy / n);
    }

    private Vector jitteredNewMemberHint(ClusterSite cluster, int clusterIndex, int newMemberIndex) {
        List<ClusterMember> members = cluster.members();
        Vector c;
        if (members.isEmpty()) {
            double x = -280 + (clusterIndex % 5) * 140;
            double y = -200 + (clusterIndex / 5) * 140;
            c = Vector.xy(x, y);
        } else {
            c = centroidOfMembers(members);
        }
        double angle = 2 * Math.PI * (newMemberIndex * 0.618033988749895);
        double radius = 18.0 + 6.0 * (newMemberIndex % 7);
        return c.add(Vector.polar(radius, angle));
    }

    private ClusterSite defaultCluster(int index) {
        double hue = (360 * index * 0.618033988749895) % 360;
        Rgba color = Rgba.hsb(hue, 0.65, 0.95);
        double x = -280 + (index % 5) * 140;
        double y = -200 + (index / 5) * 140;
        Vector center = Vector.xy(x, y);
        ClusterMember first = SiteMemberFactory.createDefault(siteMemberKind, index, 0, center);
        return new ClusterSite(
                ClusterNaming.forNewCluster(index),
                color,
                List.of(first)
        );
    }
}
