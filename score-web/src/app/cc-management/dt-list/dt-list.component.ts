import {SelectionModel} from '@angular/cdk/collections';
import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {faFlask} from '@fortawesome/free-solid-svg-icons';
import {forkJoin, ReplaySubject} from 'rxjs';
import {CreateBdtDialogComponent} from './create-bdt-dialog/create-bdt-dialog.component';
import {CcListService} from '../cc-list/domain/cc-list.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {CcList, CcListEntry, CcListRequest} from '../cc-list/domain/cc-list';
import {PageRequest} from '../../basis/basis';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker} from '@angular/material/datepicker';
import {AuthService} from '../../authentication/auth.service';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {CcNodeService} from '../domain/core-component-node.service';
import {ActivatedRoute, Router} from '@angular/router';
import {MatTableDataSource} from '@angular/material/table';
import {FormControl} from '@angular/forms';
import {initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../common/utility';
import {ReleaseSummary, WorkingRelease} from '../../release-management/domain/release';
import {OagisComponentType, OagisComponentTypes} from '../domain/core-component-node';
import {finalize} from 'rxjs/operators';
import {Location} from '@angular/common';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AboutService} from '../../basis/about/domain/about.service';
import {TagService} from '../../tag-management/domain/tag.service';
import {Tag} from '../../tag-management/domain/tag';
import {saveAs} from 'file-saver';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {WebPageInfoService} from '../../basis/basis.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-dt-list',
  templateUrl: './dt-list.component.html',
  styleUrls: ['./dt-list.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class DtListComponent implements OnInit {

  faFlask = faFlask;
  title = 'Data Type';

  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
  componentTypeList: OagisComponentType[] = OagisComponentTypes;
  workingRelease = WorkingRelease;

  get filterTypes() {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.filterTypesOfDataTypePage;
  }

  onFilterTypesChange(updatedColumns: { name: string; selected: boolean }[]) {
    this.preferencesInfo.tableColumnsInfo.filterTypesOfDataTypePage = updatedColumns;
    this.preferencesService.updateFilterTypeForDataTypePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });

    this.request.types = updatedColumns.filter(e => e.selected).map(e => e.name);
    this.onSearch();
  }

  onFilterTypesOnlyAsccp() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    defaultTableColumnInfo.filterTypesOfDataTypePage.forEach((columnInfo: TableColumnsProperty) => {
      if ('ASCCP' === columnInfo.name) {
        columnInfo.selected = true;
      } else {
        columnInfo.selected = false;
      }
    });
    this.onFilterTypesChange(defaultTableColumnInfo.filterTypesOfDataTypePage);
  }

  onFilterTypesReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.onFilterTypesChange(defaultTableColumnInfo.filterTypesOfDataTypePage);
  }

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfDataTypePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfDataTypePage = columns;
    this.updateTableColumnsForDataTypePage();
  }

  updateTableColumnsForDataTypePage() {
    this.preferencesService.updateTableColumnsForDataTypePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfDataTypePage;
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
      this.updateTableColumnsForDataTypePage();
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
        case 'DEN':
          if (column.selected) {
            displayedColumns.push('den');
          }
          break;
        case 'Value Domain':
          if (column.selected) {
            displayedColumns.push('valueDomain');
          }
          break;
        case 'Six Hexadecimal ID':
          if (column.selected) {
            displayedColumns.push('sixDigitId');
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

  dataSource = new MatTableDataSource<CcListEntry>();
  selection = new SelectionModel<CcListEntry>(true, []);
  expandedElement: CcListEntry | null;
  loading = false;
  isElasticsearchOn = false;

  get browserMode(): boolean {
    if (!this.preferencesInfo) {
      return false;
    }

    return this.preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode;
  }

  onBrowserModeChange($event: MatSlideToggleChange) {
    this.preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode = $event.checked;
    // Issue #1650
    if (this.preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode) {
      this.onFilterTypesOnlyAsccp();
    } else {
      this.onFilterTypesReset();
    }
    this.preferencesService.updateViewSettingsInfo(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  releases: ReleaseSummary[] = [];
  libraries: LibrarySummary[] = [];
  mappedLibraries: { library: LibrarySummary, selected: boolean }[] = [];
  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;
  highlightTextForModule: string;
  highlightTextForDefinition: string;
  tags: Tag[] = [];
  preferencesInfo: PreferencesInfo;
  namespaces: NamespaceSummary[] = [];
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);

  contextMenuItem: CcList;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private service: CcListService,
              private nodeService: CcNodeService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private accountService: AccountListService,
              private namespaceService: NamespaceService,
              private aboutService: AboutService,
              private auth: AuthService,
              private tagService: TagService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              public webPageInfo: WebPageInfoService) {
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
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

  ngOnInit() {
    // Init CcList table
    this.request = new CcListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
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

      this.loading = true;
      forkJoin([
        this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Draft', 'Published']),
        this.accountService.getAccountNames(),
        this.namespaceService.getNamespaceSummaries(this.request.library.libraryId),
        this.aboutService.getProductInfo(),
        this.tagService.getTags(),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([releases, loginIds, namespaces, productInfos, tags, preferencesInfo]) => {
        for (const productInfo of productInfos) {
          if (productInfo.productName === 'Elasticsearch' && productInfo.productVersion !== '0.0.0.0') {
            this.isElasticsearchOn = true;
          }
        }
        this.request.fuzzySearch = this.isElasticsearchOn;

        this.initReleases(releases);
        this.tags = tags;
        this.preferencesInfo = preferencesInfo;
        this.request.types = this.preferencesInfo.tableColumnsInfo.filterTypesOfDataTypePage.filter(e => e.selected).map(e => e.name);

        this.namespaces.push(...namespaces);
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList, this.namespaces, (e) => e.uri);

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
        this.loadCcList(true);
      }, error => {
        this.loading = false;
      });
    });
  }

  initLibraries(libraries: LibrarySummary[]) {
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

  initReleases(releases: ReleaseSummary[]) {
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

  onLibraryChange(library: LibrarySummary) {
    this.loading = true;
    this.request.library = library;
    this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Draft', 'Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadCcList();
  }

  clear() {
    this.paginator.length = 0;
    this.paginator.pageIndex = 0;
    this.dataSource.data = [];
    this.selection.clear();
  }

  loadCcList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getCcList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      this.dataSource.data = resp.list;
      this.highlightTextForModule = this.request.filters.module;
      this.highlightTextForDefinition = this.request.filters.definition;

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
      this.selection.clear();
    }, error => {
      this.dataSource.data = [];
    });
  }

  onPageChange(event: PageEvent) {
    this.loadCcList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), this.request.cookieType, source.releaseId);
    }
    if (property === 'filters.den' && !!source) {
      this.request.page.sortActive = '';
      this.request.page.sortDirection = '';
      this.sort.active = '';
      this.sort.direction = '';
    }

    if (property === 'fuzzySearch') {
      if (this.request.fuzzySearch) {
        this.request.page.sortActive = '';
        this.request.page.sortDirection = '';
        this.sort.active = '';
        this.sort.direction = '';
      } else {
        this.request.page.sortActive = 'lastUpdateTimestamp';
        this.request.page.sortDirection = 'desc';
        this.sort.active = 'lastUpdateTimestamp';
        this.sort.direction = 'desc';
      }
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

  getRouterLink(ccList: CcListEntry) {
    switch (ccList.type.toUpperCase()) {
      case 'ACC':
        if (ccList.oagisComponentType === 'UserExtensionGroup') {
          return '/core_component/extension/' + ccList.manifestId;
        } else {
          return '/core_component/acc/' + ccList.manifestId;
        }

      case 'ASCCP':
        if (this.preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode) {
          return '/core_component/browser/asccp/' + ccList.manifestId;
        } else {
          return '/core_component/asccp/' + ccList.manifestId;
        }

      case 'BCCP':
        return '/core_component/bccp/' + ccList.manifestId;

      case 'DT':
        return '/data_type/' + ccList.manifestId;

      default:
        return window.location.pathname;
    }
  }

  isEditable(element: CcListEntry) {
    return element.owner.loginId === this.currentUser && element.state === 'WIP';
  }

  isAssociation(element: CcListEntry) {
    return element.type.toUpperCase() === 'ASCC' || element.type.toUpperCase() === 'BCC';
  }

  openTransferDialog(item: CcListEntry, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.type, item.manifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadCcList();
        });
      }
    });
  }

  openTransferDialogMultiple() {
    if (this.selection.selected.length === 0) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnershipOnList(this.selection.selected, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadCcList();
        });
      }
    });
  }

  multipleUpdate(action: string) {
    if (this.selection.selected.length === 0) {
      return;
    }
    const dialogConfig = this.confirmDialogService.newConfig();
    let notiMsg = 'Updated';
    let toState = action;
    let actionType = 'Update';

    switch (action) {
      case 'WIP':
      case 'Draft':
      case 'QA':
      case 'Candidate':
      case 'Production':
        dialogConfig.data.header = 'Update state to \'' + action + '\'?';
        dialogConfig.data.content = ['Are you sure you want to update the state to \'' + action + '\'?'];
        dialogConfig.data.action = 'Update';
        break;
      case 'Delete':
        toState = 'Deleted';
        notiMsg = 'Deleted';
        actionType = 'Delete';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      case 'Restore':
        toState = 'WIP';
        notiMsg = 'Restored';
        actionType = 'Restore';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      case 'Purge':
        toState = 'Purge';
        notiMsg = 'Purged';
        actionType = 'Purge';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      default:
        return false;
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.loading = true;
        let call;
        switch (actionType) {
          case 'Update':
            call = this.service.updateState(this.selection.selected, toState);
            break;
          case 'Delete':
            call = this.service.delete(this.selection.selected);
            break;
          case 'Restore':
            call = this.service.restore(this.selection.selected);
            break;
          case 'Purge':
            call = this.service.purge(this.selection.selected);
            break;
        }
        call.subscribe(_ => {
          this.snackBar.open(notiMsg, '', {
            duration: 3000
          });
          this.selection.clear();
          this.loadCcList();

          this.loading = false;
        }, err => {
          this.loading = false;
          throw err;
        });
      });
  }

  exportStandaloneSchemas() {
    if (this.selection.selected.length === 0) {
      return;
    }

    this.loading = true;
    this.service.exportStandaloneSchemas(this.selection.selected).subscribe(resp => {
      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));
      this.loading = false;
    }, err => {
      this.loading = false;
    });
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
  }

  hasCreatePermission(): boolean {
    const userToken = this.auth.getUserToken();
    if (this.request.release.state !== 'Published') {
      return false;
    }
    if (userToken.roles.includes('developer')) {
      if (!this.request.release.workingRelease) {
        return false;
      }
    } else {
      if (this.request.release.workingRelease) {
        return false;
      }
    }
    return true;
  }

  createDt() {
    this.loading = true;

    const dialogRef = this.dialog.open(CreateBdtDialogComponent, {
      data: {
        libraryId: this.request.library.libraryId,
        releaseId: this.request.release.releaseId,
        action: 'create',
        stateList: (this.request.release.workingRelease) ? this.workingStateList : this.releaseStateList
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(finalize(() => {
      this.loading = false;
    })).subscribe(data => {
      if (!data) {
        return;
      }

      this.loading = true;
      this.nodeService.createDt(this.request.release.releaseId, data.manifestId).subscribe(resp => {
        return this.router.navigateByUrl('/data_type/' + resp.manifestId);
      });
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(e => ['ASCC', 'BCC'].indexOf(e.type) === -1).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => {
        if (['ASCC', 'BCC'].indexOf(row.type) === -1) {
          this.select(row);
        }
      });
  }

  select(row: CcListEntry) {
    this.selection.select(row);
  }

  toggle(row: CcListEntry) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcListEntry) {
    return this.selection.isSelected(row);
  }

  openDetail(ccList: CcListEntry, $event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.router.navigateByUrl('/core_component/' + this.getRouterLink(ccList));
    return;
  }

  isInitialRevision(ccList: CcListEntry): boolean {
    return !!ccList && ccList.log.revisionNum === 1;
  }

  canToolbarAction(action: string) {
    if (this.selection.selected.length === 0) {
      return false;
    }
    switch (action) {
      case 'BackWIP':
        return this.selection.selected.filter(e => {
          if (['Draft', 'QA', 'Candidate'].indexOf(e.state) > -1 && e.owner.loginId === this.currentUser) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Draft':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser && e.release.releaseNum === this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'QA':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser && e.release.releaseNum !== this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Candidate':
        return this.selection.selected.filter(e => {
          if (e.state === 'Draft' && e.owner.loginId === this.currentUser && e.release.releaseNum === this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Production':
        return this.selection.selected.filter(e => {
          if (e.state === 'QA' && e.owner.loginId === this.currentUser && e.release.releaseNum !== this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Restore':
        return this.selection.selected.filter(e => {
          if (e.state === 'Deleted') {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Delete':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser && this.isInitialRevision(e)) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Purge':
        return this.selection.selected.filter(e => {
          if (e.state === 'Deleted') {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Export':
        return this.selection.selected.filter(e => {
          if (e.type === 'ASCCP') {
            return e;
          }
        }).length === this.selection.selected.length;
      default :
        return false;
    }
  }

  displayType(elem: CcList): string {
    return (elem.type.toUpperCase() === 'DT') ? elem.dtType : elem.type;
  }
}
