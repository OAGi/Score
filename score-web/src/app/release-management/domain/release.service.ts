import {HttpClient, HttpParams} from '@angular/common/http';
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

  getSimpleReleases(states?: string[]): Observable<SimpleRelease[]> {
    let params = new HttpParams();
    if (!!states && states.length > 0) {
      params = params.set('states', states.join(','));
    }
    return this.http.get<SimpleRelease[]>('/api/simple_releases', {params: params});
  }

  getSimpleRelease(id): Observable<SimpleRelease> {
    return this.http.get<SimpleRelease>('/api/simple_release/' + id);
  }

  getReleaseList(): Observable<ReleaseList[]> {
    return this.http.get<ReleaseList[]>('/api/release_list');
  }

  getReleases(request: ReleaseListRequest): Observable<PageResponse<ReleaseList>> {
    let params = new HttpParams()
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
    return this.http.get<PageResponse<ReleaseList>>('/api/releases', {params: params});
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
      return this.http.delete('/api/release', {params: params});
    }
  }

  getReleaseDetail(releaseId: string): Observable<ReleaseDetail> {
    return this.http.get<ReleaseDetail>('/api/release/' + releaseId);
  }

  getReleaseAssignable(releaseId: string): Observable<AssignableMap> {
    return this.http.get<AssignableMap>('/api/release/' + releaseId + '/assignable');
  }

  validate(request: ReleaseValidationRequest): Observable<ReleaseValidationResponse> {
    return this.http.post<ReleaseValidationResponse>('/api/release/validate', request);
  }

  makeDraft(releaseId: number, request: ReleaseValidationRequest): Observable<ReleaseValidationResponse> {
    return this.http.post<ReleaseValidationResponse>('/api/release/' + releaseId + '/draft', request);
  }

  updateState(releaseId: number, state: string): Observable<any> {
    return this.http.post<ReleaseValidationResponse>('/api/release/' + releaseId + '/state', {state});
  }
}
