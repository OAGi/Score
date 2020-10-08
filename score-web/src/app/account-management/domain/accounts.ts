import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';

export class AccountListRequest {
  filters: {
    loginId: string;
    name: string;
    organization: string;
    status: string[];
    role: string;
    excludeSSO: boolean;
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

    this.filters = {
      loginId: params.get('loginId') || '',
      name: params.get('name') || '',
      organization: params.get('organization') || '',
      status: (params.get('status')) ? Array.from(params.get('status').split(',')) : [],
      role: params.get('role') || '',
      excludeSSO: false
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.filters.loginId && this.filters.loginId.length > 0) {
      params = params.set('loginId', '' + this.filters.loginId);
    }
    if (this.filters.name && this.filters.name.length > 0) {
      params = params.set('name', '' + this.filters.name);
    }
    if (this.filters.organization && this.filters.organization.length > 0) {
      params = params.set('organization', '' + this.filters.organization);
    }
    if (this.filters.status && this.filters.status.length > 0) {
      params = params.set('status', this.filters.status.join(','));
    }
    if (this.filters.role && this.filters.role.length > 0) {
      params = params.set('role', '' + this.filters.role);
    }
    if (this.filters.excludeSSO) {
      params = params.set('excludeSSO', '' + this.filters.excludeSSO);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class AccountList {
  appUserId: number;
  loginId: string;
  password: string;
  name: string;
  organization: string;
  developer: boolean;
  enabled: boolean;
  appOauth2UserId: number;
}
