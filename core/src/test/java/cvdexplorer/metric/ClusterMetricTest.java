package cvdexplorer.metric;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.PolygonMember;
import cvdexplorer.model.SegmentMember;
import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterMetricTest {
    @Test
    void minimumDistanceUsesClosestPointInCluster() {
        ClusterMetric metric = new NearestMemberMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(Math.sqrt(80), score, 1.0e-9);
    }

    @Test
    void maximumDistanceUsesMostDistantPointInCluster() {
        ClusterMetric metric = new FarthestMemberMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(10.0, score, 1.0e-9);
    }

    @Test
    void sumOfDistancesAddsAllMemberDistances() {
        ClusterMetric metric = new SumOfDistancesMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(10.0 + Math.sqrt(80), score, 1.0e-9);
    }

    @Test
    void meanOfDistancesIsMeanOfMemberDistances() {
        ClusterMetric metric = new MeanOfDistancesMetric();
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals((10.0 + Math.sqrt(80)) / 2.0, score, 1.0e-9);
    }

    @Test
    void kthNearestWithK1MatchesNearestMemberDistance() {
        ClusterMetric metric = new KthNearestMemberDistanceMetric(1);
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(Math.sqrt(80), score, 1.0e-9);
    }

    @Test
    void kthNearestWithK2UsesFartherOfTwoPoints() {
        ClusterMetric metric = new KthNearestMemberDistanceMetric(2);
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(10, 0))
        );

        double score = metric.score(Vector.xy(6, 8), members);

        assertEquals(10.0, score, 1.0e-9);
    }

    @Test
    void kthNearestWithK3UsesThirdSmallestDistance() {
        ClusterMetric metric = new KthNearestMemberDistanceMetric(3);
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new PointMember(Vector.xy(0, 3)),
                new PointMember(Vector.xy(0, 6))
        );

        double score = metric.score(Vector.xy(0, 1), members);

        assertEquals(5.0, score, 1.0e-9);
    }

    @Test
    void minimumDistanceUsesCircleCircumferenceDistance() {
        ClusterMetric metric = new NearestMemberMetric();
        List<ClusterMember> members = List.of(
                new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0))
        );

        double score = metric.score(Vector.xy(0, 1), members);

        assertEquals(2.0, score, 1.0e-9);
    }

    @Test
    void aggregatesTwoSegmentsByMemberDistance() {
        List<ClusterMember> members = List.of(
                new SegmentMember(Vector.xy(0, 0), Vector.xy(10, 0)),
                new SegmentMember(Vector.xy(20, 0), Vector.xy(30, 0))
        );
        assertTwoMemberAggregate(members, Vector.xy(5, 4), 4.0, Math.sqrt(241), 1.0e-9);
    }

    @Test
    void aggregatesTwoCirclesByMemberDistance() {
        List<ClusterMember> members = List.of(
                new CircleMember(Vector.xy(0, 0), Vector.xy(5, 0)),
                new CircleMember(Vector.xy(20, 0), Vector.xy(22, 0))
        );
        assertTwoMemberAggregate(members, Vector.xy(0, 0), 5.0, 18.0, 1.0e-9);
    }

    @Test
    void aggregatesTwoEllipsesByMemberDistance() {
        double b = Math.sqrt(125);
        List<ClusterMember> members = List.of(
                new EllipseMember(Vector.xy(-10, 0), Vector.xy(10, 0), Vector.xy(0, b)),
                new EllipseMember(Vector.xy(30, 0), Vector.xy(50, 0), Vector.xy(40, b))
        );
        assertTwoMemberAggregate(members, Vector.xy(0, 0), b, 25.0, 1.0e-6);
    }

    @Test
    void aggregatesTwoLinesByMemberDistance() {
        List<ClusterMember> members = List.of(
                new LineMember(Vector.xy(0, 0), Vector.xy(10, 0)),
                new LineMember(Vector.xy(0, 0), Vector.xy(0, 10))
        );
        assertTwoMemberAggregate(members, Vector.xy(0, 4), 4.0, 0.0, 1.0e-9);
    }

    @Test
    void aggregatesTwoPolygonsAsSingleMembers() {
        List<ClusterMember> members = List.of(
                new PolygonMember(List.of(
                        Vector.xy(0, 0), Vector.xy(4, 0), Vector.xy(4, 4), Vector.xy(0, 4)
                )),
                new PolygonMember(List.of(
                        Vector.xy(20, 0), Vector.xy(24, 0), Vector.xy(24, 4), Vector.xy(20, 4)
                ))
        );
        assertTwoMemberAggregate(members, Vector.xy(2, -2), 2.0, Math.hypot(18, 2), 1.0e-9);
    }

    @Test
    void aggregatesMixedMemberCluster() {
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new CircleMember(Vector.xy(10, 0), Vector.xy(12, 0)),
                new SegmentMember(Vector.xy(0, 5), Vector.xy(10, 5))
        );
        Vector query = Vector.xy(0, 0);

        assertEquals(13.0, new SumOfDistancesMetric().score(query, members), 1.0e-9);
        assertEquals(13.0 / 3.0, new MeanOfDistancesMetric().score(query, members), 1.0e-9);

        ClusterMetric.Result k1 = new KthNearestMemberDistanceMetric(1).evaluate(query, members);
        ClusterMetric.Result k2 = new KthNearestMemberDistanceMetric(2).evaluate(query, members);
        ClusterMetric.Result k3 = new KthNearestMemberDistanceMetric(3).evaluate(query, members);
        assertEquals(0.0, k1.score(), 1.0e-9);
        assertEquals(0, k1.memberIndex());
        assertEquals(5.0, k2.score(), 1.0e-9);
        assertEquals(2, k2.memberIndex());
        assertEquals(8.0, k3.score(), 1.0e-9);
        assertEquals(1, k3.memberIndex());
    }

    @Test
    void emptyClusterScoresAreInfinite() {
        List<ClusterMember> members = List.of();
        Vector query = Vector.xy(1, 1);

        assertEquals(Double.POSITIVE_INFINITY, new SumOfDistancesMetric().evaluate(query, members).score());
        assertEquals(-1, new SumOfDistancesMetric().evaluate(query, members).memberIndex());
        assertEquals(Double.POSITIVE_INFINITY, new MeanOfDistancesMetric().evaluate(query, members).score());
        assertEquals(-1, new MeanOfDistancesMetric().evaluate(query, members).memberIndex());
        assertEquals(Double.POSITIVE_INFINITY, new KthNearestMemberDistanceMetric(1).evaluate(query, members).score());
        assertEquals(-1, new KthNearestMemberDistanceMetric(1).evaluate(query, members).memberIndex());
    }

    @Test
    void kthRejectsNonpositiveRank() {
        IllegalArgumentException zero = assertThrows(
                IllegalArgumentException.class,
                () -> new KthNearestMemberDistanceMetric(0)
        );
        IllegalArgumentException negative = assertThrows(
                IllegalArgumentException.class,
                () -> new KthNearestMemberDistanceMetric(-3)
        );
        assertTrue(zero.getMessage().contains("at least 1"));
        assertTrue(negative.getMessage().contains("at least 1"));
    }

    @Test
    void kthBeyondMemberCountIsInfinite() {
        List<ClusterMember> members = List.of(
                new PointMember(Vector.xy(0, 0)),
                new CircleMember(Vector.xy(4, 0), Vector.xy(5, 0))
        );
        ClusterMetric.Result result = new KthNearestMemberDistanceMetric(3).evaluate(Vector.xy(0, 0), members);

        assertEquals(Double.POSITIVE_INFINITY, result.score());
        assertEquals(-1, result.memberIndex());
    }

    @Test
    void tiedDistancesKeepEarlierMemberFirst() {
        List<ClusterMember> members = List.of(
                new CircleMember(Vector.xy(0, 0), Vector.xy(1, 0)),
                new CircleMember(Vector.xy(10, 0), Vector.xy(11, 0))
        );
        Vector query = Vector.xy(5, 0);
        ClusterMetric.Result k1 = new KthNearestMemberDistanceMetric(1).evaluate(query, members);
        ClusterMetric.Result k2 = new KthNearestMemberDistanceMetric(2).evaluate(query, members);
        ClusterMetric.Result farthest = new FarthestMemberMetric().evaluate(query, members);

        assertEquals(4.0, k1.score(), 1.0e-9);
        assertEquals(0, k1.memberIndex());
        assertEquals(4.0, k2.score(), 1.0e-9);
        assertEquals(1, k2.memberIndex());
        assertEquals(k2.score(), farthest.score(), 1.0e-9);
        assertEquals(0, farthest.memberIndex());
    }

    private static void assertTwoMemberAggregate(
            List<ClusterMember> members,
            Vector query,
            double firstDistance,
            double secondDistance,
            double tolerance
    ) {
        double sum = firstDistance + secondDistance;
        assertEquals(sum, new SumOfDistancesMetric().score(query, members), tolerance);
        assertEquals(sum / 2.0, new MeanOfDistancesMetric().score(query, members), tolerance);

        ClusterMetric.Result nearest = new NearestMemberMetric().evaluate(query, members);
        ClusterMetric.Result farthest = new FarthestMemberMetric().evaluate(query, members);
        ClusterMetric.Result k1 = new KthNearestMemberDistanceMetric(1).evaluate(query, members);
        ClusterMetric.Result k2 = new KthNearestMemberDistanceMetric(2).evaluate(query, members);

        assertEquals(nearest.score(), k1.score(), tolerance);
        assertEquals(nearest.memberIndex(), k1.memberIndex());
        assertEquals(farthest.score(), k2.score(), tolerance);
        assertEquals(Math.min(firstDistance, secondDistance), k1.score(), tolerance);
        assertEquals(Math.max(firstDistance, secondDistance), k2.score(), tolerance);
        if (firstDistance != secondDistance) {
            int nearestIndex = firstDistance < secondDistance ? 0 : 1;
            assertEquals(nearestIndex, k1.memberIndex());
            assertEquals(1 - nearestIndex, k2.memberIndex());
        }
    }
}
