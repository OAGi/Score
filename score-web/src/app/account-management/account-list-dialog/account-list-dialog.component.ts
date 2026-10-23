import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {tap} from 'rxjs/operators';
import {AccountListService} from '../domain/account-list.service';
import {AccountListEntry} from '../domain/accounts';
import {ACCOUNT_TABLE} from '../domain/account.table';
import {PendingAccount} from '../domain/pending-list';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

/**
 * The accounts of the list, with the column settings of the account list: the dialog shows whether an account is connected to
 * an OAuth2 user where the list has the status.
 */
const ACCOUNT_LIST_DIALOG_TABLE = defineTable({
  ...ACCOUNT_TABLE,
  columns: [
    ...ACCOUNT_TABLE.columns.filter(c => c.id !== 'status'),
    {id: 'appOauth2UserId', label: 'Status', header: 'OAuth2 User', width: 100, sortable: true},
  ],
  filters: [
    ACCOUNT_TABLE.filters[0],
    ACCOUNT_TABLE.filters[1],
    ACCOUNT_TABLE.filters[2],
  ],
});

@Component({
  standalone: false,
  selector: 'score-account-list-dialog',
  templateUrl: './account-list-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./account-list-dialog.component.css']
})
export class AccountListDialogComponent {
  private service = inject(AccountListService);
  dialogRef = inject<MatDialogRef<AccountListDialogComponent>>(MatDialogRef);
  pending = inject<PendingAccount>(MAT_DIALOG_DATA);

  title = 'Link to existing account';

  readonly columns = createTableColumns(ACCOUNT_LIST_DIALOG_TABLE);
  selection = new SelectionModel<AccountListEntry>(false, []);

  readonly view = viewChild.required<ScoreTableViewComponent<AccountListEntry>>(ScoreTableViewComponent);
  readonly ready = signal(false);
  /** Only the accounts that are not connected to an SSO user, of the roles that can be linked. */
  readonly extraParams: Record<string, string> = {excludeSSO: 'true'};

  readonly load = (params: HttpParams) => this.service.getAccountsListPage(params).pipe(tap(() => this.selection.clear()));

  constructor() {
    afterNextRender(() => {
      // Both roles, to begin with.
      this.view().setFilter('roles', ['developer', 'end-user']);
      this.ready.set(true);
    });
  }

  link() {
    if (this.selection.selected.length > 0) {
      this.service.link(this.pending, this.selection.selected[0].userId).subscribe(resp => {
        this.dialogRef.close('Linked');
      }, error => {
      });
    }
  }
  close() {
    this.dialogRef.close('');
  }
}
