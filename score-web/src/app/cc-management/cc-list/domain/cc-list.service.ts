import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Acc, Ascc, Asccp, Bcc, Bccp, CcList, CcListRequest, SummaryCcExtInfo} from './cc-list';
import {PageResponse} from '../../../basis/basis';
import {BieEditAbieNode, BieEditNode} from '../../../bie-management/bie-edit/domain/bie-edit-node';
import {OagisComponentType} from '../../domain/core-component-node';
import {base64Encode} from '../../../common/utility';
import {BieEditNodeDetail} from '../../../bie-management/domain/bie-flat-tree';

@Injectable()
export class CcListService {

  constructor(private http: HttpClient) {
  }

  getSummaryCcExtList(releaseId: number): Observable<SummaryCcExtInfo> {
    return this.http.get<SummaryCcExtInfo>('/api/info/cc_ext_summary?releaseId=' + releaseId).pipe(map(
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
    let params = new HttpParams().set('releaseId', '' + request.release.releaseId)
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
      params = params.set('types', request.types.map(e => e.toLowerCase()).join(','));
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.deprecated && request.deprecated.length === 1) {
      params = params.set('deprecated', '' + request.deprecated[0]);
    }
    if (request.commonlyUsed && request.commonlyUsed.length === 1) {
      params = params.set('commonlyUsed', '' + request.commonlyUsed[0]);
    }
    if (request.ownerLoginIds.length > 0) {
      params = params.set('ownerLoginIds', request.ownerLoginIds.join(','));
    }
    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.excludes.length > 0) {
      params = params.set('excludes', request.excludes.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.componentTypes && request.componentTypes.length > 0) {
      params = params.set('componentTypes', request.componentTypes
        .map((elm: OagisComponentType) => elm.value).join(','));
    }
    if (request.dtTypes && request.dtTypes.length > 0) {
      params = params.set('dtTypes', request.dtTypes.join(','));
    }
    if (request.asccpTypes && request.asccpTypes.length > 0) {
      params = params.set('asccpTypes', request.asccpTypes.join(','));
    }
    if (request.findUsages.type && request.findUsages.manifestId > 0) {
      params = params.set('findUsagesType', request.findUsages.type)
        .set('findUsagesManifestId', '' + request.findUsages.manifestId);
    }
    if (request.isBIEUsable) {
      params = params.set('isBIEUsable', '' + request.isBIEUsable);
    }

    return this.http.get<PageResponse<CcList>>('/api/core_component', {params});
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
    const url = '/api/profile_bie/node/children/' + node.ccType.toLowerCase();
    return this.http.get<BieEditNode[]>(url, {params: this.toHttpParams(node)});
  }

  getDetail(node: BieEditNode): Observable<BieEditNodeDetail> {
    const url = '/api/profile_bie/node/detail/' + node.ccType.toLowerCase();
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
    const data = base64Encode(params.toString());
    return new HttpParams().set('data', data);
  }

  create(accId: number): Observable<any> {
    return this.http.put('/api/core_component/acc/create', {
      accId
    });
  }

  transferOwnership(type: string, manifestId: number, targetLoginId: string): Observable<any> {
    return this.http.post<any>('/api/core_component/' +
      type.toLowerCase() + '/' + manifestId + '/transfer_ownership', {
      targetLoginId
    });
  }

  transferOwnershipOnList(ccLists: CcList[], targetLoginId: string): Observable<any> {
    const accManifestIds = [];
    const asccpManifestIds = [];
    const bccpManifestIds = [];

    for (const item of ccLists) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIds.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIds.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIds.push(item.manifestId);
          break;
      }
    }
    return this.http.post<any>('/api/core_component/transfer_ownership/multiple', {
      targetLoginId,
      accManifestIds,
      asccpManifestIds,
      bccpManifestIds,
    });
  }

  updateStateOnList(actionType: string, toState: string, ccLists: CcList[]): Observable<any> {
    const accManifestIds = [];
    const asccpManifestIds = [];
    const bccpManifestIds = [];

    for (const item of ccLists) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIds.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIds.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIds.push(item.manifestId);
          break;
      }
    }

    return this.http.post<any>('/api/core_component/state/multiple', {
      action: actionType,
      toState,
      accManifestIds,
      asccpManifestIds,
      bccpManifestIds,
    });
  }

}
