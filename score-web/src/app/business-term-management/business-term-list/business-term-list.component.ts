import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BusinessTermListEntry} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';
import {BUSINESS_TERM_TABLE} from '../domain/business-term.table';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, ScoreTableViewComponent} from '../../common/score-table';
import {BusinessTermImportDialogComponent} from '../business-term-import-dialog/business-term-import-dialog.component';
import {BatchImportResult} from '../business-term-import-dialog/business-term-import.model';

@Component({
  standalone: false,
  selector: 'score-business-term-list',
  templateUrl: './business-term-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BusinessTermListComponent {
  private service = inject(BusinessTermService);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(BUSINESS_TERM_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessTermListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.service.getBusinessTermListPage(params);
  readonly rowKey = (row: BusinessTermListEntry) => row.businessTermId;
  readonly rowSelectable = (row: BusinessTermListEntry) => !row.used;

  openImportDialog() {
    const ref = this.dialog.open(BusinessTermImportDialogComponent, {
      width: '80vw',
      maxWidth: '90vw',
      maxHeight: '90vh',
      panelClass: 'score-centered-dialog-panel',
      autoFocus: false,
      disableClose: false
    });
    ref.afterClosed().subscribe((result?: BatchImportResult) => {
      if (result && (result.createdCount + result.updatedCount) > 0) {
        this.view().reload();
      }
    });
  }

  discard() {
    const businessTermIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (businessTermIds.length > 1 ? 'business terms' : 'business term') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' + (businessTermIds.length > 1 ? 'business terms' : 'business term') + '?',
      'The ' + (businessTermIds.length > 1 ? 'business terms' : 'business term') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...businessTermIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
          }, err => {
            this.snackBar.open('Discard\'s forbidden! The business term is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }
}
