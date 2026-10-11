package cvdexplorer.model;

import cvdexplorer.geometry.Vector;
import cvdexplorer.instrument.Counters;

import java.util.Arrays;
import java.util.List;

/**
 * One k-ellipse with at least three foci: the curve {@code F(y) = c} where {@code F} is the sum of
 * distances to the foci and {@code c = F(h)} for the control handle {@code h}. The foci are internal
 * geometry of this single member. A circle and a two-focus ellipse stay their own member types.
 * Distance is not available until a prepared boundary exists.
 */
public final class KEllipseMember implements ClusterMember {
    private final Vector[] foci;
    private final Vector controlHandle;
    private final double level;

    public KEllipseMember(List<Vector> foci, Vector controlHandle) {
        this(copyFoci(foci), controlHandle);
    }

    private static Vector[] copyFoci(List<Vector> foci) {
        if (foci == null) {
            throw new IllegalArgumentException("KEllipseMember requires between "
                    + SceneLimits.MIN_KELLIPSE_FOCI + " and " + SceneLimits.MAX_KELLIPSE_FOCI + " foci");
        }
        Vector[] copy = new Vector[foci.size()];
        for (int i = 0; i < foci.size(); i++) {
            copy[i] = foci.get(i);
        }
        return copy;
    }

    private KEllipseMember(Vector[] foci, Vector controlHandle) {
        if (foci.length < SceneLimits.MIN_KELLIPSE_FOCI || foci.length > SceneLimits.MAX_KELLIPSE_FOCI) {
            throw new IllegalArgumentException("KEllipseMember requires between "
                    + SceneLimits.MIN_KELLIPSE_FOCI + " and " + SceneLimits.MAX_KELLIPSE_FOCI + " foci");
        }
        for (int i = 0; i < foci.length; i++) {
            requireFinite(foci[i], "focus " + i);
        }
        requireFinite(controlHandle, "control handle");
        this.foci = foci;
        this.controlHandle = controlHandle;
        double computed = sumDistances(controlHandle.x(), controlHandle.y());
        if (!Double.isFinite(computed)) {
            throw new IllegalArgumentException("KEllipseMember level must be finite");
        }
        this.level = computed;
    }

    public List<Vector> foci() {
        return List.of(foci.clone());
    }

    public int focusCount() {
        return foci.length;
    }

    public Vector controlHandle() {
        return controlHandle;
    }

    /** Level-set constant {@code c = F(h)}. */
    public double level() {
        return level;
    }

    /**
     * Sum of distances from {@code (x, y)} to the foci. Each call is counted when instrumentation
     * is enabled. Constructing a member does not increment the counter.
     */
    public double focalSum(double x, double y) {
        if (Counters.ENABLED) {
            Counters.FOCAL_SUM_EVALUATIONS.increment();
        }
        return sumDistances(x, y);
    }

    private double sumDistances(double x, double y) {
        double sum = 0.0;
        for (Vector focus : foci) {
            double dx = x - focus.x();
            double dy = y - focus.y();
            sum += Math.sqrt(dx * dx + dy * dy);
        }
        return sum;
    }

    @Override
    public double distanceTo(Vector point) {
        throw new UnsupportedOperationException("K_ELLIPSE distance requires preparation via ScenePreparer");
    }

    @Override
    public int handleCount() {
        return foci.length + 1;
    }

    @Override
    public Vector getHandle(int index) {
        if (index < 0 || index > foci.length) {
            throw new IndexOutOfBoundsException(index);
        }
        if (index == foci.length) {
            return controlHandle;
        }
        return foci[index];
    }

    @Override
    public ClusterMember withHandle(int index, Vector v) {
        if (index < 0 || index > foci.length) {
            throw new IndexOutOfBoundsException(index);
        }
        if (index == foci.length) {
            return new KEllipseMember(foci.clone(), v);
        }
        Vector[] next = foci.clone();
        next[index] = v;
        return new KEllipseMember(next, controlHandle);
    }

    @Override
    public Vector placementCentroid() {
        double sx = 0.0;
        double sy = 0.0;
        for (Vector focus : foci) {
            sx += focus.x();
            sy += focus.y();
        }
        return Vector.xy(sx / foci.length, sy / foci.length);
    }

    @Override
    public Vector anchor() {
        return foci[0];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KEllipseMember other)) {
            return false;
        }
        return controlHandle.equals(other.controlHandle) && Arrays.equals(foci, other.foci);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(foci) + controlHandle.hashCode();
    }

    private static Vector requireFinite(Vector point, String name) {
        if (point == null || !Double.isFinite(point.x()) || !Double.isFinite(point.y())) {
            throw new IllegalArgumentException("KEllipseMember " + name + " must be finite");
        }
        return point;
    }
}
