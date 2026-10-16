import {ChangeDetectionStrategy, Component, inject, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MessageListEntry} from '../domain/messageDetails';
import {MessageService} from '../domain/message.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

const MESSAGE_TABLE = defineTable({
  storageKey: 'TableColumns-MessagePage',
  columns: [
    {id: 'subject', label: 'Subject', width: 0, sortable: true},
    {id: 'timestamp', label: 'Created On', header: 'Created on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'timestamp', direction: 'desc'},
  search: {key: 'subject', placeholder: 'Search by Subject'},
  filters: [
    // The URL has always called the senders `senderUsernameList`, the endpoint `senderLoginIds`.
    {type: 'updater', key: 'senderUsernameList', httpParam: 'senderLoginIds', label: 'Sender'},
    {type: 'dateRange', key: 'createdDate', label: 'Created', httpParam: 'createdOn'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-message-list',
  templateUrl: './message-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./message-list.component.css']
})
export class MessageListComponent {
  private service = inject(MessageService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);

  readonly columns = createTableColumns(MESSAGE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<MessageListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.service.getMessageList(params);
  readonly rowKey = (row: MessageListEntry) => row.messageId;

  discard() {
    const messageIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (messageIds.length > 1 ? 'Messages' : 'Message') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' + (messageIds.length > 1 ? 'messages' : 'message') + '?',
      'The ' + (messageIds.length > 1 ? 'messages' : 'message') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...messageIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
          });
        }
      });
  }
}
