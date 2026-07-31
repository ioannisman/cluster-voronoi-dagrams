export type CvdHandles = {
  x: Float64Array;
  y: Float64Array;
  cluster: Int32Array;
  member: Int32Array;
  within: Int32Array;
  /** 1 = visible, 0 = hidden (Uint8Array after worker normalize). */
  visible: Uint8Array | boolean[] | Int8Array;
  r: Float64Array;
  g: Float64Array;
  b: Float64Array;
  n: number;
};

export type CvdOverlays = {
  n: number;
  kind: string[];
  cluster: Int32Array | number[];
  member: Int32Array | number[];
  ax: Float64Array | number[];
  ay: Float64Array | number[];
  bx: Float64Array | number[];
  by: Float64Array | number[];
  radius: Float64Array | number[];
  ellipseX: Float64Array | number[];
  ellipseY: Float64Array | number[];
  ellipseStarts: Int32Array | number[];
};

export type CvdSceneState = {
  metricKind: string;
  neighborOrder: string;
  nearestNeighborK: number;
  shading: boolean;
  lastError: string;
  clusterCount: number;
  activeClusterIndex: number;
  activeMemberCount: number;
  siteMemberKind: string;
  selectedClusterIndex: number;
  selectedMemberIndex: number;
  selectedHandleIndex: number;
  clusterNames: string[];
};

export type CvdFrame = {
  argb?: Int32Array | number[];
  owners?: Int32Array | number[];
  members?: Int32Array | number[];
  width: number;
  height: number;
  handles: CvdHandles;
  overlays: CvdOverlays;
  scene: CvdSceneState;
  /** World box used for this classify (stamped by the worker). */
  worldView?: CvdWorldView;
  /** Site kind requested with this classify, if any (stamped by the worker). */
  requestedSiteMemberKind?: string;
};

export type CvdWorldView = {
  minX: number;
  maxX: number;
  minY: number;
  maxY: number;
};

export type CvdSceneSettings = {
  metricKind?: string;
  neighborOrder?: string;
  nearestNeighborK?: number;
  shading?: boolean;
  siteMemberKind?: string;
  worldView?: CvdWorldView;
};

export type CvdAuthoringAction =
  | { type: 'selectHandle'; index: number }
  | { type: 'clearSelection' }
  | { type: 'beginHandleDrag'; index: number }
  | { type: 'endHandleDrag' }
  | { type: 'cycleSelectedMember'; delta: number }
  | { type: 'cycleSelectedCluster'; delta: number }
  | { type: 'addMemberAt'; worldX: number; worldY: number }
  | { type: 'removeMember' }
  | { type: 'addCluster' }
  | { type: 'removeCluster' }
  | { type: 'loadSceneJson'; json: string };

export type CvdCore = {
  computeFrame: (width: number, height: number, preview: boolean, outputMask: number) => void;
  exportFrame: (outputMask: number) => CvdFrame;
  /** Convenience API retained for the standalone TeaVM smoke page. */
  renderFrame: (width: number, height: number) => CvdFrame;
  moveHandle: (index: number, worldX: number, worldY: number, coMove: boolean) => void;
  beginHandleDrag: (index: number) => void;
  endHandleDrag: () => void;
  setMetricKind: (name: string) => string;
  setNeighborOrder: (name: string) => string;
  setNearestNeighborK: (k: number) => string;
  setShadingEnabled: (enabled: boolean) => void;
  setWorldView: (minX: number, maxX: number, minY: number, maxY: number) => void;
  setActiveClusterIndex: (index: number) => string;
  setSiteMemberKind: (name: string) => string;
  selectHandle: (index: number) => void;
  cycleSelectedMember: (delta: number) => string;
  cycleSelectedCluster: (delta: number) => string;
  clearSelection: () => void;
  addMemberAt: (worldX: number, worldY: number) => string;
  removeMember: () => string;
  addCluster: () => string;
  removeCluster: () => string;
  loadSceneJson: (json: string) => string;
};

declare global {
  interface Window {
    cvdCore?: CvdCore;
  }
}

export {};
