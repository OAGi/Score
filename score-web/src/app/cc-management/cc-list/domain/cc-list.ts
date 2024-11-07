import {PageRequest} from '../../../basis/basis';
import {OagisComponentType, OagisComponentTypes} from '../../domain/core-component-node';
import {SimpleRelease} from '../../../release-management/domain/release';
import {HttpParams} from '@angular/common/http';
import {ParamMap} from '@angular/router';
import {base64Decode, base64Encode} from '../../../common/utility';
import {ShortTag} from '../../../tag-management/domain/tag';

export class CcListRequest {
  release: SimpleRelease;
  types: string[] = [];
  states: string[] = [];
  deprecated: boolean[] = [false];
  newComponent: boolean[] = [];
  commonlyUsed: boolean[] = [true];
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  excludes: string[] = [];
  findUsages: {
    type: string,
    manifestId: number
  };
  componentTypes: OagisComponentType[];
  tags: string[];
  namespaces: number[] = [];

  dtTypes: string[] = [];
  asccpTypes: string[] = [];

  updatedDate: {
    start: Date,
    end: Date,
  };
  filters: {
    den: string;
    definition: string;
    module: string;
  };
  isBIEUsable: boolean;
  fuzzySearch = false;
  cookieType: string;
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
    this.release = new SimpleRelease();
    this.release.releaseId = Number(params.get('releaseId') || 0);
    this.page.sortActive = params.get('sortActive');
    if (this.page.sortActive === 'undefined') {
      this.page.sortActive = '';
    }
    if (this.page.sortActive !== '' && !this.page.sortActive) {
      this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    }
    this.page.sortDirection = params.get('sortDirection');
    if (this.page.sortDirection === 'undefined') {
      this.page.sortDirection = '';
    }
    if (this.page.sortDirection !== '' && !this.page.sortDirection) {
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
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.deprecated = (params.get('deprecated')) ? Array.from(params.get('deprecated').split(',').map(e => e === 'true' ? true : false)) : [];
    this.newComponent = (params.get('newComponent')) ? Array.from(params.get('newComponent').split(',').map(e => e === 'true' ? true : false)) : [];
    this.commonlyUsed = (params.get('commonlyUsed')) ? Array.from(params.get('commonlyUsed').split(',').map(e => e === 'true' ? true : false)) : [];
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.tags = (params.get('tags')) ? Array.from(params.get('tags').split(',')) : [];
    this.namespaces = (params.get('namespaces')) ? Array.from(params.get('namespaces').split(',')).map(e => Number(e)) : [];
    this.componentTypes = (params.get('componentTypes')) ? Array.from(params.get('componentTypes').split(','))
      .map(elm => OagisComponentTypes[elm]) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      den: params.get('den') || '',
      definition: params.get('definition') || '',
      module: params.get('module') || '',
    };
    this.cookieType = params.get('cookieType') || 'CC';
    this.excludes = [];
    this.findUsages = {
      type: '',
      manifestId: 0
    };
    this.isBIEUsable = false;
  }

  toQuery(extras?): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    params = params.set('releaseId', '' + this.release.releaseId);
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.deprecated !== undefined && this.deprecated.length > 0) {
      params = params.set('deprecated', this.deprecated.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.newComponent !== undefined && this.newComponent.length > 0) {
      params = params.set('newComponent', this.newComponent.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.commonlyUsed !== undefined && this.commonlyUsed.length > 0) {
      params = params.set('commonlyUsed', this.commonlyUsed.map(e => (e) ? 'true' : 'false').join(','));
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
    if (this.tags && this.tags.length > 0) {
      params = params.set('tags', this.tags.join(','));
    }
    if (this.namespaces && this.namespaces.length > 0) {
      params = params.set('namespaces', this.namespaces.map(e => '' + e).join(','));
    }
    if (this.componentTypes && this.componentTypes.length > 0) {
      params = params.set('componentTypes', this.componentTypes
        .map((elm: OagisComponentType) => elm.value).join(','));
    }
    if (this.findUsages.type && this.findUsages.manifestId > 0) {
      params = params.set('findUsagesType', this.findUsages.type)
        .set('findUsagesManifestId', '' + this.findUsages.manifestId);
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
  manifestId: number;
  guid: string;
  name: string;
  den: string;
  definition: string;
  module: string;
  definitionSource: string;
  oagisComponentType: string;
  dtType: string;
  owner: string;
  state: string;
  revision: string;
  releaseNum: string;
  deprecated: boolean;
  lastUpdateUser: string;
  lastUpdateTimestamp: Date;
  id: number;
  ownedByDeveloper: boolean;
  sixDigitId: string;
  basedManifestId: number;
  defaultValueDomain: number;
  newComponent: boolean;
  tagList: ShortTag[];
}

export class CcUpdateStateListRequest {
  type: string;
  manifestId: number;
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
}

export class SummaryCcInfo {
  numberOfTotalCcByStates: Map<string, number>;
  numberOfMyCcByStates: Map<string, number>;
  ccByUsersAndStates: Map<string, Map<string, number>>;
  myRecentCCs: CcList[] = [];
}

export class SummaryCcExt {
  accId: number;
  accManifestId: number;
  releaseId: number;
  releaseNum: string;
  guid: string;
  objectClassTerm: string;
  state: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;

  ownerUsername: string;
  ownerUserId: number;

  topLevelAsbiepId: number;
  den: string;
  associationDen: string;
  seqKey: number;
}

export class SummaryCcExtInfo {
  numberOfTotalCcExtByStates: Map<string, number>;
  numberOfMyCcExtByStates: Map<string, number>;
  ccExtByUsersAndStates: Map<string, Map<string, number>>;
  myExtensionsUnusedInBIEs: SummaryCcExt[];
}

export class CcChangeResponse {
  ccChangeList: CcChange[];
}

export class CcChange {
  type: string;
  manifestId;
  number;
  den: string;
  changeType: string;
  tagList: ShortTag[];
}
