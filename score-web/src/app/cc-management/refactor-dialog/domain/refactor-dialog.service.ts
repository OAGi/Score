import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CcList} from '../../cc-list/domain/cc-list';

@Injectable()
export class RefactorDialogService {

  constructor(private http: HttpClient) {

  }

  baseAccList(manifestId: number): Observable<CcList[]> {
    return this.http.get<CcList[]>('/api/core_component/acc/' + manifestId + '/base_acc_list');
  }

  refactor(type: string, targetManifestId: number, destinationManifestId: number): Observable<any> {
    return this.http.post('/api/core_component/' + type + '/refactor', {
      type,
      targetManifestId,
      destinationManifestId
    });
  }

  validateRefactoring(type: string, targetManifestId: number, destinationManifestId: number): Observable<any> {
    const params = new HttpParams()
      .set('type', type)
      .set('targetManifestId', targetManifestId.toString())
      .set('destinationManifestId', destinationManifestId.toString());
    return this.http.get('/api/core_component/' + type + '/refactor', { params });
  }

}
