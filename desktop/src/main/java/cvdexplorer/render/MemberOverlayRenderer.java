package cvdexplorer.render;

import cvdexplorer.HandleVisibility;
import cvdexplorer.MemberStroke;
import cvdexplorer.desktop.DrawingFxGeometry;
import cvdexplorer.geometry.Vector;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
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
    /** Stroke for the selected member and its active handle, distinct from the snap indicator. */
    private static final Color SELECTION_STROKE = Color.hsb(265, 0.88, 0.52);
    /** Dark line under a cluster-colored stroke so the site stays visible on a region of the same color. */
    private static final Color STROKE_UNDERLAY = Color.gray(0.08, 0.95);
    private static final double UNDERLAY_EXTRA_PIXELS = 2.0;

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
            double lineWidthWorld,
            List<Vector> outline
    ) {
        double handleLineWidth = (memberSelected ? 4.0 : activeCluster ? 2.5 : 1.5) * lineWidthWorld;
        double lw = handleLineWidth + lineWidthWorld;
        Color stroke = memberSelected ? SELECTION_STROKE : clusterColor;
        List<Vector> ellipseBoundary = member instanceof EllipseMember ellipse ? ellipse.boundaryPolyline() : null;
        strokeGeometry(view, member, outline, ellipseBoundary, STROKE_UNDERLAY, lw + UNDERLAY_EXTRA_PIXELS * lineWidthWorld);
        strokeGeometry(view, member, outline, ellipseBoundary, stroke, lw);

        view.setLineWidth(handleLineWidth);
        boolean strokeDrawn = MemberStroke.isDrawn(member, outline);
        for (int h = 0; h < member.handleCount(); h++) {
            if (!HandleVisibility.isVisible(member, h, memberSelected, strokeDrawn)) {
                continue;
            }
            boolean handleSelected = memberSelected && h == selectedHandleIndex;
            view.setFill(clusterColor);
            view.fillCircleCentered(DrawingFxGeometry.toDrawingFx(member.getHandle(h)), handleRadiusWorld);
            view.setStroke(handleSelected ? SELECTION_STROKE : STROKE_UNDERLAY);
            view.strokeCircleCentered(DrawingFxGeometry.toDrawingFx(member.getHandle(h)), handleRadiusWorld);
        }
    }

    private static void strokeGeometry(
            View view,
            ClusterMember member,
            List<Vector> outline,
            List<Vector> ellipseBoundary,
            Color color,
            double width
    ) {
        view.setLineWidth(width);
        view.setStroke(color);
        if (member instanceof SegmentMember segment) {
            view.strokeLineSegment(DrawingFxGeometry.toDrawingFx(segment.a()), DrawingFxGeometry.toDrawingFx(segment.b()));
        } else if (member instanceof CircleMember circle) {
            view.strokeCircleCentered(DrawingFxGeometry.toDrawingFx(circle.center()), circle.radius());
        } else if (member instanceof EllipseMember) {
            strokeClosed(view, ellipseBoundary);
        } else if (member instanceof LineMember line) {
            if (line.b().sub(line.a()).lengthSquared() > 0.0) {
                view.strokeLine(Line.pq(DrawingFxGeometry.toDrawingFx(line.a()), DrawingFxGeometry.toDrawingFx(line.b())));
            }
        } else if (member instanceof PolygonMember polygon) {
            strokeClosed(view, polygon.vertices());
        } else if (member instanceof KEllipseMember && outline != null) {
            strokeClosed(view, outline);
        }
    }

    private static void strokeClosed(View view, List<Vector> points) {
        if (points.size() < 2) {
            return;
        }
        Vector prev = points.get(points.size() - 1);
        for (Vector point : points) {
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
