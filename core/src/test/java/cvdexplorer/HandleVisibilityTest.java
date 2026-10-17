package cvdexplorer;

import cvdexplorer.geometry.Vector;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
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

class HandleVisibilityTest {

    @Test
    void pointHandleStaysVisible() {
        PointMember point = new PointMember(Vector.xy(1, 2));

        assertEquals(0, HandleVisibility.primaryHandleIndex(point));
        assertTrue(HandleVisibility.isVisible(point, 0, false, true));
        assertTrue(HandleVisibility.isVisible(point, 0, false, false));
        assertTrue(HandleVisibility.isVisible(point, 0, true, true));
    }

    @Test
    void drawnStrokeHidesEveryHandleUntilSelected() {
        List<ClusterMember> members = List.of(
                new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0)),
                new EllipseMember(Vector.xy(-5, 0), Vector.xy(5, 0), Vector.xy(0, 8)),
                new KEllipseMember(
                        List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                        Vector.xy(0, 8)
                ),
                new LineMember(Vector.xy(0, 0), Vector.xy(3, 0)),
                new SegmentMember(Vector.xy(0, 0), Vector.xy(4, 0)),
                new PolygonMember(List.of(Vector.xy(0, 0), Vector.xy(4, 0), Vector.xy(2, 3)))
        );

        for (ClusterMember member : members) {
            for (int handle = 0; handle < member.handleCount(); handle++) {
                assertFalse(HandleVisibility.isVisible(member, handle, false, true));
                assertTrue(HandleVisibility.isVisible(member, handle, true, true));
            }
        }
    }

    @Test
    void missingStrokeShowsEveryHandle() {
        CircleMember circle = new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0));
        KEllipseMember ellipse = new KEllipseMember(
                List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                Vector.xy(0, 8)
        );

        assertTrue(HandleVisibility.isVisible(circle, 0, false, false));
        assertTrue(HandleVisibility.isVisible(circle, 1, false, false));
        for (int handle = 0; handle < ellipse.handleCount(); handle++) {
            assertTrue(HandleVisibility.isVisible(ellipse, handle, false, false));
        }
    }

    @Test
    void primaryHandleIsTheCurveControlOrTheFirstHandle() {
        CircleMember circle = new CircleMember(Vector.xy(0, 0), Vector.xy(3, 0));
        EllipseMember ellipse = new EllipseMember(Vector.xy(-5, 0), Vector.xy(5, 0), Vector.xy(0, 8));
        KEllipseMember kEllipse = new KEllipseMember(
                List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                Vector.xy(0, 8)
        );
        LineMember line = new LineMember(Vector.xy(0, 0), Vector.xy(3, 0));
        SegmentMember segment = new SegmentMember(Vector.xy(0, 0), Vector.xy(4, 0));
        PolygonMember polygon = new PolygonMember(List.of(Vector.xy(0, 0), Vector.xy(4, 0), Vector.xy(2, 3)));

        assertEquals(1, HandleVisibility.primaryHandleIndex(circle));
        assertEquals(2, HandleVisibility.primaryHandleIndex(ellipse));
        assertEquals(3, HandleVisibility.primaryHandleIndex(kEllipse));
        assertEquals(0, HandleVisibility.primaryHandleIndex(line));
        assertEquals(0, HandleVisibility.primaryHandleIndex(segment));
        assertEquals(0, HandleVisibility.primaryHandleIndex(polygon));
    }
}
