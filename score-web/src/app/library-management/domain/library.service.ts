import {HttpClient, HttpContext, HttpParams} from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {
  DiscardLibraryCheck,
  LibraryDetails,
  LibraryListEntry,
  LibraryReleaseDependenciesResponse,
  LibrarySummary
} from './library';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';
import {SUPPRESS_ERROR_ALERT} from '../../authentication/auth.service';

@Injectable()
export class LibraryService {
  private http = inject(HttpClient);

  getLibrarySummaryList(): Observable<LibrarySummary[]> {
    return this.http.get<LibrarySummary[]>('/api/libraries/summaries', {});
  }

  getLibraryById(libraryId: number): Observable<LibraryDetails> {
    return this.http.get<LibraryDetails>('/api/libraries/' + libraryId, {});
  }

  create(library: LibraryDetails): Observable<any> {
    return this.http.post<any>('/api/libraries', {
      name: library.name,
      type: library.type,
      organization: library.organization,
      link: library.link,
      domain: library.domain,
      description: library.description,
      namespaceUri: library.namespaceUri,
      namespacePrefix: library.namespacePrefix
    });
  }

  update(library: LibraryDetails): Observable<any> {
    return this.http.put<any>('/api/libraries/' + library.libraryId, {
      name: library.name,
      type: library.type,
      organization: library.organization,
      link: library.link,
      domain: library.domain,
      description: library.description,
      state: library.state,
      isDefault: library.isDefault
    });
  }

  discard(libraryId: number): Observable<any> {
    return this.http.delete<any>('/api/libraries/' + libraryId);
  }

  checkDiscard(libraryId: number): Observable<DiscardLibraryCheck> {
    return this.http.get<DiscardLibraryCheck>('/api/libraries/' + libraryId + '/discard-check');
  }

  getReleaseDependencies(libraryId: number): Observable<LibraryReleaseDependenciesResponse> {
    return this.http.get<LibraryReleaseDependenciesResponse>('/api/libraries/' + libraryId + '/release-dependencies');
  }

  updateReleaseDependencies(libraryId: number, releaseIds: number[]): Observable<any> {
    return this.http.put('/api/libraries/' + libraryId + '/release-dependencies', {
      releaseIds
    }, {
      context: new HttpContext().set(SUPPRESS_ERROR_ALERT, true)
    });
  }

  getLibraryList(params: HttpParams): Observable<PageResponse<LibraryListEntry>> {
    return this.http.get<PageResponse<LibraryListEntry>>('/api/libraries', {params}).pipe(
        map((res: PageResponse<LibraryListEntry>) => ({
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

}
