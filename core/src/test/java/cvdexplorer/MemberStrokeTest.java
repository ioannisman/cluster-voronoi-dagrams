package cvdexplorer;

import cvdexplorer.geometry.Vector;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.PolygonMember;
import cvdexplorer.model.SegmentMember;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberStrokeTest {

    @Test
    void segmentDistanceIsToTheSegment() {
        SegmentMember segment = new SegmentMember(Vector.xy(0, 0), Vector.xy(4, 0));

        assertTrue(MemberStroke.isDrawn(segment, null));
        assertEquals(3.0, MemberStroke.distanceTo(segment, Vector.xy(2, 3), null), 1e-9);
        assertEquals(2.0, MemberStroke.distanceTo(segment, Vector.xy(6, 0), null), 1e-9);
    }

    @Test
    void polygonDistanceIsToTheBoundary() {
        PolygonMember polygon = new PolygonMember(List.of(
                Vector.xy(0, 0),
                Vector.xy(4, 0),
                Vector.xy(4, 2),
                Vector.xy(0, 2)
        ));

        assertTrue(MemberStroke.isDrawn(polygon, null));
        assertEquals(1.0, MemberStroke.distanceTo(polygon, Vector.xy(2, 3), null), 1e-9);
        assertEquals(0.0, MemberStroke.distanceTo(polygon, Vector.xy(4, 1), null), 1e-9);
    }

    @Test
    void circleDistanceIsToTheCircumference() {
        CircleMember circle = new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0));

        assertTrue(MemberStroke.isDrawn(circle, null));
        assertEquals(1.0, MemberStroke.distanceTo(circle, Vector.xy(0, 4), null), 1e-9);
        assertEquals(0.0, MemberStroke.distanceTo(circle, Vector.xy(3, 0), null), 1e-9);
    }

    @Test
    void lineDistanceIsToTheInfiniteLine() {
        LineMember line = new LineMember(Vector.xy(0, 0), Vector.xy(3, 0));

        assertTrue(MemberStroke.isDrawn(line, null));
        assertEquals(1.0, MemberStroke.distanceTo(line, Vector.xy(-10, 1), null), 1e-9);
    }

    @Test
    void kEllipseUsesTheClosedOutline() {
        KEllipseMember ellipse = new KEllipseMember(
                List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                Vector.xy(0, 8)
        );
        List<Vector> outline = List.of(
                Vector.xy(0, 0),
                Vector.xy(10, 0),
                Vector.xy(10, 10),
                Vector.xy(0, 10)
        );

        assertTrue(MemberStroke.isDrawn(ellipse, outline));
        assertEquals(1.0, MemberStroke.distanceTo(ellipse, Vector.xy(5, -1), outline), 1e-9);
        assertEquals(1.0, MemberStroke.distanceTo(ellipse, Vector.xy(-1, 5), outline), 1e-9);
        assertFalse(MemberStroke.isDrawn(ellipse, null));
        assertFalse(MemberStroke.isDrawn(ellipse, List.of(Vector.xy(0, 0))));
        assertEquals(Double.POSITIVE_INFINITY, MemberStroke.distanceTo(ellipse, Vector.xy(5, -1), null));
    }

    @Test
    void degenerateEllipseAndPointAreNotStrokes() {
        EllipseMember degenerate = new EllipseMember(Vector.xy(-1, 0), Vector.xy(1, 0), Vector.xy(0, 0));
        EllipseMember ellipse = new EllipseMember(Vector.xy(-5, 0), Vector.xy(5, 0), Vector.xy(0, 8));
        PointMember point = new PointMember(Vector.xy(1, 2));

        assertTrue(degenerate.isDegenerate());
        assertFalse(MemberStroke.isDrawn(degenerate, null));
        assertEquals(Double.POSITIVE_INFINITY, MemberStroke.distanceTo(degenerate, Vector.xy(0, 1), null));
        assertTrue(MemberStroke.isDrawn(ellipse, null));
        assertFalse(MemberStroke.isDrawn(point, null));
        assertEquals(Double.POSITIVE_INFINITY, MemberStroke.distanceTo(point, Vector.xy(1, 2), null));
    }
}
