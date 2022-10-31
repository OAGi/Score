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
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {AuthService} from '../../authentication/auth.service';
import {
  TransferOwnershipDialogComponent
} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {UserToken} from '../../authentication/domain/auth';

@Component({
  selector: 'score-bie-list',
  templateUrl: './bie-list.component.html',
  styleUrls: ['./bie-list.component.css']
})
export class BieListComponent implements OnInit {
  title = 'BIE';

  displayedColumns: string[] = [
    'select', 'state', 'den', 'owner',
    'transferOwnership', 'businessContexts', 'version',
    'status', 'bizTerm', 'remark', 'lastUpdateTimestamp', 'more'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;

  contextMenuItem: BieList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
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
      this.onChange();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases()
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      if (this.releases.length > 0) {
        if (this.request.release.releaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === this.request.release.releaseId)[0];
        } else {
          const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
          if (savedReleaseId) {
            this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
            if (!this.request.release) {
              this.request.release = this.releases[0];
              saveBranch(this.auth.getUserToken(), 'BIE', this.request.release.releaseId);
            }
          } else {
            this.request.release = this.releases[0];
          }
        }
      }
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

    this.paginator.pageIndex = 0;
    this.loadBieList();
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
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.request.updatedDate.end = null;
        break;
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
    const numRows = this.dataSource.data.filter(row => row.access === 'CanEdit').length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BieList) {
    if (row.access === 'CanEdit') {
      this.selection.select(row.topLevelAsbiepId);
    }
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList): boolean {
    if (!row) {
      return false;
    }
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  isEditable(element: BieList): boolean {
    if (!element) {
      return false;
    }
    return element.owner === this.username && element.state === 'WIP';
  }

  discardAllSelected() {
    this.openDialogBieDiscard(this.selection.selected);
  }

  discard(bieList: BieList) {
    this.openDialogBieDiscard([bieList.topLevelAsbiepId, ]);
  }

  openDialogBieDiscard(topLevelAsbiepIds: number[]) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (topLevelAsbiepIds.length > 1 ? 'BIEs' : 'BIE') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the ' + (topLevelAsbiepIds.length > 1 ? 'BIEs' : 'BIE') + '?',
      'The ' + (topLevelAsbiepIds.length > 1 ? 'BIEs' : 'BIE') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.delete(topLevelAsbiepIds).pipe(finalize(() => {
            this.loading = false;
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
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
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.loading = true;
        this.service.transferOwnership(bieList.topLevelAsbiepId, result.loginId).pipe(finalize(() => {
          this.loading = false;
        })).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.selection.clear();
          this.loadBieList();
        });
      }
    });
  }

  openBieListDialog(bie: BieList) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      topLevelAsbiepId: bie.topLevelAsbiepId,
      releaseNum: bie.releaseNum,
      den: bie.den
    };
    const dialogRef = this.dialog.open(BieListDialogComponent, dialogConfig);
  }
}
