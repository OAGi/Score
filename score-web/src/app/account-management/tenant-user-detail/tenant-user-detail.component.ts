import {ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Title} from '@angular/platform-browser';
import {finalize} from 'rxjs/operators';
import {AccountListEntry} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {ACCOUNT_TABLE} from '../domain/account.table';
import {TenantListService} from '../domain/tenant-list.service';
import {TenantList} from '../domain/tenants';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

const TENANT_USER_TABLE = defineTable({
  ...ACCOUNT_TABLE,
  storageKey: 'TableColumns-TenantManagementForAccountPage',
  // The roles of an account are not a condition of the users of a tenant.
  filters: ACCOUNT_TABLE.filters.filter(f => f.key !== 'roles'),
  columns: [...ACCOUNT_TABLE.columns, {id: 'manage', label: 'Manage', width: 120}],
});

@Component({
  standalone: false,
  selector: 'score-tenant-user-detail',
  templateUrl: './tenant-user-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class TenantUserDetailComponent implements OnInit {
  private service = inject(TenantListService);
  private accountService = inject(AccountListService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private titleService = inject(Title);

  title = 'Users Management';
  tenantInfo: TenantList;
  tenantId: number;
  addUserToTenant = false;

  readonly columns = createTableColumns(TENANT_USER_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<AccountListEntry>>(ScoreTableViewComponent);

  /** The list waits for the tenant. */
  readonly ready = signal(false);
  readonly busy = signal(false);
  /** The users of the tenant, or the ones that can be added to it. */
  readonly extraParams = signal<Record<string, string>>({});

  readonly load = (params: HttpParams) => this.accountService.getAccountsListPage(params);

  ngOnInit(): void {
    this.tenantId = Number(this.route.snapshot.paramMap.get('id'));
    this.applyParams();
    this.service.getTenantInfo(this.tenantId).subscribe({
      next: resp => {
        if (!resp) {
          this.redirectToTenantList();
          return;
        }
        this.tenantInfo = resp;
        setAppTitleIfPresent(this.titleService, this.tenantInfo.name, 'Tenant Users');
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
    if (this.addUserToTenant) {
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

  removeUser(userId: number) {
    this.busy.set(true);
    this.service.deleteTenantUser(this.tenantId, userId).pipe(
      finalize(() => this.busy.set(false))
    ).subscribe(() => this.view().reload());
  }

  setAddUserView() {
    this.addUserToTenant = true;
    this.applyParams();
  }

  setListView() {
    this.addUserToTenant = false;
    this.applyParams();
  }

  addUser(userId: number) {
    this.busy.set(true);
    this.service.addTenantUser(this.tenantId, userId).pipe(
      finalize(() => this.busy.set(false))
    ).subscribe(() => this.view().reload());
  }
}
