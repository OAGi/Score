import {NgModule} from '@angular/core';
import {TransferOwnershipDialogComponent} from './transfer-ownership-dialog.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {MatDialogModule} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {SearchBarModule} from '../search-bar/search-bar.module';
import {ScoreCommonModule} from '../score-common.module';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    MatDialogModule,
    CommonModule,
    SearchBarModule,
    ScoreCommonModule
  ],
  declarations: [
    TransferOwnershipDialogComponent
  ],
})
export class TransferOwnershipDialogModule {
}
