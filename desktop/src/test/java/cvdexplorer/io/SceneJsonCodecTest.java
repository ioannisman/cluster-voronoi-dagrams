package cvdexplorer.io;

import cvdexplorer.core.ClusterOwnershipSelector;
import cvdexplorer.core.ScenePreparation;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.KEllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.PolygonMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SegmentMember;
import cvdexplorer.model.SiteMemberKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import cvdexplorer.field.DistanceGridLimits;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Vector;
import cvdexplorer.kellipse.KEllipsePreparationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneJsonCodecTest {

    @ParameterizedTest
    @EnumSource(MetricKind.class)
    void encodeDecodeRoundTripPreservesClustersMetricAndMembers(MetricKind metricKind) throws Exception {
        int metricParameter = metricKind == MetricKind.KTH_NEAREST_DISTANCE ? 2 : 1;
        SceneSnapshot source = new SceneSnapshot();
        source.setMetricKind(metricKind);
        source.setNeighborOrder(NeighborOrder.FARTHEST);
        source.setSiteMemberKind(SiteMemberKind.LINE);
        source.setMetricParameter(metricParameter);
        source.setMemberParameter(5);
        source.setName("Round-trip example");
        source.setClusters(mixedMemberClusters());

        String json = SceneJsonCodec.encode(source);
        SceneSnapshot parsed = SceneJsonCodec.parse(json);

        assertEquals("Round-trip example", parsed.name());
        assertTrue(json.contains("\"name\": \"Round-trip example\""));

        SceneState restored = new SceneState();
        restored.metricKind = MetricKind.MINIMUM_DISTANCE;
        restored.siteMemberKind = SiteMemberKind.POINT;
        restored.clusters().add(new ClusterSite("X", Rgba.BLACK, List.of(new PointMember(Vector.xy(0, 0)))));
        restored.numberOfClusters = 1;
        restored.applyLoadedScene(
                parsed.metricKind(),
                parsed.neighborOrder(),
                parsed.siteMemberKind(),
                parsed.clusters(),
                parsed.metricParameter(),
                parsed.memberParameter()
        );

        assertEquals(metricKind, restored.metricKind);
        assertEquals(NeighborOrder.FARTHEST, restored.neighborOrder);
        assertEquals(SiteMemberKind.LINE, restored.siteMemberKind);
        assertEquals(metricParameter, restored.metricParameter);
        assertEquals(5, restored.memberParameter);
        assertEquals(2, restored.clusters().size());

        ClusterSite a = restored.clusters().get(0);
        assertEquals("Alpha", a.name());
        assertEquals(6, a.members().size());
        PointMember p = (PointMember) a.members().get(0);
        assertEquals(10.0, p.position().x(), 1e-9);
        assertEquals(-20.0, p.position().y(), 1e-9);
        SegmentMember s = (SegmentMember) a.members().get(1);
        assertEquals(0.0, s.a().x(), 1e-9);
        assertEquals(100.0, s.b().x(), 1e-9);
        CircleMember c = assertInstanceOf(CircleMember.class, a.members().get(2));
        assertEquals(20.0, c.center().x(), 1e-9);
        assertEquals(30.0, c.center().y(), 1e-9);
        assertEquals(6.0, c.radius(), 1e-9);
        LineMember l = (LineMember) a.members().get(3);
        assertEquals(-10.0, l.a().x(), 1e-9);
        EllipseMember e = assertInstanceOf(EllipseMember.class, a.members().get(4));
        assertEquals(-20.0, e.focusA().x(), 1e-9);
        assertEquals(20.0, e.focusB().x(), 1e-9);
        PolygonMember poly = assertInstanceOf(PolygonMember.class, a.members().get(5));
        assertEquals(3, poly.handleCount());
        assertEquals(3.0, poly.getHandle(2).y(), 1e-9);
        assertEquals(0.0, e.controlHandle().x(), 1e-9);
        assertEquals(15.0, e.controlHandle().y(), 1e-9);

        assertEquals(2, restored.clusters().get(1).members().size());
        assertTrue(json.contains("\"kind\": \"CIRCLE\""));
        assertTrue(json.contains("\"kind\": \"ELLIPSE\""));
        assertClusterScoresMatch(source.clusters(), restored.clusters(), metricKind, metricParameter);
    }

    private static List<ClusterSite> mixedMemberClusters() {
        return List.of(
                new ClusterSite(
                        "Alpha",
                        new Rgba(0.2, 0.4, 0.6, 0.95),
                        List.of(
                                new PointMember(Vector.xy(10, -20)),
                                new SegmentMember(Vector.xy(0, 0), Vector.xy(100, 5)),
                                new CircleMember(Vector.xy(20, 30), Vector.xy(26, 30)),
                                new LineMember(Vector.xy(-10, 5), Vector.xy(15, 35)),
                                new EllipseMember(
                                        Vector.xy(-20, 0),
                                        Vector.xy(20, 0),
                                        Vector.xy(0, 15)
                                ),
                                new PolygonMember(List.of(
                                        Vector.xy(0, 0),
                                        Vector.xy(4, 0),
                                        Vector.xy(2, 3)
                                ))
                        )
                ),
                new ClusterSite(
                        "Beta",
                        Rgba.hsb(120, 0.5, 0.9),
                        List.of(
                                new PointMember(Vector.xy(3, 4)),
                                new PointMember(Vector.xy(8, 9))
                        )
                )
        );
    }

    private static void assertClusterScoresMatch(
            List<ClusterSite> before,
            List<ClusterSite> after,
            MetricKind metricKind,
            int metricParameter
    ) {
        ScenePreparation.PreparedScene sourceScene = ScenePreparation.prepare(
                before,
                metricKind,
                NeighborOrder.NEAREST,
                metricParameter
        );
        ScenePreparation.PreparedScene restoredScene = ScenePreparation.prepare(
                after,
                metricKind,
                NeighborOrder.NEAREST,
                metricParameter
        );
        List<Vector> queries = List.of(Vector.xy(0, 0), Vector.xy(10, -20), Vector.xy(40, 25));
        for (Vector query : queries) {
            for (int i = 0; i < before.size(); i++) {
                assertEquals(
                        sourceScene.metric().score(query, sourceScene.clusters().get(i)),
                        restoredScene.metric().score(query, restoredScene.clusters().get(i)),
                        1e-9
                );
            }
        }
    }

    @Test
    void rejectsWrongVersion() {
        String json = "{\"version\":\"99\",\"metricKind\":\"MINIMUM_DISTANCE\",\"siteMemberKind\":\"POINT\",\"clusters\":[]}";
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("version"));
    }

    @Test
    void rejectsNegativeCircleRadius() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "CIRCLE",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 1.0, "cy": 2.0, "radius": -4.0}
                      ]
                    }
                  ]
                }
                """;
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("radius"));
    }

    @Test
    void rejectsEllipseWithoutHandles() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "ELLIPSE",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "ELLIPSE", "ax": 0.0, "ay": 0.0}
                      ]
                    }
                  ]
                }
                """;
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("ELLIPSE"));
    }

    @Test
    void loadsSumOfDistancesWithCircleMembers() throws Exception {
        SceneSnapshot parsed = SceneJsonCodec.parse(circleClusterJson("SUM_OF_DISTANCES"));

        assertEquals(MetricKind.SUM_OF_DISTANCES, parsed.metricKind());
        CircleMember circle = assertInstanceOf(CircleMember.class, parsed.clusters().get(0).members().get(0));
        assertEquals(4.0, circle.radius(), 1.0e-9);
    }

    @Test
    void loadsMeanDistanceWithCircleMembers() throws Exception {
        SceneSnapshot parsed = SceneJsonCodec.parse(circleClusterJson("MEAN_DISTANCE"));

        assertEquals(MetricKind.MEAN_DISTANCE, parsed.metricKind());
        CircleMember circle = assertInstanceOf(CircleMember.class, parsed.clusters().get(0).members().get(0));
        assertEquals(4.0, circle.radius(), 1.0e-9);
    }

    @Test
    void loadsKthNearestCircleClustersAndSelectsByAggregateScore() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "KTH_NEAREST_DISTANCE",
                  "neighborOrder": "NEAREST",
                  "siteMemberKind": "CIRCLE",
                  "metricParameter": 2,
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 1.0, "g": 0.0, "b": 0.0, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 0.0, "cy": 0.0, "radius": 1.0},
                        {"kind": "CIRCLE", "cx": 0.0, "cy": 0.0, "radius": 3.0}
                      ]
                    },
                    {
                      "name": "Beta",
                      "color": {"r": 0.0, "g": 0.0, "b": 1.0, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 6.0, "cy": 0.0, "radius": 1.0},
                        {"kind": "CIRCLE", "cx": 6.0, "cy": 0.0, "radius": 2.0}
                      ]
                    }
                  ]
                }
                """;
        SceneSnapshot parsed = SceneJsonCodec.parse(json);
        assertEquals(MetricKind.KTH_NEAREST_DISTANCE, parsed.metricKind());
        assertInstanceOf(CircleMember.class, parsed.clusters().get(0).members().get(0));

        Vector query = Vector.xy(10, 0);
        ScenePreparation.PreparedScene nearest = ScenePreparation.prepare(
                parsed.clusters(),
                parsed.metricKind(),
                NeighborOrder.NEAREST,
                parsed.metricParameter()
        );
        ClusterOwnershipSelector.Result nearestOwner = nearest.ownershipSelector().selectOwner(
                query,
                nearest.clusters(),
                nearest.metric()
        );
        assertEquals(1, nearestOwner.clusterIndex());
        assertEquals(3.0, nearestOwner.score(), 1.0e-9);

        ScenePreparation.PreparedScene farthest = ScenePreparation.prepare(
                parsed.clusters(),
                parsed.metricKind(),
                NeighborOrder.FARTHEST,
                parsed.metricParameter()
        );
        ClusterOwnershipSelector.Result farthestOwner = farthest.ownershipSelector().selectOwner(
                query,
                farthest.clusters(),
                farthest.metric()
        );
        assertEquals(0, farthestOwner.clusterIndex());
        assertEquals(9.0, farthestOwner.score(), 1.0e-9);
    }

    private static String circleClusterJson(String metricKind) {
        return """
                {
                  "version": "1",
                  "metricKind": "%s",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "CIRCLE", "cx": 1.0, "cy": 2.0, "radius": 4.0}
                      ]
                    }
                  ]
                }
                """.formatted(metricKind);
    }

    @Test
    void legacyAverageDistanceMetricKindInJsonLoadsAsMeanDistance() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "AVERAGE_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [
                        {"kind": "POINT", "x": 0.0, "y": 0.0}
                      ]
                    }
                  ]
                }
                """;
        SceneSnapshot parsed = SceneJsonCodec.parse(json);
        SceneState state = SceneState.demo();
        state.applyLoadedScene(
                parsed.metricKind(),
                parsed.neighborOrder(),
                parsed.siteMemberKind(),
                parsed.clusters(),
                parsed.metricParameter(),
                parsed.memberParameter()
        );
        assertEquals(MetricKind.MEAN_DISTANCE, state.metricKind);
    }

    @Test
    void encodeDecodeRoundTripPreservesMetricParameter() throws Exception {
        SceneSnapshot source = new SceneSnapshot();
        source.setMetricKind(MetricKind.KTH_NEAREST_DISTANCE);
        source.setNeighborOrder(NeighborOrder.FARTHEST);
        source.setSiteMemberKind(SiteMemberKind.POINT);
        source.setMetricParameter(2);
        source.setClusters(List.of(
                new ClusterSite(
                        "A",
                        Rgba.RED,
                        List.of(new PointMember(Vector.xy(0, 0)), new PointMember(Vector.xy(10, 0)), new PointMember(Vector.xy(5, 5)))
                ),
                new ClusterSite(
                        "B",
                        Rgba.BLUE,
                        List.of(new PointMember(Vector.xy(100, 0)), new PointMember(Vector.xy(110, 0)))
                )
        ));

        String json = SceneJsonCodec.encode(source);
        SceneSnapshot parsed = SceneJsonCodec.parse(json);
        SceneState restored = SceneState.demo();
        restored.applyLoadedScene(
                parsed.metricKind(),
                parsed.neighborOrder(),
                parsed.siteMemberKind(),
                parsed.clusters(),
                parsed.metricParameter(),
                parsed.memberParameter()
        );

        assertEquals(MetricKind.KTH_NEAREST_DISTANCE, restored.metricKind);
        assertEquals(NeighborOrder.FARTHEST, restored.neighborOrder);
        assertEquals(2, restored.metricParameter);
    }

    @Test
    void missingNeighborOrderInJsonDefaultsToNearest() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [{"kind": "POINT", "x": 0.0, "y": 0.0}]
                    }
                  ]
                }
                """;
        SceneSnapshot parsed = SceneJsonCodec.parse(json);
        SceneState state = SceneState.demo();
        state.neighborOrder = NeighborOrder.FARTHEST;
        state.applyLoadedScene(
                parsed.metricKind(),
                parsed.neighborOrder(),
                parsed.siteMemberKind(),
                parsed.clusters(),
                parsed.metricParameter(),
                parsed.memberParameter()
        );
        assertEquals(NeighborOrder.NEAREST, state.neighborOrder);
    }

    @Test
    void rejectsInvalidMetricParameter() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "POINT",
                  "metricParameter": 0,
                  "clusters": [
                    {
                      "name": "Alpha",
                      "color": {"r": 0.1, "g": 0.2, "b": 0.3, "opacity": 1.0},
                      "members": [{"kind": "POINT", "x": 0.0, "y": 0.0}]
                    }
                  ]
                }
                """;
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("metricParameter"));
    }

    @Test
    void encodeDecodeRoundTripPreservesKEllipseGeometry() throws Exception {
        KEllipseMember ellipse = new KEllipseMember(
                List.of(Vector.xy(-4, 0), Vector.xy(0, 3), Vector.xy(4, -1)),
                Vector.xy(0, 8)
        );
        SceneSnapshot source = new SceneSnapshot();
        source.setMetricKind(MetricKind.MINIMUM_DISTANCE);
        source.setNeighborOrder(NeighborOrder.NEAREST);
        source.setSiteMemberKind(SiteMemberKind.K_ELLIPSE);
        source.setMemberParameter(4);
        source.setClusters(List.of(new ClusterSite("Cedar", Rgba.RED, List.of(ellipse, new PointMember(Vector.xy(1, 2))))));

        String json = SceneJsonCodec.encode(source);
        assertTrue(json.contains("\"kind\": \"K_ELLIPSE\""));
        assertTrue(!json.contains("polyline"));
        SceneSnapshot parsed = SceneJsonCodec.parse(json);
        assertEquals(SiteMemberKind.K_ELLIPSE, parsed.siteMemberKind());
        KEllipseMember restored = assertInstanceOf(KEllipseMember.class, parsed.clusters().get(0).members().get(0));
        assertEquals(3, restored.focusCount());
        assertEquals(-4.0, restored.getHandle(0).x(), 1e-9);
        assertEquals(3.0, restored.getHandle(1).y(), 1e-9);
        assertEquals(0.0, restored.controlHandle().x(), 1e-9);
        assertEquals(8.0, restored.controlHandle().y(), 1e-9);
        assertEquals(ellipse.level(), restored.level(), 1e-9);
        assertInstanceOf(PointMember.class, parsed.clusters().get(0).members().get(1));
    }

    @Test
    void rejectsDegenerateKEllipseFile() throws Exception {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "K_ELLIPSE",
                  "clusters": [
                    {
                      "name": "Cedar",
                      "color": {"r": 0.2, "g": 0.4, "b": 0.6, "opacity": 1.0},
                      "members": [
                        {"kind": "K_ELLIPSE", "xs": [-1.0, 0.0, 1.0], "ys": [0.0, 0.0, 0.0], "hx": 0.0, "hy": 0.0}
                      ]
                    }
                  ]
                }
                """;
        SceneSnapshot parsed = SceneJsonCodec.parse(json);
        assertInstanceOf(KEllipseMember.class, parsed.clusters().get(0).members().get(0));

        SceneState state = new SceneState();
        state.clusters().add(new ClusterSite("Keep", Rgba.RED, List.of(new PointMember(Vector.xy(1, 2)))));
        KEllipsePreparationException ex = assertThrows(
                KEllipsePreparationException.class,
                () -> PreparedSceneInstall.install(
                        state,
                        parsed,
                        Box.pq(Vector.xy(-40, -40), Vector.xy(40, 40)),
                        DistanceGridLimits.DEFAULT
                )
        );
        assertTrue(ex.getMessage().contains("Cedar"));
        assertTrue(ex.getMessage().contains("cannot be prepared"));
        assertEquals("Keep", state.clusters().get(0).name());
        assertInstanceOf(PointMember.class, state.clusters().get(0).members().get(0));
    }

    @Test
    void rejectsKEllipseWithFewerThanThreeFoci() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MINIMUM_DISTANCE",
                  "siteMemberKind": "POINT",
                  "clusters": [
                    {
                      "name": "Cedar",
                      "color": {"r": 0.2, "g": 0.4, "b": 0.6, "opacity": 1.0},
                      "members": [
                        {"kind": "K_ELLIPSE", "xs": [0.0, 1.0], "ys": [0.0, 0.0], "hx": 0.0, "hy": 5.0}
                      ]
                    }
                  ]
                }
                """;
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("Cedar"));
        assertTrue(ex.getMessage().contains("foci"));
    }
}
