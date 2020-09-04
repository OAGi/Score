import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {BieExpressOption} from './generate-expression';
import {HttpParams} from '../../../../../node_modules/@angular/common/http';
import {Observable} from 'rxjs';
import {base64Encode} from '../../../common/utility';

@Injectable()
export class BieExpressService {

  constructor(private http: HttpClient) {
  }

  generate(topLevelAsbiepIds: number[], option: BieExpressOption): Observable<HttpResponse<Blob>> {
    let params: HttpParams = new HttpParams()
      .set('topLevelAsbiepIds', topLevelAsbiepIds.join(','));
    Object.getOwnPropertyNames(option).forEach(key => {
      const value = option[key];
      if (value) {
        params = params.set(key, option[key]);
      }
    });

    return this.http.get('/api/profile_bie/generate', {
      params: new HttpParams().set('data', base64Encode(params.toString())),
      observe: 'response',
      responseType: 'blob'
    });
  }
}
