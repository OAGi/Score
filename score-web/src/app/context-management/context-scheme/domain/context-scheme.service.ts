import {Injectable} from '@angular/core';
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
export class ContextSchemeService {

  constructor(private http: HttpClient) {
  }

  getSimpleContextCategories(): Observable<SimpleContextCategory[]> {
    return this.http.get<SimpleContextCategory[]>('/api/simple_context_categories');
  }

  getSimpleContextSchemes(): Observable<SimpleContextScheme[]> {
    return this.http.get<SimpleContextScheme[]>('/api/simple_context_schemes');
  }

  getSimpleContextSchemeByCtxCategoryId(contextCategoryId: number): Observable<SimpleContextScheme[]> {
    return this.http.get<SimpleContextScheme[]>('/api/context_category/' + contextCategoryId + '/simple_context_schemes');
  }

  getSimpleContextSchemeValues(contextSchemeId: number): Observable<SimpleContextSchemeValue[]> {
    return this.http.get<SimpleContextSchemeValue[]>('/api/context_scheme/' + contextSchemeId + '/simple_context_scheme_values');
  }

  getContextSchemeList(request: ContextSchemeListRequest): Observable<PageResponse<ContextScheme>> {
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

    return this.http.get<PageResponse<ContextScheme>>('/api/context_schemes', {params});
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

    return this.http.get<PageResponse<ContextSchemeValue>>('/api/context_scheme_values', {params});
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

    return this.http.get<PageResponse<CodeList>>('/api/code_list', {params});
  }

  getCodeList(id): Observable<CodeList> {
    if (id !== 0) {
      return this.http.get<CodeList>('/api/code_list/' + id);
    }
  }

  create(contextScheme: ContextScheme): Observable<any> {
    // The foreign key codeListID needs to have a value in the table codeList, so we should create a 'fake' codelist value in the code
    // list table to state the fact that we do not use any code list when we created the ctx scheme.
    if ('' + contextScheme.codeListId === 'undefined' || contextScheme.codeListId <= 0) {
      contextScheme.codeListId = null;
    }
    return this.http.put('/api/context_scheme', {
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

  update(contextScheme: ContextScheme): Observable<any> {
    return this.http.post('/api/context_scheme/' + contextScheme.contextSchemeId, {
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
      return this.http.delete('/api/context_scheme/' + contextSchemeIds[0]);
    } else {
      return this.http.post<any>('/api/context_scheme/delete', {
        contextSchemeIdList: contextSchemeIds
      });
    }
  }

  checkUniqueness(contextScheme: ContextScheme): Observable<any> {
    return this.http.post('/api/context_scheme/check_uniqueness', {
      contextSchemeId: contextScheme.contextSchemeId,
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

  checkNameUniqueness(contextScheme: ContextScheme): Observable<any> {
    return this.http.post('/api/context_scheme/check_name_uniqueness', {
      contextSchemeId: contextScheme.contextSchemeId,
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

}
