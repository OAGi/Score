import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Acc, Ascc, Asccp, Bcc, Bccp, CcList, CcListRequest, SummaryCcExtInfo} from './cc-list';
import {PageResponse} from '../../../basis/basis';
import {BieEditAbieNode, BieEditNode, BieEditNodeDetail} from '../../../bie-management/bie-edit/domain/bie-edit-node';
import {Base64} from 'js-base64';
import {map} from 'rxjs/operators';

@Injectable()
export class CcListService {

  constructor(private http: HttpClient) {
  }

  getSummaryCcExtList(): Observable<SummaryCcExtInfo> {
    return this.http.get<SummaryCcExtInfo>('/api/info/cc_ext_summary').pipe(map(
      e => {
        if (e.myExtensionsUnusedInBIEs) {
          e.myExtensionsUnusedInBIEs = e.myExtensionsUnusedInBIEs.map(elm => {
            elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
            return elm;
          });
        }
        return e;
      }));
  }

  getCcList(request: CcListRequest): Observable<PageResponse<CcList>> {
    let params = new HttpParams().set('releaseId', '' + request.releaseId)
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    if (request.filters.den) {
      params = params.set('den', request.filters.den);
    }
    if (request.filters.definition) {
      params = params.set('definition', request.filters.definition);
    }
    if (request.filters.module) {
      params = params.set('module', request.filters.module);
    }
    if (request.types.length > 0) {
      params = params.set('types', request.types.join(','));
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.deprecated !== undefined) {
      params = params.set('deprecated', '' + request.deprecated);
    }
    if (request.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', request.ownerLoginIds.join(','));
    }
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.componentType.length > 0) {
      params = params.set('componentType', request.componentType.join(',').replace(/ /gi, ''));
    }

    return this.http.get<PageResponse<CcList>>('/api/core_component', {params: params});
  }

  getAsccp(id): Observable<Asccp> {
    return this.http.get<Asccp>('/api/core_component/asccp/' + id);
  }

  getAcc(id): Observable<Acc> {
    return this.http.get<Acc>('/api/core_component/acc/' + id);
  }

  getBccs(id): Observable<Bcc[]> {
    return this.http.get<Bcc[]>('/api/core_component/bccs/' + id);
  }

  getBccp(id): Observable<Bccp> {
    return this.http.get<Bccp>('/api/core_component/bccp/' + id);
  }

  getAscc(id): Observable<Ascc> {
    return this.http.get<Ascc>('/api/core_component/ascc/' + id);
  }

  getRootNode(topLevelAsbiepId): Observable<BieEditAbieNode> {
    return this.http.get<BieEditAbieNode>('/api/profile_bie/node/root/' + topLevelAsbiepId);
  }

  getChildren(node: BieEditNode): Observable<BieEditNode[]> {
    const url = '/api/profile_bie/node/children/' + node.type;
    return this.http.get<BieEditNode[]>(url, {params: this.toHttpParams(node)});
  }

  getDetail(node: BieEditNode): Observable<BieEditNodeDetail> {
    const url = '/api/profile_bie/node/detail/' + node.type;
    return this.http.get<BieEditNodeDetail>(url, {params: this.toHttpParams(node)});
  }

  toHttpParams(obj: Object): HttpParams {
    const params: HttpParams = Object.getOwnPropertyNames(obj)
      .reduce((p, key) => {
        if (key !== 'name' && key !== 'hasChild') {
          return p.set(key, obj[key]);
        }
        return p;
      }, new HttpParams());
    const data = Base64.encode(params.toString());
    return new HttpParams().set('data', data);
  }

  create(accId: number): Observable<any> {
    return this.http.put('/api/core_component/acc/create', {
      accId: accId
    });
  }

  transferOwnership(extensionId: number, targetLoginId: string): Observable<any> {
    return this.http.post<any>('/api/core_component/extension/' + 1 + '/' + extensionId + '/transfer_ownership', {
      targetLoginId: targetLoginId
    });
  }

}
