import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode} from '../../common/utility';
export class PendingAccount {
  appOauth2UserId: number;
  appUserId: number;
  providerName: string;
  name: string;
  email: string;
  sub: string;
  nickname: string;
  preferredUsername: string;
  phoneNumber: string;
  creationTimestamp: string;
  rejected: boolean;

  constructor(paramMap?: ParamMap) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();

    this.appOauth2UserId = parseInt(params.get('appOauth2UserId'), 10);
    if (!this.appOauth2UserId) {
      this.appOauth2UserId = undefined;
    }
    this.name = params.get('name');
    if (!this.name) {
      this.name = undefined;
    }
    this.nickname = params.get('nickname');
    if (!this.nickname) {
      this.nickname = undefined;
    }
    this.preferredUsername = params.get('preferredUsername');
    if (!this.preferredUsername) {
      this.preferredUsername = undefined;
    }
    this.email = params.get('email');
    if (!this.email) {
      this.email = undefined;
    }
    this.sub = params.get('sub');
    if (!this.sub) {
      this.sub = undefined;
    }
  }
}
