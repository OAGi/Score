import {NgModule} from '@angular/core';
import {
  ArraySortPipe,
  DateAgoPipe,
  HighlightSearch,
  JoinPipe,
  PastTensePipe,
  ReplaceAllPipe,
  SeparatePipe,
  TruncatePipe,
  UnboundedPipe,
  UndefinedPipe
} from './utility';
import {MatDialogModule} from '@angular/material/dialog';
import {MatCardModule} from '@angular/material/card';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {ConfirmDialogModule} from './confirm-dialog/confirm-dialog.module';
import {MultiActionsSnackBarModule} from './multi-actions-snack-bar/multi-actions-snack-bar.module';
import {ScoreTableColumnResizeDirective} from './score-table-column-resize/score-table-column-resize.directive';

@NgModule({
  declarations: [
    UnboundedPipe,
    HighlightSearch,
    DateAgoPipe,
    UndefinedPipe,
    SeparatePipe,
    JoinPipe,
    ArraySortPipe,
    TruncatePipe,
    PastTensePipe,
    ReplaceAllPipe,
    ScoreTableColumnResizeDirective
  ],
  imports: [
    MatDialogModule,
    MatCardModule,
    CommonModule,
    FormsModule,
    MatButtonModule,
    ConfirmDialogModule,
    MultiActionsSnackBarModule
  ],
  exports: [
    UnboundedPipe,
    HighlightSearch,
    DateAgoPipe,
    UndefinedPipe,
    SeparatePipe,
    JoinPipe,
    ArraySortPipe,
    TruncatePipe,
    PastTensePipe,
    ReplaceAllPipe,
    ScoreTableColumnResizeDirective
  ]
})
export class ScoreCommonModule {
}
