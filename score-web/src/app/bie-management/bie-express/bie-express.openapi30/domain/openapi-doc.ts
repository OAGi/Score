import {PageRequest} from '../../../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../../common/utility';
import {ScoreUser} from '../../../../authentication/domain/auth';
import {BusinessContext} from '../../../../context-management/business-context/domain/business-context';

export class OasDocListRequest {
  filters: {
    title: string;
    openAPIVersion: string;
    version: string;
    licenseName: string;
  };

  updaterUsernameList: string[] = [];
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

    this.updaterUsernameList = (params.get('updaterUsernameList')) ? Array.from(params.get('updaterUsernameList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };

    this.filters = {
      title: params.get('title') || '',
      openAPIVersion: params.get('openAPIVersion') || '',
      version: params.get('version') || '',
      licenseName: params.get('licenseName') || ''
    };
  }

  toParams(): HttpParams {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.updaterUsernameList && this.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', this.updaterUsernameList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updateStart', '' + this.updatedDate.start.getTime());
    }
    if (this.updatedDate.end) {
      params = params.set('updateEnd', '' + this.updatedDate.end.getTime());
    }
    if (this.filters.title && this.filters.title.length > 0) {
      params = params.set('title', '' + this.filters.title);
    }
    if (this.filters.openAPIVersion) {
      params = params.set('openAPIVersion', '' + this.filters.openAPIVersion);
    }
    if (this.filters.version) {
      params = params.set('version', '' + this.filters.version);
    }
    if (this.filters.licenseName) {
      params = params.set('licenseName', '' + this.filters.licenseName);
    }
    return params;
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}
export class OasDoc{
  oasDocId: number;
  guid: string;
  openAPIVersion: string;
  title: string;
  description: string;
  termsOfService: string;
  version: string;
  contactName: string;
  contactUrl: string;
  contactEmail: string;
  licenseName: string;
  licenseUrl: string;
  lastUpdateTimestamp: Date;
  creationTimestamp: Date;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
}

export class OasDocBIEListRequest {
  updaterUsernameList: string[] = [];
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

    this.updaterUsernameList = (params.get('updaterUsernameList')) ? Array.from(params.get('updaterUsernameList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
  }

  toParams(): HttpParams {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.updaterUsernameList && this.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', this.updaterUsernameList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updateStart', '' + this.updatedDate.start.getTime());
    }
    if (this.updatedDate.end) {
      params = params.set('updateEnd', '' + this.updatedDate.end.getTime());
    }
    return params;
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class OasDocBIEList{
  topLevelAsbiepId: number;
  den: string;
  propertyTerm: string;
  guid: string;
  releaseNum: string;
  bizCtxId: number;
  bizCtxName: string;
  access: string;
  owner: string;
  version: string;
  status: string;
  state: string;
  businessContexts: BusinessContext[];
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  verb: string;
  arrayIndicator: boolean;
  suppressRoot: boolean;
  messageBody: string;
  resourceName: string;
  operationId: string;
  tagName: string;
}


