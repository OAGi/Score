import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CcGraph} from '../../../cc-management/domain/core-component-node';
import {sha256} from '../../../common/utility';
import {
  AgencyIdList,
  BdtPriRestri,
  BdtScPriRestri,
  BieDetailUpdateRequest,
  BieDetailUpdateResponse,
  BieEditCreateExtensionResponse,
  CodeList
} from '../../bie-edit/domain/bie-edit-node';
import {BieEditAbieNode, RefBie, UsedBie} from './bie-edit-node';
import {AsbiepFlatNode, BieEditNodeDetail} from '../../domain/bie-flat-tree';

@Injectable()
export class BieEditService {

  constructor(private http: HttpClient) {

  }

  getGraphNode(topLevelAsbiepId: number): Observable<CcGraph> {
    return this.http.get<CcGraph>('/api/graphs/top_level_asbiep/' + topLevelAsbiepId);
  }

  getRootNode(topLevelAsbiepId): Observable<BieEditAbieNode> {
    return this.http.get<BieEditAbieNode>('/api/profile_bie/node/root/' + topLevelAsbiepId);
  }

  getUsedBieList(topLevelAsbiepId: number): Observable<UsedBie[]> {
    return this.http.get<UsedBie[]>('/api/profile_bie/' + topLevelAsbiepId + '/used_list');
  }

  getRefBieList(topLevelAsbiepId: number): Observable<RefBie[]> {
    return this.http.get<RefBie[]>('/api/profile_bie/' + topLevelAsbiepId + '/ref_list');
  }

  getBbiepBdtPriRestriList(topLevelAsbiepId: number, manifestId: number): Observable<BdtPriRestri[]> {
    return this.http.get<BdtPriRestri[]>('/api/profile_bie/' + topLevelAsbiepId + '/bbiep/' + manifestId + '/bdt_pri_restri');
  }

  getBbiepCodeList(topLevelAsbiepId: number, manifestId: number): Observable<CodeList[]> {
    return this.http.get<CodeList[]>('/api/profile_bie/' + topLevelAsbiepId + '/bbiep/' + manifestId + '/code_list');
  }

  getBbiepAgencyIdList(topLevelAsbiepId: number, manifestId: number): Observable<AgencyIdList[]> {
    return this.http.get<AgencyIdList[]>('/api/profile_bie/' + topLevelAsbiepId + '/bbiep/' + manifestId + '/agency_id_list');
  }

  getBbieScBdtScPriRestriList(topLevelAsbiepId: number, manifestId: number): Observable<BdtScPriRestri[]> {
    return this.http.get<BdtScPriRestri[]>('/api/profile_bie/' + topLevelAsbiepId + '/bbie_sc/' + manifestId + '/bdt_sc_pri_restri');
  }

  getBbieScCodeList(topLevelAsbiepId: number, manifestId: number): Observable<CodeList[]> {
    return this.http.get<CodeList[]>('/api/profile_bie/' + topLevelAsbiepId + '/bbie_sc/' + manifestId + '/code_list');
  }

  getBbieScAgencyIdList(topLevelAsbiepId: number, manifestId: number): Observable<AgencyIdList[]> {
    return this.http.get<AgencyIdList[]>('/api/profile_bie/' + topLevelAsbiepId + '/bbie_sc/' + manifestId + '/agency_id_list');
  }

  getDetail(topLevelAsbiepId: number, bieType: string, ccManifestId: number, path: string): Observable<BieEditNodeDetail> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/' + bieType.toLowerCase() + '/' + ccManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<BieEditNodeDetail>(url, {params});
  }

  updateDetails(topLevelAsbiepId: number, request: BieDetailUpdateRequest): Observable<BieDetailUpdateResponse> {
    return this.http.post<BieDetailUpdateResponse>('/api/profile_bie/' + topLevelAsbiepId + '/detail', request.json);
  }

  setState(topLevelAsbiepId: number, state: string): Observable<any> {
    return this.http.post('/api/profile_bie/node/root/' + topLevelAsbiepId + '/state', {
      state
    });
  }

  reuseBIE(asbiepNode: AsbiepFlatNode, reuseTopLevelAsbiepId: number): Observable<any> {
    const url = '/api/profile_bie/' + asbiepNode.topLevelAsbiepId + '/asbiep/' + asbiepNode.asccpNode.manifestId + '/reuse';
    return this.http.post<any>(url, {
      asbieHashPath: asbiepNode.asbieHashPath,
      reuseTopLevelAsbiepId
    });
  }

  removeReusedBIE(topLevelAsbiepId: number, asbieHashPath: string): Observable<any> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/remove_reuse';
    return this.http.post<any>(url, {asbieHashPath});
  }

  makeReusableBIE(asbieHashPath: string, topLevelAsbiepId: number, asccpManifestId: number): Observable<any> {
    const url = '/api/profile_bie/node/create_bie_from_existing_bie';
    return this.http.post<any>(url, {
      asbieHashPath,
      topLevelAsbiepId,
      asccpManifestId
    });
  }

  createLocalAbieExtension(node: AsbiepFlatNode): Observable<BieEditCreateExtensionResponse> {
    const params = {
      accManifestId: node.accNode.manifestId,
      asccManifestId: node.asccNode.manifestId,
      asccpManifestId: node.asccpNode.manifestId,
      releaseId: 0,
      abieId: 0,
      asbiepId: 0,
      asbieId: 0,
      topLevelAsbiepId: node.topLevelAsbiepId
    };
    return this.http.put<BieEditCreateExtensionResponse>('/api/profile_bie/node/extension/local', params);
  }

  createGlobalAbieExtension(node: AsbiepFlatNode): Observable<BieEditCreateExtensionResponse> {
    const params = {
      accManifestId: node.accNode.manifestId,
      asccManifestId: node.asccNode.manifestId,
      asccpManifestId: node.asccpNode.manifestId,
      releaseId: 0,
      abieId: 0,
      asbiepId: 0,
      asbieId: 0,
      topLevelAsbiepId: node.topLevelAsbiepId
    };
    return this.http.put<BieEditCreateExtensionResponse>('/api/profile_bie/node/extension/global', params);
  }

  resetDetails(topLevelAsbiepId: number, type: string, path: string): Observable<any> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/reset_detail';
    return this.http.post<any>(url, {bieType: type, path: path});
  }
}
