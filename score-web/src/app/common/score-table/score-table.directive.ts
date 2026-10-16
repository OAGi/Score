import {Directive, inject} from '@angular/core';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {MatMultiSort} from 'ngx-mat-multi-sort';
import {ScoreTableViewRef} from './score-table-view.token';

/**
 * On the `table` of a `score-table-view`, next to `matSort` or `matMultiSort`: hands the table and its sort over to the view
 * (the table is in an `@if`, so the view cannot query it), and gives a `matMultiSort` its starting sorts before its headers are
 * drawn (`matSort` takes them as `matSortActive`/`matSortDirection`).
 */
@Directive({selector: 'table[scoreTable]'})
export class ScoreTableDirective {
  constructor() {
    const view = inject(ScoreTableViewRef);
    const multiSort = inject(MatMultiSort, {self: true, optional: true});
    const sort = multiSort ?? inject(MatSort, {self: true});

    view.registerTable(inject(MatTable, {self: true}), sort);
    if (multiSort) {
      const sorts = view.initialSorts();
      multiSort.actives = sorts.map(s => s.active);
      multiSort.directions = sorts.map(s => s.direction as SortDirection);
    }
  }
}
