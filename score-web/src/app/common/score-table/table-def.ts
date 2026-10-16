import {SortDirection} from '@angular/material/sort';

/** A column of a table. `label` is what the user sees in the column selector, and also the name persisted in localStorage. */
export interface TableColumnDef {
  readonly id: string;
  readonly label: string;
  /** Header text when it differs from `label`. */
  readonly header?: string;
  /** Default width: pixels, a CSS length, or `0` for "fill the rest". */
  readonly width: number | string;
  /** `false` for a column that is hidden until the user selects it. */
  readonly selected?: boolean;
  readonly sortable?: boolean;
  /** The name of the column in the sort chips of a `multiSort` table, when it is not `label`. */
  readonly sortLabel?: string;
  /** `false` for a column that cannot be resized. */
  readonly resizable?: boolean;
}

/** A column that is always shown, and not in the column selector: e.g. a row menu. The checkbox column comes with `selection`. */
export interface TableFixedColumnDef {
  readonly id: string;
  readonly position: 'start' | 'end';
  /** Header text; none by default. */
  readonly header?: string;
  readonly width?: number | string;
}

interface FilterBase {
  /** The key in the URL (`q=`), and the HTTP parameter name unless `httpParam` says otherwise. */
  readonly key: string;
  readonly label: string;
  readonly httpParam?: string;
  /** Not shown among the advanced search fields, but still part of the query (URL and HTTP): the screen sets its value (`setFilter`). */
  readonly hidden?: boolean;
}

/** A choice of a `select` filter. */
export interface TableFilterOption {
  readonly value: string;
  readonly label: string;
}

/** Filter of a table. */
export type TableFilterDef =
  | (FilterBase & { readonly type: 'text' })
  /** Multiple login ids chosen from all accounts; URL and HTTP: comma separated. */
  | (FilterBase & { readonly type: 'updater' })
  /**
   * Multiple values chosen from `options`, or, when there are none, from what the screen gives for the filter's key in
   * `filterOptions` (choices that it has to fetch); URL and HTTP: comma separated. `searchable` adds a search box to the choices.
   * `onlyWhenOne`: the endpoint is told only when exactly one is chosen (for yes/no choices: both is the same as none).
   */
  | (FilterBase & {
    readonly type: 'select'; readonly options?: readonly TableFilterOption[]; readonly searchable?: boolean; readonly onlyWhenOne?: boolean
  })
  /** A checkbox; URL and HTTP: `true` when checked, left out otherwise. */
  | (FilterBase & { readonly type: 'boolean' })
  /**
   * URL: `<key>Start`, `<key>End` (UTC string), or, with `urlStart`/`urlEnd`, those keys with the time in ms.
   * HTTP: `<httpParam>=[<start ms>~<end ms>]`, or, for an endpoint that wants the two ends apart, `<httpStart>=<start ms>` and
   * `<httpEnd>=<end ms>`.
   */
  | (FilterBase & { readonly type: 'dateRange'; readonly urlStart?: string; readonly urlEnd?: string }
    & ({ readonly httpParam: string } | { readonly httpStart: string; readonly httpEnd: string }));

export interface TableSort {
  readonly active: string;
  readonly direction: SortDirection;
}

export interface TableDef {
  /** localStorage key (`X-Score-<storageKey>[<username>]`). Must stay as it was, or every user's column settings reset. */
  readonly storageKey: string;
  readonly columns: readonly TableColumnDef[];
  /** Columns that are always shown, before or after `columns`. They have no sort and cannot be resized. */
  readonly fixed?: readonly TableFixedColumnDef[];
  /** One sort; a table of `multiSort` can have several, in order of priority. */
  readonly defaultSort?: TableSort | readonly TableSort[];
  /** The always visible search box. */
  readonly search?: { readonly key: string; readonly placeholder: string };
  /** The advanced search fields, in display order. */
  readonly filters?: readonly TableFilterDef[];
  /**
   * How the endpoint wants the sort: `orderBy=-field` (default), the legacy `sortActive`/`sortDirection`, or only the
   * `sortDirection` (an endpoint that sorts by one thing and takes just the direction), or `sortActives`/`sortDirections` (comma
   * separated lists, for several sorts).
   */
  readonly sortParam?: 'orderBy' | 'sortActive' | 'sortDirection' | 'sortActives';
}

/** Ids of the columns of a table. */
export type TableColumnId<D extends TableDef> = D['columns'][number]['id'];
/** Ids of the fixed columns of a table. */
export type TableFixedColumnId<D extends TableDef> =
  D extends { readonly fixed: infer F extends readonly { readonly id: string }[] } ? F[number]['id'] : never;

/**
 * Declares a table once. The `const` type parameter keeps the column ids as literal types.
 */
export function defineTable<const D extends TableDef>(def: D): D {
  return def;
}
