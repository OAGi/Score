import {WhoAndWhen} from '../../basis/basis';

export class ReleaseSummary {
  releaseId: number;
  releaseNum: string;
  state: string;
  workingRelease: boolean;
}

export class ReleaseListEntry {
  releaseId: number;
  guid: string;
  releaseNum: string;
  state: string;
  namespaceId: number;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ReleaseDetails {
  releaseId: number;
  releaseNum: string;
  releaseNote: string;
  releaseLicense: string;
  state: string;
  libraryId: number;
  libraryName: string;
  namespaceId: number;
  latestRelease: boolean;
}

export class ReleaseResponse {
  release: ReleaseDetails;
  status: string;
  statusMessage: string;
}

export const WorkingRelease = {
  releaseNum: 'Working',
  state: '',
  workingRelease: true
};

export class AssignableMap {
  assignableAccManifestMap: Map<number, AssignableNode>;
  assignableAsccpManifestMap: Map<number, AssignableNode>;
  assignableBccpManifestMap: Map<number, AssignableNode>;
  assignableDtManifestMap: Map<number, AssignableNode>;
  assignableCodeListManifestMap: Map<number, AssignableNode>;
  assignableAgencyIdListManifestMap: Map<number, AssignableNode>;
  assignableXbtManifestMap: Map<number, AssignableNode>;

  assignedAccManifestMap: Map<number, AssignableNode>;
  assignedAsccpManifestMap: Map<number, AssignableNode>;
  assignedBccpManifestMap: Map<number, AssignableNode>;
  assignedDtManifestMap: Map<number, AssignableNode>;
  assignedCodeListManifestMap: Map<number, AssignableNode>;
  assignedAgencyIdListManifestMap: Map<number, AssignableNode>;
  assignedXbtManifestMap: Map<number, AssignableNode>;
}

export class AssignableNode {
  manifestId: number;
  den: string;
  type: string;
  state: string;
  revision: number;
  ownerUsername: string;
  timestamp: Date;
  visible = true;
  errors: ValidationMessage[];
}

export class AssignableList {
  assignableList: AssignableNode[];
  assignedList: AssignableNode[];

  constructor() {
    this.assignableList = [];
    this.assignedList = [];
  }
}

export class ReleaseValidationRequest {
  assignedAccComponentManifestIds: number[] = [];
  assignedAsccpComponentManifestIds: number[] = [];
  assignedBccpComponentManifestIds: number[] = [];
  assignedCodeListComponentManifestIds: number[] = [];
  assignedAgencyIdListComponentManifestIds: number[] = [];
  assignedDtComponentManifestIds: number[] = [];
}

export class ValidationMessage {
  level: string;
  message: string;
  code: string;
}

export class ReleaseValidationResponse {
  succeed: boolean;
  statusMapForAcc: Map<number, ValidationMessage[]>;
  statusMapForAsccp: Map<number, ValidationMessage[]>;
  statusMapForBccp: Map<number, ValidationMessage[]>;
  statusMapForCodeList: Map<number, ValidationMessage[]>;
  statusMapForAgencyIdList: Map<number, ValidationMessage[]>;
  statusMapForDt: Map<number, ValidationMessage[]>;
}
