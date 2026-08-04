# Site types

[← Documentation home](README.md)

Members in a cluster can be `POINT`, `LINE_SEGMENT`, `CIRCLE`, `ELLIPSE`, `LINE`, or `POLYGON`.

Open polygonal chains can still be built from `LINE_SEGMENT` members; use **`Snap to handles`** to weld segment endpoints if needed. Closed polygons should use the `POLYGON` member type.

## Points

The distance from a point in the plane to a point member is the Euclidean distance between them.

Figures where each cluster uses **point** members (`POINT`) follow in **[Metrics](metrics.md)** (all five metrics and both neighbor orders).

## Line segments

The distance from a point in the plane to a line segment is the Euclidean distance to the **closest point on the segment**: project onto the line through the endpoints, clamp to the segment, then measure distance (equivalently, the minimum distance to any point on the segment).

Figures where each cluster uses **line segment** members (`LINE_SEGMENT`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/segments_min_min.png" width="420" alt="Segments, minimum distance, nearest" /> | <img src="figures/instances/segments_max_min.png" width="420" alt="Segments, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/segments_min_max.png" width="420" alt="Segments, maximum distance, nearest" /> | <img src="figures/instances/segments_max_max.png" width="420" alt="Segments, maximum distance, farthest" /> |

## Polygons

A `POLYGON` member is a closed vertex list. The distance from a query point to the member is the Euclidean distance to the **closest point on the boundary** (minimum over edges of the usual segment distance)—the same “distance to the object” model as for circles and ellipses.

In the app, set **New member type** to `POLYGON` and **Member parameter** to choose how many vertices the *next* created polygon has (creation-time only).

Figures where each cluster uses **polygon** members (`POLYGON`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/polygons_min_min.png" width="420" alt="Polygons, minimum distance, nearest" /> | <img src="figures/instances/polygons_max_min.png" width="420" alt="Polygons, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/polygons_min_max.png" width="420" alt="Polygons, maximum distance, nearest" /> | <img src="figures/instances/polygons_max_max.png" width="420" alt="Polygons, maximum distance, farthest" /> |

## Lines

The distance from a point in the plane to an infinite **line** member is the Euclidean distance to the **closest point on the line** (orthogonal projection onto the lineß).

Figures where each cluster uses **line** members (`LINE`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/lines_min_min.png" width="420" alt="Lines, minimum distance, nearest" /> | <img src="figures/instances/lines_max_min.png" width="420" alt="Lines, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/lines_min_max.png" width="420" alt="Lines, maximum distance, nearest" /> | <img src="figures/instances/lines_max_max.png" width="420" alt="Lines, maximum distance, farthest" /> |

## Circles

The distance from a point in the plane to a **circle** member is the distance to the nearest point on the circle **boundary**: `|distance to center − radius|` (interior points are measured to the boundary, not to the center).

Figures where each cluster uses **circle** members (`CIRCLE`) follow.

### Minimum distance

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/circles_min_min.png" width="420" alt="Circles, minimum distance, nearest" /> | <img src="figures/instances/circles_max_min.png" width="420" alt="Circles, minimum distance, farthest" /> |

### Maximum distance

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/circles_min_max.png" width="420" alt="Circles, maximum distance, nearest" /> | <img src="figures/instances/circles_max_max.png" width="420" alt="Circles, maximum distance, farthest" /> |

## Ellipses (2-ellipses)

A classic **ellipse** has two foci \(f_1,f_2\) and constant \(c\). Its boundary is the set of points where \(d(p,f_1)+d(p,f_2)=c\). The distance from a query point to the member is the **Euclidean distance to that boundary** (computed analytically). The constant \(c\) is set by a control handle \(h\) on the curve: \(c = d(h,f_1)+d(h,f_2)\).
