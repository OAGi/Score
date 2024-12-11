import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {
  AssignableMap,
  ReleaseDetail,
  ReleaseList,
  ReleaseListRequest,
  ReleaseResponse,
  ReleaseValidationRequest,
  ReleaseValidationResponse,
  SimpleRelease
} from './release';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class ReleaseService {

  constructor(private http: HttpClient) {
  }

  getSimpleReleases(libraryId: number, states?: string[]): Observable<SimpleRelease[]> {
    let params = new HttpParams().set('libraryId', libraryId);
    if (!!states && states.length > 0) {
      params = params.set('states', states.join(','));
    }
    return this.http.get<SimpleRelease[]>('/api/simple_releases', {params});
  }

  getSimpleRelease(libraryId: number, id): Observable<SimpleRelease> {
    return this.http.get<SimpleRelease>('/api/simple_release/' + id);
  }

  getReleaseList(libraryId: number): Observable<ReleaseList[]> {
    return this.http.get<ReleaseList[]>('/api/release_list');
  }

  getReleases(request: ReleaseListRequest): Observable<PageResponse<ReleaseList>> {
    let params = new HttpParams()
      .set('libraryId', '' + request.library.libraryId)
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    if (request.creatorLoginIds.length > 0) {
      params = params.set('creatorLoginIds', request.creatorLoginIds.join(','));
    }
    if (request.createdDate.start) {
      params = params.set('createStart', '' + request.createdDate.start.getTime());
    }
    if (request.createdDate.end) {
      params = params.set('createEnd', '' + request.createdDate.end.getTime());
    }
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.releaseNum) {
      params = params.set('releaseNum', request.filters.releaseNum);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.excludes.length > 0) {
      params = params.set('excludes', request.excludes.join(','));
    }
    if (request.namespaces && request.namespaces.length > 0) {
      params = params.set('namespaces', request.namespaces.map(e => '' + e).join(','));
    }
    return this.http.get<PageResponse<ReleaseList>>('/api/releases', {params});
  }

  createRelease(releaseDetail: ReleaseDetail): Observable<any> {
    return this.http.post<ReleaseResponse>('/api/release/create', releaseDetail);
  }

  updateRelease(releaseDetail: ReleaseDetail): Observable<any> {
    return this.http.post('/api/release/' + releaseDetail.releaseId, releaseDetail);
  }

  discard(releaseIds: number[]): Observable<any> {
    if (releaseIds.length === 0) {
      return;
    }

    if (releaseIds.length === 1) {
      return this.http.delete('/api/release/' + releaseIds[0]);
    } else {
      const params = new HttpParams()
        .set('releaseIds', releaseIds.join(','));
      return this.http.delete('/api/release', {params});
    }
  }

  getReleaseDetail(releaseId: string): Observable<ReleaseDetail> {
    return this.http.get<ReleaseDetail>('/api/release/' + releaseId);
  }

  getReleaseAssignable(releaseId: string): Observable<AssignableMap> {
    return this.http.get<AssignableMap>('/api/release/' + releaseId + '/assignable');
  }

  validate(releaseId: number, request: ReleaseValidationRequest): Observable<ReleaseValidationResponse> {
    return this.http.post<ReleaseValidationResponse>('/api/release/' + releaseId + '/validate', request);
  }

  makeDraft(releaseId: number, request: ReleaseValidationRequest): Observable<ReleaseValidationResponse> {
    return this.http.post<ReleaseValidationResponse>('/api/release/' + releaseId + '/draft', request);
  }

  updateState(releaseId: number, state: string): Observable<any> {
    return this.http.post<ReleaseValidationResponse>('/api/release/' + releaseId + '/state', {state});
  }

  generateMigrationScript(releaseId: number): Observable<HttpResponse<Blob>> {
    return this.http.get('/api/release/' + releaseId + '/generate_migration_script', {
      observe: 'response',
      responseType: 'blob'
    });
  }

}
