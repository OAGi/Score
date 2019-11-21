import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BccpDetailComponent} from './bccp-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material';
import {BccpCreateComponent} from '../bccp-create/bccp-create.component';
import {TranslateModule} from '@ngx-translate/core';

const routes: Routes = [
  {
    path: 'core_component/bccp',
    children: [
      {
        path: ':releaseId/:bccpId',
        component: BccpDetailComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: BccpCreateComponent,
        canActivate: [AuthService]
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
    TranslateModule,
    CommonModule
  ],
  declarations: [
    BccpDetailComponent
  ]
})
export class BccpDetailModule {
}
