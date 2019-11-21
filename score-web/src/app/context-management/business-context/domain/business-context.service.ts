import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BusinessContext, BusinessContextListRequest, BusinessContextValue, BusinessContextRule} from './business-context';
import {PageResponse} from '../../../basis/basis';
import {BieEditAbieNode, BieEditNode} from '../../../bie-management/bie-edit/domain/bie-edit-node';

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
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
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

    return this.http.get<PageResponse<BusinessContext>>('/api/business_contexts', {params: params});
  }

  getBusinessContext(id): Observable<BusinessContext> {
    return this.http.get<BusinessContext>('/api/business_context/' + id);
  }

  getBusinessContextsByBizCtxIds(bizCtxIds: number[]): Observable<PageResponse<BusinessContext>> {
    const params = new HttpParams()
      .set('bizCtxIds', bizCtxIds.join(','));

    return this.http.get<PageResponse<BusinessContext>>('/api/business_contexts', {params: params});
  }

  getBusinessContextsByTopLevelAbieId(topLevelAbieId: number): Observable<PageResponse<BusinessContext>> {
    const params = new HttpParams()
      .set('topLevelAbieId', '' + topLevelAbieId);

    return this.http.get<PageResponse<BusinessContext>>('/api/business_contexts', {params: params});
  }

  getBusinessContextValues(): Observable<BusinessContextValue[]> {
    return this.http.get<BusinessContextValue[]>('/api/business_context_values');
  }

  create(bizCtx: BusinessContext): Observable<any> {
    return this.http.put('/api/business_context', {
      'name': bizCtx.name,
      'bizCtxValues': bizCtx.bizCtxValues
    });
  }

  update(bizCtx: BusinessContext): Observable<any> {
    return this.http.post('/api/business_context/' + bizCtx.bizCtxId, {
      'name': bizCtx.name,
      'bizCtxValues': bizCtx.bizCtxValues
    });
  }

  assign(topLevelAbieId: number, bizCtx: BusinessContext): Observable<any> {
    const params = new HttpParams()
      .set('topLevelAbieId', '' + topLevelAbieId);

    return this.http.put('/api/business_context/' + bizCtx.bizCtxId, null, {params: params});
  }

  dismiss(topLevelAbieId: number, bizCtx: BusinessContext): Observable<any> {
    const params = new HttpParams()
      .set('topLevelAbieId', '' + topLevelAbieId);

    return this.http.delete('/api/business_context/' + bizCtx.bizCtxId, {params: params});
  }

  delete(...bizCtxIds): Observable<any> {
    if (bizCtxIds.length === 1) {
      return this.http.delete('/api/business_context/' + bizCtxIds[0]);
    } else {
      return this.http.post<any>('/api/business_context/delete', {
        bizCtxIds: bizCtxIds
      });
    }
  }

  getAssignBizCtx(id): Observable<BusinessContextRule[]> {
    return this.http.get<BusinessContextRule[]>('/api/profile_bie/' + id + '/biz_ctx');
  }

  assignBizCtx(bie: BieEditAbieNode, bizCtxList: number[]): Observable<any> {
    return this.http.post('/api/profile_bie/' + bie.topLevelAbieId + '/assign_biz_ctx', {
      bizCtxList: bizCtxList
    });
  }
}
