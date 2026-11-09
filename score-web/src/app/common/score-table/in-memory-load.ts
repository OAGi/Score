import {HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {PageResponse} from '../../basis/basis';
import {TableDef} from './table-def';

/** Ids of the sortable columns of a table. */
type SortableColumnId<D extends TableDef> = Extract<D['columns'][number], { readonly sortable: true }>['id'];
/** The sortable columns whose id is not a property of the row: what they sort by has to be said. */
type UnmappedColumnId<T, D extends TableDef> = Exclude<SortableColumnId<D>, keyof T>;
type SortKey<T> = (row: T) => unknown;

/**
 * What a sortable column sorts by: required for each whose id is not a property of the row (compile error when one is missing),
 * optional for the others (to sort by something else than the property).
 */
export type InMemorySortKeys<T, D extends TableDef> =
  { readonly [K in UnmappedColumnId<T, D>]: SortKey<T> }
  & { readonly [K in Extract<SortableColumnId<D>, keyof T>]?: SortKey<T> };

/** `sortKeys` is left out when every sortable column is a property of the row. */
type InMemoryLoadArgs<T, D extends TableDef> = [UnmappedColumnId<T, D>] extends [never]
  ? [sortKeys?: InMemorySortKeys<T, D>, matches?: (row: T, params: HttpParams) => boolean]
  : [sortKeys: InMemorySortKeys<T, D>, matches?: (row: T, params: HttpParams) => boolean];

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
 * (`orderBy`, `sortActives`/`sortDirections` of a multi-sort table, or `sortActive`/`sortDirection`), keeps the ones that `matches` the parameters (the search) and cuts the page. Pass `score-table-view` the result and call its `reload()` when the rows change.
 *
 * It takes the table definition, so that the sortable columns are checked against the row: a column whose id is not a property
 * of the row has to be given what it sorts by, or it does not compile. Going to an endpoint that pages and sorts, the loader
 * is all that changes: the columns and their ids stay.
 */
export function inMemoryLoad<T, const D extends TableDef>(table: D, rows: () => readonly T[],
                                                                  ...[sortKeys, matches = () => true]: InMemoryLoadArgs<T, D>):
  (params: HttpParams) => Observable<PageResponse<T>> {
  const keys = (sortKeys ?? {}) as Record<string, SortKey<T>>;
  return params => {
    const sorts: { active: string; desc: boolean }[] = [];
    const orderBy = params.get('orderBy');
    if (orderBy) {
      for (const s of orderBy.split(',')) {
        sorts.push({active: s.substring(1), desc: s.startsWith('-')});
      }
    } else if (params.get('sortActives')) {
      // a multi-sort table: comma separated, the first is the most significant
      const directions = (params.get('sortDirections') ?? '').split(',');
      params.get('sortActives').split(',').forEach((active, i) => {
        if (active) {
          sorts.push({active, desc: directions[i] === 'desc'});
        }
      });
    } else if (params.get('sortActive')) {
      sorts.push({active: params.get('sortActive'), desc: params.get('sortDirection') === 'desc'});
    }

    const sorted = rows().filter(row => matches(row, params));
    if (sorts.length > 0) {
      sorted.sort((a, b) => {
        for (const s of sorts) {
          const key = keys[s.active] ?? ((row: T) => (row as any)[s.active]);
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
