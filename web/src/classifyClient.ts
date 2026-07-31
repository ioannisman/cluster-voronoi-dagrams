import type { CvdAuthoringAction, CvdFrame, CvdSceneSettings } from './teavm.d.ts';
import type {
  MoveHandleCmd,
  WorkerRequest,
  WorkerResponse,
  WorkerStageTimings,
} from './workerMessages';

export type FrameRequest = {
  width: number;
  height: number;
  move?: MoveHandleCmd;
  settings?: CvdSceneSettings;
  actions?: CvdAuthoringAction[];
};

export type FrameTimings = WorkerStageTimings & {
  responseMs: number;
  argbMs: number;
  contourMs: number;
  paintMs: number;
  totalMs: number;
};

export type FrameDelivery = {
  requestId: number;
  requestedAtEpochMs: number;
  receivedAtEpochMs: number;
  frame: CvdFrame;
  timings: FrameTimings;
};

type Waiter = {
  resolve: (delivery: FrameDelivery) => void;
  reject: (error: Error) => void;
};

type PendingFrame = {
  request: FrameRequest;
  requestedAtEpochMs: number;
  waiters: Waiter[];
};

type InFlightFrame = {
  requestId: number;
  requestedAtEpochMs: number;
  waiters: Waiter[];
};

function epochNow(): number {
  return performance.timeOrigin + performance.now();
}

/**
 * Talks to {@link ./classifyWorker.ts}: loads TeaVM once, coalesces in-flight
 * frame requests so drag spam keeps only the latest pending update.
 */
export class ClassifyClient {
  onFrame: ((delivery: FrameDelivery) => void) | null = null;
  onError: ((err: Error) => void) | null = null;

  private readonly worker: Worker;
  private readonly readyPromise: Promise<void>;
  private resolveReady!: () => void;
  private rejectReady!: (err: Error) => void;

  private nextRequestId = 1;
  private busy = false;
  private pending: PendingFrame | null = null;
  private inFlight: InFlightFrame | null = null;
  private readySettled = false;

  constructor(teavmUrl: string) {
    this.readyPromise = new Promise<void>((resolve, reject) => {
      this.resolveReady = resolve;
      this.rejectReady = reject;
    });

    this.worker = new Worker(new URL('./classifyWorker.ts', import.meta.url), {
      type: 'module',
    });

    this.worker.onmessage = (event: MessageEvent<WorkerResponse>) => {
      const msg = event.data;
      if (msg.type === 'ready') {
        if (!this.readySettled) {
          this.readySettled = true;
          this.resolveReady();
        }
        return;
      }
      if (msg.type === 'error') {
        const err = new Error(msg.message);
        if (!this.readySettled) {
          this.readySettled = true;
          this.rejectReady(err);
        }
        this.onError?.(err);
        if (msg.requestId != null) {
          if (this.inFlight?.requestId === msg.requestId) {
            for (const waiter of this.inFlight.waiters) {
              waiter.reject(err);
            }
            this.inFlight = null;
          }
          this.busy = false;
          this.pump();
        }
        return;
      }
      if (msg.type === 'frame') {
        const receivedAtEpochMs = epochNow();
        const inFlight = this.inFlight;
        if (!inFlight || inFlight.requestId !== msg.requestId) {
          this.onError?.(new Error(`Unexpected classify response ${msg.requestId}`));
          return;
        }
        const delivery: FrameDelivery = {
          requestId: msg.requestId,
          requestedAtEpochMs: inFlight.requestedAtEpochMs,
          receivedAtEpochMs,
          frame: msg.frame,
          timings: {
            ...msg.timings,
            responseMs: Math.max(0, receivedAtEpochMs - msg.postedAtEpochMs),
            argbMs: 0,
            contourMs: 0,
            paintMs: 0,
            totalMs: 0,
          },
        };
        let callbackError: Error | null = null;
        try {
          this.onFrame?.(delivery);
        } catch (err: unknown) {
          callbackError = err instanceof Error ? err : new Error(String(err));
          this.onError?.(callbackError);
        }
        for (const waiter of inFlight.waiters) {
          if (callbackError) {
            waiter.reject(callbackError);
          } else {
            waiter.resolve(delivery);
          }
        }
        this.inFlight = null;
        this.busy = false;
        this.pump();
      }
    };

    this.worker.onerror = (event) => {
      const err = new Error(event.message || 'Classify worker failed');
      if (!this.readySettled) {
        this.readySettled = true;
        this.rejectReady(err);
      }
      this.onError?.(err);
      for (const waiter of this.inFlight?.waiters ?? []) {
        waiter.reject(err);
      }
      this.inFlight = null;
      this.busy = false;
    };

    this.post({ type: 'init', teavmUrl });
  }

  whenReady(): Promise<void> {
    return this.readyPromise;
  }

  /** Queue a classify pass; coalesces pending work without dropping authoring actions. */
  enqueue(req: FrameRequest): void {
    this.enqueueInternal(req, null);
  }

  /** Queue a pass and resolve after its coalesced frame has been painted. */
  enqueueAndWait(req: FrameRequest): Promise<FrameDelivery> {
    return new Promise<FrameDelivery>((resolve, reject) => {
      this.enqueueInternal(req, { resolve, reject });
    });
  }

  private enqueueInternal(req: FrameRequest, waiter: Waiter | null): void {
    const requestedAtEpochMs = epochNow();
    if (this.pending == null) {
      this.pending = {
        request: { ...req, actions: req.actions ? [...req.actions] : undefined },
        requestedAtEpochMs,
        waiters: waiter ? [waiter] : [],
      };
    } else {
      const pendingRequest = this.pending.request;
      this.pending = {
        request: {
          width: req.width,
          height: req.height,
          move: req.move ?? pendingRequest.move,
          settings: { ...pendingRequest.settings, ...req.settings },
          actions: [...(pendingRequest.actions ?? []), ...(req.actions ?? [])],
        },
        requestedAtEpochMs,
        waiters: [...this.pending.waiters, ...(waiter ? [waiter] : [])],
      };
      if (this.pending.request.actions?.length === 0) {
        this.pending.request.actions = undefined;
      }
    }
    this.pump();
  }

  terminate(): void {
    this.worker.terminate();
  }

  private post(msg: WorkerRequest): void {
    this.worker.postMessage(msg);
  }

  private pump(): void {
    if (this.busy || this.pending == null) {
      return;
    }
    const pending = this.pending;
    const req = pending.request;
    this.pending = null;
    this.busy = true;
    const requestId = this.nextRequestId++;
    this.inFlight = {
      requestId,
      requestedAtEpochMs: pending.requestedAtEpochMs,
      waiters: pending.waiters,
    };
    this.post({
      type: 'frame',
      requestId,
      requestedAtEpochMs: pending.requestedAtEpochMs,
      width: req.width,
      height: req.height,
      move: req.move,
      settings: req.settings,
      actions: req.actions,
    });
  }
}
