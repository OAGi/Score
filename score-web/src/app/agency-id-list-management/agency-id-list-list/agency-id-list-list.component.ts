import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {AccountList} from '../../account-management/domain/accounts';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AgencyIdList, AgencyIdListForListRequest} from '../domain/agency-id-list';
import {AgencyIdListService} from '../domain/agency-id-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../common/utility';
import {SimpleRelease, WorkingRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-agency-id-list-list',
  templateUrl: './agency-id-list-list.component.html',
  styleUrls: ['./agency-id-list-list.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class AgencyIdListListComponent implements OnInit {

  title = 'Agency ID List';
  workingRelease = WorkingRelease;
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfAgencyIdListPage = columns;
    this.updateTableColumnsForAgencyIdListPage();
  }

  updateTableColumnsForAgencyIdListPage() {
    this.preferencesService.updateTableColumnsForAgencyIdListPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfAgencyIdListPage;
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
      this.updateTableColumnsForAgencyIdListPage();
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
        case 'State':
          if (column.selected) {
            displayedColumns.push('state');
          }
          break;
        case 'Name':
          if (column.selected) {
            displayedColumns.push('name');
          }
          break;
        case 'Version':
          if (column.selected) {
            displayedColumns.push('versionId');
          }
          break;
        case 'Revision':
          if (column.selected) {
            displayedColumns.push('revision');
          }
          break;
        case 'Owner':
          if (column.selected) {
            displayedColumns.push('owner');
          }
          break;
        case 'Module':
          if (column.selected) {
            displayedColumns.push('module');
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

  dataSource = new MatTableDataSource<AgencyIdList>();
  selection = new SelectionModel<AgencyIdList>(true, [],
    true, (a, b) => a.agencyIdListManifestId === b.agencyIdListManifestId);
  expandedElement: AgencyIdList | null;
  canSelect = ['WIP', 'Deleted'];
  loading = false;

  releases: SimpleRelease[] = [];
  libraries: Library[] = [];
  mappedLibraries: {library: Library, selected: boolean}[] = [];
  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: AgencyIdListForListRequest;
  highlightTextForModule: string;
  highlightTextForDefinition: string;
  preferencesInfo: PreferencesInfo;
  namespaces: SimpleNamespace[] = [];
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;
  contextMenuItem: AgencyIdList;

  constructor(private service: AgencyIdListService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private accountService: AccountListService,
              private namespaceService: NamespaceService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.request = new AgencyIdListForListRequest(this.route.snapshot.queryParamMap,
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

      this.releases = [];
      forkJoin([
        this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Draft', 'Published']),
        this.namespaceService.getSimpleNamespaces(this.request.library.libraryId),
        this.accountService.getAccountNames(),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([releases, namespaces, loginIds, preferencesInfo]) => {
        this.initReleases(releases);

        this.preferencesInfo = preferencesInfo;

        this.namespaces.push(...namespaces);
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList, this.namespaces, (e) => e.uri);

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.loadAgencyIdList(true);
      });
    });
  }

  getRelease(releaseNum: string): SimpleRelease | undefined {
    for (const release of this.releases) {
      if (release.releaseNum === releaseNum) {
        return release;
      }
    }
    return undefined;
  }

  onPageChange(event: PageEvent) {
    this.loadAgencyIdList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), this.request.cookieType, source.releaseId);
    }
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

  initReleases(releases: SimpleRelease[]) {
    this.releases = [...releases];
    if (this.releases.length > 0) {
      if (this.request.release.releaseId === 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), this.request.cookieType);
        if (savedReleaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.request.release) {
            this.request.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), this.request.cookieType, this.request.release.releaseId);
          }
        }
      } else {
        this.request.release = this.releases.filter(e => e.releaseId === this.request.release.releaseId)[0];
      }
    }
    if (!this.request.release || this.request.release.releaseId === 0) {
      this.request.release = this.releases[0];
    }
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Draft', 'Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadAgencyIdList();
  }

  loadAgencyIdList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getAgencyIdListList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.results.map((elm: AgencyIdList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      this.highlightTextForModule = this.request.filters.module;
      this.highlightTextForDefinition = this.request.filters.definition;

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
    const numRows = this.dataSource.data.filter(row => this.canSelect.indexOf(row.state) > -1 &&
      !this.hasRevision(row) && row.owner.username === this.currentUser).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: AgencyIdList) {
    if (this.canSelect.indexOf(row.state) > -1 && !this.hasRevision(row) && row.owner.username === this.currentUser) {
      this.selection.select(row);
    }
  }

  toggle(row: AgencyIdList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: AgencyIdList) {
    return this.selection.isSelected(row);
  }

  selectionClear() {
    this.selection = new SelectionModel<AgencyIdList>(true, [],
      true, (a, b) => a.agencyIdListManifestId === b.agencyIdListManifestId);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  createAgencyIdList() {
    this.service.create(this.request.release.releaseId)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/agency_id_list/' + resp.manifestId);
      });
  }

  get showCreateAgencyIdListBtn(): boolean {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes('developer')) {
      if (!this.request.release.workingRelease) {
        return false;
      }
      return this.dataSource.data.filter(e => e.state !== 'Deleted').length === 0;
    } else {
      if (this.request.release.workingRelease) {
        return false;
      } else {
        return this.request.release.state === 'Published';
      }
    }
    return true;
  }

  get showDiscardBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'Deleted').length === 0 :
      false;
  }

  get showRestoreBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'WIP').length === 0 :
      false;
  }

  openDialogAgencyIdListListDelete() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to delete selected agency id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadAgencyIdList();
          });
        }
      });
  }

  openDialogAgencyIdListListPurge() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Purge Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to purge selected agency id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Purge anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.purge(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Purged', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadAgencyIdList();
          });
        }
      });
  }

  openDialogAgencyIdListListRestore() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to restore selected agency Id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadAgencyIdList();
          });
        }
      });
  }

  hasRevision(item: AgencyIdList): boolean {
    if (!item) {
      return false;
    }
    return !!item.prevAgencyIdListManifestId;
  }

  isEditable(item: AgencyIdList) {
    if (!item) {
      return false;
    }
    return item.owner.username === this.currentUser && item.state === 'WIP';
  }

  canRestore(item: AgencyIdList) {
    if (!item) {
      return false;
    }
    return item.owner.username === this.currentUser && item.state === 'Deleted';
  }

  openTransferDialog(item: AgencyIdList, event?: MouseEvent) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.agencyIdListManifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadAgencyIdList();
        });
      }
    });
  }

  delete() {
    if (!this.isEditable(this.contextMenuItem)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete agency ID list?';
    dialogConfig.data.content = ['Are you sure you want to delete this agency ID list?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.loading = true;
        this.service.delete(this.contextMenuItem.agencyIdListManifestId)
          .pipe(
            finalize(() => {
              this.loading = false;
            })
          )
          .subscribe(_ => {
            this.loadAgencyIdList();
            this.snackBar.open('Deleted', '', {duration: 3000});
          }, error => {
          });
      });
  }

  openDetail($event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    this.router.navigateByUrl('/agency_id_list/' + this.contextMenuItem.agencyIdListManifestId);
    return;
  }

  openDialogCcListRestore(item: AgencyIdList) {

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore agency ID list';
    dialogConfig.data.content = [
      'Are you sure you want to Restore agency ID list?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(item.agencyIdListManifestId).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadAgencyIdList();
          });
        }
      });
  }

}

