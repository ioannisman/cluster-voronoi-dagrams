package cvdexplorer.kellipse;

/**
 * The foci and control handle were finite, but the curve could not be prepared.
 * Invalid coordinates and focus counts are {@link IllegalArgumentException} instead.
 */
public final class KEllipsePreparationException extends Exception {
    public enum Reason {
        /** The sublevel set has no point far enough inside {@code F(y) = c} to sample a curve. */
        DEGENERATE_CURVE,
        /** A bracket or root solve did not meet its iteration and tolerance contract. */
        NUMERICAL_FAILURE
    }

    private final Reason reason;

    public KEllipsePreparationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
