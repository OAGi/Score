import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';

import {ContextSchemeService} from '../context-scheme/domain/context-scheme.service';
import {BusinessContextService} from './domain/business-context.service';
import {
  BusinessContextListComponent,
  DialogContentBizContextListDiscardComponent
} from './business-context-list/business-context-list.component';
import {BusinessContextCreateComponent} from './business-context-create/business-context-create.component';
import {
  BusinessContextDetailComponent,
  DialogContentBizContextDialogDetailComponent,
  DialogContentBizContextDialogDiscardComponent
} from './business-context-detail/business-context-detail.component';
import {BusinessContextValueDialogComponent} from './business-context-value-dialog/business-context-value-dialog.component';
import {MatDialogModule} from '@angular/material';

const routes: Routes = [
  {
    path: 'context_management',
    children: [
      {
        path: 'business_context',
        component: BusinessContextListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'business_context/create',
        component: BusinessContextCreateComponent,
        canActivate: [AuthService]
      },
      {
        path: 'business_context/:id',
        component: BusinessContextDetailComponent,
        canActivate: [AuthService]
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
    CommonModule
  ],
  declarations: [
    BusinessContextListComponent,
    BusinessContextCreateComponent,
    BusinessContextDetailComponent,
    DialogContentBizContextDialogDetailComponent,
    DialogContentBizContextDialogDiscardComponent,
    DialogContentBizContextListDiscardComponent,
    BusinessContextValueDialogComponent,
  ],
  entryComponents: [
    BusinessContextValueDialogComponent,
    DialogContentBizContextDialogDiscardComponent,
    DialogContentBizContextListDiscardComponent,
    DialogContentBizContextDialogDetailComponent
  ],
  providers: [
    ContextSchemeService,
    BusinessContextService
  ]
})
export class BusinessContextModule {
}
