import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Namespace, NamespaceList, NamespaceListRequest, SimpleNamespace} from './namespace';
import {SimpleModule} from '../../module-management/domain/module';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class NamespaceService {

  constructor(private http: HttpClient) {
  }

  getNamespaceList(request: NamespaceListRequest): Observable<PageResponse<NamespaceList>> {
    let params = new HttpParams()
      .set('libraryId', request.library.libraryId)
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
    if (request.standard.length > 0) {
      params = params.set('standard', request.standard.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.uri) {
      params = params.set('uri', request.filters.uri);
    }
    if (request.filters.prefix) {
      params = params.set('prefix', request.filters.prefix);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    return this.http.get<PageResponse<NamespaceList>>('/api/namespace_list', {params})
      .pipe(map((resp: PageResponse<NamespaceList>) => {
        resp.list.forEach(e => {
          e.lastUpdateTimestamp = new Date(e.lastUpdateTimestamp);
        });
        return resp;
      }));
  }

  getNamespace(namespaceId): Observable<Namespace> {
    return this.http.get<Namespace>('/api/namespace/' + namespaceId);
  }

  getSimpleNamespaces(libraryId: number): Observable<SimpleNamespace[]> {
    const params = new HttpParams().set('libraryId', libraryId);
    return this.http.get<SimpleNamespace[]>('/api/simple_namespaces', {params});
  }

  getSimpleModules(): Observable<SimpleModule[]> {
    return this.http.get<SimpleModule[]>('/api/simple_modules');
  }

  create(namespace: Namespace): Observable<any> {
    return this.http.put<any>('/api/namespace', {
      libraryId: namespace.libraryId,
      uri: namespace.uri,
      prefix: namespace.prefix,
      description: namespace.description
    });
  }

  update(namespace: Namespace): Observable<any> {
    return this.http.post<any>('/api/namespace/' + namespace.namespaceId, {
      uri: namespace.uri,
      prefix: namespace.prefix,
      description: namespace.description
    });
  }

  discard(namespaceId: number): Observable<any> {
    return this.http.delete<any>('/api/namespace/' + namespaceId);
  }

  transferOwnership(namespaceId: number, targetLoginId: string): Observable<any> {
    return this.http.post<any>('/api/namespace/' + namespaceId + '/transfer_ownership', {
      targetLoginId
    });
  }

}
