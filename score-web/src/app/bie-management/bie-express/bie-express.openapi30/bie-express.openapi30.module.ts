import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BieExpressOpenapi30Component} from './bie-express.openapi30.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../../../authentication/auth.service';
import {MaterialModule} from '../../../material.module';
import {TranslateModule} from '@ngx-translate/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ScoreCommonModule} from '../../../common/score-common.module';
import {OasDocCreateComponent} from './oas-doc-create/oas-doc-create.component';
import {OasDocListComponent} from './oas-doc-list/oas-doc-list.component';
import {OasDocDetailComponent} from './oas-doc-detail/oas-doc-detail.component';

const routes: Routes = [
  {
    path: 'profile_bie/express/openapi30',
    children: [
      {
        path: 'oas_doc',
        component: OasDocListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'oas_doc/create',
        component: OasDocCreateComponent,
        canActivate: [AuthService]
      },
      {
        path: 'oas_doc/:id',
        component: OasDocDetailComponent,
        canActivate: [AuthService]
      },
    ]
  }
];

@NgModule({
  declarations: [
    BieExpressOpenapi30Component,
    OasDocCreateComponent,
    OasDocListComponent,
    OasDocDetailComponent
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
    OasDocCreateComponent,
    OasDocListComponent,
    OasDocDetailComponent,
    RouterModule,
  ]
})
export class BieExpressOpenapi30Module { }
