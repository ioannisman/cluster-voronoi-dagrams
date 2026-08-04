package cvdexplorer.io;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SiteMemberKind;
import org.junit.jupiter.api.Test;
import cvdexplorer.geometry.Vector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SceneFileIoTest {

    @Test
    void saveAndLoadRoundTripMatchesCodec() throws Exception {
        SceneState source = new SceneState();
        source.metricKind = MetricKind.MAXIMUM_DISTANCE;
        source.neighborOrder = NeighborOrder.NEAREST;
        source.siteMemberKind = SiteMemberKind.POINT;
        source.metricParameter = 2;
        source.clusters().clear();
        source.clusters().add(new ClusterSite(
                "Only",
                new Rgba(0.1, 0.2, 0.3, 1.0),
                List.of(new PointMember(Vector.xy(1, 2)), new PointMember(Vector.xy(3, 4)))
        ));
        source.numberOfClusters = 1;

        Path file = Files.createTempFile("cvd-scene-", ".json");
        try {
            SceneFileIo.save(file, source);

            SceneState loaded = new SceneState();
            loaded.metricKind = MetricKind.MINIMUM_DISTANCE;
            loaded.neighborOrder = NeighborOrder.FARTHEST;
            loaded.siteMemberKind = SiteMemberKind.LINE;
            loaded.clusters().add(new ClusterSite("X", Rgba.BLACK, List.of(new PointMember(Vector.xy(0, 0)))));
            loaded.numberOfClusters = 1;

            SceneFileIo.load(loaded, file);

            assertEquals(MetricKind.MAXIMUM_DISTANCE, loaded.metricKind);
            assertEquals(NeighborOrder.NEAREST, loaded.neighborOrder);
            assertEquals(SiteMemberKind.POINT, loaded.siteMemberKind);
            assertEquals(2, loaded.metricParameter);
            assertEquals(1, loaded.clusters().size());
            assertEquals("Only", loaded.clusters().get(0).name());
            assertEquals(2, loaded.clusters().get(0).members().size());
            PointMember p0 = (PointMember) loaded.clusters().get(0).members().get(0);
            assertEquals(1.0, p0.position().x(), 1e-9);
            assertEquals(2.0, p0.position().y(), 1e-9);
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
