import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Library} from './library';

@Injectable()
export class LibraryService {

  constructor(private http: HttpClient) {
  }

  getLibraries(): Observable<Library[]> {
    return this.http.get<Library[]>('/api/libraries', {});
  }

}
