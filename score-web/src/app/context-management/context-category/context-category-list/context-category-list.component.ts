import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategory, ContextCategoryListRequest} from '../domain/context-category';
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

@Component({
  selector: 'srt-context-category',
  templateUrl: './context-category-list.component.html',
  styleUrls: ['./context-category-list.component.css']
})
export class ContextCategoryListComponent implements OnInit {

  title = 'Context Categories';
  displayedColumns: string[] = ['select', 'name', 'description'];
  dataSource = new MatTableDataSource<ContextCategory>();
  selection = new SelectionModel<ContextCategory>(true, []);
  loading = false;

  request: ContextCategoryListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextCategoryService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new ContextCategoryListRequest();

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'name';
    this.sort.direction = 'asc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.onChange();
  }

  onPageChange(event: PageEvent) {
    this.loadContextCategoryList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadContextCategoryList();
  }

  loadContextCategoryList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getContextCategoryList(this.request)
      .subscribe(resp => {
        this.paginator.length = resp.length;
        this.dataSource.data = resp.list;
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

  select(row) {
    if (!row.used) {
      this.selection.select(row);
    }
  }

  toggle(row) {
    if (this.selection.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  discard() {
    this.openDialogContextCategoryListDiscard();
  }

  openDialogContextCategoryListDiscard() {
    const dialogConfig = new MatDialogConfig();
    const ctxCategoryIds = [];
    for (const elm of this.selection.selected) {
      ctxCategoryIds.push(elm.ctxCategoryId);
    }
    dialogConfig.data = {ids: ctxCategoryIds};

    const dialogRef = this.dialog.open(DialogDiscardContextCategoryDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {


        this.service.delete(...ctxCategoryIds).subscribe(_ => {
          this.snackBar.open('Discarded', '', {
            duration: 1000,
          });
          this.selection.clear();
          this.loadContextCategoryList();
        });
      }
    });
  }

}

@Component({
  selector: 'srt-dialog-content-context-category-dialog',
  templateUrl: 'dialog-discard-context-category-dialog.html',
})
export class DialogDiscardContextCategoryDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
