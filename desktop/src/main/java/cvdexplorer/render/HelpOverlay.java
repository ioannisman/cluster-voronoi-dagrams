package cvdexplorer.render;

import xyz.marsavic.drawingfx.drawing.DrawingUtils;
import xyz.marsavic.drawingfx.drawing.View;

public final class HelpOverlay {
    private HelpOverlay() {
    }

    public static void draw(View view) {
        DrawingUtils.drawInfoText(
                view,
                "Cluster Voronoi Diagrams Explorer",
                "",
                "Controls:",
                "    h               - Toggle help",
                "    s               - Toggle distance shading (gadget)",
                "    m               - Toggle cluster members",
                "    c               - Toggle colored regions",
                "    k               - Toggle skeleton overlay",
                "    v               - Toggle region subdivision (nearest/farthest member within cluster)",
                "    g               - Toggle snap to grid",
                "    f               - Toggle snap to handles",
                "    n / p           - Next / previous member in active cluster",
                "    Shift+n / Shift+p - Next / previous active cluster",
                "    a               - Add member (type from gadget) at pointer",
                "    d               - Remove selected member",
                "    Shift+a         - Add cluster",
                "    Shift+d         - Remove active cluster",
                "    r               - Reset to the demo scene",
                "    Ctrl+s          - Save scene (JSON)",
                "    Ctrl+o          - Load scene (JSON)",
                "    Mouse left      - Select/drag a handle; circle/ellipse show the curve handle; other handles appear after selection",
                "    Shift+drag      - Move only selected handle (unsnap from co-located handles)",
                "    Fast draw preview (gadget) - Lower diagram resolution while dragging; full quality on release",
                "    Ctrl            - Control the view:",
                "      + Mouse left      - Pan",
                "      + Mouse wheel     - Zoom",
                "      + Mouse right     - Reset"
        );
    }
}
