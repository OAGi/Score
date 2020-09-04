import {Component, OnInit, ViewChild} from '@angular/core';
import {BusinessContext, BusinessContextListRequest} from '../domain/business-context';
import {BusinessContextService} from '../domain/business-context.service';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../basis/basis';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';

@Component({
  selector: 'score-business-context',
  templateUrl: './business-context-list.component.html',
  styleUrls: ['./business-context-list.component.css']
})
export class BusinessContextListComponent implements OnInit {

  title = 'Business Context';
  displayedColumns: string[] = [
    'select', 'name', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BusinessContext>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessContextListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BusinessContextService,
              private accountService: AccountListService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new BusinessContextListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadBusinessContextList(true);
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

  loadBusinessContextList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getBusinessContextList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BusinessContext) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        elm.bizCtxValues = [];
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
    if (!row.used) {
      this.selection.select(row.bizCtxId);
    }
  }

  toggle(row: BusinessContext) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.bizCtxId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessContext) {
    return this.selection.isSelected(row.bizCtxId);
  }

  discard() {
    const bizCtxIds = this.selection.selected;

    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard Business ' + (bizCtxIds.length > 1 ? 'Contexts' : 'Context') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected business ' + (bizCtxIds.length > 1 ? 'contexts' : 'context') + '?',
      'The business ' + (bizCtxIds.length > 1 ? 'contexts' : 'context') + ' will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...bizCtxIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 1000,
            });
            this.selection.clear();
            this.loadBusinessContextList();
          });
        }
      });
  }

}
