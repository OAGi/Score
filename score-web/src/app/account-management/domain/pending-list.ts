import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';
import {PageRequest} from '../../basis/basis';

export class PendingListRequest {
  createdDate: {
    start: Date,
    end: Date,
  };
  filters: {
    preferredUsername: string;
    email: string;
    providerName: string;
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
    this.createdDate = {
      start: (params.get('createdDateStart')) ? new Date(params.get('createdDateStart')) : null,
      end: (params.get('createdDateEnd')) ? new Date(params.get('createdDateEnd')) : null
    };
    this.filters = {
      preferredUsername: params.get('preferredUsername') || '',
      email: params.get('email') || '',
      providerName: params.get('providerName') || '',
    };
  }

  toQuery(extras?): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.createdDate.start) {
      params = params.set('createdDateStart', '' + this.createdDate.start.toUTCString());
    }
    if (this.createdDate.end) {
      params = params.set('createdDateEnd', '' + this.createdDate.end.toUTCString());
    }
    if (this.filters.preferredUsername && this.filters.preferredUsername.length > 0) {
      params = params.set('preferredUsername', '' + this.filters.preferredUsername);
    }
    if (this.filters.email && this.filters.email.length > 0) {
      params = params.set('email', '' + this.filters.email);
    }
    if (this.filters.providerName && this.filters.providerName.length > 0) {
      params = params.set('providerName', '' + this.filters.providerName);
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

export class PendingAccount {
  appOauth2UserId: number;
  appUserId: number;
  providerName: string;
  name: string;
  email: string;
  sub: string;
  nickname: string;
  preferredUsername: string;
  phoneNumber: string;
  creationTimestamp: string;
  rejected: boolean;

  constructor(paramMap?: ParamMap) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

    this.appOauth2UserId = parseInt(params.get('appOauth2UserId'), 10);
    if (!this.appOauth2UserId) {
      this.appOauth2UserId = undefined;
    }
    this.name = params.get('name');
    if (!this.name) {
      this.name = undefined;
    }
    this.nickname = params.get('nickname');
    if (!this.nickname) {
      this.nickname = undefined;
    }
    this.preferredUsername = params.get('preferredUsername');
    if (!this.preferredUsername) {
      this.preferredUsername = undefined;
    }
    this.email = params.get('email');
    if (!this.email) {
      this.email = undefined;
    }
    this.sub = params.get('sub');
    if (!this.sub) {
      this.sub = undefined;
    }
  }
}
