import {PageRequest} from '../../../basis/basis';
import {HttpParams} from '@angular/common/http';
import {ParamMap} from '@angular/router';
import {base64Decode, base64Encode} from '../../../common/utility';

export class CcListRequest {
  releaseId: number;
  types: string[] = [];
  states: string[] = [];
  deprecated: boolean[] = [false];
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  componentType: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  filters: {
    den: string;
    definition: string;
    module: string;
  };
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

    this.releaseId = Number(params.get('releaseId') || 0);
    this.page.sortActive = params.get('sortActive');
    if (!this.page.sortActive) {
      this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    }
    this.page.sortDirection = params.get('sortDirection');
    if (!this.page.sortDirection) {
      this.page.sortDirection = (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    }
    if (params.get('pageIndex')) {
      this.page.pageIndex = Number(params.get('pageIndex'));
    } else {
      this.page.pageIndex = (defaultPageRequest) ? defaultPageRequest.pageIndex : 0;
    }
    if (params.get('pageSize')) {
      this.page.pageSize = Number(params.get('pageSize'));
    } else {
      this.page.pageSize = (defaultPageRequest) ? defaultPageRequest.pageSize : 0;
    }
    this.types = (params.get('types')) ? Array.from(params.get('types').split(',').map(e => e.toUpperCase())) : ['ACC', 'ASCCP', 'BCCP'];
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.deprecated = (params.get('deprecated')) ? [(('true' === params.get('deprecated')) ? true : false)] : undefined;
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.componentType = (params.get('componentType')) ? Array.from(params.get('componentType').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      den: params.get('den') || '',
      definition: params.get('definition') || '',
      module: params.get('module') || '',
    };
  }

  toQuery(extras?): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    params = params.set('releaseId', '' + this.releaseId);
    if (this.types && this.types.length > 0) {
      params = params.set('types', this.types.join(','));
    }
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.deprecated !== undefined) {
      params = params.set('deprecated', (this.deprecated) ? 'true' : 'false');
    }
    if (this.ownerLoginIds && this.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', this.ownerLoginIds.join(','));
    }
    if (this.updaterLoginIds && this.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', this.updaterLoginIds.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.den && this.filters.den.length > 0) {
      params = params.set('den', '' + this.filters.den);
    }
    if (this.filters.definition && this.filters.definition.length > 0) {
      params = params.set('definition', '' + this.filters.definition);
    }
    if (this.filters.module && this.filters.module.length > 0) {
      params = params.set('module', '' + this.filters.module);
    }
    if (this.componentType && this.componentType.length > 0) {
      params = params.set('componentType', this.componentType.join(','));
    }
    if (extras) {
      Object.keys(extras).forEach(key => {
        params = params.set(key.toString(), extras[key]);
      });
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
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

export class SummaryCcExt {
  accId: number;
  guid: string;
  objectClassTerm: string;
  state: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;

  ownerUsername: string;
  ownerUserId: number;

  topLevelAsbiepId: number;
  propertyTerm: string;
  associationPropertyTerm: string;
  seqKey: number;
}

export class SummaryCcExtInfo {
  numberOfTotalCcExtByStates: Map<string, number>;
  numberOfMyCcExtByStates: Map<string, number>;
  ccExtByUsersAndStates: Map<string, Map<string, number>>;
  myExtensionsUnusedInBIEs: SummaryCcExt[];
}
