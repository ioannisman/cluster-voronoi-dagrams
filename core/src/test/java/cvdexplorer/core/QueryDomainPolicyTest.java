package cvdexplorer.core;

import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.field.DistanceGridSpec;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryDomainPolicyTest {
    private final QueryDomainPolicy policy = new QueryDomainPolicy();

    @Test
    void reusesASpecThatStillCoversTheView() {
        Box firstView = box(0, 0, 20, 20);
        DistanceGridSpec first = policy.ensureCovers(null, firstView);
        assertTrue(first.covers(firstView));
        assertSame(first, policy.ensureCovers(first, box(2, 2, 8, 8)));
        assertSame(first, policy.ensureCovers(first, box(-1, -1, 21, 21)));
    }

    @Test
    void growsWhenTheViewLeavesThePreparedRectangle() {
        DistanceGridSpec first = policy.ensureCovers(null, box(0, 0, 20, 20));
        Box wider = box(-100, -100, 100, 100);
        DistanceGridSpec second = policy.ensureCovers(first, wider);
        assertTrue(second.version() > first.version());
        assertTrue(second.covers(wider));
        assertSame(second, policy.ensureCovers(second, wider));
    }

    @Test
    void marginGrowsTheRequiredBoxByTwentyFivePercentInTotal() {
        DistanceGridSpec spec = policy.ensureCovers(null, box(0, 0, 100, 100));
        double spanX = spec.maxX() - spec.originX();
        double spanY = spec.maxY() - spec.originY();
        assertTrue(spanX >= 125.0 - 1e-6, "spanX " + spanX);
        assertTrue(spanY >= 125.0 - 1e-6, "spanY " + spanY);
        assertTrue(spanX <= 125.0 + 2.0 * QueryDomainPolicy.DEFAULT_SPACING, "spanX " + spanX);
        assertTrue(spanY <= 125.0 + 2.0 * QueryDomainPolicy.DEFAULT_SPACING, "spanY " + spanY);
        assertTrue(spec.covers(box(0, 0, 100, 100)));
    }

    @Test
    void rejectsAGridPastTheNodeLimit() {
        QueryDomainPolicy tight = new QueryDomainPolicy(
                2.0,
                0.25,
                new DistanceGridLimits(8, 64, 256L * 1024L * 1024L)
        );
        DomainLimitExceededException failure = assertThrows(
                DomainLimitExceededException.class,
                () -> tight.ensureCovers(null, box(0, 0, 100, 100))
        );
        assertTrue(failure.getMessage().contains("Zoom in"));
    }

    private static Box box(double minX, double minY, double maxX, double maxY) {
        return Box.pq(Vector.xy(minX, minY), Vector.xy(maxX, maxY));
    }
}
