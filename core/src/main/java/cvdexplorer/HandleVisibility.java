package cvdexplorer;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.PointMember;

public final class HandleVisibility {
    private HandleVisibility() {
    }

    /**
     * Handle marked current when the member is selected from the keyboard or from its stroke.
     * The curve control for a circle or ellipse, the control handle of a k-ellipse, otherwise the first handle.
     */
    public static int primaryHandleIndex(ClusterMember member) {
        if (member instanceof CircleMember) {
            return 1;
        }
        if (member instanceof EllipseMember) {
            return 2;
        }
        if (member instanceof KEllipseMember kEllipse) {
            return kEllipse.focusCount();
        }
        return 0;
    }

    /**
     * A point handle is always shown. Every other handle is shown when the member is selected,
     * or when {@code strokeDrawn} is false because the member has no stroke to click.
     * Handles of one member share this answer.
     */
    public static boolean isVisible(
            ClusterMember member,
            int handleIndex,
            boolean memberSelected,
            boolean strokeDrawn
    ) {
        if (member instanceof PointMember) {
            return handleIndex == 0;
        }
        return memberSelected || !strokeDrawn;
    }
}
