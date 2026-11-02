import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {AccountListService} from '../domain/account-list.service';
import {ACCOUNT_TABLE} from '../domain/account.table';
import {createTableColumns} from '../../common/score-table';
import {CompactRecord, memoizeRow} from '../../common/record-compact-row';
import {AccountCompactSource, accountCompactRecord} from '../../account-management/domain/account-compact';

@Component({
  standalone: false,
  selector: 'score-account-list',
  templateUrl: './account-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class AccountListComponent {
  private service = inject(AccountListService);

  /** What the compact row shows of an account: made once for a row. */
  readonly compactRecord = memoizeRow((account: AccountCompactSource): CompactRecord => accountCompactRecord(account, {status: true, link: '/account/' + account['userId']}));

  readonly columns = createTableColumns(ACCOUNT_TABLE);
  readonly load = (params: HttpParams) => this.service.getAccountsListPage(params);
}
