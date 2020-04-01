import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {ContextScheme, ContextSchemeListRequest} from '../domain/context-scheme';
import {ContextSchemeService} from '../domain/context-scheme.service';
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
import {PageRequest} from '../../../basis/basis';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';

@Component({
  selector: 'srt-context-scheme',
  templateUrl: './context-scheme-list.component.html',
  styleUrls: ['./context-scheme-list.component.css']
})
export class ContextSchemeListComponent implements OnInit {

  title = 'Context Schemes';
  displayedColumns: string[] = [
    'select', 'schemeName', 'ctxCategoryName', 'schemeId', 'schemeAgencyId',
    'schemeVersionId', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<ContextScheme>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: ContextSchemeListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextSchemeService,
              private accountService: AccountListService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new ContextSchemeListRequest();

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
    this.loadContextSchemeList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadContextSchemeList();
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

  loadContextSchemeList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getContextSchemeList(this.request)
      .subscribe(resp => {
        this.paginator.length = resp.length;
        this.dataSource.data = resp.list.map((elm: ContextScheme) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          elm.ctxSchemeValues = [];
          return elm;
        });
        this.loading = false;
      });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => !row.used).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: ContextScheme) {
    if (!row.used) {
      this.selection.select(row.ctxSchemeId);
    }
  }

  toggle(row: ContextScheme) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.ctxSchemeId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ContextScheme) {
    return this.selection.isSelected(row.ctxSchemeId);
  }

  discard() {
    this.openDialogContextSchemeListDiscard();
  }

  openDialogContextSchemeListDiscard() {
    const dialogConfig = new MatDialogConfig();
    const ctxSchemeIds = this.selection.selected;
    dialogConfig.data = {ids: ctxSchemeIds};
    const dialogRef = this.dialog.open(DialogContentContextSchemeListDiscardComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.delete(...ctxSchemeIds).subscribe(_ => {
          this.snackBar.open('Discarded', '', {
            duration: 1000,
          });
          this.selection.clear();
          this.loadContextSchemeList();
        });
      }
    });
  }

}

@Component({
  selector: 'srt-dialog-content-context-scheme-discard-dialog',
  templateUrl: 'dialog-context-scheme-list-discard-dialog.html',
})
export class DialogContentContextSchemeListDiscardComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
