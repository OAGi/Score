export class BieCreateList {
  asccpId: number;
  releaseId: number;
  propertyTerm: string;
  guid: string;
  module: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
}

export interface Release {
  releaseId: number;
  releaseNum: string;
}

export interface BieCreateResponse {
  topLevelAbieId: number;
}
