import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {BieListEntry, BieListRequest, SummaryBieInfo} from './bie-list';
import {PageResponse} from '../../../basis/basis';

@Injectable()
export class BieListService {
  constructor(private http: HttpClient) {
  }

  getSummaryBieList(libraryId: number, releaseId: number): Observable<SummaryBieInfo> {
    let params = new HttpParams()
        .set('libraryId', libraryId);
    if (!!releaseId && releaseId > 0) {
      params = params.set('releaseId', releaseId);
    }
    return this.http.get<SummaryBieInfo>('/api/info/bie-summaries', {
      params
    }).pipe(map(
      e => {
        if (e.myRecentBIEs) {
          e.myRecentBIEs = e.myRecentBIEs.map(elm => ({
            ...elm,
            source: (!!elm.source) ? {
              ...elm.source,
              when: new Date(elm.source.when),
            } : undefined,
            based: (!!elm.based) ? {
              ...elm.based,
              when: new Date(elm.based.when),
            } : undefined,
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

  getBieListWithRequest(request: BieListRequest): Observable<PageResponse<BieListEntry>> {
    let params = new HttpParams()
      .set('libraryId', '' + request.library.libraryId);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.page.pageIndex >= 0) {
      params = params.set('pageIndex', request.page.pageIndex);
    }
    if (request.page.pageSize > 0) {
      params = params.set('pageSize', request.page.pageSize);
    }
    if (request.ownerLoginIdList.length > 0) {
      params = params.set('ownerLoginIdList', request.ownerLoginIdList.join(','));
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.den) {
      params = params.set('den', request.filters.den);
    }
    if (request.filters.propertyTerm) {
      params = params.set('propertyTerm', request.filters.propertyTerm);
    }
    if (request.filters.businessContext) {
      params = params.set('businessContext', request.filters.businessContext);
    }
    if (request.filters.version) {
      params = params.set('version', request.filters.version);
    }
    if (request.filters.remark) {
      params = params.set('remark', request.filters.remark);
    }
    if (request.filters.asccpManifestId) {
      params = params.set('asccpManifestId', '' + request.filters.asccpManifestId);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.deprecated && request.deprecated.length === 1) {
      params = params.set('deprecated', '' + request.deprecated[0]);
    }
    if (request.access) {
      params = params.set('access', request.access);
    }
    if (request.excludePropertyTerms.length > 0) {
      params = params.set('excludePropertyTerms', request.excludePropertyTerms.join(','));
    }
    if (request.topLevelAsbiepIds.length > 0) {
      params = params.set('topLevelAsbiepIds', request.topLevelAsbiepIds.join(','));
    }
    if (request.basedTopLevelAsbiepIds.length > 0) {
      params = params.set('basedTopLevelAsbiepIds', request.basedTopLevelAsbiepIds.join(','));
    }
    if (request.excludeTopLevelAsbiepIds.length > 0) {
      params = params.set('excludeTopLevelAsbiepIds', request.excludeTopLevelAsbiepIds.join(','));
    }
    if (request.releases && request.releases.length > 0) {
      params = params.set('releaseIds', request.releases.map(e => e.releaseId.toString()).join(','));
    }
    if (request.ownedByDeveloper !== undefined) {
      params = params.set('ownedByDeveloper', request.ownedByDeveloper.toString());
    }
    return this.http.get<PageResponse<BieListEntry>>('/api/bies', {params}).pipe(
        map((res: PageResponse<BieListEntry>) => ({
          ...res,
          list: res.list.map(elm => ({
            ...elm,
            source: (!!elm.source) ? {
              ...elm.source,
              when: new Date(elm.source.when),
            } : undefined,
            based: (!!elm.based) ? {
              ...elm.based,
              when: new Date(elm.based.when),
            } : undefined,
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

  getBieListByTopLevelAsbiepId(topLevelAsbiepId: number): Observable<BieListEntry> {
    const request = new BieListRequest();
    request.page.pageSize = 1;
    request.topLevelAsbiepIds = [topLevelAsbiepId,];
    return this.getBieListWithRequest(request)
      .pipe(map(resp => (resp.length !== 0) ? resp.list[0] : undefined));
  }

  delete(topLevelAsbiepIdList: number[]): Observable<any> {
    return this.http.delete<any>('/api/bies', {
      body: {
        topLevelAsbiepIdList
      }
    });
  }

  transferOwnership(topLevelAsbiepIds: number, targetLoginId: string,
                    sendNotification?: boolean, mailParameters?: any): Observable<any> {
    let url = '/api/profile_bie/' + topLevelAsbiepIds + '/transfer_ownership';
    if (sendNotification !== undefined) {
      url += '?sendNotification=' + ((sendNotification) ? 'true' : 'false');
    }
    return this.http.post<any>(url, {
      topLevelAsbiepIds,
      targetLoginId,
      parameters: mailParameters
    });
  }

  updateStateOnList(actionType: string, toState: string, bieLists: BieListEntry[]): Observable<any> {
    return this.http.post<any>('/api/bie_list/state/multiple', {
      action: actionType,
      toState,
      topLevelAsbiepIds: bieLists.map(e => e.topLevelAsbiepId)
    });
  }

  transferOwnershipOnList(bieLists: BieListEntry[], targetLoginId: string): Observable<any> {
    return this.http.post<any>('/api/bie_list/transfer_ownership/multiple', {
      targetLoginId,
      topLevelAsbiepIds: bieLists.map(e => e.topLevelAsbiepId)
    });
  }

  getPlantUml(topLevelAsbiepId: number, options: {}): Observable<any> {
    let params = new HttpParams();
    if (!!options) {
      for (const key in options) {
        params = params.append(key, options[key]);
      }
    }
    return this.http.get('/api/bies/' + topLevelAsbiepId + '/plantuml', {
      params
    });
  }

}
