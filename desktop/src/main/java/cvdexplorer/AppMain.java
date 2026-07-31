package cvdexplorer;

import cvdexplorer.core.DiagramRasterizer;
import cvdexplorer.core.ScenePreparation;
import cvdexplorer.core.ScenePreparation.PreparedScene;
import cvdexplorer.desktop.DrawingFxGeometry;
import cvdexplorer.desktop.FxColors;
import cvdexplorer.geometry.Box;
import cvdexplorer.geometry.Transformation;
import cvdexplorer.geometry.Vector;
import cvdexplorer.io.SceneFileIo;
import cvdexplorer.io.SceneGallery;
import cvdexplorer.io.SceneJsonException;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.GalleryExample;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SceneSnapshot;
import cvdexplorer.model.SiteMemberFactory;
import cvdexplorer.model.SiteMemberKind;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.MetricMemberCompatibility;
import cvdexplorer.render.ClusterColorizer;
import cvdexplorer.render.HelpOverlay;
import cvdexplorer.render.MemberOverlayRenderer;
import cvdexplorer.render.RasterDiagramRenderer;
import cvdexplorer.render.SkeletonOverlayRenderer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xyz.marsavic.drawingfx.application.DrawingApplication;
import xyz.marsavic.drawingfx.application.Options;
import xyz.marsavic.drawingfx.drawing.Drawing;
import xyz.marsavic.drawingfx.drawing.DrawingUtils;
import xyz.marsavic.drawingfx.drawing.View;
import xyz.marsavic.drawingfx.gadgets.annotations.RecurseGadgets;
import xyz.marsavic.drawingfx.utils.camera.CameraSimple;
import xyz.marsavic.functions.F_R_R;
import xyz.marsavic.input.InputEvent;
import xyz.marsavic.input.InputState;
import xyz.marsavic.input.KeyCode;

public class AppMain implements Drawing {
    // Temporary: drawing-fx Options/View use xyz.marsavic.geometry types bundled in drawing-fx.
    // Switch to cvdexplorer.geometry when the desktop UI no longer depends on drawing-fx.
    public static final xyz.marsavic.geometry.Vector sizeInitial = xyz.marsavic.geometry.Vector.xy(960, 960);
    public static final xyz.marsavic.geometry.Vector gridCellD = xyz.marsavic.geometry.Vector.xy(16, 16);

    @RecurseGadgets
    final SceneState state = SceneState.demo();

    private final CameraSimple camera = new CameraSimple(F_R_R.cutoff01(t -> F_R_R.power(t, 8)));
    private final RasterDiagramRenderer rasterDiagramRenderer = new RasterDiagramRenderer();
    private final SkeletonOverlayRenderer skeletonOverlayRenderer = new SkeletonOverlayRenderer();

    private final double pointRadius = 6.0;
    private final double mouseReach = 10.0;
    private final double draggingMinDistance = 1.0;
    /** Linear resolution used while dragging (~10× fewer classifications). */
    private static final double DRAG_RASTER_SCALE = 0.32;

    private double pixelWidth = 1.0;
    private boolean dragging = false;
    private Vector draggingStartPoint;
    private int selectedClusterIndex = -1;
    private int selectedMemberIndex = -1;
    private int selectedHandleIndex = -1;

    private GalleryExample prevGalleryExample = state.galleryExample;
    private MetricKind lastValidMetricKind = state.metricKind;
    private SiteMemberKind lastValidSiteMemberKind = state.siteMemberKind;
    private MetricKind lastRejectedMetricKind = null;
    private SiteMemberKind lastRejectedSiteMemberKind = null;
    private Integer lastRejectedClusterCount = null;
    private Integer lastRejectedMemberCount = null;
    private int lastRejectedMemberCountClusterIndex = -1;
    private Integer lastRejectedMemberCountWithoutSelection = null;
    /**
     * Cluster-count gadget requested a shrink we could not fully apply. Snap back silently
     * until the gadget value moves strictly above this request (avoids draw-loop fights).
     */
    private Integer stuckClusterShrinkRequest = null;
    /**
     * Member-count gadget requested a shrink we could not fully apply (same latch idea as clusters).
     */
    private Integer stuckMemberShrinkRequest = null;
    /** Member-count gadget value frozen while nothing is selected. */
    private int memberCountWhileNoSelection = state.targetPointCountForActiveCluster;
    private Alert activeErrorAlert = null;
    private String activeErrorDialogKey = null;
    /**
     * Key-triggered alerts must wait until the triggering keys are released. Otherwise the Alert
     * steals focus before KEY_RELEASED reaches drawing-fx, InputState keeps the key "pressed",
     * and the next KEY_PRESSED is dropped (Set.add returns false) — so d / Shift+d need two presses.
     */
    private String pendingErrorHeader = null;
    private String pendingErrorMessage = null;

    private List<Selection> coMovingHandles = List.of();
    private Vector snapTargetPosition = null;

    // After FileChooser, Control can stay pressed in InputState; ignore it for camera until released.
    private boolean ignoreControlModifierForCamera;

    @Override
    public void draw(View view) {
        normalizeMetricSelection();
        normalizeSiteMemberKindSelection();
        normalizeClusterCountGadget();
        normalizeSelectedClusterMemberCountGadget();
        state.clampNearestNeighborK();
        applyGalleryExampleSelection();

        normalizeSelection();

        view.addTransformation(camera.getTransformation());
        pixelWidth = 1.0 / view.transformation().getScale();

        DrawingUtils.clear(view, FxColors.toFx(state.backgroundColor()));

        PreparedScene preparedScene = ScenePreparation.prepare(
                state.clusters(),
                state.metricKind,
                state.neighborOrder,
                state.nearestNeighborK
        );
        if (state.showDiagram || state.showSkeleton) {
            drawDiagram(view, preparedScene);
        }
        if (state.showMembers) {
            drawMembers(view, preparedScene);
        }
        if (state.showHelp) {
            HelpOverlay.draw(view);
        }
    }

    private void drawDiagram(View view, PreparedScene preparedScene) {
        xyz.marsavic.geometry.Transformation transform = view.transformation();
        xyz.marsavic.geometry.Box imageBoxFx = view.nativeBox().positive();
        Box imageBox = DrawingFxGeometry.fromDrawingFx(imageBoxFx);
        Transformation tFromPixels = DrawingFxGeometry.fromDrawingFx(transform).inverse();
        ClusterColorizer colorizer = new ClusterColorizer(
                preparedScene.clusters(),
                state.backgroundColor(),
                state.showShading
        );
        double resolutionScale = (dragging && state.fastDrawPreview) ? DRAG_RASTER_SCALE : 1.0;
        RasterDiagramRenderer.RenderResult result = rasterDiagramRenderer.render(
                tFromPixels,
                imageBox,
                p -> classify(p, preparedScene),
                state.showDiagram ? colorizer::color : null,
                resolutionScale
        );

        if (result == null) {
            return;
        }

        view.setTransformation(xyz.marsavic.geometry.Transformation.IDENTITY);
        Image diagram = result.image();
        if (diagram != null) {
            view.drawImage(imageBoxFx, diagram);
        }
        boolean skipHeavyOverlays = dragging && state.fastDrawPreview;
        if (!skipHeavyOverlays && state.showSkeleton) {
            skeletonOverlayRenderer.draw(
                    view,
                    result.ownershipGrid(),
                    // Reuse the active metric so the contour position tracks the true cluster boundary.
                    (pixelPoint, clusterIndex) -> preparedScene.metric().score(
                            tFromPixels.applyTo(DrawingFxGeometry.fromDrawingFx(pixelPoint)),
                            preparedScene.clusters().get(clusterIndex)
                    )
            );
        }
        if (!skipHeavyOverlays && state.showRegionSubdivision) {
            skeletonOverlayRenderer.drawSubdivisions(
                    view,
                    result.ownershipGrid(),
                    (pixelPoint, clusterIndex, memberIndex) -> {
                        Vector point = tFromPixels.applyTo(DrawingFxGeometry.fromDrawingFx(pixelPoint));
                        return preparedScene.clusters().get(clusterIndex).members().get(memberIndex).distanceTo(point);
                    }
            );
        }
        view.setTransformation(transform);
    }

    private DiagramRasterizer.Classification classify(Vector point, PreparedScene preparedScene) {
        var ownership = preparedScene.ownershipSelector().selectOwner(
                point,
                preparedScene.clusters(),
                preparedScene.metric()
        );
        return new DiagramRasterizer.Classification(ownership.clusterIndex(), ownership.score(), ownership.memberIndex());
    }

    private void drawMembers(View view, PreparedScene preparedScene) {
        double handleR = pointRadius * pixelWidth;
        for (int clusterIndex = 0; clusterIndex < preparedScene.clusters().size(); clusterIndex++) {
            ClusterSite cluster = preparedScene.clusters().get(clusterIndex);
            boolean activeCluster = hasSelection() && clusterIndex == selectedClusterIndex;
            for (int memberIndex = 0; memberIndex < cluster.size(); memberIndex++) {
                ClusterMember member = cluster.members().get(memberIndex);
                boolean memberSelected = clusterIndex == selectedClusterIndex && memberIndex == selectedMemberIndex;
                int handleSel = memberSelected ? selectedHandleIndex : -1;
                MemberOverlayRenderer.drawMember(
                        view,
                        member,
                        FxColors.toFx(cluster.color()),
                        activeCluster,
                        memberSelected,
                        handleSel,
                        handleR,
                        pixelWidth
                );
            }
        }
        if (snapTargetPosition != null) {
            MemberOverlayRenderer.drawSnapIndicator(view, snapTargetPosition, handleR, pixelWidth);
        }
    }

    private void updateSelectionStart(InputEvent event, Vector pointerWorld) {
        if (!dragging && event.isMouseButtonPress(1)) {
            Selection selection = nearestHandle(pointerWorld, mouseReach * pixelWidth);
            if (selection == null) {
                clearSelection();
                coMovingHandles = List.of();
            } else {
                selectedClusterIndex = selection.clusterIndex();
                selectedMemberIndex = selection.memberIndex();
                selectedHandleIndex = selection.handleIndex();
                state.targetPointCountForActiveCluster =
                        state.clusters().get(selectedClusterIndex).size();
                stuckMemberShrinkRequest = null;
                draggingStartPoint = pointerWorld;
                coMovingHandles = findColocatedHandles(
                        selectedClusterIndex, selectedMemberIndex, selectedHandleIndex);
            }
        }
    }

    private void updateDraggingState(InputState inputState, Vector pointerWorld) {
        if (inputState.mouseButtonPressed(1)) {
            if (draggingStartPoint != null && pointerWorld.distanceTo(draggingStartPoint) > draggingMinDistance * pixelWidth) {
                dragging = true;
            }
        } else {
            dragging = false;
            draggingStartPoint = null;
            coMovingHandles = List.of();
            snapTargetPosition = null;
        }
    }

    private void applyPointerEdits(Vector position, InputState inputState) {
        if (dragging && hasSelection()) {
            Vector target = position;
            if (state.snapToHandles) {
                target = snapToNearestHandle(position, mouseReach * pixelWidth,
                        selectedClusterIndex, selectedMemberIndex, selectedHandleIndex);
                snapTargetPosition = target.equals(position) ? null : target;
            } else {
                snapTargetPosition = null;
            }

            ClusterSite cluster = state.clusters().get(selectedClusterIndex);
            ClusterMember member = cluster.members().get(selectedMemberIndex);
            cluster.setMember(selectedMemberIndex, member.withHandle(selectedHandleIndex, target));

            boolean shiftHeld = inputState.keyPressed(KeyCode.SHIFT);
            if (!shiftHeld) {
                for (Selection co : coMovingHandles) {
                    ClusterSite coCluster = state.clusters().get(co.clusterIndex());
                    ClusterMember coMember = coCluster.members().get(co.memberIndex());
                    coCluster.setMember(co.memberIndex(), coMember.withHandle(co.handleIndex(), target));
                }
            }
        } else {
            snapTargetPosition = null;
        }
    }

    private void applyKeys(InputEvent event, Vector pointerWorld, InputState inputState) {
        if (event.isKeyPress(KeyCode.H)) state.showHelp ^= true;
        if (event.isKeyPress(KeyCode.M)) state.showMembers ^= true;
        if (event.isKeyPress(KeyCode.C)) state.showDiagram ^= true;
        if (event.isKeyPress(KeyCode.V)) state.showRegionSubdivision ^= true;
        if (event.isKeyPress(KeyCode.K)) state.showSkeleton ^= true;
        if (event.isKeyPress(KeyCode.G)) state.snapToGrid ^= true;
        if (event.isKeyPress(KeyCode.F)) state.snapToHandles ^= true;
        if (event.isKeyPress(KeyCode.S)) state.showShading ^= true;

        if (event.isKeyPress(KeyCode.N) || event.isKeyPress(KeyCode.P)) {
            int delta = event.isKeyPress(KeyCode.N) ? 1 : -1;
            if (inputState.keyPressed(KeyCode.SHIFT)) {
                selectFirstMemberInAdjacentCluster(delta);
            } else {
                cycleSelectedMember(delta);
            }
        }

        if (event.isKeyPress(KeyCode.A)) {
            if (inputState.keyPressed(KeyCode.SHIFT)) {
                if (state.numberOfClusters < SceneState.MAX_CLUSTERS) {
                    state.numberOfClusters++;
                    String compatibilityError = state.ensureClusterCountMatchesGadget().orElse(null);
                    if (compatibilityError != null) {
                        showCompatibilityError(compatibilityError);
                    } else {
                        int newIdx = state.clusterCount() - 1;
                        selectMember(newIdx, 0);
                    }
                }
                return;
            }
            if (!hasSelection()) {
                showSelectionRequired("Select a member first to add to its cluster.");
                return;
            }
            String invalidAddMessage = MetricMemberCompatibility.invalidNewMemberMessage(state.metricKind, state.siteMemberKind)
                    .orElse(null);
            if (invalidAddMessage != null) {
                state.siteMemberKind = lastValidSiteMemberKind;
                showCompatibilityError(invalidAddMessage);
                return;
            }
            int clusterIdx = selectedClusterIndex;
            ClusterSite cluster = state.clusters().get(clusterIdx);
            cluster.addMember(SiteMemberFactory.createDefault(state.siteMemberKind, clusterIdx, cluster.size(), pointerWorld));
            state.targetPointCountForActiveCluster = cluster.size();
            selectMember(clusterIdx, cluster.size() - 1);
        }

        if (event.isKeyPress(KeyCode.D)) {
            if (inputState.keyPressed(KeyCode.SHIFT)) {
                if (!hasSelection()) {
                    showSelectionRequired("Select a member first to delete its cluster.");
                    return;
                }
                int removeIdx = selectedClusterIndex;
                if (state.removeClusterAt(removeIdx)) {
                    clearSelection();
                    state.resetMemberCountGadgetSync();
                }
                return;
            }
            if (!hasSelection()) {
                showSelectionRequired("Select a member first to delete it.");
                return;
            }
            ClusterSite cluster = state.clusters().get(selectedClusterIndex);
            if (cluster.size() > 1) {
                cluster.removeMember(selectedMemberIndex);
                int nextMember = Math.min(selectedMemberIndex, cluster.size() - 1);
                state.targetPointCountForActiveCluster = cluster.size();
                selectMember(selectedClusterIndex, nextMember);
            }
        }

        if (event.isKeyPress(KeyCode.R)) {
            state.copyFrom(SceneState.demo());
            state.galleryExample = GalleryExample.CURRENT;
            lastValidMetricKind = state.metricKind;
            lastValidSiteMemberKind = state.siteMemberKind;
            clearRejectedGadgetAttempts();
            prevGalleryExample = state.galleryExample;
            clearSelection();
            state.resetMemberCountGadgetSync();
        }
    }

    @Override
    public void receiveEvent(
            View view,
            InputEvent event,
            InputState inputState,
            xyz.marsavic.geometry.Vector pointerWorldFx,
            xyz.marsavic.geometry.Vector pointerViewBase
    ) {
        if (ignoreControlModifierForCamera
                && (!inputState.keyPressed(KeyCode.CONTROL) || event.isKeyRelease(KeyCode.CONTROL))) {
            ignoreControlModifierForCamera = false;
        }

        flushPendingErrorDialog(inputState);

        if (event.isKeyPress(KeyCode.S) && inputState.keyPressed(KeyCode.CONTROL)) {
            saveSceneToFile();
            return;
        }
        if (event.isKeyPress(KeyCode.O) && inputState.keyPressed(KeyCode.CONTROL)) {
            loadSceneFromFile();
            return;
        }
        boolean controlForCamera = inputState.keyPressed(KeyCode.CONTROL) && !ignoreControlModifierForCamera;
        if (controlForCamera && !event.isKey()) {
            camera.receiveEvent(view, event, inputState, pointerWorldFx, pointerViewBase);
            return;
        }

        Vector pointerWorld = DrawingFxGeometry.fromDrawingFx(pointerWorldFx);
        Vector pointer = state.snapToGrid
                ? pointerWorld.round(DrawingFxGeometry.fromDrawingFx(gridCellD))
                : pointerWorld;

        updateSelectionStart(event, pointerWorld);
        updateDraggingState(inputState, pointerWorld);
        applyPointerEdits(pointer, inputState);
        applyKeys(event, pointer, inputState);
    }

    private Selection nearestHandle(Vector pointerWorld, double radiusLimit) {
        Selection best = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (int clusterIndex = 0; clusterIndex < state.clusterCount(); clusterIndex++) {
            ClusterSite cluster = state.clusters().get(clusterIndex);
            for (int memberIndex = 0; memberIndex < cluster.size(); memberIndex++) {
                ClusterMember member = cluster.members().get(memberIndex);
                boolean memberSelected = clusterIndex == selectedClusterIndex && memberIndex == selectedMemberIndex;
                for (int h = 0; h < member.handleCount(); h++) {
                    if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                        continue;
                    }
                    double distance = member.getHandle(h).distanceTo(pointerWorld);
                    if (distance < bestDistance && distance < radiusLimit) {
                        bestDistance = distance;
                        best = new Selection(clusterIndex, memberIndex, h);
                    }
                }
            }
        }

        return best;
    }

    private Vector snapToNearestHandle(Vector position, double snapRadius,
            int excludeCluster, int excludeMember, int excludeHandle) {
        double bestDistance = Double.POSITIVE_INFINITY;
        Vector bestPosition = null;

        for (int ci = 0; ci < state.clusterCount(); ci++) {
            ClusterSite cluster = state.clusters().get(ci);
            for (int mi = 0; mi < cluster.size(); mi++) {
                ClusterMember member = cluster.members().get(mi);
                boolean memberSelected = ci == selectedClusterIndex && mi == selectedMemberIndex;
                for (int h = 0; h < member.handleCount(); h++) {
                    if (ci == excludeCluster && mi == excludeMember && h == excludeHandle) {
                        continue;
                    }
                    if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                        continue;
                    }
                    Vector handlePos = member.getHandle(h);
                    double distance = handlePos.distanceTo(position);
                    if (distance < bestDistance && distance < snapRadius) {
                        bestDistance = distance;
                        bestPosition = handlePos;
                    }
                }
            }
        }

        return bestPosition != null ? bestPosition : position;
    }

    private List<Selection> findColocatedHandles(int clusterIndex, int memberIndex, int handleIndex) {
        ClusterSite cluster = state.clusters().get(clusterIndex);
        Vector position = cluster.members().get(memberIndex).getHandle(handleIndex);
        List<Selection> result = new ArrayList<>();

        for (int mi = 0; mi < cluster.size(); mi++) {
            ClusterMember member = cluster.members().get(mi);
            boolean memberSelected = clusterIndex == selectedClusterIndex && mi == selectedMemberIndex;
            for (int h = 0; h < member.handleCount(); h++) {
                if (mi == memberIndex && h == handleIndex) {
                    continue;
                }
                if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                    continue;
                }
                if (member.getHandle(h).equals(position)) {
                    result.add(new Selection(clusterIndex, mi, h));
                }
            }
        }

        return result;
    }

    private void selectMember(int clusterIndex, int memberIndex) {
        ClusterSite cluster = state.clusters().get(clusterIndex);
        selectedClusterIndex = clusterIndex;
        selectedMemberIndex = memberIndex;
        selectedHandleIndex = HandleVisibility.primaryHandleIndex(cluster.members().get(memberIndex));
        state.targetPointCountForActiveCluster = cluster.size();
        stuckMemberShrinkRequest = null;
    }

    /** Shift+n/p: select the first member of the next/previous cluster. */
    private void selectFirstMemberInAdjacentCluster(int delta) {
        int clusterCount = state.clusterCount();
        if (clusterCount <= 0) {
            return;
        }
        int current = hasSelection() ? selectedClusterIndex : 0;
        int next = Math.floorMod(current + delta, clusterCount);
        selectMember(next, 0);
    }

    private void cycleSelectedMember(int delta) {
        if (!hasSelection()) {
            showSelectionRequired("Select a member first to cycle within its cluster.");
            return;
        }
        ClusterSite cluster = state.clusters().get(selectedClusterIndex);
        int memberCount = cluster.size();
        if (memberCount <= 0) {
            return;
        }
        int next = Math.floorMod(selectedMemberIndex + delta, memberCount);
        selectMember(selectedClusterIndex, next);
    }

    private boolean hasSelection() {
        return selectedClusterIndex >= 0 && selectedMemberIndex >= 0 && selectedHandleIndex >= 0;
    }

    private void clearSelection() {
        selectedClusterIndex = -1;
        selectedMemberIndex = -1;
        selectedHandleIndex = -1;
        dragging = false;
        draggingStartPoint = null;
        coMovingHandles = List.of();
        snapTargetPosition = null;
        memberCountWhileNoSelection = state.targetPointCountForActiveCluster;
        stuckMemberShrinkRequest = null;
    }

    private void normalizeSelection() {
        if (state.clusterCount() == 0) {
            clearSelection();
            return;
        }

        if (!hasSelection()) {
            return;
        }

        if (selectedClusterIndex >= state.clusterCount()) {
            clearSelection();
            return;
        }

        ClusterSite cluster = state.clusters().get(selectedClusterIndex);
        if (selectedMemberIndex >= cluster.size()) {
            clearSelection();
            return;
        }

        ClusterMember member = cluster.members().get(selectedMemberIndex);
        if (selectedHandleIndex >= member.handleCount()) {
            selectedHandleIndex = member.handleCount() - 1;
        }
    }

    private record Selection(int clusterIndex, int memberIndex, int handleIndex) {
    }

    /** FileChooser needs a window; any showing JavaFX window is enough. */
    private static Window firstShowingWindow() {
        for (Window w : Window.getWindows()) {
            if (w.isShowing()) {
                return w;
            }
        }
        return null;
    }

    private void afterNativeFileDialog() {
        ignoreControlModifierForCamera = true;
        Window w = firstShowingWindow();
        if (w != null) {
            Platform.runLater(w::requestFocus);
        }
    }

    private void normalizeMetricSelection() {
        MetricKind selectedMetricKind = state.metricKind;
        if (selectedMetricKind == lastValidMetricKind) {
            lastRejectedMetricKind = null;
            return;
        }
        if (selectedMetricKind == lastRejectedMetricKind) {
            state.metricKind = lastValidMetricKind;
            return;
        }

        String invalidMetricMessage = MetricMemberCompatibility.invalidMetricMessage(selectedMetricKind, state.clusters())
                .orElse(null);
        if (invalidMetricMessage != null) {
            lastRejectedMetricKind = selectedMetricKind;
            state.metricKind = lastValidMetricKind;
            showCompatibilityError(invalidMetricMessage);
            return;
        }
        lastRejectedMetricKind = null;
        lastValidMetricKind = selectedMetricKind;
    }

    private void normalizeSiteMemberKindSelection() {
        SiteMemberKind selectedSiteMemberKind = state.siteMemberKind;
        if (selectedSiteMemberKind == lastValidSiteMemberKind) {
            lastRejectedSiteMemberKind = null;
            return;
        }
        if (selectedSiteMemberKind == lastRejectedSiteMemberKind) {
            state.siteMemberKind = lastValidSiteMemberKind;
            return;
        }

        String invalidNewMemberMessage = MetricMemberCompatibility.invalidNewMemberMessage(state.metricKind, selectedSiteMemberKind)
                .orElse(null);
        if (invalidNewMemberMessage != null) {
            lastRejectedSiteMemberKind = selectedSiteMemberKind;
            state.siteMemberKind = lastValidSiteMemberKind;
            showCompatibilityError(invalidNewMemberMessage);
            return;
        }
        lastRejectedSiteMemberKind = null;
        lastValidSiteMemberKind = selectedSiteMemberKind;
    }

    private void showCompatibilityError(String message) {
        showErrorDialog("Unsupported metric/member combination", message);
    }

    private void showSelectionRequired(String message) {
        // Defer until keys are up so InputState sees the release before the Alert steals focus.
        queueErrorDialogAfterKeysUp("Selection required", message);
    }

    private void queueErrorDialogAfterKeysUp(String header, String message) {
        pendingErrorHeader = header;
        pendingErrorMessage = message;
    }

    private void flushPendingErrorDialog(InputState inputState) {
        if (pendingErrorMessage == null) {
            return;
        }
        if (inputState.keyPressed(KeyCode.A)
                || inputState.keyPressed(KeyCode.D)
                || inputState.keyPressed(KeyCode.N)
                || inputState.keyPressed(KeyCode.P)
                || inputState.keyPressed(KeyCode.SHIFT)) {
            return;
        }
        String header = pendingErrorHeader;
        String message = pendingErrorMessage;
        pendingErrorHeader = null;
        pendingErrorMessage = null;
        showErrorDialog(header, message);
    }

    private void normalizeClusterCountGadget() {
        int requestedClusterCount = state.numberOfClusters;
        String compatibilityError = state.ensureClusterCountMatchesGadget().orElse(null);
        if (compatibilityError != null) {
            if (!Integer.valueOf(requestedClusterCount).equals(lastRejectedClusterCount)) {
                lastRejectedClusterCount = requestedClusterCount;
                showCompatibilityError(compatibilityError);
            }
            return;
        }
        lastRejectedClusterCount = null;

        if (stuckClusterShrinkRequest != null && state.numberOfClusters > stuckClusterShrinkRequest) {
            stuckClusterShrinkRequest = null;
        }

        if (state.clusterCount() <= state.numberOfClusters) {
            return;
        }

        // Unfinished lower gadget value: snap only — never dialog from draw.
        if (stuckClusterShrinkRequest != null && state.numberOfClusters <= stuckClusterShrinkRequest) {
            state.numberOfClusters = state.clusterCount();
            return;
        }

        if (!hasSelection()) {
            stuckClusterShrinkRequest = requestedClusterCount;
            state.numberOfClusters = state.clusterCount();
            return;
        }

        state.removeClusterAt(selectedClusterIndex);
        clearSelection();
        state.resetMemberCountGadgetSync();
        if (requestedClusterCount < state.clusterCount()) {
            stuckClusterShrinkRequest = requestedClusterCount;
        } else {
            stuckClusterShrinkRequest = null;
        }
        state.numberOfClusters = state.clusterCount();
    }

    private void normalizeSelectedClusterMemberCountGadget() {
        int requestedMemberCount = state.targetPointCountForActiveCluster;
        if (!hasSelection()) {
            state.targetPointCountForActiveCluster = memberCountWhileNoSelection;
            return;
        }

        if (stuckMemberShrinkRequest != null && requestedMemberCount > stuckMemberShrinkRequest) {
            stuckMemberShrinkRequest = null;
        }

        int clusterIdx = selectedClusterIndex;
        ClusterSite cluster = state.clusters().get(clusterIdx);
        String compatibilityError = state.ensureMemberCountForCluster(clusterIdx).orElse(null);
        if (compatibilityError != null) {
            boolean repeatedRejectedRequest =
                    clusterIdx == lastRejectedMemberCountClusterIndex
                            && Integer.valueOf(requestedMemberCount).equals(lastRejectedMemberCount);
            if (!repeatedRejectedRequest) {
                lastRejectedMemberCountClusterIndex = clusterIdx;
                lastRejectedMemberCount = requestedMemberCount;
                showCompatibilityError(compatibilityError);
            }
            return;
        }
        lastRejectedMemberCount = null;
        lastRejectedMemberCountClusterIndex = -1;

        requestedMemberCount = state.targetPointCountForActiveCluster;
        int liveCount = cluster.size();

        if (liveCount <= requestedMemberCount) {
            memberCountWhileNoSelection = requestedMemberCount;
            return;
        }

        // Unfinished lower gadget value: snap only — never dialog from draw.
        if (stuckMemberShrinkRequest != null && requestedMemberCount <= stuckMemberShrinkRequest) {
            state.targetPointCountForActiveCluster = liveCount;
            memberCountWhileNoSelection = liveCount;
            return;
        }

        // Cannot remove the last member of a cluster.
        if (liveCount <= 1) {
            stuckMemberShrinkRequest = requestedMemberCount;
            state.targetPointCountForActiveCluster = liveCount;
            memberCountWhileNoSelection = liveCount;
            return;
        }

        int removeIdx = selectedMemberIndex;
        cluster.removeMember(removeIdx);
        int nextMember = Math.min(removeIdx, cluster.size() - 1);
        selectMember(clusterIdx, nextMember);

        if (requestedMemberCount < cluster.size()) {
            stuckMemberShrinkRequest = requestedMemberCount;
        } else {
            stuckMemberShrinkRequest = null;
        }
        state.targetPointCountForActiveCluster = cluster.size();
        memberCountWhileNoSelection = cluster.size();
    }

    private void clearRejectedGadgetAttempts() {
        lastRejectedMetricKind = null;
        lastRejectedSiteMemberKind = null;
        lastRejectedClusterCount = null;
        lastRejectedMemberCount = null;
        lastRejectedMemberCountClusterIndex = -1;
        lastRejectedMemberCountWithoutSelection = null;
        stuckClusterShrinkRequest = null;
        stuckMemberShrinkRequest = null;
    }

    private void showErrorDialog(String header, String message) {
        Platform.runLater(() -> {
            String dialogKey = header + "\n" + message;
            if (activeErrorAlert != null && activeErrorAlert.isShowing()) {
                if (dialogKey.equals(activeErrorDialogKey)) {
                    // Already showing this message; do not re-focus every draw frame.
                    return;
                }
                activeErrorAlert.setHeaderText(header);
                activeErrorAlert.setContentText(message);
                activeErrorDialogKey = dialogKey;
                return;
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cluster Voronoi Diagrams Explorer");
            alert.setHeaderText(header);
            alert.setContentText(message);
            Window owner = firstShowingWindow();
            if (owner != null) {
                alert.initOwner(owner);
            }
            alert.setOnHidden(event -> {
                if (activeErrorAlert == alert) {
                    activeErrorAlert = null;
                    activeErrorDialogKey = null;
                }
                // Prefer the drawing canvas (where drawing-fx listens); window focus alone is not enough.
                Platform.runLater(this::restoreDrawingCanvasFocus);
            });
            activeErrorAlert = alert;
            activeErrorDialogKey = dialogKey;
            alert.show();
        });
    }

    private void focusActiveErrorAlert() {
        if (activeErrorAlert == null) {
            return;
        }
        Window window = activeErrorAlert.getDialogPane().getScene() == null
                ? null
                : activeErrorAlert.getDialogPane().getScene().getWindow();
        if (window != null) {
            window.requestFocus();
        }
    }

    /** drawing-fx listens on DrawingCanvas; restore focus there after modal dialogs. */
    private void restoreDrawingCanvasFocus() {
        for (Window w : Window.getWindows()) {
            if (!w.isShowing() || w.getScene() == null) {
                continue;
            }
            Scene scene = w.getScene();
            for (Node node : scene.getRoot().lookupAll("*")) {
                if (node.getClass().getName().endsWith("DrawingCanvas")) {
                    w.requestFocus();
                    node.requestFocus();
                    return;
                }
            }
        }
        Window fallback = firstShowingWindow();
        if (fallback != null) {
            fallback.requestFocus();
        }
    }

    private void applyGalleryExampleSelection() {
        GalleryExample selected = state.galleryExample;
        if (selected == prevGalleryExample) {
            return;
        }
        prevGalleryExample = selected;
        if (selected == null || selected.fileName() == null) {
            return;
        }
        try {
            SceneSnapshot snapshot = SceneGallery.loadScene(selected.fileName());
            state.applyLoadedScene(
                    snapshot.metricKind(),
                    snapshot.neighborOrder(),
                    snapshot.siteMemberKind(),
                    snapshot.clusters(),
                    snapshot.nearestNeighborK()
            );
            lastValidMetricKind = state.metricKind;
            lastValidSiteMemberKind = state.siteMemberKind;
            clearRejectedGadgetAttempts();
            clearSelection();
            state.resetMemberCountGadgetSync();
        } catch (SceneJsonException | IOException e) {
            state.galleryExample = GalleryExample.CURRENT;
            prevGalleryExample = GalleryExample.CURRENT;
            showErrorDialog("Examples load failed", e.getMessage());
            System.err.println("Examples load failed: " + e.getMessage());
        }
    }

    private void saveSceneToFile() {
        // JSON schema: see SceneJsonCodec (version "1", clusters, metricKind, neighborOrder, siteMemberKind, nearestNeighborK).
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save scene");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON scene", "*.json"));
        File file = chooser.showSaveDialog(firstShowingWindow());
        afterNativeFileDialog();
        if (file == null) {
            return;
        }
        if (!file.getName().toLowerCase().endsWith(".json")) {
            file = new File(file.getParentFile(), file.getName() + ".json");
        }
        try {
            SceneFileIo.save(file.toPath(), state);
        } catch (IOException e) {
            showErrorDialog("Save failed", e.getMessage());
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    private void loadSceneFromFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load scene");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON scene", "*.json"));
        File file = chooser.showOpenDialog(firstShowingWindow());
        afterNativeFileDialog();
        if (file == null) {
            return;
        }
        try {
            SceneFileIo.load(state, file.toPath());
            state.galleryExample = GalleryExample.CURRENT;
            lastValidMetricKind = state.metricKind;
            lastValidSiteMemberKind = state.siteMemberKind;
            clearRejectedGadgetAttempts();
            prevGalleryExample = state.galleryExample;
            clearSelection();
            state.resetMemberCountGadgetSync();
        } catch (SceneJsonException e) {
            showErrorDialog("Load failed", e.getMessage());
            System.err.println("Load failed: " + e.getMessage());
        } catch (IOException e) {
            showErrorDialog("Load failed", e.getMessage());
            System.err.println("Load failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Options options = Options.redrawOnEvents();
        options.windowTitle = "Cluster Voronoi Diagrams Explorer";
        options.drawingSize = sizeInitial;
        options.gridSubdivision = 8;
        options.gridInterval = gridCellD.x() * options.gridSubdivision;
        options.gridColor = Color.gray(0.0, 0.12);
        DrawingApplication.launch(options);
    }
}
