import {SelectionModel} from '@angular/cdk/collections';
import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute, Router} from '@angular/router';
import {Log, LogListRequest} from '../domain/log';
import {LogService} from '../domain/log.service';
import {LogCompareDialogComponent} from '../log-compare-dialog/log-compare-dialog.component';
import {PageRequest} from '../../basis/basis';
import {finalize} from 'rxjs/operators';
import {Location} from '@angular/common';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';
import {AuthService} from '../../authentication/auth.service';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';

@Component({
  selector: 'score-log-list',
  templateUrl: './log-list.component.html',
  styleUrls: ['./log-list.component.css'],
})
export class LogListComponent implements OnInit {

  logs: Log[];
  request: LogListRequest;
  preferencesInfo: PreferencesInfo;
  loading: boolean;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfLogPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfLogPage = columns;
    this.updateTableColumnsForLogPage();
  }

  updateTableColumnsForLogPage() {
    this.preferencesService.updateTableColumnsForLogPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfLogPage;
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
      case 'Created at':
        this.setWidth('Created At', $event.width);
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
      this.updateTableColumnsForLogPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['check'];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Commit':
          if (column.selected) {
            displayedColumns.push('commit');
          }
          break;
        case 'Revision':
          if (column.selected) {
            displayedColumns.push('revisionNum');
          }
          break;
        case 'Action':
          if (column.selected) {
            displayedColumns.push('revisionAction');
          }
          break;
        case 'Actor':
          if (column.selected) {
            displayedColumns.push('loginId');
          }
          break;
        case 'Created At':
          if (column.selected) {
            displayedColumns.push('timestamp');
          }
          break;
      }
    }
    return displayedColumns;
  }

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  dataSource = new MatTableDataSource<Log>();
  selection = new SelectionModel<number>(true, []);

  constructor(private service: LogService,
              private preferencesService: SettingsPreferencesService,
              private auth: AuthService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new LogListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.reference = this.route.snapshot.paramMap.get('reference');

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

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
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.getRevisions(true);
    }, error => {
      this.loading = false;
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.getRevisions();
  }

  getRevisions(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getRevisions(this.request).pipe(
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
    this.getRevisions();
  }

  isSelected(elem: Log): boolean {
    return this.selection.isSelected(elem.logId);
  }

  toggle(elem: Log) {
    if (!this.isSelected(elem)) {
      if (this.selection.selected.length > 1) {
        this.selection.deselect(this.selection.selected[0]);
      }
    }
    this.selection.toggle(elem.logId);
  }

  openCompareDialog() {
    if (this.selection.selected.length !== 2) {
      return false;
    }

    let before;
    let after;
    if (this.selection.selected[0] > this.selection.selected[1]) {
      before = this.selection.selected[1];
      after = this.selection.selected[0];
    } else {
      before = this.selection.selected[0];
      after = this.selection.selected[1];
    }
    this.dialog.open(LogCompareDialogComponent, {
      data: {
        before,
        after
      },
      width: '100%',
      maxWidth: '100%'
    });
  }

  logAction(log: Log) {
    if (log.logAction === 'Revised' && !log.developer) {
      return 'Amended';
    }
    return log.logAction;
  }
}
