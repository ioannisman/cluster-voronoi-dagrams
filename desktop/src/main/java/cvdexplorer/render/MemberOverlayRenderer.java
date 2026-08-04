package cvdexplorer.render;

import cvdexplorer.HandleVisibility;
import cvdexplorer.desktop.DrawingFxGeometry;
import cvdexplorer.geometry.Vector;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.PolygonMember;
import cvdexplorer.model.SegmentMember;
import javafx.scene.paint.Color;
import xyz.marsavic.drawingfx.drawing.View;
import xyz.marsavic.geometry.Line;

import java.util.List;

/**
 * Draws member overlays via drawing-fx {@link View}. Converts {@link cvdexplorer.geometry.Vector}
 * through {@link DrawingFxGeometry} temporarily; that bridge goes away when drawing-fx is replaced.
 */
public final class MemberOverlayRenderer {
    private static final Color SNAP_INDICATOR_COLOR = Color.hsb(60, 0.9, 1.0, 0.8);
    /** Stroke for the selected member’s segment and its active handle (distinct from snap yellow and white edges). */
    private static final Color SELECTION_STROKE = Color.hsb(265, 0.88, 0.52);

    private MemberOverlayRenderer() {
    }

    public static void drawMember(
            View view,
            ClusterMember member,
            Color clusterColor,
            boolean activeCluster,
            boolean memberSelected,
            int selectedHandleIndex,
            double handleRadiusWorld,
            double lineWidthWorld
    ) {
        Color edge = activeCluster ? Color.WHITE : Color.gray(0.1, 0.75);
        double lw = (memberSelected ? 4.0 : activeCluster ? 2.5 : 1.5) * lineWidthWorld;

        if (member instanceof SegmentMember sm) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            view.strokeLineSegment(DrawingFxGeometry.toDrawingFx(sm.a()), DrawingFxGeometry.toDrawingFx(sm.b()));
        } else if (member instanceof CircleMember cm) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            view.strokeCircleCentered(DrawingFxGeometry.toDrawingFx(cm.center()), cm.radius());
        } else if (member instanceof EllipseMember em) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            strokeEllipse(view, em);
        } else if (member instanceof LineMember lm) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            if (lm.b().sub(lm.a()).lengthSquared() > 0.0) {
                view.strokeLine(Line.pq(DrawingFxGeometry.toDrawingFx(lm.a()), DrawingFxGeometry.toDrawingFx(lm.b())));
            }
        } else if (member instanceof PolygonMember pm) {
            view.setLineWidth(lw);
            view.setStroke(memberSelected ? SELECTION_STROKE : edge);
            strokePolygon(view, pm);
        }

        view.setLineWidth(lw);
        for (int h = 0; h < member.handleCount(); h++) {
            if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                continue;
            }
            boolean handleSelected = memberSelected && h == selectedHandleIndex;
            view.setFill(clusterColor);
            view.fillCircleCentered(DrawingFxGeometry.toDrawingFx(member.getHandle(h)), handleRadiusWorld);
            view.setStroke(handleSelected ? SELECTION_STROKE : edge);
            view.strokeCircleCentered(DrawingFxGeometry.toDrawingFx(member.getHandle(h)), handleRadiusWorld);
        }
    }

    private static void strokeEllipse(View view, EllipseMember em) {
        List<Vector> outline = em.boundaryPolyline();
        if (outline.size() < 2) {
            return;
        }
        Vector prev = outline.get(outline.size() - 1);
        for (Vector point : outline) {
            view.strokeLineSegment(DrawingFxGeometry.toDrawingFx(prev), DrawingFxGeometry.toDrawingFx(point));
            prev = point;
        }
    }

    private static void strokePolygon(View view, PolygonMember pm) {
        List<Vector> verts = pm.vertices();
        if (verts.size() < 2) {
            return;
        }
        Vector prev = verts.get(verts.size() - 1);
        for (Vector point : verts) {
            view.strokeLineSegment(DrawingFxGeometry.toDrawingFx(prev), DrawingFxGeometry.toDrawingFx(point));
            prev = point;
        }
    }

    public static void drawSnapIndicator(View view, Vector position, double handleRadiusWorld, double lineWidthWorld) {
        double outerRadius = handleRadiusWorld * 1.8;
        view.setLineWidth(2.5 * lineWidthWorld);
        view.setStroke(SNAP_INDICATOR_COLOR);
        view.strokeCircleCentered(DrawingFxGeometry.toDrawingFx(position), outerRadius);
    }
}
