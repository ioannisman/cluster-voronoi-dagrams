package cvdexplorer.field;

/**
 * Hard caps for a signed-distance grid. Exceeding them is a failed preparation,
 * not a reason to widen the spacing.
 */
public record DistanceGridLimits(int maxNodesPerAxis, int maxNodesPerGrid, long maxTotalSampleBytes) {
    public static final DistanceGridLimits DEFAULT = new DistanceGridLimits(
            8192,
            16_777_216,
            256L * 1024L * 1024L
    );

    public DistanceGridLimits {
        if (maxNodesPerAxis < 2) {
            throw new IllegalArgumentException("maxNodesPerAxis must be at least 2");
        }
        if (maxNodesPerGrid < 4) {
            throw new IllegalArgumentException("maxNodesPerGrid must be at least 4");
        }
        if (maxTotalSampleBytes < 16) {
            throw new IllegalArgumentException("maxTotalSampleBytes must be at least 16");
        }
    }
}
