import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CodeList, CodeListForList, CodeListForListRequest, SimpleAgencyIdListValue} from './code-list';
import {PageResponse} from '../../basis/basis';

@Injectable()
export class CodeListService {

  constructor(private http: HttpClient) {
  }

  getCodeListList(request: CodeListForListRequest): Observable<PageResponse<CodeListForList>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.extensible !== undefined) {
      params = params.set('extensible', (request.extensible) ? 'true' : 'false');
    }

    return this.http.get<PageResponse<CodeListForList>>('/api/code_list', {params: params});
  }

  getCodeList(id): Observable<CodeList> {
    return this.http.get<CodeList>('/api/code_list/' + id);
  }

  getCodeLists(): Observable<PageResponse<CodeList>> {
    const params = new HttpParams()
      .set('sortActive', '')
      .set('sortDirection', '')
      .set('pageIndex', '-1')
      .set('pageSize', '-1');

    return this.http.get<PageResponse<CodeList>>('/api/code_list', {params: params});
  }

  getSimpleAgencyIdListValues(): Observable<SimpleAgencyIdListValue[]> {
    return this.http.get<SimpleAgencyIdListValue[]>('/api/simple_agency_id_list_values');
  }

  create(codeList: CodeList): Observable<any> {
    return this.http.put('/api/code_list', {
      'basedCodeListId': codeList.basedCodeListId,
      'codeListName': codeList.codeListName,
      'listId': codeList.listId,
      'agencyId': codeList.agencyId,
      'versionId': codeList.versionId,
      'definition': codeList.definition,
      'definitionSource': codeList.definitionSource,
      'remark': codeList.remark,
      'extensible': codeList.extensible,
      'codeListValues': codeList.codeListValues
    });
  }

  update(codeList: CodeList, state?: string): Observable<any> {
    const body = {
      'basedCodeListId': codeList.basedCodeListId,
      'codeListName': codeList.codeListName,
      'listId': codeList.listId,
      'agencyId': codeList.agencyId,
      'versionId': codeList.versionId,
      'definition': codeList.definition,
      'definitionSource': codeList.definitionSource,
      'remark': codeList.remark,
      'extensible': codeList.extensible,
      'codeListValues': codeList.codeListValues
    };
    if (state) {
      body['state'] = state;
    }

    return this.http.post('/api/code_list/' + codeList.codeListId, body);
  }

  publish(codeList: CodeList): Observable<any> {
    return this.update(codeList, 'Published');
  }

  delete(...codeListIds): Observable<any> {
    if (codeListIds.length === 1) {
      return this.http.delete('/api/code_list/' + codeListIds[0]);
    } else {
      return this.http.post<any>('/api/code_list/delete', {
        codeListIds: codeListIds
      });
    }
  }
}
