import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable, OnInit} from '@angular/core';

@Injectable()
export class BieCreateService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  create(asccpManifestId: number, bizCtxIds: number[]): Observable<any> {
    return this.http.put('/api/profile_bie/create', {
      asccpManifestId: asccpManifestId,
      bizCtxIds: bizCtxIds
    });
  }
}
