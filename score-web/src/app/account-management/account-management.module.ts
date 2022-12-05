import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AccountListDialogComponent} from './account-list-dialog/account-list-dialog.component';
import {PendingListService} from './domain/pending-list.service';
import {PendingDetailComponent} from './pending-detail/pending-detail.component';
import {PendingListComponent} from './pending-list/pending-list.component';
import {SettingsComponent} from './settings/settings.component';
import {CanActivateAdmin, CanActivateDeveloper, CanActivateUser} from '../authentication/auth.service';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {SettingsService} from './settings/domain/settings.service';
import {AccountListComponent} from './account-list/account-list.component';
import {AccountListService} from './domain/account-list.service';
import {AccountDetailComponent} from './account-detail/account-detail.component';
import {AccountCreateComponent} from './account-create/account-create.component';
import {TenantListComponent} from './tenant-list/tenant-list.component';
import {TenantDetailComponent} from './tenant-detail/tenant-detail.component';
import {TenantListService} from './domain/tenant-list.service';
import {TenantUserDetailComponent} from './tenant-user-detail/tenant-user-detail.component';
import {TenantCreateComponent} from './tenant-create/tenant-create.component';


const routes: Routes = [
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [CanActivateUser]
  },
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
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'tenant/create',
    component: TenantCreateComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'tenant/:id',
    component: TenantDetailComponent,
    canActivate: [CanActivateAdmin]
  },
  {
    path: 'tenant/users/:id',
    component: TenantUserDetailComponent,
    canActivate: [CanActivateAdmin]
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule
  ],
  declarations: [
    SettingsComponent,
    AccountListComponent,
    AccountDetailComponent,
    AccountCreateComponent,
    PendingDetailComponent,
    PendingListComponent,
    AccountListDialogComponent,
    TenantListComponent, 
    TenantDetailComponent, 
    TenantUserDetailComponent, 
    TenantCreateComponent
  ],
  providers: [
    SettingsService,
    AccountListService,
    PendingListService,
    CanActivateUser,
    CanActivateDeveloper,
    CanActivateAdmin,
    TenantListService
  ]
})
export class AccountManagementModule {
}
