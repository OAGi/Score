import {SelectionModel} from '@angular/cdk/collections';
import {Component, OnInit, ViewChild} from '@angular/core';
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

@Component({
  selector: 'score-log-list',
  templateUrl: './log-list.component.html',
  styleUrls: ['./log-list.component.css'],
})
export class LogListComponent implements OnInit {

  logs: Log[];
  request: LogListRequest;
  loading: boolean;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  displayedColumns: string[] = [
    'check', 'commit', 'revisionNum', 'revisionAction', 'loginId', 'timestamp'
  ];
  dataSource = new MatTableDataSource<Log>();
  selection = new SelectionModel<number>(true, []);

  constructor(private service: LogService,
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
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.getRevisions();
    });

    this.getRevisions(true);
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
