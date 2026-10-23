import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MessageService} from './domain/message.service';
import {MessageListComponent} from './message-list/message-list.component';
import {MessageViewComponent} from './message-view/message-view.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {ConfirmDialogModule} from '../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {MarkdownModule} from 'ngx-markdown';
import {SearchBarModule} from '../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';
import {SCORE_TABLE} from '../common/score-table';

const routes: Routes = [
  {
    path: 'message',
    children: [
      {
        path: '',
        component: MessageListComponent,
        canActivate: [AuthService],
        title: 'Message'
      },
      {
        path: ':messageId',
        component: MessageViewComponent,
        canActivate: [AuthService],
        title: 'Message'
      }
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    ConfirmDialogModule,
    CommonModule,
    ScoreCommonModule,
    MarkdownModule,
    SearchBarModule,
    ColumnSelectorModule,
    ...SCORE_TABLE
  ],
  declarations: [
    MessageViewComponent,
    MessageListComponent
  ],
  providers: [
    MessageService
  ]
})
export class MessageManagementModule {
}
