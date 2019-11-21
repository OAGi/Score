import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {ContextCategoryService} from './domain/context-category.service';
import {
  ContextCategoryListComponent,
  DialogDiscardContextCategoryDialogComponent
} from './context-category-list/context-category-list.component';
import {ContextCategoryCreateComponent} from './context-category-create/context-category-create.component';
import {
  ContextCategoryDetailComponent,
  DialogContentContextCategoryDialogDetailComponent,
  DialogDiscardContextCategoryDialogDetailComponent
} from './context-category-detail/context-category-detail.component';
import {AuthService} from '../../authentication/auth.service';
import {MatDialogModule} from '@angular/material';
import {DialogContentContextSchemeDialogDetailComponent} from '../context-scheme/context-scheme-detail/context-scheme-detail.component';
import {DialogDiscardBieDialogComponent} from '../../bie-management/bie-list/bie-list.component';
import {DialogDiscardCodeListDialogComponent} from '../../code-list-management/code-list-list/code-list-list.component';

const routes: Routes = [
  {
    path: 'context_management',
    children: [
      {
        path: 'context_category',
        component: ContextCategoryListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'context_category/create',
        component: ContextCategoryCreateComponent,
        canActivate: [AuthService]
      },
      {
        path: 'context_category/:id',
        component: ContextCategoryDetailComponent,
        canActivate: [AuthService]
      }
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    MaterialModule,
    MatDialogModule,
    CommonModule
  ],
  declarations: [
    ContextCategoryListComponent,
    ContextCategoryCreateComponent,
    ContextCategoryDetailComponent,
    DialogContentContextCategoryDialogDetailComponent,
    DialogDiscardContextCategoryDialogDetailComponent,
    DialogDiscardBieDialogComponent,
    DialogDiscardContextCategoryDialogComponent,
    DialogContentContextSchemeDialogDetailComponent,
    DialogDiscardCodeListDialogComponent
  ],
  entryComponents: [
    DialogContentContextCategoryDialogDetailComponent,
    DialogDiscardContextCategoryDialogComponent,
    DialogDiscardContextCategoryDialogDetailComponent,
    DialogDiscardBieDialogComponent,
    DialogDiscardCodeListDialogComponent,
    DialogContentContextSchemeDialogDetailComponent
  ],
  providers: [
    ContextCategoryService,
  ]
})
export class ContextCategoryModule {
}
