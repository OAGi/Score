import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AccDetailComponent} from './acc-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material';
import {AccCreateComponent} from '../acc-create/acc-create.component';
import {ContextMenuModule} from 'ngx-contextmenu';
import {TranslateModule} from '@ngx-translate/core';

const routes: Routes = [
  {
    path: 'core_component/acc',
    children: [
      {
        path: ':releaseId/:accId',
        component: AccDetailComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: AccCreateComponent,
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
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    CommonModule,
    TranslateModule
  ],
  declarations: [
    AccDetailComponent
  ]
})
export class AccDetailModule {
}
