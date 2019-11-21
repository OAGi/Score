import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ExtensionDetailComponent} from './extension-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material';
import {ContextMenuModule} from 'ngx-contextmenu';
import {ExtensionDetailService} from './domain/extension-detail.service';
import {AppendAsccpDialogComponent} from './append-asccp-dialog/append-asccp-dialog.component';
import {AppendBccpDialogComponent} from './append-bccp-dialog/append-bccp-dialog.component';
import {ConfirmDialogComponent} from './confirm-dialog/confirm-dialog.component';
import {GrowlModule} from 'ngx-growl';
import {TranslateModule} from '@ngx-translate/core';
import {SrtCommonModule} from '../../common/srt-common.module';

const routes: Routes = [
  {
    path: 'core_component/extension',
    children: [
      {
        path: ':releaseId/:extensionId',
        component: ExtensionDetailComponent,
        canActivate: [AuthService],
      }
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    HotkeyModule,
    MatInputModule,
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    CommonModule,
    TranslateModule,
    GrowlModule,
    SrtCommonModule,
  ],
  declarations: [
    ExtensionDetailComponent,
    AppendAsccpDialogComponent,
    AppendBccpDialogComponent,
    ConfirmDialogComponent,
  ],
  entryComponents: [
    AppendAsccpDialogComponent,
    AppendBccpDialogComponent,
    ConfirmDialogComponent
  ],
  providers: [
    ExtensionDetailService
  ]
})
export class ExtensionDetailModule {
}
