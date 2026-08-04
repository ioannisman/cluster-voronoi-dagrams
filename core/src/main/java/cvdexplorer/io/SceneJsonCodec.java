package cvdexplorer.io;

import cvdexplorer.model.SceneSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Human-readable scene file encode/decode via Gson. */
public final class SceneJsonCodec {
    public static final String CURRENT_VERSION = SceneFileFormat.CURRENT_VERSION;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SceneJsonCodec() {
    }

    public static String encode(SceneSnapshot snapshot) {
        return GSON.toJson(SceneFileFormat.toDto(snapshot));
    }

    /** Parse JSON into a {@link SceneSnapshot} (authoring fields + clusters). JVM / desktop path (Gson). */
    public static SceneSnapshot parse(String json) throws SceneJsonException {
        SceneFileFormat.SceneFileV1 dto;
        try {
            dto = GSON.fromJson(json, SceneFileFormat.SceneFileV1.class);
        } catch (Exception e) {
            throw new SceneJsonException("Invalid JSON", e);
        }
        if (dto == null) {
            throw new SceneJsonException("Empty scene file");
        }
        return SceneFileFormat.fromDto(dto);
    }
}
