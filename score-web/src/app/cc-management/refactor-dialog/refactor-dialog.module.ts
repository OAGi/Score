import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {MaterialModule} from '../../material.module';
import {MatDialogModule} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {RefactorDialogService} from './domain/refactor-dialog.service';
import {RefactorDialogComponent} from './refactor-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';

@NgModule({
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MaterialModule,
        MatDialogModule,
        CommonModule,
        RouterModule,
        ScoreCommonModule,
        ColumnSelectorModule
    ],
  declarations: [
    RefactorDialogComponent,
  ],
  providers: [
    RefactorDialogService,
  ]
})
export class RefactorDialogModule {
}
