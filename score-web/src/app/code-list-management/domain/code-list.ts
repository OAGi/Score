import {PageRequest} from '../../basis/basis';
import {SimpleRelease} from '../../release-management/domain/release';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';

export class CodeListForListRequest {
  release: SimpleRelease;
  filters: {
    name: string;
    module: string;
    definition: string;
  };
  access: string;
  states: string[] = [];
  deprecated: boolean[] = [];
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
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

export class CodeListForList {
  codeListManifestId: number;
  codeListName: string;
  definition: string;
  definitionSource: string;
  modulePath: string;
  guid: string;
  basedCodeListManifestId: number;
  basedCodeListName: string;
  listId: string;
  agencyIdListValueManifestId: number;
  agencyIdListValueValue: string;
  agencyIdListValueName: string;
  versionId: string;
  lastUpdateTimestamp: Date;
  state: string;
  owner: string;
  access: string;
  revision: string;
  deprecated: boolean;
}

export class CodeList {
  releaseId: number;
  codeListManifestId: number;
  codeListName: string;
  basedCodeListManifestId: number;
  basedCodeListName: string;
  agencyIdListValueManifestId: number;
  agencyIdListValueValue: string;
  agencyIdListValueName: string;
  versionId: string;
  namespaceId: number;
  namespaceUri: string;

  guid: string;
  listId: string;
  definition: string;
  definitionSource: string;
  remark: string;

  deprecated: boolean;
  state: string;
  access: string;

  releaseNum: string;
  releaseState: string;
  revisionNum: string;
  owner: string;

  codeListValues: CodeListValue[];
}

export class CodeListValue {
  codeListValueManifestId: number;
  basedCodeListValueManifestId: number;
  guid: string;
  value: string;
  meaning: string;
  definition: string;
  definitionSource: string;

  deprecated: boolean;
  derived: boolean;
}

export class GetSimpleAgencyIdListValuesResponse {
  agencyIdLists: SimpleAgencyIdList[];
  agencyIdListValues: SimpleAgencyIdListValue[];
}

export class SimpleAgencyIdList {
  agencyIdListManifestId: number;
  name: string;
  state: string;
}

export class SimpleAgencyIdListValue {
  agencyIdListValueManifestId: number;
  agencyIdListManifestId: number;
  agencyIdListValueId: number;
  value: string;
  name: string;
}
