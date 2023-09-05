import {Injectable} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {
  AssignBieForOasDoc,
  BieForOasDoc, BieForOasDocDeleteRequest,
  BieForOasDocListRequest,
  BieForOasDocUpdateRequest,
  OasDoc,
  OasDocListRequest
} from './openapi-doc';
import {Observable} from 'rxjs';
import {PageResponse} from '../../../../basis/basis';
import {BieExpressOption} from '../../domain/generate-expression';
import {base64Encode} from '../../../../common/utility';

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

  getBieListForOasDoc(id): Observable<BieForOasDoc> {
    return this.http.get<BieForOasDoc>('/api/oas_doc/' + id + '/bie_list');
  }

  getBieForOasDoc(oasDocId: number, topLevelAsbiepId: number): Observable<BieForOasDoc> {
    return this.http.get<BieForOasDoc>('/api/oas_doc/' + oasDocId + '/bie_list/' + topLevelAsbiepId);
  }

  updateBieForOasDoc(oasDocId: number, topLevelAsbiepId: number, request: BieForOasDocUpdateRequest): Observable<BieForOasDoc> {
    return this.http.put<BieForOasDoc>('/api/oas_doc/' + oasDocId + '/bie_list/' + topLevelAsbiepId, request);
  }

  updateDetails(request: BieForOasDocUpdateRequest): Observable<any> {
    console.log(request);
    return this.http.put('/api/oas_doc/' + request.oasDocId + '/bie_list/detail', request.json);
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
    return this.http.post('/api/oas_doc', {
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
  checkBIEreusedAcrossMultipleOperations(bieForOasDoc: BieForOasDoc): Observable<any>{
    return this.http.post('/api/oas_doc/check_bie_reused_across_operations',{
      oasDocId: bieForOasDoc.oasDocId,
      verb: bieForOasDoc.verb,
      topLevelAsbiepId: bieForOasDoc.topLevelAsbiepId,
      messageBody: bieForOasDoc.messageBody,
      resourceName: bieForOasDoc.resourceName
    });
  }

  getBieForOasDocListWithRequest(request: BieForOasDocListRequest, oasDoc: OasDoc): Observable<PageResponse<BieForOasDoc>> {
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
    if (request.filters.asccpManifestId) {
      params = params.set('asccpManifestId', '' + request.filters.asccpManifestId);
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
    return this.http.get<PageResponse<BieForOasDoc>>('/api/oas_doc/' + oasDoc.oasDocId + '/bie_list', {params});
  }

  selectBieForOasDocListWithRequest(request: BieForOasDocListRequest, oasDoc: OasDoc): Observable<PageResponse<BieForOasDoc>> {
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
    if (request.filters.asccpManifestId) {
      params = params.set('asccpManifestId', '' + request.filters.asccpManifestId);
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
    return this.http.get<PageResponse<BieForOasDoc>>('/api/oas_doc/' + oasDoc.oasDocId + '/select_bie', {params});
  }

  assignBieForOasDoc(assignBieForOasDoc: AssignBieForOasDoc): Observable<any> {
    return this.http.post<any>('/api/oas_doc/' + assignBieForOasDoc.oasDocId + '/bie_list',
      {
        oasDocId: assignBieForOasDoc.oasDocId,
        oasRequest: assignBieForOasDoc.oasRequest,
        topLevelAsbiepId: assignBieForOasDoc.topLevelAsbiepId,
        propertyTerm: assignBieForOasDoc.propertyTerm,
        verb: assignBieForOasDoc.verb,
        required: assignBieForOasDoc.required,
        arrayIndicator: assignBieForOasDoc.arrayIndicator,
        suppressRootIndicator: assignBieForOasDoc.suppressRootIndicator,
        messageBody: assignBieForOasDoc.messageBody
      });
  }
  removeBieForOasDoc(request: BieForOasDocDeleteRequest): Observable<any>{
    return this.http.post<any>('/api/oas_doc/' + request.oasDocId + '/bie_list/delete', request.json);
  }

  generate(topLevelAsbiepIds: number[], option: BieExpressOption, oasDoc: OasDoc): Observable<HttpResponse<Blob>> {
    let params: HttpParams = new HttpParams()
      .set('topLevelAsbiepIds', topLevelAsbiepIds.join(','));
    Object.getOwnPropertyNames(option).forEach(key => {
      const value = option[key];
      if (value) {
        if (key === 'filenames') {
          for (const topLevelAsbiepId of Object.keys(value)) {
            params = params.set('filenames[' + topLevelAsbiepId + ']', value[topLevelAsbiepId]);
          }
        } else if (key === 'bizCtxIds') {
          for (const topLevelAsbiepId of Object.keys(value)) {
            params = params.set('bizCtxIds[' + topLevelAsbiepId + ']', Number(value[topLevelAsbiepId]));
          }
        } else {
          params = params.set(key, option[key]);
        }
      }
    });
    return this.http.get('api/oas_doc/' + oasDoc.oasDocId + '/generate', {
      params: new HttpParams().set('data', base64Encode(params.toString())),
      observe: 'response',
      responseType: 'blob'
    });
  }

  generateOpenAPI(oasDocId: number): Observable<HttpResponse<Blob>> {
    return this.http.get('api/oas_doc/' + oasDocId + '/generate', {
      observe: 'response',
      responseType: 'blob'
    });
  }
}
