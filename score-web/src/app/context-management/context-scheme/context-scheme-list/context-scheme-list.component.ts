import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ContextSchemeListEntry} from '../domain/context-scheme';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';

const CONTEXT_SCHEME_TABLE = defineTable({
  storageKey: 'TableColumns-ContextSchemePage',
  columns: [
    {id: 'schemeName', label: 'Name', width: 0, sortable: true},
    {id: 'contextCategoryName', label: 'Context Category', width: 400, sortable: true},
    {id: 'schemeId', label: 'Scheme ID', width: 200, sortable: true},
    {id: 'schemeAgencyId', label: 'Agency ID', width: 200, sortable: true},
    {id: 'schemeVersionId', label: 'Version', width: 200, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-context-scheme',
  templateUrl: './context-scheme-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class ContextSchemeListComponent {
  private service = inject(ContextSchemeService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(CONTEXT_SCHEME_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<ContextSchemeListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.service.getContextSchemeList(params);
  readonly rowKey = (row: ContextSchemeListEntry) => row.contextSchemeId;
  readonly rowSelectable = (row: ContextSchemeListEntry) => !row.used;

  discard() {
    const contextSchemeIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Context ' + (contextSchemeIds.length > 1 ? 'Schemes' : 'Scheme') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected context ' + (contextSchemeIds.length > 1 ? 'schemes' : 'scheme') + '?',
      'The context ' + (contextSchemeIds.length > 1 ? 'schemes' : 'scheme') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...contextSchemeIds).subscribe(_ => {
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
