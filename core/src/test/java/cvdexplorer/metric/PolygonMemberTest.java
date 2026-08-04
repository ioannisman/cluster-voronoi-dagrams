package cvdexplorer.metric;

import cvdexplorer.geometry.Vector;
import cvdexplorer.model.PolygonMember;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolygonMemberTest {

    private static PolygonMember unitSquare() {
        return new PolygonMember(List.of(
                Vector.xy(0, 0),
                Vector.xy(10, 0),
                Vector.xy(10, 10),
                Vector.xy(0, 10)
        ));
    }

    @Test
    void rejectsFewerThanThreeVertices() {
        assertThrows(IllegalArgumentException.class, () ->
                new PolygonMember(List.of(Vector.xy(0, 0), Vector.xy(1, 0)))
        );
    }

    @Test
    void distanceToInteriorOfEdgeIsPerpendicular() {
        PolygonMember poly = unitSquare();
        assertEquals(3.0, poly.distanceTo(Vector.xy(5, -3)), 1e-9);
    }

    @Test
    void distanceClampsToVertices() {
        PolygonMember poly = unitSquare();
        assertEquals(Math.sqrt(18), poly.distanceTo(Vector.xy(-3, -3)), 1e-9);
    }

    @Test
    void closedEdgeFromLastToFirstIsIncluded() {
        PolygonMember triangle = new PolygonMember(List.of(
                Vector.xy(0, 0),
                Vector.xy(10, 0),
                Vector.xy(0, 10)
        ));
        // Closest feature is the closing edge from (0,10) to (0,0)
        assertEquals(2.0, triangle.distanceTo(Vector.xy(-2, 5)), 1e-9);
    }

    @Test
    void withHandleReturnsNewInstance() {
        PolygonMember poly = unitSquare();
        var moved = poly.withHandle(1, Vector.xy(12, 0));
        assertNotSame(poly, moved);
        assertEquals(Vector.xy(10, 0), poly.getHandle(1));
        assertEquals(Vector.xy(12, 0), moved.getHandle(1));
        assertEquals(4, moved.handleCount());
    }

    @Test
    void placementCentroidIsAverageOfVertices() {
        PolygonMember poly = unitSquare();
        Vector c = poly.placementCentroid();
        assertEquals(5.0, c.x(), 1e-9);
        assertEquals(5.0, c.y(), 1e-9);
    }
}
