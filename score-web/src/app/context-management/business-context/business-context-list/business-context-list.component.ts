import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BusinessContextListEntry} from '../domain/business-context';
import {BusinessContextService} from '../domain/business-context.service';
import {BUSINESS_CONTEXT_TABLE} from '../domain/business-context.table';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, ScoreTableViewComponent} from '../../../common/score-table';

@Component({
  standalone: false,
  selector: 'score-business-context',
  templateUrl: './business-context-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BusinessContextListComponent {
  private service = inject(BusinessContextService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(BUSINESS_CONTEXT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessContextListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.service.getBusinessContextListPage(params);
  readonly rowKey = (row: BusinessContextListEntry) => row.businessContextId;
  readonly rowSelectable = (row: BusinessContextListEntry) => !row.used;

  discard() {
    const businessContextIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Business ' + (businessContextIds.length > 1 ? 'Contexts' : 'Context') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected business ' + (businessContextIds.length > 1 ? 'contexts' : 'context') + '?',
      'The business ' + (businessContextIds.length > 1 ? 'contexts' : 'context') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...businessContextIds).subscribe(_ => {
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
