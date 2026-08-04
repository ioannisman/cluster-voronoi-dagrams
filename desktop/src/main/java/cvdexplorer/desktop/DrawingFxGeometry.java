package cvdexplorer.desktop;

import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;

/**
 * Temporary bridge between {@link cvdexplorer.geometry} and the geometry types required by
 * the drawing-fx UI library ({@code xyz.marsavic.geometry.*}, which are bundled inside
 * {@code drawing-fx-*.jar}).
 * <p>
 * Domain, metrics, and rasterization must use {@code cvdexplorer.geometry} only.
 * Convert at drawing-fx call sites ({@link xyz.marsavic.drawingfx.drawing.View}, camera,
 * pointer events) with {@link #toDrawingFx} / {@link #fromDrawingFx}.
 * <p>
 * <b>Remove this class</b> when the desktop UI no longer depends on drawing-fx.
 */
public final class DrawingFxGeometry {
    private DrawingFxGeometry() {
    }

    public static Vector fromDrawingFx(xyz.marsavic.geometry.Vector v) {
        return Vector.xy(v.x(), v.y());
    }

    public static xyz.marsavic.geometry.Vector toDrawingFx(Vector v) {
        return xyz.marsavic.geometry.Vector.xy(v.x(), v.y());
    }

    public static Box fromDrawingFx(xyz.marsavic.geometry.Box b) {
        return Box.pq(fromDrawingFx(b.p()), fromDrawingFx(b.q()));
    }

    public static xyz.marsavic.geometry.Box toDrawingFx(Box b) {
        return xyz.marsavic.geometry.Box.pq(toDrawingFx(b.p()), toDrawingFx(b.q()));
    }

    public static Transformation fromDrawingFx(xyz.marsavic.geometry.Transformation t) {
        return new Transformation(t.mex(), t.mfx(), t.tx(), t.mey(), t.mfy(), t.ty());
    }

    public static xyz.marsavic.geometry.Transformation toDrawingFx(Transformation t) {
        return new xyz.marsavic.geometry.Transformation(
                t.mex(), t.mfx(), t.tx(), t.mey(), t.mfy(), t.ty()
        );
    }
}
