import type { CvdFrame, CvdOverlays } from './teavm.d.ts';

const SELECTION_STROKE = 'rgba(90, 40, 200, 0.95)';
/** Dark edges so geometry stays visible on the light web diagram (desktop uses white on a different backdrop). */
const ACTIVE_EDGE = 'rgba(20, 20, 20, 0.95)';
const INACTIVE_EDGE = 'rgba(40, 40, 40, 0.7)';
/** Extend infinite lines far beyond the fixed world box. */
const LINE_EXTENT = 5000;

export type WorldToPixel = (wx: number, wy: number) => { x: number; y: number };

function isTrue(flag: boolean | number): boolean {
  return flag === true || flag === 1;
}

export function handleIsVisible(frame: CvdFrame, handleIndex: number): boolean {
  const flags = frame.handles.visible;
  if (!flags || typeof (flags as { length?: number }).length !== 'number') {
    return true;
  }
  if (handleIndex < 0 || handleIndex >= flags.length) {
    return true;
  }
  const value = flags[handleIndex] as boolean | number | undefined;
  // Missing entries default to visible so a bad transfer cannot hide every handle.
  if (value === undefined || value === null) {
    return true;
  }
  return isTrue(value);
}

function strokeStyle(
  selected: boolean,
  activeCluster: boolean
): { stroke: string; lineWidth: number } {
  if (selected) {
    return { stroke: SELECTION_STROKE, lineWidth: 3 };
  }
  if (activeCluster) {
    return { stroke: ACTIVE_EDGE, lineWidth: 2.25 };
  }
  return { stroke: INACTIVE_EDGE, lineWidth: 1.5 };
}

function drawSegment(
  ctx: CanvasRenderingContext2D,
  toPixel: WorldToPixel,
  ax: number,
  ay: number,
  bx: number,
  by: number
): void {
  const a = toPixel(ax, ay);
  const b = toPixel(bx, by);
  ctx.beginPath();
  ctx.moveTo(a.x, a.y);
  ctx.lineTo(b.x, b.y);
  ctx.stroke();
}

function drawCircle(
  ctx: CanvasRenderingContext2D,
  toPixel: WorldToPixel,
  cx: number,
  cy: number,
  radius: number
): void {
  const center = toPixel(cx, cy);
  const rim = toPixel(cx + radius, cy);
  const rPx = Math.abs(rim.x - center.x);
  ctx.beginPath();
  ctx.arc(center.x, center.y, rPx, 0, Math.PI * 2);
  ctx.stroke();
}

function drawEllipsePolyline(
  ctx: CanvasRenderingContext2D,
  toPixel: WorldToPixel,
  overlays: CvdOverlays,
  index: number
): void {
  const start = overlays.ellipseStarts[index];
  const end = overlays.ellipseStarts[index + 1];
  if (end - start < 2) {
    return;
  }
  ctx.beginPath();
  for (let i = start; i < end; i++) {
    const p = toPixel(overlays.ellipseX[i], overlays.ellipseY[i]);
    if (i === start) {
      ctx.moveTo(p.x, p.y);
    } else {
      ctx.lineTo(p.x, p.y);
    }
  }
  ctx.closePath();
  ctx.stroke();
}

function drawInfiniteLine(
  ctx: CanvasRenderingContext2D,
  toPixel: WorldToPixel,
  ax: number,
  ay: number,
  bx: number,
  by: number
): void {
  const dx = bx - ax;
  const dy = by - ay;
  const len = Math.hypot(dx, dy);
  if (len <= 1e-12) {
    return;
  }
  const ux = dx / len;
  const uy = dy / len;
  drawSegment(
    ctx,
    toPixel,
    ax - ux * LINE_EXTENT,
    ay - uy * LINE_EXTENT,
    ax + ux * LINE_EXTENT,
    ay + uy * LINE_EXTENT
  );
}

/** Draws site geometry (segments, circles, ellipses, lines) under handles. */
export function drawMemberOverlays(
  ctx: CanvasRenderingContext2D,
  frame: CvdFrame,
  toPixel: WorldToPixel
): void {
  const overlays = frame.overlays;
  if (!overlays || overlays.n <= 0) {
    return;
  }
  const { scene } = frame;

  for (let i = 0; i < overlays.n; i++) {
    const cluster = overlays.cluster[i];
    const member = overlays.member[i];
    const selected =
      scene.selectedClusterIndex === cluster && scene.selectedMemberIndex === member;
    const activeCluster =
      scene.selectedClusterIndex >= 0 && cluster === scene.selectedClusterIndex;
    const style = strokeStyle(selected, activeCluster);
    ctx.strokeStyle = style.stroke;
    ctx.lineWidth = style.lineWidth;

    const kind = overlays.kind[i];
    if (kind === 'SEGMENT') {
      drawSegment(ctx, toPixel, overlays.ax[i], overlays.ay[i], overlays.bx[i], overlays.by[i]);
    } else if (kind === 'CIRCLE') {
      drawCircle(ctx, toPixel, overlays.ax[i], overlays.ay[i], overlays.radius[i]);
    } else if (kind === 'ELLIPSE') {
      drawEllipsePolyline(ctx, toPixel, overlays, i);
    } else if (kind === 'LINE') {
      drawInfiniteLine(ctx, toPixel, overlays.ax[i], overlays.ay[i], overlays.bx[i], overlays.by[i]);
    }
    // POINT: handle only
  }
}
