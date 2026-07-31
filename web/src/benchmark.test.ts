import { describe, expect, it } from 'vitest';

import { ALL_METRICS, compatibleMetrics, median } from './benchmark';

describe('median', () => {
  it('returns the middle sorted value', () => {
    expect(median([9, 1, 5])).toBe(5);
  });

  it('averages the two middle values', () => {
    expect(median([4, 1, 2, 3])).toBe(2.5);
  });

  it('returns zero for no samples', () => {
    expect(median([])).toBe(0);
  });
});

describe('compatibleMetrics', () => {
  it('allows every metric for point-only scenes', () => {
    const scene = {
      clusters: [
        { members: [{ kind: 'POINT' }, { kind: 'POINT' }] },
        { members: [{ kind: 'POINT' }] },
      ],
    };

    expect(compatibleMetrics(scene)).toEqual(ALL_METRICS);
  });

  it('limits mixed-member scenes to supported metrics', () => {
    const scene = {
      clusters: [
        { members: [{ kind: 'POINT' }] },
        { members: [{ kind: 'CIRCLE' }] },
      ],
    };

    expect(compatibleMetrics(scene)).toEqual(['MINIMUM_DISTANCE', 'MAXIMUM_DISTANCE']);
  });
});
