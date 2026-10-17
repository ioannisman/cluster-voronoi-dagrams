package cvdexplorer.geometry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolylineDistanceIndexTest {
    private static final long SEED = 20261011L;

    @Test
    void rejectsEmptyNullAndNonFinitePolylines() {
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(List.of()));
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of((List<Vector>) null));
        List<Vector> withNull = new ArrayList<>();
        withNull.add(Vector.xy(0, 0));
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(withNull));
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(List.of(Vector.xy(Double.NaN, 0))));
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(List.of(Vector.xy(0, Double.POSITIVE_INFINITY))));
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(null, new double[] {0}));
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(new double[] {0}, new double[] {0, 1}));
        assertThrows(IllegalArgumentException.class, () -> ClosedPolyline.of(new double[0], new double[0]));
        assertThrows(IllegalArgumentException.class, () -> PolylineDistanceIndex.build(null));
    }

    @Test
    void copiesVerticesAndReportsBounds() {
        double[] xs = {0, 4, 0};
        double[] ys = {0, 0, 3};
        ClosedPolyline polyline = ClosedPolyline.of(xs, ys);
        xs[0] = 50;
        ys[2] = 80;
        assertEquals(0.0, polyline.x(0), 0.0);
        assertEquals(3.0, polyline.y(2), 0.0);
        assertEquals(3, polyline.vertexCount());
        assertEquals(Vector.xy(0, 0), polyline.bounds().p());
        assertEquals(Vector.xy(4, 3), polyline.bounds().q());
        assertEquals(Vector.xy(4, 0), polyline.vertices().get(1));
        assertSame(polyline.vertices(), polyline.vertices());
        assertThrows(IndexOutOfBoundsException.class, () -> polyline.x(3));
        assertThrows(IndexOutOfBoundsException.class, () -> polyline.y(-1));
    }

    @Test
    void singleVertexMatchesPointDistance() {
        ClosedPolyline polyline = ClosedPolyline.of(List.of(Vector.xy(5, 7)));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        assertEquals(1, index.segmentCount());
        assertEquals(1, index.height());
        assertMatches(index, polyline, 5, 7);
        assertMatches(index, polyline, 5, 10);
        assertMatches(index, polyline, 2, 3);
        assertEquals(0.0, index.distanceTo(5, 7), 0.0);
        assertEquals(3.0, index.distanceTo(5, 10), 1e-12);
    }

    @Test
    void twoVerticesMatchTheSegment() {
        ClosedPolyline polyline = ClosedPolyline.of(List.of(Vector.xy(0, 0), Vector.xy(4, 0)));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        assertEquals(2, index.segmentCount());
        assertEquals(1, index.height());
        assertEquals(0.0, index.distanceTo(2, 0), 1e-12);
        assertEquals(3.0, index.distanceTo(2, 3), 1e-12);
        assertEquals(1.0, index.distanceTo(-1, 0), 1e-12);
        assertMatches(index, polyline, 2, 3);
        assertMatches(index, polyline, 9, -4);
    }

    @Test
    void pointsOnEdgesAndVerticesAreZero() {
        ClosedPolyline polyline = ClosedPolyline.of(List.of(
                Vector.xy(0, 0),
                Vector.xy(4, 0),
                Vector.xy(4, 2),
                Vector.xy(0, 2)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        int n = polyline.vertexCount();
        for (int i = 0; i < n; i++) {
            assertEquals(0.0, index.distanceTo(polyline.x(i), polyline.y(i)), 1e-12);
            int next = (i + 1) % n;
            double midX = 0.5 * (polyline.x(i) + polyline.x(next));
            double midY = 0.5 * (polyline.y(i) + polyline.y(next));
            assertEquals(0.0, index.distanceTo(midX, midY), 1e-12);
        }
    }

    @Test
    void interiorAndExteriorQueriesMatchTheBoundaryScan() {
        ClosedPolyline square = ClosedPolyline.of(List.of(
                Vector.xy(0, 0),
                Vector.xy(2, 0),
                Vector.xy(2, 2),
                Vector.xy(0, 2)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(square);
        assertMatches(index, square, 1, 1);
        assertEquals(1.0, index.distanceTo(1, 1), 1e-12);
        assertMatches(index, square, 5, 1);
        assertEquals(3.0, index.distanceTo(5, 1), 1e-12);
        assertMatches(index, square, -2, -2);
    }

    @Test
    void repeatedVerticesAndZeroLengthEdges() {
        ClosedPolyline polyline = ClosedPolyline.of(List.of(
                Vector.xy(0, 0),
                Vector.xy(2, 0),
                Vector.xy(2, 0),
                Vector.xy(2, 1),
                Vector.xy(0, 1)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        assertEquals(0.0, index.distanceTo(2, 0), 1e-12);
        assertEquals(0.0, index.distanceTo(2, 0.5), 1e-12);
        assertMatches(index, polyline, 3, 0);
        assertMatches(index, polyline, 1, 0.25);
        assertMatches(index, polyline, 2, 4);

        ClosedPolyline collapsed = ClosedPolyline.of(List.of(
                Vector.xy(4, 5),
                Vector.xy(4, 5),
                Vector.xy(4, 5)
        ));
        PolylineDistanceIndex collapsedIndex = PolylineDistanceIndex.build(collapsed);
        assertEquals(0.0, collapsedIndex.distanceTo(4, 5), 1e-12);
        assertEquals(3.0, collapsedIndex.distanceTo(4, 8), 1e-12);
        assertMatches(collapsedIndex, collapsed, 1, 5);
    }

    @Test
    void thinPolygonMatchesTheBoundaryScan() {
        ClosedPolyline polyline = ClosedPolyline.of(List.of(
                Vector.xy(0, 0),
                Vector.xy(1000, 0),
                Vector.xy(1000, 1e-6),
                Vector.xy(0, 1e-6)
        ));
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        assertEquals(0.0, index.distanceTo(500, 0), 1e-12);
        assertMatches(index, polyline, 500, 5e-7);
        assertMatches(index, polyline, 500, -2);
        assertMatches(index, polyline, -3, 1e-6);
        assertMatches(index, polyline, 1000, 1e-6);
    }

    @Test
    void treeHeightFollowsMedianSplits() {
        int[] sizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 128, 2000, 10000};
        for (int size : sizes) {
            PolylineDistanceIndex index = PolylineDistanceIndex.build(circle(size));
            assertEquals(size, index.segmentCount());
            assertEquals(balancedHeight(size), index.height(), "size " + size);
        }
    }

    @Test
    void randomPolygonsMatchBruteForceScan() {
        Random random = new Random(SEED);
        int[] sizes = {3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 31, 32, 64, 127, 128, 129, 511, 512, 2000};
        for (int size : sizes) {
            assertPolygonMatches(randomWalk(random, size), random, 24);
            assertPolygonMatches(star(random, size), random, 24);
        }
    }

    @Test
    @Timeout(180)
    void tenThousandSegmentsMatchBruteForce() {
        ClosedPolyline polyline = circle(10_000);
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        assertEquals(balancedHeight(10_000), index.height());
        Random random = new Random(SEED);
        double[] xs = new double[10_000];
        double[] ys = new double[10_000];
        double[] sequential = new double[10_000];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = -3.0 + 6.0 * random.nextDouble();
            ys[i] = -3.0 + 6.0 * random.nextDouble();
            sequential[i] = index.distanceTo(xs[i], ys[i]);
            assertMatches(index, polyline, xs[i], ys[i]);
        }
        double[] parallel = new double[xs.length];
        IntStream.range(0, xs.length).parallel().forEach(i -> parallel[i] = index.distanceTo(xs[i], ys[i]));
        for (int i = 0; i < xs.length; i++) {
            assertEquals(Double.doubleToLongBits(sequential[i]), Double.doubleToLongBits(parallel[i]));
        }
    }

    @Test
    void parallelQueriesMatchSequentialQueries() {
        ClosedPolyline polyline = star(new Random(SEED), 300);
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        Random random = new Random(7);
        int queries = 2_000;
        double[] xs = new double[queries];
        double[] ys = new double[queries];
        double[] sequential = new double[queries];
        for (int i = 0; i < queries; i++) {
            xs[i] = -30.0 + 60.0 * random.nextDouble();
            ys[i] = -30.0 + 60.0 * random.nextDouble();
            sequential[i] = index.distanceTo(xs[i], ys[i]);
        }
        double[] parallel = new double[queries];
        IntStream.range(0, queries).parallel().forEach(i -> parallel[i] = index.distanceTo(xs[i], ys[i]));
        for (int i = 0; i < queries; i++) {
            assertEquals(Double.doubleToLongBits(sequential[i]), Double.doubleToLongBits(parallel[i]));
        }
    }

    @Test
    void sortByCentroidOrdersASubrange() {
        int[] order = {0, 1, 2, 3, 4, 5, 6};
        double[] key = {9, 1, 4, 1, 3, 8, 2};
        PolylineDistanceIndex.sortByCentroid(order, key, 1, 6);
        assertEquals(0, order[0]);
        assertEquals(6, order[6]);
        for (int i = 1; i < 5; i++) {
            assertTrue(key[order[i]] <= key[order[i + 1]]);
        }
        boolean[] seen = new boolean[7];
        for (int id : order) {
            assertTrue(id >= 0 && id < seen.length && !seen[id]);
            seen[id] = true;
        }

        int[] reverse = new int[1000];
        double[] reverseKey = new double[1000];
        for (int i = 0; i < reverse.length; i++) {
            reverse[i] = i;
            reverseKey[i] = reverse.length - i;
        }
        PolylineDistanceIndex.sortByCentroid(reverse, reverseKey, 0, reverse.length);
        for (int i = 0; i < reverse.length; i++) {
            assertEquals(i + 1.0, reverseKey[reverse[i]], 0.0);
        }

        int[] equal = {3, 1, 2, 0};
        double[] equalKey = {2, 2, 2, 2};
        PolylineDistanceIndex.sortByCentroid(equal, equalKey, 0, equal.length);
        boolean[] equalSeen = new boolean[4];
        for (int id : equal) {
            equalSeen[id] = true;
        }
        for (boolean present : equalSeen) {
            assertTrue(present);
        }
    }

    @Test
    void oracleMatchesSegmentDistance() {
        double[][] segments = {
                {0, 0, 4, 0, 2, 3},
                {0, 0, 0, 0, 1, 1},
                {-2, 5, 3, -1, 0, 0},
                {1e6, -1e6, 1e6 + 4, -1e6, 1e6 + 2, -1e6 + 8}
        };
        for (double[] sample : segments) {
            double oracle = segmentDistance(sample[4], sample[5], sample[0], sample[1], sample[2], sample[3]);
            double reference = SegmentDistance.distanceToSegment(
                    Vector.xy(sample[4], sample[5]),
                    Vector.xy(sample[0], sample[1]),
                    Vector.xy(sample[2], sample[3])
            );
            assertEquals(reference, oracle, 1e-9);
        }
    }

    @Test
    void parallelBuildsKeepIndependentResults() {
        ClosedPolyline polyline = circle(64);
        AtomicInteger builds = new AtomicInteger();
        PolylineDistanceIndex[] indexes = new PolylineDistanceIndex[8];
        IntStream.range(0, indexes.length).parallel().forEach(i -> {
            indexes[i] = PolylineDistanceIndex.build(polyline);
            builds.incrementAndGet();
        });
        assertEquals(indexes.length, builds.get());
        double[] reference = new double[32];
        Random random = new Random(3);
        for (int q = 0; q < reference.length; q++) {
            double x = -2.0 + 4.0 * random.nextDouble();
            double y = -2.0 + 4.0 * random.nextDouble();
            reference[q] = indexes[0].distanceTo(x, y);
            for (int i = 1; i < indexes.length; i++) {
                assertEquals(Double.doubleToLongBits(reference[q]), Double.doubleToLongBits(indexes[i].distanceTo(x, y)));
            }
        }
    }

    private static void assertPolygonMatches(ClosedPolyline polyline, Random random, int randomQueries) {
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        assertEquals(balancedHeight(polyline.vertexCount()), index.height());
        int n = polyline.vertexCount();
        for (int i = 0; i < n; i++) {
            assertEquals(0.0, index.distanceTo(polyline.x(i), polyline.y(i)), 1e-12);
            int next = (i + 1) % n;
            double midX = 0.5 * (polyline.x(i) + polyline.x(next));
            double midY = 0.5 * (polyline.y(i) + polyline.y(next));
            assertMatches(index, polyline, midX, midY);
        }
        Box bounds = polyline.bounds();
        double minX = bounds.p().x();
        double minY = bounds.p().y();
        double maxX = bounds.q().x();
        double maxY = bounds.q().y();
        double padX = Math.max(1.0, maxX - minX);
        double padY = Math.max(1.0, maxY - minY);
        for (int i = 0; i < randomQueries; i++) {
            double x = minX - padX + random.nextDouble() * 3.0 * padX;
            double y = minY - padY + random.nextDouble() * 3.0 * padY;
            assertMatches(index, polyline, x, y);
        }
        assertMatches(index, polyline, 1e6, -1e6);
    }

    private static void assertMatches(PolylineDistanceIndex index, ClosedPolyline polyline, double x, double y) {
        double actual = index.distanceTo(x, y);
        double expected = bruteForce(polyline, x, y);
        double tolerance = 1e-9 * Math.max(1.0, expected);
        assertTrue(
                Math.abs(actual - expected) <= tolerance,
                () -> "at (" + x + ", " + y + ") actual " + actual + " expected " + expected
        );
    }

    private static double bruteForce(ClosedPolyline polyline, double x, double y) {
        double best = Double.POSITIVE_INFINITY;
        int n = polyline.vertexCount();
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            best = Math.min(best, segmentDistance(
                    x, y, polyline.x(i), polyline.y(i), polyline.x(next), polyline.y(next)
            ));
        }
        return best;
    }

    private static double segmentDistance(
            double x, double y, double ax, double ay, double bx, double by
    ) {
        double abx = bx - ax;
        double aby = by - ay;
        double ab2 = abx * abx + aby * aby;
        double apx = x - ax;
        double apy = y - ay;
        if (ab2 <= 0.0) {
            return Math.sqrt(apx * apx + apy * apy);
        }
        double t = (apx * abx + apy * aby) / ab2;
        if (t < 0.0) {
            t = 0.0;
        } else if (t > 1.0) {
            t = 1.0;
        }
        double closestX = ax + t * abx;
        double closestY = ay + t * aby;
        double dx = x - closestX;
        double dy = y - closestY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static int balancedHeight(int segmentCount) {
        if (segmentCount <= 4) {
            return 1;
        }
        int left = segmentCount / 2;
        return 1 + Math.max(balancedHeight(left), balancedHeight(segmentCount - left));
    }

    private static ClosedPolyline circle(int vertexCount) {
        List<Vector> vertices = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(Vector.polar(1.0, i / (double) vertexCount));
        }
        return ClosedPolyline.of(vertices);
    }

    private static ClosedPolyline star(Random random, int vertexCount) {
        List<Vector> vertices = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            double radius = 0.5 + 20.0 * random.nextDouble();
            vertices.add(Vector.polar(radius, i / (double) vertexCount));
        }
        return ClosedPolyline.of(vertices);
    }

    private static ClosedPolyline randomWalk(Random random, int vertexCount) {
        List<Vector> vertices = new ArrayList<>(vertexCount);
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < vertexCount; i++) {
            x += random.nextDouble() * 4.0 - 2.0;
            y += random.nextDouble() * 4.0 - 2.0;
            vertices.add(Vector.xy(x, y));
        }
        return ClosedPolyline.of(vertices);
    }
}
