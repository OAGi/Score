import {HttpClient} from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {ReuseReport} from './bie-report';

@Injectable()
export class BieReportService {
  private http = inject(HttpClient);


  getBieReuseReport(topLevelAsbiepId?: number): Observable<ReuseReport[]> {
    if (topLevelAsbiepId !== undefined) {
      return this.http.get<ReuseReport[]>('/api/profile_bie/reuse_report/' + topLevelAsbiepId);
    } else {
      return this.http.get<ReuseReport[]>('/api/profile_bie/reuse_report');
    }
  }
}
