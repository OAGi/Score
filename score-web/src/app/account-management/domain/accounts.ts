import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {Base64} from 'js-base64';

export class AccountListRequest {
  filters: {
    loginId: string;
    name: string;
    organization: string;
    role: string;
  };
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: Base64.decode(q)}) : new HttpParams();

    this.page.sortActive = params.get('sortActive') || (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    this.page.sortDirection = params.get('sortDirection') || (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    this.page.pageIndex = Number(params.get('pageIndex') || (defaultPageRequest) ? defaultPageRequest.pageIndex : 0);
    this.page.pageSize = Number(params.get('pageSize') || (defaultPageRequest) ? defaultPageRequest.pageSize : 10);

    this.filters = {
      loginId: params.get('loginId') || '',
      name: params.get('name') || '',
      organization: params.get('organization') || '',
      role: params.get('role') || ''
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
    if (this.filters.role && this.filters.role.length > 0) {
      params = params.set('role', '' + this.filters.role);
    }
    const str = Base64.encode(params.toString());
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
}

