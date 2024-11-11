import {PageRequest} from '../../../basis/basis';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../common/utility';
import {SimpleRelease} from '../../../release-management/domain/release';

export class BieListRequest {
  releases: SimpleRelease[] = [];
  filters: {
    propertyTerm: string;
    businessContext: string;
    version: string;
    remark: string;
    asccpManifestId: number;
    den: string;
  };
  excludePropertyTerms: string[] = [];
  topLevelAsbiepIds: number[] = [];
  excludeTopLevelAsbiepIds: number[] = [];
  access: string;
  states: string[] = [];
  deprecated: boolean[] = [false];
  types: string[] = [];
  ownerLoginIds: string[] = [];
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  page: PageRequest = new PageRequest();
  ownedByDeveloper: boolean = undefined;

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
    this.releases = (params.get('releaseIds')) ? Array.from(params.get('releaseIds').split(',').map(e => {
      const release = new SimpleRelease();
      release.releaseId = Number(e);
      return release;
    })) : [];
    if (this.releases.length === 0 && params.get('releaseId')) {
      const release = new SimpleRelease();
      release.releaseId = Number(params.get('releaseId'));
      if (release.releaseId >= 0) {
        this.releases = [release];
      }
    }
    this.page.sortActive = params.get('sortActive');
    if (this.page.sortActive !== '' && !this.page.sortActive) {
      this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    }
    this.page.sortDirection = params.get('sortDirection');
    if (this.page.sortDirection !== '' && !this.page.sortDirection) {
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
    this.topLevelAsbiepIds = (params.get('topLevelAsbiepIds')) ? Array.from(params.get('topLevelAsbiepIds').split(',').map(e => Number(e))) : [];
    this.excludeTopLevelAsbiepIds = (params.get('excludeTopLevelAsbiepIds')) ? Array.from(params.get('excludeTopLevelAsbiepIds').split(',').map(e => Number(e))) : [];
    this.access = params.get('access');
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.deprecated = (params.get('deprecated')) ? Array.from(params.get('deprecated').split(',').map(e => e === 'true' ? true : false)) : [];
    this.types = (params.get('types')) ? Array.from(params.get('types').split(',')) : [];
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      propertyTerm: params.get('propertyTerm') || '',
      businessContext: params.get('businessContext') || '',
      version: params.get('version') || '',
      remark: params.get('remark') || '',
      asccpManifestId: Number(params.get('asccpManifestId')) || 0,
      den: params.get('den') || '',
    };
  }

  toQuery(extras?): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.releases && this.releases.length > 0) {
      params = params.set('releaseIds', this.releases.map(e => e.releaseId.toString()).join(','));
    }
    if (this.excludePropertyTerms && this.excludePropertyTerms.length > 0) {
      params = params.set('excludePropertyTerms', this.excludePropertyTerms.join(','));
    }
    if (this.topLevelAsbiepIds && this.topLevelAsbiepIds.length > 0) {
      params = params.set('topLevelAsbiepIds', this.topLevelAsbiepIds.join(','));
    }
    if (this.excludeTopLevelAsbiepIds && this.excludeTopLevelAsbiepIds.length > 0) {
      params = params.set('excludeTopLevelAsbiepIds', this.excludeTopLevelAsbiepIds.join(','));
    }
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.deprecated !== undefined && this.deprecated.length > 0) {
      params = params.set('deprecated', this.deprecated.map(e => (e) ? 'true' : 'false').join(','));
    }
    if (this.types && this.types.length > 0) {
      params = params.set('types', this.types.join(','));
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
    if (this.filters.version && this.filters.version.length > 0) {
      params = params.set('version', '' + this.filters.version);
    }
    if (this.filters.remark && this.filters.remark.length > 0) {
      params = params.set('remark', '' + this.filters.remark);
    }
    if (this.filters.asccpManifestId) {
      params = params.set('asccpManifestId', this.filters.asccpManifestId.toString());
    }
    if (this.filters.den && this.filters.den.length > 0) {
      params = params.set('den', '' + this.filters.den);
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

export class BieList {
  topLevelAsbiepId: number;
  den: string;
  propertyTerm: string;
  displayName: string;
  guid: string;
  releaseNum: string;
  bizCtxId: number;
  bizCtxName: string;
  access: string;
  owner: string;
  ownerUserId: number;
  version: string;
  status: string;
  bizTerm: string;
  remark: string;
  deprecated: boolean;
  deprecatedReason: string;
  deprecatedRemark: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  state: string;
  businessContexts: BusinessContext[];

  sourceTopLevelAsbiepId: number;
  sourceReleaseId: number;
  sourceDen: string;
  sourceDisplayName: string;
  sourceAction: string;
  sourceTimestamp: Date;
}

export class AsbieBbieList {
  type: string;
  bieId: number;
  guid: string;
  den: string;
  state: string;
  version: string;
  status: string;
  bizCtxId: string;
  bizCtxName: string;
  releaseId: number;
  releaseNum: string;
  remark: string;
  lastUpdateUser: string;
  lastUpdateTimestamp: Date;
  used: string;
  topLevelAsbiepId: number;
  topLevelAsccpPropertyTerm: string;
  businessContexts: BusinessContext[];
  owner: string;
  access: string;
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
