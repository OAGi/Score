import {Injectable} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageResponse} from '../../../basis/basis';
import {BieListInBiePackageRequest, BiePackageDetails, BiePackageListEntry, BiePackageListRequest} from './bie-package';
import {map} from 'rxjs/operators';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {zip} from '../../../common/utility';

@Injectable()
export class BiePackageService {

  constructor(private http: HttpClient) {
  }

  getBiePackageList(request: BiePackageListRequest): Observable<PageResponse<BiePackageListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId)
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    const { sortActives, sortDirections } = request.page;

    if (sortActives?.length && sortDirections?.length) {
      const orderBy = zip(sortActives, sortDirections)
          .map(([active, direction]) => `${direction === 'desc' ? '-' : '+'}${active}`)
          .join(',');

      params = params.set('orderBy', orderBy);
    }
    if (request.filters.versionId) {
      params = params.set('versionId', request.filters.versionId);
    }
    if (request.filters.versionName) {
      params = params.set('versionName', request.filters.versionName);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    if (request.filters.den) {
      params = params.set('den', request.filters.den);
    }
    if (request.filters.businessTerm) {
      params = params.set('businessTerm', request.filters.businessTerm);
    }
    if (request.filters.version) {
      params = params.set('version', request.filters.version);
    }
    if (request.filters.remark) {
      params = params.set('remark', request.filters.remark);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.releases && request.releases.length > 0) {
      params = params.set('releaseIds', request.releases.map(e => e.releaseId.toString()).join(','));
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
    return this.http.get<PageResponse<BiePackageListEntry>>('/api/bie-packages', {params}).pipe(
        map((res: PageResponse<BiePackageListEntry>) => ({
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

  get(biePackageId: number): Observable<BiePackageDetails> {
    return this.http.get<BiePackageDetails>('/api/bie-packages/' + biePackageId).pipe(
        map((res: BiePackageDetails) => ({
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

  getBieListInBiePackage(request: BieListInBiePackageRequest): Observable<PageResponse<BieListEntry>> {
    let params = new HttpParams();

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
    if (request.filters.businessContext) {
      params = params.set('businessContext', request.filters.businessContext);
    }
    if (request.filters.version) {
      params = params.set('version', request.filters.version);
    }
    if (request.filters.remark) {
      params = params.set('remark', request.filters.remark);
    }

    return this.http.get<PageResponse<BieListEntry>>('/api/bie-packages/' + request.biePackageId + '/bies', {params}).pipe(
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

  create(libraryId: number): Observable<number> {
    return this.http.post('/api/bie-packages', {
      libraryId
    }).pipe(map(resp => Number(resp['biePackageId'])));
  }

  update(biePackage: BiePackageDetails): Observable<any> {
    return this.http.put<any>('/api/bie-packages/' + biePackage.biePackageId, {
      versionId: biePackage.versionId,
      versionName: biePackage.versionName,
      description: biePackage.description
    });
  }

  updateState(biePackageId: number, state: string): Observable<any> {
    return this.http.put<any>('/api/bie-packages/' + biePackageId, {
      state
    });
  }

  delete(...biePackageIdList: number[]): Observable<any> {
    if (biePackageIdList.length === 1) {
      return this.http.delete('/api/bie-packages/' + biePackageIdList[0]);
    } else {
      return this.http.delete('/api/bie-packages', {
        body: {biePackageIdList}
      });
    }
  }

  transferOwnership(biePackageId: number, targetLoginId: string,
                    sendNotification?: boolean, mailParameters?: any): Observable<any> {
    let url = '/api/bie-packages/' + biePackageId + '/transfer';
    return this.http.patch<any>(url, {
      targetLoginId,
      sendNotification: ((sendNotification) ? 'true' : 'false'),
      mailParameters: mailParameters
    });
  }

  copy(biePackageId: number): Observable<any> {
    return this.http.put<any>('/api/bie-packages/copy', {
      biePackageIdList: [biePackageId]
    });
  }

  addBieToBiePackage(biePackageId: number, ...topLevelAsbiepIdList: number[]): Observable<any> {
    return this.http.post('/api/bie-packages/' + biePackageId + '/bies', {
      topLevelAsbiepIdList
    });
  }

  deleteBieInBiePackage(biePackageId: number, ...topLevelAsbiepIdList: number[]): Observable<any> {
    if (topLevelAsbiepIdList.length === 1) {
      return this.http.delete('/api/bie-packages/' + biePackageId + '/bies/' + topLevelAsbiepIdList[0]);
    } else {
      return this.http.delete('/api/bie-packages/' + biePackageId + '/bies', {
        body: {topLevelAsbiepIdList}
      });
    }
  }

  generateBiePackage(biePackageId: number, options: {}, ...topLevelAsbiepIdList: number[]): Observable<HttpResponse<Blob>> {
    let params: HttpParams = new HttpParams();
    if (topLevelAsbiepIdList && topLevelAsbiepIdList.length > 0) {
      params = params.set('topLevelAsbiepIdList', topLevelAsbiepIdList.map(e => e.toString()).join(','));
    }
    if (!!options) {
      Object.entries(options).forEach(([key, value]) => {
        params = params.set(key, options[key]);
      });
    }

    return this.http.get('/api/bie-packages/' + biePackageId + '/generate', {
      params,
      observe: 'response',
      responseType: 'blob'
    });
  }

  createUpliftBiePackage(biePackageId: number, targetReleaseId: number): Observable<any> {
    return this.http.post<any>('/api/bie-packages/' + biePackageId + '/uplifting', {
      targetReleaseId
    });
  }
}
