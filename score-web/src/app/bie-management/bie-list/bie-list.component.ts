import {Component, OnInit, ViewChild} from '@angular/core';
import {SimpleRelease} from '../../release-management/domain/release';
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
import {BieList, BieListRequest} from './domain/bie-list';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {AuthService} from '../../authentication/auth.service';
import {
  TransferOwnershipDialogComponent
} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, saveBranch} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {UserToken} from '../../authentication/domain/auth';
import {WebPageInfoService} from '../../basis/basis.service';
import {BieDeprecateDialogComponent} from "../bie-deprecate-dialog/bie-deprecate-dialog.component";
import {BieEditService} from "../bie-edit/domain/bie-edit.service";

@Component({
  selector: 'score-bie-list',
  templateUrl: './bie-list.component.html',
  styleUrls: ['./bie-list.component.css']
})
export class BieListComponent implements OnInit {

  title = 'BIE';

  displayedColumns: string[] = [
    'select', 'state', 'branch', 'den', 'owner',
    'transferOwnership', 'businessContexts', 'version',
    'status', 'bizTerm', 'remark', 'lastUpdateTimestamp', 'more'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<BieList>(true, [],
    true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;

  contextMenuItem: BieList;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BieListService,
              private bieEditService: BieEditService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadBieList();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases()
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      this.request.releases = this.request.releases.map(e => this.releases.find(r => e.releaseId === r.releaseId));
      this.loadBieList(true);
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

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'filters.den') {
      this.sort.active = '';
      this.sort.direction = '';
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

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    if (selectAllValue) {
      this.request.releases = this.releases;
    } else {
      this.request.releases = [];
    }
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
      this.dataSource.data = resp.list.map((elm: BieList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => row.owner === this.username).length;
    return numSelected > 0 && numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BieList) {
    if (row.owner === this.username) {
      this.selection.select(row);
    }
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList): boolean {
    if (!row) {
      return false;
    }
    return this.selection.isSelected(row);
  }

  selectionClear() {
    this.selection = new SelectionModel<BieList>(true, [],
      true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);
  }

  isEditable(element: BieList): boolean {
    if (!element) {
      return false;
    }
    return element.owner === this.username && element.state === 'WIP';
  }

  isDeprecable(element: BieList): boolean {
    if (!element) {
      return false;
    }

    return (element.owner === this.username || this.auth.isAdmin()) && element.state === 'Production';
  }

  discardAllSelected() {
    this.openDialogBieDiscard(this.selection.selected);
  }

  discard(bieList: BieList) {
    this.openDialogBieDiscard([bieList,]);
  }

  openDialogBieDiscard(bieLists: BieList[]) {
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

  openTransferDialog(bieList: BieList) {
    if (!this.isEditable(bieList)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    if (this.auth.getUserToken().tenant.enabled) {
      dialogConfig.data = {businesCtxIds: bieList.businessContexts.map(b => b.businessContextId)};
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
  }

  openFindReuseBieListDialog(bie: BieList) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      topLevelAsbiepId: bie.topLevelAsbiepId,
      releaseNum: bie.releaseNum,
      den: bie.den
    };
    const dialogRef = this.dialog.open(BieListDialogComponent, dialogConfig);
  }

  canToolbarAction(action: string) {
    if (this.selection.selected.length === 0) {
      return false;
    }
    switch (action) {
      case 'BackWIP':
        return this.selection.selected.filter(e => {
          if (['QA'].indexOf(e.state) > -1 && e.owner === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'QA':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Production':
        return this.selection.selected.filter(e => {
          if (e.state === 'QA' && e.owner === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.username) {
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
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
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

  deprecate(bie: BieList) {
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

}
