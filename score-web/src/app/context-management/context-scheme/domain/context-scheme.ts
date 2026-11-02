import {PageRequest, WhoAndWhen} from '../../../basis/basis';
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