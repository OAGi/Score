import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ContextCategoryDetails, ContextCategoryListEntry, ContextCategoryListRequest, ContextCategorySummary} from './context-category';
import {ContextScheme} from '../../context-scheme/domain/context-scheme';
import {PageResponse} from '../../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class ContextCategoryService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getContextCategorySummaries(): Observable<ContextCategorySummary[]> {
    return this.http.get<ContextCategorySummary[]>('/api/context-categories/summaries');
  }

  getContextCategoryList(request: ContextCategoryListRequest): Observable<PageResponse<ContextCategoryListEntry>> {
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
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }

    return this.http.get<PageResponse<ContextCategoryListEntry>>('/api/context-categories', {params}).pipe(
        map((res: PageResponse<ContextCategoryListEntry>) => ({
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

  getContextSchemeFromCategoryId(id): Observable<ContextScheme[]> {
    return this.http.get<ContextScheme[]>('/api/context-categories/' + id + '/context-schemes/summaries');
  }

  getContextCategoryDetails(id): Observable<ContextCategoryDetails> {
    return this.http.get<ContextCategoryDetails>('/api/context-categories/' + id);
  }

  create(name: string, description: string): Observable<any> {
    return this.http.post('/api/context-categories', {
      name,
      description
    });
  }

  update(contextCategory: ContextCategoryDetails): Observable<any> {
    return this.http.put('/api/context-categories/' + contextCategory.contextCategoryId, {
      name: contextCategory.name,
      description: contextCategory.description
    });
  }

  delete(...contextCategoryIds): Observable<any> {
    if (contextCategoryIds.length === 1) {
      return this.http.delete('/api/context-categories/' + contextCategoryIds[0]);
    } else {
      return this.http.delete('/api/context-categories', {
        body: {
          contextCategoryIdList: contextCategoryIds
        }
      });
    }
  }
}
