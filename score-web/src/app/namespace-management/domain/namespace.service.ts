import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {NamespaceDetails, NamespaceListEntry, NamespaceListRequest, NamespaceSummary} from './namespace';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class NamespaceService {

  constructor(private http: HttpClient) {
  }

  getNamespaceList(request: NamespaceListRequest): Observable<PageResponse<NamespaceListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId)
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.standard.length > 0) {
      params = params.set('standard', request.standard.join(','));
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
    if (request.ownerLoginIdList.length > 0) {
      params = params.set('ownerLoginIdList', request.ownerLoginIdList.join(','));
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (!!request.updatedDate.start || !!request.updatedDate.end) {
      params = params.set('lastUpdatedOn',
          '[' + (!!request.updatedDate.start ? request.updatedDate.start.getTime() : '') + '~' +
          (!!request.updatedDate.end ? request.updatedDate.end.getTime() : '') + ']');
    }
    return this.http.get<PageResponse<NamespaceListEntry>>('/api/namespaces', {params}).pipe(
        map((res: PageResponse<NamespaceListEntry>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            lastUpdated: {
              ...elm.lastUpdated,
              when: new Date(elm.lastUpdated.when),
            }
          }))
        }))
    );
  }

  getNamespaceDetails(namespaceId): Observable<NamespaceDetails> {
    return this.http.get<NamespaceDetails>('/api/namespaces/' + namespaceId);
  }

  getNamespaceSummaries(libraryId: number): Observable<NamespaceSummary[]> {
    const params = new HttpParams().set('libraryId', libraryId);
    return this.http.get<NamespaceSummary[]>('/api/namespaces/summaries', {params});
  }

  create(namespace: NamespaceDetails): Observable<any> {
    return this.http.post<any>('/api/namespaces', {
      libraryId: namespace.libraryId,
      uri: namespace.uri,
      prefix: namespace.prefix,
      description: namespace.description
    });
  }

  update(namespace: NamespaceDetails): Observable<any> {
    return this.http.put<any>('/api/namespaces/' + namespace.namespaceId, {
      uri: namespace.uri,
      prefix: namespace.prefix,
      description: namespace.description
    });
  }

  discard(namespaceId: number): Observable<any> {
    return this.http.delete<any>('/api/namespaces/' + namespaceId);
  }

  transferOwnership(namespaceId: number, targetLoginId: string): Observable<any> {
    return this.http.patch<any>('/api/namespaces/' + namespaceId + '/transfer', {
      targetLoginId
    });
  }

}
