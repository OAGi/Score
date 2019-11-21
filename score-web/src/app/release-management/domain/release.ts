export class SimpleRelease {
  releaseId: number;
  releaseNum: string;
}

export class ReleaseList {
  releaseId: number;
  releaseNum: string;
  state: string;
  namespace: string;
  lastUpdatedBy: string;
  lastUpdateTimestamp: Date;
}
