import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {MatDialogModule} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {SearchOptionsDialogComponent} from './search-options-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {SearchOptionsService} from './domain/search-options-service';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    MatDialogModule,
    CommonModule,
    ScoreCommonModule
  ],
  declarations: [
    SearchOptionsDialogComponent,
  ],
  providers: [
    SearchOptionsService
  ]
})
export class SearchOptionsDialogModule {
}
