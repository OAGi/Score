import {PageRequest} from '../../../basis/basis';

export class BusinessContextListRequest {
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

export class BusinessContext {
  bizCtxId: number;
  guid: string;
  name: string;
  lastUpdateTimestamp: Date;
  lastUpdateUser: string;
  bizCtxValues: BusinessContextValue[];
  used: boolean;
}

export class BusinessContextValue {
  bizCtxValueId: number;
  guid: string;
  ctxCategoryId: number;
  ctxCategoryName: string;
  ctxSchemeId: number;
  ctxSchemeName: string;
  ctxSchemeValueId: number;
  ctxSchemeValue: string;
  bizCtxId: number;
}

export class BusinessContextRule {
  bizCtxRuleId: number;
  fromBizCtxId: number;
  topLevelBieId: number;
}
