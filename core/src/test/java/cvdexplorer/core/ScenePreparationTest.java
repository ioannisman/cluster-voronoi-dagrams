package cvdexplorer.core;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenePreparationTest {
    @Test
    void prepareUsesNearestNeighborOrderByDefault() {
        List<ClusterSite> clusters = basePointClusters();

        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(
                clusters,
                MetricKind.MINIMUM_DISTANCE,
                NeighborOrder.NEAREST,
                1
        );
        ClusterOwnershipSelector.Result ownership = preparedScene.ownershipSelector().selectOwner(
                Vector.xy(2, 0),
                preparedScene.clusters(),
                preparedScene.metric()
        );

        assertEquals(0, ownership.clusterIndex());
    }

    @Test
    void prepareUsesFarthestNeighborOrderWhenRequested() {
        List<ClusterSite> clusters = basePointClusters();

        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(
                clusters,
                MetricKind.MINIMUM_DISTANCE,
                NeighborOrder.FARTHEST,
                1
        );
        ClusterOwnershipSelector.Result ownership = preparedScene.ownershipSelector().selectOwner(
                Vector.xy(2, 0),
                preparedScene.clusters(),
                preparedScene.metric()
        );

        assertEquals(1, ownership.clusterIndex());
    }

    @Test
    void nonPointSumSelectsNearestAndFarthestClusters() {
        List<ClusterSite> clusters = List.of(
                new ClusterSite("A", Rgba.RED, List.of(
                        new CircleMember(Vector.xy(0, 0), Vector.xy(1, 0)),
                        new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0))
                )),
                new ClusterSite("B", Rgba.BLUE, List.of(
                        new CircleMember(Vector.xy(6, 0), Vector.xy(7, 0)),
                        new CircleMember(Vector.xy(6, 0), Vector.xy(8, 0))
                ))
        );
        Vector query = Vector.xy(10, 0);

        ClusterOwnershipSelector.Result nearest = ownership(
                clusters,
                MetricKind.SUM_OF_DISTANCES,
                NeighborOrder.NEAREST,
                1,
                query
        );
        assertEquals(1, nearest.clusterIndex());
        assertEquals(5.0, nearest.score(), 1.0e-9);

        ClusterOwnershipSelector.Result farthest = ownership(
                clusters,
                MetricKind.SUM_OF_DISTANCES,
                NeighborOrder.FARTHEST,
                1,
                query
        );
        assertEquals(0, farthest.clusterIndex());
        assertEquals(16.0, farthest.score(), 1.0e-9);
    }

    @Test
    void kthClampsToSmallestClusterInsteadOfInfinity() {
        List<ClusterSite> clusters = List.of(
                new ClusterSite("A", Rgba.RED, List.of(
                        new CircleMember(Vector.xy(0, 0), Vector.xy(1, 0))
                )),
                new ClusterSite("B", Rgba.BLUE, List.of(
                        new CircleMember(Vector.xy(100, 0), Vector.xy(101, 0)),
                        new CircleMember(Vector.xy(110, 0), Vector.xy(111, 0)),
                        new CircleMember(Vector.xy(120, 0), Vector.xy(121, 0))
                ))
        );
        Vector query = Vector.xy(10, 0);

        ClusterOwnershipSelector.Result nearest = ownership(
                clusters,
                MetricKind.KTH_NEAREST_DISTANCE,
                NeighborOrder.NEAREST,
                5,
                query
        );
        assertEquals(0, nearest.clusterIndex());
        assertEquals(9.0, nearest.score(), 1.0e-9);

        ClusterOwnershipSelector.Result farthest = ownership(
                clusters,
                MetricKind.KTH_NEAREST_DISTANCE,
                NeighborOrder.FARTHEST,
                5,
                query
        );
        assertEquals(1, farthest.clusterIndex());
        assertEquals(89.0, farthest.score(), 1.0e-9);
    }

    @Test
    void unequalClusterSizesMakeSumAndMeanSelectDifferentOwners() {
        Vector query = Vector.xy(0, 0);
        List<ClusterSite> clusters = List.of(
                new ClusterSite("A", Rgba.RED, List.of(
                        circleAtQuery(query, 2),
                        circleAtQuery(query, 3),
                        circleAtQuery(query, 4)
                )),
                new ClusterSite("B", Rgba.BLUE, List.of(
                        circleAtQuery(query, 5)
                ))
        );

        ClusterOwnershipSelector.Result nearestSum = ownership(
                clusters, MetricKind.SUM_OF_DISTANCES, NeighborOrder.NEAREST, 1, query
        );
        assertEquals(1, nearestSum.clusterIndex());
        assertEquals(5.0, nearestSum.score(), 1.0e-9);

        ClusterOwnershipSelector.Result nearestMean = ownership(
                clusters, MetricKind.MEAN_DISTANCE, NeighborOrder.NEAREST, 1, query
        );
        assertEquals(0, nearestMean.clusterIndex());
        assertEquals(3.0, nearestMean.score(), 1.0e-9);

        ClusterOwnershipSelector.Result farthestSum = ownership(
                clusters, MetricKind.SUM_OF_DISTANCES, NeighborOrder.FARTHEST, 1, query
        );
        assertEquals(0, farthestSum.clusterIndex());
        assertEquals(9.0, farthestSum.score(), 1.0e-9);

        ClusterOwnershipSelector.Result farthestMean = ownership(
                clusters, MetricKind.MEAN_DISTANCE, NeighborOrder.FARTHEST, 1, query
        );
        assertEquals(1, farthestMean.clusterIndex());
        assertEquals(5.0, farthestMean.score(), 1.0e-9);
    }

    private static ClusterOwnershipSelector.Result ownership(
            List<ClusterSite> clusters,
            MetricKind metricKind,
            NeighborOrder neighborOrder,
            int metricParameter,
            Vector query
    ) {
        ScenePreparation.PreparedScene preparedScene = ScenePreparation.prepare(
                clusters,
                metricKind,
                neighborOrder,
                metricParameter
        );
        return preparedScene.ownershipSelector().selectOwner(
                query,
                preparedScene.clusters(),
                preparedScene.metric()
        );
    }

    private static List<ClusterSite> basePointClusters() {
        return List.of(
                clusterAt("A", 0, 0, Rgba.RED),
                clusterAt("B", 10, 0, Rgba.BLUE)
        );
    }

    private static ClusterSite clusterAt(String name, double x, double y, Rgba color) {
        return new ClusterSite(name, color, List.of(new PointMember(Vector.xy(x, y))));
    }

    private static CircleMember circleAtQuery(Vector query, double radius) {
        return new CircleMember(query, query.add(Vector.xy(radius, 0)));
    }
}
