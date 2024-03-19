import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class SettingsAccountService {

  constructor(private http: HttpClient) {

  }

  updatePersonalInfo(personalInfo: any, parameters: any): Observable<any> {
    return this.http.post('/api/account', {
      email: personalInfo.email,
      parameters
    });
  }

  resendEmailValidationRequest(personalInfo: any, parameters: any): Observable<any> {
    return this.http.post('/api/account/resend_email_validation_request', {
      email: personalInfo.email,
      parameters
    });
  }

  updatePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.post('/api/settings/password', {
      oldPassword,
      newPassword
    });
  }

}
