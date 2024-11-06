import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ApplicationSettingsInfo} from './application-settings';

@Injectable()
export class SettingsApplicationSettingsService {

  constructor(private http: HttpClient) {

  }

  load(): Observable<ApplicationSettingsInfo> {
    return this.http.get<ApplicationSettingsInfo>('/api/application/settings');
  }

  update(applicationSettingsInfo: ApplicationSettingsInfo): Observable<any> {
    return this.http.post('/api/application/settings', {
      smtpSettingsInfo: applicationSettingsInfo.smtpSettingsInfo
    });
  }

  validateEmail(q: string): Observable<any> {
    return this.http.post('/api/account/email_validation', {
      q
    });
  }

  updateTenantConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('tenant', value);
  }

  updateBusinessTermConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('business-term', value);
  }

  updateBIEInverseModeConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('bie-inverse-mode', value);
  }

  updateFunctionsRequiringEmailTransmissionConfiguration(value: boolean): Observable<any> {
    return this.updateBooleanConfiguration('functions-requiring-email-transmission', value);
  }

  updateBooleanConfiguration(type: string, value: boolean): Observable<any> {
    return this.http.post('/api/application/' + type + '/' + (value ? 'enable' : 'disable'), {});
  }

  getConfiguration(key: string): Observable<any> {
    return this.http.get<any>('/api/application/' + key);
  }

}
