import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext, BusinessContextListRequest} from '../../context-management/business-context/domain/business-context';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-bie-create',
  templateUrl: './bie-create-biz-ctx.component.html',
  styleUrls: ['./bie-create-biz-ctx.component.css']
})
export class BieCreateBizCtxComponent implements OnInit {
  title = 'Create BIE';
  subtitle = 'Select Business Contexts';

  displayedColumns: string[] = [
    'select', 'name', 'tenant', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BusinessContext>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterUsernameListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterUsernameList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessContextListRequest;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private accountService: AccountListService,
              private authService: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new BusinessContextListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadBusinessContextList();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterUsernameListFilterCtrl, this.filteredUpdaterUsernameList, this.loginIdList);
    });

    this.loadBusinessContextList(true);
  }

  onPageChange(event: PageEvent) {
    this.loadBusinessContextList();
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

  loadBusinessContextList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);
    if (this.isTenantEnabled) {
      this.request.filters.isBieEditing = true;
    }

    this.bizCtxService.getBusinessContextList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BusinessContext) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        elm.businessContextValueList = [];
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
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BusinessContext) {
    this.selection.select(row.businessContextId);
  }

  toggle(row: BusinessContext) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.businessContextId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessContext) {
    return this.selection.isSelected(row.businessContextId);
  }

  next() {
    const selectedBizCtxIds = this.selection.selected.join(',');
    this.router.navigate(['/profile_bie/create/asccp'], {queryParams: {businessContextIdList: selectedBizCtxIds}});
  }

  get isTenantEnabled(): boolean {
    const userToken = this.authService.getUserToken();
    return userToken.tenant.enabled;
  }

}
