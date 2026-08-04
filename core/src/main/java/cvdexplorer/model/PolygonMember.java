package cvdexplorer.model;

import cvdexplorer.geometry.SegmentDistance;
import cvdexplorer.geometry.Vector;

import java.util.Arrays;
import java.util.List;

/**
 * Closed polygonal chain as a single site member. Distance is to the boundary
 * (minimum distance over edges).
 */
public final class PolygonMember implements ClusterMember {
    private final Vector[] vertices;

    public PolygonMember(List<Vector> vertices) {
        if (vertices == null || vertices.size() < SceneLimits.MIN_POLYGON_VERTICES) {
            throw new IllegalArgumentException(
                    "PolygonMember requires at least " + SceneLimits.MIN_POLYGON_VERTICES + " vertices"
            );
        }
        this.vertices = new Vector[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            Vector v = vertices.get(i);
            if (v == null) {
                throw new IllegalArgumentException("PolygonMember vertices must be non-null");
            }
            this.vertices[i] = v;
        }
    }

    public List<Vector> vertices() {
        return List.of(vertices);
    }

    @Override
    public double distanceTo(Vector point) {
        double best = Double.POSITIVE_INFINITY;
        int n = vertices.length;
        for (int i = 0; i < n; i++) {
            Vector a = vertices[i];
            Vector b = vertices[(i + 1) % n];
            best = Math.min(best, SegmentDistance.distanceToSegment(point, a, b));
        }
        return best;
    }

    @Override
    public int handleCount() {
        return vertices.length;
    }

    @Override
    public Vector getHandle(int index) {
        if (index < 0 || index >= vertices.length) {
            throw new IndexOutOfBoundsException(index);
        }
        return vertices[index];
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        if (index < 0 || index >= vertices.length) {
            throw new IndexOutOfBoundsException(index);
        }
        Vector[] next = Arrays.copyOf(vertices, vertices.length);
        next[index] = v;
        return new PolygonMember(List.of(next));
    }

    @Override
    public Vector placementCentroid() {
        double sx = 0.0;
        double sy = 0.0;
        for (Vector v : vertices) {
            sx += v.x();
            sy += v.y();
        }
        return Vector.xy(sx / vertices.length, sy / vertices.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PolygonMember other)) {
            return false;
        }
        return Arrays.equals(vertices, other.vertices);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vertices);
    }
}
