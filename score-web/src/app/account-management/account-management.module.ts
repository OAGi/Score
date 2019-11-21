import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SettingsComponent} from './settings/settings.component';
import {CanActivateDeveloper, CanActivateUser} from '../authentication/auth.service';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {ContextMenuModule} from 'ngx-contextmenu';
import {SettingsService} from './settings/domain/settings.service';
import {AccountListComponent} from './account-list/account-list.component';
import {AccountListService} from './domain/account-list.service';
import {AccountDetailComponent} from './account-detail/account-detail.component';
import {AccountCreateComponent} from './account-create/account-create.component';
import {GrowlModule} from 'ngx-growl';


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
    HotkeyModule,
    GrowlModule.forRoot({maxMessages: 1, displayTimeMs: 5000}),
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    CommonModule
  ],
  declarations: [
    SettingsComponent,
    AccountListComponent,
    AccountDetailComponent,
    AccountCreateComponent
  ],
  providers: [
    SettingsService,
    AccountListService,
    CanActivateUser,
    CanActivateDeveloper,
  ]
})
export class AccountManagementModule {
}
