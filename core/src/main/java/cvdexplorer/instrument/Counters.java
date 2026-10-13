package cvdexplorer.instrument;

import java.util.concurrent.atomic.LongAdder;

/**
 * Optional preparation and query counters. Hot paths increment an adder only when
 * {@link #ENABLED} is true ({@code -Dcvd.counters=true}). Gradle test tasks set that flag.
 */
public final class Counters {
    public static final boolean ENABLED = Boolean.getBoolean("cvd.counters");

    public static final LongAdder FOCAL_SUM_EVALUATIONS = new LongAdder();
    public static final LongAdder BOUNDARY_BUILDS = new LongAdder();
    public static final LongAdder INDEX_BUILDS = new LongAdder();
    public static final LongAdder INDEX_QUERIES = new LongAdder();
    public static final LongAdder GRID_BUILDS = new LongAdder();
    public static final LongAdder GRID_INTERPOLATIONS = new LongAdder();

    private Counters() {
    }

    public static void reset() {
        FOCAL_SUM_EVALUATIONS.reset();
        BOUNDARY_BUILDS.reset();
        INDEX_BUILDS.reset();
        INDEX_QUERIES.reset();
        GRID_BUILDS.reset();
        GRID_INTERPOLATIONS.reset();
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                FOCAL_SUM_EVALUATIONS.sum(),
                BOUNDARY_BUILDS.sum(),
                INDEX_BUILDS.sum(),
                INDEX_QUERIES.sum(),
                GRID_BUILDS.sum(),
                GRID_INTERPOLATIONS.sum()
        );
    }

    public record Snapshot(
            long focalSumEvaluations,
            long boundaryBuilds,
            long indexBuilds,
            long indexQueries,
            long gridBuilds,
            long gridInterpolations
    ) {
    }
}
