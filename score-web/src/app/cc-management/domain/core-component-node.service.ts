import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {forkJoin, Observable} from 'rxjs';
import {AccFlatNode, AsccpFlatNode, BccpFlatNode, CcFlatNode, DtFlatNode, DtScFlatNode} from './cc-flat-tree';
import {
  AccDetails,
  AiGenerationResponse,
  AsccDetails,
  AsccpDetails,
  BccDetails,
  BccpDetails,
  BodCreateResponse,
  CcAccNodeInfo,
  CcAsccpNodeInfo,
  CcBccpNodeInfo,
  CcCreateResponse,
  CcDtNodeInfo,
  CcDtScNodeInfo,
  CcGraph,
  CcNodeInfo,
  CcNodeUpdateResponse,
  CcSeqUpdateRequest,
  Comment,
  CommentRecord,
  DtDetails,
  DtScAwdPriDetails,
  DtScDetails,
  VerbCreateResponse,
  VerifyAppendAssociationResponse
} from './core-component-node';
import {base64Encode, nullObservable} from '../../common/utility';
import {map} from 'rxjs/operators';

@Injectable()
export class CcNodeService {

  constructor(private http: HttpClient) {
  }

  getAccDetails(manifestId): Observable<AccDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<AccDetails>('/api/core-components/acc/' + manifestId).pipe(
        map((res: AccDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getPrevAccDetails(manifestId): Observable<AccDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<AccDetails>('/api/core-components/acc/' + manifestId + '/prev').pipe(
        map((res: AccDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getAsccDetails(manifestId: number): Observable<AsccDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<AsccDetails>('/api/core-components/ascc/' + manifestId).pipe(
        map((res: AsccDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getBccDetails(manifestId: number): Observable<BccDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<BccDetails>('/api/core-components/bcc/' + manifestId).pipe(
        map((res: BccDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getAsccpDetails(manifestId: number): Observable<AsccpDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<AsccpDetails>('/api/core-components/asccp/' + manifestId).pipe(
        map((res: AsccpDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getPrevAsccpDetails(manifestId: number): Observable<AsccpDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<AsccpDetails>('/api/core-components/asccp/' + manifestId + '/prev').pipe(
        map((res: AsccpDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getBccpDetails(manifestId: number): Observable<BccpDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<BccpDetails>('/api/core-components/bccp/' + manifestId).pipe(
        map((res: BccpDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getPrevBccpDetails(manifestId: number): Observable<BccpDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<BccpDetails>('/api/core-components/bccp/' + manifestId + '/prev').pipe(
        map((res: BccpDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getDtDetails(manifestId: number): Observable<DtDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<DtDetails>('/api/core-components/dt/' + manifestId).pipe(
        map((res: DtDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getPrevDtDetails(manifestId: number): Observable<DtDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<DtDetails>('/api/core-components/dt/' + manifestId + '/prev').pipe(
        map((res: DtDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  getDtScDetails(manifestId: number): Observable<DtScDetails> {
    if (!manifestId) {
      return nullObservable(undefined);
    }
    return this.http.get<DtScDetails>('/api/core-components/dt-sc/' + manifestId).pipe(
        map((res: DtScDetails) => ({
          ...res,
          created: {
            ...res.created,
            when: new Date(res.created.when),
          },
          lastUpdated: {
            ...res.lastUpdated,
            when: new Date(res.lastUpdated.when),
          }
        }))
    );
  }

  createAcc(releaseId: number): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core-components/acc', {
      releaseId
    });
  }

  createAsccp(releaseId: number, accManifestId: number, initialPropertyTerm: string, asccpType?: string): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core-components/asccp', {
      releaseId,
      roleOfAccManifestId: accManifestId,
      asccpType: asccpType || 'Default',
      initialPropertyTerm
    });
  }

  createBccp(releaseId: number, basedDtManifestId: number): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core-components/bccp', {
      releaseId,
      basedDtManifestId,
    });
  }

  createDt(releaseId: number, basedDtManifestId: number): Observable<CcCreateResponse> {
    return this.http.post<CcCreateResponse>('/api/core-components/dt', {
      releaseId,
      basedDtManifestId
    });
  }

  createBOD(verbManifestIdList: number[], nounManifestIdList: number[]): Observable<BodCreateResponse> {
    return this.http.post<BodCreateResponse>('/api/core-components/oagis/bod', {
      verbManifestIdList,
      nounManifestIdList
    });
  }

  createVerb(basedVerbAccManifestId: number): Observable<VerbCreateResponse> {
    return this.http.post<VerbCreateResponse>('/api/core-components/oagis/verb', {
      basedVerbAccManifestId
    });
  }

  getGraphNode(type: string, manifestId: number): Observable<CcGraph> {
    return this.http.get<CcGraph>('/api/graphs/' + type.toLowerCase() + '/' + manifestId);
  }

  getDetail(node: CcFlatNode): Observable<CcNodeInfo> {
    switch (node.type.toUpperCase()) {
      case 'ACC':
        return this.getAccDetails(node.manifestId).pipe(map(detail => {
          node.detail = new CcAccNodeInfo(node as AccFlatNode, detail);
          return node.detail;
        }));
      case 'ASCCP':
        return forkJoin([
          this.getAsccDetails((node as AsccpFlatNode).asccManifestId),
          this.getAsccpDetails(node.manifestId)
        ]).pipe(map(([asccDetails, asccpDetails]) => {
          node.detail = new CcAsccpNodeInfo(node as AsccpFlatNode, asccDetails, asccpDetails);
          return node.detail;
        }));
      case 'BCCP':
        return forkJoin([
          this.getBccDetails((node as BccpFlatNode).bccManifestId),
          this.getBccpDetails(node.manifestId),
          this.getDtDetails((node as BccpFlatNode).bdtManifestId),
        ]).pipe(map(([bccDetails, bccpDetails, dtDetails]) => {
          node.detail = new CcBccpNodeInfo(node as BccpFlatNode, bccDetails, bccpDetails, dtDetails);
          return node.detail;
        }));
      case 'DT':
        return this.getDtDetails(node.manifestId).pipe(map(detail => {
          node.detail = new CcDtNodeInfo(node as DtFlatNode, detail);
          return node.detail;
        }));
      case 'DT_SC':
        return this.getDtScDetails(node.manifestId).pipe(map(detail => {
          node.detail = new CcDtScNodeInfo(node as DtScFlatNode, detail);
          return node.detail;
        }));
    }
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

    let params = new HttpParams();
    if (type === 'ASCCP') {
      params = params.set('asccpManifestId', manifestId);
    } else if (type === 'BCCP') {
      params = params.set('bccpManifestId', manifestId);
    }

    return this.http.get<VerifyAppendAssociationResponse>('/api/core-components/acc/' + accManifestId + '/verify', {
      params
    });
  }

  appendAscc(accManifestId: number, asccpManifestId: number,
             pos: number, attribute: boolean): Observable<any> {

    return this.http.post('/api/core-components/acc/' + accManifestId + '/ascc/' + asccpManifestId, {}, {
      params: new HttpParams().set('pos', '' + pos).set('attribute', attribute)
    });
  }

  appendBcc(accManifestId: number, bccpManifestId: number,
            pos: number, attribute: boolean): Observable<any> {

    return this.http.post('/api/core-components/acc/' + accManifestId + '/bcc/' + bccpManifestId, {}, {
      params: new HttpParams().set('pos', '' + pos).set('attribute', attribute)
    });
  }

  verifySetBasedAcc(accManifestId: number, basedAccManifestId: number): Observable<VerifyAppendAssociationResponse> {
    return this.http.get<VerifyAppendAssociationResponse>('/api/core-components/acc/' + accManifestId + '/verify', {
      params: new HttpParams().set('basedAccManifestId', basedAccManifestId)
    });
  }

  setBasedAcc(accManifestId: number, basedAccManifestId: number): Observable<any> {
    const params = new HttpParams().set('basedAccManifestId', basedAccManifestId);
    const url = '/api/core-components/acc/' + accManifestId + '/base';
    return this.http.patch<any>(url, {}, {params});
  }

  discardBasedAcc(accManifestId: number): Observable<any> {
    const url = '/api/core-components/acc/' + accManifestId + '/base';
    return this.http.patch<any>(url, {}, {});
  }

  updateNodes(nodes: CcFlatNode[]): Observable<any> {
    const body = {
      accUpdateRequestList: [],
      asccUpdateRequestList: [],
      asccpUpdateRequestList: [],
      bccUpdateRequestList: [],
      bccpUpdateRequestList: [],
      dtUpdateRequestList: [],
      dtScUpdateRequestList: []
    };
    for (const node of nodes) {
      const detail = node.detail;
      switch (node.type.toUpperCase()) {
        case 'ACC':
          body.accUpdateRequestList.push((detail as CcAccNodeInfo).json);
          break;
        case 'BCCP':
          if ((detail as CcBccpNodeInfo).bcc) {
            body.bccUpdateRequestList.push((detail as CcBccpNodeInfo).bcc.json);
          } else {
            body.bccpUpdateRequestList.push((detail as CcBccpNodeInfo).bccp.json);
          }
          break;
        case 'ASCCP':
          if ((detail as CcAsccpNodeInfo).ascc) {
            body.asccUpdateRequestList.push((detail as CcAsccpNodeInfo).ascc.json);
          } else {
            body.asccpUpdateRequestList.push((detail as CcAsccpNodeInfo).asccp.json);
          }
          break;
        case 'DT':
          body.dtUpdateRequestList.push((detail as CcDtNodeInfo).json);
          break;
        case 'DT_SC':
          body.dtScUpdateRequestList.push((detail as CcDtScNodeInfo).json);
          break;
      }
    }
    return this.http.put('/api/core-components', body);
  }

  updateRoleOfAcc(manifestId: number, accManifestId: number): Observable<any> {
    return this.http.patch('/api/core-components/asccp/' + manifestId + '/role-of-acc', {}, {
      params: new HttpParams().set('accManifestId', accManifestId)
    });
  }

  updateBccpDt(manifestId: number, dtManifestId: number): Observable<any> {
    return this.http.patch('/api/core-components/bccp/' + manifestId + '/dt', {}, {
      params: new HttpParams().set('dtManifestId', dtManifestId)
    });
  }

  updateState(type: string, manifestId: number, state: string): Observable<any> {
    const params = new HttpParams().set('state', state);
    const url = '/api/core-components/' + type.toLowerCase() + '/' + manifestId + '/state';
    return this.http.patch<any>(url, {}, {params});
  }

  purge(type: string, manifestId: number): Observable<any> {
    const url = '/api/core-components/' + type.toLowerCase() + '/' + manifestId;
    return this.http.delete<any>(url);
  }

  verifyCreateExtensionComponent(accManifestId: number): Observable<VerifyAppendAssociationResponse> {
    return this.http.get<VerifyAppendAssociationResponse>('/api/core-components/acc/' + accManifestId + '/verify', {
      params: new HttpParams().set('propertyTerm', 'Extension')
    });
  }

  createExtensionComponent(manifestId: number): Observable<CcNodeUpdateResponse> {
    const url = '/api/core-components/acc/' + manifestId + '/extension';
    return this.http.post<CcNodeUpdateResponse>(url, {});
  }

  makeNewRevision(type: string, manifestId: number): Observable<any> {
    const url = '/api/core-components/' + type.toLowerCase() + '/' + manifestId + '/revise';
    return this.http.patch<any>(url, {});
  }

  cancelRevision(type: string, manifestId: number): Observable<any> {
    const url = '/api/core-components/' + type.toLowerCase() + '/' + manifestId + '/cancel';
    return this.http.patch<any>(url, {});
  }

  postComment(reference: string, text: string, prevCommentId?: number): Observable<any> {
    return this.http.post('/api/comments/' + reference, {
      reference,
      text,
      prevCommentId: prevCommentId ? prevCommentId : null
    });
  }

  editComment(commentId: number, text: string): Observable<any> {
    return this.http.put('/api/comments/' + commentId, {
      commentId,
      text,
    });
  }

  deleteComment(comment: Comment): Observable<any> {
    return this.http.delete('/api/comments/' + comment.commentId, {});
  }

  getComments(reference): Observable<Comment[]> {
    return this.http.get<CommentRecord[]>('/api/comments/' + reference).pipe(map((resp: CommentRecord[]) => {
      return resp.map(elm => {
        const comment = new Comment();
        comment.commentId = elm.commentId;
        comment.prevCommentId = elm.prevCommentId;
        comment.text = elm.text;
        comment.hidden = elm.hidden;
        comment.created = elm.created;
        comment.timestamp = new Date(elm.lastUpdated.when);
        return comment;
      });
    }));
  }

  updateAccSequence(changes: CcSeqUpdateRequest, manifestId: number): Observable<any> {
    return this.http.patch('/api/core-components/acc/' + manifestId + '/sequence', changes);
  }

  appendDtSc(ownerDtManifestId: number): Observable<any> {
    return this.http.post('/api/core-components/dt/' + ownerDtManifestId + '/dt-sc', {});
  }

  discardDtSc(dtScManifestId: number): Observable<any> {
    return this.http.delete('/api/core-components/dt-sc/' + dtScManifestId, {});
  }

  ungroup(sourceAccManifestId: number, targetAsccManifestId: number, pos: number): Observable<any> {
    let params = new HttpParams()
        .set('asccManifestId', targetAsccManifestId)
        .set('pos', pos);
    return this.http.post('/api/core-components/acc/' + sourceAccManifestId + '/ungroup', {}, {
      params
    });
  }

  getPrimitiveListByRepresentationTerm(representationTerm: string, bdtScManifestId?: number): Observable<DtScAwdPriDetails[]> {
    let params = new HttpParams();
    if (!!bdtScManifestId) {
      params = params.set('dtScManifestId', bdtScManifestId);
    }
    return this.http.get<DtScAwdPriDetails[]>('/api/core-components/dt/' + representationTerm + '/primitive-values', {
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
    return this.http.get('/api/core-components/asccp/' + manifestId + '/plantuml', {
      params
    });
  }

  availableModels(): Observable<string[]> {
    return this.http.get<string[]>('/api/ai/models');
  }

  generateDefinition(type: string, manifestId: number, model: string, originalText?: string): Observable<AiGenerationResponse> {
    let params = new HttpParams()
        .set('model', model);
    if (!!originalText) {
      params = params.set('originalText', originalText);
    }
    return this.http.get<AiGenerationResponse>('/api/ai/generate/' + type.toLowerCase() + '/' + manifestId + '/definition', {params});
  }

  generateName(type: string, manifestId: number, model: string, originalName?: string): Observable<AiGenerationResponse> {
    let params = new HttpParams()
        .set('model', model);
    if (!!originalName) {
      params = params.set('originalName', originalName);
    }
    return this.http.get<AiGenerationResponse>('/api/ai/generate/' + type.toLowerCase() + '/' + manifestId + '/name', {params});
  }

}
