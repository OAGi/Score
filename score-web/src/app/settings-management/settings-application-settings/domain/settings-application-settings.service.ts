import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class SettingsApplicationSettingsService {

  constructor(private http: HttpClient) {

  }

  updateTenantConfiguration(value: boolean): Observable<any> {
    return this.updateConfiguration('tenant', value);
  }

  updateBusinessTermConfiguration(value: boolean): Observable<any> {
    return this.updateConfiguration('business-term', value);
  }

  updateBIEInverseModeConfiguration(value: boolean): Observable<any> {
    return this.updateConfiguration('bie-inverse-mode', value);
  }

  updateConfiguration(type: string, value: boolean): Observable<any> {
    return this.http.post('/api/application/' + type + '/' + (value ? 'enable' : 'disable'), {});
  }

}
