package cvdexplorer.io;

import cvdexplorer.geometry.Vector;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.MetricMemberCompatibility;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SceneLimits;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SegmentMember;
import cvdexplorer.model.SiteMemberKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene file DTOs and Gson-free mapping to {@link SceneSnapshot}.
 * Kept separate from {@link SceneJsonCodec}, which owns the Gson encode/decode layer.
 */
public final class SceneFileFormat {
    public static final String CURRENT_VERSION = "1";

    private SceneFileFormat() {
    }

    public static SceneSnapshot fromDto(SceneFileV1 dto) throws SceneJsonException {
        if (dto == null) {
            throw new SceneJsonException("Empty scene file");
        }
        if (dto.version == null || !dto.version.equals(CURRENT_VERSION)) {
            throw new SceneJsonException("Unsupported or missing version (expected " + CURRENT_VERSION + ")");
        }
        if (dto.clusters == null || dto.clusters.isEmpty()) {
            throw new SceneJsonException("Scene must contain at least one cluster");
        }
        if (dto.clusters.size() > SceneLimits.MAX_CLUSTERS) {
            throw new SceneJsonException("Too many clusters (max " + SceneLimits.MAX_CLUSTERS + ")");
        }

        MetricKind metricKind = parseMetricKind(dto.metricKind);
        NeighborOrder neighborOrder = parseNeighborOrder(dto.neighborOrder);
        SiteMemberKind siteMemberKind;
        try {
            siteMemberKind = SiteMemberKind.valueOf(dto.siteMemberKind);
        } catch (Exception e) {
            throw new SceneJsonException("Unknown siteMemberKind: " + dto.siteMemberKind);
        }

        List<ClusterSite> loaded = new ArrayList<>();
        for (ClusterJson cj : dto.clusters) {
            if (cj.name == null) {
                throw new SceneJsonException("Cluster name is required");
            }
            if (cj.color == null) {
                throw new SceneJsonException("Cluster color is required for " + cj.name);
            }
            Rgba color = new Rgba(
                    cj.color.r,
                    cj.color.g,
                    cj.color.b,
                    cj.color.opacity
            );
            if (cj.members == null || cj.members.isEmpty()) {
                throw new SceneJsonException("Cluster must have at least one member: " + cj.name);
            }
            List<ClusterMember> members = parseMembers(cj.members, cj.name);
            loaded.add(new ClusterSite(cj.name, color, members));
        }

        String invalidMetricMessage = MetricMemberCompatibility.invalidMetricMessage(metricKind, loaded).orElse(null);
        if (invalidMetricMessage != null) {
            throw new SceneJsonException(invalidMetricMessage);
        }

        int loadedNearestNeighborK = 1;
        if (dto.nearestNeighborK != null) {
            if (dto.nearestNeighborK < 1 || dto.nearestNeighborK > SceneLimits.MAX_MEMBERS_PER_CLUSTER) {
                throw new SceneJsonException(
                        "nearestNeighborK must be between 1 and " + SceneLimits.MAX_MEMBERS_PER_CLUSTER
                );
            }
            loadedNearestNeighborK = dto.nearestNeighborK;
        }

        SceneSnapshot snapshot = new SceneSnapshot();
        if (dto.name != null && !dto.name.isBlank()) {
            snapshot.setName(dto.name.trim());
        }
        snapshot.setMetricKind(metricKind);
        snapshot.setNeighborOrder(neighborOrder);
        snapshot.setSiteMemberKind(siteMemberKind);
        snapshot.setNearestNeighborK(loadedNearestNeighborK);
        snapshot.setClusters(loaded);
        return snapshot;
    }

    public static SceneFileV1 toDto(SceneSnapshot snapshot) {
        List<ClusterJson> clusters = new ArrayList<>();
        for (ClusterSite site : snapshot.clusters()) {
            List<MemberJson> members = new ArrayList<>();
            for (ClusterMember member : site.members()) {
                if (member instanceof PointMember pm) {
                    Vector p = pm.position();
                    MemberJson mj = new MemberJson();
                    mj.kind = "POINT";
                    mj.x = p.x();
                    mj.y = p.y();
                    members.add(mj);
                } else if (member instanceof SegmentMember sm) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "LINE_SEGMENT";
                    mj.ax = sm.a().x();
                    mj.ay = sm.a().y();
                    mj.bx = sm.b().x();
                    mj.by = sm.b().y();
                    members.add(mj);
                } else if (member instanceof CircleMember cm) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "CIRCLE";
                    mj.cx = cm.center().x();
                    mj.cy = cm.center().y();
                    mj.radius = cm.radius();
                    members.add(mj);
                } else if (member instanceof EllipseMember em) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "ELLIPSE";
                    mj.ax = em.focusA().x();
                    mj.ay = em.focusA().y();
                    mj.bx = em.focusB().x();
                    mj.by = em.focusB().y();
                    mj.hx = em.controlHandle().x();
                    mj.hy = em.controlHandle().y();
                    members.add(mj);
                } else if (member instanceof LineMember lm) {
                    MemberJson mj = new MemberJson();
                    mj.kind = "LINE";
                    mj.px = lm.a().x();
                    mj.py = lm.a().y();
                    mj.qx = lm.b().x();
                    mj.qy = lm.b().y();
                    members.add(mj);
                }
            }
            Rgba c = site.color();
            ColorJson color = new ColorJson();
            color.r = c.r();
            color.g = c.g();
            color.b = c.b();
            color.opacity = c.a();
            ClusterJson cj = new ClusterJson();
            cj.name = site.name();
            cj.color = color;
            cj.members = members;
            clusters.add(cj);
        }
        SceneFileV1 root = new SceneFileV1();
        root.version = CURRENT_VERSION;
        root.name = snapshot.name();
        root.metricKind = snapshot.metricKind().name();
        root.neighborOrder = snapshot.neighborOrder().name();
        root.siteMemberKind = snapshot.siteMemberKind().name();
        root.nearestNeighborK = snapshot.nearestNeighborK();
        root.clusters = clusters;
        return root;
    }

    /**
     * Parses {@code metricKind} from JSON. {@code AVERAGE_DISTANCE} is accepted as a legacy alias for {@link MetricKind#MEAN_DISTANCE}.
     */
    public static MetricKind parseMetricKind(String name) throws SceneJsonException {
        if (name == null || name.isEmpty()) {
            throw new SceneJsonException("metricKind is required");
        }
        if ("AVERAGE_DISTANCE".equals(name)) {
            return MetricKind.MEAN_DISTANCE;
        }
        try {
            return MetricKind.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new SceneJsonException("Unknown metricKind: " + name);
        }
    }

    public static NeighborOrder parseNeighborOrder(String name) throws SceneJsonException {
        if (name == null || name.isEmpty()) {
            return NeighborOrder.NEAREST;
        }
        try {
            return NeighborOrder.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new SceneJsonException("Unknown neighborOrder: " + name);
        }
    }

    private static List<ClusterMember> parseMembers(List<MemberJson> members, String clusterName) throws SceneJsonException {
        List<ClusterMember> out = new ArrayList<>();
        for (MemberJson mj : members) {
            if (mj.kind == null) {
                throw new SceneJsonException("Member kind is required in cluster " + clusterName);
            }
            switch (mj.kind) {
                case "POINT" -> {
                    if (mj.x == null || mj.y == null) {
                        throw new SceneJsonException("POINT member requires x and y in cluster " + clusterName);
                    }
                    out.add(new PointMember(Vector.xy(mj.x, mj.y)));
                }
                case "LINE_SEGMENT" -> {
                    if (mj.ax == null || mj.ay == null || mj.bx == null || mj.by == null) {
                        throw new SceneJsonException("LINE_SEGMENT member requires ax, ay, bx, by in cluster " + clusterName);
                    }
                    out.add(new SegmentMember(Vector.xy(mj.ax, mj.ay), Vector.xy(mj.bx, mj.by)));
                }
                case "CIRCLE" -> {
                    if (mj.cx == null || mj.cy == null || mj.radius == null) {
                        throw new SceneJsonException("CIRCLE member requires cx, cy, radius in cluster " + clusterName);
                    }
                    if (mj.radius < 0.0) {
                        throw new SceneJsonException("CIRCLE member radius must be non-negative in cluster " + clusterName);
                    }
                    Vector center = Vector.xy(mj.cx, mj.cy);
                    out.add(new CircleMember(center, center.add(Vector.xy(mj.radius, 0.0))));
                }
                case "ELLIPSE" -> out.add(parseEllipse(mj, clusterName));
                case "LINE" -> {
                    if (mj.px == null || mj.py == null || mj.qx == null || mj.qy == null) {
                        throw new SceneJsonException("LINE member requires px, py, qx, qy in cluster " + clusterName);
                    }
                    out.add(new LineMember(Vector.xy(mj.px, mj.py), Vector.xy(mj.qx, mj.qy)));
                }
                default -> throw new SceneJsonException("Unknown member kind: " + mj.kind + " in cluster " + clusterName);
            }
        }
        if (out.size() > SceneLimits.MAX_MEMBERS_PER_CLUSTER) {
            throw new SceneJsonException("Too many members in cluster " + clusterName);
        }
        return out;
    }

    private static EllipseMember parseEllipse(MemberJson mj, String clusterName) throws SceneJsonException {
        if (mj.ax == null || mj.ay == null || mj.bx == null || mj.by == null || mj.hx == null || mj.hy == null) {
            throw new SceneJsonException("ELLIPSE member requires ax, ay, bx, by, hx, hy in cluster " + clusterName);
        }
        return new EllipseMember(
                Vector.xy(mj.ax, mj.ay),
                Vector.xy(mj.bx, mj.by),
                Vector.xy(mj.hx, mj.hy)
        );
    }

    /** Field names match JSON keys. */
    public static final class SceneFileV1 {
        public String version;
        public String name;
        public String metricKind;
        public String neighborOrder;
        public String siteMemberKind;
        public Integer nearestNeighborK;
        public List<ClusterJson> clusters;
    }

    public static final class ClusterJson {
        public String name;
        public ColorJson color;
        public List<MemberJson> members;
    }

    public static final class ColorJson {
        public double r;
        public double g;
        public double b;
        public double opacity;
    }

    public static final class MemberJson {
        public String kind;
        public Double x;
        public Double y;
        public Double ax;
        public Double ay;
        public Double bx;
        public Double by;
        public Double cx;
        public Double cy;
        public Double radius;
        public Double px;
        public Double py;
        public Double qx;
        public Double qy;
        public Double hx;
        public Double hy;
    }
}
