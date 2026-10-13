package cvdexplorer.kellipse;

import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.ClosedPolyline;
import cvdexplorer.geometry.PolylineDistanceIndex;
import cvdexplorer.geometry.Vector;
import cvdexplorer.model.KEllipseMember;

/**
 * Immutable polyline prepared from one k-ellipse. {@code maxAcceptedDeviation} is the largest
 * midpoint-to-chord distance among arcs that refinement accepted. That check is a sampling
 * heuristic, not a certified Hausdorff bound between the polyline and the true curve.
 */
public record PreparedKEllipseBoundary(
        KEllipseMember source,
        KEllipseApproximationSettings settings,
        ClosedPolyline polyline,
        PolylineDistanceIndex index,
        Vector interiorPoint,
        Box bounds,
        Diagnostics diagnostics
) {
    public record Diagnostics(double maxAcceptedDeviation, boolean refinementCapHit) {
    }
}
