import type {Signal} from '@angular/core';
import type {MatSort} from '@angular/material/sort';
import type {MatTable} from '@angular/material/table';
import type {QuerySort} from './table-query';
import type {TableColumnsState} from './table-columns-state';

/**
 * What `score-table-view` offers to what is placed inside it (column definitions, header cells, cells, the table) and to a page that
 * hosts it (e.g. `score-list-page`). They look for this token instead of the component itself, so that none of them depends on it.
 */
export abstract class ScoreTableViewRef {
  /** The column state of the defined table. */
  abstract readonly table: Signal<TableColumnsState>;

  /** The sort of its table (`MatSort`, or `MatMultiSort` for `multiSort`). A column's header cell is declared outside the table, so it cannot find it by itself. */
  abstract matSort(): MatSort;

  /** The table hands itself and its sort over when it is created. */
  abstract registerTable(table: MatTable<any>, sort: MatSort): void;

  /** The sorts to start with, from the URL or the definition. */
  abstract initialSorts(): QuerySort[];
}
