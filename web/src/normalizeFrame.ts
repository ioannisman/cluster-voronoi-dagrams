import type { CvdFrame, CvdHandles, CvdOverlays, CvdSceneState } from './teavm.d.ts';

function toFloat64(source: ArrayLike<number> | null | undefined): Float64Array {
  if (source == null) {
    return new Float64Array(0);
  }
  return source instanceof Float64Array ? source : Float64Array.from(source as ArrayLike<number>);
}

function toInt32(source: ArrayLike<number> | null | undefined): Int32Array {
  if (source == null) {
    return new Int32Array(0);
  }
  return source instanceof Int32Array ? source : Int32Array.from(source as ArrayLike<number>);
}

function toOptionalInt32(
  source: ArrayLike<number> | null | undefined
): Int32Array | undefined {
  return source == null ? undefined : toInt32(source);
}

function toBool01(source: ArrayLike<boolean | number> | null | undefined, length: number): Uint8Array {
  const out = new Uint8Array(length);
  if (source == null) {
    out.fill(1);
    return out;
  }
  for (let i = 0; i < length; i++) {
    const v = source[i] as boolean | number | undefined;
    out[i] = v === false || v === 0 ? 0 : 1;
  }
  return out;
}

function toStringArray(source: ArrayLike<string> | null | undefined, length: number): string[] {
  const out: string[] = new Array(length);
  if (source == null) {
    out.fill('POINT');
    return out;
  }
  for (let i = 0; i < length; i++) {
    out[i] = String(source[i] ?? 'POINT');
  }
  return out;
}

/**
 * Copy TeaVM frame payloads into plain structured-clone-safe buffers.
 * Avoids odd worker-transfer cases where visibility flags or overlay kinds
 * become unusable and members disappear from the canvas.
 */
export function normalizeFrame(frame: CvdFrame): CvdFrame {
  const n = frame.handles?.n ?? 0;
  const handles: CvdHandles = {
    x: toFloat64(frame.handles?.x),
    y: toFloat64(frame.handles?.y),
    cluster: toInt32(frame.handles?.cluster),
    member: toInt32(frame.handles?.member),
    within: toInt32(frame.handles?.within),
    visible: toBool01(frame.handles?.visible as ArrayLike<boolean | number>, n),
    r: toFloat64(frame.handles?.r),
    g: toFloat64(frame.handles?.g),
    b: toFloat64(frame.handles?.b),
    n,
  };

  const oc = frame.overlays?.n ?? 0;
  const overlays: CvdOverlays = {
    n: oc,
    kind: toStringArray(frame.overlays?.kind, oc),
    cluster: toInt32(frame.overlays?.cluster),
    member: toInt32(frame.overlays?.member),
    ax: toFloat64(frame.overlays?.ax),
    ay: toFloat64(frame.overlays?.ay),
    bx: toFloat64(frame.overlays?.bx),
    by: toFloat64(frame.overlays?.by),
    radius: toFloat64(frame.overlays?.radius),
    ellipseX: toFloat64(frame.overlays?.ellipseX),
    ellipseY: toFloat64(frame.overlays?.ellipseY),
    ellipseStarts: toInt32(frame.overlays?.ellipseStarts),
  };

  const scene: CvdSceneState = {
    metricKind: frame.scene?.metricKind ?? 'MINIMUM_DISTANCE',
    neighborOrder: frame.scene?.neighborOrder ?? 'NEAREST',
    nearestNeighborK: frame.scene?.nearestNeighborK ?? 1,
    shading: !!frame.scene?.shading,
    lastError: frame.scene?.lastError ?? '',
    clusterCount: frame.scene?.clusterCount ?? 0,
    activeClusterIndex: frame.scene?.activeClusterIndex ?? -1,
    activeMemberCount: frame.scene?.activeMemberCount ?? 0,
    siteMemberKind: frame.scene?.siteMemberKind ?? 'POINT',
    selectedClusterIndex: frame.scene?.selectedClusterIndex ?? -1,
    selectedMemberIndex: frame.scene?.selectedMemberIndex ?? -1,
    selectedHandleIndex: frame.scene?.selectedHandleIndex ?? -1,
    clusterNames: Array.isArray(frame.scene?.clusterNames)
      ? frame.scene.clusterNames.map(String)
      : [],
  };

  return {
    argb: toOptionalInt32(frame.argb),
    owners: toOptionalInt32(frame.owners),
    members: toOptionalInt32(frame.members),
    width: frame.width,
    height: frame.height,
    handles,
    overlays,
    scene,
  };
}
