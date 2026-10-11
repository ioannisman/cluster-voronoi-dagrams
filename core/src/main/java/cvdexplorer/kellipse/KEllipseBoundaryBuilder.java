package cvdexplorer.kellipse;

import cvdexplorer.geometry.ClosedPolyline;
import cvdexplorer.geometry.PolylineDistanceIndex;
import cvdexplorer.geometry.SegmentDistance;
import cvdexplorer.geometry.Vector;
import cvdexplorer.instrument.Counters;
import cvdexplorer.kellipse.KEllipsePreparationException.Reason;
import cvdexplorer.model.KEllipseMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Samples a k-ellipse as a closed polyline and indexes that polyline.
 * Sample directions are radians. {@link Vector#polar(double, double)} is not used here because it expects turns.
 */
public final class KEllipseBoundaryBuilder {
    private final KEllipseApproximationSettings settings;

    public KEllipseBoundaryBuilder(KEllipseApproximationSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("KEllipseApproximationSettings is required");
        }
        this.settings = settings;
    }

    public KEllipseApproximationSettings settings() {
        return settings;
    }

    public PreparedKEllipseBoundary build(KEllipseMember member) throws KEllipsePreparationException {
        if (member == null) {
            throw new IllegalArgumentException("KEllipseMember is required");
        }
        double scale = sceneScale(member);
        Vector interior = interiorPoint(member, scale);
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();
        Refinement refinement = new Refinement();
        sample(member, interior.x(), interior.y(), xs, ys, refinement);
        if (xs.size() < 3) {
            throw new KEllipsePreparationException(
                    Reason.NUMERICAL_FAILURE,
                    "K-ellipse boundary has fewer than 3 vertices"
            );
        }
        double[] x = new double[xs.size()];
        double[] y = new double[ys.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = xs.get(i);
            y[i] = ys.get(i);
            if (!Double.isFinite(x[i]) || !Double.isFinite(y[i])) {
                throw new KEllipsePreparationException(
                        Reason.NUMERICAL_FAILURE,
                        "K-ellipse boundary vertex " + i + " is not finite"
                );
            }
        }
        if (Counters.ENABLED) {
            Counters.BOUNDARY_BUILDS.increment();
        }
        ClosedPolyline polyline = ClosedPolyline.of(x, y);
        PolylineDistanceIndex index = PolylineDistanceIndex.build(polyline);
        return new PreparedKEllipseBoundary(
                member,
                settings,
                polyline,
                index,
                interior,
                polyline.bounds(),
                new PreparedKEllipseBoundary.Diagnostics(
                        refinement.maxAcceptedDeviation,
                        refinement.capHit
                )
        );
    }

    private void sample(
            KEllipseMember member,
            double originX,
            double originY,
            List<Double> xs,
            List<Double> ys,
            Refinement refinement
    ) throws KEllipsePreparationException {
        int samples = settings.initialAngularSamples();
        double[] theta = new double[samples];
        double[] px = new double[samples];
        double[] py = new double[samples];
        for (int k = 0; k < samples; k++) {
            theta[k] = 2.0 * Math.PI * k / samples;
            Vector point = radialPoint(member, originX, originY, theta[k]);
            px[k] = point.x();
            py[k] = point.y();
        }
        xs.add(px[0]);
        ys.add(py[0]);
        for (int k = 0; k < samples; k++) {
            int next = k + 1;
            if (next == samples) {
                next = 0;
            }
            double thetaB = theta[next];
            if (next == 0) {
                thetaB += 2.0 * Math.PI;
            }
            int reserved = samples - k - 1;
            subdivide(
                    member,
                    originX,
                    originY,
                    theta[k],
                    px[k],
                    py[k],
                    thetaB,
                    px[next],
                    py[next],
                    0,
                    reserved,
                    xs,
                    ys,
                    refinement
            );
            if (next != 0) {
                xs.add(px[next]);
                ys.add(py[next]);
            }
        }
    }

    private void subdivide(
            KEllipseMember member,
            double originX,
            double originY,
            double thetaA,
            double ax,
            double ay,
            double thetaB,
            double bx,
            double by,
            int depth,
            int reserved,
            List<Double> xs,
            List<Double> ys,
            Refinement refinement
    ) throws KEllipsePreparationException {
        double thetaM = 0.5 * (thetaA + thetaB);
        Vector mid = radialPoint(member, originX, originY, thetaM);
        double deviation = SegmentDistance.distanceToSegment(
                mid,
                Vector.xy(ax, ay),
                Vector.xy(bx, by)
        );
        boolean room = xs.size() + reserved < settings.maxSegments();
        boolean withinDepth = depth < settings.maxRefinementDepth();
        if (deviation > settings.chordTolerance() && room && withinDepth) {
            // Hold the midpoint's vertex slot so the left arc cannot spend it.
            subdivide(
                    member, originX, originY,
                    thetaA, ax, ay, thetaM, mid.x(), mid.y(),
                    depth + 1, reserved + 1, xs, ys, refinement
            );
            xs.add(mid.x());
            ys.add(mid.y());
            subdivide(
                    member, originX, originY,
                    thetaM, mid.x(), mid.y(), thetaB, bx, by,
                    depth + 1, reserved, xs, ys, refinement
            );
            return;
        }
        if (deviation > refinement.maxAcceptedDeviation) {
            refinement.maxAcceptedDeviation = deviation;
        }
        if (deviation > settings.chordTolerance()) {
            refinement.capHit = true;
        }
    }

    private Vector radialPoint(
            KEllipseMember member,
            double originX,
            double originY,
            double thetaRadians
    ) throws KEllipsePreparationException {
        double ux = Math.cos(thetaRadians);
        double uy = Math.sin(thetaRadians);
        double radius = solveRadius(member, originX, originY, ux, uy);
        double x = originX + radius * ux;
        double y = originY + radius * uy;
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new KEllipsePreparationException(
                    Reason.NUMERICAL_FAILURE,
                    "K-ellipse radial sample is not finite"
            );
        }
        return Vector.xy(x, y);
    }

    private double solveRadius(
            KEllipseMember member,
            double originX,
            double originY,
            double ux,
            double uy
    ) throws KEllipsePreparationException {
        int focusCount = member.focusCount();
        double level = member.level();
        double focalAtOrigin = member.focalSum(originX, originY);
        double low = (level - focalAtOrigin) / focusCount;
        double high = (level + focalAtOrigin) / focusCount;
        if (excess(member, originX, originY, low, ux, uy) > 0.0) {
            low = 0.0;
        }
        int expansions = 0;
        while (excess(member, originX, originY, high, ux, uy) < 0.0) {
            if (expansions >= settings.maxBracketExpansions() || high > Double.MAX_VALUE / 2.0) {
                throw new KEllipsePreparationException(
                        Reason.NUMERICAL_FAILURE,
                        "K-ellipse radial bracket did not reach the level set"
                );
            }
            high *= 2.0;
            expansions++;
        }
        int iterations = 0;
        while (high - low > settings.rootTolerance() && iterations < settings.maxBisectionIterations()) {
            double mid = 0.5 * (low + high);
            if (mid == low || mid == high) {
                break;
            }
            if (excess(member, originX, originY, mid, ux, uy) > 0.0) {
                high = mid;
            } else {
                low = mid;
            }
            iterations++;
        }
        if (high - low > 1e3 * settings.rootTolerance()) {
            throw new KEllipsePreparationException(
                    Reason.NUMERICAL_FAILURE,
                    "K-ellipse radial bisection stopped at width " + (high - low)
            );
        }
        return 0.5 * (low + high);
    }

    private static double excess(
            KEllipseMember member,
            double originX,
            double originY,
            double radius,
            double ux,
            double uy
    ) {
        return member.focalSum(originX + radius * ux, originY + radius * uy) - member.level();
    }

    private Vector interiorPoint(KEllipseMember member, double scale) throws KEllipsePreparationException {
        Vector centroid = member.placementCentroid();
        if (strictlyInside(member, centroid.x(), centroid.y())) {
            return centroid;
        }
        double x = centroid.x();
        double y = centroid.y();
        double stop = 1e-9 * scale;
        for (int iteration = 0; iteration < settings.maxInteriorIterations(); iteration++) {
            InteriorStep step = weiszfeldStep(member, x, y, scale);
            if (!Double.isFinite(step.x()) || !Double.isFinite(step.y())) {
                throw new KEllipsePreparationException(
                        Reason.NUMERICAL_FAILURE,
                        "K-ellipse interior search produced a non-finite point"
                );
            }
            if (step.median()) {
                x = step.x();
                y = step.y();
                break;
            }
            double stepX = step.x() - x;
            double stepY = step.y() - y;
            x = step.x();
            y = step.y();
            if (strictlyInside(member, x, y)) {
                return Vector.xy(x, y);
            }
            if (stepX * stepX + stepY * stepY < stop * stop) {
                break;
            }
        }
        if (strictlyInside(member, x, y)) {
            return Vector.xy(x, y);
        }
        throw new KEllipsePreparationException(
                Reason.DEGENERATE_CURVE,
                "K-ellipse has no interior point far enough inside F(y) = c"
        );
    }

    private boolean strictlyInside(KEllipseMember member, double x, double y) {
        double margin = member.focusCount() * settings.minInnerRadius();
        return member.focalSum(x, y) < member.level() - margin;
    }

    /**
     * One modified Weiszfeld step. A point on one or more foci is the geometric median only when
     * the summed unit vectors toward the other foci are no longer than the number of foci there.
     * That test does not by itself make the point strictly inside the curve.
     */
    private InteriorStep weiszfeldStep(KEllipseMember member, double x, double y, double scale) throws KEllipsePreparationException {
        double near = 1e-12 * scale;
        double nearSquared = near * near;
        double weightSum = 0.0;
        double weightedX = 0.0;
        double weightedY = 0.0;
        double unitX = 0.0;
        double unitY = 0.0;
        double coincidentX = 0.0;
        double coincidentY = 0.0;
        int coincident = 0;
        List<Vector> foci = member.foci();
        for (Vector focus : foci) {
            double dx = focus.x() - x;
            double dy = focus.y() - y;
            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= nearSquared) {
                coincident++;
                coincidentX += focus.x();
                coincidentY += focus.y();
                continue;
            }
            double distance = Math.sqrt(distanceSquared);
            double inverse = 1.0 / distance;
            unitX += dx * inverse;
            unitY += dy * inverse;
            weightSum += inverse;
            weightedX += inverse * focus.x();
            weightedY += inverse * focus.y();
        }
        if (coincident > 0) {
            double unitNorm = Math.hypot(unitX, unitY);
            if (unitNorm <= coincident) {
                return new InteriorStep(coincidentX / coincident, coincidentY / coincident, true);
            }
            if (weightSum == 0.0) {
                throw new KEllipsePreparationException(
                        Reason.NUMERICAL_FAILURE,
                        "K-ellipse interior search lost every non-coincident focus"
                );
            }
            double targetX = weightedX / weightSum;
            double targetY = weightedY / weightSum;
            double stay = coincident / unitNorm;
            double leave = 1.0 - stay;
            return new InteriorStep(leave * targetX + stay * x, leave * targetY + stay * y, false);
        }
        if (weightSum == 0.0) {
            throw new KEllipsePreparationException(
                    Reason.NUMERICAL_FAILURE,
                    "K-ellipse interior search has no focus weights"
            );
        }
        return new InteriorStep(weightedX / weightSum, weightedY / weightSum, false);
    }

    /** Half the diagonal of the foci-and-handle box, and at least 1. */
    private static double sceneScale(KEllipseMember member) {
        double minX = member.controlHandle().x();
        double maxX = minX;
        double minY = member.controlHandle().y();
        double maxY = minY;
        for (Vector focus : member.foci()) {
            minX = Math.min(minX, focus.x());
            maxX = Math.max(maxX, focus.x());
            minY = Math.min(minY, focus.y());
            maxY = Math.max(maxY, focus.y());
        }
        double halfDiagonal = 0.5 * Math.hypot(maxX - minX, maxY - minY);
        return Math.max(1.0, halfDiagonal);
    }

    private record InteriorStep(double x, double y, boolean median) {
    }

    private static final class Refinement {
        private double maxAcceptedDeviation;
        private boolean capHit;
    }
}
