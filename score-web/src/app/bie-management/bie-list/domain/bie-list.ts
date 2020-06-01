import {PageRequest} from '../../../basis/basis';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {Base64} from 'js-base64';

export class BieListRequest {
  filters: {
    propertyTerm: string;
    businessContext: string;
  };
  excludes: string[] = [];
  access: string;
  states: string[] = [];
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: Base64.decode(q)}) : new HttpParams();

    this.page.sortActive = params.get('sortActive') || (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    this.page.sortDirection = params.get('sortDirection') || (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    this.page.pageIndex = Number(params.get('pageIndex') || (defaultPageRequest) ? defaultPageRequest.pageIndex : 0);
    this.page.pageSize = Number(params.get('pageSize') || (defaultPageRequest) ? defaultPageRequest.pageSize : 10);

    this.excludes = (params.get('excludes')) ? Array.from(params.get('excludes').split(',')) : [];
    this.access = params.get('access');
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      propertyTerm: params.get('propertyTerm') || '',
      businessContext: params.get('businessContext') || ''
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.excludes && this.excludes.length > 0) {
      params = params.set('excludes', this.excludes.join(','));
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
    const str = Base64.encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class BieList {
  topLevelAbieId: number;
  propertyTerm: string;
  guid: string;
  releaseNum: string;
  bizCtxId: number;
  bizCtxName: string;
  access: string;
  owner: string;
  version: string;
  status: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  state: string;
  businessContexts: BusinessContext[];
}

export class SummaryBie {
  topLevelAbieId: number;
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
