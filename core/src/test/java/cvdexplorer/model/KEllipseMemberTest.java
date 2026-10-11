package cvdexplorer.model;

import cvdexplorer.instrument.Counters;
import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KEllipseMemberTest {

    @Test
    void rejectsEmptyTooManyAndNonFiniteGeometry() {
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(List.of(), Vector.xy(0, 1)));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(List.of(Vector.xy(0, 0)), Vector.xy(0, 1)));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(
                List.of(Vector.xy(0, 0), Vector.xy(1, 0)),
                Vector.xy(0, 1)
        ));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(null, Vector.xy(0, 1)));
        List<Vector> tooMany = new ArrayList<>();
        for (int i = 0; i < SceneLimits.MAX_KELLIPSE_FOCI + 1; i++) {
            tooMany.add(Vector.xy(i, 0));
        }
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(tooMany, Vector.xy(0, 1)));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(
                List.of(Vector.xy(Double.NaN, 0), Vector.xy(1, 0), Vector.xy(0, 1)),
                Vector.xy(0, 1)
        ));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(
                List.of(Vector.xy(0, 0), Vector.xy(1, 0), Vector.xy(0, 1)),
                Vector.xy(0, Double.POSITIVE_INFINITY)
        ));
        List<Vector> withNull = new ArrayList<>();
        withNull.add(Vector.xy(0, 0));
        withNull.add(null);
        withNull.add(Vector.xy(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new KEllipseMember(withNull, Vector.xy(0, 1)));
        assertEquals(SceneLimits.MAX_POLYGON_VERTICES, SceneLimits.MAX_KELLIPSE_FOCI);
        assertEquals(3, SceneLimits.MIN_KELLIPSE_FOCI);
    }

    @Test
    void levelIsFocalSumAtTheControlHandle() {
        KEllipseMember ellipse = member(Vector.xy(0, 4), Vector.xy(-2, 0), Vector.xy(0, 0), Vector.xy(2, 0));
        assertEquals(3, ellipse.focusCount());
        assertEquals(4, ellipse.handleCount());
        assertEquals(ellipse.focalSum(0, 4), ellipse.level(), 0.0);
        assertEquals(Vector.xy(-2, 0), ellipse.anchor());
        assertEquals(Vector.xy(0, 0), ellipse.placementCentroid());
        assertEquals(Vector.xy(0, 4), ellipse.getHandle(3));
    }

    @Test
    void withHandleRecomputesLevelAndLeavesTheOriginal() {
        KEllipseMember ellipse = member(Vector.xy(0, 3), Vector.xy(0, 0), Vector.xy(2, 0), Vector.xy(4, 0));
        double originalLevel = ellipse.level();
        Counters.reset();
        KEllipseMember moved = (KEllipseMember) ellipse.withHandle(3, Vector.xy(0, 6));
        assertEquals(0, Counters.snapshot().boundaryBuilds());
        assertEquals(0, Counters.snapshot().focalSumEvaluations());
        assertEquals(originalLevel, ellipse.level(), 0.0);
        assertEquals(Vector.xy(0, 3), ellipse.controlHandle());
        assertEquals(moved.focalSum(0, 6), moved.level(), 0.0);
        assertTrue(moved.level() > originalLevel);

        KEllipseMember movedFocus = (KEllipseMember) ellipse.withHandle(0, Vector.xy(-1, 0));
        assertEquals(Vector.xy(-1, 0), movedFocus.getHandle(0));
        assertEquals(Vector.xy(2, 0), movedFocus.getHandle(1));
        assertEquals(Vector.xy(4, 0), movedFocus.getHandle(2));
        assertNotEquals(originalLevel, movedFocus.level());
        assertThrows(IndexOutOfBoundsException.class, () -> ellipse.getHandle(4));
        assertThrows(IndexOutOfBoundsException.class, () -> ellipse.withHandle(-1, Vector.ZERO));
    }

    @Test
    void unpreparedDistanceThrows() {
        KEllipseMember ellipse = member(Vector.xy(0, 5), Vector.xy(0, 0), Vector.xy(1, 0), Vector.xy(0, 1));
        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> ellipse.distanceTo(Vector.xy(1, 1))
        );
        assertEquals("K_ELLIPSE distance requires preparation via ScenePreparer", ex.getMessage());
    }

    @Test
    void equalityFollowsGeometryNotIdentity() {
        KEllipseMember first = member(Vector.xy(0, 5), Vector.xy(0, 0), Vector.xy(1, 0), Vector.xy(0, 1));
        KEllipseMember second = member(Vector.xy(0, 5), Vector.xy(0, 0), Vector.xy(1, 0), Vector.xy(0, 1));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, first.withHandle(3, Vector.xy(0, 6)));
        List<Vector> supplied = new ArrayList<>(List.of(Vector.xy(0, 0), Vector.xy(1, 0), Vector.xy(0, 1)));
        KEllipseMember copied = new KEllipseMember(supplied, Vector.xy(0, 5));
        supplied.set(0, Vector.xy(9, 9));
        assertEquals(Vector.xy(0, 0), copied.getHandle(0));
    }

    private static KEllipseMember member(Vector handle, Vector... foci) {
        return new KEllipseMember(List.of(foci), handle);
    }
}
