import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CodeListListComponent} from './code-list-list/code-list-list.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {CodeListService} from './domain/code-list.service';
import {CodeListValueDialogComponent} from './code-list-value-dialog/code-list-value-dialog.component';
import {CodeListDetailComponent} from './code-list-detail/code-list-detail.component';
import {CodeListForDerivingComponent} from './code-list-for-deriving/code-list-for-deriving.component';
import {ConfirmDialogModule} from '../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {CodeListUpliftComponent} from './code-list-uplift/code-list-uplift.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

const routes: Routes = [
  {
    path: 'code_list',
    children: [
      {
        path: '',
        component: CodeListListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        children: [
          {
            path: 'from_another',
            component: CodeListForDerivingComponent,
            canActivate: [AuthService],
          }
        ],
      },
      {
        path: 'uplift',
        component: CodeListUpliftComponent,
        canActivate: [AuthService],
      },
      {
        path: ':manifestId',
        component: CodeListDetailComponent,
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
        ConfirmDialogModule,
        CommonModule,
        ScoreCommonModule,
        FontAwesomeModule
    ],
  declarations: [
    CodeListListComponent,
    CodeListDetailComponent,
    CodeListForDerivingComponent,
    CodeListValueDialogComponent,
    CodeListUpliftComponent
  ],
  entryComponents: [
    CodeListValueDialogComponent
  ],
  providers: [
    CodeListService
  ]
})
export class CodeListModule {
}
