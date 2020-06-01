import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {ContextCategoryService} from './domain/context-category.service';
import {ContextCategoryListComponent} from './context-category-list/context-category-list.component';
import {ContextCategoryCreateComponent} from './context-category-create/context-category-create.component';
import {ContextCategoryDetailComponent} from './context-category-detail/context-category-detail.component';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogModule} from '../../common/confirm-dialog/confirm-dialog.module';

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
    ConfirmDialogModule,
    CommonModule
  ],
  declarations: [
    ContextCategoryListComponent,
    ContextCategoryCreateComponent,
    ContextCategoryDetailComponent
  ],
  providers: [
    ContextCategoryService,
  ]
})
export class ContextCategoryModule {
}
