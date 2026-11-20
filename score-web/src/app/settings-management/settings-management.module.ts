import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {CommonModule} from '@angular/common';
import {canActivateUser, ONLY_ADMINISTRATORS} from '../authentication/can-activate-user';
import {SettingsAccountComponent} from './settings-account/settings-account.component';
import {SettingsAccountService} from './settings-account/domain/settings-account.service';
import {SettingsMenuComponent} from './settings-menu/settings-menu.component';
import {SettingsPasskeysComponent} from './settings-passkeys/settings-passkeys.component';
import {SettingsApplicationSettingsComponent} from './settings-application-settings/settings-application-settings.component';
import {FilenameExpressionInputComponent} from './settings-application-settings/filename-expression-input.component';
import {SettingsApplicationSettingsService} from './settings-application-settings/domain/settings-application-settings.service';
import {MarkdownModule} from 'ngx-markdown';
import {EmailValidationComponent} from './email-validation/email-validation.component';
import {SettingsPreferencesComponent} from './settings-preferences/settings-preferences.component';
import {SettingsPreferencesService} from './settings-preferences/domain/settings-preferences.service';

const signedIn = canActivateUser();
/** A user who may not change the application settings goes to the first page of the account settings. */
const administrator = canActivateUser(ONLY_ADMINISTRATORS, '/settings/account/personal-info');

const routes: Routes = [
  {
    path: 'settings/account',
    pathMatch: 'full',
    redirectTo: 'settings/account/personal-info'
  },
  {
    path: 'settings/account/personal-info',
    component: SettingsAccountComponent,
    data: {section: 'personal-info'},
    canActivate: [signedIn],
    title: 'Personal Info'
  },
  {
    path: 'settings/account/password',
    component: SettingsAccountComponent,
    data: {section: 'password'},
    canActivate: [signedIn],
    title: 'Change password'
  },
  {
    path: 'settings/account/passkeys',
    component: SettingsAccountComponent,
    data: {section: 'passkeys'},
    canActivate: [signedIn],
    title: 'Passkeys'
  },
  {
    path: 'settings/email_validation',
    component: EmailValidationComponent,
    canActivate: [signedIn],
    title: 'Email Validation',
  },
  {
    path: 'settings/preferences',
    component: SettingsPreferencesComponent,
    canActivate: [signedIn],
    title: 'Preferences'
  },
  {
    path: 'settings/application_settings',
    pathMatch: 'full',
    redirectTo: 'settings/application_settings/features'
  },
  {
    path: 'settings/application_settings/features',
    component: SettingsApplicationSettingsComponent,
    data: {section: 'features'},
    canActivate: [administrator],
    title: 'Application Features'
  },
  {
    path: 'settings/application_settings/smtp',
    component: SettingsApplicationSettingsComponent,
    data: {section: 'smtp'},
    canActivate: [administrator],
    title: 'SMTP Settings'
  },
  {
    path: 'settings/application_settings/filename-expressions',
    component: SettingsApplicationSettingsComponent,
    data: {section: 'filename-expressions'},
    canActivate: [administrator],
    title: 'Filename Expressions'
  },
  {
    path: 'settings/application_settings/web-page',
    component: SettingsApplicationSettingsComponent,
    data: {section: 'web-page'},
    canActivate: [administrator],
    title: 'Web Page Settings'
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule,
    MarkdownModule,
    ScoreCommonModule,
    FilenameExpressionInputComponent
  ],
  declarations: [
    SettingsMenuComponent,
    SettingsAccountComponent,
    SettingsPasskeysComponent,
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
