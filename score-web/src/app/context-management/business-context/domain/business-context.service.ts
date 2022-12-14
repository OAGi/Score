import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BusinessContext, BusinessContextListRequest, BusinessContextRule, BusinessContextValue} from './business-context';
import {PageResponse} from '../../../basis/basis';
import {BieEditAbieNode} from '../../../bie-management/bie-edit/domain/bie-edit-node';

@Injectable()
export class BusinessContextService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getBusinessContextList(request: BusinessContextListRequest): Observable<PageResponse<BusinessContext>> {
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
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.filters.tenantId) {
      params = params.set('tenantId', request.filters.tenantId);
    }
    if (request.filters.notConnectedToTenant) {
      params = params.set('notConnectedToTenant', 'true');
    }
    if (request.filters.isBieEditing) {
      params = params.set('isBieEditing', 'true');
    }
    return this.http.get<PageResponse<BusinessContext>>('/api/business_contexts', {params: params});
  }

  getBusinessContext(id): Observable<BusinessContext> {
    return this.http.get<BusinessContext>('/api/business_context/' + id);
  }

  getBusinessContextsByBizCtxIds(businessContextIdList: number[]): Observable<PageResponse<BusinessContext>> {
    const params = new HttpParams()
      .set('businessContextIdList', businessContextIdList.join(','));

    return this.http.get<PageResponse<BusinessContext>>('/api/business_contexts', {params: params});
  }

  getBusinessContextsByTopLevelAsbiepId(topLevelAsbiepId: number): Observable<PageResponse<BusinessContext>> {
    console.log(`here ${topLevelAsbiepId}`)
    const params = new HttpParams()
      .set('topLevelAsbiepId', '' + topLevelAsbiepId);

    return this.http.get<PageResponse<BusinessContext>>('/api/business_contexts', {params: params});
  }

  getBusinessContextValues(): Observable<BusinessContextValue[]> {
    return this.http.get<BusinessContextValue[]>('/api/business_context_values');
  }

  create(businessContext: BusinessContext): Observable<any> {
    return this.http.put('/api/business_context', {
      'name': businessContext.name,
      'businessContextValueList': businessContext.businessContextValueList
    });
  }

  update(businessContext: BusinessContext): Observable<any> {
    return this.http.post('/api/business_context/' + businessContext.businessContextId, {
      'name': businessContext.name,
      'businessContextValueList': businessContext.businessContextValueList
    });
  }

  assign(topLevelAsbiepId: number, businessContext: BusinessContext): Observable<any> {
    const params = new HttpParams()
      .set('topLevelAsbiepId', '' + topLevelAsbiepId);

    return this.http.put('/api/business_context/' + businessContext.businessContextId, null, {params: params});
  }

  dismiss(topLevelAsbiepId: number, businessContext: BusinessContext): Observable<any> {
    const params = new HttpParams()
      .set('topLevelAsbiepId', '' + topLevelAsbiepId);

    return this.http.delete('/api/business_context/' + businessContext.businessContextId, {params: params});
  }

  delete(...businessContextIds): Observable<any> {
    if (businessContextIds.length === 1) {
      return this.http.delete('/api/business_context/' + businessContextIds[0]);
    } else {
      return this.http.post<any>('/api/business_context/delete', {
        businessContextIdList: businessContextIds
      });
    }
  }

  getAssignBizCtx(id): Observable<BusinessContextRule[]> {
    return this.http.get<BusinessContextRule[]>('/api/profile_bie/' + id + '/biz_ctx');
  }

  assignBizCtx(bie: BieEditAbieNode, businessContextList: number[]): Observable<any> {
    return this.http.post('/api/profile_bie/' + bie.topLevelAsbiepId + '/assign_biz_ctx', {
      businessContextList: businessContextList
    });
  }
}
