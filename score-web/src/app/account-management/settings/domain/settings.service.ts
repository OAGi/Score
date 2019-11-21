import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class SettingsService {

  constructor(private http: HttpClient) {

  }

  updatePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.post('/api/settings/password', {
      oldPassword: oldPassword,
      newPassword: newPassword
    });
  }

}
