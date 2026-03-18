import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import { Injectable, OnInit, inject } from '@angular/core';

@Injectable()
export class BieCopyService implements OnInit {
  private http = inject(HttpClient);


  ngOnInit() {
  }

  copy(topLevelAsbiepId: number, bizCtxIdList: number[]): Observable<any> {
    return this.http.post('/api/bies/' + topLevelAsbiepId + '/copy', {
      bizCtxIdList
    });
  }
}
