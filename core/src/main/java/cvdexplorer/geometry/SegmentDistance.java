package cvdexplorer.geometry;

/** Euclidean distance from a point to a closed line segment. */
public final class SegmentDistance {
    private SegmentDistance() {
    }

    public static double distanceToSegment(Vector point, Vector a, Vector b) {
        Vector ap = point.sub(a);
        Vector ab = b.sub(a);
        double ab2 = ab.lengthSquared();
        if (ab2 <= 0) {
            return point.distanceTo(a);
        }
        double t = ap.dot(ab) / ab2;
        t = Math.max(0.0, Math.min(1.0, t));
        Vector closest = a.add(ab.mul(t));
        return point.distanceTo(closest);
    }
}
