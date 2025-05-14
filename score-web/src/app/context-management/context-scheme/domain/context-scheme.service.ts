import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  ContextSchemeCreateRequest,
  ContextSchemeDetails,
  ContextSchemeListEntry,
  ContextSchemeListRequest,
  ContextSchemeSummary,
  ContextSchemeUpdateRequest,
  ContextSchemeValue,
  ContextSchemeValueSummary
} from './context-scheme';
import {PageResponse} from '../../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class ContextSchemeService {

  constructor(private http: HttpClient) {
  }

  getContextSchemeSummaries(): Observable<ContextSchemeSummary[]> {
    return this.http.get<ContextSchemeSummary[]>('/api/context-schemes/summaries');
  }

  getContextSchemeValueSummaries(): Observable<ContextSchemeValueSummary[]> {
    return this.http.get<ContextSchemeValueSummary[]>('/api/context-schemes/values/summaries');
  }

  getContextSchemeList(request: ContextSchemeListRequest): Observable<PageResponse<ContextSchemeListEntry>> {
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

    return this.http.get<PageResponse<ContextSchemeListEntry>>('/api/context-schemes', {params}).pipe(
        map((res: PageResponse<ContextSchemeListEntry>) => ({
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

  getContextSchemeDetails(contextSchemeId): Observable<ContextSchemeDetails> {
    return this.http.get<ContextSchemeDetails>('/api/context-schemes/' + contextSchemeId);
  }

  getContextSchemeValues(contextSchemeId: number): Observable<ContextSchemeValue[]> {
    return this.http.get<ContextSchemeValue[]>('/api/context-schemes/' + contextSchemeId + '/values');
  }

  create(contextScheme: ContextSchemeCreateRequest): Observable<any> {
    return this.http.post('/api/context-schemes', {
      contextCategoryId: contextScheme.contextCategoryId,
      schemeName: contextScheme.schemeName,
      codeListId: contextScheme.codeListId,
      schemeId: contextScheme.schemeId,
      schemeAgencyId: contextScheme.schemeAgencyId,
      schemeVersionId: contextScheme.schemeVersionId,
      description: contextScheme.description,
      contextSchemeValueList: contextScheme.contextSchemeValueList
    });
  }

  update(contextScheme: ContextSchemeUpdateRequest): Observable<any> {
    return this.http.put('/api/context-schemes/' + contextScheme.contextSchemeId, {
      contextCategoryId: contextScheme.contextCategoryId,
      schemeName: contextScheme.schemeName,
      codeListId: contextScheme.codeListId,
      schemeId: contextScheme.schemeId,
      schemeAgencyId: contextScheme.schemeAgencyId,
      schemeVersionId: contextScheme.schemeVersionId,
      description: contextScheme.description,
      contextSchemeValueList: contextScheme.contextSchemeValueList
    });
  }

  delete(...contextSchemeIds): Observable<any> {
    if (contextSchemeIds.length === 1) {
      return this.http.delete('/api/context-schemes/' + contextSchemeIds[0]);
    } else {
      return this.http.delete<any>('/api/context-schemes', {
        body: {
          contextSchemeIdList: contextSchemeIds
        }
      });
    }
  }

  checkUniqueness(schemeId: string, schemeAgencyId: string, schemeVersionId: string,
                  contextSchemeId?: number): Observable<boolean> {
    let params = new HttpParams()
        .set('schemeId', schemeId)
        .set('schemeAgencyId', schemeAgencyId)
        .set('schemeVersionId', schemeVersionId);
    if (!!contextSchemeId) {
      params = params.set('ctxSchemeId', '' + contextSchemeId);
    }

    return this.http.get<boolean>('/api/context-schemes/check-uniqueness', {params});
  }

  checkNameUniqueness(schemeName: string, schemeId: string, schemeAgencyId: string, schemeVersionId: string,
                      contextSchemeId?: number): Observable<any> {
    let params = new HttpParams()
        .set('schemeName', schemeName)
        .set('schemeId', schemeId)
        .set('schemeAgencyId', schemeAgencyId)
        .set('schemeVersionId', schemeVersionId);
    if (!!contextSchemeId) {
      params = params.set('ctxSchemeId', '' + contextSchemeId);
    }

    return this.http.get<boolean>('/api/context-schemes/check-name-uniqueness', {params});
  }

}
