import {WhoAndWhen} from '../../../basis/basis';

export class ContextCategorySummary {
  contextCategoryId: number;
  guid: string;
  name: string;
  description: string;
}

export class ContextCategoryListEntry {
  contextCategoryId: number;
  guid: string;
  name: string;
  description?: string;
  used: boolean;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ContextCategoryDetails {
  contextCategoryId: number;
  guid: string;
  name: string;
  description?: string;
  used: boolean;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}
