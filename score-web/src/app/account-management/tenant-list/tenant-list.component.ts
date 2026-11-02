import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {TenantListService} from '../domain/tenant-list.service';
import {createTableColumns, defineTable} from '../../common/score-table';
import {CompactRecord, memoizeRow} from '../../common/record-compact-row';

const TENANT_TABLE = defineTable({
  storageKey: 'TableColumns-TenantPage',
  columns: [
    {id: 'name', label: 'Tenant Name', width: 0, sortable: true},
    {id: 'users', label: 'Users', width: 400},
    {id: 'businessContexts', label: 'Business Contexts', width: 400},
  ],
  // The endpoint takes only the direction of the sort.
  sortParam: 'sortDirection',
  defaultSort: {active: 'sortActive', direction: 'asc'},
  search: {key: 'name', placeholder: 'Search by Name'},
});

@Component({
  standalone: false,
  selector: 'score-tenant-list',
  templateUrl: './tenant-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class TenantListComponent {
  private service = inject(TenantListService);

  /** What the compact row shows of a tenant: made once for a row. */
  readonly compactRecord = memoizeRow((tenant: { tenantId: number; name: string }): CompactRecord => ({
    title: tenant.name,
    link: '/tenant/' + tenant.tenantId
  }));

  readonly columns = createTableColumns(TENANT_TABLE);
  readonly load = (params: HttpParams) => this.service.getTenants(params);
}
