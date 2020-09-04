import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PendingAccount, PendingListRequest} from './pending-list';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class PendingListService{

  constructor(private http: HttpClient) {
  }

  getPendingList(request: PendingListRequest): Observable<PageResponse<PendingAccount>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    if (request.filters.preferredUsername) {
      params = params.set('preferredUsername', request.filters.preferredUsername);
    }
    if (request.filters.email) {
      params = params.set('email', request.filters.email);
    }
    if (request.filters.providerName) {
      params = params.set('providerName', request.filters.providerName);
    }
    if (request.createdDate.start) {
      params = params.set('createStart', '' + request.createdDate.start.getTime());
    }
    if (request.createdDate.end) {
      params = params.set('createEnd', '' + request.createdDate.end.getTime());
    }

    return this.http.get<PageResponse<PendingAccount>>('/api/pending_list', {params});
  }

  getPending(appOauth2UserId: number): Observable<PendingAccount> {
    return this.http.get<PendingAccount>('/api/pending/' + appOauth2UserId);
  }

  update(pending: PendingAccount): Observable<PendingAccount> {
    return this.http.post<PendingAccount>('/api/pending/' + pending.appOauth2UserId, {
      appOauth2UserId : pending.appOauth2UserId,
      rejected : pending.rejected,
    });
  }

}
