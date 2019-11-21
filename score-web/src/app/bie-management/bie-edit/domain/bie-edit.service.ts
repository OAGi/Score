import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BieEditAbieNode, BieEditCreateExtensionResponse, BieEditNode, BieEditNodeDetail, DynamicBieFlatNode} from './bie-edit-node';
import {CodeList} from '../../../code-list-management/domain/code-list';

@Injectable()
export class BieEditService {

  constructor(private http: HttpClient) {

  }

  getRootNode(topLevelAbieId): Observable<BieEditAbieNode> {
    return this.http.get<BieEditAbieNode>('/api/profile_bie/node/root/' + topLevelAbieId);
  }

  getChildren(node: DynamicBieFlatNode, hideUnused: boolean): Observable<BieEditNode[]> {
    const url = '/api/profile_bie/node/children/' + node.item.type;
    const params = node.toHttpParams()
      .set('hideUnused', (hideUnused) ? 'true' : 'false');
    return this.http.get<BieEditNode[]>(url, {params: params});
  }

  getDetail(node: DynamicBieFlatNode): Observable<BieEditNodeDetail> {
    const url = '/api/profile_bie/node/detail/' + node.item.type;
    return this.http.get<BieEditNodeDetail>(url, {params: node.toHttpParams()});
  }

  setState(topLevelAbieId: number, state: string): Observable<any> {
    return this.http.post('/api/profile_bie/node/root/' + topLevelAbieId + '/state', {
      state: state
    });
  }

  getPublishedCodeLists(): Observable<CodeList[]> {
    return this.http.get<CodeList[]>('/api/code_list_published');
  }

  updateDetails(topLevelAbidId: number, details: BieEditNodeDetail[]): Observable<any> {
    const body = {
      topLevelAbieId: topLevelAbidId,
      abieNodeDetail: undefined,
      asbiepNodeDetails: [],
      bbiepNodeDetails: [],
      bbieScNodeDetails: []
    };

    for (const detail of details) {
      switch (detail.type) {
        case 'abie':
          body.abieNodeDetail = detail;
          break;
        case 'asbiep':
          body.asbiepNodeDetails.push(detail);
          break;
        case 'bbiep':
          body.bbiepNodeDetails.push(detail);
          break;
        case 'bbie_sc':
          body.bbieScNodeDetails.push(detail);
          break;
      }
    }

    return this.http.post('/api/profile_bie/node/detail', body);
  }

  createLocalAbieExtension(node: BieEditNode): Observable<BieEditCreateExtensionResponse> {
    return this.http.put<BieEditCreateExtensionResponse>('/api/profile_bie/node/extension/local', node);
  }

  createGlobalAbieExtension(node: BieEditNode): Observable<BieEditCreateExtensionResponse> {
    return this.http.put<BieEditCreateExtensionResponse>('/api/profile_bie/node/extension/global', node);
  }
}
