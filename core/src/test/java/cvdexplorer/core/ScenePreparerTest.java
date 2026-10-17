package cvdexplorer.core;

import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenePreparerTest {

    @Test
    void pointScenesStayOnTheStaticPreparationPath() throws Exception {
        List<ClusterSite> clusters = List.of(new ClusterSite(
                "A", Rgba.RED, List.of(new PointMember(Vector.xy(0, 0)))
        ));
        ScenePreparer preparer = new ScenePreparer();
        ScenePreparation.PreparedScene prepared = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, box(-10, -10, 10, 10)
        );
        assertNull(prepared.domain());
        assertSame(clusters.get(0), prepared.clusters().get(0));
        assertNull(preparer.currentDomain());
    }

    @Test
    void staticPrepareRejectsAnUnpreparedKEllipse() {
        List<ClusterSite> clusters = List.of(new ClusterSite(
                "A", Rgba.RED, List.of(ellipseAt(0, 0))
        ));
        assertThrows(IllegalArgumentException.class, () -> ScenePreparation.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1
        ));
    }

    @Test
    void preparedSceneKeepsOrderNamesAndForeignMembers() throws Exception {
        PointMember point = new PointMember(Vector.xy(3, 4));
        KEllipseMember ellipse = ellipseAt(0, 0);
        ClusterSite first = new ClusterSite("Amber", Rgba.RED, List.of(point, ellipse));
        ClusterSite second = new ClusterSite("Azure", Rgba.BLUE, List.of(new PointMember(Vector.xy(30, 0))));
        ScenePreparer preparer = new ScenePreparer();
        ScenePreparation.PreparedScene prepared = preparer.prepare(
                List.of(first, second),
                MetricKind.MINIMUM_DISTANCE,
                NeighborOrder.NEAREST,
                1,
                box(-30, -30, 40, 40)
        );
        assertEquals(2, prepared.clusters().size());
        assertEquals("Amber", prepared.clusters().get(0).name());
        assertEquals("Azure", prepared.clusters().get(1).name());
        assertSame(first.color(), prepared.clusters().get(0).color());
        assertSame(point, prepared.clusters().get(0).members().get(0));
        PreparedKEllipseMember preparedEllipse = assertInstanceOf(
                PreparedKEllipseMember.class,
                prepared.clusters().get(0).members().get(1)
        );
        assertSame(ellipse, preparedEllipse.source());
        assertSame(second.members().get(0), prepared.clusters().get(1).members().get(0));
        assertTrue(prepared.domain().covers(box(-30, -30, 40, 40)));
        assertThrows(UnsupportedOperationException.class, () -> preparedEllipse.withHandle(0, Vector.ZERO));
    }

    @Test
    void oneKEllipseContributesOneDistanceToEveryMetric() throws Exception {
        PointMember point = new PointMember(Vector.xy(0, 0));
        KEllipseMember ellipse = ellipseAt(12, 0);
        List<ClusterSite> clusters = List.of(new ClusterSite("A", Rgba.RED, List.of(point, ellipse)));
        Vector query = Vector.xy(1, 0);
        ScenePreparer preparer = new ScenePreparer();
        ScenePreparation.PreparedScene prepared = preparer.prepare(
                clusters, MetricKind.SUM_OF_DISTANCES, NeighborOrder.NEAREST, 1, box(-20, -20, 40, 40)
        );
        ClusterMember preparedPoint = prepared.clusters().get(0).members().get(0);
        ClusterMember preparedEllipse = prepared.clusters().get(0).members().get(1);
        double pointDistance = preparedPoint.distanceTo(query);
        double ellipseDistance = preparedEllipse.distanceTo(query);
        assertEquals(1.0, pointDistance, 1e-9);
        assertTrue(ellipseDistance > 0.0);
        assertEquals(2, prepared.clusters().get(0).members().size());

        assertEquals(pointDistance + ellipseDistance, score(preparer, clusters, MetricKind.SUM_OF_DISTANCES, 1, query), 1e-9);
        assertEquals((pointDistance + ellipseDistance) / 2.0, score(preparer, clusters, MetricKind.MEAN_DISTANCE, 1, query), 1e-9);
        assertEquals(Math.min(pointDistance, ellipseDistance), score(preparer, clusters, MetricKind.MINIMUM_DISTANCE, 1, query), 1e-9);
        assertEquals(Math.max(pointDistance, ellipseDistance), score(preparer, clusters, MetricKind.MAXIMUM_DISTANCE, 1, query), 1e-9);
        assertEquals(Math.max(pointDistance, ellipseDistance), score(preparer, clusters, MetricKind.KTH_NEAREST_DISTANCE, 2, query), 1e-9);
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());
    }

    private static double score(
            ScenePreparer preparer,
            List<ClusterSite> clusters,
            MetricKind metricKind,
            int metricParameter,
            Vector query
    ) throws Exception {
        ScenePreparation.PreparedScene prepared = preparer.prepare(
                clusters, metricKind, NeighborOrder.NEAREST, metricParameter, box(-20, -20, 40, 40)
        );
        return prepared.metric().score(query, prepared.clusters().get(0));
    }

    private static KEllipseMember ellipseAt(double x, double y) {
        return new KEllipseMember(
                List.of(Vector.xy(x - 2, y), Vector.xy(x + 2, y), Vector.xy(x, y + 2)),
                Vector.xy(x, y + 12)
        );
    }

    private static Box box(double minX, double minY, double maxX, double maxY) {
        return Box.pq(Vector.xy(minX, minY), Vector.xy(maxX, maxY));
    }
}
