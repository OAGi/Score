import {PageRequest} from '../../../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../../common/utility';
import {ScoreUser} from '../../../../authentication/domain/auth';
import {BusinessContext} from '../../../../context-management/business-context/domain/business-context';
import {SimpleRelease} from 'src/app/release-management/domain/release';

export class OasDocListRequest {
  filters: {
    title: string;
    description: string;
  };
  access: string;
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

    this.access = params.get('access') || '';
    this.updaterUsernameList = (params.get('updaterUsernameList')) ? Array.from(params.get('updaterUsernameList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };

    this.filters = {
      title: params.get('title') || '',
      description: params.get('description') || '',
    };
  }

  toParams(): HttpParams {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.access && this.access.length > 0) {
      params = params.set('access', '' + this.access);
    }
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
    if (this.filters.description) {
      params = params.set('description', '' + this.filters.description);
    }
    return params;
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class OasDoc {
  oasDocId: number;
  guid: string;
  releaseId: number;
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
  access: string;
  ownerUserId: string;
  lastUpdateTimestamp: Date;
  creationTimestamp: Date;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
  used: boolean;
  bieList: BieForOasDoc[];
}

export interface simpleOasDoc {
  oasDocId: number;
  guid: string;
  openAPIVersion: string;
  title: string;
  description: string;
  version: string;
  licenseName: string;
  ownerUserId: string;
}

export class BieForOasDocListRequest {
  release: SimpleRelease;
  filters: {
    propertyTerm: string;
    businessContext: string;
    asccpManifestId: number;
    den: string;
  };
  excludePropertyTerms: string[] = [];
  excludeTopLevelAsbiepIds: number[] = [];
  access: string;
  states: string[] = [];
  updaterUsernameList: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  page: PageRequest = new PageRequest();
  ownedByDeveloper: boolean = undefined;

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
    this.release = new SimpleRelease();
    this.release.releaseId = Number(params.get('releaseId') || 0);
    this.excludePropertyTerms = (params.get('excludePropertyTerms')) ? Array.from(params.get('excludePropertyTerms').split(',')) : [];
    this.excludeTopLevelAsbiepIds = (params.get('excludeTopLevelAsbiepIds')) ? Array.from(params.get('excludeTopLevelAsbiepIds').split(',').map(e => Number(e))) : [];
    this.access = params.get('access') || '';
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updaterUsernameList = (params.get('updaterUsernameList')) ? Array.from(params.get('updaterUsernameList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      propertyTerm: params.get('propertyTerm') || '',
      businessContext: params.get('businessContext') || '',
      asccpManifestId: Number(params.get('asccpManifestId')) || 0,
      den: params.get('den') || ''
    };
  }

  toParams(): HttpParams {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);
    if (this.release) {
      params = params.set('releaseId', this.release.releaseId.toString());
    }
    if (this.excludePropertyTerms && this.excludePropertyTerms.length > 0) {
      params = params.set('excludePropertyTerms', this.excludePropertyTerms.join(','));
    }
    if (this.excludeTopLevelAsbiepIds && this.excludeTopLevelAsbiepIds.length > 0) {
      params = params.set('excludeTopLevelAsbiepIds', this.excludeTopLevelAsbiepIds.join(','));
    }
    if (this.access && this.access.length > 0) {
      params = params.set('access', '' + this.access);
    }
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.ownerLoginIds && this.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', this.ownerLoginIds.join(','));
    }
    if (this.updaterLoginIds && this.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', this.updaterLoginIds.join(','));
    }
    if (this.updaterUsernameList && this.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', this.updaterUsernameList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updateStart', '' + this.updatedDate.start.getTime());
    }
    if (this.updatedDate.end) {
      params = params.set('updateEnd', '' + this.updatedDate.end.getTime());
    }
    if (this.filters.propertyTerm && this.filters.propertyTerm.length > 0) {
      params = params.set('propertyTerm', '' + this.filters.propertyTerm);
    }
    if (this.filters.businessContext && this.filters.businessContext.length > 0) {
      params = params.set('businessContext', '' + this.filters.businessContext);
    }
    if (this.filters.asccpManifestId) {
      params = params.set('asccpManifestId', this.filters.asccpManifestId.toString());
    }
    if (this.filters.den && this.filters.den.length > 0) {
      params = params.set('den', '' + this.filters.den);
    }
    return params;
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class BieForOasDoc {
  oasDocId: number;
  topLevelAsbiepId: number;
  den: string;
  propertyTerm: string;
  guid: string;
  bizCtxId: number;
  bizCtxName: string;
  access: string;
  releaseNum: string;
  owner: string;
  version: string;
  status: string;
  state: string;
  businessContexts: BusinessContext[];
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  verbs: string[];
  arrayIndicator: boolean;
  suppressRoot: boolean;
  messageBody: string[];
  resourceName: string;
  operationId: string;
  tagName: string;
}
