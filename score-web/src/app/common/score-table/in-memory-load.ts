import {HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {PageResponse} from '../../basis/basis';

/** What a column sorts by when it is not the row's own property of the same name. */
export type InMemorySortKeys<T> = Record<string, (row: T) => unknown>;

function compareValues(a: unknown, b: unknown): number {
  if (a === b) {
    return 0;
  }
  if (a === null || a === undefined || a === '') {
    return -1;
  }
  if (b === null || b === undefined || b === '') {
    return 1;
  }
  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }
  if (a instanceof Date && b instanceof Date) {
    return a.getTime() - b.getTime();
  }
  return ('' + a).localeCompare('' + b, undefined, {numeric: true, sensitivity: 'base'});
}

/**
 * The loader of a table whose rows the screen already has (no endpoint): sorts them by the sort of the parameters
 * (`orderBy`, or `sortActive`/`sortDirection`), keeps the ones that `matches` the parameters (the search) and cuts the page. Pass `score-table-view` the result and call its `reload()` when the rows change.
 */
export function inMemoryLoad<T>(rows: () => readonly T[], sortKeys: InMemorySortKeys<T> = {},
                                matches: (row: T, params: HttpParams) => boolean = () => true):
  (params: HttpParams) => Observable<PageResponse<T>> {
  return params => {
    const sorts: { active: string; desc: boolean }[] = [];
    const orderBy = params.get('orderBy');
    if (orderBy) {
      for (const s of orderBy.split(',')) {
        sorts.push({active: s.substring(1), desc: s.startsWith('-')});
      }
    } else if (params.get('sortActive')) {
      sorts.push({active: params.get('sortActive'), desc: params.get('sortDirection') === 'desc'});
    }

    const sorted = rows().filter(row => matches(row, params));
    if (sorts.length > 0) {
      sorted.sort((a, b) => {
        for (const s of sorts) {
          const key = sortKeys[s.active] ?? ((row: T) => (row as any)[s.active]);
          const c = compareValues(key(a), key(b));
          if (c !== 0) {
            return s.desc ? -c : c;
          }
        }
        return 0;
      });
    }

    const pageSize = Number(params.get('pageSize') ?? sorted.length) || sorted.length;
    let pageIndex = Number(params.get('pageIndex') ?? 0);
    // The page that was left by the removal of rows.
    if (pageIndex * pageSize >= sorted.length && pageIndex > 0) {
      pageIndex = Math.max(0, Math.ceil(sorted.length / pageSize) - 1);
    }
    return of({
      list: sorted.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize),
      page: pageIndex,
      size: pageSize,
      length: sorted.length
    });
  };
}
