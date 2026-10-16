import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {AccountListEntry} from '../../account-management/domain/accounts';
import {ACCOUNT_TABLE} from '../../account-management/domain/account.table';
import {AuthService} from '../../authentication/auth.service';
import {createTableColumns, defineTable} from '../score-table';

/** The account list, to choose who the ownership goes to: the role is given by the one who opens the dialog. */
const TRANSFER_OWNERSHIP_TABLE = defineTable({
  ...ACCOUNT_TABLE,
  filters: [ACCOUNT_TABLE.filters[0], ACCOUNT_TABLE.filters[1]],
});

@Component({
  standalone: false,
  selector: 'score-transfer-ownership-dialog',
  templateUrl: './transfer-ownership-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./transfer-ownership-dialog.component.css']
})
export class TransferOwnershipDialogComponent {
  dialogRef = inject<MatDialogRef<TransferOwnershipDialogComponent>>(MatDialogRef);
  private auth = inject(AuthService);
  private accountService = inject(AccountListService);
  data = inject(MAT_DIALOG_DATA);

  readonly columns = createTableColumns(TRANSFER_OWNERSHIP_TABLE);
  selection = new SelectionModel<AccountListEntry>(false, []);

  /** Not the requester; and, of the roles and the business contexts that the dialog is opened for. */
  readonly extraParams: Record<string, string> = this.extraParamsOf();

  readonly load = (params: HttpParams) => this.accountService.getAccountsListPage(params);

  private extraParamsOf(): Record<string, string> {
    const params: Record<string, string> = {excludeRequester: 'true'};
    if (this.auth.getUserToken().tenant.enabled && this.data?.businessCtxIds?.length > 0) {
      params['businessCtxIds'] = this.data.businessCtxIds.join(',');
    }
    if (this.data && this.data.roles) {
      params['roles'] = this.data.roles.join(',');
    }
    return params;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
