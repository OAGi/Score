import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {LibraryDetails, LibraryListEntry, LibraryListRequest, LibrarySummary} from './library';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class LibraryService {

  constructor(private http: HttpClient) {
  }

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
      description: library.description
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
      state: library.state
    });
  }

  discard(libraryId: number): Observable<any> {
    return this.http.delete<any>('/api/libraries/' + libraryId);
  }

  getLibraryList(request: LibraryListRequest): Observable<PageResponse<LibraryListEntry>> {
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
    if (request.filters.type) {
      params = params.set('type', request.filters.type);
    }
    if (request.filters.organization) {
      params = params.set('organization', request.filters.organization);
    }
    if (request.filters.domain) {
      params = params.set('domain', request.filters.domain);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    if (request.filters.state) {
      params = params.set('state', request.filters.state);
    }
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
