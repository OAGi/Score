import {PageRequest} from '../../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../common/utility';
import {ScoreUser} from '../../../authentication/domain/auth';
import {SimpleRelease} from '../../../release-management/domain/release';

export class BiePackage {
  biePackageId: number;
  versionId: string;
  versionName: string;
  description: string;
  releaseId: number;
  releaseNum: string;
  state: string;
  access: string;
  owner: ScoreUser;
  lastUpdateTimestamp: Date;
  creationTimestamp: Date;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;

  sourceBiePackageId: number;
  sourceBiePackageVersionName: string;
  sourceBiePackageVersionId: string;
  sourceAction: string;
  sourceTimestamp: Date;
}

export class BiePackageListRequest {
  releases: SimpleRelease[] = [];
  filters: {
    versionId: string;
    versionName: string;
    description: string;

    den: string;
    businessTerm: string;
    version: string;
    remark: string;
  };
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

    this.page.sortActives = (params.get('sortActives')) ? Array.from(params.get('sortActives').split(',')) : undefined;
    if (!this.page.sortActives) {
      this.page.sortActives = (defaultPageRequest) ? defaultPageRequest.sortActives : [];
    }
    this.page.sortDirections = (params.get('sortDirections')) ? Array.from(params.get('sortDirections').split(',')) : undefined;
    if (!this.page.sortDirections) {
      this.page.sortDirections = (defaultPageRequest) ? defaultPageRequest.sortDirections : [];
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

    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.ownerLoginIds = (params.get('ownerLoginIds')) ? Array.from(params.get('ownerLoginIds').split(',')) : [];
    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };

    this.filters = {
      versionId: params.get('versionId') || '',
      versionName: params.get('versionName') || '',
      description: params.get('description') || '',

      den: params.get('den') || '',
      businessTerm: params.get('businessTerm') || '',
      version: params.get('version') || '',
      remark: params.get('remark') || '',
    };
  }

  toParams(): HttpParams {
    let params = new HttpParams()
      .set('sortActives', this.page.sortActives.join(','))
      .set('sortDirections', this.page.sortDirections.join(','))
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.releases && this.releases.length > 0) {
      params = params.set('releaseIds', this.releases.map(e => e.releaseId.toString()).join(','));
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
    if (this.updatedDate.start) {
      params = params.set('updateStart', '' + this.updatedDate.start.getTime());
    }
    if (this.updatedDate.end) {
      params = params.set('updateEnd', '' + this.updatedDate.end.getTime());
    }
    if (this.filters.versionId && this.filters.versionId.length > 0) {
      params = params.set('versionId', '' + this.filters.versionId);
    }
    if (this.filters.versionName && this.filters.versionName.length > 0) {
      params = params.set('versionName', '' + this.filters.versionName);
    }
    if (this.filters.description) {
      params = params.set('description', '' + this.filters.description);
    }
    if (this.filters.den) {
      params = params.set('den', '' + this.filters.den);
    }
    if (this.filters.businessTerm) {
      params = params.set('businessTerm', '' + this.filters.businessTerm);
    }
    if (this.filters.version) {
      params = params.set('version', '' + this.filters.version);
    }
    if (this.filters.remark) {
      params = params.set('remark', '' + this.filters.remark);
    }
    return params;
  }

  toQuery(extras?): string {
    let params = this.toParams();
    if (extras) {
      Object.keys(extras).forEach(key => {
        params = params.set(key.toString(), extras[key]);
      });
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }

}

export class BieListInBiePackageRequest {

  biePackageId: number;
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

    this.biePackageId = (params.get('biePackageId')) ? Number(params.get('biePackageId')) : 0;
    this.page.sortActives = (params.get('sortActives')) ? Array.from(params.get('sortActives').split(',')) : undefined;
    if (!this.page.sortActives) {
      this.page.sortActives = (defaultPageRequest) ? defaultPageRequest.sortActives : [];
    }
    this.page.sortDirections = (params.get('sortDirections')) ? Array.from(params.get('sortDirections').split(',')) : undefined;
    if (!this.page.sortDirections) {
      this.page.sortDirections = (defaultPageRequest) ? defaultPageRequest.sortDirections : [];
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
  }

  toParams(): HttpParams {
    let params = new HttpParams()
      .set('sortActives', this.page.sortActives.join(','))
      .set('sortDirections', this.page.sortDirections.join(','))
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    params = params.set('biePackageId', this.biePackageId);

    return params;
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }

}
