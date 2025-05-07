import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AccountDetails, AccountList, AccountListEntry, AccountListRequest} from './accounts';
import {PageResponse} from '../../basis/basis';
import {PendingAccount} from './pending-list';

@Injectable()
export class AccountListService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getAccountsList(request: AccountListRequest, excludeRequester?: boolean): Observable<PageResponse<AccountListEntry>> {
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
    if (request.filters.tenantId) {
      params = params.set('tenantId', request.filters.tenantId);
    }
    if (request.filters.notConnectedToTenant) {
      params = params.set('notConnectedToTenant', 'true');
    }
    if (request.filters.businessCtxIds) {
      params = params.set('businessCtxIds', request.filters.businessCtxIds.join(','));
    }
    if (excludeRequester) {
      params = params.set('excludeRequester', 'true');
    }
    return this.http.get<PageResponse<AccountListEntry>>('/api/accounts', {params});
  }

  getAccount(appUserIdOrUsername: number | string): Observable<AccountDetails> {
    return this.http.get<AccountDetails>('/api/accounts/' + appUserIdOrUsername);
  }

  getAccountNames(): Observable<string[]> {
    return this.http.get<string[]>('/api/accounts/names');
  }

  update(userId: number,
         username: string, organization: string,
         admin: boolean,
         newPassword: string): Observable<any> {

    return this.http.put('/api/accounts/' + userId, {
      username, organization, admin, newPassword
    });
  }

  updatePassword(userId: number, newPassword: string): Observable<any> {
    if (newPassword !== '') {
      return this.http.put('/api/accounts/' + userId + '/password', {
        newPassword
      });
    } else {
      return this.http.put('/api/accounts/' + userId + '/password', {
        newPassword: ''
      });
    }
  }

  create(account: AccountList, newPassword?: string, pending?: PendingAccount): Observable<any> {
    if (pending && pending.appOauth2UserId !== undefined) {
      return this.http.post('/api/accounts', {
        loginId: account.loginId,
        name: account.name,
        organization: account.organization,
        developer: account.developer,
        admin: account.admin,
        oAuth2UserId: pending.appOauth2UserId,
        sub: pending.sub
      });
    } else {
      return this.http.post('/api/accounts', {
        loginId: account.loginId,
        password: newPassword,
        name: account.name,
        organization: account.organization,
        developer: account.developer,
        admin: account.admin
      });
    }
  }

  link(pending: PendingAccount, userId: number): Observable<any> {
    return this.http.post('/api/pending/link/' + pending.appOauth2UserId, {
      appUserId: userId
    });
  }

  setEnable(userId: number, val: boolean): Observable<any> {
    return this.http.post('/api/accounts/' + userId + '/' + ((val) ? 'enable' : 'disable'), {});
  }

  transferOwnership(userId: number): Observable<any> {
    return this.http.post('/api/accounts/' + userId + '/transfer', {});
  }

  delink(userId: number): Observable<any> {
    return this.http.put('/api/accounts/' + userId + '/delink', {});
  }

  remove(userId: number): Observable<any> {
    return this.http.delete('/api/accounts/' + userId, {});
  }
}
