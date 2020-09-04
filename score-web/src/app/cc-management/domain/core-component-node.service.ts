import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {base64Encode} from '../../common/utility';
import {CcAccNode, CcAsccpNode, CcAsccpNodeDetail, CcBccpNode, CcNode, CcNodeDetail} from './core-component-node';
import {Acc} from '../cc-list/domain/cc-list';

@Injectable()
export class CcNodeService {

  constructor(private http: HttpClient) {

  }

  getAccNode(accId, releaseId): Observable<CcAccNode> {
    return this.http.get<CcAccNode>('/api/core_component/node/acc/' + (releaseId ? releaseId : 0) + '/' + accId);
  }

  getExtensionNode(extensionId, releaseId): Observable<CcAccNode> {
    return this.http.get<CcAccNode>('/api/core_component/node/extension/' + (releaseId ? releaseId : 0) + '/' + extensionId);
  }

  getAcc(accId): Observable<Acc> {
    return this.http.get<Acc>('/api/core_component/acc/' + accId);
  }

  getAccNode2(accId): Observable<CcAccNode> {
    return this.http.get<CcAccNode>('/api/core_component/node/acc/' + accId);
  }

  getLastAcc(): Observable<String> {
    return this.http.get<String>('api/core_component/acc_id');
  }

  createAcc(): Observable<CcAccNode> {
    return this.http.put<CcAccNode>('/api/core_component/acc', {});
  }

  update(accNode: CcAccNode): Observable<any> {
    return this.http.post('/api/core_component/acc/' + accNode.accId, {
      'objectClassTerm': accNode.objectClassTerm,
      'deprecated': accNode.deprecated,
      'abstract': accNode.abstracted,
      'definition': accNode.definition,
      'oagisComponentType': accNode.oagisComponentType
    });
  }

  getLastAsccp(): Observable<String> {
    return this.http.get<String>('api/core_component/asccp_id');
  }

  getLastBccp(): Observable<String> {
    return this.http.get<String>('api/core_component/bccp_id');
  }

  createAsccp(asccpId: number, roleOfAccId: number, seqKey: number): Observable<any> {
    return this.http.put('api/core_component/asccp/create', {
      asccpId: asccpId,
      roleOfAccId: roleOfAccId,
      seqKey: seqKey
    });
  }

  updateAsccp(asccp: CcAsccpNodeDetail): Observable<any> {
    return this.http.post('api/core_component/asccp/' + asccp.asccp.asccpId, {
      'definition': asccp.asccp.definition,
      'deprecated': asccp.asccp.deprecated,
      'den': asccp.asccp.den,
      'reusable': asccp.asccp.reusable,
      'propertyTerm': asccp.asccp.propertyTerm
    });
  }

  getAsccp(asccpId): Observable<CcAsccpNodeDetail> {
    return this.http.get<CcAsccpNodeDetail>('/api/core_component/asccp/' + asccpId);
  }

  getAsccpNode(asccpId, releaseId): Observable<CcAsccpNode> {
    return this.http.get<CcAsccpNode>('/api/core_component/node/asccp/' + (releaseId ? releaseId : 0) + '/' + asccpId);
  }

  getBccpNode(bccpId, releaseId): Observable<CcBccpNode> {
    return this.http.get<CcBccpNode>('/api/core_component/node/bccp/' + (releaseId ? releaseId : 0) + '/' + bccpId);
  }

  getChildren(node: CcNode, releaseId): Observable<CcNode[]> {
    const url = '/api/core_component/node/children/' + node.type + '/' + (releaseId ? releaseId : 0);
    return this.http.get<CcNode[]>(url, {params: this.toHttpParams(node)});
  }

  getDetail(node: CcNode, releaseId): Observable<CcNodeDetail> {
    const url = '/api/core_component/node/detail/' + node.type + '/' + (releaseId ? releaseId : 0);
    return this.http.get<CcNodeDetail>(url, {params: this.toHttpParams(node)});
  }

  toHttpParams(obj: Object): HttpParams {
    const params: HttpParams = Object.getOwnPropertyNames(obj)
      .reduce((p, key) => {
        if (key !== 'name' && key !== 'hasChild') {
          return p.set(key, obj[key]);
        }
        return p;
      }, new HttpParams());
    const data = base64Encode(params.toString());
    return new HttpParams().set('data', data);
  }

  appendAscc(asccId: number, releaseId: number, accId: number): Observable<any> {
    return this.http.post('/api/core_component/ascc/' + (releaseId ? releaseId : 0) + '/' + accId, {
      'action': 'append',
      'type': 'ascc',
      'id': asccId
    });
  }

  updateDetails(topLevelId: number, details: CcNodeDetail[]): Observable<any> {
    const body = {
      topLevelId: topLevelId,
      accNodeDetails: undefined,
      asccpNodeDetails: [],
      bccpNodeDetails: [],
      bdtScNodeDetails: []
    };

    for (const detail of details) {
      switch (detail.type) {
        case 'acc':
          body.accNodeDetails = detail;
          break;
        case 'bccp':
          body.bccpNodeDetails.push(detail);
          break;
        case 'asccp':
          body.asccpNodeDetails.push(detail);
          break;
        case 'bdt_sc':
          body.bdtScNodeDetails.push(detail);
          break;
      }
    }

    return this.http.post('/api/core_component/node/detail', body);
  }
}
