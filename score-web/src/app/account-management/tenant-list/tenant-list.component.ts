import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Location} from '@angular/common';
import {MatTableDataSource} from '@angular/material/table';
import {AuthService} from '../../authentication/auth.service';
import {TenantList, TenantListRequest} from '../domain/tenants';
import {TenantListService} from '../domain/tenant-list.service';
import {PageRequest} from '../../basis/basis';
import {finalize} from 'rxjs/operators';
import {PreferencesInfo, TableColumnsInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-tenant-list',
  templateUrl: './tenant-list.component.html',
  styleUrls: ['./tenant-list.component.css']
})

export class TenantListComponent implements OnInit {

  title = 'Tenant Roles';
  loading = false;

  get columns() {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfTenantPage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    this.preferencesInfo.tableColumnsInfo.columnsOfTenantPage = updatedColumns;
    this.preferencesService.update(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {});
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.onColumnsChange(defaultTableColumnInfo.columnsOfTenantPage);
  }

  get displayedColumns(): string[] {
    let displayedColumns = [];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    const columns = this.preferencesInfo.tableColumnsInfo.columnsOfTenantPage;
    for (const column of columns) {
      switch (column.name) {
        case 'Tenant Name':
          if (column.selected) {
            displayedColumns.push('name');
          }
          break;
        case 'Users':
          if (column.selected) {
            displayedColumns.push('users');
          }
          break;
        case 'Business Contexts':
          if (column.selected) {
            displayedColumns.push('businessContexts');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<TenantList>();

  request: TenantListRequest;
  preferencesInfo: PreferencesInfo;

  contextMenuItem: TenantList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: TenantListService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {

  }

  ngOnInit() {
    this.request = new TenantListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('sortActive', 'asc', 0, 10));

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

      this.loadTenantList(true);
    });
  }

  onChange(property?: string, source?) {
  }

  onPageChange(event: PageEvent) {
    this.loadTenantList();
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadTenantList();
  }

  loadTenantList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getTenants(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

}
