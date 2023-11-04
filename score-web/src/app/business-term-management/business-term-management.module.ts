import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ScoreCommonModule} from '../common/score-common.module';
import {MaterialModule} from '../material.module';
import {AuthService} from '../authentication/auth.service';
import {BusinessTermService} from './domain/business-term.service';
import {BusinessTermListComponent} from './business-term-list/business-term-list.component';
import {BusinessTermCreateComponent} from './business-term-create/business-term-create.component';
import {ConfirmDialogModule} from '../common/confirm-dialog/confirm-dialog.module';
import {BusinessTermDetailComponent} from './business-term-detail/business-term-detail.component';
import {AssignedBusinessTermListComponent} from './assigned-business-term-list/assigned-business-term-list.component';
import {AssignBusinessTermBtComponent} from './assign-business-term/assign-business-term-bt.component';
import {AssignBusinessTermBieComponent} from './assign-business-term/assign-business-term-bie.component';
import {
  AssignedBusinessTermDetailComponent
} from './assigned-business-term-detail/assigned-business-term-detail.component';
import {BusinessTermUploadFileComponent} from './business-term-upload-file/business-term-upload-file.component';

const routes: Routes = [
  {
    path: 'business_term_management',
    children: [
      {
        path: 'business_term',
        component: BusinessTermListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'business_term/create',
        component: BusinessTermCreateComponent,
        canActivate: [AuthService]
      },
      {
        path: 'business_term/upload',
        component: BusinessTermUploadFileComponent,
        canActivate: [AuthService]
      },
      {
        path: 'business_term/:id',
        component: BusinessTermDetailComponent,
        canActivate: [AuthService]
      },
      {
        path: 'assign_business_term',
        children: [
          {
            path: '',
            component: AssignedBusinessTermListComponent,
            canActivate: [AuthService]
          },
          {
            path: 'details/:id',
            component: AssignedBusinessTermDetailComponent,
            canActivate: [AuthService]
          },
          {
            path: 'create',
            children: [
              {
                path: '',
                component: AssignBusinessTermBieComponent,
                canActivate: [AuthService]
              },
              {
                path: 'bt',
                component: AssignBusinessTermBtComponent,
                canActivate: [AuthService]
              }
            ]
          }
        ]
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
    ConfirmDialogModule,
    CommonModule,
    ScoreCommonModule
  ],
  declarations: [
    BusinessTermListComponent,
    BusinessTermCreateComponent,
    BusinessTermDetailComponent,
    BusinessTermUploadFileComponent,
    AssignedBusinessTermListComponent,
    AssignBusinessTermBtComponent,
    AssignBusinessTermBieComponent,
    AssignedBusinessTermDetailComponent
  ],
  entryComponents: [
    BusinessTermListComponent,
    AssignedBusinessTermListComponent,
    AssignBusinessTermBtComponent,
    AssignedBusinessTermDetailComponent
  ],
  providers: [
    BusinessTermService
  ]
})
export class BusinessTermManagementModule {
}
