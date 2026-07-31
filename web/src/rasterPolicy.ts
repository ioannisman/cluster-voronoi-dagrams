export const PREVIEW_MAX_EDGE = 384;
export const BALANCED_MAX_EDGE = 768;
export const HIGH_QUALITY_MAX_EDGE = 1024;

export type RasterQuality = 'balanced' | 'high';

export type RasterPolicyInput = {
  displayWidth: number;
  displayHeight: number;
  interactive: boolean;
  quality: RasterQuality;
};

export type RasterSelection = {
  width: number;
  height: number;
  preview: boolean;
  maxEdge: number;
};

export function fitWithinMaxEdge(
  width: number,
  height: number,
  maxEdge: number
): { width: number; height: number } {
  const safeWidth = Math.max(1, Math.round(width));
  const safeHeight = Math.max(1, Math.round(height));
  const safeMaxEdge = Math.max(1, Math.round(maxEdge));
  const scale = Math.min(1, safeMaxEdge / Math.max(safeWidth, safeHeight));
  return {
    width: Math.max(1, Math.round(safeWidth * scale)),
    height: Math.max(1, Math.round(safeHeight * scale)),
  };
}

export function selectRasterSize(input: RasterPolicyInput): RasterSelection {
  const maxEdge = input.interactive
    ? PREVIEW_MAX_EDGE
    : input.quality === 'high'
      ? HIGH_QUALITY_MAX_EDGE
      : BALANCED_MAX_EDGE;
  return {
    ...fitWithinMaxEdge(input.displayWidth, input.displayHeight, maxEdge),
    preview: input.interactive,
    maxEdge,
  };
}
