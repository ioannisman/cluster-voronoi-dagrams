/// <reference lib="webworker" />

import type { CvdAuthoringAction, CvdCore, CvdSceneSettings } from './teavm.d.ts';
import {
  rasterOutputMask,
  type RasterOutputs,
  type WorkerRequest,
  type WorkerResponse,
} from './workerMessages';
import { normalizeFrame } from './normalizeFrame';

declare const self: DedicatedWorkerGlobalScope;

function post(msg: WorkerResponse, transfer: Transferable[] = []): void {
  self.postMessage(msg, transfer);
}

function epochNow(): number {
  return performance.timeOrigin + performance.now();
}

function assertRasterOutputs(
  frame: ReturnType<typeof normalizeFrame>,
  width: number,
  height: number,
  outputs: RasterOutputs
): void {
  const expectedLength = width * height;
  const checks = [
    ['argb', outputs.argb, frame.argb],
    ['owners', outputs.owners, frame.owners],
    ['members', outputs.members, frame.members],
  ] as const;
  for (const [name, requested, values] of checks) {
    if (requested && values?.length !== expectedLength) {
      throw new Error(
        `${name} raster length ${values?.length ?? 'missing'} does not match ${expectedLength}`
      );
    }
    if (!requested && values != null) {
      throw new Error(`${name} raster was returned without being requested`);
    }
  }
}

function cvdCore(): CvdCore {
  const core = (globalThis as typeof globalThis & { cvdCore?: CvdCore }).cvdCore;
  if (
    !core?.computeFrame ||
    !core.exportFrame ||
    !core?.renderFrame ||
    !core.moveHandle ||
    !core.beginHandleDrag ||
    !core.endHandleDrag ||
    !core.cycleSelectedMember ||
    !core.setMetricKind ||
    !core.addMemberAt ||
    !core.setWorldView ||
    !core.loadSceneJson
  ) {
    throw new Error('globalThis.cvdCore was not installed by TeaVM');
  }
  return core;
}

function applySettings(core: CvdCore, settings: CvdSceneSettings): void {
  if (settings.metricKind != null) {
    core.setMetricKind(settings.metricKind);
  }
  if (settings.neighborOrder != null) {
    core.setNeighborOrder(settings.neighborOrder);
  }
  if (settings.nearestNeighborK != null) {
    core.setNearestNeighborK(settings.nearestNeighborK);
  }
  if (settings.shading != null) {
    core.setShadingEnabled(settings.shading);
  }
  if (settings.worldView != null) {
    const w = settings.worldView;
    core.setWorldView(w.minX, w.maxX, w.minY, w.maxY);
  }
  if (settings.siteMemberKind != null) {
    core.setSiteMemberKind(settings.siteMemberKind);
  }
}

function applyActions(core: CvdCore, actions: CvdAuthoringAction[]): void {
  for (const action of actions) {
    switch (action.type) {
      case 'selectHandle':
        core.selectHandle(action.index);
        break;
      case 'clearSelection':
        core.clearSelection();
        break;
      case 'beginHandleDrag':
        core.beginHandleDrag(action.index);
        break;
      case 'endHandleDrag':
        core.endHandleDrag();
        break;
      case 'cycleSelectedMember':
        core.cycleSelectedMember(action.delta);
        break;
      case 'cycleSelectedCluster':
        core.cycleSelectedCluster(action.delta);
        break;
      case 'addMemberAt':
        core.addMemberAt(action.worldX, action.worldY);
        break;
      case 'removeMember':
        core.removeMember();
        break;
      case 'addCluster':
        core.addCluster();
        break;
      case 'removeCluster':
        core.removeCluster();
        break;
      case 'loadSceneJson': {
        const err = core.loadSceneJson(action.json);
        if (err) {
          throw new Error(err);
        }
        break;
      }
    }
  }
}

async function initTeavm(teavmUrl: string): Promise<void> {
  const mod = (await import(/* @vite-ignore */ teavmUrl)) as { main: () => void };
  mod.main();
  cvdCore();
}

self.onmessage = (event: MessageEvent<WorkerRequest>) => {
  const msg = event.data;
  void (async () => {
    try {
      if (msg.type === 'init') {
        await initTeavm(msg.teavmUrl);
        post({ type: 'ready' });
        return;
      }

      if (msg.type === 'frame') {
        const workerStart = performance.now();
        const core = cvdCore();
        if (msg.settings) {
          applySettings(core, msg.settings);
        }
        if (msg.actions && msg.actions.length > 0) {
          applyActions(core, msg.actions);
        }
        if (msg.move) {
          core.moveHandle(
            msg.move.index,
            msg.move.worldX,
            msg.move.worldY,
            msg.move.coMove !== false
          );
        }

        const outputMask = rasterOutputMask(msg.outputs);
        const classifyStart = performance.now();
        core.computeFrame(msg.width, msg.height, msg.preview, outputMask);
        const classifyMs = performance.now() - classifyStart;

        const exportStart = performance.now();
        const raw = core.exportFrame(outputMask);
        const exportMs = performance.now() - exportStart;

        const normalizeStart = performance.now();
        const frame = normalizeFrame(raw);
        assertRasterOutputs(frame, msg.width, msg.height, msg.outputs);
        const normalizeMs = performance.now() - normalizeStart;
        // Stamp request settings so the UI can ignore stale frames when syncing controls.
        if (msg.settings?.worldView != null) {
          frame.worldView = { ...msg.settings.worldView };
        }
        if (msg.settings?.siteMemberKind != null) {
          frame.requestedSiteMemberKind = msg.settings.siteMemberKind;
        }
        const response: WorkerResponse = {
          type: 'frame',
          requestId: msg.requestId,
          requestedAtEpochMs: msg.requestedAtEpochMs,
          postedAtEpochMs: epochNow(),
          timings: {
            classifyMs,
            exportMs,
            normalizeMs,
            workerMs: performance.now() - workerStart,
          },
          frame,
        };
        const transfer: Transferable[] = [];
        if (frame.argb instanceof Int32Array) transfer.push(frame.argb.buffer);
        if (frame.owners instanceof Int32Array) transfer.push(frame.owners.buffer);
        if (frame.members instanceof Int32Array) transfer.push(frame.members.buffer);
        post(response, transfer);
      }
    } catch (err: unknown) {
      post({
        type: 'error',
        message: err instanceof Error ? err.message : String(err),
        requestId: msg.type === 'frame' ? msg.requestId : undefined,
      });
    }
  })();
};
