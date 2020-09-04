import {PageRequest} from '../../../basis/basis';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../common/utility';

export class BieListRequest {
  filters: {
    propertyTerm: string;
    businessContext: string;
    releaseId: number;
    asccpId: number;
  };
  excludePropertyTerms: string[] = [];
  excludeTopLevelAsbiepIds: number[] = [];
  access: string;
  states: string[] = [];
  ownerLoginIds: string[] = [];
  ownedByDeveloper: boolean;
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

    this.excludePropertyTerms = (params.get('excludePropertyTerms')) ? Array.from(params.get('excludePropertyTerms').split(',')) : [];
    this.excludeTopLevelAsbiepIds = (params.get('excludeTopLevelAsbiepIds')) ? Array.from(params.get('excludeTopLevelAsbiepIds').split(',').map(e => Number(e))) : [];
    this.access = params.get('access');
    this.ownedByDeveloper = params.get('ownedByDeveloper') === 'true' || false;
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      propertyTerm: params.get('propertyTerm') || '',
      businessContext: params.get('businessContext') || '',
      releaseId: Number(params.get('releaseId')) || 0,
      asccpId: Number(params.get('asccpId')) || 0
    };
  }

  toQuery(extras?): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.excludePropertyTerms && this.excludePropertyTerms.length > 0) {
      params = params.set('excludePropertyTerms', this.excludePropertyTerms.join(','));
    }
    if (this.excludeTopLevelAsbiepIds && this.excludeTopLevelAsbiepIds.length > 0) {
      params = params.set('excludeTopLevelAsbiepIds', this.excludeTopLevelAsbiepIds.join(','));
    }
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.access && this.access.length > 0) {
      params = params.set('access', this.access);
    }
    if (this.ownerLoginIds && this.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', this.ownerLoginIds.join(','));
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
    if (this.filters.propertyTerm && this.filters.propertyTerm.length > 0) {
      params = params.set('propertyTerm', '' + this.filters.propertyTerm);
    }
    if (this.filters.businessContext && this.filters.businessContext.length > 0) {
      params = params.set('businessContext', '' + this.filters.businessContext);
    }
    if (this.filters.releaseId) {
      params = params.set('releaseId', this.filters.releaseId.toString());
    }
    if (this.filters.asccpId) {
      params = params.set('asccpId', this.filters.asccpId.toString());
    }
    if (extras) {
      Object.keys(extras).forEach(key => {
        params = params.set(key.toString(), extras[key]);
      })
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class BieList {
  topLevelAsbiepId: number;
  propertyTerm: string;
  guid: string;
  releaseNum: string;
  bizCtxId: number;
  bizCtxName: string;
  access: string;
  owner: string;
  version: string;
  status: string;
  bizTerm: string;
  remark: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  state: string;
  businessContexts: BusinessContext[];
}

export class SummaryBie {
  topLevelAsbiepId: number;
  lastUpdateTimestamp: Date;
  state: string;
  ownerUsername: string;
  propertyTerm: string;
  businessContexts: BusinessContext[];
}

export class SummaryBieInfo {
  numberOfTotalBieByStates: Map<string, number>;
  numberOfMyBieByStates: Map<string, number>;
  bieByUsersAndStates: Map<string, Map<string, number>>;
  myRecentBIEs: BieList[] = [];
}
