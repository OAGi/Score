import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {PendingListService} from '../domain/pending-list.service';
import {createTableColumns, defineTable} from '../../common/score-table';
import {CompactRecord, memoizeRow} from '../../common/record-compact-row';
import {formatDate} from '@angular/common';
import {PendingAccount} from '../domain/pending-list';

const PENDING_ACCOUNT_TABLE = defineTable({
  storageKey: 'TableColumns-PendingAccountPage',
  columns: [
    {id: 'preferredUsername', label: 'Preferred Username', width: 0, sortable: true},
    {id: 'email', label: 'Email', width: 400, sortable: true},
    {id: 'providerName', label: 'Provider', width: 400, sortable: true},
    {id: 'creationTimestamp', label: 'Created On', header: 'Created', width: 160, sortable: true},
  ],
  sortParam: 'sortActive',
  defaultSort: {active: 'creationTimestamp', direction: 'desc'},
  search: {key: 'preferredUsername', placeholder: 'Search by Preferred Username'},
  filters: [
    {type: 'text', key: 'email', label: 'Email'},
    {type: 'text', key: 'providerName', label: 'Provider'},
    {type: 'dateRange', key: 'createdDate', label: 'Created', httpParam: 'createdOn'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-pending-list',
  templateUrl: './pending-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class PendingListComponent {
  private service = inject(PendingListService);

  /** What the compact row shows of a pending account: made once for a row. */
  readonly compactRecord = memoizeRow((account: PendingAccount): CompactRecord => ({
    badges: account.providerName ? [{text: account.providerName}] : [],
    title: account.preferredUsername,
    link: '/account/pending/' + account['appOauth2UserId'],
    subtitle: account.email,
    meta: account.creationTimestamp ? [{label: 'Created', value: formatDate(account.creationTimestamp, 'yyyy-MM-dd HH:mm', 'en-US')}] : []
  }));

  readonly columns = createTableColumns(PENDING_ACCOUNT_TABLE);
  readonly load = (params: HttpParams) => this.service.getPendingList(params);
}
