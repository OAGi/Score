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
import {DetailPageComponent} from './detail-page/detail-page.component';
import {DetailCardComponent} from './detail-card/detail-card.component';
import {SidenavPageComponent} from './sidenav-page/sidenav-page.component';
import {MaterialModule} from '../material.module';
import {BasePageComponent} from './base-page/base-page.component';
import {ListPageComponent} from './list-page/list-page.component';
import {ScoreMatSelectComponent} from './score-mat-select/score-mat-select.component';

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
    SidenavPageComponent
  ],
  imports: [
    ScoreTableColumnResizeDirective,
    MaterialModule,
    BasePageComponent,
    DetailPageComponent,
    DetailCardComponent,
    ListPageComponent,
    ScoreMatSelectComponent,
    MatDialogModule,
    MatCardModule,
    CommonModule,
    FormsModule,
    MatButtonModule,
    ConfirmDialogModule,
    MultiActionsSnackBarModule
  ],
  exports: [
    SidenavPageComponent,
    BasePageComponent,
    DetailPageComponent,
    DetailCardComponent,
    ListPageComponent,
    ScoreMatSelectComponent,
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
