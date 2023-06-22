import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {BieForOasDoc, BieForOasDocListRequest, OasDoc, OasDocListRequest} from './openapi-doc';
import {Observable} from 'rxjs';
import {PageResponse} from '../../../../basis/basis';

@Injectable()
export class OpenAPIService {
  constructor(private http: HttpClient) {
  }

  getOasDocList(request: OasDocListRequest): Observable<PageResponse<OasDoc>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
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

  getOasDoc(id): Observable<OasDoc> {
    return this.http.get<OasDoc>('/api/oas_doc/' + id);
  }
  getBieForOasDoc(id): Observable<BieForOasDoc> {
    return this.http.get<BieForOasDoc>('/api/oas_doc/' + id + '/bie_list');
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

  createOasDoc(oasDoc: OasDoc): Observable<any> {
    if ('' + oasDoc.oasDocId === 'undefined' || !oasDoc.oasDocId) {
      oasDoc.oasDocId = null;
    }
    return this.http.put('/api/oas_doc', {
      oasDocId: oasDoc.oasDocId,
      title: oasDoc.title,
      openAPIVersion: oasDoc.openAPIVersion,
      version: oasDoc.version,
      licenseName: oasDoc.licenseName,
      description: oasDoc.description,
      termsOfService: oasDoc.termsOfService,
      contactName: oasDoc.contactName,
      contactUrl: oasDoc.contactUrl,
      contactEmail: oasDoc.contactEmail,
      licenseUrl: oasDoc.licenseUrl
    });
  }

  updateOasDoc(oasDoc: OasDoc): Observable<any> {
    return this.http.post('/api/oas_doc/' + oasDoc.oasDocId, {
      oasDocId: oasDoc.oasDocId,
      title: oasDoc.title,
      openAPIVersion: oasDoc.openAPIVersion,
      version: oasDoc.version,
      licenseName: oasDoc.licenseName,
      description: oasDoc.description,
      termsOfService: oasDoc.termsOfService,
      contactName: oasDoc.contactName,
      contactUrl: oasDoc.contactUrl,
      contactEmail: oasDoc.contactEmail,
      licenseUrl: oasDoc.licenseUrl
    });
  }

  checkUniqueness(oasDoc: OasDoc): Observable<any> {
    return this.http.post('/api/oas_docs/check_uniqueness', {
      oasDocId: oasDoc.oasDocId,
      title: oasDoc.title,
      openAPIVersion: oasDoc.openAPIVersion,
      version: oasDoc.version,
      licenseName: oasDoc.licenseName
    });
  }

  checkTitleUniqueness(oasDoc: OasDoc): Observable<any> {
    return this.http.post('/api/oas_docs/check_name_uniqueness', {
      oasDocId: oasDoc.oasDocId,
      title: oasDoc.title
    });
  }

  getBieForOasDocListWithRequest(request: BieForOasDocListRequest): Observable<PageResponse<BieForOasDoc>>{
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', request.ownerLoginIds.join(','));
    }
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.den) {
      params = params.set('den', request.filters.den);
    }
    if (request.filters.propertyTerm) {
      params = params.set('propertyTerm', request.filters.propertyTerm);
    }
    if (request.filters.businessContext) {
      params = params.set('businessContext', request.filters.businessContext);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.access) {
      params = params.set('access', request.access);
    }
    if (request.excludePropertyTerms.length > 0) {
      params = params.set('excludePropertyTerms', request.excludePropertyTerms.join(','));
    }
    if (request.excludeTopLevelAsbiepIds.length > 0) {
      params = params.set('excludeTopLevelAsbiepIds', request.excludeTopLevelAsbiepIds.join(','));
    }
    if (request.release) {
      params = params.set('releaseId', request.release.releaseId.toString());
    }
    if (request.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', request.ownedByDeveloper.toString());
    }
    return this.http.get<PageResponse<BieForOasDoc>>('/api/oas_doc/bie_list', {params});
  }
}
