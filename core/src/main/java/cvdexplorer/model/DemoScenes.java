package cvdexplorer.model;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.geometry.Vector;

import java.util.List;

/**
 * Default explorer scene used by desktop reset/demo.
 * Geometry matches {@code scenes/default_cvd.json}.
 */
public final class DemoScenes {
    private DemoScenes() {
    }

    public static SceneSnapshot defaultSnapshot() {
        SceneSnapshot snapshot = new SceneSnapshot();
        snapshot.setMetricKind(MetricKind.MINIMUM_DISTANCE);
        snapshot.setNeighborOrder(NeighborOrder.NEAREST);
        snapshot.setSiteMemberKind(SiteMemberKind.POINT);
        snapshot.setMetricParameter(1);
        snapshot.setMemberParameter(SceneLimits.DEFAULT_MEMBER_PARAMETER);
        snapshot.setClusters(defaultClusters());
        return snapshot;
    }

    public static List<ClusterSite> defaultClusters() {
        return List.of(
                new ClusterSite(
                        "Amber",
                        new Rgba(1.0, 0.625, 0.25, 1.0),
                        List.of(
                                new PointMember(Vector.xy(-154.5, 98.0)),
                                circle(-51.5, -279.0, 115.77672477661476),
                                new PolygonMember(List.of(
                                        Vector.xy(-240.0, 160.0),
                                        Vector.xy(-370.0, 190.0),
                                        Vector.xy(-320.0, 290.0)
                                ))
                        )
                ),
                new ClusterSite(
                        "Azure",
                        new Rgba(0.2375, 0.59375, 0.95, 1.0),
                        List.of(
                                new PointMember(Vector.xy(-61.5, -40.0)),
                                new PointMember(Vector.xy(343.5, -133.0)),
                                new PointMember(Vector.xy(372.5, 24.0)),
                                new EllipseMember(
                                        Vector.xy(120.0, -40.0),
                                        Vector.xy(285.5, 320.0),
                                        Vector.xy(180.0, 50.0)
                                )
                        )
                ),
                new ClusterSite(
                        "Rose",
                        new Rgba(1.0, 0.3, 0.65, 1.0),
                        List.of(
                                new SegmentMember(Vector.xy(-376.5, 12.0), Vector.xy(-37.5, -99.0)),
                                new SegmentMember(Vector.xy(-0.5, -12.0), Vector.xy(-120.0, 220.0)),
                                new SegmentMember(Vector.xy(-120.0, 220.0), Vector.xy(-376.5, 12.0)),
                                new SegmentMember(Vector.xy(-210.5, 87.0), Vector.xy(-37.5, -99.0)),
                                new SegmentMember(Vector.xy(-0.5, -12.0), Vector.xy(-210.5, 87.0)),
                                new PointMember(Vector.xy(-87.5, -267.0))
                        )
                ),
                new ClusterSite(
                        "Lime",
                        new Rgba(0.375, 0.9, 0.27, 1.0),
                        List.of(
                                circle(80.0, 200.0, 83.95981181493917),
                                new PointMember(Vector.xy(272.5, 107.0)),
                                new LineMember(Vector.xy(-317.5, -105.0), Vector.xy(261.5, -182.0))
                        )
                )
        );
    }

    /** Same radius encoding as {@code SceneJsonCodec}: handle at center + (radius, 0). */
    private static CircleMember circle(double cx, double cy, double radius) {
        Vector center = Vector.xy(cx, cy);
        return new CircleMember(center, center.add(Vector.xy(radius, 0.0)));
    }
}
