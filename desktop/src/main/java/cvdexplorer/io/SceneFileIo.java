package cvdexplorer.io;

import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SceneState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** UTF-8 JSON on disk; body is whatever {@link SceneJsonCodec} produces. */
public final class SceneFileIo {
    private SceneFileIo() {
    }

    public static void save(Path path, SceneState state) throws IOException {
        Files.writeString(path, SceneJsonCodec.encode(state.toSnapshot()), StandardCharsets.UTF_8);
    }

    public static void load(SceneState state, Path path) throws IOException, SceneJsonException {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        SceneSnapshot snapshot = SceneJsonCodec.parse(json);
        state.applyLoadedScene(
                snapshot.metricKind(),
                snapshot.neighborOrder(),
                snapshot.siteMemberKind(),
                snapshot.clusters(),
                snapshot.metricParameter(),
                snapshot.memberParameter()
        );
    }
}
