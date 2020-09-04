import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AccountList, AccountListRequest} from './accounts';
import {PageResponse} from '../../basis/basis';
import {PendingAccount} from './pending-list';

@Injectable()
export class AccountListService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getAccountsList(request: AccountListRequest, excludeRequester?: boolean): Observable<PageResponse<AccountList>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.loginId) {
      params = params.set('loginId', request.filters.loginId);
    }
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.filters.organization) {
      params = params.set('organization', request.filters.organization);
    }
    if (request.filters.role) {
      params = params.set('role', request.filters.role);
    }
    if (request.filters.excludeSSO) {
      params = params.set('excludeSSO', 'true');
    }
    if (excludeRequester) {
      params = params.set('excludeRequester', 'true');
    }

    return this.http.get<PageResponse<AccountList>>('/api/accounts_list', {params: params});
  }

  getAccount(appUserId: number): Observable<AccountList> {
    return this.http.get<AccountList>('/api/account/' + appUserId);
  }

  getAccountNames(): Observable<string[]> {
    return this.http.get<string[]>('/api/accounts/names');
  }

  updatePasswordAccount(account: AccountList, newPassword: string): Observable<any> {
    if (newPassword !== '') {
      return this.http.post('/api/account/password', {
        account: account,
        newPassword: newPassword
      });
    } else {
      return this.http.post('/api/account/password', {
        account: account,
        newPassword: ''
      });
    }
  }

  create(account: AccountList, newPassword?: string, pending?: PendingAccount): Observable<any> {
    if (pending && pending.appOauth2UserId !== undefined) {
      return this.http.put('/api/account', {
        'loginId': account.loginId,
        'name': account.name,
        'organization': account.organization,
        'developer': account.developer,
        'appOauth2UserId': pending.appOauth2UserId,
        'sub': pending.sub
      });
    } else {
      return this.http.put('/api/account', {
        'loginId': account.loginId,
        'password': newPassword,
        'name': account.name,
        'organization': account.organization,
        'developer': account.developer
      });
    }
  }
  link(pending: PendingAccount, account: AccountList): Observable<any> {
      return this.http.post('/api/pending/link/' + pending.appOauth2UserId, {
        'appUserId': account.appUserId});
  }

}
