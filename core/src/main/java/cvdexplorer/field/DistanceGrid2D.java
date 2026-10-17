package cvdexplorer.field;

/**
 * Immutable signed-distance samples on a {@link DistanceGridSpec}.
 * Samples are stored as {@code float}. Interpolation runs in {@code double}.
 * A feature thinner than the spacing may have no negative sample; interpolation cannot recover it.
 */
public final class DistanceGrid2D {
    private final DistanceGridSpec spec;
    private final float[] samples;

    public DistanceGrid2D(DistanceGridSpec spec, float[] samples) {
        this(spec, samples, true);
    }

    /** Takes ownership of {@code samples}. The caller must not modify the array afterward. */
    static DistanceGrid2D adopt(DistanceGridSpec spec, float[] samples) {
        return new DistanceGrid2D(spec, samples, false);
    }

    private DistanceGrid2D(DistanceGridSpec spec, float[] samples, boolean copy) {
        if (spec == null || samples == null) {
            throw new IllegalArgumentException("Distance grid requires a spec and samples");
        }
        long expected = spec.nodeCount();
        if (expected > Integer.MAX_VALUE || samples.length != expected) {
            throw new IllegalArgumentException("Sample length " + samples.length + " does not match " + expected);
        }
        this.spec = spec;
        this.samples = copy ? samples.clone() : samples;
    }

    public DistanceGridSpec spec() {
        return spec;
    }

    public float sample(int i, int j) {
        if (i < 0 || j < 0 || i >= spec.nodesX() || j >= spec.nodesY()) {
            throw new IndexOutOfBoundsException("(" + i + ", " + j + ")");
        }
        return samples[j * spec.nodesX() + i];
    }

    /**
     * Bilinear value at a world point. The far edges use the last cell with interpolation weight 1.
     * A point outside the node rectangle, including NaN, is a domain-contract failure.
     */
    public double interpolate(double x, double y) {
        double fx = (x - spec.originX()) / spec.spacing();
        double fy = (y - spec.originY()) / spec.spacing();
        int lastX = spec.nodesX() - 1;
        int lastY = spec.nodesY() - 1;
        if (!(fx >= 0.0 && fx <= lastX && fy >= 0.0 && fy <= lastY)) {
            throw new OutOfDomainException(
                    "Distance grid query (" + x + ", " + y + ") is outside ["
                            + spec.originX() + ", " + spec.maxX() + "] x ["
                            + spec.originY() + ", " + spec.maxY() + "]"
            );
        }
        int i = (int) fx;
        int j = (int) fy;
        if (i >= lastX) {
            i = lastX - 1;
        }
        if (j >= lastY) {
            j = lastY - 1;
        }
        double tx = fx - i;
        double ty = fy - j;
        int stride = spec.nodesX();
        double s00 = samples[j * stride + i];
        double s10 = samples[j * stride + i + 1];
        double s01 = samples[(j + 1) * stride + i];
        double s11 = samples[(j + 1) * stride + i + 1];
        double alongBottom = s00 + tx * (s10 - s00);
        double alongTop = s01 + tx * (s11 - s01);
        return alongBottom + ty * (alongTop - alongBottom);
    }

    public static final class OutOfDomainException extends IllegalStateException {
        public OutOfDomainException(String message) {
            super(message);
        }
    }
}
