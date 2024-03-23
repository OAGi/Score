import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageResponse} from '../../../basis/basis';
import {BieListInBiePackageRequest, BiePackage, BiePackageListRequest} from './bie-package';
import {map} from 'rxjs/operators';
import {BieList} from '../../bie-list/domain/bie-list';

@Injectable()
export class BiePackageService {

  constructor(private http: HttpClient) {
  }

  getBiePackageList(request: BiePackageListRequest): Observable<PageResponse<BiePackage>> {
    let params = new HttpParams()
      .set('sortActives', request.page.sortActives.join(','))
      .set('sortDirections', request.page.sortDirections.join(','))
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);
    if (request.filters.versionId) {
      params = params.set('versionId', request.filters.versionId);
    }
    if (request.filters.versionName) {
      params = params.set('versionName', request.filters.versionName);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    if (request.states.length > 0) {
      params = params.set('states', request.states.join(','));
    }
    if (request.releases && request.releases.length > 0) {
      params = params.set('releaseIds', request.releases.map(e => e.releaseId.toString()).join(','));
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
    return this.http.get<PageResponse<BiePackage>>('/api/bie_packages', {params});
  }

  getBieListInBiePackage(request: BieListInBiePackageRequest): Observable<PageResponse<BieList>> {
    let params = new HttpParams()
      .set('sortActives', request.page.sortActives.join(','))
      .set('sortDirections', request.page.sortDirections.join(','))
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    return this.http.get<PageResponse<BieList>>('/api/bie_packages/' + request.biePackageId + '/bie_list', {params});
  }

  create(): Observable<number> {
    return this.http.post('/api/bie_packages', {}).pipe(map(resp => Number(resp['biePackageId'])));
  }

  get(biePackageId: number): Observable<BiePackage> {
    return this.http.get<BiePackage>('/api/bie_packages/' + biePackageId);
  }

  update(biePackage: BiePackage): Observable<any> {
    return this.http.post<any>('/api/bie_packages/' + biePackage.biePackageId, {
      versionId: biePackage.versionId,
      versionName: biePackage.versionName,
      description: biePackage.description
    });
  }

  updateState(biePackageId: number, state: string): Observable<any> {
    return this.http.post<any>('/api/bie_packages/' + biePackageId, {
      state
    });
  }

  delete(...biePackageIdList: number[]): Observable<any> {
    if (biePackageIdList.length === 1) {
      return this.http.delete('/api/bie_packages/' + biePackageIdList[0]);
    } else {
      return this.http.delete('/api/bie_packages', {
        body: {biePackageIdList}
      });
    }
  }

  addBieToBiePackage(biePackageId: number, ...topLevelAsbiepIdList: number[]): Observable<any> {
    return this.http.post('/api/bie_packages/' + biePackageId + '/bie', {
      topLevelAsbiepIdList
    });
  }

  deleteBieInBiePackage(biePackageId: number, ...topLevelAsbiepIdList: number[]): Observable<any> {
    if (topLevelAsbiepIdList.length === 1) {
      return this.http.delete('/api/bie_packages/' + biePackageId + '/bie/' + topLevelAsbiepIdList[0]);
    } else {
      return this.http.delete('/api/bie_packages/' + biePackageId + '/bie', {
        body: {topLevelAsbiepIdList}
      });
    }
  }
}
