package cvdexplorer.core;

import cvdexplorer.field.DistanceGrid2D;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KEllipseRuntimeContractTest {

    @Test
    void productionQueriesUseGridInterpolationOnly() throws Exception {
        List<ClusterSite> clusters = List.of(
                new ClusterSite("A", Rgba.RED, List.of(
                        new PointMember(Vector.xy(2, 2)),
                        ellipseAt(8, 8)
                )),
                new ClusterSite("B", Rgba.BLUE, List.of(
                        new PointMember(Vector.xy(18, 4)),
                        ellipseAt(16, 14)
                ))
        );
        Box view = Box.pq(Vector.xy(0, 0), Vector.xy(24, 24));
        ScenePreparer preparer = new ScenePreparer();
        ScenePreparation.PreparedScene scene = preparer.prepare(
                clusters, MetricKind.MINIMUM_DISTANCE, NeighborOrder.NEAREST, 1, view
        );

        for (MetricKind metricKind : MetricKind.values()) {
            int parameter = metricKind == MetricKind.KTH_NEAREST_DISTANCE ? 2 : 1;
            for (NeighborOrder order : NeighborOrder.values()) {
                ScenePreparation.PreparedScene prepared = preparer.prepare(clusters, metricKind, order, parameter, view);
                DiagramRasterizer.RasterResult raster = rasterize(prepared, view);
                int[] owners = raster.ownershipGrid().clusterIndices().clone();
                int[] members = raster.ownershipGrid().memberIndices().clone();
                DiagramRasterizer.RasterResult again = rasterize(prepared, view);
                assertTrue(Arrays.equals(owners, again.ownershipGrid().clusterIndices()));
                assertTrue(Arrays.equals(members, again.ownershipGrid().memberIndices()));
                scoreSkeletonAndSubdivision(prepared, raster.ownershipGrid());
            }
        }

        assertEquals(0, preparer.statistics().lastBoundaryBuilds());
        assertEquals(0, preparer.statistics().lastGridBuilds());

        PreparedKEllipseMember preparedEllipse = (PreparedKEllipseMember) scene.clusters().get(0).members().get(1);
        assertThrows(DistanceGrid2D.OutOfDomainException.class, () -> preparedEllipse.distanceTo(Vector.xy(1_000_000, 1_000_000)));
        assertParallelQueriesMatch(preparedEllipse);
    }

    private static void scoreSkeletonAndSubdivision(
            ScenePreparation.PreparedScene prepared,
            DiagramRasterizer.OwnershipGrid grid
    ) {
        for (int y = 0; y < grid.height() - 1; y++) {
            for (int x = 0; x < grid.width() - 1; x++) {
                Vector point = Vector.xy(x + 0.5, y + 0.5);
                int clusterIndex = grid.clusterIndexAt(x, y);
                if (clusterIndex < 0) {
                    continue;
                }
                prepared.metric().score(point, prepared.clusters().get(clusterIndex));
                int memberIndex = grid.memberIndexAt(x, y);
                if (memberIndex >= 0) {
                    prepared.clusters().get(clusterIndex).members().get(memberIndex).distanceTo(point);
                }
            }
        }
    }

    private static DiagramRasterizer.RasterResult rasterize(ScenePreparation.PreparedScene prepared, Box view) {
        DiagramRasterizer rasterizer = new DiagramRasterizer();
        return rasterizer.renderParallel(
                Transformation.IDENTITY,
                view,
                point -> {
                    ClusterOwnershipSelector.Result ownership = prepared.ownershipSelector().selectOwner(
                            point, prepared.clusters(), prepared.metric()
                    );
                    return new DiagramRasterizer.Classification(
                            ownership.clusterIndex(), ownership.score(), ownership.memberIndex()
                    );
                },
                null,
                1.0
        );
    }

    private static void assertParallelQueriesMatch(PreparedKEllipseMember member) {
        int queries = 64;
        double[] sequential = new double[queries];
        for (int i = 0; i < queries; i++) {
            sequential[i] = member.distanceTo(Vector.xy(i * 0.2, 4));
        }
        double[] parallel = new double[queries];
        IntStream.range(0, queries).parallel().forEach(i -> parallel[i] = member.distanceTo(Vector.xy(i * 0.2, 4)));
        for (int i = 0; i < queries; i++) {
            assertEquals(Double.doubleToLongBits(sequential[i]), Double.doubleToLongBits(parallel[i]));
        }
    }

    private static KEllipseMember ellipseAt(double x, double y) {
        return new KEllipseMember(
                List.of(Vector.xy(x - 2, y), Vector.xy(x + 2, y), Vector.xy(x, y + 2)),
                Vector.xy(x, y + 8)
        );
    }
}
