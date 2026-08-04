package cvdexplorer.model;

import cvdexplorer.metric.MetricKind;

import java.util.ArrayList;
import java.util.List;

/** Authoring fields and clusters for scene JSON (no UI gadgets). */
public final class SceneSnapshot {
    /** Optional human-readable gallery label; null when unset. */
    private String name;
    private MetricKind metricKind = MetricKind.MINIMUM_DISTANCE;
    private NeighborOrder neighborOrder = NeighborOrder.NEAREST;
    private SiteMemberKind siteMemberKind = SiteMemberKind.POINT;
    private int metricParameter = 1;
    /** Creation-time member parameter (e.g. vertex count for new {@link SiteMemberKind#POLYGON} members). */
    private int memberParameter = SceneLimits.DEFAULT_MEMBER_PARAMETER;
    private final List<ClusterSite> clusters = new ArrayList<>();

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricKind metricKind() {
        return metricKind;
    }

    public void setMetricKind(MetricKind metricKind) {
        this.metricKind = metricKind;
    }

    public NeighborOrder neighborOrder() {
        return neighborOrder;
    }

    public void setNeighborOrder(NeighborOrder neighborOrder) {
        this.neighborOrder = neighborOrder;
    }

    public SiteMemberKind siteMemberKind() {
        return siteMemberKind;
    }

    public void setSiteMemberKind(SiteMemberKind siteMemberKind) {
        this.siteMemberKind = siteMemberKind;
    }

    public int metricParameter() {
        return metricParameter;
    }

    public void setMetricParameter(int metricParameter) {
        this.metricParameter = metricParameter;
    }

    public int memberParameter() {
        return memberParameter;
    }

    public void setMemberParameter(int memberParameter) {
        this.memberParameter = memberParameter;
    }

    public List<ClusterSite> clusters() {
        return clusters;
    }

    public void setClusters(List<ClusterSite> sites) {
        clusters.clear();
        clusters.addAll(sites);
    }
}
