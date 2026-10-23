import {HttpClient, HttpParams} from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {NamespaceDetails, NamespaceListEntry, NamespaceSummary} from './namespace';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class NamespaceService {
  private http = inject(HttpClient);

  getNamespaceListPage(params: HttpParams): Observable<PageResponse<NamespaceListEntry>> {
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
