import {Definition, PageRequest, WhoAndWhen} from '../../basis/basis';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';
import {LibrarySummary} from '../../library-management/domain/library';
import {LogSummary} from '../../log-management/domain/log';
import {ScoreUser} from '../../authentication/domain/auth';
import {AgencyIdListValueSummary} from '../../agency-id-list-management/domain/agency-id-list';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';

export class CodeListListEntryRequest {
  library: LibrarySummary = new LibrarySummary();
  release: ReleaseSummary;
  filters: {
    name: string;
    module: string;
    definition: string;
  };
  access: string;
  states: string[] = [];
  deprecated: boolean[] = [];
  extensible: boolean[] = [];
  newComponent: boolean[] = [];
  ownerLoginIdList: string[] = [];
  updaterLoginIdList: string[] = [];
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
    this.release = new ReleaseSummary();
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
    this.extensible = (params.get('extensible')) ? Array.from(params.get('extensible').split(',').map(e => e === 'true' ? true : false)) : [];
    this.newComponent = (params.get('newComponent')) ? Array.from(params.get('newComponent').split(',').map(e => e === 'true' ? true : false)) : [];
    this.ownedByDeveloper = (params.get('ownedByDeveloper')) ? (('true' === params.get('ownedByDeveloper'))) : undefined;
    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.namespaces = (params.get('namespaces')) ? Array.from(params.get('namespaces').split(',')).map(e => Number(e)) : [];
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

    params = params.set('releaseId', '' + this.release.releaseId);
    if (this.access && this.access.length > 0) {
      params = params.set('access', '' + this.access);
    }
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.deprecated !== undefined && this.deprecated.length > 0) {
      params = params.set('deprecated', this.deprecated.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.extensible !== undefined && this.extensible.length > 0) {
      params = params.set('extensible', this.extensible.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.newComponent !== undefined && this.newComponent.length > 0) {
      params = params.set('newComponent', this.newComponent.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', (this.ownedByDeveloper) ? 'true' : 'false');
    }
    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
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

export class CodeListSummary {

  codeListManifestId: number;
  codeListId: number;
  guid: string;

  name: string;
  listId: string;
  versionId: string;

  deprecated: boolean;
  state: string;

  valueList: CodeListValueSummary[];

}

export class CodeListValueSummary {

  codeListValueManifestId: number;
  codeListValueId: number;
  guid: string;

  value: string;
  name: string;

}

export class CodeListListEntry {

  library: LibrarySummary;
  release: ReleaseSummary;

  codeListManifestId: number;
  codeListId: number;
  guid: string;
  enumTypeGuid: string;

  based: CodeListSummary;
  agencyIdListValue: AgencyIdListValueSummary;

  name: string;
  listId: string;
  versionId: string;
  definition: string;
  definitionSource: string;
  remark: string;

  namespaceId: number;

  deprecated: boolean;
  extensible: boolean;
  newComponent: boolean;
  state: string;
  access: string;

  log: LogSummary;
  module: string;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

}

export class CodeListDetails {

  library: LibrarySummary = new LibrarySummary();
  release: ReleaseSummary = new ReleaseSummary();

  codeListManifestId: number;
  codeListId: number;
  guid: string;
  enumTypeGuid: string;

  based: CodeListSummary = new CodeListSummary();
  agencyIdListValue: AgencyIdListValueSummary = new AgencyIdListValueSummary();

  name: string;
  listId: string;
  versionId: string;
  definition: Definition = new Definition();
  remark: string;

  namespace: NamespaceSummary = new NamespaceSummary();

  deprecated: boolean;
  extensible: boolean;
  newComponent: boolean;
  state: string;
  access: string;

  log: LogSummary = new LogSummary();
  module: string;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  valueList: CodeListValueDetails[] = [];

}

export class CodeListValueDetails {

  codeListValueManifestId: number;
  codeListValueId: number;
  guid: string;

  value: string;
  meaning: string;

  definition: Definition = new Definition();

  deprecated: boolean;
  used: boolean;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

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
  newComponent: boolean;
}

export class CodeList {
  libraryId: number;
  releaseId: number;
  workingRelease: boolean;
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
  definition: Definition;

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

export class CodeListCreateResponse {
  codeListManifestId: number;
}