package cvdexplorer.model;

import cvdexplorer.geometry.SegmentDistance;
import cvdexplorer.geometry.Vector;

public record SegmentMember(Vector a, Vector b) implements ClusterMember {
    @Override
    public double distanceTo(Vector point) {
        return SegmentDistance.distanceToSegment(point, a, b);
    }

    @Override
    public int handleCount() {
        return 2;
    }

    @Override
    public Vector getHandle(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        return switch (index) {
            case 0 -> new SegmentMember(v, b);
            case 1 -> new SegmentMember(a, v);
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public Vector placementCentroid() {
        return a.add(b).mul(0.5);
    }
}
