import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {ReleaseService} from '../domain/release.service';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, Router} from '@angular/router';
import {ReleaseList, ReleaseListRequest, SimpleRelease, WorkingRelease} from '../domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadLibrary, saveLibrary} from '../../common/utility';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {finalize} from 'rxjs/operators';
import {Location} from '@angular/common';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-release-list',
  templateUrl: './release-list.component.html',
  styleUrls: ['./release-list.component.css']
})
export class ReleaseListComponent implements OnInit {

  title = 'Release';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfReleasePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfReleasePage = columns;
    this.updateTableColumnsForReleasePage();
  }

  updateTableColumnsForReleasePage() {
    this.preferencesService.updateTableColumnsForReleasePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfReleasePage;
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
      case 'Created on':
        this.setWidth('Created On', $event.width);
        break;

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
      this.updateTableColumnsForReleasePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }

    for (const column of this.columns) {
      switch (column.name) {
        case 'Release':
          if (column.selected) {
            displayedColumns.push('releaseNum');
          }
          break;
        case 'State':
          if (column.selected) {
            displayedColumns.push('state');
          }
          break;
        case 'Created On':
          if (column.selected) {
            displayedColumns.push('creationTimestamp');
          }
          break;
        case 'Updated On':
          if (column.selected) {
            displayedColumns.push('lastUpdateTimestamp');
          }
          break;
      }
    }
    displayedColumns.push('more');
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<ReleaseList>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  libraries: Library[] = [];
  mappedLibraries: {library: Library, selected: boolean}[] = [];
  loginIdList: string[] = [];
  creatorIdListFilterCtrl: FormControl = new FormControl();
  filteredCreatorIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['Initialized', 'Draft', 'Published'];
  request: ReleaseListRequest;
  preferencesInfo: PreferencesInfo;
  namespaces: SimpleNamespace[] = [];
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

  contextMenuItem: ReleaseList;
  @ViewChild('createdDateStart', {static: true}) createdDateStart: MatDatepicker<any>;
  @ViewChild('createdDateEnd', {static: true}) createdDateEnd: MatDatepicker<any>;
  @ViewChild('updatedDateStart', {static: true}) updatedDateStart: MatDatepicker<any>;
  @ViewChild('updatedDateEnd', {static: true}) updatedDateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private service: ReleaseService,
              private libraryService: LibraryService,
              private accountService: AccountListService,
              private namespaceService: NamespaceService,
              private snackBar: MatSnackBar,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new ReleaseListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.excludes.push(WorkingRelease.releaseNum);

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
        this.namespaceService.getSimpleNamespaces(this.request.library.libraryId),
        this.accountService.getAccountNames(),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([namespaces, loginIds, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.namespaces.push(...namespaces);
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList, this.namespaces, (e) => e.uri);

        this.loginIdList.push(...loginIds);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
        initFilter(this.creatorIdListFilterCtrl, this.filteredCreatorIdList, this.loginIdList);

        this.loadReleases(true);
      });
    });
  }

  get userToken() {
    return this.auth.getUserToken();
  }

  onPageChange(event: PageEvent) {
    this.loadReleases();
  }

  onChange(property?: string, source?) {
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'created.startDate':
        this.request.createdDate.start = new Date(event.value);
        break;
      case 'created.endDate':
        this.request.createdDate.end = new Date(event.value);
        break;
      case 'updated.startDate':
        this.request.updatedDate.start = new Date(event.value);
        break;
      case 'updated.endDate':
        this.request.updatedDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
    switch (type) {
      case 'created.startDate':
        this.createdDateStart.select(undefined);
        this.request.createdDate.start = null;
        break;
      case 'created.endDate':
        this.createdDateEnd.select(undefined);
        this.request.createdDate.end = null;
        break;
      case 'updated.startDate':
        this.updatedDateStart.select(undefined);
        this.request.updatedDate.start = null;
        break;
      case 'updated.endDate':
        this.updatedDateEnd.select(undefined);
        this.request.updatedDate.end = null;
        break;
    }
  }

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.request.library || !this.request.library.libraryId) {
        this.request.library = this.libraries[0];
      }
      if (this.request.library) {
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
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
    this.loadReleases();
  }

  loadReleases(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getReleases(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;
      this.dataSource.data = resp.list.map((elm: ReleaseList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => this.isSelectable(row)).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: ReleaseList) {
    if (this.isSelectable(row)) {
      this.selection.select(row.releaseId);
    }
  }

  isSelectable(row: ReleaseList) {
    return (row.state === 'Initialized');
  }

  toggle(row: ReleaseList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.releaseId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ReleaseList) {
    return this.selection.isSelected(row.releaseId);
  }

  create() {
    this.router.navigateByUrl('/release/create');
  }

  createDraft(release: SimpleRelease) {
    this.router.navigateByUrl('release/' + release.releaseId + '/assign');
  }

  updateState(release: SimpleRelease, state: string) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.updateState(release.releaseId, state)
            .pipe(finalize(() => {
              this.loadReleases();
              this.loading = false;
            }))
            .subscribe(_ => {
              this.snackBar.open('Updated', '', {
                duration: 3000,
              });
            });
        }
      });
  }

  discard(release?: SimpleRelease) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Release?';
    if (release) {
      dialogConfig.data.content = ['Are you sure you want to discard the release \'' + release.releaseNum + '\'?'];
    } else if (this.selection.selected.length > 0) {
      dialogConfig.data.content = ['Are you sure you want to discard selected releases?'];
    } else {
      return;
    }

    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const releaseIds = (release) ? [release.releaseId, ] : this.selection.selected;
          this.service.discard(releaseIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.loadReleases();
          });
        }
      });
  }
}
