import {SortDirection} from '@angular/material/sort/typings/sort-direction';

export class PageRequest {
  sortActive: string;
  sortDirection: string;
  pageIndex: number;
  pageSize: number;

  constructor(sortActive?: string, sortDirection?: string, pageIndex?: number, pageSize?: number) {
    this.sortActive = sortActive;
    this.sortDirection = sortDirection;
    this.pageIndex = pageIndex;
    this.pageSize = pageSize;
  }
}

export class PageResponse<T> {
  list: T[];
  page: number;
  size: number;
  length: number;
}
