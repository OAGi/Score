import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Base64} from 'js-base64';
import {BieExpressOption} from './generate-expression';
import {HttpParams} from '../../../../../node_modules/@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class BieExpressService {

  constructor(private http: HttpClient) {
  }

  generate(topLevelAbieIds: number[], option: BieExpressOption): Observable<HttpResponse<Blob>> {
    let params: HttpParams = new HttpParams()
      .set('topLevelAbieIds', topLevelAbieIds.join(','));
    Object.getOwnPropertyNames(option).forEach(key => {
      const value = option[key];
      if (value) {
        params = params.set(key, option[key]);
      }
    });

    return this.http.get('/api/profile_bie/generate', {
      params: new HttpParams().set('data', Base64.encode(params.toString())),
      observe: 'response',
      responseType: 'blob'
    });
  }
}
