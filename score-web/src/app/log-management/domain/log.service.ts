import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageResponse} from '../../basis/basis';
import {CcSnapshot, Log, LogListRequest} from './log';

@Injectable()
export class LogService {

  constructor(private http: HttpClient) {
  }

  getRevisions(request: LogListRequest): Observable<PageResponse<Log>> {
    const params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    return this.http.get<PageResponse<Log>>('/api/logs/' + request.reference, {params});
  }

  getSnapshot(revisionId: number): Observable<CcSnapshot> {
    return this.http.get<CcSnapshot>('/api/logs/' + revisionId + '/snapshot');
  }
}
