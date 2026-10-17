# K-ellipse distance grid

[← Documentation home](README.md)

A k-ellipse distance is the Euclidean distance to the curve \(F(y) = c\), inside and outside. The running diagram does not evaluate \(F\) per pixel.

## Query path

After preparation, every production distance query for that member is:

```text
query point
    → bilinear interpolation of this member's signed-distance grid
    → absolute value
    → unsigned distance
```

There is no second method for dragging, for points inside the curve, or for points near the boundary. The fast drag preview only classifies fewer pixels.

## Preparation path

```text
foci and control handle
    → closed polyline approximation of the curve
    → segment-distance index
    → signed-distance grid over the working rectangle
```

The index is used to build the grid and in tests. It is not a fallback when a query misses the grid. A query outside the prepared rectangle is a preparation failure.

The polyline is cached for drawing. The overlay does not rebuild the curve on each frame.

Samples are stored as 32-bit floats. Interpolation uses double precision. On one three-focus example with spacing 1, the extra interpolation error against the polyline index had mean about 0.018 and max about 0.16. Spacing in the app is 2 world units, so the grid error is larger than that example. A shape thinner than the spacing can have no negative sample, and interpolation cannot invent that interior.

Chord refinement of the polyline is a practical midpoint check. It is not a certified bound on the distance to the true curve.

## Working rectangle

One world-space rectangle and one spacing are shared by every k-ellipse. Each member stores its own samples on that rectangle.

The rectangle comes from the current view, including skeleton and subdivision queries, plus one spacing of padding, then grown by 25% in total (12.5% on each side) and snapped to the grid. It does not follow the k-ellipse's own bounding box.

- Moving a focus does not move the rectangle. It rebuilds that member's boundary and grid.
- Moving another member does not rebuild this member's grid.
- A curve may lie outside the rectangle and still have valid distances inside it.
- Panning or zooming inside the prepared rectangle reuses the grids.
- Zooming does not change the spacing. A closer view can show the grid.
- Leaving the rectangle expands it and rebuilds the grids. Boundaries that did not change are kept.
- The rectangle does not shrink on every frame. If keeping the larger rectangle would put the distance grids over the memory limit, it is rebuilt for the current view at the same spacing.

If the requested grid would exceed 8192 nodes on an axis, 16,777,216 nodes in one grid, or 256 MB of samples in total, preparation stops and the diagram stays hidden. Quality is not reduced to fit.

## What is saved

Scene files store the site kind, the foci, and the control handle. They do not store polylines, trees, grids, or cache revisions.

A k-ellipse in a file must have 3 to 16 foci and a curve the builder can prepare. A circle or a two-focus ellipse remains a `CIRCLE` or `ELLIPSE` member.
