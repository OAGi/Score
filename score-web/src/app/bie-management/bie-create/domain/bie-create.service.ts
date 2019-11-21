import {BieCreateList} from './bie-create-list';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable, OnInit} from '@angular/core';
import {CcListRequest} from '../../../cc-management/cc-list/domain/cc-list';

@Injectable()
export class BieCreateService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getBieCreateList(releaseId: number, request: CcListRequest): Observable<BieCreateList[]> {
    return this.http.get<BieCreateList[]>('/api/profile_bie/asccp/release/' + releaseId);
  }

  create(asccpId: number, releaseId: number, bizCtxIds: number[]): Observable<any> {
    return this.http.put('/api/profile_bie/create', {
      asccpId: asccpId,
      releaseId: releaseId,
      bizCtxIds: bizCtxIds
    });
  }
}
