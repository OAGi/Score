import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {MaterialModule} from '../../material.module';
import {MatDialogModule} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {RefactorDialogService} from './domain/refactor-dialog.service';
import {RefactorDialogComponent} from './refactor-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {SCORE_TABLE} from '../../common/score-table';
import {CcCompactRowComponent} from '../cc-compact-row/cc-compact-row.component';
import {RecordCompactRowComponent} from '../../common/record-compact-row';

@NgModule({
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MaterialModule,
        MatDialogModule,
        CommonModule,
        RouterModule,
        ScoreCommonModule,
        CcCompactRowComponent,
        RecordCompactRowComponent,
        ...SCORE_TABLE
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
