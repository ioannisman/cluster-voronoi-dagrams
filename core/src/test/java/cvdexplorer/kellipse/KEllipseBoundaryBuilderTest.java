package cvdexplorer.kellipse;

import cvdexplorer.geometry.ClosedPolyline;
import cvdexplorer.geometry.Vector;
import cvdexplorer.instrument.Counters;
import cvdexplorer.kellipse.KEllipsePreparationException.Reason;
import cvdexplorer.model.KEllipseMember;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KEllipseBoundaryBuilderTest {
    private static final KEllipseBoundaryBuilder BUILDER = new KEllipseBoundaryBuilder(
            KEllipseApproximationSettings.DEFAULT
    );

    @Test
    void threeCoincidentFociMatchACircleOfRadiusLevelOverThree() throws Exception {
        Vector focus = Vector.xy(1, 1);
        KEllipseMember member = new KEllipseMember(List.of(focus, focus, focus), Vector.xy(1, 11));
        double radius = member.level() / 3.0;
        assertEquals(10.0, radius, 1e-12);
        PreparedKEllipseBoundary boundary = BUILDER.build(member);
        double tolerance = 2.0 * KEllipseApproximationSettings.DEFAULT.chordTolerance();
        double[][] points = {{1, 1}, {1, 11}, {1, 16}, {6, 1}, {1, -4}};
        for (double[] point : points) {
            double expected = Math.abs(Math.hypot(point[0] - 1, point[1] - 1) - radius);
            assertEquals(expected, boundary.index().distanceTo(point[0], point[1]), tolerance);
        }
        assertAnglesIncrease(boundary);
        assertFalse(boundary.diagnostics().refinementCapHit());
    }

    @Test
    void randomFociMatchAFinerBoundary() throws Exception {
        Random random = new Random(20261011L);
        for (int focusCount : new int[] {3, 5, 8}) {
            List<Vector> foci = new ArrayList<>();
            for (int i = 0; i < focusCount; i++) {
                foci.add(Vector.xy(-5.0 + 10.0 * random.nextDouble(), -5.0 + 10.0 * random.nextDouble()));
            }
            KEllipseMember member = new KEllipseMember(foci, Vector.xy(0, 30));
            PreparedKEllipseBoundary coarse = BUILDER.build(member);
            assertLevelSet(member, coarse.polyline());
            assertAnglesIncrease(coarse);
            KEllipseApproximationSettings fineSettings = new KEllipseApproximationSettings(
                    KEllipseApproximationSettings.DEFAULT.chordTolerance() / 50.0,
                    KEllipseApproximationSettings.DEFAULT.rootTolerance(),
                    KEllipseApproximationSettings.DEFAULT.initialAngularSamples(),
                    KEllipseApproximationSettings.DEFAULT.maxRefinementDepth(),
                    KEllipseApproximationSettings.DEFAULT.maxSegments(),
                    KEllipseApproximationSettings.DEFAULT.maxBisectionIterations(),
                    KEllipseApproximationSettings.DEFAULT.maxBracketExpansions(),
                    KEllipseApproximationSettings.DEFAULT.minInnerRadius(),
                    KEllipseApproximationSettings.DEFAULT.maxInteriorIterations()
            );
            PreparedKEllipseBoundary fine = new KEllipseBoundaryBuilder(fineSettings).build(member);
            double tolerance = 2.0 * KEllipseApproximationSettings.DEFAULT.chordTolerance();
            double[][] queries = {{0, 0}, {0, 30}, {40, -10}, {-20, 15}, {8, 8}};
            for (double[] query : queries) {
                assertEquals(
                        fine.index().distanceTo(query[0], query[1]),
                        coarse.index().distanceTo(query[0], query[1]),
                        tolerance,
                        "foci " + focusCount + " at " + query[0] + ", " + query[1]
                );
            }
        }
    }

    @Test
    void collinearAndElongatedFociStayOnTheLevelSet() throws Exception {
        PreparedKEllipseBoundary collinear = BUILDER.build(new KEllipseMember(
                List.of(Vector.xy(0, 0), Vector.xy(10, 0), Vector.xy(20, 0), Vector.xy(30, 0)),
                Vector.xy(15, 40)
        ));
        assertLevelSet(collinear.source(), collinear.polyline());
        assertAnglesIncrease(collinear);

        KEllipseMember flat = new KEllipseMember(
                List.of(Vector.xy(0, 0), Vector.xy(50, 0), Vector.xy(100, 0)),
                Vector.xy(50, 5)
        );
        PreparedKEllipseBoundary elongated = BUILDER.build(flat);
        assertLevelSet(flat, elongated.polyline());
        assertAnglesIncrease(elongated);
    }

    @Test
    void centroidOutsideUsesAnInteriorPoint() throws Exception {
        KEllipseMember member = new KEllipseMember(
                List.of(Vector.xy(0, 0), Vector.xy(0, 0), Vector.xy(0, 0), Vector.xy(100, 0)),
                Vector.xy(10, 0)
        );
        assertEquals(120.0, member.level(), 1e-9);
        Vector centroid = member.placementCentroid();
        assertEquals(25.0, centroid.x(), 1e-12);
        assertTrue(member.focalSum(centroid.x(), centroid.y()) > member.level());
        PreparedKEllipseBoundary boundary = BUILDER.build(member);
        Vector interior = boundary.interiorPoint();
        double margin = member.focusCount() * KEllipseApproximationSettings.DEFAULT.minInnerRadius();
        assertTrue(member.focalSum(interior.x(), interior.y()) < member.level() - margin);
        assertTrue(interior.distanceTo(centroid) > 1.0);
        assertLevelSet(member, boundary.polyline());
        assertAnglesIncrease(boundary);
    }

    @Test
    void handleAtTheMedianIsDegenerate() {
        KEllipseMember onSegment = new KEllipseMember(
                List.of(Vector.xy(-1, 0), Vector.xy(0, 0), Vector.xy(1, 0)),
                Vector.xy(0, 0)
        );
        KEllipsePreparationException segment = assertThrows(
                KEllipsePreparationException.class,
                () -> BUILDER.build(onSegment)
        );
        assertEquals(Reason.DEGENERATE_CURVE, segment.reason());

        Vector focus = Vector.xy(4, 5);
        KEllipseMember onFocus = new KEllipseMember(List.of(focus, focus, focus), focus);
        KEllipsePreparationException atFocus = assertThrows(
                KEllipsePreparationException.class,
                () -> BUILDER.build(onFocus)
        );
        assertEquals(Reason.DEGENERATE_CURVE, atFocus.reason());
    }

    @Test
    void handleJustAboveTheInnerMarginSucceeds() throws Exception {
        double inner = KEllipseApproximationSettings.DEFAULT.minInnerRadius();
        double y = 0.0032;
        KEllipseMember member = new KEllipseMember(
                List.of(Vector.xy(-1, 0), Vector.xy(0, 0), Vector.xy(1, 0)),
                Vector.xy(0, y)
        );
        double required = 2.0 + 3.0 * inner;
        assertTrue(member.level() > required);
        assertTrue(member.level() < required + 1e-3);
        PreparedKEllipseBoundary boundary = BUILDER.build(member);
        assertTrue(boundary.polyline().vertexCount() >= 3);
        assertLevelSet(member, boundary.polyline());
    }

    @Test
    void exhaustedBisectionIsANumericalFailure() {
        KEllipseApproximationSettings settings = new KEllipseApproximationSettings(
                0.05,
                1e-15,
                8,
                1,
                8,
                1,
                8,
                1e-3,
                50
        );
        KEllipseMember member = new KEllipseMember(
                List.of(Vector.xy(-10, 0), Vector.xy(0, 0), Vector.xy(10, 0)),
                Vector.xy(0, 12)
        );
        KEllipsePreparationException failure = assertThrows(
                KEllipsePreparationException.class,
                () -> new KEllipseBoundaryBuilder(settings).build(member)
        );
        assertEquals(Reason.NUMERICAL_FAILURE, failure.reason());
    }

    @Test
    void buildCountsFocalSumsAndDoesNotCountARejectedCurve() throws Exception {
        Counters.reset();
        KEllipseMember member = new KEllipseMember(
                List.of(Vector.xy(0, 0), Vector.xy(4, 0), Vector.xy(2, 3)),
                Vector.xy(2, 10)
        );
        BUILDER.build(member);
        Counters.Snapshot built = Counters.snapshot();
        assertTrue(built.focalSumEvaluations() > 0);
        assertEquals(1, built.boundaryBuilds());
        assertEquals(1, built.indexBuilds());

        Counters.reset();
        assertThrows(KEllipsePreparationException.class, () -> BUILDER.build(
                new KEllipseMember(List.of(Vector.xy(0, 0), Vector.xy(0, 0), Vector.xy(0, 0)), Vector.xy(0, 0))
        ));
        assertEquals(0, Counters.snapshot().boundaryBuilds());
        assertEquals(0, Counters.snapshot().indexBuilds());
        assertTrue(Counters.snapshot().focalSumEvaluations() > 0);
    }

    @Test
    void rejectsInvalidApproximationSettings() {
        assertThrows(IllegalArgumentException.class, () -> new KEllipseApproximationSettings(
                0.0, 1e-7, 64, 6, 4096, 200, 8, 1e-3, 200
        ));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseApproximationSettings(
                0.05, 1e-7, 2, 6, 4096, 200, 8, 1e-3, 200
        ));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseBoundaryBuilder(null));
        assertThrows(IllegalArgumentException.class, () -> BUILDER.build(null));
    }

    private static void assertLevelSet(KEllipseMember member, ClosedPolyline polyline) {
        double tolerance = 1e-6 * Math.max(1.0, member.level());
        for (int i = 0; i < polyline.vertexCount(); i++) {
            double value = member.focalSum(polyline.x(i), polyline.y(i));
            assertEquals(member.level(), value, tolerance, "vertex " + i);
        }
    }

    private static void assertAnglesIncrease(PreparedKEllipseBoundary boundary) {
        ClosedPolyline polyline = boundary.polyline();
        Vector origin = boundary.interiorPoint();
        double previous = 0.0;
        double start = Math.atan2(
                polyline.y(0) - origin.y(),
                polyline.x(0) - origin.x()
        );
        for (int i = 1; i < polyline.vertexCount(); i++) {
            double angle = Math.atan2(polyline.y(i) - origin.y(), polyline.x(i) - origin.x()) - start;
            if (angle < 0.0) {
                angle += 2.0 * Math.PI;
            }
            assertTrue(angle > previous && angle < 2.0 * Math.PI, "angle " + i + " = " + angle);
            previous = angle;
        }
    }
}
