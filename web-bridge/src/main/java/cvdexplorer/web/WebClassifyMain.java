package cvdexplorer.web;

import cvdexplorer.HandleVisibility;
import cvdexplorer.core.DiagramRasterizer;
import cvdexplorer.core.ScenePreparation;
import cvdexplorer.core.ScenePreparation.PreparedScene;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.MetricMemberCompatibility;
import cvdexplorer.model.CircleMember;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterNaming;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.DemoScenes;
import cvdexplorer.model.EllipseMember;
import cvdexplorer.model.LineMember;
import cvdexplorer.model.NeighborOrder;
import cvdexplorer.model.PointMember;
import cvdexplorer.model.Rgba;
import cvdexplorer.model.SceneLimits;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SegmentMember;
import cvdexplorer.model.SiteMemberFactory;
import cvdexplorer.model.SiteMemberKind;
import cvdexplorer.render.ClusterColorizer;
import cvdexplorer.io.SceneJsonException;
import org.teavm.jso.JSBody;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TeaVM entry for the web shell: installs {@code globalThis.cvdCore} with
 * frame render, handle move, and scene/view setters backed by a mutable live scene.
 */
public final class WebClassifyMain {
    private static final double DEFAULT_WORLD_MIN = -400.0;
    private static final double DEFAULT_WORLD_MAX = 400.0;

    private static double worldMinX = DEFAULT_WORLD_MIN;
    private static double worldMaxX = DEFAULT_WORLD_MAX;
    private static double worldMinY = DEFAULT_WORLD_MIN;
    private static double worldMaxY = DEFAULT_WORLD_MAX;

    private static final DiagramRasterizer RASTERIZER = new DiagramRasterizer();

    /** Mutable live scene backing the web demo; point handles are dragged in place via {@link #moveHandle}. */
    private static SceneSnapshot sceneSnapshot = demoSnapshot();

    private static boolean shadingEnabled = false;
    private static String lastError = "";

    private static int activeClusterIndex = 0;
    private static int selectedClusterIndex = -1;
    private static int selectedMemberIndex = -1;
    private static int selectedHandleIndex = -1;

    /** Other handles colocated with the drag start (same cluster); cleared by {@link #endHandleDrag}. */
    private static final List<int[]> coMovingHandles = new ArrayList<>();
    private static int coMoveClusterIndex = -1;

    private static int[] lastArgb = new int[0];
    private static int[] lastOwners = new int[0];
    private static int[] lastMembers = new int[0];
    private static int lastWidth = 0;
    private static int lastHeight = 0;

    private static double[] handleXs = new double[0];
    private static double[] handleYs = new double[0];
    private static int[] handleClusterIndices = new int[0];
    private static double[] handleRs = new double[0];
    private static double[] handleGs = new double[0];
    private static double[] handleBs = new double[0];
    private static boolean[] handleVisible = new boolean[0];
    private static int handleTotal = 0;

    // Maps flat handle index -> (member index within cluster, handle index within member) for moveHandle.
    private static int[] handleMemberIndices = new int[0];
    private static int[] handleWithinMemberIndices = new int[0];

    // Packed member overlays for canvas drawing (see computeOverlays).
    private static String[] overlayKinds = new String[0];
    private static int[] overlayClusters = new int[0];
    private static int[] overlayMembers = new int[0];
    private static double[] overlayAx = new double[0];
    private static double[] overlayAy = new double[0];
    private static double[] overlayBx = new double[0];
    private static double[] overlayBy = new double[0];
    private static double[] overlayRadius = new double[0];
    private static double[] ellipseXs = new double[0];
    private static double[] ellipseYs = new double[0];
    private static int[] ellipseStarts = new int[0];
    private static int overlayCount = 0;

    private WebClassifyMain() {
    }

    public static void main(String[] args) {
        installApi();
    }

    /**
     * Rasterizes the current live scene and refreshes the cached handle layout.
     * Pixel (0,0) is top-left; world Y increases upward (canvas Y is flipped in the mapping).
     */
    public static void computeFrame(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        PreparedScene prepared = ScenePreparation.prepare(
                sceneSnapshot.clusters(),
                sceneSnapshot.metricKind(),
                sceneSnapshot.neighborOrder(),
                sceneSnapshot.nearestNeighborK()
        );
        ClusterColorizer colorizer = new ClusterColorizer(
                prepared.clusters(),
                Rgba.gray(0.92),
                shadingEnabled
        );
        Box imageBox = Box.pq(Vector.ZERO, Vector.xy(width, height)).positive();
        Transformation tFromPixels = pixelToWorld(width, height);
        DiagramRasterizer.RasterResult result = RASTERIZER.render(
                tFromPixels,
                imageBox,
                point -> {
                    var ownership = prepared.ownershipSelector().selectOwner(
                            point,
                            prepared.clusters(),
                            prepared.metric()
                    );
                    return new DiagramRasterizer.Classification(
                            ownership.clusterIndex(),
                            ownership.score(),
                            ownership.memberIndex()
                    );
                },
                colorizer::color,
                1.0
        );
        if (result == null || result.argbPixels() == null) {
            throw new IllegalStateException("rasterizer returned null");
        }
        lastWidth = result.width();
        lastHeight = result.height();
        lastArgb = result.argbPixels();
        lastOwners = result.ownershipGrid().clusterIndices();
        lastMembers = result.ownershipGrid().memberIndices();
        computeHandles(prepared.clusters());
        computeOverlays(prepared.clusters());
    }

    /**
     * Applies a metric if compatible with the current members.
     * @return empty string on success, otherwise an error message (metric unchanged)
     */
    public static String setMetricKindName(String name) {
        MetricKind kind;
        try {
            kind = MetricKind.valueOf(name);
        } catch (IllegalArgumentException ex) {
            lastError = "Unknown metric: " + name;
            return lastError;
        }
        Optional<String> invalid = MetricMemberCompatibility.invalidMetricMessage(kind, sceneSnapshot.clusters());
        if (invalid.isPresent()) {
            lastError = invalid.get();
            return lastError;
        }
        sceneSnapshot.setMetricKind(kind);
        lastError = "";
        return "";
    }

    public static String setNeighborOrderName(String name) {
        try {
            sceneSnapshot.setNeighborOrder(NeighborOrder.valueOf(name));
            lastError = "";
            return "";
        } catch (IllegalArgumentException ex) {
            lastError = "Unknown neighbor order: " + name;
            return lastError;
        }
    }

    public static String setNearestNeighborK(int k) {
        if (k < 1 || k > SceneLimits.MAX_MEMBERS_PER_CLUSTER) {
            lastError = "Metric parameter k must be between 1 and " + SceneLimits.MAX_MEMBERS_PER_CLUSTER;
            return lastError;
        }
        sceneSnapshot.setNearestNeighborK(k);
        lastError = "";
        return "";
    }

    public static void setWorldView(double minX, double maxX, double minY, double maxY) {
        if (!(maxX > minX) || !(maxY > minY)) {
            lastError = "World view requires max > min on both axes";
            return;
        }
        worldMinX = minX;
        worldMaxX = maxX;
        worldMinY = minY;
        worldMaxY = maxY;
        lastError = "";
    }

    public static double worldMinX() {
        return worldMinX;
    }

    public static double worldMaxX() {
        return worldMaxX;
    }

    public static double worldMinY() {
        return worldMinY;
    }

    public static double worldMaxY() {
        return worldMaxY;
    }

    public static void setShadingEnabled(boolean enabled) {
        shadingEnabled = enabled;
    }

    public static String metricKindName() {
        return sceneSnapshot.metricKind().name();
    }

    public static String neighborOrderName() {
        return sceneSnapshot.neighborOrder().name();
    }

    public static int nearestNeighborK() {
        return sceneSnapshot.nearestNeighborK();
    }

    public static boolean shadingEnabled() {
        return shadingEnabled;
    }

    public static String lastError() {
        return lastError;
    }

    public static int clusterCount() {
        return sceneSnapshot.clusters().size();
    }

    public static int activeClusterIndex() {
        // Selection is the only current cluster; -1 when nothing is selected.
        return selectedClusterIndex;
    }

    public static int activeMemberCount() {
        if (selectedClusterIndex < 0 || selectedClusterIndex >= sceneSnapshot.clusters().size()) {
            return 0;
        }
        return sceneSnapshot.clusters().get(selectedClusterIndex).size();
    }

    public static String siteMemberKindName() {
        return sceneSnapshot.siteMemberKind().name();
    }

    public static int selectedClusterIndex() {
        return selectedClusterIndex;
    }

    public static int selectedMemberIndex() {
        return selectedMemberIndex;
    }

    public static int selectedHandleIndex() {
        return selectedHandleIndex;
    }

    public static String clusterNameAt(int index) {
        if (index < 0 || index >= sceneSnapshot.clusters().size()) {
            return "";
        }
        return sceneSnapshot.clusters().get(index).name();
    }

    /** Kept for API compatibility; active cluster is derived from selection only. */
    public static String setActiveClusterIndex(int index) {
        lastError = "";
        return "";
    }

    public static String setSiteMemberKindName(String name) {
        SiteMemberKind kind;
        try {
            kind = SiteMemberKind.valueOf(name);
        } catch (IllegalArgumentException ex) {
            lastError = "Unknown site member kind: " + name;
            return lastError;
        }
        Optional<String> invalid = MetricMemberCompatibility.invalidNewMemberMessage(
                sceneSnapshot.metricKind(),
                kind
        );
        if (invalid.isPresent()) {
            lastError = invalid.get();
            return lastError;
        }
        sceneSnapshot.setSiteMemberKind(kind);
        lastError = "";
        return "";
    }

    public static void selectHandle(int handleIndex) {
        if (handleIndex < 0 || handleIndex >= handleTotal) {
            return;
        }
        selectedClusterIndex = handleClusterIndices[handleIndex];
        selectedMemberIndex = handleMemberIndices[handleIndex];
        selectedHandleIndex = handleWithinMemberIndices[handleIndex];
        activeClusterIndex = selectedClusterIndex;
    }

    public static void clearSelection() {
        selectedClusterIndex = -1;
        selectedMemberIndex = -1;
        selectedHandleIndex = -1;
        endHandleDrag();
    }

    /**
     * Replaces the live scene from scene JSON (same schema as desktop Ctrl+O).
     * @return empty string on success, otherwise an error message (scene unchanged)
     */
    public static String loadSceneJson(String json) {
        if (json == null || json.isBlank()) {
            lastError = "Scene JSON is empty";
            return lastError;
        }
        try {
            SceneSnapshot loaded = SceneJsonJs.parse(json);
            sceneSnapshot = loaded;
            activeClusterIndex = 0;
            clearSelection();
            lastError = "";
            return "";
        } catch (SceneJsonException e) {
            lastError = e.getMessage() != null ? e.getMessage() : "Invalid scene JSON";
            return lastError;
        }
    }

    /**
     * Cycle the selected member within the selected cluster ({@code delta} +1 next / -1 previous).
     * @return empty on success, otherwise an error message
     */
    public static String cycleSelectedMember(int delta) {
        if (selectedClusterIndex < 0 || selectedMemberIndex < 0) {
            lastError = "Select a member first to cycle within its cluster.";
            return lastError;
        }
        ClusterSite cluster = sceneSnapshot.clusters().get(selectedClusterIndex);
        int memberCount = cluster.size();
        if (memberCount <= 0) {
            lastError = "Selected cluster has no members";
            return lastError;
        }
        int next = Math.floorMod(selectedMemberIndex + delta, memberCount);
        selectedMemberIndex = next;
        selectedHandleIndex = HandleVisibility.primaryHandleIndex(cluster.members().get(next));
        activeClusterIndex = selectedClusterIndex;
        lastError = "";
        return "";
    }

    /**
     * Select the first member of the next/previous cluster (Shift+n/p).
     */
    public static String cycleSelectedCluster(int delta) {
        int clusterCount = sceneSnapshot.clusters().size();
        if (clusterCount <= 0) {
            lastError = "No clusters";
            return lastError;
        }
        int current = selectedClusterIndex >= 0 ? selectedClusterIndex : 0;
        int next = Math.floorMod(current + delta, clusterCount);
        ClusterSite cluster = sceneSnapshot.clusters().get(next);
        selectedClusterIndex = next;
        selectedMemberIndex = 0;
        selectedHandleIndex = HandleVisibility.primaryHandleIndex(cluster.members().get(0));
        activeClusterIndex = next;
        lastError = "";
        return "";
    }

    /** Adds a member of the current site kind to the selected cluster at the given world point. */
    public static String addMemberAt(double worldX, double worldY) {
        Optional<String> invalid = MetricMemberCompatibility.invalidNewMemberMessage(
                sceneSnapshot.metricKind(),
                sceneSnapshot.siteMemberKind()
        );
        if (invalid.isPresent()) {
            lastError = invalid.get();
            return lastError;
        }
        if (selectedClusterIndex < 0) {
            lastError = "Select a member first to add to its cluster.";
            return lastError;
        }
        ClusterSite cluster = sceneSnapshot.clusters().get(selectedClusterIndex);
        if (cluster.size() >= SceneLimits.MAX_MEMBERS_PER_CLUSTER) {
            lastError = "Selected cluster already has the maximum number of members";
            return lastError;
        }
        int clusterIdx = selectedClusterIndex;
        ClusterMember member = SiteMemberFactory.createDefault(
                sceneSnapshot.siteMemberKind(),
                clusterIdx,
                cluster.size(),
                Vector.xy(worldX, worldY)
        );
        cluster.addMember(member);
        selectedClusterIndex = clusterIdx;
        selectedMemberIndex = cluster.size() - 1;
        selectedHandleIndex = HandleVisibility.primaryHandleIndex(member);
        activeClusterIndex = clusterIdx;
        lastError = "";
        return "";
    }

    /** Removes the selected member. */
    public static String removeMember() {
        if (selectedClusterIndex < 0 || selectedMemberIndex < 0) {
            lastError = "Select a member first to delete it.";
            return lastError;
        }
        ClusterSite cluster = sceneSnapshot.clusters().get(selectedClusterIndex);
        if (cluster.size() <= 1) {
            lastError = "Cannot remove the last member of a cluster";
            return lastError;
        }
        int clusterIdx = selectedClusterIndex;
        int memberIdx = selectedMemberIndex;
        cluster.removeMember(memberIdx);
        selectedClusterIndex = clusterIdx;
        selectedMemberIndex = Math.min(memberIdx, cluster.size() - 1);
        selectedHandleIndex = HandleVisibility.primaryHandleIndex(cluster.members().get(selectedMemberIndex));
        activeClusterIndex = clusterIdx;
        lastError = "";
        return "";
    }

    public static String addCluster() {
        Optional<String> invalid = MetricMemberCompatibility.invalidNewMemberMessage(
                sceneSnapshot.metricKind(),
                sceneSnapshot.siteMemberKind()
        );
        if (invalid.isPresent()) {
            lastError = invalid.get();
            return lastError;
        }
        if (sceneSnapshot.clusters().size() >= SceneLimits.MAX_CLUSTERS) {
            lastError = "Already at the maximum number of clusters";
            return lastError;
        }
        int index = sceneSnapshot.clusters().size();
        sceneSnapshot.clusters().add(defaultCluster(index));
        activeClusterIndex = index;
        selectedClusterIndex = index;
        selectedMemberIndex = 0;
        selectedHandleIndex = HandleVisibility.primaryHandleIndex(sceneSnapshot.clusters().get(index).members().get(0));
        lastError = "";
        return "";
    }

    public static String removeCluster() {
        if (sceneSnapshot.clusters().size() <= 1) {
            lastError = "Cannot remove the last cluster";
            return lastError;
        }
        if (selectedClusterIndex < 0) {
            lastError = "Select a member first to delete its cluster.";
            return lastError;
        }
        int removeIdx = selectedClusterIndex;
        sceneSnapshot.clusters().remove(removeIdx);
        if (activeClusterIndex >= sceneSnapshot.clusters().size()) {
            activeClusterIndex = sceneSnapshot.clusters().size() - 1;
        }
        selectedClusterIndex = -1;
        selectedMemberIndex = -1;
        selectedHandleIndex = -1;
        lastError = "";
        return "";
    }

    private static ClusterSite defaultCluster(int index) {
        double hue = (360 * index * 0.618033988749895) % 360;
        Rgba color = Rgba.hsb(hue, 0.65, 0.95);
        double x = -280 + (index % 5) * 140;
        double y = -200 + (index / 5) * 140;
        Vector center = Vector.xy(x, y);
        ClusterMember first = SiteMemberFactory.createDefault(sceneSnapshot.siteMemberKind(), index, 0, center);
        return new ClusterSite(ClusterNaming.forNewCluster(index), color, List.of(first));
    }

    private static void computeHandles(List<ClusterSite> clusters) {
        int total = 0;
        for (ClusterSite cluster : clusters) {
            for (ClusterMember member : cluster.members()) {
                total += member.handleCount();
            }
        }

        double[] xs = new double[total];
        double[] ys = new double[total];
        int[] clusterIdx = new int[total];
        double[] rs = new double[total];
        double[] gs = new double[total];
        double[] bs = new double[total];
        int[] memberIdx = new int[total];
        int[] withinIdx = new int[total];
        boolean[] visible = new boolean[total];

        int i = 0;
        for (int c = 0; c < clusters.size(); c++) {
            ClusterSite cluster = clusters.get(c);
            Rgba color = cluster.color();
            List<ClusterMember> members = cluster.members();
            for (int m = 0; m < members.size(); m++) {
                ClusterMember member = members.get(m);
                boolean memberSelected = c == selectedClusterIndex && m == selectedMemberIndex;
                for (int h = 0; h < member.handleCount(); h++) {
                    Vector handle = member.getHandle(h);
                    xs[i] = handle.x();
                    ys[i] = handle.y();
                    clusterIdx[i] = c;
                    rs[i] = color.r();
                    gs[i] = color.g();
                    bs[i] = color.b();
                    memberIdx[i] = m;
                    withinIdx[i] = h;
                    visible[i] = HandleVisibility.isVisible(member, h, memberSelected);
                    i++;
                }
            }
        }

        handleXs = xs;
        handleYs = ys;
        handleClusterIndices = clusterIdx;
        handleRs = rs;
        handleGs = gs;
        handleBs = bs;
        handleVisible = visible;
        handleMemberIndices = memberIdx;
        handleWithinMemberIndices = withinIdx;
        handleTotal = total;
    }

    private static void computeOverlays(List<ClusterSite> clusters) {
        int count = 0;
        for (ClusterSite cluster : clusters) {
            count += cluster.size();
        }

        String[] kinds = new String[count];
        int[] clustersIdx = new int[count];
        int[] membersIdx = new int[count];
        double[] ax = new double[count];
        double[] ay = new double[count];
        double[] bx = new double[count];
        double[] by = new double[count];
        double[] radius = new double[count];
        int[] starts = new int[count + 1];
        List<Double> polyX = new ArrayList<>();
        List<Double> polyY = new ArrayList<>();

        int i = 0;
        for (int c = 0; c < clusters.size(); c++) {
            List<ClusterMember> members = clusters.get(c).members();
            for (int m = 0; m < members.size(); m++) {
                ClusterMember member = members.get(m);
                clustersIdx[i] = c;
                membersIdx[i] = m;
                starts[i] = polyX.size();
                if (member instanceof PointMember pm) {
                    kinds[i] = "POINT";
                    ax[i] = pm.getHandle(0).x();
                    ay[i] = pm.getHandle(0).y();
                } else if (member instanceof SegmentMember sm) {
                    kinds[i] = "SEGMENT";
                    ax[i] = sm.a().x();
                    ay[i] = sm.a().y();
                    bx[i] = sm.b().x();
                    by[i] = sm.b().y();
                } else if (member instanceof CircleMember cm) {
                    kinds[i] = "CIRCLE";
                    ax[i] = cm.center().x();
                    ay[i] = cm.center().y();
                    radius[i] = cm.radius();
                } else if (member instanceof EllipseMember em) {
                    kinds[i] = "ELLIPSE";
                    List<Vector> outline = em.boundaryPolyline();
                    if (outline.isEmpty()) {
                        // Degenerate: draw focus segment.
                        ax[i] = em.focusA().x();
                        ay[i] = em.focusA().y();
                        bx[i] = em.focusB().x();
                        by[i] = em.focusB().y();
                        kinds[i] = "SEGMENT";
                    } else {
                        for (Vector p : outline) {
                            polyX.add(p.x());
                            polyY.add(p.y());
                        }
                    }
                } else if (member instanceof LineMember lm) {
                    kinds[i] = "LINE";
                    ax[i] = lm.a().x();
                    ay[i] = lm.a().y();
                    bx[i] = lm.b().x();
                    by[i] = lm.b().y();
                } else {
                    kinds[i] = "POINT";
                    ax[i] = member.getHandle(0).x();
                    ay[i] = member.getHandle(0).y();
                }
                i++;
            }
        }
        starts[count] = polyX.size();

        overlayKinds = kinds;
        overlayClusters = clustersIdx;
        overlayMembers = membersIdx;
        overlayAx = ax;
        overlayAy = ay;
        overlayBx = bx;
        overlayBy = by;
        overlayRadius = radius;
        ellipseStarts = starts;
        ellipseXs = toDoubleArray(polyX);
        ellipseYs = toDoubleArray(polyY);
        overlayCount = count;
    }

    private static double[] toDoubleArray(List<Double> values) {
        double[] out = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }

    /** Mutates the live scene's point handle; the next {@link #computeFrame} reflects the change.
     * When {@code coMove} is true, other handles that were colocated at {@link #beginHandleDrag}
     * move to the same target (polygon vertices stay welded). Shift-detach passes {@code false}. */
    public static void moveHandle(int handleIndex, double worldX, double worldY, boolean coMove) {
        if (handleIndex < 0 || handleIndex >= handleTotal) {
            return;
        }
        int clusterIdx = handleClusterIndices[handleIndex];
        int memberIdx = handleMemberIndices[handleIndex];
        int withinIdx = handleWithinMemberIndices[handleIndex];
        ClusterSite cluster = sceneSnapshot.clusters().get(clusterIdx);
        ClusterMember member = cluster.members().get(memberIdx);
        Vector target = Vector.xy(worldX, worldY);
        cluster.setMember(memberIdx, member.withHandle(withinIdx, target));

        if (coMove && coMoveClusterIndex == clusterIdx) {
            for (int[] co : coMovingHandles) {
                int coMemberIdx = co[0];
                int coWithinIdx = co[1];
                ClusterMember coMember = cluster.members().get(coMemberIdx);
                cluster.setMember(coMemberIdx, coMember.withHandle(coWithinIdx, target));
            }
        }

        selectedClusterIndex = clusterIdx;
        selectedMemberIndex = memberIdx;
        selectedHandleIndex = withinIdx;
        activeClusterIndex = clusterIdx;
    }

    /**
     * Capture other visible handles in the same cluster that share this handle's position,
     * matching desktop {@code findColocatedHandles}.
     */
    public static void beginHandleDrag(int handleIndex) {
        coMovingHandles.clear();
        coMoveClusterIndex = -1;
        if (handleIndex < 0 || handleIndex >= handleTotal) {
            return;
        }
        int clusterIdx = handleClusterIndices[handleIndex];
        int memberIdx = handleMemberIndices[handleIndex];
        int withinIdx = handleWithinMemberIndices[handleIndex];
        ClusterSite cluster = sceneSnapshot.clusters().get(clusterIdx);
        Vector position = cluster.members().get(memberIdx).getHandle(withinIdx);
        coMoveClusterIndex = clusterIdx;

        for (int mi = 0; mi < cluster.size(); mi++) {
            ClusterMember candidate = cluster.members().get(mi);
            boolean memberSelected = mi == memberIdx;
            for (int h = 0; h < candidate.handleCount(); h++) {
                if (mi == memberIdx && h == withinIdx) {
                    continue;
                }
                if (!HandleVisibility.isVisible(candidate, h, memberSelected)) {
                    continue;
                }
                if (candidate.getHandle(h).equals(position)) {
                    coMovingHandles.add(new int[] { mi, h });
                }
            }
        }
    }

    public static void endHandleDrag() {
        coMovingHandles.clear();
        coMoveClusterIndex = -1;
    }

    public static int[] lastArgb() {
        return lastArgb;
    }

    public static int[] lastOwners() {
        return lastOwners;
    }

    public static int[] lastMembers() {
        return lastMembers;
    }

    public static int lastWidth() {
        return lastWidth;
    }

    public static int lastHeight() {
        return lastHeight;
    }

    public static double[] handleXs() {
        return handleXs;
    }

    public static double[] handleYs() {
        return handleYs;
    }

    public static int[] handleClusters() {
        return handleClusterIndices;
    }

    public static int[] handleMembers() {
        return handleMemberIndices;
    }

    public static int[] handleWithin() {
        return handleWithinMemberIndices;
    }

    public static boolean[] handleVisibleFlags() {
        return handleVisible;
    }

    public static double[] handleRs() {
        return handleRs;
    }

    public static double[] handleGs() {
        return handleGs;
    }

    public static double[] handleBs() {
        return handleBs;
    }

    public static int handleTotal() {
        return handleTotal;
    }

    public static int overlayCount() {
        return overlayCount;
    }

    public static String[] overlayKinds() {
        return overlayKinds;
    }

    public static int[] overlayClusters() {
        return overlayClusters;
    }

    public static int[] overlayMembers() {
        return overlayMembers;
    }

    public static double[] overlayAx() {
        return overlayAx;
    }

    public static double[] overlayAy() {
        return overlayAy;
    }

    public static double[] overlayBx() {
        return overlayBx;
    }

    public static double[] overlayBy() {
        return overlayBy;
    }

    public static double[] overlayRadius() {
        return overlayRadius;
    }

    public static double[] ellipseXs() {
        return ellipseXs;
    }

    public static double[] ellipseYs() {
        return ellipseYs;
    }

    public static int[] ellipseStarts() {
        return ellipseStarts;
    }

    private static Transformation pixelToWorld(int width, int height) {
        double sx = (worldMaxX - worldMinX) / width;
        double sy = (worldMinY - worldMaxY) / height; // flip Y for canvas
        double tx = worldMinX;
        double ty = worldMaxY;
        return Transformation.scaling(sx, sy).then(Transformation.translation(Vector.xy(tx, ty)));
    }

    /** Same layout as desktop {@code SceneState.demo()} via {@link DemoScenes}. */
    static SceneSnapshot demoSnapshot() {
        return DemoScenes.defaultSnapshot();
    }

    @JSBody(
            params = {},
            script = ""
                    + "globalThis.cvdCore = {"
                    + "  renderFrame: function (w, h) {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.computeFrame(II)V').invoke(w, h);"
                    + "    var argb = javaMethods.get('cvdexplorer.web.WebClassifyMain.lastArgb()[I').invoke();"
                    + "    var owners = javaMethods.get('cvdexplorer.web.WebClassifyMain.lastOwners()[I').invoke();"
                    + "    var members = javaMethods.get('cvdexplorer.web.WebClassifyMain.lastMembers()[I').invoke();"
                    + "    var width = javaMethods.get('cvdexplorer.web.WebClassifyMain.lastWidth()I').invoke();"
                    + "    var height = javaMethods.get('cvdexplorer.web.WebClassifyMain.lastHeight()I').invoke();"
                    + "    var hx = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleXs()[D').invoke();"
                    + "    var hy = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleYs()[D').invoke();"
                    + "    var hc = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleClusters()[I').invoke();"
                    + "    var hm = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleMembers()[I').invoke();"
                    + "    var hw = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleWithin()[I').invoke();"
                    + "    var hv = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleVisibleFlags()[Z').invoke();"
                    + "    var hr = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleRs()[D').invoke();"
                    + "    var hg = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleGs()[D').invoke();"
                    + "    var hb = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleBs()[D').invoke();"
                    + "    var hn = javaMethods.get('cvdexplorer.web.WebClassifyMain.handleTotal()I').invoke();"
                    + "    var metric = javaMethods.get('cvdexplorer.web.WebClassifyMain.metricKindName()Ljava/lang/String;').invoke();"
                    + "    var order = javaMethods.get('cvdexplorer.web.WebClassifyMain.neighborOrderName()Ljava/lang/String;').invoke();"
                    + "    var k = javaMethods.get('cvdexplorer.web.WebClassifyMain.nearestNeighborK()I').invoke();"
                    + "    var shading = javaMethods.get('cvdexplorer.web.WebClassifyMain.shadingEnabled()Z').invoke();"
                    + "    var err = javaMethods.get('cvdexplorer.web.WebClassifyMain.lastError()Ljava/lang/String;').invoke();"
                    + "    var clusterCount = javaMethods.get('cvdexplorer.web.WebClassifyMain.clusterCount()I').invoke();"
                    + "    var activeCluster = javaMethods.get('cvdexplorer.web.WebClassifyMain.activeClusterIndex()I').invoke();"
                    + "    var activeMembers = javaMethods.get('cvdexplorer.web.WebClassifyMain.activeMemberCount()I').invoke();"
                    + "    var siteKind = javaMethods.get('cvdexplorer.web.WebClassifyMain.siteMemberKindName()Ljava/lang/String;').invoke();"
                    + "    var selCluster = javaMethods.get('cvdexplorer.web.WebClassifyMain.selectedClusterIndex()I').invoke();"
                    + "    var selMember = javaMethods.get('cvdexplorer.web.WebClassifyMain.selectedMemberIndex()I').invoke();"
                    + "    var selHandle = javaMethods.get('cvdexplorer.web.WebClassifyMain.selectedHandleIndex()I').invoke();"
                    + "    var names = [];"
                    + "    for (var i = 0; i < clusterCount; i++) {"
                    + "      names.push(javaMethods.get('cvdexplorer.web.WebClassifyMain.clusterNameAt(I)Ljava/lang/String;').invoke(i));"
                    + "    }"
                    + "    var oc = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayCount()I').invoke();"
                    + "    var ok = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayKinds()[Ljava/lang/String;').invoke();"
                    + "    var ocl = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayClusters()[I').invoke();"
                    + "    var om = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayMembers()[I').invoke();"
                    + "    var oax = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayAx()[D').invoke();"
                    + "    var oay = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayAy()[D').invoke();"
                    + "    var obx = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayBx()[D').invoke();"
                    + "    var oby = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayBy()[D').invoke();"
                    + "    var orad = javaMethods.get('cvdexplorer.web.WebClassifyMain.overlayRadius()[D').invoke();"
                    + "    var ex = javaMethods.get('cvdexplorer.web.WebClassifyMain.ellipseXs()[D').invoke();"
                    + "    var ey = javaMethods.get('cvdexplorer.web.WebClassifyMain.ellipseYs()[D').invoke();"
                    + "    var es = javaMethods.get('cvdexplorer.web.WebClassifyMain.ellipseStarts()[I').invoke();"
                    + "    return {"
                    + "      argb: argb,"
                    + "      owners: owners,"
                    + "      members: members,"
                    + "      width: width,"
                    + "      height: height,"
                    + "      handles: { x: hx, y: hy, cluster: hc, member: hm, within: hw, visible: hv, r: hr, g: hg, b: hb, n: hn },"
                    + "      overlays: {"
                    + "        n: oc, kind: ok, cluster: ocl, member: om,"
                    + "        ax: oax, ay: oay, bx: obx, by: oby, radius: orad,"
                    + "        ellipseX: ex, ellipseY: ey, ellipseStarts: es"
                    + "      },"
                    + "      scene: {"
                    + "        metricKind: metric, neighborOrder: order, nearestNeighborK: k, shading: shading, lastError: err,"
                    + "        clusterCount: clusterCount, activeClusterIndex: activeCluster, activeMemberCount: activeMembers,"
                    + "        siteMemberKind: siteKind, selectedClusterIndex: selCluster, selectedMemberIndex: selMember,"
                    + "        selectedHandleIndex: selHandle, clusterNames: names"
                    + "      }"
                    + "    };"
                    + "  },"
                    + "  moveHandle: function (index, worldX, worldY, coMove) {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.moveHandle(IDDZ)V').invoke(index, worldX, worldY, !!coMove);"
                    + "  },"
                    + "  beginHandleDrag: function (index) {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.beginHandleDrag(I)V').invoke(index);"
                    + "  },"
                    + "  endHandleDrag: function () {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.endHandleDrag()V').invoke();"
                    + "  },"
                    + "  setMetricKind: function (name) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.setMetricKindName(Ljava/lang/String;)Ljava/lang/String;').invoke(name);"
                    + "  },"
                    + "  setNeighborOrder: function (name) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.setNeighborOrderName(Ljava/lang/String;)Ljava/lang/String;').invoke(name);"
                    + "  },"
                    + "  setNearestNeighborK: function (k) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.setNearestNeighborK(I)Ljava/lang/String;').invoke(k);"
                    + "  },"
                    + "  setShadingEnabled: function (enabled) {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.setShadingEnabled(Z)V').invoke(enabled);"
                    + "  },"
                    + "  setWorldView: function (minX, maxX, minY, maxY) {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.setWorldView(DDDD)V').invoke(minX, maxX, minY, maxY);"
                    + "  },"
                    + "  setActiveClusterIndex: function (index) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.setActiveClusterIndex(I)Ljava/lang/String;').invoke(index);"
                    + "  },"
                    + "  setSiteMemberKind: function (name) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.setSiteMemberKindName(Ljava/lang/String;)Ljava/lang/String;').invoke(name);"
                    + "  },"
                    + "  selectHandle: function (index) {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.selectHandle(I)V').invoke(index);"
                    + "  },"
                    + "  cycleSelectedMember: function (delta) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.cycleSelectedMember(I)Ljava/lang/String;').invoke(delta);"
                    + "  },"
                    + "  cycleSelectedCluster: function (delta) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.cycleSelectedCluster(I)Ljava/lang/String;').invoke(delta);"
                    + "  },"
                    + "  clearSelection: function () {"
                    + "    javaMethods.get('cvdexplorer.web.WebClassifyMain.clearSelection()V').invoke();"
                    + "  },"
                    + "  addMemberAt: function (worldX, worldY) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.addMemberAt(DD)Ljava/lang/String;').invoke(worldX, worldY);"
                    + "  },"
                    + "  removeMember: function () {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.removeMember()Ljava/lang/String;').invoke();"
                    + "  },"
                    + "  addCluster: function () {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.addCluster()Ljava/lang/String;').invoke();"
                    + "  },"
                    + "  removeCluster: function () {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.removeCluster()Ljava/lang/String;').invoke();"
                    + "  },"
                    + "  loadSceneJson: function (json) {"
                    + "    return javaMethods.get('cvdexplorer.web.WebClassifyMain.loadSceneJson(Ljava/lang/String;)Ljava/lang/String;').invoke(json);"
                    + "  }"
                    + "};"
    )
    private static native void installApi();
}
