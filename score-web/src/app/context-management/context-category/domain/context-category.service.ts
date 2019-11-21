import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ContextCategory, ContextCategoryListRequest} from './context-category';
import {ContextScheme} from '../../context-scheme/domain/context-scheme';
import {PageResponse} from '../../../basis/basis';

@Injectable()
export class ContextCategoryService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getContextCategoryList(request: ContextCategoryListRequest): Observable<PageResponse<ContextCategory>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }

    return this.http.get<PageResponse<ContextCategory>>('/api/context_categories', {params: params});
  }

  getContextSchemeFromCategoryId(id): Observable<ContextScheme[]> {
    return this.http.get<ContextScheme[]>('/api/context_schemes_from_ctg/' + id);
  }

  getContextCategory(id): Observable<ContextCategory> {
    return this.http.get<ContextCategory>('/api/context_category/' + id);
  }

  create(contextCategory: ContextCategory): Observable<any> {
    return this.http.put('/api/context_category', {
      'name': contextCategory.name,
      'description': contextCategory.description
    });
  }

  update(contextCategory: ContextCategory): Observable<any> {
    return this.http.post('/api/context_category/' + contextCategory.ctxCategoryId, {
      'name': contextCategory.name,
      'description': contextCategory.description
    });
  }

  delete(...ctxCategoryIds): Observable<any> {
    if (ctxCategoryIds.length === 1) {
      return this.http.delete('/api/context_category/' + ctxCategoryIds[0]);
    } else {
      return this.http.post<any>('/api/context_category/delete', {
        ctxCategoryIds: ctxCategoryIds
      });
    }
  }
}
