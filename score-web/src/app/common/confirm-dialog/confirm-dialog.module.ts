import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MaterialModule} from '../../material.module';

import {CommonModule} from '@angular/common';
import {ConfirmDialogComponent} from './confirm-dialog.component';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    MatDialogModule,
    CommonModule
  ],
  declarations: [
    ConfirmDialogComponent
  ],
  entryComponents: [
    ConfirmDialogComponent
  ],
})
export class ConfirmDialogModule {
}
