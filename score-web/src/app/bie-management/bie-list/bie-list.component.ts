import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {BieListDialogComponent} from '../bie-list-dialog/bie-list-dialog.component';
import {BieListService} from './domain/bie-list.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, Router} from '@angular/router';
import {BieListEntry, BieListRequest} from './domain/bie-list';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {AuthService} from '../../authentication/auth.service';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadLibrary, saveBranch, saveLibrary} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {UserToken} from '../../authentication/domain/auth';
import {WebPageInfoService} from '../../basis/basis.service';
import {BieDeprecateDialogComponent} from '../bie-deprecate-dialog/bie-deprecate-dialog.component';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';
import {MailService} from '../../common/score-mail.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {BieCreateService} from '../bie-create/domain/bie-create.service';
import {MultiActionsSnackBarComponent} from '../../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';
import {BieDiagramDialogComponent} from '../bie-diagram-dialog/bie-diagram-dialog.component';

@Component({
  selector: 'score-bie-list',
  templateUrl: './bie-list.component.html',
  styleUrls: ['./bie-list.component.css']
})
export class BieListComponent implements OnInit {

  title = 'BIE';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBiePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBiePage = columns;
    this.updateTableColumnsForBiePage();
  }

  updateTableColumnsForBiePage() {
    this.preferencesService.updateTableColumnsForBiePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBiePage;
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
      this.updateTableColumnsForBiePage();
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
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'State':
            if (column.selected) {
              displayedColumns.push('state');
            }
            break;
          case 'Branch':
            if (column.selected) {
              displayedColumns.push('branch');
            }
            break;
          case 'DEN':
            if (column.selected) {
              displayedColumns.push('den');
            }
            break;
          case 'Owner':
            if (column.selected) {
              displayedColumns.push('owner');
            }
            break;
          case 'Business Contexts':
            if (column.selected) {
              displayedColumns.push('businessContexts');
            }
            break;
          case 'Version':
            if (column.selected) {
              displayedColumns.push('version');
            }
            break;
          case 'Status':
            if (column.selected) {
              displayedColumns.push('status');
            }
            break;
          case 'Business Term':
            if (column.selected) {
              displayedColumns.push('bizTerm');
            }
            break;
          case 'Remark':
            if (column.selected) {
              displayedColumns.push('remark');
            }
            break;
          case 'Updated On':
            if (column.selected) {
              displayedColumns.push('lastUpdateTimestamp');
            }
            break;
        }
      }
    }
    displayedColumns.push('more');
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<BieListEntry>();
  selection = new SelectionModel<BieListEntry>(true, [],
    true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);
  loading = false;

  loginIdList: string[] = [];
  releases: ReleaseSummary[] = [];
  libraries: LibrarySummary[] = [];
  mappedLibraries: {library: LibrarySummary, selected: boolean}[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;
  preferencesInfo: PreferencesInfo;

  contextMenuItem: BieListEntry;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private service: BieListService,
              private bieCreateService: BieCreateService,
              private bieEditService: BieEditService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private mailService: MailService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
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

      forkJoin([
        this.accountService.getAccountNames(),
        this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Published']),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([loginIds, releases, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.initReleases(releases);

        this.loadBieList(true);
      });
    });
  }

  get username(): string {
    const userToken = this.userToken;
    return (userToken) ? userToken.username : undefined;
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : [];
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'filters.den' && !!source) {
      this.request.page.sortActive = '';
      this.request.page.sortDirection = '';
      this.sort.active = '';
      this.sort.direction = '';
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

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    if (selectAllValue) {
      this.request.releases = this.releases;
    } else {
      this.request.releases = [];
    }
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
    this.releases = releases.filter(e => !e.workingRelease);
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
    this.request.releases = this.request.releases.map(e => this.releases.find(r => e.releaseId === r.releaseId));
  }

  onLibraryChange(library: LibrarySummary) {
    this.request.library = library;
    this.request.releases = [];
    this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBieList();
  }

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
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
    const numRows = this.dataSource.data.filter(row => (this.isAdmin) || row.owner.loginId === this.username).length;
    return numSelected > 0 && numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BieListEntry) {
    if ((this.isAdmin) || row.owner.loginId === this.username) {
      this.selection.select(row);
    }
  }

  toggle(row: BieListEntry) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieListEntry): boolean {
    if (!row) {
      return false;
    }
    return this.selection.isSelected(row);
  }

  selectionClear() {
    this.selection = new SelectionModel<BieListEntry>(true, [],
      true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);
  }

  isEditable(element: BieListEntry): boolean {
    if (!element) {
      return false;
    }
    return element.owner.loginId === this.username && element.state === 'WIP';
  }

  isDeprecatable(element: BieListEntry): boolean {
    if (!element) {
      return false;
    }
    return element.owner.loginId === this.username && element.state === 'Production';
  }

  isInheritable(element: BieListEntry): boolean {
    if (!element) {
      return false;
    }
    return (this.auth.isDeveloper() && element.ownedByDeveloper ||
            this.auth.isEndUser() && !element.ownedByDeveloper);
  }

  discardAllSelected() {
    this.openDialogBieDiscard(this.selection.selected);
  }

  discard(bieList: BieListEntry) {
    this.openDialogBieDiscard([bieList,]);
  }

  openDialogBieDiscard(bieLists: BieListEntry[]) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (bieLists.length > 1 ? 'BIEs' : 'BIE') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the ' + (bieLists.length > 1 ? 'BIEs' : 'BIE') + '?',
      'The ' + (bieLists.length > 1 ? 'BIEs' : 'BIE') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.delete(bieLists.map(e => e.topLevelAsbiepId)).pipe(finalize(() => {
            this.loading = false;
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadBieList();
          });
        }
      });
  }

  openTransferDialog(bieList: BieListEntry, $event?: Event) {
    if ($event) {
      $event.stopPropagation();
      $event.preventDefault();
    }

    if (!this.isEditable(bieList) && !this.isAdmin) {
      return;
    }

    this.accountService.getAccount(bieList.owner.userId).subscribe(resp => {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {roles: [resp.developer ? 'developer' : 'end-user']};
      if (this.userToken.tenant.enabled) {
        dialogConfig.data = {businessCtxIds: bieList.businessContextList.map(b => b.businessContextId)};
      }
      const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

      dialogRef.afterClosed().subscribe((result: AccountList) => {
        if (result) {
          this.loading = true;
          this.service.transferOwnership(bieList.topLevelAsbiepId, result.loginId).subscribe(_ => {
            this.snackBar.open('Transferred', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadBieList();
            this.loading = false;
          }, error => {
            this.loading = false;
          });
        }
      });
    });
  }

  requestOwnershipTransfer(bie: BieListEntry) {
    this.loading = true;
    this.mailService.sendMail('bie-ownership-transfer-request', bie.owner.userId, {
      parameters: {
        bie_link: window.location.href + '/' + bie.topLevelAsbiepId,
        bie_name: bie.den,
        topLevelAsbiepId: bie.topLevelAsbiepId,
        targetLoginId: this.auth.getUserToken().username
      }
    }).subscribe(resp => {
      this.loading = false;
      this.snackBar.open('Request Sent', '', {
        duration: 3000,
      });
    }, error => {
      this.loading = false;
    });
  }

  openFindReuseBieListDialog(bie: BieListEntry) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      topLevelAsbiepId: bie.topLevelAsbiepId,
      releaseNum: bie.release.releaseNum,
      den: bie.den
    };
    const dialogRef = this.dialog.open(BieListDialogComponent, dialogConfig);
  }

  createInheritedBie(bie: BieListEntry) {
    if (!bie) {
      return;
    }

    this.loading = true;
    this.bieCreateService.createInheritedBie(bie.topLevelAsbiepId)
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe(_ => {
        this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
          data: {
            titleIcon: 'info',
            title: 'Request Received',
            message: 'This may take a moment, so please check back shortly.',
            action: 'Search',
            onAction: (data, snackBarRef) => {
              this.onSearch();
              snackBarRef.dismissWithAction();
            }
          }
        });
      }, error => {
        this.loading = false;
      });
  }

  canToolbarAction(action: string) {
    if (this.selection.selected.length === 0) {
      return false;
    }
    switch (action) {
      case 'BackWIP':
        return this.selection.selected.filter(e => {
          if (['QA'].indexOf(e.state) > -1 && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'QA':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Production':
        return this.selection.selected.filter(e => {
          if (e.state === 'QA' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      default :
        return false;
    }
  }

  multipleUpdate(action: string) {
    if (this.selection.selected.length === 0) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    const notiMsg = 'Updated';
    const toState = action;
    const actionType = 'Update';

    switch (action) {
      case 'WIP':
      case 'QA':
      case 'Production':
        dialogConfig.data.header = 'Update state to \'' + action + '\'?';
        dialogConfig.data.content = ['Are you sure you want to update the state to \'' + action + '\'?'];
        dialogConfig.data.action = 'Update';
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
        this.service.updateStateOnList(actionType, toState, this.selection.selected)
          .pipe(
            finalize(() => {
              this.loading = false;
            })
          )
          .subscribe(_ => {
            this.loadBieList();
            this.snackBar.open(notiMsg, '', {
              duration: 3000
            });
            this.selectionClear();
          }, error => {
          });
      });
  }

  openTransferDialogMultiple() {
    if (this.selection.selected.length === 0) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.userToken.roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.loading = true;
        this.service.transferOwnershipOnList(this.selection.selected, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadBieList();
          this.selectionClear();
          this.loading = false;
        }, error => {
          this.loading = false;
        });
      }
    });
  }

  deprecate(bie: BieListEntry) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      deprecated: bie.deprecated,
      reason: bie.deprecatedReason,
      remark: bie.deprecatedRemark
    };
    const dialogRef = this.dialog.open(BieDeprecateDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.loading = true;
        this.bieEditService.deprecate(bie.topLevelAsbiepId, result['reason'], result['remark']).subscribe(_ => {
          this.snackBar.open('Deprecated', '', {
            duration: 3000,
          });
          this.loadBieList();
          this.selectionClear();
          this.loading = false;
        }, error => {
          this.loading = false;
        });
      }
    });
  }

  showUmlDiagram(bieListEntry: BieListEntry, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.height = '80vh';
    dialogConfig.data = {topLevelAsbiepId: bieListEntry.topLevelAsbiepId};
    const dialogRef = this.dialog.open(BieDiagramDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(_ => {
    });
  }

}
