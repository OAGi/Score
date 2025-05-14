import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CcListEntry} from '../../cc-list/domain/cc-list';
import {map} from 'rxjs/operators';
import {ValidateRefactoringResponse} from './refactor-dialog';

@Injectable()
export class RefactorDialogService {

  constructor(private http: HttpClient) {

  }

  baseAccList(manifestId: number): Observable<CcListEntry[]> {
    return this.http.get<CcListEntry[]>('/api/core-components/acc/' + manifestId + '/base-acc-list').pipe(
        map((res: CcListEntry[]) =>
            res.map(elm => ({
              ...elm,
              created: {
                ...elm.created,
                when: new Date(elm.created.when),
              },
              lastUpdated: {
                ...elm.lastUpdated,
                when: new Date(elm.lastUpdated.when),
              }
            }))
        )
    );
  }

  refactor(type: string, targetManifestId: number, destinationManifestId: number): Observable<any> {
    const params = new HttpParams()
        .set('targetManifestId', targetManifestId.toString())
        .set('destinationManifestId', destinationManifestId.toString());
    return this.http.post('/api/core-components/' + type.toLowerCase() + '/refactor', {}, {
      params
    });
  }

  validateRefactoring(type: string, targetManifestId: number, destinationManifestId: number): Observable<ValidateRefactoringResponse> {
    const params = new HttpParams()
      .set('targetManifestId', targetManifestId.toString())
      .set('destinationManifestId', destinationManifestId.toString());
    return this.http.get<ValidateRefactoringResponse>('/api/core-components/' + type.toLowerCase() + '/refactor', { params });
  }

}
