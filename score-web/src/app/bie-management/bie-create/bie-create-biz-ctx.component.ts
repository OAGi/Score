import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContextListEntry, BusinessContextListRequest} from '../../context-management/business-context/domain/business-context';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDatepicker} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {AuthService} from '../../authentication/auth.service';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';

@Component({
  selector: 'score-bie-create',
  templateUrl: './bie-create-biz-ctx.component.html',
  styleUrls: ['./bie-create-biz-ctx.component.css']
})
export class BieCreateBizCtxComponent implements OnInit {
  title = 'Create BIE';
  subtitle = 'Select Business Contexts';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return (this.isTenantEnabled) ?
      this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextWithTenantPage :
      this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    if (this.isTenantEnabled) {
      this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextWithTenantPage = columns;
    } else {
      this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage = columns;
    }
    this.updateTableColumnsForBusinessContextPage();
  }

  updateTableColumnsForBusinessContextPage() {
    if (this.isTenantEnabled) {
      this.preferencesService.updateTableColumnsForBusinessContextWithTenantPage(
        this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
      });
    } else {
      this.preferencesService.updateTableColumnsForBusinessContextPage(
        this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
      });
    }
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    if (this.isTenantEnabled) {
      this.columns = defaultTableColumnInfo.columnsOfBusinessContextWithTenantPage;
    } else {
      this.columns = defaultTableColumnInfo.columnsOfBusinessContextPage;
    }
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
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBusinessContextPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'Name':
            if (column.selected) {
              displayedColumns.push('name');
            }
            break;
          case 'Tenant':
            if (column.selected) {
              displayedColumns.push('tenant');
            }
            break;
          case 'Updated On':
            if (column.selected) {
              displayedColumns.push('lastUpdateTimestamp');
            }
            break;
        }
      }
    }

    return displayedColumns;
  }

  dataSource = new MatTableDataSource<BusinessContextListEntry>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterUsernameListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterUsernameList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessContextListRequest;
  preferencesInfo: PreferencesInfo;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private bizCtxService: BusinessContextService,
              private accountService: AccountListService,
              private auth: AuthService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new BusinessContextListRequest(this.route.snapshot.queryParamMap,
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

    forkJoin([
      this.accountService.getAccountNames(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([loginIds, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.loginIdList.push(...loginIds);
      initFilter(this.updaterUsernameListFilterCtrl, this.filteredUpdaterUsernameList, this.loginIdList);

      this.loadBusinessContextList(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBusinessContextList();
  }

  onChange(property?: string, source?) {
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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBusinessContextList();
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
      this.dataSource.data = resp.list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
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

  select(row: BusinessContextListEntry) {
    this.selection.select(row.businessContextId);
  }

  toggle(row: BusinessContextListEntry) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.businessContextId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessContextListEntry) {
    return this.selection.isSelected(row.businessContextId);
  }

  next() {
    const selectedBizCtxIds = this.selection.selected.join(',');
    this.router.navigate(['/profile_bie/create/asccp'], {queryParams: {businessContextIdList: selectedBizCtxIds}});
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

}
