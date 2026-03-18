import { Injectable, inject } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {FindUsagesResponse} from './find-usages-dialog';

@Injectable()
export class FindUsagesDialogService {
  private http = inject(HttpClient);


  findUsages(type: string, manifestId: number): Observable<FindUsagesResponse> {
    return this.http.get<FindUsagesResponse>('/api/graphs/find_usages/' + type.toLowerCase() + '/' + manifestId);
  }

}
