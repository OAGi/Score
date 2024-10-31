import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
import {AccountList} from '../../account-management/domain/accounts';
import {
  TransferOwnershipDialogComponent
} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {CodeListForList, CodeListForListRequest} from '../domain/code-list';
import {CodeListService} from '../domain/code-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {WorkingRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {Release} from '../../bie-management/bie-create/domain/bie-create-list';
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

@Component({
  selector: 'score-code-list-list',
  templateUrl: './code-list-list.component.html',
  styleUrls: ['./code-list-list.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class CodeListListComponent implements OnInit {

  faLocationArrow = faLocationArrow;
  title = 'Code List';
  workingRelease = WorkingRelease;
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfCodeListPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfCodeListPage = columns;
    this.updateTableColumnsForCodeListPage();
  }

  updateTableColumnsForCodeListPage() {
    this.preferencesService.updateTableColumnsForCodeListPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfCodeListPage;
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
      this.updateTableColumnsForCodeListPage();
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
            displayedColumns.push('codeListName');
          }
          break;
        case 'Based Code List':
          if (column.selected) {
            displayedColumns.push('basedCodeListName');
          }
          break;
        case 'Agency ID':
          if (column.selected) {
            displayedColumns.push('agencyId');
          }
          break;
        case 'Version':
          if (column.selected) {
            displayedColumns.push('versionId');
          }
          break;
        case 'Extensible':
          if (column.selected) {
            displayedColumns.push('extensible');
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

  dataSource = new MatTableDataSource<CodeListForList>();
  selection = new SelectionModel<CodeListForList>(true, [],
    true, (a, b) => a.codeListManifestId === b.codeListManifestId);
  expandedElement: CodeListForList | null;
  canSelect = ['WIP', 'Deleted'];
  loading = false;

  releases: Release[] = [];
  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CodeListForListRequest;
  preferencesInfo: PreferencesInfo;
  namespaces: SimpleNamespace[] = [];
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

  contextMenuItem: CodeListForList;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private service: CodeListService,
              private releaseService: ReleaseService,
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
    this.request = new CodeListForListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

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
      this.releaseService.getSimpleReleases(['Published', 'Draft']),
      this.namespaceService.getSimpleNamespaces(),
      this.accountService.getAccountNames(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([releases, namespaces, loginIds, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.releases.push(...releases);
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      if (this.releases.length > 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), this.request.cookieType);
        if (savedReleaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.request.release) {
            this.request.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), this.request.cookieType, this.request.release.releaseId);
          }
        } else {
          this.request.release = this.releases[0];
        }
      }

      this.namespaces.push(...namespaces);
      initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList, this.namespaces, (e) => e.uri);

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.loadCodeList(true);
    });
  }

  getRelease(releaseNum: string): Release | undefined {
    for (const release of this.releases) {
      if (release.releaseNum === releaseNum) {
        return release;
      }
    }
    return undefined;
  }

  onPageChange(event: PageEvent) {
    this.loadCodeList();
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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadCodeList();
  }

  loadCodeList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getCodeListList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: CodeListForList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        if (this.request.filters.module.length > 0) {
          elm.modulePath = elm.modulePath.replace(
            new RegExp(this.request.filters.module, 'ig'),
            '<b class="bg-warning">$&</b>');
        }
        if (this.request.filters.definition.length > 0) {
          elm.definition = elm.definition.replace(
            new RegExp(this.request.filters.definition, 'ig'),
            '<b class="bg-warning">$&</b>');
        }
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
    const numRows = this.dataSource.data.filter(row =>
      row.owner === this.currentUser && this.canSelect.indexOf(row.state) > -1 && !this.hasRevision(row)).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: CodeListForList) {
    if (row.owner === this.currentUser && this.canSelect.indexOf(row.state) > -1 && !this.hasRevision(row)) {
      this.selection.select(row);
    }
  }

  toggle(row: CodeListForList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CodeListForList) {
    return this.selection.isSelected(row);
  }

  selectionClear() {
    this.selection = new SelectionModel<CodeListForList>(true, [],
      true, (a, b) => a.codeListManifestId === b.codeListManifestId);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  createCodeList() {
    this.loading = true;
    this.service.create(this.request.release.releaseId)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/code_list/' + resp.manifestId);
      }, _ => {
        this.loading = false;
      });
  }

  get showCreateCodeListBtn(): boolean {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes('developer')) {
      if (this.request.release.releaseId !== WorkingRelease.releaseId) {
        return false;
      }
    } else {
      if (this.request.release.releaseId === WorkingRelease.releaseId) {
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

  openDialogCodeListListDelete() {
    const codeListIds = this.selection.selected.map(e => e.codeListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete Code ' + (codeListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to delete selected code ' + (codeListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...codeListIds).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadCodeList();
          });
        }
      });
  }

  openDialogCodeListListRestore() {
    const codeListIds = this.selection.selected.map(e => e.codeListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore Code ' + (codeListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to Restore selected code ' + (codeListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(...codeListIds).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadCodeList();
          });
        }
      });
  }

  hasRevision(codeList: CodeListForList): boolean {
    if (!codeList) {
      return false;
    }
    return codeList.revision !== '1';
  }

  isEditable(item: CodeListForList) {
    if (!item) {
      return false;
    }
    return item.access === 'CanEdit';
  }

  openTransferDialog(item: CodeListForList, event?: MouseEvent) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.codeListManifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadCodeList();
        });
      }
    });
  }

  delete(item: CodeListForList) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete code list?';
    dialogConfig.data.content = ['Are you sure you want to delete this code list?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.loading = true;
        this.service.delete(item.codeListManifestId)
          .pipe(
            finalize(() => {
              this.loading = false;
            })
          )
          .subscribe(_ => {
            this.loadCodeList();
            this.snackBar.open('Deleted', '', {duration: 3000});
          }, error => {
          });
      });
  }

  openDetail(item: CodeListForList) {
    this.router.navigateByUrl('/code_list/' + item.codeListManifestId);
    return;
  }

  openDialogCcListRestore(item: CodeListForList) {

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore code list';
    dialogConfig.data.content = [
      'Are you sure you want to Restore code list?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(item.codeListManifestId).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.loadCodeList();
          });
        }
      });
  }

}

