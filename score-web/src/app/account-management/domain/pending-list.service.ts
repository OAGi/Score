import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PendingAccount} from './pending-list';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class PendingListService{
  private http = inject(HttpClient);


  getPendingList(params: HttpParams): Observable<PageResponse<PendingAccount>> {
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
