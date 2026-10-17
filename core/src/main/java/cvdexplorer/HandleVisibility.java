package cvdexplorer;

import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.LineMember;

public final class HandleVisibility {
    private HandleVisibility() {
    }

    /**
     * Handle shown even when the member is not selected (curve control for circle/ellipse;
     * first endpoint for lines). Returns {@code 0} for types that show all handles.
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

    public static boolean isVisible(ClusterMember member, int handleIndex, boolean memberSelected) {
        if (member instanceof CircleMember
                || member instanceof EllipseMember
                || member instanceof KEllipseMember
                || member instanceof LineMember) {
            return handleIndex == primaryHandleIndex(member) || memberSelected;
        }
        return true;
    }
}
