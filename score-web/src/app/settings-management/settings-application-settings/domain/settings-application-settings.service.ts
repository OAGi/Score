import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class SettingsApplicationSettingsService {

  constructor(private http: HttpClient) {

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

  updateBooleanConfiguration(type: string, value: boolean): Observable<any> {
    return this.http.post('/api/application/' + type + '/' + (value ? 'enable' : 'disable'), {});
  }

  updateConfiguration(key: string, value: string): Observable<any> {
    return this.http.post('/api/application/' + key, {
      value
    });
  }

}
