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
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';

@Component({
  selector: 'score-bie-create',
  templateUrl: './bie-copy-biz-ctx.component.html',
  styleUrls: ['./bie-copy-biz-ctx.component.css']
})
export class BieCopyBizCtxComponent implements OnInit {
  title = 'Copy BIE';
  subtitle = 'Select Business Contexts';

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      const columns = this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextPage;
      for (const column of columns) {
        switch (column.name) {
          case 'Name':
            if (column.selected) {
              displayedColumns.push('name');

              if (this.isTenantEnabled) {
                displayedColumns.push('tenant');
              }
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

  dataSource = new MatTableDataSource<BusinessContext>();
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

    if (this.isTenantEnabled) {
      this.request.filters.isBieEditing = true;
    }

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBusinessContextList();
  }

  loadBusinessContextList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

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
    this.router.navigate(['/profile_bie/copy/bie'], {queryParams: {bizCtxIds: selectedBizCtxIds}});
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

}
