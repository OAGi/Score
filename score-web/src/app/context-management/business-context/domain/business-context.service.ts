import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  BusinessContext,
  BusinessContextDetails,
  BusinessContextListEntry,
  BusinessContextListRequest,
  BusinessContextSummary,
  BusinessContextValue
} from './business-context';
import {PageResponse} from '../../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class BusinessContextService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getBusinessContextSummaries(): Observable<BusinessContextSummary[]> {
    return this.http.get<BusinessContextSummary[]>('/api/business-contexts/summaries');
  }

  getBusinessContextList(request: BusinessContextListRequest): Observable<PageResponse<BusinessContextListEntry>> {
    let params = new HttpParams()
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (!!request.updatedDate.start || !!request.updatedDate.end) {
      params = params.set('lastUpdatedOn',
          '[' + (!!request.updatedDate.start ? request.updatedDate.start.getTime() : '') + '~' +
          (!!request.updatedDate.end ? request.updatedDate.end.getTime() : '') + ']');
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
    return this.http.get<PageResponse<BusinessContextListEntry>>('/api/business-contexts', {params}).pipe(
        map((res: PageResponse<BusinessContextListEntry>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            created: {
              ...elm.created,
              when: new Date(elm.created.when),
            },
            lastUpdated: {
              ...elm.lastUpdated,
              when: new Date(elm.lastUpdated.when),
            }
          }))
        }))
    );
  }

  getBusinessContextDetails(businessContextId): Observable<BusinessContextDetails> {
    return this.http.get<BusinessContextDetails>('/api/business-contexts/' + businessContextId);
  }

  getBusinessContextValues(businessContextId): Observable<BusinessContextValue[]> {
    return this.http.get<BusinessContextValue[]>('/api/business-contexts/' + businessContextId + '/values');
  }

  create(name: string, businessContextValues: BusinessContextValue[]): Observable<any> {
    return this.http.post('/api/business-contexts', {
      name,
      businessContextValueList: businessContextValues
    });
  }

  update(businessContextId: number, name: string, businessContextValues: BusinessContextValue[]): Observable<any> {
    return this.http.put('/api/business-contexts/' + businessContextId, {
      name,
      businessContextValueList: businessContextValues
    });
  }

  delete(...businessContextIds): Observable<any> {
    if (businessContextIds.length === 1) {
      return this.http.delete('/api/business-contexts/' + businessContextIds[0]);
    } else {
      return this.http.delete<any>('/api/business-contexts', {
        body: {
          businessContextIdList: businessContextIds
        }
      });
    }
  }

  assign(topLevelAsbiepId: number, businessContext: BusinessContext): Observable<any> {
    return this.http.post('/api/business-contexts/' + businessContext.businessContextId + '/assignments/' + topLevelAsbiepId, {});
  }

  unassign(topLevelAsbiepId: number, businessContext: BusinessContext): Observable<any> {
    return this.http.delete('/api/business-contexts/' + businessContext.businessContextId + '/assignments/' + topLevelAsbiepId, {});
  }

  getBusinessContextsByBizCtxIds(businessContextIdList: number[]): Observable<BusinessContextSummary[]> {
    const params = new HttpParams()
        .set('businessContextIdList', businessContextIdList.join(','));

    return this.http.get<BusinessContextSummary[]>('/api/business-contexts/summaries', {params});
  }

  getBusinessContextsByTopLevelAsbiepId(topLevelAsbiepId: number): Observable<BusinessContextSummary[]> {
    const params = new HttpParams()
        .set('topLevelAsbiepId', '' + topLevelAsbiepId);

    return this.http.get<BusinessContextSummary[]>('/api/business-contexts/summaries', {params});
  }
}
