import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {finalize} from 'rxjs/operators';
import {SimpleRelease} from '../../../release-management/domain/release';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {initFilter, loadBranch, saveBranch} from '../../../common/utility';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {BieList, BieListRequest} from '../../bie-list/domain/bie-list';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {UserToken} from '../../../authentication/domain/auth';

@Component({
  selector: 'score-bie-package-add-bie-dialog',
  templateUrl: './bie-package-add-bie-dialog.component.html',
  styleUrls: ['./bie-package-add-bie-dialog.component.css']
})
export class BiePackageAddBieDialogComponent implements OnInit {

  title = 'Add BIE';
  subtitle = 'Selected Top-Level ABIEs';

  displayedColumns = [
    {id: 'select', name: ''},
    {id: 'state', name: 'State'},
    {id: 'branch', name: 'Branch'},
    {id: 'den', name: 'DEN'},
    {id: 'owner', name: 'Owner'},
    {id: 'businessContexts', name: 'Business Contexts'},
    {id: 'version', name: 'Version'},
    {id: 'status', name: 'Status'},
    {id: 'bizTerm', name: 'Business Term'},
    {id: 'remark', name: 'Remark'},
    {id: 'lastUpdateTimestamp', name: 'Updated on'},
  ];

  table: TableData<BieList>;
  selection = new SelectionModel<BieList>(true, []);
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  selectedRelease: SimpleRelease;
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['Production'];
  request: BieListRequest;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              public webPageInfo: WebPageInfoService,
              public dialogRef: MatDialogRef<BiePackageAddBieDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit(): void {
    this.table = new TableData<BieList>(this.displayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<BieList>(this.sort, false);

    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));
    this.request.states = ['Production'];

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    this.table.sortObservable.subscribe(() => {
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
      if (this.releases.length > 1) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
        if (savedReleaseId) {
          this.selectedRelease = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.selectedRelease) {
            this.selectedRelease = this.releases[0];
            saveBranch(this.auth.getUserToken(), 'BIE', this.selectedRelease.releaseId);
          }
        } else {
          this.selectedRelease = this.releases[0];
        }
      } else {
        this.selectedRelease = this.releases[0];
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
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);
    this.request.releases = (!!this.selectedRelease) ? [this.selectedRelease, ] : [];

    this.service.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list.map((elm: BieList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
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
    const numRows = this.table.dataSource.data.filter(row => row.owner === this.username).length;
    return numSelected > 0 && numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.table.dataSource.data.forEach(row => this.select(row));
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

  add() {
    const selectedBieList = this.selection.selected;
    this.dialogRef.close(selectedBieList.map(e => e.topLevelAsbiepId));
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
