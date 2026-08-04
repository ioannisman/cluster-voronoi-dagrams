package cvdexplorer.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformationTest {
    @Test
    void identityLeavesPointsUnchanged() {
        Vector p = Vector.xy(12.5, -7.25);
        assertEquals(p, Transformation.IDENTITY.applyTo(p));
    }

    @Test
    void scalingThenTranslationMatchesPixelToWorldOrder() {
        // Scale then translate matches typical pixel-to-world composition.
        double sx = 2.0;
        double sy = -3.0;
        double tx = -100.0;
        double ty = 200.0;
        Transformation t = Transformation.scaling(sx, sy).then(Transformation.translation(Vector.xy(tx, ty)));

        Vector pixel = Vector.xy(10, 20);
        Vector expected = Vector.xy(sx * 10 + tx, sy * 20 + ty);
        assertEquals(expected, t.applyTo(pixel));
    }

    @Test
    void inverseUndoesAffineMap() {
        Transformation t = Transformation.scaling(2, -0.5).then(Transformation.translation(Vector.xy(3, 7)));
        Vector p = Vector.xy(11, -4);
        assertEquals(p.x(), t.inverse().applyTo(t.applyTo(p)).x(), 1e-12);
        assertEquals(p.y(), t.inverse().applyTo(t.applyTo(p)).y(), 1e-12);
    }

    @Test
    void thenIsApplyAfterReversed() {
        Transformation a = Transformation.scaling(2, 3);
        Transformation b = Transformation.translation(Vector.xy(4, 5));
        Vector p = Vector.xy(1, 1);
        assertEquals(a.then(b).applyTo(p), b.applyAfter(a).applyTo(p));
    }
}
