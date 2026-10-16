import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategoryListEntry} from '../domain/context-category';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';

const CONTEXT_CATEGORY_TABLE = defineTable({
  storageKey: 'TableColumns-ContextCategoryPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'description', label: 'Description', width: '60%', sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'text', key: 'description', label: 'Description'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-context-category',
  templateUrl: './context-category-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class ContextCategoryListComponent {
  private service = inject(ContextCategoryService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(CONTEXT_CATEGORY_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<ContextCategoryListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.service.getContextCategoryList(params);
  readonly rowKey = (row: ContextCategoryListEntry) => row.contextCategoryId;
  readonly rowSelectable = (row: ContextCategoryListEntry) => !row.used;

  discard() {
    const contextCategoryIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Context ' + (contextCategoryIds.length > 1 ? 'Categories' : 'Category') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected context ' + (contextCategoryIds.length > 1 ? 'categories' : 'category') + '?',
      'The context ' + (contextCategoryIds.length > 1 ? 'categories' : 'category') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...contextCategoryIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
          });
        }
      });
  }
}
