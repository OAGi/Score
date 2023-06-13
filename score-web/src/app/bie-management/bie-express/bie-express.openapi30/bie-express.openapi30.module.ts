import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {BieExpressOpenapi30Component} from './bie-express.openapi30.component';
import {Routes} from '@angular/router';
import {AuthService} from '../../../authentication/auth.service';

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
    CommonModule
  ],
  exports: [
    BieExpressOpenapi30Component
  ]
})
export class BieExpressOpenapi30Module { }
