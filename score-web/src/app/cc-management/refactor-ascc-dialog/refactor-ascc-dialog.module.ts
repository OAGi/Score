import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {MatDialogModule} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {RefactorAsccDialogService} from './domain/refactor-ascc-dialog.service';
import {RefactorAsccDialogComponent} from './refactor-ascc-dialog.component';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    MatDialogModule,
    CommonModule
  ],
  declarations: [
    RefactorAsccDialogComponent,
  ],
  entryComponents: [
    RefactorAsccDialogComponent
  ],
  providers: [
    RefactorAsccDialogService,
  ]
})
export class RefactorAsccDialogModule {
}
