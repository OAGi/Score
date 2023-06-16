import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {BieListForOasDoc, BieListForOasDocRequest, OasDoc, OasDocListRequest} from './openapi-doc';
import {Observable} from 'rxjs';
import {PageResponse} from '../../../../basis/basis';
import {BieList} from '../../../bie-list/domain/bie-list';

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
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
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



  getBieListForOasDoc(request: BieListForOasDocRequest): Observable<PageResponse<BieListForOasDoc>>{
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.propertyTerm) {
      params = params.set('propertyTerm', request.filters.propertyTerm);
    }
    if (request.filters.bizCtxName) {
      params = params.set('bizCtxName', request.filters.bizCtxName);
    }
    if (request.filters.topLevelAsbiepId) {
      params = params.set('topLevelAsbiepId', request.filters.topLevelAsbiepId.toString());
    }
    if (request.filters.den) {
      params = params.set('den', request.filters.den);
    }
    if (request.release) {
      params = params.set('releaseId', request.release.releaseId.toString());
    }
    if (request.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', request.ownerLoginIds.join(','));
    }
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
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
    return this.http.get<PageResponse<BieListForOasDoc>>('/api/oas_doc/bie_list', {params});
  }

}
