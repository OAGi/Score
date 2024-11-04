import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {forkJoin, of} from 'rxjs';
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
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';

@Component({
  selector: 'score-tenant-user-detail',
  templateUrl: './tenant-user-detail.component.html',
  styleUrls: ['./tenant-user-detail.component.css']
})

export class TenantUserDetailComponent implements OnInit {

  title = 'Users Management';
  loading = false;
  tenantInfo: TenantList;
  tenantId: any;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfTenantManagementForAccountPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfTenantManagementForAccountPage = columns;
    this.updateTableColumnsForTenantManagementForAccountPage();
  }

  updateTableColumnsForTenantManagementForAccountPage() {
    this.preferencesService.updateTableColumnsForTenantManagementForAccountPage(
      this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfTenantManagementForAccountPage;
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
      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForTenantManagementForAccountPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = [];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Login ID':
          if (column.selected) {
            displayedColumns.push('loginId');
          }
          break;
        case 'Role':
          if (column.selected) {
            displayedColumns.push('role');
          }
          break;
        case 'Name':
          if (column.selected) {
            displayedColumns.push('name');
          }
          break;
        case 'Organization':
          if (column.selected) {
            displayedColumns.push('organization');
          }
          break;
        case 'Status':
          if (column.selected) {
            displayedColumns.push('status');
          }
          break;
        case 'Manage':
          if (column.selected) {
            displayedColumns.push('manage');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<AccountList>();

  addUserToTenant = false;

  request: AccountListRequest;
  preferencesInfo: PreferencesInfo;

  contextMenuItem: AccountList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private auth: AuthService,
              private service: TenantListService,
              private accountService: AccountListService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.request = new AccountListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

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

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => of(Number(params.get('id'))))
    ).subscribe(t => {
      this.loading = true;
      this.tenantId = t;
      this.request.filters.tenantId = t;

      forkJoin([
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.getTenantInfo();
      });
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.getTenantInfo();
  }

  getTenantInfo() {
    this.loading = true;

    this.service.getTenantInfo(this.tenantId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.tenantInfo = resp;
      this.loadAccounts(true);
    }, error => {
      this.dataSource.data = [];
    });
  }

  loadAccounts(isInit?: boolean) {
    this.loading = true;

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
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  onPageChange(event: PageEvent) {
    this.loadAccounts();
  }

  onChange(property?: string, source?) {
  }

  removeUser(userId: number) {
    this.loading = true;

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
    this.loading = true;

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
