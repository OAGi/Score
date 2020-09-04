import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AccountListDialogComponent} from './account-list-dialog/account-list-dialog.component';
import {PendingListService} from './domain/pending-list.service';
import {PendingDetailComponent} from './pending-detail/pending-detail.component';
import {PendingListComponent} from './pending-list/pending-list.component';
import {SettingsComponent} from './settings/settings.component';
import {CanActivateDeveloper, CanActivateUser} from '../authentication/auth.service';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {ContextMenuModule} from 'ngx-contextmenu';
import {SettingsService} from './settings/domain/settings.service';
import {AccountListComponent} from './account-list/account-list.component';
import {AccountListService} from './domain/account-list.service';
import {AccountDetailComponent} from './account-detail/account-detail.component';
import {AccountCreateComponent} from './account-create/account-create.component';


const routes: Routes = [
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [CanActivateUser]
  },
  {
    path: 'account',
    component: AccountListComponent,
    canActivate: [CanActivateDeveloper]
  },
  {
    path: 'account/create',
    component: AccountCreateComponent,
    canActivate: [CanActivateDeveloper]
  },
  {
    path: 'account/pending',
    component: PendingListComponent,
    canActivate: [CanActivateDeveloper]
  },
  {
    path: 'account/pending/:id',
    component: PendingDetailComponent,
    canActivate: [CanActivateDeveloper]
  },
  {
    path: 'account/:id',
    component: AccountDetailComponent,
    canActivate: [CanActivateDeveloper]
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    CommonModule
  ],
  declarations: [
    SettingsComponent,
    AccountListComponent,
    AccountDetailComponent,
    AccountCreateComponent,
    PendingDetailComponent,
    PendingListComponent,
    AccountListDialogComponent
  ],
  providers: [
    SettingsService,
    AccountListService,
    PendingListService,
    CanActivateUser,
    CanActivateDeveloper,
  ]
})
export class AccountManagementModule {
}
