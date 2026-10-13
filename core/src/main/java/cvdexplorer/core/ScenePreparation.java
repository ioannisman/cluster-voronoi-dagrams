package cvdexplorer.core;

import cvdexplorer.field.DistanceGridSpec;
import cvdexplorer.metric.ClusterMetric;
import cvdexplorer.metric.FarthestMemberMetric;
import cvdexplorer.metric.NearestMemberMetric;
import cvdexplorer.metric.KthNearestMemberDistanceMetric;
import cvdexplorer.metric.MeanOfDistancesMetric;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.SumOfDistancesMetric;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.NeighborOrder;

import java.util.List;

public final class ScenePreparation {
    private static final ClusterMetric MINIMUM_DISTANCE = new NearestMemberMetric();
    private static final ClusterMetric MAXIMUM_DISTANCE = new FarthestMemberMetric();
    private static final ClusterMetric SUM_OF_DISTANCES = new SumOfDistancesMetric();
    private static final ClusterMetric MEAN_DISTANCE = new MeanOfDistancesMetric();
    private static final ClusterOwnershipSelector NEAREST_OWNERSHIP = new ClusterOwnershipSelector(true);
    private static final ClusterOwnershipSelector FARTHEST_OWNERSHIP = new ClusterOwnershipSelector(false);

    private ScenePreparation() {
    }

    public static PreparedScene prepare(
            List<ClusterSite> clusters,
            MetricKind metricKind,
            NeighborOrder neighborOrder,
            int metricParameter
    ) {
        if (containsKEllipse(clusters)) {
            throw new IllegalArgumentException("K_ELLIPSE distance requires preparation via ScenePreparer");
        }
        return new PreparedScene(
                List.copyOf(clusters),
                metricFor(clusters, metricKind, metricParameter),
                ownershipSelectorFor(neighborOrder),
                null
        );
    }

    static boolean containsKEllipse(List<ClusterSite> clusters) {
        for (ClusterSite cluster : clusters) {
            for (ClusterMember member : cluster.members()) {
                if (member instanceof KEllipseMember) {
                    return true;
                }
            }
        }
        return false;
    }

    static ClusterMetric metricFor(
            List<ClusterSite> clusters,
            MetricKind metricKind,
            int metricParameter
    ) {
        return switch (metricKind) {
            case MINIMUM_DISTANCE -> MINIMUM_DISTANCE;
            case MAXIMUM_DISTANCE -> MAXIMUM_DISTANCE;
            case SUM_OF_DISTANCES -> SUM_OF_DISTANCES;
            case MEAN_DISTANCE -> MEAN_DISTANCE;
            case KTH_NEAREST_DISTANCE -> {
                int minSize = clusters.stream().mapToInt(ClusterSite::size).min().orElse(0);
                int k = minSize < 1 ? 1 : Math.max(1, Math.min(metricParameter, minSize));
                yield new KthNearestMemberDistanceMetric(k);
            }
        };
    }

    static ClusterOwnershipSelector ownershipSelectorFor(NeighborOrder neighborOrder) {
        return switch (neighborOrder) {
            case NEAREST -> NEAREST_OWNERSHIP;
            case FARTHEST -> FARTHEST_OWNERSHIP;
        };
    }

    public record PreparedScene(
            List<ClusterSite> clusters,
            ClusterMetric metric,
            ClusterOwnershipSelector ownershipSelector,
            DistanceGridSpec domain
    ) {
    }
}
