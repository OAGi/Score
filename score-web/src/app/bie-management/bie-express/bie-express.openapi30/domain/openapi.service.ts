import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {OasDoc, OasDocListRequest} from './openapi-doc';
import {Observable} from 'rxjs';
import {PageResponse} from '../../../../basis/basis';

@Injectable()
export class OpenAPIService{
  constructor(private http: HttpClient) {
  }
  getOasDocList(request: OasDocListRequest): Observable<PageResponse<OasDoc>>{
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.title) {
      params = params.set('title', request.filters.title);
    }
    if (request.filters.openAPIVersion) {
      params = params.set('openAPIVersion', request.filters.openAPIVersion);
    }
    if (request.filters.version) {
      params = params.set('version', request.filters.version);
    }
    if (request.filters.licenseName) {
      params = params.set('licenseName', request.filters.licenseName);
    }

    if (request.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', request.updaterUsernameList.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    return this.http.get<PageResponse<OasDoc>>('/api/oas_docs', {params});
  }
  getOasDoc(id): Observable<OasDoc>{
    return this.http.get<OasDoc>('/api/oas_doc/' + id);
  }

  delete(...oasDocIds): Observable<any> {
    if (oasDocIds.length === 1) {
      return this.http.delete('/api/oas_doc/' + oasDocIds[0]);
    } else {
      return this.http.post<any>('/api/oas_doc/delete', {
        oasDocIdList: oasDocIds
      });
    }
  }




}
