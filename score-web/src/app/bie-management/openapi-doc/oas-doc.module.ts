import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {OasDocCreateComponent} from './oas-doc-create/oas-doc-create.component';
import {OasDocListComponent} from './oas-doc-list/oas-doc-list.component';
import {OasDocDetailComponent} from './oas-doc-detail/oas-doc-detail.component';
import {OpenAPIService} from './domain/openapi.service';
import {OasDocBieListComponent} from './oas-doc-create/oas-doc-bie-list.component';
import {OasDocAssignDialogComponent} from './oas-doc-assign-dialog/oas-doc-assign-dialog.component';
import {MatMultiSortModule} from 'ngx-mat-multi-sort';
import {AuthService} from '../../authentication/auth.service';
import {MaterialModule} from '../../material.module';
import {ScoreCommonModule} from '../../common/score-common.module';
import {SearchBarModule} from '../../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';


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
        children: [{
          path: '',
          component: OasDocDetailComponent,
          canActivate: [AuthService],
        }, {
          path: 'assign',
          component: OasDocAssignDialogComponent,
          canActivate: [AuthService],
        },
          {
            path: 'bie_list',
            component: OasDocBieListComponent,
            canActivate: [AuthService],
          }]
      },
    ]
  }
];

@NgModule({
  declarations: [
    OasDocCreateComponent,
    OasDocListComponent,
    OasDocDetailComponent,
    OasDocBieListComponent,
    OasDocAssignDialogComponent
  ],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    MaterialModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
    ScoreCommonModule,
    MatMultiSortModule,
    SearchBarModule,
    ColumnSelectorModule
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
export class OasDocModule {
}
