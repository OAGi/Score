import {Component, OnInit, ViewChild} from '@angular/core';
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
import {initFilter} from '../../../common/utility';
import {BiePackage, BiePackageListRequest} from '../domain/bie-package';
import {BiePackageService} from '../domain/bie-package.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {SimpleRelease, WorkingRelease} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {UserToken} from '../../../authentication/domain/auth';
import {TransferOwnershipDialogComponent} from '../../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../../account-management/domain/accounts';
import {BieList} from '../../bie-list/domain/bie-list';
import {MailService} from '../../../common/score-mail.service';

@Component({
  selector: 'score-bie-package-list',
  templateUrl: './bie-package-list.component.html',
  styleUrls: ['./bie-package-list.component.css']
})
export class BiePackageListComponent implements OnInit {

  title = 'BIE Package';
  displayedColumns = [
    {id: 'select', name: ''},
    {id: 'state', name: 'State'},
    {id: 'branch', name: 'Branch'},
    {id: 'versionName', name: 'Package Version Name'},
    {id: 'versionId', name: 'Package Version ID'},
    {id: 'owner', name: 'Owner'},
    {id: 'transferOwnership', name: 'Transfer Ownership'},
    {id: 'description', name: 'Description'},
    {id: 'lastUpdateTimestamp', name: 'Last Update Timestamp'},
    {id: 'more', name: 'More'},
  ];
  table: TableData<BiePackage>;
  selection = new SelectionModel<number>(true, []);
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
  request: BiePackageListRequest;

  contextMenuItem: BiePackage;
  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private biePackageService: BiePackageService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private mailService: MailService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit(): void {
    const localStorageKey = 'X-Score-Table[BiePackageList]';
    const value = JSON.parse(localStorage.getItem(localStorageKey)!);
    this.table = new TableData<BiePackage>((value) ? value._columns : this.displayedColumns, {localStorageKey: localStorageKey});
    this.table.dataSource = new MatMultiSortTableDataSource<BiePackage>(this.sort, false);

    this.request = new BiePackageListRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    this.table.sortObservable.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadBiePackageList();
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
      this.loadBiePackageList(true);
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

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    if (selectAllValue) {
      this.request.releases = this.releases;
    } else {
      this.request.releases = [];
    }
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
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
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

  }
}
