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
    K_ELLIPSES("k_ellipses.json");

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
