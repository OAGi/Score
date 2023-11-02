import {Component, OnInit, ViewChild} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Location} from '@angular/common';
import {of, ReplaySubject} from 'rxjs';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {FormControl} from '@angular/forms';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {TenantListService} from '../domain/tenant-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {
  BusinessContext,
  BusinessContextListRequest
} from '../../context-management/business-context/domain/business-context';
import {PageRequest} from '../../basis/basis';
import {initFilter} from '../../common/utility';


import {finalize, switchMap} from 'rxjs/operators';

@Component({
  selector: 'score-tenant-biz-ctx-detail',
  templateUrl: './tenant-biz-ctx-detail.component.html',
  styleUrls: ['./tenant-biz-ctx-detail.component.css']
})
export class TenantBusinessCtxDetailComponent implements OnInit {
  title = ' - Business Context Management';
  displayedColumns: string[] = [
    'name', 'lastUpdateTimestamp', 'manage'
  ];
  dataSource = new MatTableDataSource<BusinessContext>();
  tenantId: number;
  loading: boolean;
  addBusinessCtxToTenant = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessContextListRequest;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;


  constructor(private service: TenantListService,
              private bizCtxservice: BusinessContextService,
              private accountService: AccountListService,
              private location: Location,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.request = new BusinessContextListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

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
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => of(Number(params.get('id'))))
    ).subscribe(t => {
      this.loading = true;
      this.tenantId = t;
      this.request.filters.tenantId = t;
      this.getTenantInfo(t);
    });
  }

  getTenantInfo(tenantId: number) {
    this.loading = true;

    this.service.getTenantInfo(this.tenantId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.title = resp.name + this.title;
      this.loadBusinessContextList(true);
    }, error => {
      this.dataSource.data = [];
    });
  }

  loadBusinessContextList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bizCtxservice.getBusinessContextList(this.request).pipe(
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

  onChange(property?: string, source?) {
  }

  onPageChange(event: PageEvent) {
    this.loadBusinessContextList();
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

  setAddBusinessCtxView() {
    this.addBusinessCtxToTenant = !this.addBusinessCtxToTenant;
    this.request.filters.notConnectedToTenant = true;
    this.loadBusinessContextList();
  }

  setListView() {
    this.addBusinessCtxToTenant = !this.addBusinessCtxToTenant;
    this.request.filters.notConnectedToTenant = false;
    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.loadBusinessContextList();
  }

  removeTenantBusinessCtx(businessCtxId: number) {
    this.loading = true;

    this.service.deleteTenantBusinessCtx(this.tenantId, businessCtxId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.loadBusinessContextList();
    }, error => {
      this.dataSource.data = [];
    });
  }

  addTenantBusinessCtx(businessCtxId: number) {
    this.loading = true;

    this.service.addTenantBusinessCtx(this.tenantId, businessCtxId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.loadBusinessContextList();
    }, error => {
      this.dataSource.data = [];
    });
  }
}
