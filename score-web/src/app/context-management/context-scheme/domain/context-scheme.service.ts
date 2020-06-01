import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  ContextScheme,
  ContextSchemeListRequest,
  ContextSchemeValue,
  ContextSchemeValueListRequest,
  SimpleContextCategory,
  SimpleContextScheme,
  SimpleContextSchemeValue
} from './context-scheme';
import {CodeList} from '../../../code-list-management/domain/code-list';
import {PageResponse} from '../../../basis/basis';

@Injectable()
export class ContextSchemeService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getSimpleContextCategories(): Observable<SimpleContextCategory[]> {
    return this.http.get<SimpleContextCategory[]>('/api/simple_context_categories');
  }

  getSimpleContextSchemes(): Observable<SimpleContextScheme[]> {
    return this.http.get<SimpleContextScheme[]>('/api/simple_context_schemes');
  }

  getSimpleContextSchemeByCtxCategoryId(ctxCategoryId: number): Observable<SimpleContextScheme[]> {
    return this.http.get<SimpleContextScheme[]>('/api/context_category/' + ctxCategoryId + '/simple_context_schemes');
  }

  getSimpleContextSchemeValues(ctxSchemeId: number): Observable<SimpleContextSchemeValue[]> {
    return this.http.get<SimpleContextSchemeValue[]>('/api/context_scheme/' + ctxSchemeId + '/simple_context_scheme_values');
  }

  getContextSchemeList(request: ContextSchemeListRequest): Observable<PageResponse<ContextScheme>> {
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

    return this.http.get<PageResponse<ContextScheme>>('/api/context_schemes', {params: params});
  }

  getContextSchemeValueList(request: ContextSchemeValueListRequest): Observable<PageResponse<ContextSchemeValue>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.value) {
      params = params.set('value', request.filters.value);
    }

    return this.http.get<PageResponse<ContextSchemeValue>>('/api/context_scheme_values', {params: params});
  }

  getContextScheme(id): Observable<ContextScheme> {
    return this.http.get<ContextScheme>('/api/context_scheme/' + id);
  }

  getCodeLists(): Observable<PageResponse<CodeList>> {
    // TODO:: Maybe needs endless scrolling against large amounts of codeList
    const pageSize = '1000';
    const params = new HttpParams()
      .set('sortActive', '')
      .set('sortDirection', '')
      .set('pageIndex', '-1')
      .set('states', 'Published')
      .set('pageSize', pageSize);

    return this.http.get<PageResponse<CodeList>>('/api/code_list', {params: params});
  }

  getCodeList(id): Observable<CodeList> {
    if (id !== 0) {
      return this.http.get<CodeList>('/api/code_list/' + id);
    }
  }

  getSimpleContextSchemeValueByCtxSchemeValuesId(id): Observable<ContextSchemeValue> {
    return this.http.get<ContextSchemeValue>('/api/simple_context_scheme_value_from_ctx_values/' + id);
  }

  create(contextScheme: ContextScheme): Observable<any> {
    // The foreign key codeListID needs to have a value in the table codeList, so we should create a 'fake' codelist value in the code
    // list table to state the fact that we do not use any code list when we created the ctx scheme.
    if ('' + contextScheme.codeListId === 'undefined' || contextScheme.codeListId <= 0) {
      contextScheme.codeListId = null;
    }
    return this.http.put('/api/context_scheme', {
      'ctxCategoryId': contextScheme.ctxCategoryId,
      'schemeName': contextScheme.schemeName,
      'codeListId': contextScheme.codeListId,
      'schemeId': contextScheme.schemeId,
      'schemeAgencyId': contextScheme.schemeAgencyId,
      'schemeVersionId': contextScheme.schemeVersionId,
      'description': contextScheme.description,
      'ctxSchemeValues': contextScheme.ctxSchemeValues
    });
  }

  update(contextScheme: ContextScheme): Observable<any> {
    return this.http.post('/api/context_scheme/' + contextScheme.ctxSchemeId, {
      'ctxCategoryId': contextScheme.ctxCategoryId,
      'schemeName': contextScheme.schemeName,
      'codeListId': contextScheme.codeListId,
      'schemeId': contextScheme.schemeId,
      'schemeAgencyId': contextScheme.schemeAgencyId,
      'schemeVersionId': contextScheme.schemeVersionId,
      'description': contextScheme.description,
      'ctxSchemeValues': contextScheme.ctxSchemeValues
    });
  }

  delete(...ctxSchemeIds): Observable<any> {
    if (ctxSchemeIds.length === 1) {
      return this.http.delete('/api/context_scheme/' + ctxSchemeIds[0]);
    } else {
      return this.http.post<any>('/api/context_scheme/delete', {
        ctxSchemeIds: ctxSchemeIds
      });
    }
  }
}
