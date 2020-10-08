import {Component, OnInit, ViewChild} from '@angular/core';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategory, ContextCategoryListRequest} from '../domain/context-category';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {PageRequest} from '../../../basis/basis';
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-context-category',
  templateUrl: './context-category-list.component.html',
  styleUrls: ['./context-category-list.component.css']
})
export class ContextCategoryListComponent implements OnInit {

  title = 'Context Category';
  displayedColumns: string[] = ['select', 'name', 'description'];
  dataSource = new MatTableDataSource<ContextCategory>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  request: ContextCategoryListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextCategoryService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new ContextCategoryListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.loadContextCategoryList(true);
  }

  onPageChange(event: PageEvent) {
    this.loadContextCategoryList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadContextCategoryList();
  }

  loadContextCategoryList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getContextCategoryList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
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

  select(row: ContextCategory) {
    if (!row.used) {
      this.selection.select(row.ctxCategoryId);
    }
  }

  toggle(row: ContextCategory) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.ctxCategoryId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ContextCategory) {
    return this.selection.isSelected(row.ctxCategoryId);
  }

  discard() {
    const ctxCategoryIds = this.selection.selected;

    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard Context ' + (ctxCategoryIds.length > 1 ? 'Categories' : 'Category') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected context ' + (ctxCategoryIds.length > 1 ? 'categories' : 'category') + '?',
      'The context ' + (ctxCategoryIds.length > 1 ? 'categories' : 'category') + ' will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...ctxCategoryIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadContextCategoryList();
          });
        }
      });
  }

}
