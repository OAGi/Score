import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {CcChangeResponse, CcListEntry, CcListRequest, SummaryCcExtInfo, SummaryCcInfo} from './cc-list';
import {PageResponse} from '../../../basis/basis';
import {BieEditAbieNode, BieEditNode} from '../../../bie-management/bie-edit/domain/bie-edit-node';
import {CcDtNodeInfo, OagisComponentType, XbtSummary} from '../../domain/core-component-node';
import {base64Encode} from '../../../common/utility';
import {BieEditNodeDetail} from '../../../bie-management/domain/bie-flat-tree';

@Injectable()
export class CcListService {

  constructor(private http: HttpClient) {
  }

  getSummaryCcList(libraryId: number): Observable<SummaryCcInfo> {
    return this.http.get<SummaryCcInfo>('/api/info/cc-summaries?libraryId=' + libraryId).pipe(map(
      e => {
        if (e.myRecentCCs) {
          e.myRecentCCs = e.myRecentCCs.map(elm => ({
            ...elm,
            created: {
              ...elm.created,
              when: new Date(elm.created.when),
            },
            lastUpdated: {
              ...elm.lastUpdated,
              when: new Date(elm.lastUpdated.when),
            }
          }));
        }
        return e;
      }));
  }

  getSummaryCcExtList(libraryId: number, releaseId: number): Observable<SummaryCcExtInfo> {
    let params = new HttpParams()
        .set('libraryId', libraryId);
    if (!!releaseId && releaseId > 0) {
      params = params.set('releaseId', releaseId);
    }
    return this.http.get<SummaryCcExtInfo>('/api/info/cc-ext-summaries', {
      params
    }).pipe(map(
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

  getCcList(request: CcListRequest): Observable<PageResponse<CcListEntry>> {
    let params = new HttpParams()
      .set('libraryId', '' + request.library.libraryId)
      .set('releaseId', '' + request.release.releaseId);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.page.pageIndex >= 0) {
      params = params.set('pageIndex', request.page.pageIndex);
    }
    if (request.page.pageSize > 0) {
      params = params.set('pageSize', request.page.pageSize);
    }

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
    if (request.newComponent && request.newComponent.length === 1) {
      params = params.set('newComponent', '' + request.newComponent[0]);
    }
    if (request.reusable && request.reusable.length === 1) {
      params = params.set('reusable', '' + request.reusable[0]);
    }
    if (request.commonlyUsed && request.commonlyUsed.length === 1) {
      params = params.set('commonlyUsed', '' + request.commonlyUsed[0]);
    }
    if (request.ownerLoginIdList.length > 0) {
      params = params.set('ownerLoginIdList', request.ownerLoginIdList.join(','));
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
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
    if (request.tags && request.tags.length > 0) {
      params = params.set('tags', request.tags.join(','));
    }
    if (request.namespaces && request.namespaces.length > 0) {
      params = params.set('namespaces', request.namespaces.map(e => '' + e).join(','));
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

    return this.http.get<PageResponse<CcListEntry>>('/api/core-components', {params}).pipe(
        map((res: PageResponse<CcListEntry>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            created: {
              ...elm.created,
              when: new Date(elm.created.when),
            },
            lastUpdated: {
              ...elm.lastUpdated,
              when: new Date(elm.lastUpdated.when),
            }
          }))
        }))
    );
  }

  transferOwnership(type: string, manifestId: number, targetLoginId: string): Observable<any> {
    return this.http.patch<any>('/api/core-components/' +
      type.toLowerCase() + '/' + manifestId + '/transfer', {
      targetLoginId
    });
  }

  transferOwnershipOnList(ccLists: CcListEntry[], targetLoginId: string): Observable<any> {
    const accManifestIdList = [];
    const asccpManifestIdList = [];
    const bccpManifestIdList = [];
    const dtManifestIdList = [];

    for (const item of ccLists) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIdList.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIdList.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIdList.push(item.manifestId);
          break;
        case 'DT':
          dtManifestIdList.push(item.manifestId);
          break;
      }
    }
    return this.http.patch<any>('/api/core-components/transfer', {
      targetLoginId,
      accManifestIdList,
      asccpManifestIdList,
      bccpManifestIdList,
      dtManifestIdList
    });
  }

  updateState(ccListEntries: CcListEntry[], toState: string): Observable<any> {
    const accManifestIdList = [];
    const asccpManifestIdList = [];
    const bccpManifestIdList = [];
    const dtManifestIdList = [];

    for (const item of ccListEntries) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIdList.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIdList.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIdList.push(item.manifestId);
          break;
        case 'DT':
          dtManifestIdList.push(item.manifestId);
          break;
      }
    }

    return this.http.patch<any>('/api/core-components/state', {
      toState,
      accManifestIdList,
      asccpManifestIdList,
      bccpManifestIdList,
      dtManifestIdList
    });
  }

  delete(ccListEntries: CcListEntry[]): Observable<any> {
    const accManifestIdList = [];
    const asccpManifestIdList = [];
    const bccpManifestIdList = [];
    const dtManifestIdList = [];

    for (const item of ccListEntries) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIdList.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIdList.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIdList.push(item.manifestId);
          break;
        case 'DT':
          dtManifestIdList.push(item.manifestId);
          break;
      }
    }

    return this.http.patch<any>('/api/core-components/mark-as-deleted', {
      accManifestIdList,
      asccpManifestIdList,
      bccpManifestIdList,
      dtManifestIdList
    });
  }

  restore(ccListEntries: CcListEntry[]): Observable<any> {
    const accManifestIdList = [];
    const asccpManifestIdList = [];
    const bccpManifestIdList = [];
    const dtManifestIdList = [];

    for (const item of ccListEntries) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIdList.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIdList.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIdList.push(item.manifestId);
          break;
        case 'DT':
          dtManifestIdList.push(item.manifestId);
          break;
      }
    }

    return this.http.patch<any>('/api/core-components/restore', {
      accManifestIdList,
      asccpManifestIdList,
      bccpManifestIdList,
      dtManifestIdList
    });
  }

  purge(ccListEntries: CcListEntry[]): Observable<any> {
    const accManifestIdList = [];
    const asccpManifestIdList = [];
    const bccpManifestIdList = [];
    const dtManifestIdList = [];

    for (const item of ccListEntries) {
      switch (item.type.toUpperCase()) {
        case 'ACC':
          accManifestIdList.push(item.manifestId);
          break;
        case 'ASCCP':
          asccpManifestIdList.push(item.manifestId);
          break;
        case 'BCCP':
          bccpManifestIdList.push(item.manifestId);
          break;
        case 'DT':
          dtManifestIdList.push(item.manifestId);
          break;
      }
    }

    return this.http.delete<any>('/api/core-components', {
      body: {
        accManifestIdList,
        asccpManifestIdList,
        bccpManifestIdList,
        dtManifestIdList
      }
    });
  }

  getXbtListSummaries(releaseId: number): Observable<XbtSummary[]> {
    const params = new HttpParams()
        .set('releaseId', releaseId.toString());

    return this.http.get<XbtSummary[]>('/api/xbts/summaries', {params});
  }

  createPrimitiveRestriction(releaseId: number, dtManifestId: number, type: string, primitiveXbtMapList: any[]): Observable<CcDtNodeInfo> {
    return this.http.post<any>('/api/core_component/dt/' + dtManifestId + '/restriction/add', {
      releaseId,
      dtManifestId,
      restrictionType: type,
      primitiveXbtMapList,
    });
  }

  createCodeListRestriction(releaseId: number, dtManifestId: number, type: string, codeListManifestId: number): Observable<CcDtNodeInfo> {
    return this.http.post<any>('/api/core_component/dt/' + dtManifestId + '/restriction/add', {
      releaseId,
      dtManifestId,
      restrictionType: type,
      codeListManifestId,
    });
  }

  createAgencyIdListRestriction(releaseId: number, dtManifestId: number, type: string, agencyIdListManifestId: number): Observable<CcDtNodeInfo> {
    return this.http.post<any>('/api/core_component/dt/' + dtManifestId + '/restriction/add', {
      releaseId,
      dtManifestId,
      restrictionType: type,
      agencyIdListManifestId,
    });
  }

  exportStandaloneSchemas(asccpManifestIdList: CcListEntry[]): Observable<HttpResponse<Blob>> {
    const params = new HttpParams().set('asccpManifestIdList',
      asccpManifestIdList.map(e => e.manifestId.toString().toLowerCase()).join(','));
    return this.http.get('/api/core-components/export/standalone', {
      params,
      observe: 'response',
      responseType: 'blob'
    });
  }

  getCcChanges(releaseId: number): Observable<CcChangeResponse> {
    return this.http.get<CcChangeResponse>('/api/core-components/changes-in-release/' + releaseId);
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

}
