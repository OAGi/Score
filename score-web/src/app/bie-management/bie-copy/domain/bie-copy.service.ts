import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable, OnInit} from '@angular/core';

@Injectable()
export class BieCopyService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  copy(topLevelAsbiepId: number, bizCtxIdList: number[]): Observable<any> {
    return this.http.post('/api/bies/' + topLevelAsbiepId + '/copy', {
      bizCtxIdList
    });
  }
}
