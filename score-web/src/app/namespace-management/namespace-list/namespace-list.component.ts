import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountList} from '../../account-management/domain/accounts';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {NamespaceList, NamespaceListRequest} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';
import {PageRequest} from '../../basis/basis';
import {initFilter, loadLibrary, saveLibrary} from '../../common/utility';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {Location} from '@angular/common';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AuthService} from '../../authentication/auth.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-namespace-list',
  templateUrl: './namespace-list.component.html',
  styleUrls: ['./namespace-list.component.css']
})
export class NamespaceListComponent implements OnInit {

  title = 'Namespace';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfNamespacePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfNamespacePage = columns;
    this.updateTableColumnsForNamespacePage();
  }

  updateTableColumnsForNamespacePage() {
    this.preferencesService.updateTableColumnsForNamespacePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfNamespacePage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;
  }

  onResizeWidth($event) {
    switch ($event.name) {
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForNamespacePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = [];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'URI':
          if (column.selected) {
            displayedColumns.push('uri');
          }
          break;
        case 'Prefix':
          if (column.selected) {
            displayedColumns.push('prefix');
          }
          break;
        case 'Owner':
          if (column.selected) {
            displayedColumns.push('owner');
          }
          break;
        case 'Standard':
          if (column.selected) {
            displayedColumns.push('std');
          }
          break;
        case 'Description':
          if (column.selected) {
            displayedColumns.push('description');
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

  dataSource = new MatTableDataSource<NamespaceList>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  libraries: Library[] = [];
  mappedLibraries: {library: Library, selected: boolean}[] = [];
  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: NamespaceListRequest;
  highlightText: string;
  preferencesInfo: PreferencesInfo;

  contextMenuItem: NamespaceList;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private service: NamespaceService,
              private libraryService: LibraryService,
              private accountService: AccountListService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.request = new NamespaceListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      this.searchBar.showAdvancedSearch =
        (this.route.snapshot.queryParamMap && this.route.snapshot.queryParamMap.get('adv_ser') === 'true');

      this.paginator.pageIndex = this.request.page.pageIndex;
      this.paginator.pageSize = this.request.page.pageSize;
      this.paginator.length = 0;

      this.sort.active = this.request.page.sortActive;
      this.sort.direction = this.request.page.sortDirection as SortDirection;
      // Prevent the sorting event from being triggered if any columns are currently resizing.
      const originalSort = this.sort.sort;
      this.sort.sort = (sortChange) => {
        if (this.tableColumnResizeDirectives &&
          this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
          return;
        }
        originalSort.apply(this.sort, [sortChange]);
      };
      this.sort.sortChange.subscribe(() => {
        this.onSearch();
      });

      forkJoin([
        this.accountService.getAccountNames(),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([loginIds, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.loadNamespaceList(true);
      });
    });
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'startDate':
        this.request.updatedDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.request.updatedDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
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

  onChange(property?: string, source?) {
  }

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      }
      if (!this.request.library || this.request.library.libraryId === 0) {
        this.request.library = this.libraries[0];
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.request.library.libraryId === e.libraryId)};
      });
    }
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
    this.onSearch();
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadNamespaceList();
  }

  loadNamespaceList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getNamespaceList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
      this.highlightText = this.request.filters.description;

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: NamespaceList) {
    this.selection.select(row.namespaceId);
  }

  toggle(row: NamespaceList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.namespaceId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: NamespaceList) {
    return this.selection.isSelected(row.namespaceId);
  }

  create() {
    this.router.navigateByUrl('/namespace/create');
  }

  isEditable(item: NamespaceList): boolean {
    if (!item) {
      return false;
    }
    return (item.owner === this.auth.getUserToken().username);
  }

  discard(item: NamespaceList) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Namespace?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this namespace?',
      'The namespace will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.discard(item.namespaceId).pipe(finalize(() => {
            this.loading = false;
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadNamespaceList();
          });
        }
      });
  }

  openTransferDialog(item: NamespaceList) {
    if (!this.isEditable(item)) {
      return;
    }
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.namespaceId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadNamespaceList();
        });
      }
    });
  }

}
