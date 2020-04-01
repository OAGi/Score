import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {BusinessContext, BusinessContextListRequest} from '../domain/business-context';
import {BusinessContextService} from '../domain/business-context.service';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogConfig,
  MatPaginator,
  MatSnackBar,
  MatSort,
  MatTableDataSource,
  PageEvent
} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {PageRequest} from '../../../basis/basis';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';

@Component({
  selector: 'srt-business-context',
  templateUrl: './business-context-list.component.html',
  styleUrls: ['./business-context-list.component.css']
})
export class BusinessContextListComponent implements OnInit {

  title = 'Business Contexts';
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
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new BusinessContextListRequest();

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'lastUpdateTimestamp';
    this.sort.direction = 'desc';
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

    this.service.getBusinessContextList(this.request)
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
    this.openDialogContextSchemeListDiscard();
  }

  openDialogContextSchemeListDiscard() {
    const dialogConfig = new MatDialogConfig();
    const bizCtxIds = this.selection.selected;
    dialogConfig.data = {ids: bizCtxIds};

    const dialogRef = this.dialog.open(DialogContentBizContextListDiscardComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
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

@Component({
  selector: 'srt-dialog-content-biz-context-dialog-discard',
  templateUrl: 'dialog-biz-context-list-discard-dialog.html',
})
export class DialogContentBizContextListDiscardComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
