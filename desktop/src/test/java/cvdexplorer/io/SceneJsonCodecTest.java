package cvdexplorer.io;

import cvdexplorer.metric.MetricKind;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.EllipseMember;
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
import cvdexplorer.geometry.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneJsonCodecTest {

    @Test
    void encodeDecodeRoundTripPreservesClustersMetricAndMembers() throws Exception {
        SceneSnapshot source = new SceneSnapshot();
        source.setMetricKind(MetricKind.MINIMUM_DISTANCE);
        source.setNeighborOrder(NeighborOrder.FARTHEST);
        source.setSiteMemberKind(SiteMemberKind.LINE);
        source.setMemberParameter(5);
        source.setName("Round-trip example");
        source.setClusters(List.of(
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
                        List.of(new PointMember(Vector.xy(3, 4)))
                )
        ));

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

        assertEquals(MetricKind.MINIMUM_DISTANCE, restored.metricKind);
        assertEquals(NeighborOrder.FARTHEST, restored.neighborOrder);
        assertEquals(SiteMemberKind.LINE, restored.siteMemberKind);
        assertEquals(source.metricParameter(), restored.metricParameter);
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

        assertTrue(json.contains("\"kind\": \"CIRCLE\""));
        assertTrue(json.contains("\"kind\": \"ELLIPSE\""));
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
    void rejectsSumOfDistancesWithCircleMembers() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "SUM_OF_DISTANCES",
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
                """;
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("SUM_OF_DISTANCES"));
    }

    @Test
    void rejectsMeanDistanceWithCircleMembers() {
        String json = """
                {
                  "version": "1",
                  "metricKind": "MEAN_DISTANCE",
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
                """;
        SceneJsonException ex = assertThrows(SceneJsonException.class, () -> SceneJsonCodec.parse(json));
        assertTrue(ex.getMessage().contains("MEAN_DISTANCE"));
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
}
