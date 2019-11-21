import {PageRequest} from '../../../basis/basis';

export class CcListRequest {
  releaseId: number;
  types: string[] = [];
  states: string[] = [];
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  filters: {
    den: string;
    definition: string;
    module: string;
  };
  page: PageRequest;

  constructor() {
    this.releaseId = 0;
    this.types = ['ACC', 'ASCCP', 'BCCP'];
    this.updatedDate = {
      start: null,
      end: null,
    };
    this.filters = {
      den: '',
      definition: '',
      module: '',
    };
  }
}

export class CcList {
  type: string;
  id: number;
  guid: string;
  den: string;
  definition: string;
  module: string;
  definitionSource: string;
  oagisComponentType: string;
  owner: string;
  state: string;
  revision: string;
  deprecated: boolean;
  currentId: number;
  lastUpdateUser: string;
  lastUpdateTimestamp: Date;
}

export class Asccp {
  asccpId: number;
  guid: string;
  propertyTerm: string;
  definition: string;
  definitionSource: number;
  roleOfAccId: string;
  den: string;

  createdBy: string;
  ownerUserId: string;
  lastUpdatedBy: string;
  creationTimestamp: string;
  lastUpdateTimestamp: string;

  state: string;
  moduleId: number;
  namespaceId: number;
  reusableIndicator: boolean;
  deprecated: boolean;
  revisionNum: string;
  revisionAction: string;
  releaseId: number;
  currentAsccpId: number;
  nillable: boolean;
}

export class Acc {
  accId: number;
  guid: string;
  objectClassTerm: string;
  den: string;
  definition: string;
  definitionSource: string;
  basedAccId: number;
  objectClassQualifier: string;
  oagisComponentType: string;

  moduleId: number;
  namespaceId: number;
  createdBy: string;
  ownerUserId: string;
  lastUpdatedBy: string;
  creationTimestamp: string;
  lastUpdateTimestamp: string;
  state: string;
  revisionNum: string;
  revisionAction: string;
  releaseId: number;
  currentAccId: number;
  deprecated: boolean;
  abstract: boolean;
}

export class Bcc {
  bccId: number;
  guid: string;
  cardinalityMin: string;
  cardinalityMax: string;
  toBccpId: number;
  fromAccId: number;
  seqKey: string;
  EntityType: string;
  den: string;

  definition: string;
  definitionSource: number;
  createdBy: string;
  ownerUserId: string;
  lastUpdatedBy: string;
  creationTimestamp: string;
  lastUpdateTimestamp: string;
  state: string;
  revisionNum: string;
  revisionTrackingNum: string;
  revisionAction: string;
  releaseId: number;
  currentBccId: number;
  deprecated: boolean;
  nillable: boolean;
  defaultValue: string;
}

export class Bccp {
  bccpId: number;
  guid: string;
  propertyTerm: string;
  representationTerm: string;
  bdtId: number;
  den: string;
  definition: string;
  definitionSource: number;
  moduleId: number;
  namespaceId: number;
  deprecated: boolean;

  createdBy: string;
  ownerUserId: string;
  lastUpdatedBy: string;
  creationTimestamp: string;
  lastUpdateTimestamp: string;

  state: string;
  revisionNum: string;
  revisionTrackingNum: string;
  revisionAction: string;
  releaseId: number;
  currentBccpId: number;
  nillable: boolean;
  defaultValue: string;
}

export class Ascc {
  asccId: number;
  guid: string;
  cardinalityMin: string;
  cardinalityMax: string;
  seqKey: string;
  fromAccId: number;
  toAsccpId: number;
  den: string;
  definition: string;
  definitionSource: number;
  deprecated: boolean;

  createdBy: string;
  ownerUserId: string;
  lastUpdatedBy: string;
  creationTimestamp: string;
  lastUpdateTimestamp: string;

  state: string;
  revisionNum: string;
  revisionTrackingNum: string;
  revisionAction: string;
  releaseId: number;
  currentAsccId: number;
}
