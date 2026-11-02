import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import { Injectable, inject } from '@angular/core';
import {
  AssignableMap,
  ReleaseDetails,
  ReleaseListEntry,
  ReleaseResponse,
  ReleaseSummary,
  ReleaseValidationRequest,
  ReleaseValidationResponse
} from './release';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';

@Injectable()
export class ReleaseService {
  private http = inject(HttpClient);

  getReleaseSummaryList(libraryId: number, states?: string[]): Observable<ReleaseSummary[]> {
    let params = new HttpParams().set('libraryId', libraryId);
    if (!!states && states.length > 0) {
      params = params.set('releaseStates', states.join(','));
    }
    return this.http.get<ReleaseSummary[]>('/api/releases/summaries', {params});
  }

  getWorkingRelease(libraryId: number): Observable<ReleaseSummary> {
    const params = new HttpParams().set('libraryId', libraryId);
    return this.http.get<ReleaseSummary>('/api/releases/working', {params});
  }

  getReleaseListPage(params: HttpParams): Observable<PageResponse<ReleaseListEntry>> {
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

  generateMigrationScript(releaseId: number, includeViewOrder = false): Observable<HttpResponse<Blob>> {
    let params = new HttpParams();
    if (includeViewOrder) {
      params = params.set('includeViewOrder', 'true');
    }
    return this.http.get('/api/releases/' + releaseId + '/generate_migration_script', {
      params,
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
