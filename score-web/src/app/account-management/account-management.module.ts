import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AccountListDialogComponent} from './account-list-dialog/account-list-dialog.component';
import {PendingListService} from './domain/pending-list.service';
import {PendingDetailComponent} from './pending-detail/pending-detail.component';
import {PendingListComponent} from './pending-list/pending-list.component';
import {
  CanActivateAdmin,
  CanActivateDeveloper,
  CanActivateTenantInstance,
  CanActivateUser
} from '../authentication/auth.service';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {AccountListComponent} from './account-list/account-list.component';
import {AccountListService} from './domain/account-list.service';
import {AccountDetailComponent} from './account-detail/account-detail.component';
import {AccountCreateComponent} from './account-create/account-create.component';
import {TenantListComponent} from './tenant-list/tenant-list.component';
import {TenantBusinessCtxDetailComponent} from './tenant-biz-ctx-detail/tenant-biz-ctx-detail.component';
import {TenantListService} from './domain/tenant-list.service';
import {TenantUserDetailComponent} from './tenant-user-detail/tenant-user-detail.component';
import {TenantCreateComponent} from './tenant-create/tenant-create.component';
import {UpdateTenantComponent} from './tenant-update/tenant-update.component';
import {TransferOwnershipListComponent} from './take-ownership-list/transfer-ownership-list.component';
import {SearchBarModule} from '../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';


const routes: Routes = [
  {
    path: 'account',
    component: AccountListComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'account/create',
    component: AccountCreateComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'account/transfer_ownership',
    component: TransferOwnershipListComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'account/pending',
    component: PendingListComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'account/pending/:id',
    component: PendingDetailComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'account/:id',
    component: AccountDetailComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'tenant',
    component: TenantListComponent,
    canActivate: [(CanActivateAdmin && CanActivateTenantInstance)]
  },
  {
    path: 'tenant/create',
    component: TenantCreateComponent,
    canActivate: [(CanActivateAdmin && CanActivateTenantInstance)]
  },
  {
    path: 'tenant/:id',
    component: UpdateTenantComponent,
    canActivate: [(CanActivateAdmin && CanActivateTenantInstance)]
  },
  {
    path: 'tenant/bis-ctx/:id',
    component: TenantBusinessCtxDetailComponent,
    canActivate: [(CanActivateAdmin && CanActivateTenantInstance)]
  },
  {
    path: 'tenant/users/:id',
    component: TenantUserDetailComponent,
    canActivate: [(CanActivateAdmin && CanActivateTenantInstance)]
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule,
    SearchBarModule,
    ColumnSelectorModule
  ],
  declarations: [
    AccountListComponent,
    AccountDetailComponent,
    AccountCreateComponent,
    PendingDetailComponent,
    PendingListComponent,
    AccountListDialogComponent,
    TransferOwnershipListComponent,
    TenantListComponent,
    TenantBusinessCtxDetailComponent,
    TenantUserDetailComponent,
    TenantCreateComponent,
    UpdateTenantComponent
  ],
  providers: [
    AccountListService,
    PendingListService,
    CanActivateUser,
    CanActivateDeveloper,
    CanActivateAdmin,
    CanActivateTenantInstance,
    TenantListService
  ]
})
export class AccountManagementModule {
}
