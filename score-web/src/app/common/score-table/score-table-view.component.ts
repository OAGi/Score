import {animate, state, style, transition, trigger} from '@angular/animations';
import {
  afterNextRender,
  AfterContentInit,
  booleanAttribute,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  forwardRef,
  contentChild,
  contentChildren,
  inject,
  Injector,
  input,
  output,
  isDevMode,
  OnInit,
  effect,
  signal,
  untracked,
  viewChild,
  viewChildren
} from '@angular/core';
import {AsyncPipe, Location, NgClass, NgTemplateOutlet} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {HttpParams} from '@angular/common/http';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {DataSource, SelectionModel} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {Observable, ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {PageResponse} from '../../basis/basis';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MaterialModule} from '../../material.module';
import {ColumnSelectorModule} from '../column-selector/column-selector.module';
import {SearchBarModule} from '../search-bar/search-bar.module';
import {SearchBarComponent} from '../search-bar/search-bar.component';
import {ScoreCommonModule} from '../score-common.module';
import {ScoreTableColumnResizeDirective} from '../score-table-column-resize/score-table-column-resize.directive';
import {initFilter} from '../utility';
import {ScoreTableViewRef} from './score-table-view.token';
import {ScoreColumnDefDirective, ScoreDefaultHeaderDirective, ScoreHeaderCellDirective} from './score-column-def.directive';
import {ScoreTableDirective} from './score-table.directive';
import {ScoreDetailDefDirective} from './score-detail-def.directive';
import {DateRange, QuerySort, queryFromUrl, queryToHttpParams, queryToUrl, TableFilterValue, TableQuery} from './table-query';
import {TableColumnsState} from './table-columns-state';
import {TableDef, TableFilterDef, TableFilterOption} from './table-def';

/**
 * A table with everything around it, as a self-contained component: search bar and advanced filters, column selector,
 * sortable/resizable table, row selection, paginator, loading indicator and the query state in the URL (`q=`).
 * It knows nothing about the page around it, so it can be put in `score-list-page`, `score-detail-page`,
 * a wizard step or a dialog alike.
 *
 * ```html
 * <score-table-view [table]="columns" [load]="load" [selection]="selection" [rowKey]="rowKey">
 *   <ng-container scoreColumnDef="name">
 *     <td score-cell *scoreCellDef="let row">...</td>
 *   </ng-container>
 * </score-table-view>
 * ```
 * Columns, filters and the endpoint's parameter convention all come from the table definition (`defineTable`);
 * the screen only gives the loader and the cell templates of the columns.
 *
 * A row can have a detail row that opens under it when it is clicked (`*scoreDetailDef`), a class (`rowClass`) and a click
 * handler (`rowClick`). Fixed columns (`fixed` in the definition) are shown beside the selectable ones, like the checkbox column.
 *
 * In a dialog: `[syncUrl]="false"` keeps the URL out of it, `singleSelect` lets one row be chosen, `[extraParams]`/`[ready]` bring the
 * parameters that the screen decides (and holds the first load back until they are there), and the screen can put its own things
 * before the search box (`[searchPrefix]`, with `advancedSearchOffset` as wide as it) and among the advanced filters (`[advancedFilters]`).
 *
 * `multiSort` sorts by several columns (`MatMultiSort`, with the sort chips of `ngx-mat-multi-sort`) instead of one (`MatSort`).
 * It is read once, when the view is created.
 */
@Component({
  selector: 'score-table-view',
  templateUrl: './score-table-view.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    AsyncPipe, NgClass, NgTemplateOutlet, ScoreDefaultHeaderDirective, ScoreHeaderCellDirective, ScoreTableDirective, FormsModule, ReactiveFormsModule, MaterialModule, ColumnSelectorModule, SearchBarModule,
    ScoreCommonModule, TranslatePipe
  ],
  providers: [{provide: ScoreTableViewRef, useExisting: forwardRef(() => ScoreTableViewComponent)}],
  styleUrl: './score-table-view.component.css',
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class ScoreTableViewComponent<T = any> implements OnInit, AfterContentInit {
  private readonly injector = inject(Injector);
  private readonly destroyRef = inject(DestroyRef);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);

  /** Column state (from `createTableColumns`) of the defined table. */
  readonly table = input.required<TableColumnsState<TableDef>>();
  /** Fetches a page. The parameters are already in the endpoint's convention (see the definition). */
  readonly load = input.required<(params: HttpParams) => Observable<PageResponse<T>>>();
  /** Gives the table a checkbox column when set. */
  readonly selection = input<SelectionModel<any>>();
  readonly rowKey = input<(row: T) => any>(row => row);
  /** All the rows of a table whose rows the screen holds (see `inMemoryLoad`): the checkbox of the header selects them, not just the page. */
  readonly allRows = input<(() => readonly T[]) | null>(null);
  /** Rows that cannot be selected (e.g. in use). */
  readonly rowSelectable = input<(row: T) => boolean>(() => true);
  readonly unselectableTooltip = input('');
  readonly pageSizeOptions = input([10, 25, 50]);
  /** The direction of the first click on a sortable header. */
  readonly sortStart = input<'asc' | 'desc'>('desc');
  /** Highlights the row under the pointer. */
  readonly rowHover = input(false, {transform: booleanAttribute});
  /** The choices of the `select` filters that have no `options` in the definition, by the filter's key. */
  readonly filterOptions = input<Record<string, readonly TableFilterOption[]>>({});
  /**
   * Columns that the screen does not show (e.g. a menu column for a role, or a column of a shared definition that the screen has no use for).
   * Their column definitions are still needed.
   */
  readonly hiddenColumns = input<readonly string[]>([]);
  /** A class (or classes) for a row, e.g. for an invalid one. */
  readonly rowClass = input<(row: T) => string | string[] | Record<string, boolean> | null | undefined>(() => null);
  /** A click on a row (a click also opens the detail row, and selects the row if the table has `selection`). */
  readonly rowClick = output<T>();
  /** Whether a click on a row selects it, when the table has `selection`. */
  readonly selectOnRowClick = input(true, {transform: booleanAttribute});
  /** Only one row can be selected. */
  readonly singleSelect = input(false, {transform: booleanAttribute});
  /** `false` leaves the checkbox that selects all rows out of the header. */
  readonly selectAll = input(true, {transform: booleanAttribute});
  /**
   * Whether the query state lives in the URL (`q=`): read when the view is created, written with every search.
   * A view in a dialog has to leave it out.
   */
  readonly syncUrl = input(true, {transform: booleanAttribute});
  /**
   * Parameters that the screen decides, e.g. the library and release a list belongs to. They go to the loader with the rest;
   * a change searches again (from the first page). A `null`/`undefined` value leaves the parameter out.
   */
  readonly extraParams = input<Record<string, string | null | undefined>>({});
  /** The screen is busy with something else (e.g. an action on the selected row): shows the loading indicator too. */
  readonly busy = input(false);
  /** `false` holds back the first load until the screen has what `extraParams` needs. */
  readonly ready = input(true);
  /** Take the page index from the response (an endpoint that corrects the page it is asked for). */
  readonly pageFromResponse = input(false, {transform: booleanAttribute});
  /** Left space of the advanced search, as wide as what the screen puts before the search box (`[searchPrefix]`). */
  readonly advancedSearchOffset = input('3rem');
  /** `false` leaves out the column selector (and the `[toolbar]` content with it): the columns cannot be chosen. */
  readonly columnSelector = input(true, {transform: booleanAttribute});
  /** A search by text has no order of its own to keep: typing in the search box leaves the table unsorted (a single sort only). */
  readonly searchClearsSort = input(false, {transform: booleanAttribute});
  /** The search box is read only: what it says is given by the screen (`setFilter`), and cannot be changed. */
  readonly searchDisabled = input(false, {transform: booleanAttribute});
  /** The advanced search is open at first, unless the URL says that it was left closed (it is open there when it was left open). */
  readonly advancedSearchOpen = input(false, {transform: booleanAttribute});
  /** Sort by several columns instead of one. Not changeable after the view is created. */
  readonly multiSort = input(false, {transform: booleanAttribute});
  /** The chips of the sorts above the table of a `multiSort`. */
  readonly multiSortSettings = input(true, {transform: booleanAttribute});

  private readonly columnDefs = contentChildren(ScoreColumnDefDirective);
  /** The detail row of every row; the rows have none without it. */
  protected readonly detailDef = contentChild(ScoreDetailDefDirective);
  /** The row whose detail row is open. */
  readonly expanded = signal<T | null>(null);
  private readonly paginator = viewChild.required(MatPaginator);
  /** The search bar; a table without a search box or filters has none. */
  private readonly searchBar = viewChild(SearchBarComponent);
  private readonly resizers = viewChildren(ScoreTableColumnResizeDirective);

  /** Rows of the table: `MatTableDataSource`, or for `multiSort` the one of `ngx-mat-multi-sort`. */
  dataSource: DataSource<T> & { data: T[] } = new MatTableDataSource<T>();
  readonly loading = signal(false);
  /** The state of the sorts of a `multiSort`, which `mat-multi-sort-table-settings` works on. */
  readonly tableData = signal<TableData<T> | null>(null);
  private sort: MatSort;

  /** What the user is editing in the filter fields. */
  query: TableQuery;
  /** The filter values of the last search, e.g. for highlighting. */
  private appliedValues: Record<string, TableFilterValue> = {};

  /** The columns that the screen gave no header cell: they get the default one. */
  protected readonly defaultHeaderColumns = computed(() => this.columnDefs().filter(c => !c.headerCellDef()));

  readonly displayedColumns = computed(() => [
    ...(this.selection() ? ['select'] : []),
    ...this.table().fixed('start').filter(id => !this.hiddenColumns().includes(id)),
    ...this.table().displayed().filter(id => !this.hiddenColumns().includes(id)),
    ...this.table().fixed('end').filter(id => !this.hiddenColumns().includes(id))
  ]);

  /** The choices of every `updater` filter, which the select search narrows. */
  readonly updaterFilters: Record<string, { ctrl: FormControl; options: ReplaySubject<string[]> }> = {};
  protected initialAdvancedSearch = false;
  private started = false;
  private loaded = false;

  constructor() {
    // A cell is declared by the screen but drawn here, so an event in it does not mark the screen's own view as changed: what the screen
    // shows from the state that the cell changes (e.g. a button enabled by a checkbox) would be stale. Capturing the events (a checkbox
    // stops them from bubbling) marks this view and everything above it, the screen included.
    const host: HTMLElement = inject(ElementRef).nativeElement;
    const markForCheck = () => this.changeDetector.markForCheck();
    for (const type of ['click', 'change', 'input', 'keydown']) {
      host.addEventListener(type, markForCheck, true);
      this.destroyRef.onDestroy(() => host.removeEventListener(type, markForCheck, true));
    }
    // The rows are drawn in the table, which is OnPush: a checkbox that the selection changes is drawn on the next change only, so a
    // click on a row would need another click to show. Draw the rows as soon as the selection changes.
    effect(onCleanup => {
      const subscription = this.selection()?.changed.subscribe(() => {
        if (this.started) {
          this.changeDetector.detectChanges();
        }
      });
      onCleanup(() => subscription?.unsubscribe());
    });
    // The table is in an `@if`, so its sort is known only after the first render.
    afterNextRender(() => this.start());
    // The screen decides again: search again.
    effect(() => {
      this.extraParams();
      untracked(() => {
        if (this.loaded) {
          this.onSearch();
        }
      });
    });
    // The first load waits until the screen is ready.
    effect(() => {
      if (this.ready()) {
        untracked(() => this.startLoading());
      }
    });
    // The sort chips list the columns that the user has chosen.
    effect(() => {
      const tableData = this.tableData();
      if (tableData) {
        tableData.displayedColumns = this.table().displayed();
      }
    });
  }

  /** The search boxes of the `select` filters that are `searchable`. */
  readonly selectSearch: Record<string, FormControl> = {};

  /** The choices of a `select` filter that are shown now: all of them, or those that the search box matches. */
  protected visibleOptions(f: TableFilterDef): readonly TableFilterOption[] {
    const all = (f as { options?: readonly TableFilterOption[] }).options ?? this.filterOptions()[f.key] ?? [];
    const text = (this.selectSearch[f.key]?.value ?? '').toLowerCase();
    return text ? all.filter(o => o.label.toLowerCase().includes(text)) : all;
  }

  /** The advanced search is offered when there is a filter to show in it (a hidden one does not count). */
  get hasAdvancedSearch(): boolean {
    return !!this.def.filters?.some(f => !f.hidden);
  }

  protected onSearchModelChange(value: string) {
    this.query.values[this.def.search.key] = value;
    if (this.searchClearsSort() && !!value && !this.multiSort()) {
      this.setSort('', '');
    }
  }

  /** Sorts the table by a column (none: `''`) from now on, without searching: the screen searches (`onSearch`). */
  setSort(active: string, direction: SortDirection) {
    this.sort.active = active;
    this.sort.direction = direction;
  }

  /** Whether there is anything to search with. */
  get hasSearch(): boolean {
    return !!this.def.search || !!this.def.filters?.length;
  }

  get def(): TableDef {
    return this.table().def;
  }

  ngOnInit() {
    const paramMap = this.syncUrl() ? this.route.snapshot.queryParamMap : null;
    this.query = queryFromUrl(this.def, paramMap?.get('q'), this.multiSort());
    const advancedSearch = paramMap?.get('adv_ser');
    this.initialAdvancedSearch = advancedSearch === 'true' || (this.advancedSearchOpen() && advancedSearch !== 'false');

    const paginator = this.paginator();
    paginator.pageIndex = this.query.pageIndex;
    paginator.pageSize = this.query.pageSize;
    paginator.length = 0;

    (this.def.filters ?? []).filter(f => f.type === 'select' && f.searchable).forEach(f => this.selectSearch[f.key] = new FormControl());
    const updaters = (this.def.filters ?? []).filter(f => f.type === 'updater');
    if (updaters.length > 0) {
      // Only a table with an updater filter needs the accounts.
      updaters.forEach(f => this.updaterFilters[f.key] = {ctrl: new FormControl(), options: new ReplaySubject<string[]>(1)});
      this.injector.get(AccountListService).getAccountNames().subscribe(loginIds => {
        updaters.forEach(f => initFilter(this.updaterFilters[f.key].ctrl, this.updaterFilters[f.key].options, [...loginIds]));
      });
    }
  }

  /** Once the table exists: connects the sort and loads the first page. */
  private start() {
    const sort = this.sort;
    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = sort.sort;
    sort.sort = (sortChange) => {
      if (this.resizers().some(e => e.resizing)) {
        return;
      }
      originalSort.apply(sort, [sortChange]);
    };

    if (sort instanceof MatMultiSort) {
      const tableData = new TableData<T>(this.def.columns.map(c => ({id: c.id, name: c.sortLabel ?? c.label})), {});
      this.dataSource = new MatMultiSortTableDataSource<T>(sort, false);
      tableData.dataSource = this.dataSource as MatMultiSortTableDataSource<T>;
      const sorts = this.query.sorts;
      tableData.sortParams = sorts.map(s => s.active);
      tableData.sortDirs = sorts.map(s => s.direction);
      tableData.sortObservable.subscribe(() => this.onSearch());
      this.tableData.set(tableData);
    } else {
      sort.sortChange.subscribe(() => this.onSearch());
    }

    this.started = true;
    this.startLoading();
  }

  /** The first load, once the table exists and the screen is ready. */
  private startLoading() {
    if (this.started && !this.loaded && this.ready()) {
      this.loaded = true;
      this.reload(true);
    }
  }

  registerTable(table: MatTable<any>, sort: MatSort) {
    this.sort = sort;
    // The column definitions are written by the screen, outside of the table: hand them over.
    for (const columnDef of this.columnDefs()) {
      table.addColumnDef(columnDef.columnDef);
    }
    this.destroyRef.onDestroy(() => this.columnDefs().forEach(c => table.removeColumnDef(c.columnDef)));
  }

  initialSorts(): QuerySort[] {
    return this.query.sorts;
  }

  /** The table's `(matSortChange)` of a `multiSort`: the sorts are in `TableData`. */
  protected onMultiSortChange() {
    this.tableData()?.onSortEvent();
  }

  ngAfterContentInit() {
    if (!isDevMode()) {
      return;
    }
    const defined = [...this.def.columns.map(c => c.id), ...(this.def.fixed ?? []).map(c => c.id)];
    const given = this.columnDefs().map(c => c.id);
    const missing = defined.filter(id => !given.includes(id));
    const unknown = given.filter(id => !defined.includes(id));
    if (missing.length > 0 || unknown.length > 0) {
      throw new Error(`score-table-view '${this.def.storageKey}': missing scoreColumnDef for [${missing}], ` +
        `scoreColumnDef of undefined column [${unknown}].`);
    }
  }

  /** What the default header needs to know about a column of the definition or a fixed one. */
  protected columnOf(id: string): { label?: string; header?: string; sortable?: boolean } {
    return this.def.columns.find(c => c.id === id) ?? this.def.fixed?.find(c => c.id === id);
  }

  matSort(): MatSort {
    return this.sort;
  }

  /** The sorts as the table has them now. */
  private currentSorts(): QuerySort[] {
    const tableData = this.tableData();
    if (tableData) {
      return tableData.sortParams.map((active, i) => ({active, direction: tableData.sortDirs[i]}));
    }
    return this.sort.active ? [{active: this.sort.active, direction: this.sort.direction}] : [];
  }

  /** A filter value of the last search (not what is being typed). */
  applied(key: string): any {
    return this.appliedValues[key];
  }

  onSearch() {
    this.paginator().pageIndex = 0;
    this.reload();
  }

  onPage() {
    this.reload();
  }

  reload(isInit = false) {
    this.loading.set(true);

    this.query.sorts = this.currentSorts();
    this.query.pageIndex = this.paginator().pageIndex;
    this.query.pageSize = this.paginator().pageSize;

    let params = queryToHttpParams(this.def, this.query);
    for (const [name, value] of Object.entries(this.extraParams())) {
      if (value !== null && value !== undefined) {
        params = params.set(name, value);
      }
    }

    this.load()(params).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: resp => {
        this.paginator().length = resp.length;
        if (this.pageFromResponse()) {
          this.paginator().pageIndex = resp.page;
        }
        this.dataSource.data = resp.list;
        this.appliedValues = structuredClone(this.query.values);

        if (!isInit && this.syncUrl()) {
          this.location.replaceState(this.router.url.split('?')[0],
            queryToUrl(this.def, this.query, this.multiSort()) + '&adv_ser=' + (this.searchBar()?.showAdvancedSearch ?? false));
        }
      },
      error: () => this.dataSource.data = []
    });
  }

  /** Sets a filter by the screen (a hidden one, or from an action); searching is the screen's call (`reload` or `onSearch`). */
  setFilter(key: string, value: TableFilterValue) {
    this.query.values[key] = value;
  }

  /** The value of a filter as it is now (not as of the last search: that is `applied`); none before the view is initialized. */
  filterValue(key: string): TableFilterValue | undefined {
    return this.query?.values[key];
  }

  dateRange(key: string): DateRange {
    return this.query.values[key] as DateRange;
  }

  // --- rows ---
  /** A click on a row. */
  protected onRowClick(row: T) {
    this.rowClick.emit(row);
    if (this.detailDef()) {
      this.expanded.update(current => current === row ? null : row);
    }
    if (this.selection() && this.selectOnRowClick()) {
      this.toggle(row);
    }
  }

  /** A click on a detail row only selects. */
  protected onDetailClick(row: T) {
    if (this.selection() && this.selectOnRowClick()) {
      this.toggle(row);
    }
  }

  // --- selection ---
  isSelected(row: T): boolean {
    return this.selection().isSelected(this.rowKey()(row));
  }

  /** Whether all the rows that can be selected are. */
  isAllSelected(): boolean {
    return this.selection().selected.length === this.selectableRows().filter(row => this.rowSelectable()(row)).length;
  }

  masterToggle() {
    if (this.isAllSelected()) {
      this.selection().clear();
    } else {
      this.selectableRows().forEach(row => this.select(row));
    }
  }

  /** The rows that the checkbox of the header selects: the page, or all of them for a table of `allRows`. */
  private selectableRows(): readonly T[] {
    return this.allRows()?.() ?? this.dataSource.data;
  }

  select(row: T) {
    if (this.rowSelectable()(row)) {
      if (this.singleSelect()) {
        this.selection().clear();
      }
      this.selection().select(this.rowKey()(row));
    }
  }

  toggle(row: T) {
    if (this.isSelected(row)) {
      this.selection().deselect(this.rowKey()(row));
    } else {
      this.select(row);
    }
  }
}
