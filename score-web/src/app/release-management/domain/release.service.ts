import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable, OnInit} from '@angular/core';
import {ReleaseList, SimpleRelease} from './release';

@Injectable()
export class ReleaseService implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  getSimpleReleases(): Observable<SimpleRelease[]> {
    return this.http.get<SimpleRelease[]>('/api/simple_releases');
  }

  getSimpleRelease(id): Observable<SimpleRelease> {
    return this.http.get<SimpleRelease>('/api/simple_release/' + id);
  }

  getReleaseList(): Observable<ReleaseList[]> {
    return this.http.get<ReleaseList[]>('/api/release_list');
  }
}
