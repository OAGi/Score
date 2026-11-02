import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../utility';
import {TableDef, TableFilterDef} from './table-def';

export interface DateRange {
  start: Date | null;
  end: Date | null;
}

/** Value of a filter: text → string, updater and select → string[], boolean → boolean, dateRange → DateRange. */
export type TableFilterValue = string | string[] | boolean | DateRange;

export interface QuerySort {
  active: string;
  direction: string;
}

/** The state of a table query: page, sorts (in order of priority) and the filter values. */
export interface TableQuery {
  pageIndex: number;
  pageSize: number;
  sorts: QuerySort[];
  values: Record<string, TableFilterValue>;
}

const DEFAULT_PAGE_SIZE = 10;

/** What a filter of a type has to say: its empty value, and how its value goes to and comes from the URL and the HTTP parameters. */
interface FilterCodec<V extends TableFilterValue> {
  empty(): V;
  fromUrl(f: TableFilterDef, params: HttpParams): V;
  toUrl(f: TableFilterDef, value: V, params: HttpParams): HttpParams;
  toHttp(f: TableFilterDef, value: V, params: HttpParams): HttpParams;
}

const httpName = (f: TableFilterDef) => f.httpParam ?? f.key;

const textCodec: FilterCodec<string> = {
  empty: () => '',
  fromUrl: (f, params) => params.get(f.key) || '',
  toUrl: (f, value, params) => value ? params.set(f.key, '' + value) : params,
  toHttp: (f, value, params) => value ? params.set(httpName(f), '' + value) : params
};

const listCodec: FilterCodec<string[]> = {
  empty: () => [],
  fromUrl: (f, params) => params.get(f.key) ? params.get(f.key).split(',') : [],
  toUrl: (f, value, params) => value.length > 0 ? params.set(f.key, value.join(',')) : params,
  toHttp: (f, value, params) => value.length > 0 ? params.set(httpName(f), value.join(',')) : params
};

/** Like `listCodec`, but the endpoint hears of it only when exactly one value is chosen. */
const selectCodec: FilterCodec<string[]> = {
  ...listCodec,
  toHttp: (f, value, params) => 'onlyWhenOne' in f && f.onlyWhenOne ?
    (value.length === 1 ? params.set(httpName(f), value[0]) : params) :
    listCodec.toHttp(f, value, params)
};

const booleanCodec: FilterCodec<boolean> = {
  empty: () => false,
  fromUrl: (f, params) => params.get(f.key) === 'true',
  toUrl: (f, value, params) => value ? params.set(f.key, 'true') : params,
  toHttp: (f, value, params) => value ? params.set(httpName(f), 'true') : params
};

/** The URL keys of a date range, and how a date is read from and written to them: a UTC string, or ms when the keys are given. */
function urlOfRange(f: TableFilterDef): [string, string, (v: string) => Date, (d: Date) => string] {
  if ('urlStart' in f && f.urlStart) {
    return [f.urlStart, f.urlEnd, v => new Date(Number(v)), d => '' + d.getTime()];
  }
  return [f.key + 'Start', f.key + 'End', v => new Date(v), d => d.toUTCString()];
}

const dateRangeCodec: FilterCodec<DateRange> = {
  empty: () => ({start: null, end: null}),
  fromUrl: (f, params) => {
    const [startKey, endKey, parse] = urlOfRange(f);
    return {
      start: params.get(startKey) ? parse(params.get(startKey)) : null,
      end: params.get(endKey) ? parse(params.get(endKey)) : null
    };
  },
  toUrl: (f, range, params) => {
    const [startKey, endKey, , format] = urlOfRange(f);
    if (range.start) {
      params = params.set(startKey, format(range.start));
    }
    if (range.end) {
      params = params.set(endKey, format(range.end));
    }
    return params;
  },
  toHttp: (f, range, params) => {
    if (!range.start && !range.end) {
      return params;
    }
    if ('httpStart' in f) {
      if (range.start) {
        params = params.set(f.httpStart, '' + range.start.getTime());
      }
      if (range.end) {
        params = params.set(f.httpEnd, '' + range.end.getTime());
      }
      return params;
    }
    return params.set(httpName(f),
      '[' + (range.start ? range.start.getTime() : '') + '~' + (range.end ? range.end.getTime() : '') + ']');
  }
};

const CODECS: Record<TableFilterDef['type'], FilterCodec<any>> = {
  text: textCodec,
  updater: listCodec,
  select: selectCodec,
  boolean: booleanCodec,
  dateRange: dateRangeCodec
};

function filterDefs(def: TableDef): TableFilterDef[] {
  return [
    ...(def.search ? [{type: 'text' as const, key: def.search.key, label: def.search.placeholder}] : []),
    ...(def.filters ?? [])
  ];
}

export function emptyQuery(def: TableDef): TableQuery {
  const values: Record<string, TableFilterValue> = {};
  for (const f of filterDefs(def)) {
    values[f.key] = CODECS[f.type].empty();
  }
  const defaultSort = def.defaultSort ?? {active: def.columns[0].id, direction: 'asc'};
  const sorts = (Array.isArray(defaultSort) ? defaultSort : [defaultSort]).map(s => ({active: s.active, direction: s.direction as string}));
  return {pageIndex: 0, pageSize: DEFAULT_PAGE_SIZE, sorts, values};
}

/**
 * Restores a query from the `q` URL parameter (base64 of a query string).
 * A table of `multiSort` keeps its sorts as `sortActives`/`sortDirections` (comma separated), the others as `sortActive`/`sortDirection`.
 */
export function queryFromUrl(def: TableDef, q: string | null | undefined, multiSort = false): TableQuery {
  const query = emptyQuery(def);
  const params = q ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

  if (multiSort) {
    if (params.get('sortActives')) {
      const actives = params.get('sortActives').split(',');
      const directions = (params.get('sortDirections') ?? '').split(',');
      query.sorts = actives.map((active, i) => ({active, direction: directions[i] || 'asc'}));
    }
  } else if (params.has('sortActive')) {
    // An empty one is a table that was left unsorted.
    const active = params.get('sortActive');
    query.sorts = active ? [{active, direction: params.get('sortDirection') || query.sorts[0]?.direction || ''}] : [];
  }

  query.pageIndex = params.get('pageIndex') ? Number(params.get('pageIndex')) : query.pageIndex;
  query.pageSize = params.get('pageSize') ? Number(params.get('pageSize')) : query.pageSize;

  for (const f of filterDefs(def)) {
    query.values[f.key] = CODECS[f.type].fromUrl(f, params);
  }
  return query;
}

/** The `q=...` URL parameter of a query. */
export function queryToUrl(def: TableDef, query: TableQuery, multiSort = false): string {
  let params = multiSort ?
    new HttpParams()
      .set('sortActives', query.sorts.map(s => s.active).join(','))
      .set('sortDirections', query.sorts.map(s => s.direction).join(',')) :
    new HttpParams()
      .set('sortActive', query.sorts[0]?.active ?? '')
      .set('sortDirection', query.sorts[0]?.direction ?? '');
  params = params
    .set('pageIndex', '' + query.pageIndex)
    .set('pageSize', '' + query.pageSize);

  for (const f of filterDefs(def)) {
    params = CODECS[f.type].toUrl(f, query.values[f.key], params);
  }
  return 'q=' + base64Encode(params.toString());
}

/** The HTTP parameters of a list request, in the convention that the endpoint declares in the definition. */
export function queryToHttpParams(def: TableDef, query: TableQuery): HttpParams {
  let params = new HttpParams()
    .set('pageIndex', '' + query.pageIndex)
    .set('pageSize', '' + query.pageSize);

  const sorts = query.sorts.filter(s => s.active && s.direction);
  if (def.sortParam === 'sortActive') {
    // These endpoints ask for the parameters of the sort even when the table is not sorted.
    params = params.set('sortActive', sorts[0]?.active ?? '').set('sortDirection', sorts[0]?.direction ?? '');
  } else if (def.sortParam === 'sortActives') {
    params = params.set('sortActives', sorts.map(s => s.active).join(',')).set('sortDirections', sorts.map(s => s.direction).join(','));
  } else if (def.sortParam === 'sortDirection') {
    params = params.set('sortDirection', sorts[0]?.direction ?? '');
  } else if (sorts.length > 0) {
    params = params.set('orderBy', sorts.map(s => (s.direction === 'desc' ? '-' : '+') + s.active).join(','));
  }

  for (const f of filterDefs(def)) {
    params = CODECS[f.type].toHttp(f, query.values[f.key], params);
  }
  return params;
}
