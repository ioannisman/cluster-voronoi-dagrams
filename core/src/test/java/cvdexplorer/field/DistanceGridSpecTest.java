package cvdexplorer.field;

import cvdexplorer.core.DomainLimitExceededException;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceGridSpecTest {

    @Test
    void rejectsInvalidSpec() {
        assertThrows(IllegalArgumentException.class, () -> new DistanceGridSpec(0, 0, 0, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new DistanceGridSpec(0, 0, -1, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new DistanceGridSpec(Double.NaN, 0, 1, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> new DistanceGridSpec(0, 0, 1, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> DistanceGridSpec.alignedCovering(null, 1));
    }

    @Test
    void maxAndBytesFollowTheNodeLattice() {
        DistanceGridSpec spec = new DistanceGridSpec(-2, 4, 0.5, 5, 3);
        assertEquals(0.0, spec.maxX(), 0.0);
        assertEquals(5.0, spec.maxY(), 0.0);
        assertEquals(15L, spec.nodeCount());
        assertEquals(60L, spec.sampleBytes());
    }

    @Test
    void wideNodeCountsStayPositive() {
        DistanceGridSpec spec = new DistanceGridSpec(0, 0, 1, 50_000, 50_000);
        assertEquals(2_500_000_000L, spec.nodeCount());
        assertEquals(10_000_000_000L, spec.sampleBytes());
    }

    @Test
    void coversIsInclusiveAndOrientsTheBox() {
        DistanceGridSpec spec = new DistanceGridSpec(0, 0, 1, 5, 4);
        assertTrue(spec.covers(Box.pq(Vector.xy(0, 0), Vector.xy(4, 3))));
        assertTrue(spec.covers(Box.pq(Vector.xy(4, 3), Vector.xy(0, 0))));
        assertFalse(spec.covers(Box.pq(Vector.xy(-0.001, 0), Vector.xy(1, 1))));
        assertFalse(spec.covers(Box.pq(Vector.xy(0, 0), Vector.xy(4.001, 1))));
    }

    @Test
    void alignedCoveringSnapsOriginAndCoversTheBox() {
        Box box = Box.pq(Vector.xy(1.2, -0.4), Vector.xy(3.8, 2.1));
        double spacing = 0.5;
        DistanceGridSpec spec = DistanceGridSpec.alignedCovering(box, spacing);
        assertTrue(spec.covers(box));
        assertEquals(0.0, spec.originX() / spacing - Math.rint(spec.originX() / spacing), 1e-9);
        assertEquals(0.0, spec.originY() / spacing - Math.rint(spec.originY() / spacing), 1e-9);
        assertTrue(spec.originX() <= 1.2);
        assertTrue(spec.originY() <= -0.4);
    }

    @Test
    void hugeCoveringRequestIsADomainLimit() {
        Box huge = Box.pq(Vector.xy(-3e9, -3e9), Vector.xy(3e9, 3e9));
        DomainLimitExceededException failure = assertThrows(
                DomainLimitExceededException.class,
                () -> DistanceGridSpec.alignedCovering(huge, 2.0)
        );
        assertTrue(failure.getMessage().contains("Zoom in"));
    }

    @Test
    void defaultLimitsMatchTheFixedCaps() {
        assertEquals(8192, DistanceGridLimits.DEFAULT.maxNodesPerAxis());
        assertEquals(16_777_216, DistanceGridLimits.DEFAULT.maxNodesPerGrid());
        assertEquals(256L * 1024L * 1024L, DistanceGridLimits.DEFAULT.maxTotalSampleBytes());
    }
}
