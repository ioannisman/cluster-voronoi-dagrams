package cvdexplorer.model;

import cvdexplorer.io.SceneGallery;

/**
 * Examples gadget values. DrawingFX enum combos need a constant per scene file;
 * the dropdown label comes from the JSON {@code name} via {@link #toString()}.
 */
public enum GalleryExample {
    /** Placeholder: leave the live scene unchanged (startup / after arbitrary Ctrl+O). */
    CURRENT(null),
    DEFAULT_CVD("default_cvd.json"),
    POINTS_NEAREST("points_nearest.json"),
    SEGMENTS_NEAREST("segments_nearest.json"),
    POLYGONS_NEAREST("polygons_nearest.json"),
    LINES_NEAREST("lines_nearest.json"),
    CIRCLES_NEAREST("circles_nearest.json"),
    ELLIPSES_NEAREST("ellipses_nearest.json"),
    K_ELLIPSES_NEAREST("k_ellipses_nearest.json"),
    POINTS_FARTHEST("points_farthest.json"),
    SEGMENTS_FARTHEST("segments_farthest.json"),
    POLYGONS_FARTHEST("polygons_farthest.json"),
    LINES_FARTHEST("lines_farthest.json"),
    CIRCLES_FARTHEST("circles_farthest.json"),
    ELLIPSES_FARTHEST("ellipses_farthest.json"),
    K_ELLIPSES_FARTHEST("k_ellipses_farthest.json");

    private final String fileName;

    GalleryExample(String fileName) {
        this.fileName = fileName;
    }

    /** Gallery JSON filename, or {@code null} for {@link #CURRENT}. */
    public String fileName() {
        return fileName;
    }

    @Override
    public String toString() {
        if (fileName == null) {
            return "See list of pre-defined examples";
        }
        return SceneGallery.displayName(fileName);
    }
}
