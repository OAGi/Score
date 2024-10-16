import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {AccountList, AccountListRequest} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {AuthService} from '../../authentication/auth.service';
import {PageRequest} from '../../basis/basis';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {PreferencesInfo, TableColumnsInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';
import {initFilter} from '../../common/utility';

@Component({
  selector: 'score-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {

  title = 'Accounts';

  get columns() {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfAccountPage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    this.preferencesInfo.tableColumnsInfo.columnsOfAccountPage = updatedColumns;
    this.preferencesService.update(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {});
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.onColumnsChange(defaultTableColumnInfo.columnsOfAccountPage);
  }

  get displayedColumns(): string[] {
    let displayedColumns = [];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    const columns = this.preferencesInfo.tableColumnsInfo.columnsOfAccountPage;
    for (const column of columns) {
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
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<AccountList>();
  loading = false;

  request: AccountListRequest;
  preferencesInfo: PreferencesInfo;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: AccountListService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new AccountListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.onSearch();
    });

    forkJoin([
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.loadAccounts(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadAccounts();
  }

  onChange(property?: string, source?) {
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadAccounts();
  }

  loadAccounts(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getAccountsList(this.request).pipe(
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

}
