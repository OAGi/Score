import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContextListEntry} from '../../context-management/business-context/domain/business-context';
import {BUSINESS_CONTEXT_TABLE} from '../../context-management/business-context/domain/business-context.table';
import {AuthService} from '../../authentication/auth.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

const BIZ_CTX_TABLE = defineTable({
  ...BUSINESS_CONTEXT_TABLE,
  defaultSort: {active: 'name', direction: 'asc'},
});

/** In the multi-tenant mode, the business contexts have a tenant column, and their own column settings. */
const BIZ_CTX_WITH_TENANT_TABLE = defineTable({
  ...BIZ_CTX_TABLE,
  storageKey: 'TableColumns-BusinessContextWithTenantPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'tenant', label: 'Tenant', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-bie-create',
  templateUrl: './bie-create-biz-ctx.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-create-biz-ctx.component.css']
})
export class BieCreateBizCtxComponent {
  private bizCtxService = inject(BusinessContextService);
  private auth = inject(AuthService);
  private router = inject(Router);

  title = 'Create BIE';
  subtitle = 'Select Business Contexts';

  readonly columns = createTableColumns(this.isTenantEnabled ? BIZ_CTX_WITH_TENANT_TABLE : BIZ_CTX_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessContextListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  /** In the multi-tenant mode, only the business contexts of the BIEs that can be edited. */
  readonly extraParams: Record<string, string> = this.isTenantEnabled ? {isBieEditing: 'true'} : {};

  readonly load = (params: HttpParams) => this.bizCtxService.getBusinessContextListPage(params);
  readonly rowKey = (row: BusinessContextListEntry) => row.businessContextId;

  next() {
    const selectedBizCtxIds = this.selection.selected.join(',');
    this.router.navigate(['/profile_bie/create/asccp'], {queryParams: {businessContextIdList: selectedBizCtxIds}});
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

}
