package cvdexplorer.core;

import cvdexplorer.geometry.Vector;
import cvdexplorer.kellipse.PreparedKEllipseBoundary;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.KEllipseMember;

/**
 * Temporary query wrapper. Distance is the prepared polyline index.
 * The grid stage replaces this path; production queries must not keep it.
 */
public final class PreparedKEllipseMember implements ClusterMember {
    private final KEllipseMember source;
    private final PreparedKEllipseBoundary boundary;

    public PreparedKEllipseMember(KEllipseMember source, PreparedKEllipseBoundary boundary) {
        if (source == null || boundary == null) {
            throw new IllegalArgumentException("Prepared k-ellipse requires a source and a boundary");
        }
        if (boundary.source() != source) {
            throw new IllegalArgumentException("Prepared k-ellipse boundary does not belong to this source");
        }
        this.source = source;
        this.boundary = boundary;
    }

    public KEllipseMember source() {
        return source;
    }

    public PreparedKEllipseBoundary boundary() {
        return boundary;
    }

    @Override
    public double distanceTo(Vector point) {
        return boundary.index().distanceTo(point);
    }

    @Override
    public int handleCount() {
        return source.handleCount();
    }

    @Override
    public Vector getHandle(int index) {
        return source.getHandle(index);
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        throw new UnsupportedOperationException("Prepared k-ellipse geometry is immutable");
    }

    @Override
    public Vector placementCentroid() {
        return source.placementCentroid();
    }

    @Override
    public Vector anchor() {
        return source.anchor();
    }
}
