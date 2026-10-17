package cvdexplorer.field;

import cvdexplorer.core.DomainLimitExceededException;
import cvdexplorer.geometry.Box;

/**
 * Uniform world-space grid. Node {@code (i, j)} is at
 * {@code (originX + i * spacing, originY + j * spacing)}.
 */
public record DistanceGridSpec(
        double originX,
        double originY,
        double spacing,
        int nodesX,
        int nodesY
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

    public long nodeCount() {
        return (long) nodesX * (long) nodesY;
    }

    /** Sample storage in bytes. Samples are {@code float}. */
    public long sampleBytes() {
        long nodes = nodeCount();
        if (nodes > Long.MAX_VALUE / 4L) {
            return Long.MAX_VALUE;
        }
        return nodes * 4L;
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
     * A request that does not fit in an {@code int} node count is a domain-limit failure.
     */
    public static DistanceGridSpec alignedCovering(Box box, double spacing) {
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
        long nodesX = nodesFromOrigin(originX, maxX, spacing);
        long nodesY = nodesFromOrigin(originY, maxY, spacing);
        if (nodesX > Integer.MAX_VALUE || nodesY > Integer.MAX_VALUE) {
            throw new DomainLimitExceededException(
                    "K-ellipse grid would be " + (nodesX == Long.MAX_VALUE ? "too many" : nodesX)
                            + " by " + (nodesY == Long.MAX_VALUE ? "too many" : nodesY)
                            + " nodes. Zoom in."
            );
        }
        return new DistanceGridSpec(originX, originY, spacing, (int) nodesX, (int) nodesY);
    }

    /**
     * Nodes needed to cover {@code [min, max]} at {@code spacing}.
     * {@link Long#MAX_VALUE} means the count is not a finite grid size.
     */
    public static long nodesCovering(double min, double max, double spacing) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || !Double.isFinite(spacing) || spacing <= 0.0) {
            throw new IllegalArgumentException("Distance grid extent and spacing must be finite, with spacing > 0");
        }
        if (max < min) {
            double swap = min;
            min = max;
            max = swap;
        }
        return nodesFromOrigin(alignedOrigin(min, spacing), max, spacing);
    }

    private static double alignedOrigin(double min, double spacing) {
        double origin = Math.floor(min / spacing) * spacing;
        if (origin > min) {
            origin -= spacing;
        }
        return origin;
    }

    private static long nodesFromOrigin(double origin, double max, double spacing) {
        double span = max - origin;
        if (!(span >= 0.0) || !Double.isFinite(span)) {
            return Long.MAX_VALUE;
        }
        double quotient = span / spacing;
        if (!Double.isFinite(quotient) || quotient > Integer.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        long count = (long) Math.ceil(quotient) + 1L;
        if (count < 2L) {
            count = 2L;
        }
        double covered = origin + (count - 1L) * spacing;
        if (covered < max) {
            count++;
        }
        return count;
    }
}
