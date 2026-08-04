package cvdexplorer.model;

/** Caps for cluster and member counts. */
public final class SceneLimits {
    public static final int MAX_CLUSTERS = 32;
    public static final int MAX_MEMBERS_PER_CLUSTER = 32;
    public static final int MIN_POLYGON_VERTICES = 3;
    public static final int MAX_POLYGON_VERTICES = 16;
    public static final int DEFAULT_MEMBER_PARAMETER = 4;

    private SceneLimits() {
    }
}
