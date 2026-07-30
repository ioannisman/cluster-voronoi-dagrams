/// <reference lib="webworker" />

import type { CvdAuthoringAction, CvdCore, CvdSceneSettings } from './teavm.d.ts';
import type { WorkerRequest, WorkerResponse } from './workerMessages';
import { normalizeFrame } from './normalizeFrame';

declare const self: DedicatedWorkerGlobalScope;

function post(msg: WorkerResponse): void {
  self.postMessage(msg);
}

function cvdCore(): CvdCore {
  const core = (globalThis as typeof globalThis & { cvdCore?: CvdCore }).cvdCore;
  if (
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
        const t0 = performance.now();
        const raw = core.renderFrame(msg.width, msg.height);
        const frame = normalizeFrame(raw);
        // Stamp request settings so the UI can ignore stale frames when syncing controls.
        if (msg.settings?.worldView != null) {
          frame.worldView = { ...msg.settings.worldView };
        }
        if (msg.settings?.siteMemberKind != null) {
          frame.requestedSiteMemberKind = msg.settings.siteMemberKind;
        }
        const ms = performance.now() - t0;
        post({ type: 'frame', requestId: msg.requestId, ms, frame });
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
