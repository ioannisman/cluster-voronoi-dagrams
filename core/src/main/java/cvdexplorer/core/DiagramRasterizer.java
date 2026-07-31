package cvdexplorer.core;

import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;

import java.util.stream.IntStream;

/** Pixel ownership grid and optional ARGB fill for a diagram raster. */
public final class DiagramRasterizer {
    public enum BufferSizing {
        GROW_ONLY,
        EXACT
    }

    public record Classification(int clusterIndex, double score, int memberIndex) {
    }

    public record OwnershipGrid(int width, int height, int[] clusterIndices, int[] memberIndices) {
        public int clusterIndexAt(int x, int y) {
            return clusterIndices[y * width + x];
        }

        public int memberIndexAt(int x, int y) {
            return memberIndices[y * width + x];
        }
    }

    public record RasterResult(int width, int height, int[] argbPixels, OwnershipGrid ownershipGrid) {
    }

    @FunctionalInterface
    public interface Classifier {
        Classification classify(Vector point);
    }

    @FunctionalInterface
    public interface Colorizer {
        int color(Classification classification);
    }

    private int[] pixels;
    private int[] clusterIndices;
    private int[] memberIndices;
    private int sizeYp = 0;
    private int sizeXp = 0;
    private final BufferSizing bufferSizing;

    public DiagramRasterizer() {
        this(BufferSizing.GROW_ONLY);
    }

    public DiagramRasterizer(BufferSizing bufferSizing) {
        this.bufferSizing = bufferSizing;
    }

    /** Sequential row scan (no parallel streams). */
    public RasterResult render(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer
    ) {
        return render(tFromPixels, bImage, classifier, colorizer, 1.0);
    }

    /**
     * Sequential render at {@code resolutionScale} of the image extent (clamped to (0, 1]).
     * Samples are taken in full pixel-space so {@code tFromPixels} stays correct.
     */
    public RasterResult render(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer,
            double resolutionScale
    ) {
        GridSpec spec = begin(bImage, resolutionScale);
        if (spec == null) {
            return null;
        }
        for (int y = 0; y < spec.sizeY; y++) {
            fillRow(tFromPixels, classifier, colorizer, spec.sizeX, y, spec.sx, spec.sy);
        }
        return finish(spec.sizeX, spec.sizeY, colorizer != null);
    }

    /**
     * Same sampling as {@link #render(Transformation, Box, Classifier, Colorizer, double)},
     * with rows filled via {@link IntStream#parallel()} (JVM desktop).
     */
    public RasterResult renderParallel(
            Transformation tFromPixels,
            Box bImage,
            Classifier classifier,
            Colorizer colorizer,
            double resolutionScale
    ) {
        GridSpec spec = begin(bImage, resolutionScale);
        if (spec == null) {
            return null;
        }
        IntStream.range(0, spec.sizeY).parallel().forEach(y ->
                fillRow(tFromPixels, classifier, colorizer, spec.sizeX, y, spec.sx, spec.sy)
        );
        return finish(spec.sizeX, spec.sizeY, colorizer != null);
    }

    private GridSpec begin(Box bImage, double resolutionScale) {
        Vector diag = bImage.d().abs();
        int fullW = diag.xInt();
        int fullH = diag.yInt();
        if (fullW == 0 || fullH == 0) {
            return null;
        }
        double scale = Math.min(1.0, Math.max(Double.MIN_VALUE, resolutionScale));
        int sizeX = Math.max(1, (int) Math.round(fullW * scale));
        int sizeY = Math.max(1, (int) Math.round(fullH * scale));
        ensureBuffers(sizeX, sizeY);
        return new GridSpec(sizeX, sizeY, fullW / (double) sizeX, fullH / (double) sizeY);
    }

    private RasterResult finish(int sizeX, int sizeY, boolean withPixels) {
        OwnershipGrid grid = new OwnershipGrid(sizeX, sizeY, clusterIndices, memberIndices);
        return new RasterResult(sizeX, sizeY, withPixels ? pixels : null, grid);
    }

    private void fillRow(
            Transformation tFromPixels,
            Classifier classifier,
            Colorizer colorizer,
            int sizeX,
            int y,
            double sx,
            double sy
    ) {
        for (int x = 0; x < sizeX; x++) {
            Vector pixelCenter = Vector.xy((x + 0.5) * sx, (y + 0.5) * sy);
            Vector point = tFromPixels.applyTo(pixelCenter);
            Classification classification = classifier.classify(point);
            int index = y * sizeX + x;
            clusterIndices[index] = classification.clusterIndex();
            memberIndices[index] = classification.memberIndex();
            if (colorizer != null) {
                pixels[index] = colorizer.color(classification);
            }
        }
    }

    private void ensureBuffers(int sizeX, int sizeY) {
        boolean exactSizeChanged = bufferSizing == BufferSizing.EXACT
                && (pixels == null || pixels.length != sizeX * sizeY);
        if (exactSizeChanged || (sizeYp < sizeY) || (sizeXp < sizeX)) {
            sizeYp = sizeY;
            sizeXp = sizeX;
            pixels = new int[sizeY * sizeX];
            clusterIndices = new int[sizeY * sizeX];
            memberIndices = new int[sizeY * sizeX];
        }
    }

    private record GridSpec(int sizeX, int sizeY, double sx, double sy) {
    }
}
