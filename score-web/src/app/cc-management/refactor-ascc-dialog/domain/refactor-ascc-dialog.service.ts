import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CcList} from '../../cc-list/domain/cc-list';
import {FindUsagesResponse} from './refactor-ascc-dialog';

@Injectable()
export class RefactorAsccDialogService {

  constructor(private http: HttpClient) {

  }

  baseAccList(manifestId: number): Observable<CcList[]> {
    return this.http.get<CcList[]>('/api/core_component/acc/' + manifestId + '/base_acc_list');
  }

}
