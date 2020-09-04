import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AsccpDetailComponent} from './asccp-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {MatInputModule} from '@angular/material/input';
import {AuthService} from '../../authentication/auth.service';
import {AsccpCreateComponent} from '../asccp-create/asccp-create.component';
import {TranslateModule} from '@ngx-translate/core';

const routes: Routes = [
  {
    path: 'core_component/asccp',
    children: [
      {
        path: ':releaseId/:asccpId',
        component: AsccpDetailComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: AsccpCreateComponent,
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
    MatInputModule,
    CommonModule,
    TranslateModule
  ],
  declarations: [
    AsccpDetailComponent
  ]
})
export class AsccpDetailModule {
}
