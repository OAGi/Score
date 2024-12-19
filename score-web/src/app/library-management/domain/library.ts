import {ScoreUser} from '../../authentication/domain/auth';
import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';

export class Library {
  libraryId: number = 0;
  name: string;
  organization: string;
  link: string;
  domain: string;
  description: string;
  state: string;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
  creationTimestamp: Date;
  lastUpdateTimestamp: Date;
}

export class LibraryList {
  libraryId: number;
  name: string;
  organization: string;
  lastUpdateUser: string;
  lastUpdateTimestamp: Date;
}

export class LibraryListRequest {
  filters: {
    name: string;
    organization: string;
    domain: string;
    description: string;
    state: string;
  };
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
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

    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      name: params.get('name') || '',
      organization: params.get('organization') || '',
      domain: params.get('domain') || '',
      description: params.get('description') || '',
      state: params.get('state') || ''
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

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
    if (this.filters.organization && this.filters.organization.length > 0) {
      params = params.set('organization', '' + this.filters.organization);
    }
    if (this.filters.domain && this.filters.domain.length > 0) {
      params = params.set('domain', '' + this.filters.domain);
    }
    if (this.filters.description && this.filters.description.length > 0) {
      params = params.set('description', '' + this.filters.description);
    }
    if (this.filters.state && this.filters.state.length > 0) {
      params = params.set('state', '' + this.filters.state);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}
