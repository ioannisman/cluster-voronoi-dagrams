package cvdexplorer.field;

import cvdexplorer.geometry.Box;

/**
 * Uniform world-space grid. Node {@code (i, j)} is at
 * {@code (originX + i * spacing, originY + j * spacing)}. {@code version} identifies the domain,
 * not the samples.
 */
public record DistanceGridSpec(
        double originX,
        double originY,
        double spacing,
        int nodesX,
        int nodesY,
        long version
) {
    public DistanceGridSpec {
        if (!Double.isFinite(originX) || !Double.isFinite(originY) || !Double.isFinite(spacing) || spacing <= 0.0) {
            throw new IllegalArgumentException("Distance grid origin and spacing must be finite, with spacing > 0");
        }
        if (nodesX < 2 || nodesY < 2) {
            throw new IllegalArgumentException("Distance grid needs at least 2 nodes on each axis");
        }
    }

    public double maxX() {
        return originX + (nodesX - 1) * spacing;
    }

    public double maxY() {
        return originY + (nodesY - 1) * spacing;
    }

    public int nodeCount() {
        return nodesX * nodesY;
    }

    /** Sample storage in bytes. Samples are {@code float}. */
    public long sampleBytes() {
        return (long) nodeCount() * 4L;
    }

    /** Inclusive coverage of the oriented box. */
    public boolean covers(Box box) {
        Box oriented = box.positive();
        return oriented.p().x() >= originX
                && oriented.q().x() <= maxX()
                && oriented.p().y() >= originY
                && oriented.q().y() <= maxY();
    }

    /**
     * Smallest grid-aligned spec whose nodes cover {@code box}. The origin is a multiple of
     * {@code spacing}. A zero-extent box still gets two nodes so interpolation has a cell.
     */
    public static DistanceGridSpec alignedCovering(Box box, double spacing, long version) {
        if (box == null) {
            throw new IllegalArgumentException("Box is required");
        }
        if (!Double.isFinite(spacing) || spacing <= 0.0) {
            throw new IllegalArgumentException("Spacing must be positive");
        }
        Box oriented = box.positive();
        double minX = oriented.p().x();
        double minY = oriented.p().y();
        double maxX = oriented.q().x();
        double maxY = oriented.q().y();
        if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(maxX) || !Double.isFinite(maxY)) {
            throw new IllegalArgumentException("Box coordinates must be finite");
        }
        double originX = alignedOrigin(minX, spacing);
        double originY = alignedOrigin(minY, spacing);
        return new DistanceGridSpec(
                originX,
                originY,
                spacing,
                nodeCount(originX, maxX, spacing),
                nodeCount(originY, maxY, spacing),
                version
        );
    }

    private static double alignedOrigin(double min, double spacing) {
        double origin = Math.floor(min / spacing) * spacing;
        if (origin > min) {
            origin -= spacing;
        }
        return origin;
    }

    private static int nodeCount(double origin, double max, double spacing) {
        double span = max - origin;
        long count = (long) Math.ceil(span / spacing) + 1L;
        if (count < 2L) {
            count = 2L;
        }
        double covered = origin + (count - 1L) * spacing;
        if (covered < max) {
            count++;
        }
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Distance grid is too large");
        }
        return (int) count;
    }
}
