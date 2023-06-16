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
import {OpenAPIService} from './domain/openapi.service';
import {OasDocBieListComponent} from './oas-doc-create/oas-doc-bie-list.component';

const routes: Routes = [
  {
    path: 'profile_bie/express/oas_doc',
    children: [
      {
        path: '',
        component: OasDocListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'create',
        children: [
          {
            path: '',
            component: OasDocCreateComponent,
            canActivate: [AuthService]
          },
          {
            path: 'bie_list',
            component: OasDocBieListComponent,
            canActivate: [AuthService]
          }
        ]
      },
      {
        path: ':id',
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
    OasDocDetailComponent,
    OasDocBieListComponent
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
  ],
  providers: [
    OpenAPIService
  ]
})
export class BieExpressOpenapi30Module {
}
