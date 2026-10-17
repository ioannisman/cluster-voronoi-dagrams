package cvdexplorer.io;

import cvdexplorer.core.ScenePreparer;
import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.geometry.Box;
import cvdexplorer.kellipse.KEllipsePreparationException;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SceneState;

/**
 * Prepares a loaded scene on a new cache, then copies it into {@code state}.
 * A preparation failure leaves {@code state} as it was.
 */
public final class PreparedSceneInstall {
    private PreparedSceneInstall() {
    }

    public static ScenePreparer install(
            SceneState state,
            SceneSnapshot snapshot,
            Box bounds,
            DistanceGridLimits limits
    ) throws KEllipsePreparationException {
        ScenePreparer trial = ScenePreparer.prepareNew(
                snapshot.clusters(),
                snapshot.metricKind(),
                snapshot.neighborOrder(),
                snapshot.metricParameter(),
                bounds,
                limits
        );
        state.applyLoadedScene(
                snapshot.metricKind(),
                snapshot.neighborOrder(),
                snapshot.siteMemberKind(),
                snapshot.clusters(),
                snapshot.metricParameter(),
                snapshot.memberParameter()
        );
        return trial;
    }
}
