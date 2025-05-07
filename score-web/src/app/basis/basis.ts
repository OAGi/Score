import {ScoreUser} from '../authentication/domain/auth';

export class PageRequest {
  sortActives: string[];
  sortDirections: string[];
  pageIndex: number;
  pageSize: number;

  constructor(sortActives?: string | string[], sortDirections?: string | string[], pageIndex?: number, pageSize?: number) {
    if (typeof sortActives === 'string') {
      this.sortActives = [sortActives as string,];
    } else {
      this.sortActives = sortActives;
    }

    if (typeof sortDirections === 'string') {
      this.sortDirections = [sortDirections as string,];
    } else {
      this.sortDirections = sortDirections;
    }

    this.pageIndex = pageIndex;
    this.pageSize = pageSize;
  }

  get sortActive(): string {
    return (!!this.sortActives) ? this.sortActives[0] : undefined;
  }

  set sortActive(val: string) {
    this.sortActives = (!val) ? [] : [val,];
  }

  get sortDirection(): string {
    return (!!this.sortDirections) ? this.sortDirections[0] : undefined;
  }

  set sortDirection(val: string) {
    this.sortDirections = (!val) ? [] : [val,];
  }

}

export class PageResponse<T> {
  list: T[];
  page: number;
  size: number;
  length: number;
}

export class PaginationResponse<T> {
  results: T[];
  page: number;
  size: number;
  length: number;
}

export class WhoAndWhen {
  who: ScoreUser;
  when: Date;
}

export class Definition {
  content: string;
  source: string;
}

export class Cardinality {
  min: number;
  max: number;
}

export class PrimitiveRestriction {
  xbtManifestId: number;
  codeListManifestId: number;
  agencyIdListManifestId: number;
}

export class ValueConstraint {
  defaultValue: string;
  fixedValue: string;
}

export class Facet {
  minLength: number;
  maxLength: number;
  pattern: string;
}
