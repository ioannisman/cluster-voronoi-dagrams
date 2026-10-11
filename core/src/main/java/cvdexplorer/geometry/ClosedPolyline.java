package cvdexplorer.geometry;

import java.util.Arrays;
import java.util.List;

/**
 * Immutable closed polyline. The edge from the last vertex returns to the first.
 */
public final class ClosedPolyline {
    private final double[] xs;
    private final double[] ys;
    private final Box bounds;

    private ClosedPolyline(double[] xs, double[] ys) {
        if (xs.length < 1) {
            throw new IllegalArgumentException("ClosedPolyline requires at least one vertex");
        }
        for (int i = 0; i < xs.length; i++) {
            if (!Double.isFinite(xs[i]) || !Double.isFinite(ys[i])) {
                throw new IllegalArgumentException("ClosedPolyline vertex " + i + " must be finite");
            }
        }
        this.xs = xs;
        this.ys = ys;

        double minX = xs[0];
        double maxX = xs[0];
        double minY = ys[0];
        double maxY = ys[0];
        for (int i = 1; i < xs.length; i++) {
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        this.bounds = Box.pq(Vector.xy(minX, minY), Vector.xy(maxX, maxY));
    }

    public static ClosedPolyline of(List<Vector> vertices) {
        if (vertices == null || vertices.isEmpty()) {
            throw new IllegalArgumentException("ClosedPolyline requires at least one vertex");
        }
        double[] xs = new double[vertices.size()];
        double[] ys = new double[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            Vector vertex = vertices.get(i);
            if (vertex == null) {
                throw new IllegalArgumentException("ClosedPolyline vertex " + i + " must be non-null");
            }
            xs[i] = vertex.x();
            ys[i] = vertex.y();
        }
        return new ClosedPolyline(xs, ys);
    }

    public static ClosedPolyline of(double[] xs, double[] ys) {
        if (xs == null || ys == null) {
            throw new IllegalArgumentException("ClosedPolyline coordinates must be non-null");
        }
        if (xs.length != ys.length) {
            throw new IllegalArgumentException("ClosedPolyline xs and ys length mismatch");
        }
        return new ClosedPolyline(Arrays.copyOf(xs, xs.length), Arrays.copyOf(ys, ys.length));
    }

    public int vertexCount() {
        return xs.length;
    }

    public double x(int index) {
        if (index < 0 || index >= xs.length) {
            throw new IndexOutOfBoundsException(index);
        }
        return xs[index];
    }

    public double y(int index) {
        if (index < 0 || index >= xs.length) {
            throw new IndexOutOfBoundsException(index);
        }
        return ys[index];
    }

    public List<Vector> vertices() {
        Vector[] copy = new Vector[xs.length];
        for (int i = 0; i < xs.length; i++) {
            copy[i] = Vector.xy(xs[i], ys[i]);
        }
        return List.of(copy);
    }

    public Box bounds() {
        return bounds;
    }
}
