import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable, OnInit} from '@angular/core';
import {BieCreateResponse} from './bie-create-list';

@Injectable()
export class BieCreateService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  create(asccpManifestId: number, bizCtxIds: number[]): Observable<any> {
    return this.http.put('/api/profile_bie/create', {
      asccpManifestId,
      bizCtxIds
    });
  }

  createInheritedBie(basedTopLevelAsbiepId: number): Observable<any> {
    return this.http.post<BieCreateResponse>('/api/profile_bie/' + basedTopLevelAsbiepId + '/create_inherited_bie', {});
  }

}
