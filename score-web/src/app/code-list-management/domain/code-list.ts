import {PageRequest} from '../../basis/basis';

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
