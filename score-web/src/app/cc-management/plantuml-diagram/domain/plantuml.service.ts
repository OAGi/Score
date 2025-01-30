import {Injectable} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class PlantUmlService {

  constructor(private http: HttpClient) {

  }

  getDiagram(encodedText: string, format?: string): Observable<HttpResponse<Blob>> {
    format = format || 'svg';
    return this.http.get('/api/plantuml/' + format + '/' + encodedText, {
      observe: 'response',
      responseType: 'blob'
    });
  }
}