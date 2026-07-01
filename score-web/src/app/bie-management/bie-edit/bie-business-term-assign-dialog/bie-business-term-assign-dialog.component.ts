import {Component, inject, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {SelectionModel} from '@angular/cdk/collections';
import {MatSnackBar} from '@angular/material/snack-bar';
import {finalize} from 'rxjs/operators';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {MatDatepicker} from '@angular/material/datepicker';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {PageRequest} from '../../../basis/basis';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {BusinessTermService} from '../../../business-term-management/domain/business-term.service';
import {
  BieToAssign,
  BusinessTermListEntry,
  BusinessTermListRequest
} from '../../../business-term-management/domain/business-term';
import {AuthService} from '../../../authentication/auth.service';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {
  SettingsPreferencesService
} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {
  ScoreTableColumnResizeDirective
} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {initFilter} from '../../../common/utility';

export interface BieBusinessTermAssignDialogData {
  // The BIE node the term is assigned to. bieType is 'ASBIE' or 'BBIE'; bieId is the asbieId/bbieId.
  bieId: number;
  bieType: string;
  den: string;
}

/**
 * Issue #1754: in-place Business Term assignment from the BIE editor. This is the "pick a term" step lifted from
 * the standalone AssignBusinessTermBtComponent wizard so the user never leaves the editor. The node context
 * arrives via MAT_DIALOG_DATA (no bieIds/bieTypes query-param plumbing and no confirmAsbieBbieListByIdAndType
 * round-trip — the node id is already known). It performs the assign itself (keeping the same uniqueness and
 * uniqueness check as the wizard and closes with {changed:true} so the caller refreshes only that node's
 * Business Term panel — never reloading the whole tree.
 */
@Component({
  standalone: false,
  selector: 'score-bie-business-term-assign-dialog',
  templateUrl: './bie-business-term-assign-dialog.component.html',
  styleUrls: ['./bie-business-term-assign-dialog.component.css']
})
export class BieBusinessTermAssignDialogComponent implements OnInit {
  private businessTermService = inject(BusinessTermService);
  private accountService = inject(AccountListService);
  private confirmDialogService = inject(ConfirmDialogService);
  private auth = inject(AuthService);
  private preferencesService = inject(SettingsPreferencesService);
  private snackBar = inject(MatSnackBar);
  dialogRef = inject(MatDialogRef<BieBusinessTermAssignDialogComponent>);
  data: BieBusinessTermAssignDialogData = inject(MAT_DIALOG_DATA);

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage = columns;
    this.syncDisplayedColumns();
    this.updateTableColumnsForBusinessTermPage();
  }

  loading = false;
  table: TableData<BusinessTermListEntry>;
  // Multi-select: assign every selected businessTermId to the current BIE.
  selection = new SelectionModel<number>(true, []);
  request: BusinessTermListRequest;
  biesToAssign: BieToAssign[] = [];
  highlightText: string;
  preferencesInfo: PreferencesInfo = new PreferencesInfo();
  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);

  typeCode = '';
  private readonly primaryIndicator = false;

  readonly defaultDisplayedColumns = [
    {id: 'select', name: '', isActive: true},
    {id: 'businessTerm', name: 'Business Term', isActive: true},
    {id: 'externalReferenceUri', name: 'External Reference URI', isActive: true},
    {id: 'externalReferenceId', name: 'External Reference ID', isActive: true},
    {id: 'definition', name: 'Definition', isActive: true},
    {id: 'lastUpdateTimestamp', name: 'Updated On', isActive: true},
  ];

  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  ngOnInit(): void {
    this.table = new TableData<BusinessTermListEntry>(this.defaultDisplayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<BusinessTermListEntry>(this.sort, false);

    this.request = new BusinessTermListRequest(undefined, new PageRequest(['businessTerm'], ['asc'], 0, 10));
    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;
    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    this.table.sortObservable.subscribe(() => this.onSearch());
    this.syncDisplayedColumns();
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };

    const bie = new BieToAssign();
    bie.bieId = this.data.bieId;
    bie.bieType = this.data.bieType;
    this.biesToAssign = [bie];

    forkJoin([
      this.accountService.getAccountNames(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([loginIds, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
      this.syncDisplayedColumns();
    });
    this.loadBusinessTermList();
  }

  onColumnsReset(): void {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBusinessTermPage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]): void {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;
  }

  onResizeWidth($event): void {
    switch ($event.name) {
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string): void {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBusinessTermPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  updateTableColumnsForBusinessTermPage(): void {
    this.preferencesService.updateTableColumnsForBusinessTermPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  private getDisplayedColumns(): string[] {
    const displayedColumns = ['select'];
    for (const column of this.columns) {
      switch (column.name) {
        case 'Business Term':
          if (column.selected) {
            displayedColumns.push('businessTerm');
          }
          break;
        case 'External Reference URI':
          if (column.selected) {
            displayedColumns.push('externalReferenceUri');
          }
          break;
        case 'External Reference ID':
          if (column.selected) {
            displayedColumns.push('externalReferenceId');
          }
          break;
        case 'Definition':
          if (column.selected) {
            displayedColumns.push('definition');
          }
          break;
        case 'Updated On':
          if (column.selected) {
            displayedColumns.push('lastUpdateTimestamp');
          }
          break;
      }
    }
    return displayedColumns;
  }

  private syncDisplayedColumns(): void {
    if (this.table) {
      this.table.displayedColumns = this.getDisplayedColumns();
    }
  }

  onChange(property?: string, source?): void {
  }

  reset(type: string): void {
    switch (type) {
      case 'startDate':
        this.dateStart.select(undefined);
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.dateEnd.select(undefined);
        this.request.updatedDate.end = null;
        break;
    }
  }

  onPageChange(_event: PageEvent): void {
    this.loadBusinessTermList();
  }

  onSearch(): void {
    this.paginator.pageIndex = 0;
    this.loadBusinessTermList();
  }

  loadBusinessTermList(): void {
    this.loading = true;
    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.businessTermService.getBusinessTermList(this.request, this.biesToAssign).pipe(
      finalize(() => this.loading = false)
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list;
      this.highlightText = this.request.filters.definition;
    }, _error => {
      this.table.dataSource.data = [];
    });
  }

  select(row: BusinessTermListEntry): void {
    // `row.used` is a catalog-level deletion guard, not an assignment guard.
    this.selection.select(row.businessTermId);
  }

  toggle(row: BusinessTermListEntry): void {
    if (this.isSelected(row)) {
      this.selection.deselect(row.businessTermId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessTermListEntry): boolean {
    return this.selection.isSelected(row.businessTermId);
  }

  private visibleRows(): BusinessTermListEntry[] {
    return this.table?.dataSource?.data || [];
  }

  isAllSelected(): boolean {
    const rows = this.visibleRows();
    return rows.length > 0 && rows.every(row => this.isSelected(row));
  }

  isPartiallySelected(): boolean {
    const rows = this.visibleRows();
    return rows.some(row => this.isSelected(row)) && !this.isAllSelected();
  }

  masterToggle(): void {
    if (this.isAllSelected()) {
      this.visibleRows().forEach(row => this.selection.deselect(row.businessTermId));
    } else {
      this.visibleRows().forEach(row => this.select(row));
    }
  }

  isCreateDisabled(): boolean {
    return this.loading || this.selection.selected.length === 0;
  }

  create(): void {
    if (this.isCreateDisabled()) {
      return;
    }
    const businessTermIds = [...this.selection.selected];
    this.loading = true;
    this.checkUniqueness(
      businessTermIds,
      () => this.doCreate(businessTermIds),
      () => this.loading = false
    );
  }

  private checkUniqueness(businessTermIds: number[], callbackFn: () => void, doneFn: () => void): void {
    forkJoin(businessTermIds.map(businessTermId =>
      this.businessTermService.checkAssignmentUniqueness(this.data.bieId, this.data.bieType,
        businessTermId, this.typeCode, this.primaryIndicator)
    )).subscribe({
      next: isUniques => {
        if (isUniques.some(isUnique => !isUnique)) {
          doneFn();
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Invalid parameters';
          dialogConfig.data.content = [
            'One or more selected business term assignments for the same BIE and type code already exist!'
          ];
          this.confirmDialogService.open(dialogConfig);
          return;
        }
        callbackFn();
      },
      error: () => {
        doneFn();
        this.snackBar.open('Failed to validate the selected business terms.', '', {duration: 5000});
      }
    });
  }

  private doCreate(businessTermIds: number[]): void {
    forkJoin(businessTermIds.map(businessTermId =>
      this.businessTermService.assignBusinessTermToBie(businessTermId, this.biesToAssign,
        this.primaryIndicator, this.typeCode)
    )).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: () => {
        this.snackBar.open(
          businessTermIds.length === 1 ? 'Business term assigned.' : 'Business terms assigned.',
          '', {duration: 3000});
        this.dialogRef.close({changed: true});
      },
      error: (err) => {
        const message = (err && err.error && err.error.message)
          ? err.error.message : 'Failed to assign the selected business terms.';
        this.snackBar.open(message, '', {duration: 5000});
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
