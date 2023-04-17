import {ScoreUser} from '../../authentication/domain/auth';
import {PageRequest} from '../../basis/basis';
import {SimpleRelease} from '../../release-management/domain/release';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode, hashCode4String} from '../../common/utility';

export class AgencyIdListForListRequest {
  release: SimpleRelease;
  filters: {
    name: string;
    module: string;
    definition: string;
  };
  access: string;
  states: string[] = [];
  deprecated: boolean[] = [];
  newComponent: boolean[] = [];
  extensible: boolean;
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  namespaces: number[] = [];
  ownedByDeveloper: boolean;
  cookieType: string;
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
    this.release = new SimpleRelease();
    this.release.releaseId = Number(params.get('releaseId') || 0);
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

    this.access = params.get('access') || '';
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.deprecated = (params.get('deprecated')) ? Array.from(params.get('deprecated').split(',').map(e => e === 'true' ? true : false)) : [];
    this.newComponent = (params.get('newComponent')) ? Array.from(params.get('newComponent').split(',').map(e => e === 'true' ? true : false)) : [];
    this.extensible = (params.get('extensible')) ? (('true' === params.get('extensible'))) : undefined;
    this.namespaces = (params.get('namespaces')) ? Array.from(params.get('namespaces').split(',')).map(e => Number(e)) : [];
    this.ownedByDeveloper = (params.get('ownedByDeveloper')) ? (('true' === params.get('ownedByDeveloper'))) : undefined;
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      name: params.get('name') || '',
      definition: params.get('definition') || '',
      module: params.get('module') || ''
    };
    this.cookieType = params.get('cookieType') || 'CC';
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.release) {
      params = params.set('releaseId', this.release.releaseId.toString());
    }
    if (this.access && this.access.length > 0) {
      params = params.set('access', '' + this.access);
    }
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.deprecated !== undefined && this.deprecated.length > 0) {
      params = params.set('deprecated', this.deprecated.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.newComponent !== undefined && this.newComponent.length > 0) {
      params = params.set('newComponent', this.newComponent.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.extensible !== undefined) {
      params = params.set('extensible', (this.extensible) ? 'true' : 'false');
    }
    if (this.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', (this.ownedByDeveloper) ? 'true' : 'false');
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
    if (this.namespaces && this.namespaces.length > 0) {
      params = params.set('namespaces', this.namespaces.map(e => '' + e).join(','));
    }
    if (this.filters.name && this.filters.name.length > 0) {
      params = params.set('name', '' + this.filters.name);
    }
    if (this.filters.definition && this.filters.definition.length > 0) {
      params = params.set('definition', '' + this.filters.definition);
    }
    if (this.filters.module && this.filters.module.length > 0) {
      params = params.set('module', '' + this.filters.module);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class AgencyIdList {
  releaseId: number;
  agencyIdListManifestId: number;
  agencyIdListValueManifestId: number;
  name: string;
  basedAgencyIdListManifestId: number;
  basedAgencyIdListName: string;
  agencyId: number;
  versionId: string;
  namespaceId: number;
  namespaceUri: string;

  guid: string;
  listId: string;
  definition: string;
  definitionSource: string;
  remark: string;

  deprecated: boolean;
  newComponent: boolean;
  state: string;
  access: string;

  releaseNum: string;
  releaseState: string;
  revisionNum: string;
  owner: ScoreUser;
  lastUpdatedBy: ScoreUser;

  lastUpdateTimestamp: Date;

  values: AgencyIdListValue[];
  prev: AgencyIdList;

  prevAgencyIdListManifestId: number;

  constructor(obj?: AgencyIdList) {
    if (!!obj) {
      this.releaseId = obj.releaseId;
      this.agencyIdListManifestId = obj.agencyIdListManifestId;
      this.agencyIdListValueManifestId = obj.agencyIdListValueManifestId;
      this.name = obj.name;
      this.basedAgencyIdListManifestId = obj.basedAgencyIdListManifestId;
      this.basedAgencyIdListName = obj.basedAgencyIdListName;
      this.agencyId = obj.agencyId;
      this.versionId = obj.versionId;
      this.namespaceId = obj.namespaceId;
      this.namespaceUri = obj.namespaceUri;

      this.guid = obj.guid;
      this.listId = obj.listId;
      this.definition = obj.definition;
      this.definitionSource = obj.definitionSource;
      this.remark = obj.remark;

      this.deprecated = obj.deprecated;
      this.state = obj.state;
      this.access = obj.access;

      this.releaseNum = obj.releaseNum;
      this.releaseState = obj.releaseState;
      this.revisionNum = obj.revisionNum;
      this.owner = obj.owner;
      this.lastUpdatedBy = obj.lastUpdatedBy;

      this.lastUpdateTimestamp = obj.lastUpdateTimestamp;

      this.values = (obj.values) ? obj.values.map(val => new AgencyIdListValue(val)) : [];
      this.prev = obj.prev;

      this.prevAgencyIdListManifestId = obj.prevAgencyIdListManifestId;
    }
  }

  get hashCode(): number {
    return ((this.releaseId) ? this.releaseId : 0) +
      ((this.agencyIdListManifestId) ? this.agencyIdListManifestId : 0) +
      ((this.agencyIdListValueManifestId) ? this.agencyIdListValueManifestId : 0) +
      ((this.name) ? hashCode4String(this.name) : 0) +
      ((this.basedAgencyIdListManifestId) ? this.basedAgencyIdListManifestId : 0) +
      ((this.agencyId) ? this.agencyId : 0) +
      ((this.versionId) ? hashCode4String(this.versionId) : 0) +
      ((this.namespaceId) ? this.namespaceId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.listId) ? hashCode4String(this.listId) : 0) +
      ((this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((this.remark) ? hashCode4String(this.remark) : 0) +
      ((this.deprecated) ? 1231 : 1237) +
      ((this.state) ? hashCode4String(this.state) : 0) +
      ((this.values) ? this.values.map(val => val.hashCode).reduce((s, a) => s + a, 0) : 0);
  }
}

export class AgencyIdListValue {
  agencyIdListValueManifestId: number;
  basedAgencyIdListValueManifestId: number;
  guid: string;
  value: string;
  name: string;
  definition: string;
  definitionSource: string;

  deprecated: boolean;
  used: boolean;

  get derived(): boolean {
    return !!this.basedAgencyIdListValueManifestId;
  }

  constructor(obj?: AgencyIdListValue) {
    if (!!obj) {
      this.agencyIdListValueManifestId = obj.agencyIdListValueManifestId;
      this.basedAgencyIdListValueManifestId = obj.basedAgencyIdListValueManifestId;
      this.guid = obj.guid;
      this.value = obj.value;
      this.name = obj.name;
      this.definition = obj.definition;
      this.definitionSource = obj.definitionSource;

      this.deprecated = obj.deprecated;
      this.used = obj.used;
    }
  }

  get hashCode(): number {
    return ((this.agencyIdListValueManifestId) ? this.agencyIdListValueManifestId : 0) +
      ((this.basedAgencyIdListValueManifestId) ? this.basedAgencyIdListValueManifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.value) ? hashCode4String(this.value) : 0) +
      ((this.name) ? hashCode4String(this.name) : 0) +
      ((this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((this.deprecated) ? 1231 : 1237) +
      ((this.used) ? 1231 : 1237);
  }
}

export class SimpleAgencyIdListValue {
  agencyIdListValueId: number;
  name: string;
}
