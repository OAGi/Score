import {contentChild, Directive, inject, Injector, input, OnInit, TemplateRef} from '@angular/core';
import {MatCell, MatColumnDef, MatHeaderCell, MatCellDef, MatHeaderCellDef} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatMultiSort} from 'ngx-mat-multi-sort';
import {CdkColumnDef, CdkHeaderCellDef} from '@angular/cdk/table';
import {TableColumnRef} from './table-columns-state';
import {ScoreTableViewRef} from './score-table-view.token';
import {ScoreTableColumnResizeDirective} from '../score-table-column-resize/score-table-column-resize.directive';

@Directive({selector: '[scoreHeaderCellDef]', hostDirectives: [MatHeaderCellDef]})
export class ScoreHeaderCellDefDirective {
}

/**
 * A column of a `score-table-view`, written like Material's `matColumnDef`:
 *
 * ```html
 * <ng-container scoreColumnDef="name">
 *   <td score-cell *scoreCellDef="let element">{{ element.name }}</td>
 * </ng-container>
 * ```
 * The header cell comes from the table definition (text, sortable, width, resizable). Only a column that needs a different
 * one writes it: `<th score-header-cell *scoreHeaderCellDef>...</th>`.
 *
 * The column must be in the table definition (`defineTable`); `[scoreColumnDef]="columns.column('name')"` has the id checked
 * when the template is compiled.
 */
@Directive({
  selector: '[scoreColumnDef]',
  hostDirectives: [MatColumnDef],
  // `mat-sort-header` in a header cell finds its sort by injection, but the cell is declared here, outside of the table.
  providers: [
    {provide: MatSort, useFactory: () => inject(ScoreTableViewRef).matSort()},
    {
      provide: MatMultiSort, useFactory: () => {
        const sort = inject(ScoreTableViewRef).matSort();
        return sort instanceof MatMultiSort ? sort : null;
      }
    }
  ]
})
export class ScoreColumnDefDirective implements OnInit {
  readonly scoreColumnDef = input.required<string | TableColumnRef>();
  readonly columnDef = inject(MatColumnDef, {self: true});
  /** Where the column lives in the injector tree: what a header cell made by the table view needs to be injected with. */
  readonly injector = inject(Injector);
  readonly headerCellDef = contentChild(ScoreHeaderCellDefDirective);

  /** The header cell of the table view, for a column that has none of its own. */
  defaultHeaderCell: { template: TemplateRef<any> } | undefined;
  private contentHeaderCell: CdkHeaderCellDef | undefined;

  constructor() {
    // `headerCell` is a content query of `CdkColumnDef`, which runs again (and assigns `undefined`) whenever a view with control flow
    // in the cell is created: a default header cell put there would be lost. So the column def gets a `headerCell` that falls back to it.
    Object.defineProperty(this.columnDef, 'headerCell', {
      configurable: true,
      get: () => this.contentHeaderCell ?? this.defaultHeaderCell,
      set: (headerCell: CdkHeaderCellDef | undefined) => this.contentHeaderCell = headerCell
    });
  }

  get id(): string {
    return this.columnDef.name;
  }

  ngOnInit() {
    const column = this.scoreColumnDef();
    this.columnDef.name = typeof column === 'string' ? column : column.id;
  }
}

@Directive({selector: '[scoreCellDef]', hostDirectives: [MatCellDef]})
export class ScoreCellDefDirective {
}

/** The `td` of a column. */
@Directive({selector: 'td[score-cell]', hostDirectives: [MatCell]})
export class ScoreCellDirective {
}

/**
 * The `th` of a column. The width comes from, and a resize goes to, the column state of the table;
 * add `mat-sort-header` for a sortable column.
 */
@Directive({selector: 'th[score-header-cell]', hostDirectives: [MatHeaderCell, ScoreTableColumnResizeDirective]})
export class ScoreHeaderCellDirective {
  constructor() {
    const table = inject(ScoreTableViewRef).table;
    const columnDef = inject(CdkColumnDef);
    const resizer = inject(ScoreTableColumnResizeDirective, {self: true});

    resizer.defaultWidth = table().width(columnDef.name);
    // Only a column of the table definition can be resized; a fixed column cannot.
    const column = table().def.columns.find(c => c.id === columnDef.name);
    resizer.resizable = !!column && column.resizable !== false;
    resizer.onResize.subscribe(e => table().resize(e.columnId, e.width));
  }
}

/**
 * Gives a column that has no header cell of its own the default one, made by `score-table-view` from the table definition.
 * `<ng-template scoreDefaultHeader [column]="...">` holds the template of that header.
 */
@Directive({selector: 'ng-template[scoreDefaultHeader]'})
export class ScoreDefaultHeaderDirective implements OnInit {
  readonly column = input.required<ScoreColumnDefDirective>({alias: 'scoreDefaultHeader'});
  private readonly template = inject(TemplateRef);

  ngOnInit() {
    this.column().defaultHeaderCell = {template: this.template};
  }
}
