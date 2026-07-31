import { describe, expect, it } from 'vitest';

import { ALL_RASTER_OUTPUTS, rasterOutputMask } from './workerMessages';

describe('rasterOutputMask', () => {
  it('encodes all raster outputs', () => {
    expect(rasterOutputMask(ALL_RASTER_OUTPUTS)).toBe(0b111);
  });

  it('encodes only requested outputs', () => {
    expect(
      rasterOutputMask({
        argb: true,
        owners: true,
        members: false,
      })
    ).toBe(0b011);
    expect(
      rasterOutputMask({
        argb: false,
        owners: true,
        members: false,
      })
    ).toBe(0b010);
  });
});
