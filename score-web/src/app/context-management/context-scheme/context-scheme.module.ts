import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {MaterialModule} from '../../material.module';

import {AuthService} from '../../authentication/auth.service';
import {ContextSchemeService} from './domain/context-scheme.service';
import {
  ContextSchemeListComponent,
  DialogContentContextSchemeListDiscardComponent
} from './context-scheme-list/context-scheme-list.component';
import {ContextSchemeCreateComponent} from './context-scheme-create/context-scheme-create.component';
import {
  ContextSchemeDetailComponent,
  DialogContentContextSchemeDiscardComponent,
  DialogContentContextSchemeUpdateCreateDialogDetailComponent,
  DialogContentContextSchemeUpdateDialogDetailComponent
} from './context-scheme-detail/context-scheme-detail.component';
import {ContextSchemeValueDialogComponent} from './context-scheme-value-dialog/context-scheme-value-dialog.component';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {GrowlModule} from 'ngx-growl';

const routes: Routes = [
  {
    path: 'context_management',
    children: [
      {
        path: 'context_scheme',
        component: ContextSchemeListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'context_scheme/create',
        component: ContextSchemeCreateComponent,
        canActivate: [AuthService]
      },
      {
        path: 'context_scheme/:id',
        component: ContextSchemeDetailComponent,
        canActivate: [AuthService]
      },
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    GrowlModule.forRoot({maxMessages: 2, displayTimeMs: 5000}),
    CommonModule,
  ],
  declarations: [
    ContextSchemeListComponent,
    ContextSchemeCreateComponent,
    ContextSchemeDetailComponent,
    ContextSchemeValueDialogComponent,
    DialogContentContextSchemeUpdateDialogDetailComponent,
    DialogContentContextSchemeDiscardComponent,
    DialogContentContextSchemeListDiscardComponent,
    DialogContentContextSchemeUpdateCreateDialogDetailComponent
  ],
  entryComponents: [
    ContextSchemeValueDialogComponent,
    DialogContentContextSchemeUpdateDialogDetailComponent,
    DialogContentContextSchemeListDiscardComponent,
    DialogContentContextSchemeDiscardComponent,
    DialogContentContextSchemeUpdateCreateDialogDetailComponent
  ],
  providers: [
    ContextSchemeService,
    {provide: MatDialogRef, useValue: {}},
    {provide: MAT_DIALOG_DATA, useValue: []},
  ]
})
export class ContextSchemeModule {
}
