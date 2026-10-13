package cvdexplorer.core;

import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.field.DistanceGridSpec;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;

/**
 * Turns the world rectangle that will be queried into one shared grid spec.
 * An existing spec is reused while it covers that rectangle.
 * Growth adds a margin so a small pan does not rebuild. The margin fraction is total growth:
 * {@code 0.25} adds 12.5% of the extent on each side.
 */
public final class QueryDomainPolicy {
    public static final double DEFAULT_SPACING = 2.0;
    public static final double DEFAULT_MARGIN_FRACTION = 0.25;

    private final double spacing;
    private final double marginFraction;
    private final DistanceGridLimits limits;

    public QueryDomainPolicy() {
        this(DEFAULT_SPACING, DEFAULT_MARGIN_FRACTION, DistanceGridLimits.DEFAULT);
    }

    public QueryDomainPolicy(double spacing, double marginFraction, DistanceGridLimits limits) {
        if (!Double.isFinite(spacing) || spacing <= 0.0) {
            throw new IllegalArgumentException("Grid spacing must be positive");
        }
        if (!Double.isFinite(marginFraction) || marginFraction < 0.0) {
            throw new IllegalArgumentException("Margin fraction must be non-negative");
        }
        if (limits == null) {
            throw new IllegalArgumentException("Distance grid limits are required");
        }
        this.spacing = spacing;
        this.marginFraction = marginFraction;
        this.limits = limits;
    }

    public double spacing() {
        return spacing;
    }

    public DistanceGridSpec ensureCovers(DistanceGridSpec current, Box requiredWorldBounds) {
        if (requiredWorldBounds == null) {
            throw new IllegalArgumentException("Required world bounds are required");
        }
        if (current != null && current.covers(requiredWorldBounds)) {
            return current;
        }
        Box expanded = expanded(requiredWorldBounds);
        long version = current == null ? 1L : current.version() + 1L;
        DistanceGridSpec spec = DistanceGridSpec.alignedCovering(expanded, spacing, version);
        if (spec.nodesX() > limits.maxNodesPerAxis()
                || spec.nodesY() > limits.maxNodesPerAxis()
                || spec.nodeCount() > limits.maxNodesPerGrid()) {
            throw new DomainLimitExceededException(
                    "K-ellipse grid would be " + spec.nodesX() + " by " + spec.nodesY()
                            + " nodes (limit " + limits.maxNodesPerAxis() + " per axis, "
                            + limits.maxNodesPerGrid() + " nodes). Zoom in."
            );
        }
        return spec;
    }

    private Box expanded(Box required) {
        Box oriented = required.positive();
        double minX = oriented.p().x();
        double minY = oriented.p().y();
        double maxX = oriented.q().x();
        double maxY = oriented.q().y();
        double padX = 0.5 * marginFraction * (maxX - minX);
        double padY = 0.5 * marginFraction * (maxY - minY);
        return Box.pq(Vector.xy(minX - padX, minY - padY), Vector.xy(maxX + padX, maxY + padY));
    }
}
