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
import {CodeListCreateComponent} from './code-list-create/code-list-create.component';
import {CodeListForCreatingComponent} from './code-list-for-creating/code-list-for-creating.component';
import {ConfirmDialogModule} from '../common/confirm-dialog/confirm-dialog.module';

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
            path: '',
            component: CodeListCreateComponent,
            canActivate: [AuthService],
          },
          {
            path: 'from_another',
            component: CodeListForCreatingComponent,
            canActivate: [AuthService],
          },
          {
            path: ':basedCodeListId',
            component: CodeListCreateComponent,
            canActivate: [AuthService],
          },
        ],
      },
      {
        path: ':id',
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
    CommonModule
  ],
  declarations: [
    CodeListListComponent,
    CodeListCreateComponent,
    CodeListDetailComponent,
    CodeListForCreatingComponent,
    CodeListValueDialogComponent
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
