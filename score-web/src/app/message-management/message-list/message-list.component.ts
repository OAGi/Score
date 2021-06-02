import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {PageRequest} from '../../basis/basis';
import {initFilter} from '../../common/utility';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {finalize} from 'rxjs/operators';
import {MessageList, MessageListRequest} from '../domain/message';
import {MessageService} from '../domain/message.service';

@Component({
  selector: 'score-message-list',
  templateUrl: './message-list.component.html',
  styleUrls: ['./message-list.component.css']
})
export class MessageListComponent implements OnInit {

  title = 'Message';
  displayedColumns: string[] = [
    'select', 'sender', 'subject', 'timestamp'
  ];
  dataSource = new MatTableDataSource<MessageList>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  senderIdListFilterCtrl: FormControl = new FormControl();
  filteredSenderIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: MessageListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: MessageService,
              private accountService: AccountListService,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new MessageListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('timestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.senderIdListFilterCtrl, this.filteredSenderIdList, this.loginIdList);
    });

    this.loadMessageList(true);
  }

  onPageChange(event: PageEvent) {
    this.loadMessageList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadMessageList();
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'startDate':
        this.request.createdDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.request.createdDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.request.createdDate.start = null;
        break;
      case 'endDate':
        this.request.createdDate.end = null;
        break;
    }
  }

  loadMessageList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getMessageList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: MessageList) => {
        elm.timestamp = new Date(elm.timestamp);
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: MessageList) {
    this.selection.select(row.messageId);
  }

  toggle(row: MessageList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.messageId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: MessageList) {
    return this.selection.isSelected(row.messageId);
  }

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
            this.loadMessageList();
          });
        }
      });
  }

}
