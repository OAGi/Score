import {Injectable} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AccFlatNode, AsccpFlatNode, BccpFlatNode, CcFlatNode, DtFlatNode, DtScFlatNode} from './cc-flat-tree';
import {
  BodCreateResponse,
  CcAccNode,
  CcAccNodeDetail,
  CcAsccpNode,
  CcAsccpNodeDetail,
  CcBccpNode,
  CcBccpNodeDetail,
  CcBdtNode,
  CcBdtScNodeDetail,
  CcCreateResponse,
  CcDtNodeDetail,
  CcGraph,
  CcNodeDetail,
  CcNodeUpdateResponse,
  CcRevisionResponse,
  CcSeqUpdateRequest,
  Comment,
  VerifyAppendAssociationResponse
} from './core-component-node';
import {base64Encode} from '../../common/utility';
import {map} from 'rxjs/operators';

@Injectable()
export class CcNodeService {

  constructor(private http: HttpClient) {

  }

  getAccNode(manifestId): Observable<CcAccNode> {
    return this.http.get<CcAccNode>('/api/core_component/acc/' + manifestId);
  }

  getExtensionNode(manifestId: number): Observable<CcAccNode> {
    return this.http.get<CcAccNode>('/api/core_component/node/extension/' + manifestId);
  }

  createAcc(releaseId: number): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core_component/acc', {
      releaseId
    });
  }

  createAsccp(releaseId: number, accManifestId: number, initialPropertyTerm: string, asccpType?: string): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core_component/asccp', {
      releaseId,
      roleOfAccManifestId: accManifestId,
      asccpType,
      initialPropertyTerm
    });
  }

  createBccp(releaseId: number, bdtManifestId: number): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core_component/bccp', {
      releaseId,
      bdtManifestId,
    });
  }

  createBdt(releaseId: number, bdtManifestId: number, specId: number): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core_component/dt', {
      releaseId,
      bdtManifestId,
      specId,
    });
  }

  createBOD(verbManifestIdList: number[], nounManifestIdList: number[]): Observable<BodCreateResponse> {
    return this.http.post<BodCreateResponse>('/api/core_component/oagis/bod', {
      verbManifestIdList,
      nounManifestIdList
    });
  }

  update(accNode: CcAccNode): Observable<any> {
    return this.http.post('/api/core_component/acc/' + accNode.accId, {
      objectClassTerm: accNode.objectClassTerm,
      deprecated: accNode.deprecated,
      abstract: accNode.abstracted,
      definition: accNode.definition,
      oagisComponentType: accNode.oagisComponentType
    });
  }

  updateAsccp(asccp: CcAsccpNodeDetail): Observable<any> {
    return this.http.post('api/core_component/asccp/' + asccp.asccp.asccpId, {
      definition: asccp.asccp.definition,
      deprecated: asccp.asccp.deprecated,
      den: asccp.asccp.den,
      reusable: asccp.asccp.reusable,
      propertyTerm: asccp.asccp.propertyTerm
    });
  }

  getAsccp(asccpId): Observable<CcAsccpNodeDetail> {
    return this.http.get<CcAsccpNodeDetail>('/api/core_component/asccp/' + asccpId);
  }

  getAsccpNode(manifestId: number): Observable<CcAsccpNode> {
    return this.http.get<CcAsccpNode>('/api/core_component/asccp/' + manifestId);
  }

  getBccpNode(manifestId: number): Observable<CcBccpNode> {
    return this.http.get<CcBccpNode>('/api/core_component/bccp/' + manifestId);
  }

  getBdtNode(manifestId: number): Observable<CcBdtNode> {
    return this.http.get<CcBdtNode>('/api/core_component/dt/' + manifestId);
  }

  getGraphNode(type: string, manifestId: number): Observable<CcGraph> {
    return this.http.get<CcGraph>('/api/graphs/' + type.toLowerCase() + '/' + manifestId);
  }

  getDetail(node: CcFlatNode): Observable<CcNodeDetail> {
    const url = '/api/core_component/' + node.type.toLowerCase() + '/detail';
    return this.http.get<CcNodeDetail>(url, {params: this.toHttpParams(node)}).pipe(map(detail => {
      switch (detail.type.toUpperCase()) {
        case 'ACC':
          node.detail = new CcAccNodeDetail(node as AccFlatNode, detail);
          break;
        case 'ASCCP':
          node.detail = new CcAsccpNodeDetail(node as AsccpFlatNode, detail);
          break;
        case 'BCCP':
          node.detail = new CcBccpNodeDetail(node as BccpFlatNode, detail);
          break;
        case 'DT':
          node.detail = new CcDtNodeDetail(node as DtFlatNode, detail);
          break;
        case 'DT_SC':
          node.detail = new CcBdtScNodeDetail(node as DtScFlatNode, detail);
          break;
      }
      return node.detail;
    }));
  }

  toHttpParams(node: CcFlatNode): HttpParams {
    let params = new HttpParams();
    params = params.set('type', node.type);
    if (node.type.toUpperCase() === 'ACC') {
      params = params.set('manifestId', String((node as AccFlatNode).accManifestId));
    } else if (node.type.toUpperCase() === 'ASCCP') {
      if ((node as AsccpFlatNode).asccManifestId) {
        params = params.set('asccManifestId', String((node as AsccpFlatNode).asccManifestId));
      }
      params = params.set('manifestId', String((node as AsccpFlatNode).asccpManifestId));
    } else if (node.type.toUpperCase() === 'BCCP') {
      if ((node as BccpFlatNode).bccManifestId) {
        params = params.set('bccManifestId', String((node as BccpFlatNode).bccManifestId));
      }
      params = params.set('manifestId', String((node as BccpFlatNode).bccpManifestId));
      params = params.set('bdtManifestId', String((node as BccpFlatNode).bdtManifestId));
    } else if (node.type.toUpperCase() === 'DT') {
      params = params.set('manifestId', String((node as DtFlatNode).manifestId));
    } else if (node.type.toUpperCase() === 'DT_SC') {
      params = params.set('manifestId', String((node as DtScFlatNode).bdtScManifestId));
    }

    const data = base64Encode(params.toString());
    return new HttpParams().set('data', data);
  }

  verifyAppendAssociation(accManifestId: number,
                          manifestId: number, type: string): Observable<VerifyAppendAssociationResponse> {
    const body = {
      asccpManifestId: (type === 'ASCCP') ? manifestId : undefined,
      bccpManifestId: (type === 'BCCP') ? manifestId : undefined
    };

    return this.http.post<VerifyAppendAssociationResponse>('/api/core_component/acc/' + accManifestId + '/verify_append', body);
  }

  appendAssociation(releaseId: number, accManifestId: number,
                    manifestId: number, type: string, attribute: boolean,
                    pos: number): Observable<any> | undefined {
    const body = {
      releaseId,
      pos,
      asccpManifestId: (type === 'ASCCP') ? manifestId : undefined,
      bccpManifestId: (type === 'BCCP') ? manifestId : undefined,
      attribute: (attribute) ? true : false
    };

    return this.http.post('/api/core_component/acc/' + accManifestId + '/append', body);
  }

  verifySetBasedAcc(accManifestId: number, basedAccManifestId: number): Observable<VerifyAppendAssociationResponse> {
    const body = {
      basedAccManifestId
    };

    return this.http.post<VerifyAppendAssociationResponse>('/api/core_component/acc/' + accManifestId + '/verify_append', body);
  }

  setBasedAcc(accManifestId: number, basedAccManifestId: number): Observable<any> {
    return this.http.post('/api/core_component/acc/' + accManifestId + '/base', {
      basedAccManifestId
    });
  }

  discardBasedAcc(accManifestId: number): Observable<any> {
    return this.http.post('/api/core_component/acc/' + accManifestId + '/base', {});
  }

  updateDetails(manifestId: number, nodes: CcFlatNode[]): Observable<any> {
    const body = {
      accNodeDetails: [],
      asccpNodeDetails: [],
      bccpNodeDetails: [],
      dtScNodeDetails: [],
      dtNodeDetails: []
    };
    for (const node of nodes) {
      const detail = node.detail;
      switch (node.type.toUpperCase()) {
        case 'ACC':
          body.accNodeDetails.push((detail as CcAccNodeDetail).json);
          break;
        case 'BCCP':
          body.bccpNodeDetails.push({
            bccp: (detail as CcBccpNodeDetail).bccp.json,
            bcc: (detail as CcBccpNodeDetail).bcc ? (detail as CcBccpNodeDetail).bcc.json : null,
          });
          break;
        case 'ASCCP':
          body.asccpNodeDetails.push({
            asccp: (detail as CcAsccpNodeDetail).asccp.json,
            ascc: (detail as CcAsccpNodeDetail).ascc ? (detail as CcAsccpNodeDetail).ascc.json : null,
          });
          break;
        case 'DT':
          body.dtNodeDetails.push((detail as CcDtNodeDetail).json);
          break;
        case 'DT_SC':
          body.dtScNodeDetails.push((detail as CcBdtScNodeDetail).json);
          break;
      }
    }
    return this.http.post('/api/core_component', body);
  }

  updateAsccpManifest(manifestId: number, accManifestId: number): Observable<any> {
    return this.http.post('/api/core_component/asccp/' + manifestId, {accManifestId: accManifestId});
  }

  updateBccpManifest(manifestId: number, bdtManifestId: number): Observable<any> {
    return this.http.post('/api/core_component/bccp/' + manifestId, {bdtManifestId: bdtManifestId});
  }

  updateState(type: string, manifestId: number, state: string): Observable<CcNodeUpdateResponse> {
    const url = '/api/core_component/' + type.toLowerCase() + '/' + manifestId + '/state';
    return this.http.post<CcNodeUpdateResponse>(url, {state: state});
  }

  verifyCreateExtensionComponent(accManifestId: number): Observable<VerifyAppendAssociationResponse> {
    const body = {
      propertyTerm: 'Extension'
    };

    return this.http.post<VerifyAppendAssociationResponse>('/api/core_component/acc/' + accManifestId + '/verify_append', body);
  }

  createExtensionComponent(manifestId: number): Observable<CcNodeUpdateResponse> {
    const url = '/api/core_component/acc/extension';
    return this.http.post<CcNodeUpdateResponse>(url, {
      accManifestId: manifestId
    });
  }

  cancelRevision(type: string, manifestId: number): Observable<CcNodeUpdateResponse> {
    const url = '/api/core_component/' + type.toLowerCase() + '/' + manifestId + '/revision/cancel';
    return this.http.post<CcNodeUpdateResponse>(url, {});
  }

  makeNewRevision(type: string, manifestId: number): Observable<CcNodeUpdateResponse> {
    const url = '/api/core_component/' + type.toLowerCase() + '/' + manifestId + '/revision';
    return this.http.post<CcNodeUpdateResponse>(url, {});
  }

  deleteNode(type: string, manifestId: number): Observable<any> {
    return this.http.delete('/api/core_component/' + type.toLowerCase() + '/' + manifestId, {});
  }

  getLastPublishedRevision(type: string, manifestId: number): Observable<CcRevisionResponse> {
    return this.http.get<CcRevisionResponse>('/api/core_component/' + type.toLowerCase() + '/' + manifestId + '/revision');
  }

  postComment(reference: string, text: string, prevCommentId?: number): Observable<any> {
    return this.http.put('/api/comment/' + reference, {
      reference,
      text,
      prevCommentId: prevCommentId ? prevCommentId : null
    });
  }

  editComment(commentId: number, text: string): Observable<any> {
    return this.http.post('/api/comment/' + commentId, {
      commentId,
      text,
    });
  }

  deleteComment(comment: Comment): Observable<any> {
    return this.http.post('/api/comment/' + comment.commentId, {
      commentId: comment.commentId,
      delete: true,
    });
  }

  getComments(reference): Observable<Comment[]> {
    return this.http.get<Comment[]>('/api/comments/' + reference );
  }

  updateCcSeq(changes: CcSeqUpdateRequest, manifestId: number): Observable<any> {
    return this.http.post('/api/core_component/acc/' + manifestId + '/seq_key', changes);
  }

  appendDtSc(ownerDtManifestId: number): Observable<any> {
    return this.http.post('/api/core_component/dt/' + ownerDtManifestId + '/append_sc', {});
  }

  discardDtSc(dtScManifestId: number): Observable<any> {
    return this.http.delete('/api/core_component/dt_sc/' + dtScManifestId, {});
  }

  ungroup(sourceAccManifestId: number, targetAsccManifestId: number, pos: number): Observable<any> {
    return this.http.post('/api/core_component/acc/' + sourceAccManifestId + '/ungroup', {
      asccManifestId: targetAsccManifestId,
      pos
    });
  }

  getPrimitiveListByRepresentationTerm(representationTerm: string, bdtScManifestId?: number): Observable<any> {
    let params = new HttpParams();
    if (!!bdtScManifestId) {
      params = params.set('bdtScManifestId', bdtScManifestId);
    }
    return this.http.get('/api/core_component/dt/' + representationTerm + '/primitive_values', {
      params
    });
  }

  getAsccpNodePlantUml(manifestId: number, options: {}): Observable<any> {
    let params = new HttpParams();
    if (!!options) {
      for (const key in options) {
        params = params.append(key, options[key]);
      }
    }
    return this.http.get('/api/core_component/asccp/' + manifestId + '/plantuml', {
      params
    });
  }

}
