import {PageRequest} from '../../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {SimpleRelease} from 'src/app/release-management/domain/release';
import {base64Decode, base64Encode, hashCode4Array} from '../../../common/utility';
import {ScoreUser} from '../../../authentication/domain/auth';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {ChangeListener} from '../../domain/bie-flat-tree';

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
      .set('sortActives', this.page.sortActives.join(','))
      .set('sortDirections', this.page.sortDirections.join(','))
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
  bieList: BieForOasDoc[];
}

export interface SimpleOasDoc {
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
    version: string;
    remark: string;
    asccpManifestId: number;
    den: string;
  };
  excludePropertyTerms: string[] = [];
  excludeTopLevelAsbiepIds: number[] = [];
  access: string;
  states: string[] = [];
  types: string[] = [];
  ownerLoginIds: string[] = [];
  updaterUsernameList: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  updaterLoginIds: string[] = [];
  page: PageRequest = new PageRequest();
  ownedByDeveloper: boolean = undefined;

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

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
    this.release = new SimpleRelease();
    this.release.releaseId = Number(params.get('releaseId') || 0);
    this.excludePropertyTerms = (params.get('excludePropertyTerms')) ? Array.from(params.get('excludePropertyTerms').split(',')) : [];
    this.excludeTopLevelAsbiepIds = (params.get('excludeTopLevelAsbiepIds')) ? Array.from(params.get('excludeTopLevelAsbiepIds').split(',').map(e => Number(e))) : [];
    this.access = params.get('access') || '';
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.types = (params.get('types')) ? Array.from(params.get('types').split(',')) : [];
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
      version: params.get('version') || '',
      remark: params.get('remark') || '',
      asccpManifestId: Number(params.get('asccpManifestId')) || 0,
      den: params.get('den') || ''
    };
  }

  toQuery(extras?): string {
    let params = new HttpParams()
      .set('sortActives', this.page.sortActives.join(','))
      .set('sortDirections', this.page.sortDirections.join(','))
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
    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
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

export class BieForOasDoc {
  oasDocId: number;
  topLevelAsbiepId: number;
  oasResourceId: number;
  oasOperationId: number;
  den: string;
  propertyTerm: string;
  remark: string;
  guid: string;
  bizCtxId: number;
  bizCtxName: string;
  access: string;
  releaseNum: string;
  owner: string;
  version: string;
  status: string;
  state: string;
  businessContext: string;
  lastUpdateTimestamp: Date;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
  businessContexts: BusinessContext[] = [];
  private _resourceName: string;
  private _operationId: string;
  private _tagName: string;
  private _verb: string;
  private _arrayIndicator: boolean;
  private _suppressRootIndicator: boolean;
  private _messageBody: string;
  private $hashCode: number;
  listeners: ChangeListener<BieForOasDoc>[] = [];

  constructor(obj?: BieForOasDoc) {
    this.oasDocId = obj && obj.oasDocId || 0;
    this.topLevelAsbiepId = obj && obj.topLevelAsbiepId || 0;
    this.den = obj && obj.den || '';
    this.propertyTerm = obj && obj.propertyTerm || '';
    this.remark = obj && obj.remark || '';
    this.guid = obj && obj.guid || '';
    this.bizCtxId = obj && obj.bizCtxId || 0;
    this.bizCtxName = obj && obj.bizCtxName || '';
    this.access = obj && obj.access || '';
    this.releaseNum = obj && obj.releaseNum || '';
    this.owner = obj && obj.owner || '';
    this.version = obj && obj.version || '';
    this.status = obj && obj.status || '';
    this.state = obj && obj.state || '';
    this.businessContext = obj && obj.businessContext || '';
    this.verb = obj && obj.verb || '';
    this.arrayIndicator = obj && obj.arrayIndicator || false;
    this.suppressRootIndicator = obj && obj.suppressRootIndicator || false;
    this.messageBody = obj && obj.messageBody || '';
    this.resourceName = obj && obj.resourceName || '';
    this.operationId = obj && obj.operationId || '';
    this.tagName = obj && obj.tagName || '';
    this.oasOperationId = obj && obj.oasOperationId || 0;
    this.oasResourceId = obj && obj.oasResourceId || 0;
    this.lastUpdateTimestamp = obj && obj.lastUpdateTimestamp || undefined;
    this.createdBy = obj && obj.createdBy || undefined;
    this.lastUpdatedBy = obj && obj.lastUpdatedBy || undefined;
    this.businessContexts = obj && obj.businessContexts || [];
  }

  get json(): any {
    return {
      oasDocId: this.oasDocId,
      topLevelAsbiepId: this.topLevelAsbiepId,
      oasResourceId: this.oasResourceId,
      oasOperationId: this.oasOperationId,
      resourceName: this.resourceName,
      operationId: this.operationId,
      verb: this.verb,
      arrayIndicator: this.arrayIndicator,
      suppressRootIndicator: this.suppressRootIndicator,
      messageBody: this.messageBody,
      tagName: this.tagName
    };
  }

  get resourceName(): string {
    return this._resourceName;
  }

  set resourceName(value: string) {
    this._resourceName = value;
    this.listeners.forEach(e => e.onChange(this, 'resourceName', value));
  }

  get operationId(): string {
    return this._operationId;
  }

  set operationId(value: string) {
    this._operationId = value;
    this.listeners.forEach(e => e.onChange(this, 'operationId', value));
  }

  get tagName(): string {
    return this._tagName;
  }

  set tagName(value: string) {
    this._tagName = value;
  }

  get verb(): string {
    return this._verb;
  }

  set verb(value: string) {
    this._verb = value;
  }

  get arrayIndicator(): boolean {
    return this._arrayIndicator;
  }

  set arrayIndicator(arrayIndicator: boolean) {
    this._arrayIndicator = arrayIndicator;
  }

  get suppressRootIndicator(): boolean {
    return this._suppressRootIndicator;
  }

  set suppressRootIndicator(arrayIndicator: boolean) {
    this._suppressRootIndicator = arrayIndicator;
  }

  get messageBody(): string {
    return this._messageBody;
  }

  set messageBody(messageBody: string) {
    this._messageBody = messageBody;
  }

  get hashCode(): number {
    return hashCode4Array([this.oasDocId, this.topLevelAsbiepId, this.oasResourceId, this.oasOperationId,
      this.verb, this.messageBody, this.resourceName, this.operationId, this.tagName,
      this.arrayIndicator, this.suppressRootIndicator]);
  }

  reset(): void {
    this.$hashCode = this.hashCode;
  }

  get isChanged(): boolean {
    return this.$hashCode !== this.hashCode;
  }
}

export class AssignBieForOasDoc {
  oasRequest: boolean;
  oasDocId: number;
  topLevelAsbiepId: number;
  propertyTerm: string;
  verb: string;
  required: boolean;
  arrayIndicator: boolean;
  suppressRootIndicator: boolean;
  messageBody: string;
  tagName: string;
}

export class BieForOasDocUpdateRequest {
  oasDocId: number;
  bieForOasDocList: BieForOasDoc[];

  constructor() {
    this.bieForOasDocList = [];
  }

  get json(): any {
    return {
      oasDocId: this.oasDocId,
      bieForOasDocList: this.bieForOasDocList.map(e => e.json)
    };
  }
}

export class BieForOasDocDeleteRequest {
  oasDocId: number;
  bieForOasDocList: BieForOasDoc[];

  constructor() {
    this.bieForOasDocList = [];
  }

  get json(): any {
    return {
      oasDocId: this.oasDocId,
      bieForOasDocList: this.bieForOasDocList.map(e => e.json)
    };
  }
}

export class ReusedBIEViolationCheck {
  errorMessages: string[];
}

export class OasTag {
  oasTagId: number;
  guid: string;
  name: string;
  lastUpdateTimestamp: Date;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
  used: boolean;
}

export class AssignedOasTag {
  assignedOasTagId: number;
  bieId: number;
  oasTagId: number;
  guid: string;
  name: string;
  lastUpdateTimestamp: Date;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
}

export class PostAssignOasTag {
  biesToAssign: number[];
  oasTagId: number;
}
