import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {CommonModule} from '@angular/common';
import {CanActivateUser} from '../authentication/auth.service';
import {SettingsAccountComponent} from './settings-account/settings-account.component';
import {SettingsAccountService} from './settings-account/domain/settings-account.service';
import {SettingsMenuComponent} from './settings-menu/settings-menu.component';
import {SettingsApplicationSettingsComponent} from './settings-application-settings/settings-application-settings.component';
import {SettingsApplicationSettingsService} from './settings-application-settings/domain/settings-application-settings.service';
import {MarkdownModule} from 'ngx-markdown';
import {EmailValidationComponent} from './email-validation/email-validation.component';
import {SettingsPreferencesComponent} from './settings-preferences/settings-preferences.component';
import {SettingsPreferencesService} from './settings-preferences/domain/settings-preferences.service';

const routes: Routes = [
  {
    path: 'settings/account',
    component: SettingsAccountComponent,
    canActivate: [CanActivateUser],
    title: 'Settings'
  },
  {
    path: 'settings/email_validation',
    component: EmailValidationComponent,
    canActivate: [CanActivateUser],
    title: 'Email Validation',
  },
  {
    path: 'settings/preferences',
    component: SettingsPreferencesComponent,
    canActivate: [CanActivateUser],
    title: 'Preferences'
  },
  {
    path: 'settings/application_settings',
    component: SettingsApplicationSettingsComponent,
    canActivate: [CanActivateUser],
    title: 'Application Settings'
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule,
    MarkdownModule
  ],
  declarations: [
    SettingsMenuComponent,
    SettingsAccountComponent,
    SettingsPreferencesComponent,
    SettingsApplicationSettingsComponent,
    EmailValidationComponent
  ],
  providers: [
    SettingsAccountService,
    SettingsPreferencesService,
    SettingsApplicationSettingsService
  ]
})
export class SettingsManagementModule {
}
