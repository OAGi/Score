import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {BieExpressOpenapi30Component} from './bie-express.openapi30.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../../../authentication/auth.service';
import {MaterialModule} from '../../../material.module';
import {TranslateModule} from '@ngx-translate/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ScoreCommonModule} from '../../../common/score-common.module';

const routes: Routes = [
  {
    path: 'profile_bie/express/openapi30',
    component: BieExpressOpenapi30Component,
    canActivate: [AuthService],
  }
];


@NgModule({
  declarations: [
    BieExpressOpenapi30Component
  ],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    MaterialModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
    ScoreCommonModule
  ],
  exports: [
    BieExpressOpenapi30Component,
    RouterModule,
  ]
})
export class BieExpressOpenapi30Module { }
