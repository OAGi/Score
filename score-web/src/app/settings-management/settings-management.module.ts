import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {CommonModule} from '@angular/common';
import {CanActivateUser} from '../authentication/auth.service';
import {SettingsPasswordComponent} from './settings-password/settings-password.component';
import {SettingsPasswordService} from './settings-password/domain/settings-password.service';
import {SettingsMenuComponent} from './settings-menu/settings-menu.component';
import {SettingsApplicationSettingsComponent} from './settings-application-settings/settings-application-settings.component';
import {SettingsApplicationSettingsService} from './settings-application-settings/domain/settings-application-settings.service';

const routes: Routes = [
  {
    path: 'settings/password',
    component: SettingsPasswordComponent,
    canActivate: [CanActivateUser]
  },
  {
    path: 'settings/application_settings',
    component: SettingsApplicationSettingsComponent,
    canActivate: [CanActivateUser]
  }
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
    SettingsMenuComponent,
    SettingsPasswordComponent,
    SettingsApplicationSettingsComponent
  ],
  providers: [
    SettingsPasswordService,
    SettingsApplicationSettingsService
  ]
})
export class SettingsManagementModule {
}
