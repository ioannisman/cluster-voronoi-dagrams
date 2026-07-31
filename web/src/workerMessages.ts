import type { CvdAuthoringAction, CvdFrame, CvdSceneSettings } from './teavm.d.ts';

export type MoveHandleCmd = {
  index: number;
  worldX: number;
  worldY: number;
  /** When true (default), colocated endpoints move together. False = Shift detach. */
  coMove?: boolean;
};

export type WorkerStageTimings = {
  classifyMs: number;
  exportMs: number;
  normalizeMs: number;
  workerMs: number;
};

/** Main → worker */
export type WorkerRequest =
  | { type: 'init'; teavmUrl: string }
  | {
      type: 'frame';
      requestId: number;
      requestedAtEpochMs: number;
      width: number;
      height: number;
      move?: MoveHandleCmd;
      settings?: CvdSceneSettings;
      actions?: CvdAuthoringAction[];
    };

/** Worker → main */
export type WorkerResponse =
  | { type: 'ready' }
  | {
      type: 'frame';
      requestId: number;
      requestedAtEpochMs: number;
      postedAtEpochMs: number;
      timings: WorkerStageTimings;
      frame: CvdFrame;
    }
  | { type: 'error'; message: string; requestId?: number };
