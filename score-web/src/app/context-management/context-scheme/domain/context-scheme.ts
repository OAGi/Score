import {PageRequest, WhoAndWhen} from '../../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../common/utility';
import {ScoreUser} from '../../../authentication/domain/auth';
import {ContextCategorySummary} from '../../context-category/domain/context-category';
import {CodeListSummary} from '../../../code-list-management/domain/code-list';

export class ContextSchemeSummary {
  contextSchemeId: number;
  guid: string;
  schemeId: string;
  schemeName: string;
  schemeAgencyId: string;
  schemeVersionId: string;
  description: string;
  contextCategory: ContextCategorySummary;
}

export class ContextSchemeValueSummary {
  contextSchemeValueId: number;
  guid: string;
  value: string;
  meaning: string;
  contextSchemeId: number;
}

export class ContextSchemeListEntry {
  contextSchemeId: number;
  guid: string;
  schemeName: string;
  schemeId?: string;
  schemeAgencyId?: string;
  schemeVersionId?: string;
  description?: string;
  used: boolean;

  contextCategory: ContextCategorySummary;
  codeList?: CodeListSummary;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ContextSchemeDetails {
  contextSchemeId: number;
  guid: string;
  schemeName: string;
  schemeId?: string;
  schemeAgencyId?: string;
  schemeVersionId?: string;
  description?: string;
  used: boolean;

  contextCategory: ContextCategorySummary;
  codeList?: CodeListSummary;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ContextSchemeCreateRequest {
  schemeName: string;
  schemeId?: string;
  schemeAgencyId?: string;
  schemeVersionId?: string;
  description?: string;

  contextCategoryId: number;
  codeListId: number;

  contextSchemeValueList: ContextSchemeValue[] = [];
}

export class ContextSchemeUpdateRequest {
  contextSchemeId: number;
  schemeName: string;
  schemeId?: string;
  schemeAgencyId?: string;
  schemeVersionId?: string;
  description?: string;
  used: boolean;

  contextCategoryId: number;
  codeListId: number;

  contextSchemeValueList: ContextSchemeValue[] = [];
}

export class ContextSchemeListRequest {
  filters: {
    name: string;
  };
  updaterLoginIdList: string[] = [];
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

    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
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

    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
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
  contextSchemeId: number;
  guid: string;
  schemeName: string;
  contextCategoryId: number;
  contextCategoryName: string;
  codeListId: number;
  codeListName: string;
  schemeId?: string;
  schemeAgencyId?: string;
  schemeVersionId?: string;
  description?: string;
  lastUpdateTimestamp: Date;
  lastUpdatedBy: ScoreUser;
  contextSchemeValueList: ContextSchemeValue[];
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
  contextSchemeValueId: number;
  guid: string;
  value: string;
  meaning: string;
  used: boolean;
  ownerContextSchemeId: number;
}