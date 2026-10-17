package cvdexplorer;

import cvdexplorer.geometry.SegmentDistance;
import cvdexplorer.geometry.Vector;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.PointMember;

import java.util.List;

/**
 * Whether a member draws a stroke, and the distance from a point to that stroke.
 * {@code outline} is the cached k-ellipse polyline; other members ignore it.
 */
public final class MemberStroke {
    private MemberStroke() {
    }

    public static boolean isDrawn(ClusterMember member, List<Vector> outline) {
        if (member instanceof PointMember) {
            return false;
        }
        if (member instanceof KEllipseMember) {
            return outline != null && outline.size() >= 2;
        }
        if (member instanceof EllipseMember ellipse) {
            return !ellipse.isDegenerate();
        }
        return true;
    }

    /**
     * Distance to the drawn stroke, or positive infinity when nothing is drawn.
     * A k-ellipse is measured against {@code outline}. {@link KEllipseMember#distanceTo} is not used.
     */
    public static double distanceTo(ClusterMember member, Vector point, List<Vector> outline) {
        if (!isDrawn(member, outline)) {
            return Double.POSITIVE_INFINITY;
        }
        if (member instanceof KEllipseMember) {
            return distanceToClosedPolyline(point, outline);
        }
        return member.distanceTo(point);
    }

    private static double distanceToClosedPolyline(Vector point, List<Vector> outline) {
        double best = Double.POSITIVE_INFINITY;
        Vector previous = outline.get(outline.size() - 1);
        for (Vector next : outline) {
            best = Math.min(best, SegmentDistance.distanceToSegment(point, previous, next));
            previous = next;
        }
        return best;
    }
}
