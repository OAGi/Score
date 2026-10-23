import {ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Title} from '@angular/platform-browser';
import {finalize} from 'rxjs/operators';
import {TenantListService} from '../domain/tenant-list.service';
import {TenantList} from '../domain/tenants';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContextListEntry} from '../../context-management/business-context/domain/business-context';
import {BUSINESS_CONTEXT_TABLE} from '../../context-management/business-context/domain/business-context.table';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

const TENANT_BUSINESS_CONTEXT_TABLE = defineTable({
  ...BUSINESS_CONTEXT_TABLE,
  storageKey: 'TableColumns-TenantManagementForBusinessContextPage',
  columns: [...BUSINESS_CONTEXT_TABLE.columns, {id: 'manage', label: 'Manage', width: 120}],
});

@Component({
  standalone: false,
  selector: 'score-tenant-biz-ctx-detail',
  templateUrl: './tenant-biz-ctx-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class TenantBusinessCtxDetailComponent implements OnInit {
  private service = inject(TenantListService);
  private bizCtxService = inject(BusinessContextService);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private titleService = inject(Title);

  title = 'Business Context Management';
  tenantInfo: TenantList;
  tenantId: number;
  addBusinessCtxToTenant = false;

  readonly columns = createTableColumns(TENANT_BUSINESS_CONTEXT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessContextListEntry>>(ScoreTableViewComponent);

  /** The list waits for the tenant. */
  readonly ready = signal(false);
  readonly busy = signal(false);
  /** The business contexts of the tenant, or the ones that can be added to it. */
  readonly extraParams = signal<Record<string, string>>({});

  readonly load = (params: HttpParams) => this.bizCtxService.getBusinessContextListPage(params);

  ngOnInit() {
    this.tenantId = Number(this.route.snapshot.paramMap.get('id'));
    this.applyParams();
    this.service.getTenantInfo(this.tenantId).subscribe({
      next: resp => {
        if (!resp) {
          this.redirectToTenantList();
          return;
        }
        this.tenantInfo = resp;
        setAppTitleIfPresent(this.titleService, this.tenantInfo.name, 'Tenant Business Context');
        this.ready.set(true);
      },
      error: error => {
        if (error.status === 404) {
          this.redirectToTenantList();
        }
      }
    });
  }

  private applyParams() {
    const params: Record<string, string> = {tenantId: '' + this.tenantId};
    if (this.addBusinessCtxToTenant) {
      params['notConnectedToTenant'] = 'true';
    }
    this.extraParams.set(params);
  }

  private redirectToTenantList() {
    this.snackBar.open('The requested tenant is unavailable.', '', {
      duration: 3000,
    });
    this.router.navigateByUrl('/tenant');
  }

  setAddBusinessCtxView() {
    this.addBusinessCtxToTenant = true;
    this.applyParams();
  }

  setListView() {
    this.addBusinessCtxToTenant = false;
    this.applyParams();
  }

  removeTenantBusinessCtx(businessCtxId: number) {
    this.busy.set(true);
    this.service.deleteTenantBusinessCtx(this.tenantId, businessCtxId).pipe(
      finalize(() => this.busy.set(false))
    ).subscribe(() => this.view().reload());
  }

  addTenantBusinessCtx(businessCtxId: number) {
    this.busy.set(true);
    this.service.addTenantBusinessCtx(this.tenantId, businessCtxId).pipe(
      finalize(() => this.busy.set(false))
    ).subscribe(() => this.view().reload());
  }
}
