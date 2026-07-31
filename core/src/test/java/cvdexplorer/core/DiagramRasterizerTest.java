package cvdexplorer.core;

import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagramRasterizerTest {
    private static final DiagramRasterizer.Classifier CLASSIFIER = point ->
            new DiagramRasterizer.Classification(
                    point.x() < point.y() ? 0 : 1,
                    point.x() + point.y(),
                    point.x() < point.y() ? 2 : 3
            );

    private static DiagramRasterizer.RasterResult render(
            DiagramRasterizer rasterizer,
            int width,
            int height,
            boolean parallel
    ) {
        Box image = Box.pq(Vector.ZERO, Vector.xy(width, height)).positive();
        if (parallel) {
            return rasterizer.renderParallel(
                    Transformation.IDENTITY,
                    image,
                    CLASSIFIER,
                    classification -> 0xff000000 | classification.clusterIndex(),
                    1.0
            );
        }
        return rasterizer.render(
                Transformation.IDENTITY,
                image,
                CLASSIFIER,
                classification -> 0xff000000 | classification.clusterIndex(),
                1.0
        );
    }

    @Test
    void exactBuffersMatchEveryLogicalRasterSize() {
        DiagramRasterizer rasterizer = new DiagramRasterizer(DiagramRasterizer.BufferSizing.EXACT);

        DiagramRasterizer.RasterResult full = render(rasterizer, 16, 12, false);
        assertEquals(16 * 12, full.argbPixels().length);
        assertEquals(16 * 12, full.ownershipGrid().clusterIndices().length);
        assertEquals(16 * 12, full.ownershipGrid().memberIndices().length);

        DiagramRasterizer.RasterResult preview = render(rasterizer, 4, 3, false);
        assertEquals(4 * 3, preview.argbPixels().length);
        assertEquals(4 * 3, preview.ownershipGrid().clusterIndices().length);
        assertEquals(4 * 3, preview.ownershipGrid().memberIndices().length);
    }

    @Test
    void defaultBuffersRetainTheirLargerCapacity() {
        DiagramRasterizer rasterizer = new DiagramRasterizer();

        render(rasterizer, 16, 12, false);
        DiagramRasterizer.RasterResult preview = render(rasterizer, 4, 3, false);

        assertTrue(preview.argbPixels().length > preview.width() * preview.height());
        assertTrue(preview.ownershipGrid().clusterIndices().length > preview.width() * preview.height());
    }

    @Test
    void exactSequentialAndParallelRastersMatch() {
        DiagramRasterizer.RasterResult sequential = render(
                new DiagramRasterizer(DiagramRasterizer.BufferSizing.EXACT),
                24,
                18,
                false
        );
        DiagramRasterizer.RasterResult parallel = render(
                new DiagramRasterizer(DiagramRasterizer.BufferSizing.EXACT),
                24,
                18,
                true
        );

        assertArrayEquals(sequential.argbPixels(), parallel.argbPixels());
        assertArrayEquals(
                sequential.ownershipGrid().clusterIndices(),
                parallel.ownershipGrid().clusterIndices()
        );
        assertArrayEquals(
                sequential.ownershipGrid().memberIndices(),
                parallel.ownershipGrid().memberIndices()
        );
    }
}
