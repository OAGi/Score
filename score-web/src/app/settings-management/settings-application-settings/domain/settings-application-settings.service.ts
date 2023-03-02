import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class SettingsApplicationSettingsService {

  constructor(private http: HttpClient) {

  }

  updateTenantConfiguration(value: boolean): Observable<any> {
    return this.http.post('/api/application/tenant/' + (value ? 'enable' : 'disable'), {});
  }

  updateBusinessTermConfiguration(value: boolean): Observable<any> {
    return this.http.post('/api/application/business-term/' + (value ? 'enable' : 'disable'), {});
  }

}
