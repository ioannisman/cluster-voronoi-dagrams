package cvdexplorer.core;

import cvdexplorer.field.DistanceGrid2D;
import cvdexplorer.geometry.Vector;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.KEllipseMember;

/**
 * Query wrapper for one k-ellipse. Distance is the absolute value of this member's
 * signed-distance grid. The grid is fixed for the wrapper; queries do not build or search.
 */
public final class PreparedKEllipseMember implements ClusterMember {
    private final KEllipseMember source;
    private final DistanceGrid2D grid;

    public PreparedKEllipseMember(KEllipseMember source, DistanceGrid2D grid) {
        if (source == null || grid == null) {
            throw new IllegalArgumentException("Prepared k-ellipse requires a source and a distance grid");
        }
        this.source = source;
        this.grid = grid;
    }

    public KEllipseMember source() {
        return source;
    }

    public DistanceGrid2D grid() {
        return grid;
    }

    @Override
    public double distanceTo(Vector point) {
        return Math.abs(grid.interpolate(point.x(), point.y()));
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
