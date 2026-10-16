import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AccountListEntry} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {ACCOUNT_TABLE} from '../domain/account.table';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns} from '../../common/score-table';

@Component({
  standalone: false,
  selector: 'score-transfer-ownership-list',
  templateUrl: './transfer-ownership-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class TransferOwnershipListComponent {
  private service = inject(AccountListService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(ACCOUNT_TABLE);
  selection = new SelectionModel<AccountListEntry>(false, []);
  /** While the ownership is being transferred. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getAccountsListPage(params);

  transferOwnership(row: AccountListEntry): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Transfer Ownership?';
    dialogConfig.data.content = [
      'Are you sure you want to transfer ownership of all components from the user ',
      '\'' + row.username + ' (' + row.loginId + ')\' to you? This action is irreversible.'
    ];
    dialogConfig.data.action = 'Transfer';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.busy.set(true);
          this.service.transferOwnership(row.userId).subscribe(_ => {
            this.busy.set(false);
            this.selection.clear();

            this.snackBar.open('Transferred', '', {
              duration: 3000,
            });
          }, error => {
            this.busy.set(false);
          });
        }
      });
  }
}
