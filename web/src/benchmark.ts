import type { FrameDelivery, FrameRequest, FrameTimings } from './classifyClient';

export const BENCHMARK_EDGES = [512, 768, 960, 1536] as const;
export const BENCHMARK_WARMUPS = 2;
export const BENCHMARK_RUNS = 5;

export const ALL_METRICS = [
  'MINIMUM_DISTANCE',
  'MAXIMUM_DISTANCE',
  'SUM_OF_DISTANCES',
  'MEAN_DISTANCE',
  'KTH_NEAREST_DISTANCE',
] as const;

type GalleryMember = { kind?: string };
type GalleryCluster = { members?: GalleryMember[] };
type GalleryScene = {
  name?: string;
  nearestNeighborK?: number;
  clusters?: GalleryCluster[];
};

type TimingKey = keyof FrameTimings;

export type BenchmarkResult = {
  scene: string;
  sceneFile: string;
  metric: string;
  raster: string;
  samples: number;
  medians: FrameTimings;
};

export type PreviewAfterFullResult = {
  scene: string;
  fullEdge: number;
  previewEdge: number;
  previewMedians: FrameTimings;
  argbLength: number;
  ownersLength: number;
  membersLength: number;
};

export type BenchmarkReport = {
  generatedAt: string;
  userAgent: string;
  devicePixelRatio: number;
  viewport: string;
  warmups: number;
  measuredRuns: number;
  results: BenchmarkResult[];
  previewAfterFull: PreviewAfterFullResult[];
};

type BenchmarkOptions = {
  request: (request: FrameRequest) => Promise<FrameDelivery>;
  sceneFiles: string[];
  galleryUrl: (fileName: string) => string;
  onProgress?: (message: string) => void;
};

const TIMING_KEYS: TimingKey[] = [
  'classifyMs',
  'exportMs',
  'normalizeMs',
  'workerMs',
  'responseMs',
  'argbMs',
  'contourMs',
  'paintMs',
  'totalMs',
];

export function median(values: number[]): number {
  if (values.length === 0) {
    return 0;
  }
  const sorted = [...values].sort((a, b) => a - b);
  const middle = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 0
    ? (sorted[middle - 1] + sorted[middle]) / 2
    : sorted[middle];
}

export function compatibleMetrics(scene: GalleryScene): string[] {
  const members = (scene.clusters ?? []).flatMap((cluster) => cluster.members ?? []);
  const pointOnly = members.every((member) => member.kind === 'POINT');
  return pointOnly ? [...ALL_METRICS] : ['MINIMUM_DISTANCE', 'MAXIMUM_DISTANCE'];
}

function medianTimings(samples: FrameDelivery[]): FrameTimings {
  const result = {} as FrameTimings;
  for (const key of TIMING_KEYS) {
    result[key] = median(samples.map((sample) => sample.timings[key]));
  }
  return result;
}

async function fetchScene(fileName: string, url: string): Promise<{ json: string; scene: GalleryScene }> {
  const response = await fetch(url, { cache: 'no-store' });
  if (!response.ok) {
    throw new Error(`Failed to load benchmark scene ${fileName} (${response.status})`);
  }
  const json = await response.text();
  return { json, scene: JSON.parse(json) as GalleryScene };
}

async function requestRepeated(
  request: BenchmarkOptions['request'],
  frameRequest: FrameRequest,
  warmups: number,
  measuredRuns: number
): Promise<FrameDelivery[]> {
  for (let i = 0; i < warmups; i++) {
    await request(frameRequest);
  }
  const samples: FrameDelivery[] = [];
  for (let i = 0; i < measuredRuns; i++) {
    samples.push(await request(frameRequest));
  }
  return samples;
}

function publishReport(report: BenchmarkReport): void {
  const target = document.createElement('pre');
  target.id = 'cvd-benchmark-results';
  target.hidden = true;
  target.dataset.complete = 'true';
  target.textContent = JSON.stringify(report);
  document.body.appendChild(target);

  (globalThis as typeof globalThis & { cvdBenchmarkResults?: BenchmarkReport })
    .cvdBenchmarkResults = report;
  console.info('CVD benchmark complete', report);
  console.table(
    report.results.map((result) => ({
      scene: result.scene,
      metric: result.metric,
      raster: result.raster,
      classifyMs: result.medians.classifyMs.toFixed(1),
      exportMs: result.medians.exportMs.toFixed(1),
      transferMs: result.medians.responseMs.toFixed(1),
      paintMs: result.medians.paintMs.toFixed(1),
      totalMs: result.medians.totalMs.toFixed(1),
    }))
  );
  console.info(`[cvd-benchmark-json] ${JSON.stringify(report)}`);
}

export async function runBenchmark(options: BenchmarkOptions): Promise<BenchmarkReport> {
  const scenes = await Promise.all(
    options.sceneFiles.map(async (fileName) => ({
      fileName,
      ...(await fetchScene(fileName, options.galleryUrl(fileName))),
    }))
  );
  const results: BenchmarkResult[] = [];
  const previewAfterFull: PreviewAfterFullResult[] = [];

  for (const { fileName, json, scene } of scenes) {
    const sceneName = scene.name?.trim() || fileName;
    options.onProgress?.(`Benchmark: loading ${sceneName}`);
    await options.request({
      width: 64,
      height: 64,
      actions: [{ type: 'loadSceneJson', json }],
    });

    for (const metric of compatibleMetrics(scene)) {
      for (const edge of BENCHMARK_EDGES) {
        options.onProgress?.(`Benchmark: ${sceneName}, ${metric}, ${edge}²`);
        const samples = await requestRepeated(
          options.request,
          {
            width: edge,
            height: edge,
            settings: {
              metricKind: metric,
              nearestNeighborK: scene.nearestNeighborK ?? 1,
            },
          },
          BENCHMARK_WARMUPS,
          BENCHMARK_RUNS
        );
        results.push({
          scene: sceneName,
          sceneFile: fileName,
          metric,
          raster: `${edge}×${edge}`,
          samples: samples.length,
          medians: medianTimings(samples),
        });
      }
    }

    options.onProgress?.(`Benchmark: ${sceneName}, preview after full`);
    await options.request({
      width: 1536,
      height: 1536,
      settings: { metricKind: 'MINIMUM_DISTANCE' },
    });
    const previewSamples = await requestRepeated(
      options.request,
      {
        width: 384,
        height: 384,
        settings: { metricKind: 'MINIMUM_DISTANCE' },
      },
      0,
      BENCHMARK_RUNS
    );
    const previewFrame = previewSamples.at(-1)!.frame;
    previewAfterFull.push({
      scene: sceneName,
      fullEdge: 1536,
      previewEdge: 384,
      previewMedians: medianTimings(previewSamples),
      argbLength: previewFrame.argb?.length ?? 0,
      ownersLength: previewFrame.owners?.length ?? 0,
      membersLength: previewFrame.members?.length ?? 0,
    });
  }

  const report: BenchmarkReport = {
    generatedAt: new Date().toISOString(),
    userAgent: navigator.userAgent,
    devicePixelRatio: window.devicePixelRatio || 1,
    viewport: `${window.innerWidth}×${window.innerHeight}`,
    warmups: BENCHMARK_WARMUPS,
    measuredRuns: BENCHMARK_RUNS,
    results,
    previewAfterFull,
  };
  publishReport(report);
  return report;
}
