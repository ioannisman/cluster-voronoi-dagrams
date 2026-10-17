package cvdexplorer.field;

import cvdexplorer.core.DomainLimitExceededException;
import cvdexplorer.geometry.ClosedPolyline;
import cvdexplorer.geometry.PolylineDistanceIndex;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Builds a signed-distance grid from one closed polyline and its segment index.
 * Sign and magnitude both refer to that polyline. Each row finds interior spans once;
 * horizontal edges and shared vertices use a half-open crossing rule. Node distances come from
 * the index. Queries do not call this class.
 */
public final class DistanceGridBuilder {
    private DistanceGridBuilder() {
    }

    public static DistanceGrid2D build(DistanceGridSpec spec, ClosedPolyline polygon, PolylineDistanceIndex index) {
        if (spec == null || polygon == null || index == null) {
            throw new IllegalArgumentException("Grid build requires a spec, a polyline, and an index");
        }
        if (index.segmentCount() != polygon.vertexCount()) {
            throw new IllegalArgumentException("Polyline and segment index do not describe the same chain");
        }
        int vertexCount = polygon.vertexCount();
        double[] xs = new double[vertexCount];
        double[] ys = new double[vertexCount];
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < vertexCount; i++) {
            xs[i] = polygon.x(i);
            ys[i] = polygon.y(i);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        int nodesX = spec.nodesX();
        int nodesY = spec.nodesY();
        long nodeCount = spec.nodeCount();
        if (nodeCount > Integer.MAX_VALUE) {
            throw new DomainLimitExceededException(
                    "K-ellipse distance grid has " + nodeCount + " nodes, which is too many to allocate. Zoom in."
            );
        }
        float[] samples = new float[Math.toIntExact(nodeCount)];
        double originX = spec.originX();
        double originY = spec.originY();
        double spacing = spec.spacing();
        double rowMinY = minY;
        double rowMaxY = maxY;
        IntStream.range(0, nodesY).parallel().forEach(j -> fillRow(
                samples, xs, ys, index, j, nodesX, originX, originY, spacing, rowMinY, rowMaxY
        ));
        return DistanceGrid2D.adopt(spec, samples);
    }

    private static void fillRow(
            float[] samples,
            double[] xs,
            double[] ys,
            PolylineDistanceIndex index,
            int row,
            int nodesX,
            double originX,
            double originY,
            double spacing,
            double minY,
            double maxY
    ) {
        double y = originY + row * spacing;
        double[] crossings = (y >= minY && y <= maxY) ? crossingsAt(xs, ys, y) : new double[0];
        int offset = row * nodesX;
        for (int i = 0; i < nodesX; i++) {
            double x = originX + i * spacing;
            double distance = index.distanceTo(x, y);
            samples[offset + i] = (float) (inside(crossings, x) ? -distance : distance);
        }
    }

    /**
     * X coordinates where non-horizontal edges cross {@code y}. The rule
     * {@code (ay <= y < by) || (by <= y < ay)} counts each shared vertex once and skips horizontal edges.
     */
    static double[] crossingsAt(double[] xs, double[] ys, double y) {
        int vertexCount = xs.length;
        double[] crossings = new double[vertexCount];
        int count = 0;
        for (int i = 0; i < vertexCount; i++) {
            int next = i + 1;
            if (next == vertexCount) {
                next = 0;
            }
            double ay = ys[i];
            double by = ys[next];
            boolean crosses = (ay <= y && y < by) || (by <= y && y < ay);
            if (!crosses) {
                continue;
            }
            double ax = xs[i];
            double bx = xs[next];
            crossings[count] = ax + (y - ay) * (bx - ax) / (by - ay);
            count++;
        }
        double[] sorted = Arrays.copyOf(crossings, count);
        Arrays.sort(sorted);
        return sorted;
    }

    /** Even-odd spans. An unpaired trailing crossing does not extend inside to infinity. */
    static boolean inside(double[] crossings, double x) {
        for (int span = 0; span + 1 < crossings.length; span += 2) {
            if (x < crossings[span]) {
                return false;
            }
            if (x < crossings[span + 1]) {
                return true;
            }
        }
        return false;
    }
}
