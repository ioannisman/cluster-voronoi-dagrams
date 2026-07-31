import { describe, expect, it } from 'vitest';

import {
  BALANCED_MAX_EDGE,
  HIGH_QUALITY_MAX_EDGE,
  PREVIEW_MAX_EDGE,
  fitWithinMaxEdge,
  selectRasterSize,
} from './rasterPolicy';

describe('fitWithinMaxEdge', () => {
  it('preserves aspect ratio while reducing the longest edge', () => {
    expect(fitWithinMaxEdge(1600, 800, 400)).toEqual({ width: 400, height: 200 });
  });

  it('does not upscale a smaller display', () => {
    expect(fitWithinMaxEdge(320, 240, 768)).toEqual({ width: 320, height: 240 });
  });
});

describe('selectRasterSize', () => {
  it('uses the preview cap during interaction', () => {
    expect(
      selectRasterSize({
        displayWidth: 1536,
        displayHeight: 1536,
        interactive: true,
        quality: 'high',
      })
    ).toEqual({
      width: PREVIEW_MAX_EDGE,
      height: PREVIEW_MAX_EDGE,
      preview: true,
      maxEdge: PREVIEW_MAX_EDGE,
    });
  });

  it('uses balanced as the completed default', () => {
    expect(
      selectRasterSize({
        displayWidth: 1536,
        displayHeight: 1536,
        interactive: false,
        quality: 'balanced',
      }).width
    ).toBe(BALANCED_MAX_EDGE);
  });

  it('allows explicit high quality without native-size refinement', () => {
    const selected = selectRasterSize({
      displayWidth: 1536,
      displayHeight: 1536,
      interactive: false,
      quality: 'high',
    });
    expect(selected.width).toBe(HIGH_QUALITY_MAX_EDGE);
    expect(selected.width).toBeLessThan(1536);
  });
});
