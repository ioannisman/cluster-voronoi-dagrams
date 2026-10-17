package cvdexplorer.core;

/**
 * The requested k-ellipse distance grids would exceed a fixed node or memory cap.
 * Callers should report this and avoid querying an unbuilt domain.
 */
public final class DomainLimitExceededException extends RuntimeException {
    public DomainLimitExceededException(String message) {
        super(message);
    }
}
