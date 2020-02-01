import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource, PageEvent} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext, BusinessContextListRequest} from '../../context-management/business-context/domain/business-context';
import {Router} from '@angular/router';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {PageRequest} from '../../basis/basis';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';

@Component({
  selector: 'srt-bie-create',
  templateUrl: './bie-create-biz-ctx.component.html',
  styleUrls: ['./bie-create-biz-ctx.component.css']
})
export class BieCreateBizCtxComponent implements OnInit {
  title = 'Create BIE';
  subtitle = 'Select Business Contexts';

  displayedColumns: string[] = [
    'select', 'name', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BusinessContext>();
  selection = new SelectionModel<BusinessContext>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessContextListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private accountService: AccountListService,
              private router: Router) {
  }

  ngOnInit() {
    this.request = new BusinessContextListRequest();

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'name';
    this.sort.direction = 'asc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });
    this.onChange();
  }

  onPageChange(event: PageEvent) {
    this.loadBusinessContextList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadBusinessContextList();
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
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.request.updatedDate.end = null;
        break;
    }
  }

  loadBusinessContextList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bizCtxService.getBusinessContextList(this.request)
      .subscribe(resp => {
        this.paginator.length = resp.length;
        this.dataSource.data = resp.list.map((elm: BusinessContext) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          elm.bizCtxValues = [];
          return elm;
        });
        this.loading = false;
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
      this.dataSource.data.forEach(row => this.selection.select(row));
  }

  next() {
    const selectedBizCtxIds = this.selection.selected.map(e => e.bizCtxId).join(',');
    this.router.navigate(['/profile_bie/create/asccp'], {queryParams: {bizCtxIds: selectedBizCtxIds}});
  }

}
