package cvdexplorer.field;

import cvdexplorer.geometry.ClosedPolyline;
import cvdexplorer.geometry.PolylineDistanceIndex;
import cvdexplorer.geometry.SegmentDistance;
import cvdexplorer.geometry.Vector;
import cvdexplorer.instrument.Counters;
import cvdexplorer.kellipse.KEllipseApproximationSettings;
import cvdexplorer.kellipse.KEllipseBoundaryBuilder;
import cvdexplorer.kellipse.PreparedKEllipseBoundary;
import cvdexplorer.model.KEllipseMember;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceGridBuilderTest {

    @Test
    void squareStoresSignedBoundaryDistance() {
        ClosedPolyline square = ClosedPolyline.of(List.of(
                Vector.xy(0, 0),
                Vector.xy(4, 0),
                Vector.xy(4, 4),
                Vector.xy(0, 4)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(square);
        DistanceGridSpec spec = new DistanceGridSpec(-1, -1, 1, 7, 7);
        Counters.reset();
        DistanceGrid2D grid = DistanceGridBuilder.build(spec, square, index);
        assertEquals(1, Counters.snapshot().gridBuilds());

        assertTrue(grid.sample(3, 3) < 0.0f);
        assertTrue(grid.sample(0, 3) > 0.0f);
        assertNodeMagnitude(grid, square, 3, 3);
        assertNodeMagnitude(grid, square, 0, 3);
        assertNodeMagnitude(grid, square, 1, 3);
        assertEquals(0.0, Math.abs(grid.sample(1, 3)), 1e-5);
    }

    @Test
    void thinRectangleMayLoseItsInterior() {
        ClosedPolyline rectangle = ClosedPolyline.of(List.of(
                Vector.xy(0, 0),
                Vector.xy(0.5, 0),
                Vector.xy(0.5, 4),
                Vector.xy(0, 4)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(rectangle);
        DistanceGridSpec spec = new DistanceGridSpec(-1, -1, 1, 4, 7);
        DistanceGrid2D grid = DistanceGridBuilder.build(spec, rectangle, index);
        boolean anyNegative = false;
        for (int j = 0; j < spec.nodesY(); j++) {
            for (int i = 0; i < spec.nodesX(); i++) {
                anyNegative |= grid.sample(i, j) < 0.0f;
            }
        }
        assertTrue(!anyNegative);
        double halfWidth = 0.25;
        double center = Math.abs(grid.interpolate(0.25, 2));
        assertTrue(center <= halfWidth + spec.spacing());
    }

    @Test
    void repeatedBuildsMatch() {
        ClosedPolyline square = ClosedPolyline.of(List.of(
                Vector.xy(0, 0), Vector.xy(3, 0), Vector.xy(3, 2), Vector.xy(0, 2)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(square);
        DistanceGridSpec spec = new DistanceGridSpec(-1, -1, 0.5, 10, 8);
        DistanceGrid2D first = DistanceGridBuilder.build(spec, square, index);
        DistanceGrid2D second = DistanceGridBuilder.build(spec, square, index);
        for (int j = 0; j < spec.nodesY(); j++) {
            for (int i = 0; i < spec.nodesX(); i++) {
                assertEquals(first.sample(i, j), second.sample(i, j), 0.0f);
            }
        }
    }

    @Test
    void kEllipseInterpolationStaysNearThePolylineIndex() throws Exception {
        KEllipseMember member = new KEllipseMember(
                List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                Vector.xy(0, 8)
        );
        PreparedKEllipseBoundary boundary = new KEllipseBoundaryBuilder(KEllipseApproximationSettings.DEFAULT).build(member);
        double spacing = 1.0;
        DistanceGridSpec spec = DistanceGridSpec.alignedCovering(boundary.bounds(), spacing);
        DistanceGrid2D grid = DistanceGridBuilder.build(spec, boundary.polyline(), boundary.index());
        Random random = new Random(7);
        double maxError = 0.0;
        double sumError = 0.0;
        int queries = 200;
        for (int n = 0; n < queries; n++) {
            double x = spec.originX() + random.nextDouble() * (spec.maxX() - spec.originX());
            double y = spec.originY() + random.nextDouble() * (spec.maxY() - spec.originY());
            double error = Math.abs(Math.abs(grid.interpolate(x, y)) - boundary.index().distanceTo(x, y));
            sumError += error;
            maxError = Math.max(maxError, error);
        }
        double meanError = sumError / queries;
        System.out.printf(
                "k-ellipse grid interpolation error mean=%g max=%g spacing=%g%n",
                meanError,
                maxError,
                spacing
        );
        assertTrue(maxError <= spacing / 2.0 + 1e-6, "max error " + maxError);
    }

    private static void assertNodeMagnitude(DistanceGrid2D grid, ClosedPolyline polyline, int i, int j) {
        double x = grid.spec().originX() + i * grid.spec().spacing();
        double y = grid.spec().originY() + j * grid.spec().spacing();
        assertEquals(boundaryDistance(polyline, x, y), Math.abs(grid.sample(i, j)), 1e-5);
    }

    private static double boundaryDistance(ClosedPolyline polyline, double x, double y) {
        Vector point = Vector.xy(x, y);
        double best = Double.POSITIVE_INFINITY;
        int n = polyline.vertexCount();
        for (int i = 0; i < n; i++) {
            Vector a = Vector.xy(polyline.x(i), polyline.y(i));
            Vector b = Vector.xy(polyline.x((i + 1) % n), polyline.y((i + 1) % n));
            best = Math.min(best, SegmentDistance.distanceToSegment(point, a, b));
        }
        return best;
    }
}
