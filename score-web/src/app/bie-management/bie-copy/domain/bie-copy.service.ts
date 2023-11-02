import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable, OnInit} from '@angular/core';

@Injectable()
export class BieCopyService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  copy(topLevelAsbiepId: number, bizCtxIds: number[]): Observable<any> {
    return this.http.put('/api/profile_bie/copy', {
      topLevelAsbiepId,
      bizCtxIds
    });
  }
}
