import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {of} from 'rxjs';
import {AuthService} from '../../authentication/auth.service';
import {Location} from '@angular/common';
import {AccountList, AccountListRequest} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {TenantListService} from '../domain/tenant-list.service';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {PageRequest} from '../../basis/basis';
import {TenantList} from '../domain/tenants';
import {finalize, switchMap} from 'rxjs/operators';

@Component({
  selector: 'score-tenant-user-detail',
  templateUrl: './tenant-user-detail.component.html',
  styleUrls: ['./tenant-user-detail.component.css']
})

export class TenantUserDetailComponent implements OnInit {

  title = ' - Users Management';
  loading = false;
  tenantInfo: TenantList;
  tenantId: any;
  displayedColumns: string[] = [
    'loginId', 'role', 'name', 'organization', 'status', 'manage'
  ];
  dataSource = new MatTableDataSource<AccountList>();

  addUserToTenant = false;

  request: AccountListRequest;

  contextMenuItem: AccountList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: TenantListService,
              private accountService: AccountListService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.request = new AccountListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });
    this.loading = true;

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
    this.service.getTenantInfo(this.tenantId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.title = resp.name + this.title;
      this.loadAccounts(true);
    }, error => {
      this.dataSource.data = [];
    });
  }

  loadAccounts(isInit?: boolean) {
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.accountService.getAccountsList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;
      this.dataSource.data = resp.list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  onPageChange(event: PageEvent) {
    this.loadAccounts();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadAccounts();
  }

  removeUser(userId: number) {
    this.service.deleteTenantUser(this.tenantId, userId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.loadAccounts();
    }, error => {
      this.dataSource.data = [];
    });
  }

  setAddUserView() {
    this.addUserToTenant = !this.addUserToTenant;
    this.request.filters.notConnectedToTenant = true;
    this.loadAccounts();
  }

  setListView() {
    this.addUserToTenant = !this.addUserToTenant;
    this.request.filters.notConnectedToTenant = false;
    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.loadAccounts();
  }

  addUser(userId: number) {
    this.service.addTenantUser(this.tenantId, userId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.loadAccounts();
    }, error => {
      this.dataSource.data = [];
    });
  }
}
