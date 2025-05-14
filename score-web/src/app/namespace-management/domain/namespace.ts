import {PageRequest, WhoAndWhen} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {base64Decode, base64Encode} from '../../common/utility';
import {HttpParams} from '@angular/common/http';
import {LibrarySummary} from '../../library-management/domain/library';
import {ScoreUser} from '../../authentication/domain/auth';

export class NamespaceDetails {
  namespaceId: number;
  libraryId: number;
  uri: string;
  prefix: string;
  description: string;
  standard: boolean;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  canEdit: boolean;
}

export class NamespaceListEntry {
  namespaceId: number;
  uri: string;
  prefix: string;
  description: string;
  standard: boolean;

  owner: ScoreUser;
  lastUpdated: WhoAndWhen;
}

export class NamespaceSummary {
  namespaceId: number;
  uri: string;
  standard: boolean;
}

export class NamespaceListRequest {
  library: LibrarySummary = new LibrarySummary();
  filters: {
    uri: string;
    prefix: string;
    description: string;
  };
  ownerLoginIdList: string[] = [];
  updaterLoginIdList: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  standard: string[] = [];
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
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

    this.ownerLoginIdList = (params.get('ownerLoginIdList')) ? Array.from(params.get('ownerLoginIdList').split(',')) : [];
    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.standard = (params.get('standard')) ? Array.from(params.get('standard').split(',')) : [];
    this.filters = {
      uri: params.get('uri') || '',
      prefix: params.get('prefix') || '',
      description: params.get('description') || '',
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.ownerLoginIdList && this.ownerLoginIdList.length > 0) {
      params = params.set('ownerLoginIdList', this.ownerLoginIdList.join(','));
    }
    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
    }
    if (this.standard && this.standard.length > 0) {
      params = params.set('standard', this.standard.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.uri && this.filters.uri.length > 0) {
      params = params.set('uri', '' + this.filters.uri);
    }
    if (this.filters.prefix && this.filters.prefix.length > 0) {
      params = params.set('prefix', '' + this.filters.prefix);
    }
    if (this.filters.description && this.filters.description.length > 0) {
      params = params.set('description', '' + this.filters.description);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}
