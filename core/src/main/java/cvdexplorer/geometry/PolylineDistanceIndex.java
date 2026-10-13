package cvdexplorer.geometry;

import cvdexplorer.instrument.Counters;

/**
 * Euclidean distance from a query point to a closed polyline.
 * <p>
 * Segment endpoints, directions, and inverse squared lengths are cached in leaf order.
 * A count-balanced axis-aligned box tree is built by recursive median splits on the
 * longer axis of the segment-centroid bounds. Queries compare squared distances, visit
 * the nearer child first, prune with squared point-to-box distance, and take one square
 * root at the return. Prepared arrays are immutable and queries keep all traversal state
 * on the stack, so parallel reads are safe. A zero-length segment is handled as a point.
 */
public final class PolylineDistanceIndex {
    /** Segments per leaf. Splits below this size would not pay for another box test. */
    static final int LEAF_SIZE = 4;

    private final double[] ax;
    private final double[] ay;
    private final double[] dx;
    private final double[] dy;
    private final double[] invLen2;

    private final double[] minX;
    private final double[] minY;
    private final double[] maxX;
    private final double[] maxY;
    private final int[] left;
    private final int[] right;
    private final int[] first;
    private final int[] count;
    private final int root;

    private PolylineDistanceIndex(
            double[] ax,
            double[] ay,
            double[] dx,
            double[] dy,
            double[] invLen2,
            double[] minX,
            double[] minY,
            double[] maxX,
            double[] maxY,
            int[] left,
            int[] right,
            int[] first,
            int[] count,
            int root
    ) {
        this.ax = ax;
        this.ay = ay;
        this.dx = dx;
        this.dy = dy;
        this.invLen2 = invLen2;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.left = left;
        this.right = right;
        this.first = first;
        this.count = count;
        this.root = root;
    }

    public static PolylineDistanceIndex build(ClosedPolyline polyline) {
        if (polyline == null) {
            throw new IllegalArgumentException("ClosedPolyline is required");
        }
        if (Counters.ENABLED) {
            Counters.INDEX_BUILDS.increment();
        }
        int segmentCount = polyline.vertexCount();
        double[] sourceAx = new double[segmentCount];
        double[] sourceAy = new double[segmentCount];
        double[] sourceBx = new double[segmentCount];
        double[] sourceBy = new double[segmentCount];
        double[] centroidX = new double[segmentCount];
        double[] centroidY = new double[segmentCount];
        int[] order = new int[segmentCount];
        for (int i = 0; i < segmentCount; i++) {
            int next = i + 1;
            if (next == segmentCount) {
                next = 0;
            }
            sourceAx[i] = polyline.x(i);
            sourceAy[i] = polyline.y(i);
            sourceBx[i] = polyline.x(next);
            sourceBy[i] = polyline.y(next);
            centroidX[i] = 0.5 * (sourceAx[i] + sourceBx[i]);
            centroidY[i] = 0.5 * (sourceAy[i] + sourceBy[i]);
            order[i] = i;
        }

        int nodeCount = nodeSlots(segmentCount);
        double[] ax = new double[segmentCount];
        double[] ay = new double[segmentCount];
        double[] dx = new double[segmentCount];
        double[] dy = new double[segmentCount];
        double[] invLen2 = new double[segmentCount];
        double[] minX = new double[nodeCount];
        double[] minY = new double[nodeCount];
        double[] maxX = new double[nodeCount];
        double[] maxY = new double[nodeCount];
        int[] left = new int[nodeCount];
        int[] right = new int[nodeCount];
        int[] first = new int[nodeCount];
        int[] count = new int[nodeCount];
        int[] nodeCursor = {0};
        int[] leafCursor = {0};
        int root = buildNode(
                order,
                0,
                segmentCount,
                sourceAx,
                sourceAy,
                sourceBx,
                sourceBy,
                centroidX,
                centroidY,
                ax,
                ay,
                dx,
                dy,
                invLen2,
                minX,
                minY,
                maxX,
                maxY,
                left,
                right,
                first,
                count,
                nodeCursor,
                leafCursor
        );
        if (nodeCursor[0] != nodeCount || leafCursor[0] != segmentCount) {
            throw new IllegalStateException("Polyline distance tree did not cover every segment");
        }
        return new PolylineDistanceIndex(
                ax, ay, dx, dy, invLen2, minX, minY, maxX, maxY, left, right, first, count, root
        );
    }

    public int segmentCount() {
        return ax.length;
    }

    public double distanceTo(Vector point) {
        return distanceTo(point.x(), point.y());
    }

    public double distanceTo(double x, double y) {
        if (Counters.ENABLED) {
            Counters.INDEX_QUERIES.increment();
        }
        double bestSquared = visit(root, x, y, Double.POSITIVE_INFINITY);
        return Math.sqrt(bestSquared);
    }

    /** Longest root-to-leaf path, in nodes. A single leaf has height 1. */
    int height() {
        return height(root);
    }

    private int height(int node) {
        if (left[node] < 0) {
            return 1;
        }
        return 1 + Math.max(height(left[node]), height(right[node]));
    }

    private double visit(int node, double x, double y, double bestSquared) {
        int leftChild = left[node];
        if (leftChild < 0) {
            int start = first[node];
            int stop = start + count[node];
            for (int i = start; i < stop; i++) {
                double candidate = segmentDistanceSquared(i, x, y);
                if (candidate < bestSquared) {
                    bestSquared = candidate;
                }
            }
            return bestSquared;
        }
        int rightChild = right[node];
        double leftBox = boxDistanceSquared(leftChild, x, y);
        double rightBox = boxDistanceSquared(rightChild, x, y);
        if (leftBox < rightBox) {
            if (leftBox < bestSquared) {
                bestSquared = visit(leftChild, x, y, bestSquared);
            }
            if (rightBox < bestSquared) {
                bestSquared = visit(rightChild, x, y, bestSquared);
            }
        } else {
            if (rightBox < bestSquared) {
                bestSquared = visit(rightChild, x, y, bestSquared);
            }
            if (leftBox < bestSquared) {
                bestSquared = visit(leftChild, x, y, bestSquared);
            }
        }
        return bestSquared;
    }

    private double segmentDistanceSquared(int segment, double x, double y) {
        double offsetX = x - ax[segment];
        double offsetY = y - ay[segment];
        double inverseLengthSquared = invLen2[segment];
        if (inverseLengthSquared == 0.0) {
            return offsetX * offsetX + offsetY * offsetY;
        }
        double t = (offsetX * dx[segment] + offsetY * dy[segment]) * inverseLengthSquared;
        if (t <= 0.0) {
            return offsetX * offsetX + offsetY * offsetY;
        }
        if (t >= 1.0) {
            double endX = x - (ax[segment] + dx[segment]);
            double endY = y - (ay[segment] + dy[segment]);
            return endX * endX + endY * endY;
        }
        double closestX = ax[segment] + t * dx[segment];
        double closestY = ay[segment] + t * dy[segment];
        double deltaX = x - closestX;
        double deltaY = y - closestY;
        return deltaX * deltaX + deltaY * deltaY;
    }

    private double boxDistanceSquared(int node, double x, double y) {
        double deltaX = 0.0;
        if (x < minX[node]) {
            deltaX = minX[node] - x;
        } else if (x > maxX[node]) {
            deltaX = x - maxX[node];
        }
        double deltaY = 0.0;
        if (y < minY[node]) {
            deltaY = minY[node] - y;
        } else if (y > maxY[node]) {
            deltaY = y - maxY[node];
        }
        return deltaX * deltaX + deltaY * deltaY;
    }

    private static int buildNode(
            int[] order,
            int begin,
            int end,
            double[] sourceAx,
            double[] sourceAy,
            double[] sourceBx,
            double[] sourceBy,
            double[] centroidX,
            double[] centroidY,
            double[] ax,
            double[] ay,
            double[] dx,
            double[] dy,
            double[] invLen2,
            double[] minX,
            double[] minY,
            double[] maxX,
            double[] maxY,
            int[] left,
            int[] right,
            int[] first,
            int[] count,
            int[] nodeCursor,
            int[] leafCursor
    ) {
        int node = nodeCursor[0];
        nodeCursor[0] = node + 1;
        int segmentCount = end - begin;
        double boundMinX = Double.POSITIVE_INFINITY;
        double boundMinY = Double.POSITIVE_INFINITY;
        double boundMaxX = Double.NEGATIVE_INFINITY;
        double boundMaxY = Double.NEGATIVE_INFINITY;
        for (int i = begin; i < end; i++) {
            int segment = order[i];
            boundMinX = Math.min(boundMinX, Math.min(sourceAx[segment], sourceBx[segment]));
            boundMinY = Math.min(boundMinY, Math.min(sourceAy[segment], sourceBy[segment]));
            boundMaxX = Math.max(boundMaxX, Math.max(sourceAx[segment], sourceBx[segment]));
            boundMaxY = Math.max(boundMaxY, Math.max(sourceAy[segment], sourceBy[segment]));
        }
        minX[node] = boundMinX;
        minY[node] = boundMinY;
        maxX[node] = boundMaxX;
        maxY[node] = boundMaxY;

        if (segmentCount <= LEAF_SIZE) {
            int start = leafCursor[0];
            for (int i = begin; i < end; i++) {
                int segment = order[i];
                int slot = leafCursor[0];
                leafCursor[0] = slot + 1;
                ax[slot] = sourceAx[segment];
                ay[slot] = sourceAy[segment];
                double directionX = sourceBx[segment] - sourceAx[segment];
                double directionY = sourceBy[segment] - sourceAy[segment];
                dx[slot] = directionX;
                dy[slot] = directionY;
                double lengthSquared = directionX * directionX + directionY * directionY;
                invLen2[slot] = lengthSquared == 0.0 ? 0.0 : 1.0 / lengthSquared;
            }
            left[node] = -1;
            right[node] = -1;
            first[node] = start;
            count[node] = segmentCount;
            return node;
        }

        double centroidMinX = Double.POSITIVE_INFINITY;
        double centroidMinY = Double.POSITIVE_INFINITY;
        double centroidMaxX = Double.NEGATIVE_INFINITY;
        double centroidMaxY = Double.NEGATIVE_INFINITY;
        for (int i = begin; i < end; i++) {
            int segment = order[i];
            centroidMinX = Math.min(centroidMinX, centroidX[segment]);
            centroidMinY = Math.min(centroidMinY, centroidY[segment]);
            centroidMaxX = Math.max(centroidMaxX, centroidX[segment]);
            centroidMaxY = Math.max(centroidMaxY, centroidY[segment]);
        }
        double spanX = centroidMaxX - centroidMinX;
        double spanY = centroidMaxY - centroidMinY;
        if (spanX > 0.0 || spanY > 0.0) {
            double[] key = spanX >= spanY ? centroidX : centroidY;
            sortByCentroid(order, key, begin, end);
        }
        int mid = begin + leftCount(segmentCount);
        int leftChild = buildNode(
                order, begin, mid,
                sourceAx, sourceAy, sourceBx, sourceBy, centroidX, centroidY,
                ax, ay, dx, dy, invLen2, minX, minY, maxX, maxY, left, right, first, count,
                nodeCursor, leafCursor
        );
        int rightChild = buildNode(
                order, mid, end,
                sourceAx, sourceAy, sourceBx, sourceBy, centroidX, centroidY,
                ax, ay, dx, dy, invLen2, minX, minY, maxX, maxY, left, right, first, count,
                nodeCursor, leafCursor
        );
        left[node] = leftChild;
        right[node] = rightChild;
        first[node] = first[leftChild];
        count[node] = count[leftChild] + count[rightChild];
        return node;
    }

    private static int nodeSlots(int segmentCount) {
        if (segmentCount <= LEAF_SIZE) {
            return 1;
        }
        int leftSegments = leftCount(segmentCount);
        return 1 + nodeSlots(leftSegments) + nodeSlots(segmentCount - leftSegments);
    }

    private static int leftCount(int segmentCount) {
        return segmentCount / 2;
    }

    static void sortByCentroid(int[] order, double[] key, int begin, int end) {
        while (end - begin > 16) {
            int pivot = partition(order, key, begin, end);
            int leftSize = pivot - begin;
            int rightSize = end - (pivot + 1);
            if (leftSize < rightSize) {
                sortByCentroid(order, key, begin, pivot);
                begin = pivot + 1;
            } else {
                sortByCentroid(order, key, pivot + 1, end);
                end = pivot;
            }
        }
        insertionSort(order, key, begin, end);
    }

    private static int partition(int[] order, double[] key, int begin, int end) {
        int mid = (begin + end) >>> 1;
        int last = end - 1;
        if (key[order[mid]] < key[order[begin]]) {
            swap(order, begin, mid);
        }
        if (key[order[last]] < key[order[mid]]) {
            swap(order, mid, last);
        }
        if (key[order[mid]] < key[order[begin]]) {
            swap(order, begin, mid);
        }
        swap(order, mid, last);
        double pivot = key[order[last]];
        int store = begin;
        for (int i = begin; i < last; i++) {
            if (key[order[i]] <= pivot) {
                swap(order, store, i);
                store++;
            }
        }
        swap(order, store, last);
        return store;
    }

    private static void insertionSort(int[] order, double[] key, int begin, int end) {
        for (int i = begin + 1; i < end; i++) {
            int id = order[i];
            double value = key[id];
            int j = i;
            while (j > begin && key[order[j - 1]] > value) {
                order[j] = order[j - 1];
                j--;
            }
            order[j] = id;
        }
    }

    private static void swap(int[] order, int i, int j) {
        int tmp = order[i];
        order[i] = order[j];
        order[j] = tmp;
    }
}
