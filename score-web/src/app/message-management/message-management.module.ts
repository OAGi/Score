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
import {ContextMenuModule} from 'ngx-contextmenu';
import {MarkdownModule} from 'ngx-markdown';

const routes: Routes = [
  {
    path: 'message',
    children: [
      {
        path: '',
        component: MessageListComponent,
        canActivate: [AuthService],
      },
      {
        path: ':messageId',
        component: MessageViewComponent,
        canActivate: [AuthService],
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
    ContextMenuModule,
    MarkdownModule
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
