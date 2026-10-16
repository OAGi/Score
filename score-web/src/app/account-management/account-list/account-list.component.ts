import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {AccountListService} from '../domain/account-list.service';
import {ACCOUNT_TABLE} from '../domain/account.table';
import {createTableColumns} from '../../common/score-table';

@Component({
  standalone: false,
  selector: 'score-account-list',
  templateUrl: './account-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class AccountListComponent {
  private service = inject(AccountListService);

  readonly columns = createTableColumns(ACCOUNT_TABLE);
  readonly load = (params: HttpParams) => this.service.getAccountsListPage(params);
}
