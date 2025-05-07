import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {
  AssignableMap,
  ReleaseDetails,
  ReleaseListEntry,
  ReleaseListRequest,
  ReleaseResponse,
  ReleaseSummary,
  ReleaseValidationRequest,
  ReleaseValidationResponse
} from './release';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class ReleaseService {

  constructor(private http: HttpClient) {
  }

  getReleaseSummaryList(libraryId: number, states?: string[]): Observable<ReleaseSummary[]> {
    let params = new HttpParams().set('libraryId', libraryId);
    if (!!states && states.length > 0) {
      params = params.set('releaseStates', states.join(','));
    }
    return this.http.get<ReleaseSummary[]>('/api/releases/summaries', {params});
  }

  getReleaseList(request: ReleaseListRequest): Observable<PageResponse<ReleaseListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId)
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.creatorLoginIdList.length > 0) {
      params = params.set('creatorLoginIdList', request.creatorLoginIdList.join(','));
    }
    if (!!request.createdDate.start || !!request.createdDate.end) {
      params = params.set('createdOn',
          '[' + (!!request.createdDate.start ? request.createdDate.start.getTime() : '') + '~' +
          (!!request.createdDate.end ? request.createdDate.end.getTime() : '') + ']');
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (!!request.updatedDate.start || !!request.updatedDate.end) {
      params = params.set('lastUpdatedOn',
          '[' + (!!request.updatedDate.start ? request.updatedDate.start.getTime() : '') + '~' +
          (!!request.updatedDate.end ? request.updatedDate.end.getTime() : '') + ']');
    }
    if (request.filters.releaseNum) {
      params = params.set('releaseNum', request.filters.releaseNum);
    }
    if (request.states.length > 0) {
      params = params.set('releaseStates', request.states.join(','));
    }
    if (request.excludes.length > 0) {
      params = params.set('excludeReleaseNums', request.excludes.join(','));
    }
    if (request.namespaces && request.namespaces.length > 0) {
      params = params.set('namespaceIds', request.namespaces.map(e => '' + e).join(','));
    }
    return this.http.get<PageResponse<ReleaseListEntry>>('/api/releases', {params}).pipe(
        map((res: PageResponse<ReleaseListEntry>) => ({
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

  getReleaseDetail(releaseId: string): Observable<ReleaseDetails> {
    return this.http.get<ReleaseDetails>('/api/releases/' + releaseId);
  }

  createRelease(releaseDetail: ReleaseDetails): Observable<any> {
    return this.http.post<ReleaseResponse>('/api/releases', {
      libraryId: releaseDetail.libraryId,
      releaseNum: releaseDetail.releaseNum,
      namespaceId: releaseDetail.namespaceId,
      releaseNote: releaseDetail.releaseNote,
      releaseLicense: releaseDetail.releaseLicense
    });
  }

  updateRelease(releaseDetail: ReleaseDetails): Observable<any> {
    return this.http.put('/api/releases/' + releaseDetail.releaseId, {
      releaseNum: releaseDetail.releaseNum,
      namespaceId: releaseDetail.namespaceId,
      releaseNote: releaseDetail.releaseNote,
      releaseLicense: releaseDetail.releaseLicense
    });
  }

  discard(releaseIds: number[]): Observable<any> {
    if (releaseIds.length === 0) {
      return;
    }

    if (releaseIds.length === 1) {
      return this.http.delete('/api/releases/' + releaseIds[0]);
    } else {
      const params = new HttpParams()
        .set('releaseIds', releaseIds.join(','));
      return this.http.delete('/api/releases', {params});
    }
  }

  getReleaseAssignable(releaseId: string): Observable<AssignableMap> {
    return this.http.get<AssignableMap>('/api/releases/' + releaseId + '/assignable');
  }

  validate(releaseId: number, request: ReleaseValidationRequest): Observable<ReleaseValidationResponse> {
    return this.http.post<ReleaseValidationResponse>('/api/releases/' + releaseId + '/validate', request);
  }

  makeDraft(releaseId: number, request: ReleaseValidationRequest): Observable<ReleaseValidationResponse> {
    return this.http.post<ReleaseValidationResponse>('/api/releases/' + releaseId + '/draft', request);
  }

  updateState(releaseId: number, state: string): Observable<any> {
    return this.http.post<ReleaseValidationResponse>('/api/releases/' + releaseId + '/state', {state});
  }

  generateMigrationScript(releaseId: number): Observable<HttpResponse<Blob>> {
    return this.http.get('/api/releases/' + releaseId + '/generate_migration_script', {
      observe: 'response',
      responseType: 'blob'
    });
  }

  getPlantUml(releaseId: number, options: {}): Observable<any> {
    let params = new HttpParams();
    if (!!options) {
      for (const key in options) {
        params = params.append(key, options[key]);
      }
    }
    return this.http.get('/api/releases/' + releaseId + '/plantuml', {
      params
    });
  }
}
