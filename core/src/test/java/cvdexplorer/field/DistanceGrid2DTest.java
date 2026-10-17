package cvdexplorer.field;

import cvdexplorer.instrument.Counters;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceGrid2DTest {

    @Test
    void bilinearReproducesLinearAndBilinearFields() {
        DistanceGridSpec spec = new DistanceGridSpec(0, 0, 1, 5, 5);
        DistanceGrid2D linear = grid(spec, (x, y) -> 3 * x - 2 * y + 1);
        DistanceGrid2D product = grid(spec, (x, y) -> x * y);
        Random random = new Random(20261011L);
        for (int n = 0; n < 30; n++) {
            double x = 4.0 * random.nextDouble();
            double y = 4.0 * random.nextDouble();
            assertEquals(3 * x - 2 * y + 1, linear.interpolate(x, y), 1e-5);
            assertEquals(x * y, product.interpolate(x, y), 1e-5);
        }
    }

    @Test
    void cornersAndFarEdgesMatchTheNodeSamples() {
        DistanceGridSpec spec = new DistanceGridSpec(0, 0, 1, 5, 5);
        DistanceGrid2D grid = grid(spec, (x, y) -> 3 * x - 2 * y + 1);
        assertEquals(1.0, grid.interpolate(0, 0), 1e-6);
        assertEquals(13.0, grid.interpolate(4, 0), 1e-6);
        assertEquals(-7.0, grid.interpolate(0, 4), 1e-6);
        assertEquals(5.0, grid.interpolate(spec.maxX(), spec.maxY()), 1e-6);
        assertEquals(grid.sample(4, 4), (float) grid.interpolate(spec.maxX(), spec.maxY()), 0.0f);
    }

    @Test
    void outsideAndNanFail() {
        DistanceGrid2D grid = grid(new DistanceGridSpec(0, 0, 1, 5, 5), (x, y) -> 0);
        DistanceGrid2D.OutOfDomainException outside = assertThrows(
                DistanceGrid2D.OutOfDomainException.class,
                () -> grid.interpolate(grid.spec().maxX() + 1e-9, 1)
        );
        assertTrue(outside.getMessage().contains("outside"));
        assertThrows(DistanceGrid2D.OutOfDomainException.class, () -> grid.interpolate(Double.NaN, 1));
        assertThrows(DistanceGrid2D.OutOfDomainException.class, () -> grid.interpolate(-1e-15, 0));
    }

    @Test
    void interpolationIncrementsTheCounter() {
        assertTrue(Counters.ENABLED);
        Counters.reset();
        DistanceGrid2D grid = grid(new DistanceGridSpec(0, 0, 1, 3, 3), (x, y) -> x + y);
        grid.interpolate(0.5, 0.5);
        grid.interpolate(1, 1);
        assertEquals(2, Counters.snapshot().gridInterpolations());
        assertThrows(DistanceGrid2D.OutOfDomainException.class, () -> grid.interpolate(5, 5));
        assertEquals(2, Counters.snapshot().gridInterpolations());
    }

    private static DistanceGrid2D grid(DistanceGridSpec spec, Field field) {
        float[] samples = new float[Math.toIntExact(spec.nodeCount())];
        for (int j = 0; j < spec.nodesY(); j++) {
            for (int i = 0; i < spec.nodesX(); i++) {
                double x = spec.originX() + i * spec.spacing();
                double y = spec.originY() + j * spec.spacing();
                samples[j * spec.nodesX() + i] = (float) field.value(x, y);
            }
        }
        return new DistanceGrid2D(spec, samples);
    }

    @FunctionalInterface
    private interface Field {
        double value(double x, double y);
    }
}
