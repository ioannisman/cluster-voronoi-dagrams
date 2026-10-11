package cvdexplorer.model;

import cvdexplorer.metric.MetricKind;
import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneStateTest {

    @Test
    void minMemberCountAcrossClustersIsMinimumOfSizes() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("A", Rgba.RED, List.of(new PointMember(Vector.xy(0, 0)))));
        state.clusters().add(new ClusterSite(
                "B",
                Rgba.BLUE,
                List.of(
                        new PointMember(Vector.xy(1, 0)),
                        new PointMember(Vector.xy(2, 0)),
                        new PointMember(Vector.xy(3, 0))
                )
        ));
        assertEquals(1, state.minMemberCountAcrossClusters());
    }

    @Test
    void minMemberCountAcrossClustersIsZeroWhenNoClusters() {
        SceneState state = new SceneState();
        state.clusters().clear();
        assertEquals(0, state.minMemberCountAcrossClusters());
    }

    @Test
    void clampMetricParameterSetsOneWhenNoMembers() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.metricParameter = 5;
        state.clampMetricParameter();
        assertEquals(1, state.metricParameter);
    }

    @Test
    void clampMetricParameterRespectsSmallestClusterSize() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("A", Rgba.RED, List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(1, 0)),
                new PointMember(Vector.xy(2, 0))
        )));
        state.clusters().add(new ClusterSite("B", Rgba.GREEN, List.of(
                new PointMember(Vector.xy(0, 1)),
                new PointMember(Vector.xy(1, 1))
        )));
        state.metricParameter = 100;
        state.clampMetricParameter();
        assertEquals(2, state.metricParameter);
    }

    @Test
    void applyLoadedSceneReplacesClustersAndClampsK() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("Old", Rgba.BLACK, List.of(new PointMember(Vector.xy(0, 0)))));

        List<ClusterSite> incoming = List.of(
                new ClusterSite("C1", Rgba.RED, List.of(
                        new PointMember(Vector.xy(1, 1)),
                        new PointMember(Vector.xy(2, 2))
                )),
                new ClusterSite("C2", Rgba.BLUE, List.of(new PointMember(Vector.xy(3, 3))))
        );
        state.applyLoadedScene(
                MetricKind.MINIMUM_DISTANCE,
                NeighborOrder.NEAREST,
                SiteMemberKind.POINT,
                incoming,
                99,
                3
        );
        assertEquals(2, state.clusterCount());
        assertEquals(1, state.metricParameter);
        assertEquals(2, state.targetPointCountForActiveCluster);
    }

    @Test
    void applyLoadedSceneRejectsEmptyOrTooManyClusters() {
        SceneState state = new SceneState();
        assertThrows(IllegalArgumentException.class, () ->
                state.applyLoadedScene(
                        MetricKind.MINIMUM_DISTANCE,
                        NeighborOrder.NEAREST,
                        SiteMemberKind.POINT,
                        List.of(),
                        1,
                3
                )
        );
        List<ClusterSite> tooMany = new java.util.ArrayList<>();
        for (int i = 0; i < SceneState.MAX_CLUSTERS + 1; i++) {
            tooMany.add(new ClusterSite("C" + i, Rgba.RED, List.of(new PointMember(Vector.xy(i, 0)))));
        }
        assertThrows(IllegalArgumentException.class, () ->
                state.applyLoadedScene(
                        MetricKind.MINIMUM_DISTANCE,
                        NeighborOrder.NEAREST,
                        SiteMemberKind.POINT,
                        tooMany,
                        1,
                3
                )
        );
    }

    @Test
    void removeClusterAtRemovesGivenIndexNotLast() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("A", Rgba.RED, List.of(new PointMember(Vector.xy(0, 0)))));
        state.clusters().add(new ClusterSite("B", Rgba.GREEN, List.of(new PointMember(Vector.xy(1, 0)))));
        state.clusters().add(new ClusterSite("C", Rgba.BLUE, List.of(new PointMember(Vector.xy(2, 0)))));
        state.numberOfClusters = 3;

        assertTrue(state.removeClusterAt(1));

        assertEquals(2, state.clusters().size());
        assertEquals("A", state.clusters().get(0).name());
        assertEquals("C", state.clusters().get(1).name());
        assertEquals(2, state.numberOfClusters);
    }

    @Test
    void ensureClusterCountMatchesGadgetGrowsButDoesNotShrink() {
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(new ClusterSite("A", Rgba.RED, List.of(new PointMember(Vector.xy(0, 0)))));
        state.clusters().add(new ClusterSite("B", Rgba.GREEN, List.of(new PointMember(Vector.xy(1, 0)))));
        state.clusters().add(new ClusterSite("C", Rgba.BLUE, List.of(new PointMember(Vector.xy(2, 0)))));
        state.numberOfClusters = 2;

        state.ensureClusterCountMatchesGadget();

        assertEquals(3, state.clusters().size());
        assertEquals(2, state.numberOfClusters);
    }

    @Test
    void ensureClusterCountMatchesGadgetAddsLineUnderSumOfDistances() {
        ClusterSite first = new ClusterSite("A", Rgba.ORANGE, List.of(new PointMember(Vector.xy(0, 0))));
        SceneState state = new SceneState();
        state.clusters().clear();
        state.clusters().add(first);
        state.numberOfClusters = 1;
        state.metricKind = MetricKind.SUM_OF_DISTANCES;
        state.siteMemberKind = SiteMemberKind.LINE;
        state.numberOfClusters = 2;

        state.ensureClusterCountMatchesGadget();

        assertEquals(2, state.clusters().size());
        assertEquals(2, state.numberOfClusters);
        assertInstanceOf(LineMember.class, state.clusters().get(1).members().get(0));
    }

    @Test
    void ensureMemberCountForClusterAddsCircleUnderMeanDistance() {
        ClusterSite cluster = new ClusterSite("A", Rgba.CYAN, List.of(new PointMember(Vector.xy(10, 10))));
        List<ClusterSite> loaded = List.of(cluster);
        SceneState state = new SceneState();
        state.applyLoadedScene(
                MetricKind.MEAN_DISTANCE,
                NeighborOrder.NEAREST,
                SiteMemberKind.CIRCLE,
                loaded,
                1,
                3
        );
        state.targetPointCountForActiveCluster = 2;

        state.ensureMemberCountForCluster(0);

        assertEquals(2, state.clusters().get(0).size());
        assertEquals(2, state.targetPointCountForActiveCluster);
        assertInstanceOf(CircleMember.class, state.clusters().get(0).members().get(1));
    }
}
