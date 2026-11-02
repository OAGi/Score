import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageResponse} from '../../basis/basis';
import {CcSnapshot, ComponentChangeSummary, Log} from './log';
import {map} from 'rxjs/operators';
import {CcListEntry} from '../../cc-management/cc-list/domain/cc-list';

@Injectable()
export class LogService {
  private http = inject(HttpClient);


  getRevisions(reference: string, params: HttpParams): Observable<PageResponse<Log>> {
    return this.http.get<PageResponse<Log>>('/api/logs/' + reference, {params}).pipe(
        map((res: PageResponse<Log>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            timestamp: new Date(elm.timestamp)
          }))
        }))
    );
  }

  getSnapshot(revisionId: number): Observable<CcSnapshot> {
    return this.http.get<CcSnapshot>('/api/logs/' + revisionId + '/snapshot');
  }

  getChangeSummary(ccType: string, manifestId: number): Observable<ComponentChangeSummary> {
    const params = new HttpParams()
      .set('ccType', ccType)
      .set('manifestId', '' + manifestId);
    return this.http.get<ComponentChangeSummary>('/api/logs/change-summary', {params});
  }

  getChangeSummaryByCompare(beforeLogId: number, afterLogId: number): Observable<ComponentChangeSummary> {
    const params = new HttpParams()
      .set('before', '' + beforeLogId)
      .set('after', '' + afterLogId);
    return this.http.get<ComponentChangeSummary>('/api/logs/change-summary/compare', {params});
  }
}
