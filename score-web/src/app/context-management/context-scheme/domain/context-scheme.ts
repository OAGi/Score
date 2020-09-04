import {PageRequest} from '../../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../common/utility';

export class ContextSchemeListRequest {
  filters: {
    name: string;
  };
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

    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      name: params.get('name') || ''
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.updaterLoginIds && this.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', this.updaterLoginIds.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.name && this.filters.name.length > 0) {
      params = params.set('name', '' + this.filters.name);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class ContextScheme {
  ctxSchemeId: number;
  guid: string;
  schemeName: string;
  ctxCategoryId: number;
  ctxCategoryName: string;
  codeListId: number;
  codeListName: string;
  schemeId?: string;
  schemeAgencyId?: string;
  schemeVersionId?: string;
  description?: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  ctxSchemeValues: ContextSchemeValue[];
  used: boolean;
}

export class ContextSchemeValueListRequest {
  filters: {
    value: string;
  };
  page: PageRequest = new PageRequest();

  constructor() {
    this.filters = {
      value: '',
    };
  }
}

export class ContextSchemeValue {
  ctxSchemeValueId: number;
  guid: string;
  value: string;
  meaning: string;
  used: boolean;
  ownerCtxSchemeId: number;
}

export interface SimpleContextCategory {
  ctxCategoryId: number;
  name: string;
}

export interface SimpleContextScheme {
  ctxSchemeId: number;
  schemeName: string;
  codeListId: number;
  codeListIdName;
  schemeId: string;
  schemeAgencyId: string;
  schemeVersionId: string;
}

export interface SimpleContextSchemeValue {
  ctxSchemeValueId: number;
  value: string;
  meaning: string;
}

export interface SimpleCodeList {
  codeListValueId: number;
  name: string;
}
