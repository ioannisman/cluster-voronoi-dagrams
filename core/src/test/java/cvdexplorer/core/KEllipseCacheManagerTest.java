package cvdexplorer.core;

import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import cvdexplorer.kellipse.KEllipseApproximationSettings;
import cvdexplorer.kellipse.KEllipseBoundaryBuilder;
import cvdexplorer.kellipse.KEllipsePreparationException;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KEllipseCacheManagerTest {
    private static final Box VIEW = box(-40, -40, 40, 40);

    @Test
    void cachesByMemberAndRebuildsOnlyWhatChanged() throws Exception {
        ScenePreparer preparer = new ScenePreparer();
        KEllipseMember a = ellipseAt(-10, 0);
        KEllipseMember b = ellipseAt(0, 0);
        KEllipseMember c = ellipseAt(10, 0);
        ClusterSite cluster = new ClusterSite("A", Rgba.RED, List.of(a, b, c));
        List<ClusterSite> clusters = List.of(cluster);

        preparer.prepare(clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW);
        assertEquals(3, preparer.statistics().lastBoundaryBuilds());
        assertEquals(3, preparer.statistics().lastGridBuilds());

        preparer.prepare(clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW);
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());

        preparer.prepare(clusters, MetricKind.SUM_OF_DISTANCES, NeighborOrder.FARTHEST, 2, VIEW);
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());

        ScenePreparation.PreparedScene beforeEdit = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW
        );
        PreparedKEllipseMember preparedB = prepared(beforeEdit, 0, 1);
        PreparedKEllipseMember preparedC = prepared(beforeEdit, 0, 2);
        var boundaryB = preparer.cachedBoundary(b);
        var boundaryC = preparer.cachedBoundary(c);

        KEllipseMember edited = (KEllipseMember) a.withHandle(0, Vector.xy(-12, 1));
        cluster.setMember(0, edited);
        ScenePreparation.PreparedScene afterEdit = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW
        );
        assertEquals(1, preparer.statistics().lastBoundaryBuilds());
        assertEquals(1, preparer.statistics().lastGridBuilds());
        assertSame(preparedB, prepared(afterEdit, 0, 1));
        assertSame(preparedC, prepared(afterEdit, 0, 2));
        assertSame(edited, prepared(afterEdit, 0, 0).source());

        preparer.prepare(clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW);
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());

        long bytesBeforeGrowth = preparer.statistics().totalSampleBytes();
        preparer.prepare(clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, box(-80, -80, 80, 80));
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(3, preparer.statistics().lastGridBuilds());
        assertSame(boundaryB, preparer.cachedBoundary(b));
        assertSame(boundaryC, preparer.cachedBoundary(c));
        assertNull(preparer.cachedBoundary(a));
        assertTrue(preparer.statistics().totalSampleBytes() > bytesBeforeGrowth);

        long bytesBeforeRemoval = preparer.statistics().totalSampleBytes();
        cluster.removeMember(1);
        preparer.prepare(clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, box(-80, -80, 80, 80));
        assertEquals(1, preparer.statistics().lastEvictions());
        assertEquals(0, preparer.statistics().lastGridBuilds());
        assertTrue(preparer.statistics().totalSampleBytes() < bytesBeforeRemoval);
        assertNull(preparer.cachedBoundary(b));
    }

    @Test
    void reorderAndMoveDoNotReuseAnotherMembersGrid() throws Exception {
        ScenePreparer preparer = new ScenePreparer();
        KEllipseMember first = ellipseAt(-6, 0);
        KEllipseMember second = ellipseAt(6, 0);
        ClusterSite alpha = new ClusterSite("Alpha", Rgba.RED, new ArrayList<>(List.of(first, second)));
        ClusterSite beta = new ClusterSite("Beta", Rgba.BLUE, new ArrayList<>(List.of(new PointMember(Vector.xy(0, 20)))));
        List<ClusterSite> clusters = List.of(alpha, beta);

        ScenePreparation.PreparedScene prepared = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW
        );
        PreparedKEllipseMember preparedFirst = prepared(prepared, 0, 0);
        PreparedKEllipseMember preparedSecond = prepared(prepared, 0, 1);

        alpha.setMember(0, second);
        alpha.setMember(1, first);
        ScenePreparation.PreparedScene reordered = preparer.prepare(
                clusters, MetricKind.KTH_NEAREST_DISTANCE, NeighborOrder.FARTHEST, 3, VIEW
        );
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());
        assertSame(preparedSecond, prepared(reordered, 0, 0));
        assertSame(preparedFirst, prepared(reordered, 0, 1));

        alpha.removeMember(0);
        beta.addMember(second);
        ScenePreparation.PreparedScene moved = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW
        );
        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());
        assertSame(second, prepared(moved, 1, 1).source());
        assertSame(preparedSecond.grid(), prepared(moved, 1, 1).grid());
        assertSame(first, prepared(moved, 0, 0).source());

        alpha.removeMember(0);
        ScenePreparation.PreparedScene survivor = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW
        );
        assertSame(preparedSecond, prepared(survivor, 1, 1));
        assertTrue(preparedSecond.distanceTo(second.controlHandle()) <= QueryDomainPolicy.DEFAULT_SPACING);
    }

    @Test
    void failedBoundaryIsNotCached() {
        KEllipseCacheManager cache = new KEllipseCacheManager(
                new KEllipseBoundaryBuilder(KEllipseApproximationSettings.DEFAULT)
        );
        KEllipseMember degenerate = new KEllipseMember(
                List.of(Vector.xy(-1, 0), Vector.xy(0, 0), Vector.xy(1, 0)),
                Vector.xy(0, 0)
        );
        assertThrows(KEllipsePreparationException.class, () -> cache.prepareBoundary(degenerate));
        assertNull(cache.outlineFor(degenerate));
        assertEquals(0, cache.statistics().cachedMembers());
    }

    @Test
    void rejectsACacheThatWouldExceedTheByteCap() throws Exception {
        KEllipseCacheManager cache = new KEllipseCacheManager(
                new KEllipseBoundaryBuilder(KEllipseApproximationSettings.DEFAULT),
                new DistanceGridLimits(8192, 16_777_216, 64)
        );
        ScenePreparer preparer = new ScenePreparer(new QueryDomainPolicy(), cache);
        List<ClusterSite> clusters = List.of(new ClusterSite("A", Rgba.RED, List.of(
                ellipseAt(0, 0),
                ellipseAt(8, 0),
                ellipseAt(-8, 0)
        )));
        assertThrows(DomainLimitExceededException.class, () -> preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, VIEW
        ));
    }

    private static PreparedKEllipseMember prepared(ScenePreparation.PreparedScene scene, int cluster, int member) {
        return (PreparedKEllipseMember) scene.clusters().get(cluster).members().get(member);
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
