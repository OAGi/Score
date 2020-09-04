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

@Component({
  selector: 'score-pending-list',
  templateUrl: './pending-list.component.html',
  styleUrls: ['./pending-list.component.css']
})
export class PendingListComponent implements OnInit {

  title = 'Pending Account';
  displayedColumns: string[] = [
    'preferredUsername', 'email', 'providerName', 'creationTimestamp'
  ];
  dataSource = new MatTableDataSource<PendingAccount>();
  loading = false;

  request: PendingListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: PendingListService,
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
      this.paginator.pageIndex = 0;
      this.loadPendingList();
    });

    this.loadPendingList(true);
  }

  onPageChange(event: PageEvent) {
    this.loadPendingList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadPendingList();
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
