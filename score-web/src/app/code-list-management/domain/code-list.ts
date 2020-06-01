import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {Base64} from 'js-base64';

export class CodeListForListRequest {
  filters: {
    name: string;
  };
  states: string[] = [];
  extensible: boolean;
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

    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.extensible = (params.get('extensible')) ? (('true' === params.get('extensible')) ? true : false) : undefined;
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

    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.extensible !== undefined) {
      params = params.set('extensible', (this.extensible) ? 'true' : 'false');
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
    if (this.filters.name && this.filters.name.length > 0) {
      params = params.set('name', '' + this.filters.name);
    }
    const str = Base64.encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class CodeListForList {
  codeListId: number;
  codeListName: string;
  guid: string;
  basedCodeListId: number;
  basedCodeListName: string;
  listId: string;
  agencyId: number;
  agencyIdName: string;
  versionId: string;
  lastUpdateTimestamp: Date;
  extensible: boolean;
  state: string;
}

export class CodeList {
  codeListId: number;
  codeListName: string;
  basedCodeListId: number;
  basedCodeListName: string;
  agencyId: number;
  agencyIdName: string;
  versionId: string;

  guid: string;
  listId: string;
  definition: string;
  definitionSource: string;
  remark: string;

  extensible: boolean;
  state: string;

  codeListValues: CodeListValue[];
}

export class CodeListValue {
  codeListValueId: number;
  guid: string;
  value: string;
  name: string;
  definition: string;
  definitionSource: string;

  used: boolean;
  locked: boolean;
  extension: boolean;
}

export class SimpleAgencyIdListValue {
  agencyIdListValueId: number;
  name: string;
}
