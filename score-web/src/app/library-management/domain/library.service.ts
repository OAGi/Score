import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Library, LibraryList, LibraryListRequest} from './library';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class LibraryService {

  constructor(private http: HttpClient) {
  }

  getLibraries(): Observable<Library[]> {
    return this.http.get<Library[]>('/api/libraries', {});
  }

  getLibraryById(libraryId: number): Observable<Library> {
    return this.http.get<Library>('/api/library/' + libraryId, {});
  }

  create(library: Library): Observable<any> {
    return this.http.put<any>('/api/library', {
      name: library.name,
      organization: library.organization,
      link: library.link,
      domain: library.domain,
      description: library.description,
      enabled: false
    });
  }

  update(library: Library): Observable<any> {
    return this.http.post<any>('/api/library/' + library.libraryId, {
      name: library.name,
      organization: library.organization,
      link: library.link,
      domain: library.domain,
      description: library.description,
      enabled: library.enabled
    });
  }

  discard(libraryId: number): Observable<any> {
    return this.http.delete<any>('/api/library/' + libraryId);
  }

  getLibraryList(request: LibraryListRequest): Observable<PageResponse<LibraryList>> {
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
    if (request.filters.organization) {
      params = params.set('organization', request.filters.organization);
    }
    if (request.filters.domain) {
      params = params.set('domain', request.filters.domain);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    if (request.filters.status) {
      params = params.set('status', request.filters.status.join(','));
    }
    return this.http.get<PageResponse<LibraryList>>('/api/library_list', {params})
        .pipe(map((resp: PageResponse<LibraryList>) => {
          resp.list.forEach(e => {
            e.lastUpdateTimestamp = new Date(e.lastUpdateTimestamp);
          });
          return resp;
        }));
  }

}
