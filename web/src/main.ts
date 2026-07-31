import type { CvdAuthoringAction, CvdFrame, CvdSceneSettings } from './teavm.d.ts';
import { ClassifyClient } from './classifyClient';
import type { MoveHandleCmd } from './workerMessages';
import { drawMemberOverlays, handleIsVisible } from './memberOverlays';
import {
  boundsOf,
  defaultWorldView,
  handleSnapRadius,
  panByPixels,
  pixelToWorld,
  snapToNearestHandle,
  worldBoundsMatch,
  worldToPixel,
  zoomAt,
  type WorldView,
} from './camera';

const HANDLE_RADIUS_CSS = 6;
const HIT_RADIUS_CSS = 10;
/** Match desktop AppMain#DRAG_RASTER_SCALE while a handle is dragged. */
const DRAG_RASTER_SCALE = 0.32;
/** Cap backing-store edge length so TeaVM classify stays responsive. */
const MAX_RASTER_EDGE = 1536;

const canvas = document.querySelector<HTMLCanvasElement>('#diagram');
const statusEl = document.querySelector<HTMLElement>('#status');
const selectMetric = document.querySelector<HTMLSelectElement>('#select-metric');
const selectNeighbor = document.querySelector<HTMLSelectElement>('#select-neighbor');
const inputK = document.querySelector<HTMLInputElement>('#input-k');
const selectExample = document.querySelector<HTMLSelectElement>('#select-example');
const selectSiteKind = document.querySelector<HTMLSelectElement>('#select-site-kind');
const btnAddMember = document.querySelector<HTMLButtonElement>('#btn-add-member');
const btnRemoveMember = document.querySelector<HTMLButtonElement>('#btn-remove-member');
const btnAddCluster = document.querySelector<HTMLButtonElement>('#btn-add-cluster');
const btnRemoveCluster = document.querySelector<HTMLButtonElement>('#btn-remove-cluster');
const btnResetView = document.querySelector<HTMLButtonElement>('#btn-reset-view');
const btnCloseHelp = document.querySelector<HTMLButtonElement>('#btn-close-help');
const helpOverlay = document.querySelector<HTMLElement>('#help-overlay');
const togglePlaceMember = document.querySelector<HTMLInputElement>('#toggle-place-member');
const toggleDiagram = document.querySelector<HTMLInputElement>('#toggle-diagram');
const toggleMembers = document.querySelector<HTMLInputElement>('#toggle-members');
const toggleSkeleton = document.querySelector<HTMLInputElement>('#toggle-skeleton');
const toggleSubdivision = document.querySelector<HTMLInputElement>('#toggle-subdivision');
const toggleFastPreview = document.querySelector<HTMLInputElement>('#toggle-fast-preview');
const toggleSnapHandles = document.querySelector<HTMLInputElement>('#toggle-snap-handles');
const toggleHelp = document.querySelector<HTMLInputElement>('#toggle-help');

if (
  !canvas ||
  !statusEl ||
  !selectMetric ||
  !selectNeighbor ||
  !inputK ||
  !selectExample ||
  !selectSiteKind ||
  !btnAddMember ||
  !btnRemoveMember ||
  !btnAddCluster ||
  !btnRemoveCluster ||
  !btnResetView ||
  !btnCloseHelp ||
  !helpOverlay ||
  !togglePlaceMember ||
  !toggleDiagram ||
  !toggleMembers ||
  !toggleSkeleton ||
  !toggleSubdivision ||
  !toggleFastPreview ||
  !toggleSnapHandles ||
  !toggleHelp
) {
  throw new Error('Missing required UI elements');
}

const ctx = canvas.getContext('2d');
if (!ctx) {
  throw new Error('2D canvas context unavailable');
}

let client: ClassifyClient | null = null;
let latestFrame: CvdFrame | null = null;
let draggingHandle: number | null = null;
let panning = false;
/**
 * Left-button press on empty canvas: wait for drag threshold before panning so a
 * click (no drag) can clear selection instead.
 */
let pendingEmptyClick: { pointerId: number; x: number; y: number } | null = null;
let zooming = false;
let zoomIdleTimer: ReturnType<typeof setTimeout> | null = null;
let lastPanPixel: { x: number; y: number } | null = null;
/** Bitmap-pixel distance before an empty left-press becomes a pan. */
const EMPTY_CLICK_PAN_THRESHOLD_PX = 5;
let lastRenderMs = 0;
let syncingControls = false;
let worldView: WorldView = defaultWorldView();
let snapIndicator: { x: number; y: number } | null = null;
/** Physical pixels per CSS pixel used for the current backing store. */
let canvasPixelRatio = 1;
/** Last pointer position over the canvas (bitmap pixels), for shortcut add-at-pointer. */
let lastPointerPixel: { x: number; y: number } | null = null;

function setStatus(text: string, isError = false): void {
  statusEl!.textContent = text;
  statusEl!.classList.toggle('error', isError);
}

function handleRadiusPx(): number {
  return HANDLE_RADIUS_CSS * canvasPixelRatio;
}

function hitRadiusPx(): number {
  return HIT_RADIUS_CSS * canvasPixelRatio;
}

/**
 * Size the canvas bitmap to the on-screen box × devicePixelRatio (capped),
 * so Retina / large layouts are not stuck at a soft 512×512 upsample.
 */
function syncCanvasResolution(): boolean {
  const rect = canvas!.getBoundingClientRect();
  const css = Math.max(1, Math.min(rect.width, rect.height));
  const dpr = Math.min(window.devicePixelRatio || 1, 2);
  const edge = Math.max(1, Math.min(MAX_RASTER_EDGE, Math.round(css * dpr)));
  if (canvas!.width === edge && canvas!.height === edge && canvasPixelRatio === dpr) {
    return false;
  }
  canvas!.width = edge;
  canvas!.height = edge;
  canvasPixelRatio = dpr;
  return true;
}

function displaySize(): { width: number; height: number } {
  return { width: canvas!.width, height: canvas!.height };
}

function rasterSize(): { width: number; height: number } {
  const { width, height } = displaySize();
  // Low-res while dragging (optional) or while the camera is moving — TeaVM is sequential.
  const usePreview =
    panning ||
    zooming ||
    (draggingHandle !== null && toggleFastPreview!.checked);
  if (!usePreview) {
    return { width, height };
  }
  return {
    width: Math.max(1, Math.round(width * DRAG_RASTER_SCALE)),
    height: Math.max(1, Math.round(height * DRAG_RASTER_SCALE)),
  };
}

function argbToImageData(argb: ArrayLike<number>, width: number, height: number): ImageData {
  const rgba = new Uint8ClampedArray(width * height * 4);
  for (let i = 0; i < width * height; i++) {
    const pixel = argb[i] >>> 0;
    const o = i * 4;
    rgba[o] = (pixel >>> 16) & 0xff;
    rgba[o + 1] = (pixel >>> 8) & 0xff;
    rgba[o + 2] = pixel & 0xff;
    rgba[o + 3] = (pixel >>> 24) & 0xff;
  }
  return new ImageData(rgba, width, height);
}

function toWorld(px: number, py: number): { x: number; y: number } {
  const { width, height } = displaySize();
  return pixelToWorld(px, py, width, height, worldView);
}

function toPixel(wx: number, wy: number): { x: number; y: number } {
  const { width, height } = displaySize();
  return worldToPixel(wx, wy, width, height, worldView);
}

function applySnap(x: number, y: number, excludeHandle: number | null): { x: number; y: number } {
  let wx = x;
  let wy = y;
  snapIndicator = null;

  if (toggleSnapHandles!.checked && latestFrame) {
    const { width } = displaySize();
    const snapped = snapToNearestHandle(
      wx,
      wy,
      latestFrame.handles,
      excludeHandle,
      handleSnapRadius(worldView, width)
    );
    if (snapped.snapped) {
      wx = snapped.x;
      wy = snapped.y;
      snapIndicator = { x: wx, y: wy };
    }
  }

  return { x: wx, y: wy };
}

type Segment = { ax: number; ay: number; bx: number; by: number };

function extractSkeletonSegments(owners: ArrayLike<number>, width: number, height: number): Segment[] {
  const segments: Segment[] = [];
  if (width < 2 || height < 2) {
    return segments;
  }
  const at = (x: number, y: number) => owners[y * width + x];

  for (let y = 0; y < height - 1; y++) {
    for (let x = 0; x < width - 1; x++) {
      const topLeft = at(x, y);
      const topRight = at(x + 1, y);
      const bottomLeft = at(x, y + 1);
      const bottomRight = at(x + 1, y + 1);
      const crossings: Array<[number, number]> = [];
      if (topLeft !== topRight) crossings.push([x + 1, y + 0.5]);
      if (topRight !== bottomRight) crossings.push([x + 1.5, y + 1]);
      if (bottomLeft !== bottomRight) crossings.push([x + 1, y + 1.5]);
      if (topLeft !== bottomLeft) crossings.push([x + 0.5, y + 1]);
      if (crossings.length === 2) {
        segments.push({ ax: crossings[0][0], ay: crossings[0][1], bx: crossings[1][0], by: crossings[1][1] });
      } else if (crossings.length >= 3) {
        const cx = x + 1;
        const cy = y + 1;
        for (const [px, py] of crossings) {
          segments.push({ ax: px, ay: py, bx: cx, by: cy });
        }
      }
    }
  }
  return segments;
}

function extractSubdivisionSegments(
  owners: ArrayLike<number>,
  members: ArrayLike<number>,
  width: number,
  height: number
): Segment[] {
  const segments: Segment[] = [];
  if (width < 2 || height < 2) {
    return segments;
  }
  const clusterAt = (x: number, y: number) => owners[y * width + x];
  const memberAt = (x: number, y: number) => members[y * width + x];

  for (let y = 0; y < height - 1; y++) {
    for (let x = 0; x < width - 1; x++) {
      const tlC = clusterAt(x, y);
      const trC = clusterAt(x + 1, y);
      const blC = clusterAt(x, y + 1);
      const brC = clusterAt(x + 1, y + 1);
      const tlM = memberAt(x, y);
      const trM = memberAt(x + 1, y);
      const blM = memberAt(x, y + 1);
      const brM = memberAt(x + 1, y + 1);
      const crossings: Array<[number, number]> = [];
      if (tlC === trC && tlM !== trM && tlM >= 0 && trM >= 0) crossings.push([x + 1, y + 0.5]);
      if (trC === brC && trM !== brM && trM >= 0 && brM >= 0) crossings.push([x + 1.5, y + 1]);
      if (blC === brC && blM !== brM && blM >= 0 && brM >= 0) crossings.push([x + 1, y + 1.5]);
      if (tlC === blC && tlM !== blM && tlM >= 0 && blM >= 0) crossings.push([x + 0.5, y + 1]);
      if (crossings.length === 2) {
        segments.push({ ax: crossings[0][0], ay: crossings[0][1], bx: crossings[1][0], by: crossings[1][1] });
      } else if (crossings.length >= 3) {
        const cx = x + 1;
        const cy = y + 1;
        for (const [px, py] of crossings) {
          segments.push({ ax: px, ay: py, bx: cx, by: cy });
        }
      }
    }
  }
  return segments;
}

function strokeSegments(segments: Segment[], scaleX: number, scaleY: number, style: string, lineWidth: number): void {
  if (segments.length === 0) {
    return;
  }
  ctx!.strokeStyle = style;
  ctx!.lineWidth = lineWidth;
  ctx!.beginPath();
  for (const s of segments) {
    ctx!.moveTo(s.ax * scaleX, s.ay * scaleY);
    ctx!.lineTo(s.bx * scaleX, s.by * scaleY);
  }
  ctx!.stroke();
}

function drawHandles(frame: CvdFrame, displayWidth: number, displayHeight: number): void {
  const { handles, scene } = frame;
  for (let i = 0; i < handles.n; i++) {
    if (!handleIsVisible(frame, i)) {
      continue;
    }
    const { x: px, y: py } = worldToPixel(handles.x[i], handles.y[i], displayWidth, displayHeight, worldView);
    const r = Math.round(handles.r[i] * 255);
    const g = Math.round(handles.g[i] * 255);
    const b = Math.round(handles.b[i] * 255);
    const cluster = handles.cluster[i];
    const member = handles.member[i];
    const within = handles.within?.[i] ?? 0;
    const isActiveCluster =
      scene.selectedClusterIndex >= 0 && cluster === scene.selectedClusterIndex;
    const isSelectedMember =
      scene.selectedClusterIndex === cluster && scene.selectedMemberIndex === member;
    const isSelectedHandle = isSelectedMember && scene.selectedHandleIndex === within;
    const isDragging = draggingHandle === i;

    ctx!.beginPath();
    ctx!.arc(px, py, handleRadiusPx() + (isSelectedHandle ? canvasPixelRatio : 0), 0, Math.PI * 2);
    ctx!.fillStyle = `rgb(${r}, ${g}, ${b})`;
    ctx!.fill();
    ctx!.lineWidth = (isDragging || isSelectedHandle ? 2.5 : isActiveCluster ? 2 : 1.5) * canvasPixelRatio;
    ctx!.strokeStyle = isSelectedHandle || isDragging
      ? 'rgba(90, 40, 200, 0.95)'
      : isActiveCluster
        ? 'rgba(255, 255, 255, 1)'
        : 'rgba(255, 255, 255, 0.75)';
    ctx!.stroke();
  }
}

function drawSnapIndicator(): void {
  if (!snapIndicator) {
    return;
  }
  const p = toPixel(snapIndicator.x, snapIndicator.y);
  ctx!.beginPath();
  ctx!.arc(p.x, p.y, handleRadiusPx() * 1.8, 0, Math.PI * 2);
  ctx!.strokeStyle = 'rgba(220, 180, 20, 0.9)';
  ctx!.lineWidth = 2.5 * canvasPixelRatio;
  ctx!.stroke();
}

function syncControlsFromScene(frame: CvdFrame): void {
  syncingControls = true;
  selectMetric!.value = frame.scene.metricKind;
  selectNeighbor!.value = frame.scene.neighborOrder;
  inputK!.value = String(frame.scene.nearestNeighborK);
  // Keep the site-kind dropdown local while a new kind is in flight; snap back on
  // reject, or refresh after loads that did not request a kind (e.g. Examples).
  if (
    frame.requestedSiteMemberKind == null ||
    frame.requestedSiteMemberKind !== frame.scene.siteMemberKind
  ) {
    selectSiteKind!.value = frame.scene.siteMemberKind;
  }
  syncingControls = false;
}

function paint(frame: CvdFrame, ms: number): void {
  latestFrame = frame;
  lastRenderMs = ms;
  syncControlsFromScene(frame);

  const { width: dw, height: dh } = displaySize();
  const scaleX = dw / frame.width;
  const scaleY = dh / frame.height;

  if (toggleDiagram!.checked) {
    const imageData = argbToImageData(frame.argb, frame.width, frame.height);
    if (frame.width === dw && frame.height === dh) {
      ctx!.putImageData(imageData, 0, 0);
    } else {
      const tmp = document.createElement('canvas');
      tmp.width = frame.width;
      tmp.height = frame.height;
      tmp.getContext('2d')!.putImageData(imageData, 0, 0);
      ctx!.imageSmoothingEnabled = false;
      ctx!.drawImage(tmp, 0, 0, dw, dh);
    }
  } else {
    ctx!.fillStyle = '#ebe8e2';
    ctx!.fillRect(0, 0, dw, dh);
  }

  // Hide skeleton/subdivision while camera moves or during fast-preview drag.
  // Keep members visible while dragging so the selected site (and others) stay visible as they move.
  const cameraBusy = panning || zooming;
  const frameMatchesView = worldBoundsMatch(frame.worldView, boundsOf(worldView));
  const skipContourOverlays =
    cameraBusy ||
    !frameMatchesView ||
    (draggingHandle !== null && toggleFastPreview!.checked);
  const skipMembers =
    cameraBusy || !frameMatchesView;

  if (!skipContourOverlays && toggleSkeleton!.checked) {
    strokeSegments(
      extractSkeletonSegments(frame.owners, frame.width, frame.height),
      scaleX,
      scaleY,
      'rgba(0, 0, 0, 0.8)',
      1.5 * canvasPixelRatio
    );
  }

  if (!skipContourOverlays && toggleSubdivision!.checked && frame.members) {
    strokeSegments(
      extractSubdivisionSegments(frame.owners, frame.members, frame.width, frame.height),
      scaleX,
      scaleY,
      'rgba(0, 0, 0, 0.4)',
      1 * canvasPixelRatio
    );
  }

  if (toggleMembers!.checked && !skipMembers) {
    try {
      drawMemberOverlays(ctx!, frame, (wx, wy) => toPixel(wx, wy));
    } catch (err) {
      console.error('Member overlay draw failed', err);
    }
    try {
      drawHandles(frame, dw, dh);
    } catch (err) {
      console.error('Handle draw failed', err);
    }
    drawSnapIndicator();
  }

  const preview = draggingHandle !== null || frame.width !== dw;
  const base =
    `${dw}×${dh}` +
    (canvasPixelRatio > 1 ? ` @${canvasPixelRatio.toFixed(2)}x` : '') +
    (preview ? ` (preview ${frame.width}×${frame.height})` : '') +
    `, view ±${worldView.half.toFixed(0)} @ (${worldView.cx.toFixed(0)}, ${worldView.cy.toFixed(0)})` +
    `, ${frame.scene.clusterCount} clusters (${ms.toFixed(1)} ms).`;
  if (frame.scene.lastError) {
    setStatus(`${frame.scene.lastError} — ${base}`, true);
  } else {
    setStatus(base, false);
  }
}

function currentSettings(): CvdSceneSettings {
  const k = Number.parseInt(inputK!.value, 10);
  return {
    metricKind: selectMetric!.value,
    neighborOrder: selectNeighbor!.value,
    nearestNeighborK: Number.isFinite(k) ? k : 1,
    shading: false,
    siteMemberKind: selectSiteKind!.value,
    worldView: boundsOf(worldView),
  };
}

function requestClassify(opts?: {
  move?: MoveHandleCmd;
  settings?: CvdSceneSettings;
  actions?: CvdAuthoringAction[];
}): void {
  if (!client) {
    return;
  }
  const { width, height } = rasterSize();
  const settings = opts?.settings
    ? { ...opts.settings, worldView: opts.settings.worldView ?? boundsOf(worldView) }
    : { worldView: boundsOf(worldView) };
  client.enqueue({
    width,
    height,
    move: opts?.move,
    settings,
    actions: opts?.actions,
  });
}

function eventToBitmapPixel(event: PointerEvent): { x: number; y: number } {
  const rect = canvas!.getBoundingClientRect();
  const scaleX = canvas!.width / rect.width;
  const scaleY = canvas!.height / rect.height;
  return {
    x: (event.clientX - rect.left) * scaleX,
    y: (event.clientY - rect.top) * scaleY,
  };
}

function findNearestHandle(px: number, py: number): number | null {
  if (!latestFrame) {
    return null;
  }
  const { width: dw, height: dh } = displaySize();
  const { handles } = latestFrame;
  let bestIndex: number | null = null;
  let bestDistanceSq = hitRadiusPx() * hitRadiusPx();
  for (let i = 0; i < handles.n; i++) {
    if (!handleIsVisible(latestFrame, i)) {
      continue;
    }
    const { x: hx, y: hy } = worldToPixel(handles.x[i], handles.y[i], dw, dh, worldView);
    const dx = hx - px;
    const dy = hy - py;
    const distanceSq = dx * dx + dy * dy;
    if (distanceSq <= bestDistanceSq) {
      bestDistanceSq = distanceSq;
      bestIndex = i;
    }
  }
  return bestIndex;
}

function updateCursor(): void {
  if (panning || draggingHandle !== null) {
    canvas!.style.cursor = 'grabbing';
  } else if (togglePlaceMember!.checked) {
    canvas!.style.cursor = 'crosshair';
  } else {
    canvas!.style.cursor = 'grab';
  }
}

function repaintLatest(): void {
  if (latestFrame) {
    paint(latestFrame, lastRenderMs);
  }
}

function markZooming(): void {
  const wasZooming = zooming;
  zooming = true;
  if (zoomIdleTimer !== null) {
    clearTimeout(zoomIdleTimer);
  }
  zoomIdleTimer = setTimeout(() => {
    zooming = false;
    zoomIdleTimer = null;
    // Do not show overlays here — wait for a classify frame that matches worldView.
    requestClassify({ settings: currentSettings() });
  }, 160);
  if (!wasZooming) {
    repaintLatest();
  }
}

function setHelpVisible(visible: boolean): void {
  helpOverlay!.hidden = !visible;
  if (toggleHelp!.checked !== visible) {
    syncingControls = true;
    toggleHelp!.checked = visible;
    syncingControls = false;
  }
}

function beginPan(pointerId: number, x: number, y: number): void {
  pendingEmptyClick = null;
  panning = true;
  lastPanPixel = { x, y };
  canvas!.setPointerCapture(pointerId);
  updateCursor();
  repaintLatest();
}

function onPointerDown(event: PointerEvent): void {
  const { x, y } = eventToBitmapPixel(event);
  const nearest = findNearestHandle(x, y);
  const middle = event.button === 1;

  // Middle-click always pans immediately.
  if (middle) {
    beginPan(event.pointerId, x, y);
    return;
  }

  // Empty left-press: defer pan until drag so a click clears selection.
  if (event.button === 0 && nearest === null && !togglePlaceMember!.checked) {
    pendingEmptyClick = { pointerId: event.pointerId, x, y };
    canvas!.setPointerCapture(event.pointerId);
    return;
  }

  if (nearest !== null && toggleMembers!.checked && event.button === 0) {
    draggingHandle = nearest;
    canvas!.setPointerCapture(event.pointerId);
    updateCursor();
    repaintLatest();
    requestClassify({
      settings: currentSettings(),
      actions: [
        { type: 'selectHandle', index: nearest },
        { type: 'beginHandleDrag', index: nearest },
      ],
    });
    return;
  }

  if (togglePlaceMember!.checked && event.button === 0) {
    const raw = toWorld(x, y);
    const world = applySnap(raw.x, raw.y, null);
    requestClassify({
      settings: currentSettings(),
      actions: [{ type: 'addMemberAt', worldX: world.x, worldY: world.y }],
    });
  }
}

function onPointerMove(event: PointerEvent): void {
  const { x, y } = eventToBitmapPixel(event);
  const { width, height } = displaySize();

  if (pendingEmptyClick && event.pointerId === pendingEmptyClick.pointerId) {
    const dx = x - pendingEmptyClick.x;
    const dy = y - pendingEmptyClick.y;
    const threshold = EMPTY_CLICK_PAN_THRESHOLD_PX * canvasPixelRatio;
    if (dx * dx + dy * dy >= threshold * threshold) {
      beginPan(pendingEmptyClick.pointerId, pendingEmptyClick.x, pendingEmptyClick.y);
      // Apply this move as the first pan delta from the press origin.
      worldView = panByPixels(worldView, x - pendingEmptyClick.x, y - pendingEmptyClick.y, width, height);
      lastPanPixel = { x, y };
      requestClassify({ settings: currentSettings() });
    }
    return;
  }

  if (panning && lastPanPixel) {
    worldView = panByPixels(worldView, x - lastPanPixel.x, y - lastPanPixel.y, width, height);
    lastPanPixel = { x, y };
    requestClassify({ settings: currentSettings() });
    return;
  }

  if (draggingHandle === null) {
    return;
  }
  const raw = toWorld(x, y);
  const snapped = applySnap(raw.x, raw.y, draggingHandle);
  requestClassify({
    settings: { worldView: boundsOf(worldView) },
    move: {
      index: draggingHandle,
      worldX: snapped.x,
      worldY: snapped.y,
      coMove: !event.shiftKey,
    },
  });
}

function onPointerUp(event: PointerEvent): void {
  if (pendingEmptyClick && event.pointerId === pendingEmptyClick.pointerId) {
    pendingEmptyClick = null;
    if (canvas!.hasPointerCapture(event.pointerId)) {
      canvas!.releasePointerCapture(event.pointerId);
    }
    requestClassify({
      settings: currentSettings(),
      actions: [{ type: 'clearSelection' }],
    });
    return;
  }

  if (panning) {
    panning = false;
    lastPanPixel = null;
    if (canvas!.hasPointerCapture(event.pointerId)) {
      canvas!.releasePointerCapture(event.pointerId);
    }
    updateCursor();
    requestClassify({ settings: currentSettings() });
    repaintLatest();
    return;
  }

  if (draggingHandle === null) {
    return;
  }
  draggingHandle = null;
  snapIndicator = null;
  updateCursor();
  if (canvas!.hasPointerCapture(event.pointerId)) {
    canvas!.releasePointerCapture(event.pointerId);
  }
  requestClassify({
    settings: currentSettings(),
    actions: [{ type: 'endHandleDrag' }],
  });
}

/** Abort pending empty-click without clearing selection; otherwise same as pointerup. */
function onPointerCancel(event: PointerEvent): void {
  if (pendingEmptyClick && event.pointerId === pendingEmptyClick.pointerId) {
    pendingEmptyClick = null;
    if (canvas!.hasPointerCapture(event.pointerId)) {
      canvas!.releasePointerCapture(event.pointerId);
    }
    return;
  }
  onPointerUp(event);
}

function onWheel(event: WheelEvent): void {
  event.preventDefault();
  const rect = canvas!.getBoundingClientRect();
  const scaleX = canvas!.width / rect.width;
  const scaleY = canvas!.height / rect.height;
  const x = (event.clientX - rect.left) * scaleX;
  const y = (event.clientY - rect.top) * scaleY;
  const { width, height } = displaySize();
  const factor = event.deltaY < 0 ? 1.12 : 1 / 1.12;
  worldView = zoomAt(worldView, x, y, width, height, factor);
  markZooming();
  requestClassify({ settings: currentSettings() });
}

function flipToggle(toggle: HTMLInputElement, then?: () => void): void {
  toggle.checked = !toggle.checked;
  then?.();
}

function addMemberAtPointerOrCenter(): void {
  const { width: dw, height: dh } = displaySize();
  const px = lastPointerPixel?.x ?? dw / 2;
  const py = lastPointerPixel?.y ?? dh / 2;
  const raw = pixelToWorld(px, py, dw, dh, worldView);
  const world = applySnap(raw.x, raw.y, null);
  requestClassify({
    settings: currentSettings(),
    actions: [{ type: 'addMemberAt', worldX: world.x, worldY: world.y }],
  });
}

function removeActiveMember(): void {
  requestClassify({ settings: currentSettings(), actions: [{ type: 'removeMember' }] });
}

function cycleActiveCluster(delta: number): void {
  requestClassify({
    settings: currentSettings(),
    actions: [{ type: 'cycleSelectedCluster', delta }],
  });
}

function cycleSelectedMember(delta: number): void {
  requestClassify({
    settings: currentSettings(),
    actions: [{ type: 'cycleSelectedMember', delta }],
  });
}

function isTypingTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) {
    return false;
  }
  const tag = target.tagName;
  return tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA' || target.isContentEditable;
}

function addCluster(): void {
  requestClassify({ settings: currentSettings(), actions: [{ type: 'addCluster' }] });
}

function removeCluster(): void {
  requestClassify({ settings: currentSettings(), actions: [{ type: 'removeCluster' }] });
}

function onKeyDown(event: KeyboardEvent): void {
  if (event.metaKey || event.ctrlKey || event.altKey) {
    return;
  }
  if (isTypingTarget(event.target)) {
    return;
  }

  const key = event.key.length === 1 ? event.key.toLowerCase() : event.key;
  const repaintOnly = () => {
    if (latestFrame) {
      paint(latestFrame, lastRenderMs);
    }
  };

  switch (key) {
    case 'h':
      event.preventDefault();
      setHelpVisible(helpOverlay!.hidden);
      break;
    case 'c':
      event.preventDefault();
      flipToggle(toggleDiagram!, repaintOnly);
      break;
    case 'm':
      event.preventDefault();
      flipToggle(toggleMembers!, repaintOnly);
      break;
    case 'k':
      event.preventDefault();
      flipToggle(toggleSkeleton!, repaintOnly);
      break;
    case 'v':
      event.preventDefault();
      flipToggle(toggleSubdivision!, repaintOnly);
      break;
    case 'f':
      event.preventDefault();
      flipToggle(toggleSnapHandles!);
      break;
    case 'n':
      event.preventDefault();
      if (event.shiftKey) {
        cycleActiveCluster(1);
      } else {
        cycleSelectedMember(1);
      }
      break;
    case 'p':
      event.preventDefault();
      if (event.shiftKey) {
        cycleActiveCluster(-1);
      } else {
        cycleSelectedMember(-1);
      }
      break;
    case 'a':
      event.preventDefault();
      if (event.shiftKey) {
        addCluster();
      } else {
        addMemberAtPointerOrCenter();
      }
      break;
    case 'd':
      event.preventDefault();
      if (event.shiftKey) {
        removeCluster();
      } else {
        removeActiveMember();
      }
      break;
    case 'Escape':
      if (!helpOverlay!.hidden) {
        event.preventDefault();
        setHelpVisible(false);
      }
      break;
    default:
      break;
  }
}

function galleryUrl(path: string): string {
  const base = import.meta.env.BASE_URL.endsWith('/')
    ? import.meta.env.BASE_URL
    : `${import.meta.env.BASE_URL}/`;
  return new URL(`${base}scenes/gallery/${path}`, window.location.href).href;
}

async function populateExamplesDropdown(): Promise<void> {
  const res = await fetch(galleryUrl('manifest.json'), { cache: 'no-store' });
  if (!res.ok) {
    throw new Error(`Failed to load gallery manifest (${res.status})`);
  }
  const manifest = (await res.json()) as { files?: string[] };
  const files = Array.isArray(manifest.files) ? manifest.files : [];
  for (const fileName of files) {
    const sceneRes = await fetch(galleryUrl(fileName), { cache: 'no-store' });
    if (!sceneRes.ok) {
      throw new Error(`Failed to load gallery scene ${fileName} (${sceneRes.status})`);
    }
    const scene = (await sceneRes.json()) as { name?: string };
    const name = typeof scene.name === 'string' ? scene.name.trim() : '';
    if (!name) {
      throw new Error(`Gallery scene missing name: ${fileName}`);
    }
    const opt = document.createElement('option');
    opt.value = fileName;
    opt.textContent = name;
    selectExample!.appendChild(opt);
  }
}

async function onExampleChange(): Promise<void> {
  if (syncingControls) {
    return;
  }
  const fileName = selectExample!.value;
  if (!fileName) {
    return;
  }
  try {
    const res = await fetch(galleryUrl(fileName), { cache: 'no-store' });
    if (!res.ok) {
      throw new Error(`Failed to load ${fileName} (${res.status})`);
    }
    const json = await res.text();
    worldView = defaultWorldView();
    requestClassify({
      settings: { worldView: boundsOf(worldView) },
      actions: [{ type: 'loadSceneJson', json }],
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    setStatus(message, true);
    syncingControls = true;
    selectExample!.value = '';
    syncingControls = false;
  }
}

function onSceneControlChange(): void {
  if (syncingControls) {
    return;
  }
  requestClassify({ settings: currentSettings() });
}

function wireControls(): void {
  canvas!.addEventListener('pointerdown', onPointerDown);
  canvas!.addEventListener('pointermove', onPointerMove);
  canvas!.addEventListener('pointerup', onPointerUp);
  canvas!.addEventListener('pointercancel', onPointerCancel);
  canvas!.addEventListener('wheel', onWheel, { passive: false });
  canvas!.addEventListener('contextmenu', (e) => e.preventDefault());
  canvas!.addEventListener('pointermove', (event) => {
    lastPointerPixel = eventToBitmapPixel(event);
  });
  window.addEventListener('keydown', onKeyDown);

  const resizeObserver = new ResizeObserver(() => {
    if (syncCanvasResolution()) {
      requestClassify({ settings: currentSettings() });
    }
  });
  resizeObserver.observe(canvas!);
  window.addEventListener('resize', () => {
    if (syncCanvasResolution()) {
      requestClassify({ settings: currentSettings() });
    }
  });

  selectMetric!.addEventListener('change', onSceneControlChange);
  selectNeighbor!.addEventListener('change', onSceneControlChange);
  inputK!.addEventListener('change', onSceneControlChange);
  selectExample!.addEventListener('change', () => {
    void onExampleChange();
  });
  selectSiteKind!.addEventListener('change', onSceneControlChange);

  btnAddMember!.addEventListener('click', () => addMemberAtPointerOrCenter());
  btnRemoveMember!.addEventListener('click', () => removeActiveMember());
  btnAddCluster!.addEventListener('click', () => addCluster());
  btnRemoveCluster!.addEventListener('click', () => removeCluster());
  btnResetView!.addEventListener('click', () => {
    worldView = defaultWorldView();
    requestClassify({ settings: currentSettings() });
  });

  togglePlaceMember!.addEventListener('change', updateCursor);
  toggleHelp!.addEventListener('change', () => {
    if (!syncingControls) {
      setHelpVisible(toggleHelp!.checked);
    }
  });
  btnCloseHelp!.addEventListener('click', () => setHelpVisible(false));
  helpOverlay!.addEventListener('click', (e) => {
    if (e.target === helpOverlay) {
      setHelpVisible(false);
    }
  });

  const repaintOnly = () => {
    if (latestFrame) {
      paint(latestFrame, lastRenderMs);
    } else {
      requestClassify({ settings: currentSettings() });
    }
  };
  toggleDiagram!.addEventListener('change', repaintOnly);
  toggleMembers!.addEventListener('change', repaintOnly);
  toggleSkeleton!.addEventListener('change', repaintOnly);
  toggleSubdivision!.addEventListener('change', repaintOnly);
}

function teavmModuleUrl(): string {
  return new URL(`${import.meta.env.BASE_URL}teavm/cvd-core.js`, window.location.href).href;
}

async function main(): Promise<void> {
  setStatus('Loading TeaVM in worker…');
  syncCanvasResolution();
  client = new ClassifyClient(teavmModuleUrl());
  client.onFrame = paint;
  client.onError = (err) => {
    console.error(err);
    setStatus(err.message, true);
  };
  await client.whenReady();
  try {
    await populateExamplesDropdown();
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    setStatus(message, true);
  }

  updateCursor();
  wireControls();
  syncCanvasResolution();
  requestClassify({ settings: currentSettings() });
}

main().catch((err: unknown) => {
  console.error(err);
  setStatus(err instanceof Error ? err.message : String(err), true);
});
