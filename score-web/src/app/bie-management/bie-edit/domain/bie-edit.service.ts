import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CcGraph, DtAwdPriSummary, DtScAwdPriSummary} from '../../../cc-management/domain/core-component-node';
import {sha256} from '../../../common/utility';
import {BieDetailUpdateRequest, BieDetailUpdateResponse, BieEditCreateExtensionResponse} from '../../bie-edit/domain/bie-edit-node';
import {BieEditAbieNode, RefBie, UsedBie} from './bie-edit-node';
import {
  AbieDetails,
  AbieFlatNode,
  AsbieDetails,
  AsbiepDetails,
  AsbiepFlatNode,
  BbieDetails,
  BbiepDetails,
  BbieScDetails
} from '../../domain/bie-flat-tree';
import {CodeListSummary} from '../../../code-list-management/domain/code-list';
import {AgencyIdListSummary} from '../../../agency-id-list-management/domain/agency-id-list';
import {map} from 'rxjs/operators';

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

  getDtAwdPriList(dtManifestId: number): Observable<DtAwdPriSummary[]> {
    return this.http.get<DtAwdPriSummary[]>('/api/core-components/dt/' + dtManifestId + '/primitives');
  }

  getDtCodeList(dtManifestId: number): Observable<CodeListSummary[]> {
    return this.http.get<CodeListSummary[]>('/api/core-components/dt/' + dtManifestId + '/code-lists');
  }

  getDtAgencyIdList(dtManifestId: number): Observable<AgencyIdListSummary[]> {
    return this.http.get<AgencyIdListSummary[]>('/api/core-components/dt/' + dtManifestId + '/agency-id-lists');
  }

  getDtScAwdPriList(dtScManifestId: number): Observable<DtScAwdPriSummary[]> {
    return this.http.get<DtScAwdPriSummary[]>('/api/core-components/dt-sc/' + dtScManifestId + '/primitives');
  }

  getDtScCodeList(dtScManifestId: number): Observable<CodeListSummary[]> {
    return this.http.get<CodeListSummary[]>('/api/core-components/dt-sc/' + dtScManifestId + '/code-lists');
  }

  getDtScAgencyIdList(dtScManifestId: number): Observable<AgencyIdListSummary[]> {
    return this.http.get<AgencyIdListSummary[]>('/api/core-components/dt-sc/' + dtScManifestId + '/agency-id-lists');
  }

  getAbieDetails(bieId: number): Observable<AbieDetails> {
    const url = '/api/profile_bie/abie/' + bieId;
    return this.http.get<AbieDetails>(url, {}).pipe(
        map((elm: AbieDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getAbieDetailsByPath(topLevelAsbiepId: number, accManifestId: number, path: string): Observable<AbieDetails> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/abie/' + accManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<AbieDetails>(url, {params}).pipe(
        map((elm: AbieDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getAsbieDetails(bieId: number): Observable<AsbieDetails> {
    const url = '/api/profile_bie/asbie/' + bieId;
    return this.http.get<AsbieDetails>(url, {}).pipe(
        map((elm: AsbieDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getAsbieDetailsByPath(topLevelAsbiepId: number, asccManifestId: number, path: string): Observable<AsbieDetails> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/asbie/' + asccManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<AsbieDetails>(url, {params}).pipe(
        map((elm: AsbieDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getBbieDetails(bieId: number): Observable<BbieDetails> {
    const url = '/api/profile_bie/bbie/' + bieId;
    return this.http.get<BbieDetails>(url, {}).pipe(
        map((elm: BbieDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getBbieDetailsByPath(topLevelAsbiepId: number, bccpManifestId: number, path: string): Observable<BbieDetails> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/bbie/' + bccpManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<BbieDetails>(url, {params}).pipe(
        map((elm: BbieDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getAsbiepDetails(bieId: number): Observable<AsbiepDetails> {
    const url = '/api/profile_bie/asbiep/' + bieId;
    return this.http.get<AsbiepDetails>(url, {}).pipe(
        map((elm: AsbiepDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getAsbiepDetailsByPath(topLevelAsbiepId: number, asccpManifestId: number, path: string): Observable<AsbiepDetails> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/asbiep/' + asccpManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<AsbiepDetails>(url, {params}).pipe(
        map((elm: AsbiepDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getBbiepDetails(bieId: number): Observable<BbiepDetails> {
    const url = '/api/profile_bie/bbiep/' + bieId;
    return this.http.get<BbiepDetails>(url, {}).pipe(
        map((elm: BbiepDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getBbiepDetailsByPath(topLevelAsbiepId: number, bccpManifestId: number, path: string): Observable<BbiepDetails> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/bbiep/' + bccpManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<BbiepDetails>(url, {params}).pipe(
        map((elm: BbiepDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getBbieScDetails(bieId: number): Observable<BbieScDetails> {
    const url = '/api/profile_bie/bbie_sc/' + bieId;
    return this.http.get<BbieScDetails>(url, {}).pipe(
        map((elm: BbieScDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
  }

  getBbieScDetailsByPath(topLevelAsbiepId: number, dtScManifestId: number, path: string): Observable<BbieScDetails> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/bbie_sc/' + dtScManifestId;
    let params;
    if (path.length > 0) {
      params = new HttpParams().set('hashPath', sha256(path));
    }
    return this.http.get<BbieScDetails>(url, {params}).pipe(
        map((elm: BbieScDetails) => ({
          ...elm,
          created: {
            ...elm.created,
            when: new Date(elm.created?.when),
          },
          lastUpdated: {
            ...elm.lastUpdated,
            when: new Date(elm.lastUpdated?.when),
          }
        }))
    );
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
      asccManifestId: asbiepNode.asccNode.manifestId,
      accManifestId: asbiepNode.accNode.manifestId,
      asbiePath: asbiepNode.asbiePath,
      asbieHashPath: asbiepNode.asbieHashPath,
      fromAbiePath: asbiepNode.abiePath,
      fromAbieHashPath: asbiepNode.abieHashPath,
      reuseTopLevelAsbiepId
    });
  }

  retainReusedBIE(topLevelAsbiepId: number, asbieHashPath: string): Observable<any> {
    const url = '/api/profile_bie/' + topLevelAsbiepId + '/retain_reuse';
    return this.http.post<any>(url, {asbieHashPath});
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

  useBaseBIE(abieNode: AbieFlatNode, baseTopLevelAsbiepId: number): Observable<any> {
    const url = '/api/profile_bie/' + abieNode.topLevelAsbiepId + '/use_base';
    return this.http.post<any>(url, {
      baseTopLevelAsbiepId
    });
  }

  removeBaseBIE(abieNode: AbieFlatNode): Observable<any> {
    const url = '/api/profile_bie/' + abieNode.topLevelAsbiepId + '/remove_base';
    return this.http.post<any>(url, {});
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
    return this.http.post<any>(url, {bieType: type, path});
  }

  deprecate(topLevelAsbiepId: number, reason: string, remark: string): Observable<any> {
    return this.http.post<any>('/api/profile_bie/' + topLevelAsbiepId + '/deprecate', {
      reason,
      remark
    });
  }
}
