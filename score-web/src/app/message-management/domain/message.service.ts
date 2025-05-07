import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {MessageDetails, MessageListEntry, MessageListRequest} from './messageDetails';
import {map} from 'rxjs/operators';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class MessageService {

  constructor(private http: HttpClient) {
  }

  getCountOfUnreadMessages(): Observable<number> {
    return this.http.get<number>('/api/messages/count-of-unread');
  }

  getMessage(messageId: number): Observable<MessageDetails> {
    return this.http.get<MessageDetails>('/api/messages/' + messageId)
        .pipe(map((resp: MessageDetails) => {
          resp.created.when = new Date(resp.created.when);
          return resp;
        }));
  }

  discard(messageId: number): Observable<any> {
    return this.http.delete('/api/messages/' + messageId);
  }

  getMessageList(request: MessageListRequest): Observable<PageResponse<MessageListEntry>> {
    let params = new HttpParams()
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.senderLoginIds.length > 0) {
      params = params.set('senderLoginIds', request.senderLoginIds.join(','));
    }
    if (!!request.createdDate.start || !!request.createdDate.end) {
      params = params.set('createdOn',
          '[' + (!!request.createdDate.start ? request.createdDate.start.getTime() : '') + '~' +
          (!!request.createdDate.end ? request.createdDate.end.getTime() : '') + ']');
    }

    return this.http.get<PageResponse<MessageListEntry>>('/api/messages', {params}).pipe(
        map((res: PageResponse<MessageListEntry>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            created: {
              ...elm.created,
              when: new Date(elm.created.when),
            }
          }))
        }))
    );
  }

  delete(...messageIdList): Observable<any> {
    if (messageIdList.length === 1) {
      return this.http.delete('/api/messages/' + messageIdList[0]);
    } else {
      return this.http.delete<any>('/api/messages', {
        body: {
          messageIdList
        }
      });
    }
  }
}
