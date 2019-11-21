import {PageRequest} from '../../../basis/basis';

export class ContextSchemeListRequest {
  filters: {
    name: string;
  };
  updaterLoginIds: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  page: PageRequest;

  constructor() {
    this.updatedDate = {
      start: null,
      end: null,
    };
    this.filters = {
      name: '',
    };
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

export class ContextSchemeValue {
  ctxSchemeValueId: number;
  guid: string;
  value: string;
  meaning: string;
  used: boolean;
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
