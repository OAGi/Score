import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {PendingAccount, PendingListRequest} from '../domain/pending-list';
import {AuthService} from '../../authentication/auth.service';
import {PageRequest} from '../../basis/basis';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {PendingListService} from '../domain/pending-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PreferencesInfo, TableColumnsInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-pending-list',
  templateUrl: './pending-list.component.html',
  styleUrls: ['./pending-list.component.css']
})
export class PendingListComponent implements OnInit {

  title = 'Pending Account';

  get columns() {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    this.preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage = updatedColumns;
    this.preferencesService.update(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {});
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.onColumnsChange(defaultTableColumnInfo.columnsOfPendingAccountPage);
  }

  get displayedColumns(): string[] {
    let displayedColumns = [];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    const columns = this.preferencesInfo.tableColumnsInfo.columnsOfPendingAccountPage;
    for (const column of columns) {
      switch (column.name) {
        case 'Preferred Username':
          if (column.selected) {
            displayedColumns.push('preferredUsername');
          }
          break;
        case 'Email':
          if (column.selected) {
            displayedColumns.push('email');
          }
          break;
        case 'Provider':
          if (column.selected) {
            displayedColumns.push('providerName');
          }
          break;
        case 'Created On':
          if (column.selected) {
            displayedColumns.push('creationTimestamp');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<PendingAccount>();
  loading = false;

  request: PendingListRequest;
  preferencesInfo: PreferencesInfo;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: PendingListService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new PendingListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('creationTimestamp', 'desc', 0, 10));

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

      this.loadPendingList(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadPendingList();
  }

  onChange(property?: string, source?) {
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'startDate':
        this.request.createdDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.request.createdDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.request.createdDate.start = null;
        break;
      case 'endDate':
        this.request.createdDate.end = null;
        break;
    }
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadPendingList();
  }

  loadPendingList(isInit?: boolean) {
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getPendingList(this.request).pipe(
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
