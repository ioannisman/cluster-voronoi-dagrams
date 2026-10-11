package cvdexplorer.kellipse;

/**
 * Frame-rate-independent limits for the k-ellipse polyline approximation.
 * Tolerances are world-space distances.
 */
public record KEllipseApproximationSettings(
        double chordTolerance,
        double rootTolerance,
        int initialAngularSamples,
        int maxRefinementDepth,
        int maxSegments,
        int maxBisectionIterations,
        int maxBracketExpansions,
        double minInnerRadius,
        int maxInteriorIterations
) {
    public static final KEllipseApproximationSettings DEFAULT = new KEllipseApproximationSettings(
            0.05,
            1e-7,
            64,
            6,
            4096,
            200,
            8,
            1e-3,
            200
    );

    public KEllipseApproximationSettings {
        requirePositive(chordTolerance, "chordTolerance");
        requirePositive(rootTolerance, "rootTolerance");
        requirePositive(minInnerRadius, "minInnerRadius");
        if (initialAngularSamples < 3) {
            throw new IllegalArgumentException("initialAngularSamples must be at least 3");
        }
        if (maxRefinementDepth < 0) {
            throw new IllegalArgumentException("maxRefinementDepth must be non-negative");
        }
        if (maxSegments < initialAngularSamples) {
            throw new IllegalArgumentException("maxSegments must be at least the initial angular sample count");
        }
        if (maxBisectionIterations < 1) {
            throw new IllegalArgumentException("maxBisectionIterations must be at least 1");
        }
        if (maxBracketExpansions < 0) {
            throw new IllegalArgumentException("maxBracketExpansions must be non-negative");
        }
        if (maxInteriorIterations < 1) {
            throw new IllegalArgumentException("maxInteriorIterations must be at least 1");
        }
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
