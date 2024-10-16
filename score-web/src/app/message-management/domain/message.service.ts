import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CountOfUnreadMessages, Message, MessageList, MessageListRequest} from './message';
import {map} from 'rxjs/operators';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class MessageService {

  constructor(private http: HttpClient) {
  }

  getCountOfUnreadMessages(): Observable<number> {
    return this.http.get<CountOfUnreadMessages>('/api/message/count_of_unread')
      .pipe(map((resp: CountOfUnreadMessages) => {
        return resp.countOfUnreadMessages;
      }));
  }

  getMessage(messageId: number): Observable<Message> {
    return this.http.get<Message>('/api/message/' + messageId)
      .pipe(map((resp: Message) => {
        resp.timestamp = new Date(resp.timestamp);
        return resp;
      }));
  }

  discard(messageId: number): Observable<any> {
    return this.http.delete('/api/message/' + messageId);
  }

  getMessageList(request: MessageListRequest): Observable<PageResponse<MessageList>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.subject) {
      params = params.set('subject', request.filters.subject);
    }
    if (request.senderUsernameList.length > 0) {
      params = params.set('senderUsernameList', request.senderUsernameList.join(','));
    }
    if (request.createdDate.start) {
      params = params.set('createStart', '' + request.createdDate.start.getTime());
    }
    if (request.createdDate.end) {
      params = params.set('createEnd', '' + request.createdDate.end.getTime());
    }

    return this.http.get<PageResponse<MessageList>>('/api/message_list', {params});
  }

  delete(...messageIdList): Observable<any> {
    if (messageIdList.length === 1) {
      return this.http.delete('/api/message/' + messageIdList[0]);
    } else {
      return this.http.post<any>('/api/message/delete', {
        messageIdList
      });
    }
  }
}
