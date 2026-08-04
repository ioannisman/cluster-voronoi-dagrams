package cvdexplorer.model;

import cvdexplorer.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

public final class SiteMemberFactory {
    private static final double DEFAULT_POLYGON_RADIUS = 28.0;

    private SiteMemberFactory() {
    }

    public static ClusterMember createDefault(SiteMemberKind kind, int clusterIndex, int memberIndex, Vector hint) {
        return createDefault(kind, clusterIndex, memberIndex, hint, SceneLimits.DEFAULT_MEMBER_PARAMETER);
    }

    public static ClusterMember createDefault(
            SiteMemberKind kind,
            int clusterIndex,
            int memberIndex,
            Vector hint,
            int memberParameter
    ) {
        return switch (kind) {
            case POINT -> new PointMember(hint);
            case LINE_SEGMENT -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(22, angle);
                yield new SegmentMember(hint.sub(half), hint.add(half));
            }
            case CIRCLE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector radius = Vector.polar(22, angle);
                yield new CircleMember(hint, hint.add(radius));
            }
            case ELLIPSE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(18, angle);
                Vector focusA = hint.sub(half);
                Vector focusB = hint.add(half);
                Vector control = hint.add(Vector.polar(40, angle + Math.PI * 0.5));
                yield new EllipseMember(focusA, focusB, control);
            }
            case LINE -> {
                double angle = 2 * Math.PI * (memberIndex * 0.618033988749895 + clusterIndex * 0.31);
                Vector half = Vector.polar(22, angle);
                yield new LineMember(hint.sub(half), hint.add(half));
            }
            case POLYGON -> regularPolygon(hint, memberParameter, clusterIndex, memberIndex);
        };
    }

    private static PolygonMember regularPolygon(Vector hint, int vertexCount, int clusterIndex, int memberIndex) {
        int n = Math.max(SceneLimits.MIN_POLYGON_VERTICES, Math.min(SceneLimits.MAX_POLYGON_VERTICES, vertexCount));
        double rotationTurns = memberIndex * 0.618033988749895 + clusterIndex * 0.31;
        List<Vector> vertices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double angleTurns = rotationTurns + (double) i / n;
            vertices.add(hint.add(Vector.polar(DEFAULT_POLYGON_RADIUS, angleTurns)));
        }
        return new PolygonMember(vertices);
    }
}
