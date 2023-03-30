import {NgModule} from '@angular/core';
import {
  ArraySortPipe,
  DateAgoPipe,
  HighlightSearch,
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
import {MultiActionsSnackBarModule} from "./multi-actions-snack-bar/multi-actions-snack-bar.module";

@NgModule({
  declarations: [
    UnboundedPipe,
    HighlightSearch,
    DateAgoPipe,
    UndefinedPipe,
    SeparatePipe,
    ArraySortPipe,
    TruncatePipe,
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
    ArraySortPipe,
    TruncatePipe,
  ]
})
export class ScoreCommonModule {
}
