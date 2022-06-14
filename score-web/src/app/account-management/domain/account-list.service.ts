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

  getAllAccountList(): Observable<PageResponse<AccountList>> {
    const params = new HttpParams()
      .set('sortActive', 'loginId')
      .set('sortDirection', 'asc')
      .set('pageIndex', '' + 0)
      .set('pageSize', '' + 1000);

    return this.http.get<PageResponse<AccountList>>('/api/accounts_list', {params: params});
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
    if (request.filters.status) {
      params = params.set('status', request.filters.status.join(','));
    }
    if (request.filters.roles) {
      params = params.set('roles', request.filters.roles.join(','));
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
        'admin': account.admin,
        'appOauth2UserId': pending.appOauth2UserId,
        'sub': pending.sub
      });
    } else {
      return this.http.put('/api/account', {
        'loginId': account.loginId,
        'password': newPassword,
        'name': account.name,
        'organization': account.organization,
        'developer': account.developer,
        'admin': account.admin
      });
    }
  }
  link(pending: PendingAccount, account: AccountList): Observable<any> {
    return this.http.post('/api/pending/link/' + pending.appOauth2UserId, {
      'appUserId': account.appUserId});
  }

  setEnable(account: AccountList, val: boolean): Observable<any> {
    return this.http.post('/api/accounts/' + account.appUserId + '/' + ((val) ? 'enable' : 'disable'), {});
  }
}
