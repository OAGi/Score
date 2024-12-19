import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Location} from '@angular/common';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {finalize} from 'rxjs/operators';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {PageRequest} from '../../../basis/basis';
import {initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../../common/utility';
import {BiePackage, BiePackageListRequest} from '../domain/bie-package';
import {BiePackageService} from '../domain/bie-package.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {SimpleRelease} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {UserToken} from '../../../authentication/domain/auth';
import {TransferOwnershipDialogComponent} from '../../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../../account-management/domain/accounts';
import {MailService} from '../../../common/score-mail.service';
import {BiePackageUpliftDialogComponent} from '../bie-package-uplift-dialog/bie-package-uplift-dialog.component';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../../common/search-bar/search-bar.component';
import {LibraryService} from '../../../library-management/domain/library.service';
import {Library} from '../../../library-management/domain/library';

@Component({
  selector: 'score-bie-package-list',
  templateUrl: './bie-package-list.component.html',
  styleUrls: ['./bie-package-list.component.css']
})
export class BiePackageListComponent implements OnInit {

  title = 'BIE Package';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage = columns;
    this.updateTableColumnsForBiePackagePage();
  }

  updateTableColumnsForBiePackagePage() {
    this.preferencesService.updateTableColumnsForBiePackagePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBiePackagePage;
    this.onColumnsChange(this.columns);
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;

    let columns = [];
    for (const tableColumn of this.table.columns) {
      for (const updatedColumn of updatedColumns) {
        if (tableColumn.name === updatedColumn.name) {
          tableColumn.isActive = updatedColumn.selected;
        }
      }
      columns.push(tableColumn);
    }

    this.table.columns = columns;
    this.table.displayedColumns = this.displayedColumns;
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
      this.updateTableColumnsForBiePackagePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  defaultDisplayedColumns = [
    {id: 'select', name: '', isActive: true},
    {id: 'state', name: 'State', isActive: true},
    {id: 'branch', name: 'Branch', isActive: true},
    {id: 'versionName', name: 'Package Version Name', isActive: true},
    {id: 'versionId', name: 'Package Version ID', isActive: true},
    {id: 'owner', name: 'Owner', isActive: true},
    {id: 'description', name: 'Description', isActive: true},
    {id: 'lastUpdateTimestamp', name: 'Last Update Timestamp', isActive: true},
    {id: 'more', name: 'More', isActive: true},
  ];

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
          case 'Package Version Name':
            if (column.selected) {
              displayedColumns.push('versionName');
            }
            break;
          case 'Package Version ID':
            if (column.selected) {
              displayedColumns.push('versionId');
            }
            break;
          case 'Owner':
            if (column.selected) {
              displayedColumns.push('owner');
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
    }
    return displayedColumns;
  }

  table: TableData<BiePackage>;
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  libraries: Library[] = [];
  mappedLibraries: { library: Library, selected: boolean }[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BiePackageListRequest;
  highlightText: string;
  preferencesInfo: PreferencesInfo;

  contextMenuItem: BiePackage;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private biePackageService: BiePackageService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private mailService: MailService,
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

  ngOnInit(): void {
    this.table = new TableData<BiePackage>(this.defaultDisplayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<BiePackage>(this.sort, false);

    this.request = new BiePackageListRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      this.searchBar.showAdvancedSearch =
        (this.route.snapshot.queryParamMap && this.route.snapshot.queryParamMap.get('adv_ser') === 'true');

      this.paginator.pageIndex = this.request.page.pageIndex;
      this.paginator.pageSize = this.request.page.pageSize;
      this.paginator.length = 0;

      this.table.sortParams = this.request.page.sortActives;
      this.table.sortDirs = this.request.page.sortDirections;
      // Prevent the sorting event from being triggered if any columns are currently resizing.
      const originalSort = this.sort.sort;
      this.sort.sort = (sortChange) => {
        if (this.tableColumnResizeDirectives &&
          this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
          return;
        }
        originalSort.apply(this.sort, [sortChange]);
      };
      this.table.sortObservable.subscribe(() => {
        this.onSearch();
      });

      forkJoin([
        this.accountService.getAccountNames(),
        this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Published',]),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([loginIds, releases, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;
        this.onColumnsChange(this.preferencesInfo.tableColumnsInfo.columnsOfBiePackagePage);

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.initReleases(releases);

        this.loadBiePackageList(true);
      });
    });
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  onPageChange(event: PageEvent) {
    this.loadBiePackageList();
  }

  onChange(property?: string, source?) {
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

  releaseNums(elm: BiePackage): string[] {
    return (!!elm.releases && elm.releases.length > 0) ? elm.releases.map(e => e.releaseNum) : [];
  }

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    if (selectAllValue) {
      this.request.releases = this.releases;
    } else {
      this.request.releases = [];
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

  initReleases(releases: SimpleRelease[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
    this.request.releases = this.request.releases.map(e => this.releases.find(r => e.releaseId === r.releaseId));
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBiePackageList();
  }

  loadBiePackageList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.biePackageService.getBiePackageList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list.map((elm: BiePackage) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        if (!!elm.sourceTimestamp) {
          elm.sourceTimestamp = new Date(elm.sourceTimestamp);
        }
        return elm;
      });
      this.highlightText = this.request.filters.description;

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.table.dataSource.data = [];
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.table.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.table.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BiePackage) {
    this.selection.select(row.biePackageId);
  }

  toggle(row: BiePackage) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.biePackageId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BiePackage) {
    return this.selection.isSelected(row.biePackageId);
  }

  isEditable(element: BiePackage): boolean {
    if (!element) {
      return false;
    }
    return element.access === 'CanEdit';
  }

  create() {
    this.biePackageService.create().subscribe(biePackageId => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });

      this.router.navigateByUrl('/bie_package/' + biePackageId);
    });
  }

  discard(biePackage?: BiePackage) {
    const biePackageIds = (!!biePackage) ? [biePackage.biePackageId,] : this.selection.selected;
    this.openDialogBiePackageDiscard(biePackageIds);
  }

  openDialogBiePackageDiscard(biePackageIds: number[]) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard BIE Package' + (biePackageIds.length > 1 ? 's' : '') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected BIE package' + (biePackageIds.length > 1 ? 's' : '') + '?',
      'The BIE package' + (biePackageIds.length > 1 ? 's' : '') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.biePackageService.delete(...biePackageIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadBiePackageList();
          });
        }
      });
  }

  openTransferDialog(biePackage: BiePackage, $event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!this.isEditable(biePackage) && !this.isAdmin) {
      return;
    }

    this.accountService.getAccount(biePackage.owner.userId).subscribe(resp => {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {roles: [resp.developer ? 'developer' : 'end-user']};
      const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

      dialogRef.afterClosed().subscribe((result: AccountList) => {
        if (result) {
          this.loading = true;
          this.biePackageService.transferOwnership(biePackage.biePackageId, result.loginId).subscribe(_ => {
            this.snackBar.open('Transferred', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadBiePackageList();
            this.loading = false;
          }, error => {
            this.loading = false;
            throw error;
          });
        }
      });
    });
  }

  requestOwnershipTransfer(biePackage: BiePackage) {
    this.loading = true;
    this.mailService.sendMail('bie-package-ownership-transfer-request', biePackage.owner.userId, {
      parameters: {
        bie_package_link: window.location.href + '/' + biePackage.biePackageId,
        bie_package_version_name: biePackage.versionName,
        bie_package_version_id: biePackage.versionId,
        biePackageId: biePackage.biePackageId,
        targetLoginId: this.auth.getUserToken().username
      }
    }).subscribe(resp => {
      this.loading = false;
      this.snackBar.open('Request Sent', '', {
        duration: 3000,
      });
    }, error => {
      this.loading = false;
      throw error;
    });
  }

  copy(biePackage: BiePackage) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Copy BIE Package?';
    dialogConfig.data.content = [
      'Are you sure you want to copy this BIE package?'
    ];
    dialogConfig.data.action = 'Copy';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.biePackageService.copy(biePackage.biePackageId).subscribe(_ => {
            this.snackBar.open('Copied', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadBiePackageList();
            this.loading = false;
          }, error => {
            this.loading = false;
            throw error;
          });
        }
      });
  }

  uplift(biePackage: BiePackage) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {biePackageId: biePackage.biePackageId};
    const dialogRef = this.dialog.open(BiePackageUpliftDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((targetReleaseId: number) => {
      if (targetReleaseId) {
        this.loading = true;

        this.biePackageService.createUpliftBiePackage(biePackage.biePackageId, targetReleaseId).subscribe(_ => {
          this.snackBar.open('The uplift process has begun. This process may take a few minutes.', '', {
            duration: 3000,
          });

          this.selection.clear();
          this.loadBiePackageList();
          this.loading = false;
        }, error => {
          this.loading = false;
          throw error;
        });
      }
    });
  }
}
