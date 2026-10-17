package cvdexplorer;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.PointMember;
import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandleVisibilityTest {

    @Test
    void circleShowsRadiusHandleUntilSelectedThenAll() {
        CircleMember circle = new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0));

        assertEquals(1, HandleVisibility.primaryHandleIndex(circle));
        assertFalse(HandleVisibility.isVisible(circle, 0, false));
        assertTrue(HandleVisibility.isVisible(circle, 1, false));
        assertTrue(HandleVisibility.isVisible(circle, 0, true));
        assertTrue(HandleVisibility.isVisible(circle, 1, true));
    }

    @Test
    void ellipseShowsControlHandleUntilSelectedThenAll() {
        EllipseMember ellipse = new EllipseMember(
                Vector.xy(-5, 0),
                Vector.xy(5, 0),
                Vector.xy(0, 8)
        );

        assertEquals(2, HandleVisibility.primaryHandleIndex(ellipse));
        assertFalse(HandleVisibility.isVisible(ellipse, 0, false));
        assertFalse(HandleVisibility.isVisible(ellipse, 1, false));
        assertTrue(HandleVisibility.isVisible(ellipse, 2, false));
        assertTrue(HandleVisibility.isVisible(ellipse, 0, true));
        assertTrue(HandleVisibility.isVisible(ellipse, 1, true));
        assertTrue(HandleVisibility.isVisible(ellipse, 2, true));
    }

    @Test
    void kEllipseShowsControlHandleUntilSelectedThenAll() {
        KEllipseMember ellipse = new KEllipseMember(
                List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                Vector.xy(0, 8)
        );

        assertEquals(3, HandleVisibility.primaryHandleIndex(ellipse));
        assertFalse(HandleVisibility.isVisible(ellipse, 0, false));
        assertFalse(HandleVisibility.isVisible(ellipse, 1, false));
        assertFalse(HandleVisibility.isVisible(ellipse, 2, false));
        assertTrue(HandleVisibility.isVisible(ellipse, 3, false));
        assertTrue(HandleVisibility.isVisible(ellipse, 0, true));
        assertTrue(HandleVisibility.isVisible(ellipse, 3, true));
    }

    @Test
    void nonEllipseHandlesRemainVisible() {
        PointMember point = new PointMember(Vector.xy(1, 2));

        assertEquals(0, HandleVisibility.primaryHandleIndex(point));
        assertTrue(HandleVisibility.isVisible(point, 0, false));
    }

    @Test
    void lineSecondHandleIsHiddenUntilSelected() {
        LineMember line = new LineMember(Vector.xy(0, 0), Vector.xy(3, 0));

        assertEquals(0, HandleVisibility.primaryHandleIndex(line));
        assertTrue(HandleVisibility.isVisible(line, 0, false));
        assertFalse(HandleVisibility.isVisible(line, 1, false));
        assertTrue(HandleVisibility.isVisible(line, 1, true));
    }
}
