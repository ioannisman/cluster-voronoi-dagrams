package cvdexplorer.io;

import cvdexplorer.core.DomainLimitExceededException;
import cvdexplorer.core.ScenePreparer;
import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SiteMemberKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparedSceneInstallTest {

    @Test
    void leavesTheCurrentSceneWhenTheCandidateDoesNotFit() {
        SceneState state = pointScene();
        SceneSnapshot candidate = kEllipseScene(3);
        Box view = Box.pq(Vector.xy(-40, -40), Vector.xy(40, 40));

        assertThrows(DomainLimitExceededException.class, () -> PreparedSceneInstall.install(
                state,
                candidate,
                view,
                new DistanceGridLimits(8192, 16_777_216, 64)
        ));

        assertEquals("Keep", state.clusters().get(0).name());
        assertInstanceOf(PointMember.class, state.clusters().get(0).members().get(0));
        assertEquals(SiteMemberKind.POINT, state.siteMemberKind);
    }

    @Test
    void installsACandidateWhoseGridsAlreadyFit() throws Exception {
        SceneState state = pointScene();
        SceneSnapshot candidate = kEllipseScene(1);
        Box view = Box.pq(Vector.xy(-40, -40), Vector.xy(40, 40));

        ScenePreparer installed = PreparedSceneInstall.install(
                state,
                candidate,
                view,
                DistanceGridLimits.DEFAULT
        );

        assertTrue(installed.statistics().cachedMembers() > 0);
        assertEquals("Incoming", state.clusters().get(0).name());
        assertInstanceOf(KEllipseMember.class, state.clusters().get(0).members().get(0));
        assertEquals(SiteMemberKind.K_ELLIPSE, state.siteMemberKind);
    }

    private static SceneState pointScene() {
        SceneState state = new SceneState();
        state.siteMemberKind = SiteMemberKind.POINT;
        state.metricKind = MetricKind.MINIMUM_DISTANCE;
        state.neighborOrder = NeighborOrder.NEAREST;
        state.clusters().add(new ClusterSite(
                "Keep",
                Rgba.RED,
                List.of(new PointMember(Vector.xy(1, 2)))
        ));
        state.numberOfClusters = 1;
        return state;
    }

    private static SceneSnapshot kEllipseScene(int count) {
        SceneSnapshot snapshot = new SceneSnapshot();
        snapshot.setSiteMemberKind(SiteMemberKind.K_ELLIPSE);
        snapshot.setMetricKind(MetricKind.MINIMUM_DISTANCE);
        snapshot.setNeighborOrder(NeighborOrder.NEAREST);
        List<ClusterMember> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double x = i * 12.0;
            members.add(new KEllipseMember(
                    List.of(Vector.xy(x - 2, 0), Vector.xy(x + 2, 0), Vector.xy(x, 2)),
                    Vector.xy(x, 12)
            ));
        }
        snapshot.setClusters(List.of(new ClusterSite("Incoming", Rgba.RED, members)));
        return snapshot;
    }
}
