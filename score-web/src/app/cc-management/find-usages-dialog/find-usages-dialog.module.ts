import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {MatDialogModule} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {FindUsagesDialogComponent} from './find-usages-dialog.component';
import {FindUsagesDialogService} from './domain/find-usages-dialog.service';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    MatDialogModule,
    CommonModule
  ],
  declarations: [
    FindUsagesDialogComponent,
  ],
  entryComponents: [
    FindUsagesDialogComponent
  ],
  providers: [
    FindUsagesDialogService,
  ]
})
export class FindUsagesDialogModule {
}
