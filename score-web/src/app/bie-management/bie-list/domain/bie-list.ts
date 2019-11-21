import {PageRequest} from '../../../basis/basis';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';

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
  page: PageRequest;

  constructor() {
    this.updatedDate = {
      start: null,
      end: null,
    };
    this.filters = {
      propertyTerm: '',
      businessContext: '',
    };
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
