# Metrics and neighbor order

[← Documentation home](README.md)

## Neighbor order

After the distance to each cluster is computed, **neighbor order** picks the owner:

| Order | Wins when |
|-------|-----------|
| `NEAREST` | The cluster with the **smallest** distance |
| `FARTHEST` | The cluster with the **largest** distance |


## Distance metrics

Each metric defines how the distance from a query point to a cluster is measured. The same neighbor order is then applied across clusters.

The metrics below are illustrated with **clusters of point members** only (`points_*` figures). Each definition is followed by two images: **nearest** and **farthest** neighbor order.

Sum, mean, and *k*-th nearest distance each use one distance per member. A member may be a point, segment, circle, ellipse, line, polygon, or k-ellipse, and one cluster may mix those types. A circle, a polygon, or a k-ellipse contributes its distance to the query as a single term, not one term for every focus, boundary point, or vertex.

### `MINIMUM_DISTANCE`

Distance to the nearest member in that cluster.

| Nearest (min-min) | Farthest (max-min) |
|:---:|:---:|
| <img src="figures/instances/points_min_min.png" width="420" alt="Points, minimum distance, nearest" /> | <img src="figures/instances/points_max_min.png" width="420" alt="Points, minimum distance, farthest" /> |

### `MAXIMUM_DISTANCE`

Distance to the farthest member in that cluster.

| Nearest (min-max) | Farthest (max-max) |
|:---:|:---:|
| <img src="figures/instances/points_min_max.png" width="420" alt="Points, maximum distance, nearest" /> | <img src="figures/instances/points_max_max.png" width="420" alt="Points, maximum distance, farthest" /> |

### `SUM_OF_DISTANCES`

Sum of the distances to the members in the cluster.

| Nearest (min-sum) | Farthest (max-sum) |
|:---:|:---:|
| <img src="figures/instances/points_min_sum.png" width="420" alt="Points, sum of distances, nearest" /> | <img src="figures/instances/points_max_sum.png" width="420" alt="Points, sum of distances, farthest" /> |

### `MEAN_DISTANCE`

Mean of the distances to the members in the cluster.

| Nearest (min-mean) | Farthest (max-mean) |
|:---:|:---:|
| <img src="figures/instances/points_min_mean.png" width="420" alt="Points, mean distance, nearest" /> | <img src="figures/instances/points_max_mean.png" width="420" alt="Points, mean distance, farthest" /> |

### `KTH_NEAREST_DISTANCE`

Distance to the *k*-th nearest member in that cluster. *k* = 1 has the same score as `MINIMUM_DISTANCE`. When *k* equals the number of members, the score matches `MAXIMUM_DISTANCE`. The application uses one shared *k* for every cluster, clamped to the size of the smallest cluster. Examples use *k* = 2.

| Nearest (min-2nd) | Farthest (max-2nd) |
|:---:|:---:|
| <img src="figures/instances/points_min_2nd.png" width="420" alt="Points, 2nd nearest, nearest neighbor order" /> | <img src="figures/instances/points_max_2nd.png" width="420" alt="Points, 2nd nearest, farthest neighbor order" /> |
