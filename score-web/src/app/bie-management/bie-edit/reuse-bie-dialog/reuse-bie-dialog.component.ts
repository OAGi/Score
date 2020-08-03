import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {AuthService} from '../../../authentication/auth.service';
import {BieList, BieListRequest} from '../../bie-list/domain/bie-list';
import {SelectionModel} from '@angular/cdk/collections';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../basis/basis';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {BieEditAsbiepNode} from '../domain/bie-edit-node';

@Component({
  selector: 'score-reuse-bie-dialog',
  templateUrl: './reuse-bie-dialog.component.html',
  styleUrls: ['./reuse-bie-dialog.component.css']
})
export class ReuseBieDialogComponent implements OnInit {

  displayedColumns: string[] = [
    'select', 'state', 'propertyTerm', 'owner', 'businessContexts',
    'version', 'status', 'bizTerm', 'remark', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<number>(false, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BieListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(
    public dialogRef: MatDialogRef<ReuseBieDialogComponent>,
    private bieListService: BieListService,
    private accountService: AccountListService,
    private authService: AuthService,
    private location: Location,
    private router: Router,
    private route: ActivatedRoute,
    @Inject(MAT_DIALOG_DATA) public asbiepNode: BieEditAsbiepNode) {
  }

  ngOnInit() {
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.filters.asccpId = this.asbiepNode.asccpId;
    this.request.filters.releaseId = this.asbiepNode.releaseId;
    this.request.excludeTopLevelAsbiepIds = [this.asbiepNode.topLevelAsbiepId,];
    this.request.states = ['Published'];

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });
    const userToken = this.authService.getUserToken()
    if (userToken) {
      this.request.ownedByDeveloper = userToken.role === 'developer';
    }

    this.loadBieList();
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange() {
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

  loadBieList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bieListService.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
    }, error => {
      this.dataSource.data = [];
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  isDisabled() {
    return this.selection.selected.length === 0;
  }

  select(row: BieList) {
    this.selection.select(row.topLevelAsbiepId);
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

}
