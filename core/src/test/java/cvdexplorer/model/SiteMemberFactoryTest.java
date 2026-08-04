package cvdexplorer.model;

import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteMemberFactoryTest {

    private static final Vector HINT = Vector.xy(100, -50);

    @Test
    void pointMemberUsesHintAsPosition() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.POINT, 0, 0, HINT);
        PointMember p = assertInstanceOf(PointMember.class, m);
        assertEquals(HINT.x(), p.position().x(), 1e-9);
        assertEquals(HINT.y(), p.position().y(), 1e-9);
    }

    @Test
    void lineSegmentMemberIsCenteredOnHint() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.LINE_SEGMENT, 1, 2, HINT);
        SegmentMember s = assertInstanceOf(SegmentMember.class, m);
        Vector mid = s.a().add(s.b()).mul(0.5);
        assertEquals(HINT.x(), mid.x(), 1e-9);
        assertEquals(HINT.y(), mid.y(), 1e-9);
        assertTrue(s.a().distanceTo(s.b()) > 1e-6);
    }

    @Test
    void circleMemberHasCenterAtHintAndPositiveRadius() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.CIRCLE, 2, 1, HINT);
        CircleMember c = assertInstanceOf(CircleMember.class, m);
        assertEquals(HINT.x(), c.center().x(), 1e-9);
        assertEquals(HINT.y(), c.center().y(), 1e-9);
        assertTrue(c.radius() > 1e-6);
    }

    @Test
    void ellipseMemberHasTwoFociAroundHint() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.ELLIPSE, 0, 0, HINT);
        EllipseMember e = assertInstanceOf(EllipseMember.class, m);
        assertEquals(3, e.handleCount());
        assertTrue(e.focusA().distanceTo(e.focusB()) > 1e-6);
        assertTrue(e.distanceTo(e.controlHandle()) < 1e-3);
    }

    @Test
    void lineMemberIsCenteredOnHint() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.LINE, 0, 3, HINT);
        LineMember l = assertInstanceOf(LineMember.class, m);
        Vector mid = l.a().add(l.b()).mul(0.5);
        assertEquals(HINT.x(), mid.x(), 1e-9);
        assertEquals(HINT.y(), mid.y(), 1e-9);
        assertTrue(l.a().distanceTo(l.b()) > 1e-6);
    }

    @Test
    void polygonMemberIsRegularNGonCenteredOnHint() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.POLYGON, 0, 0, HINT, 5);
        PolygonMember p = assertInstanceOf(PolygonMember.class, m);
        assertEquals(5, p.handleCount());
        Vector c = p.placementCentroid();
        assertEquals(HINT.x(), c.x(), 1e-6);
        assertEquals(HINT.y(), c.y(), 1e-6);
    }

    @Test
    void memberParameterIsClampedToMinimumThree() {
        ClusterMember m = SiteMemberFactory.createDefault(SiteMemberKind.POLYGON, 1, 0, HINT, 1);
        PolygonMember p = assertInstanceOf(PolygonMember.class, m);
        assertEquals(3, p.handleCount());
    }
}
