import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageResponse, PaginationResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';
import {
  ModuleSet,
  ModuleSetListRequest,
  ModuleSetModule,
  ModuleSetModuleListRequest,
  ModuleSetRelease,
  ModuleSetReleaseListRequest
} from './module';

@Injectable()
export class ModuleService {

  constructor(private http: HttpClient) {
  }

  getModuleSetList(request?: ModuleSetListRequest): Observable<PaginationResponse<ModuleSet>> {
    if (!request) {
      request = new ModuleSetListRequest();
      request.page.pageIndex = -1;
      request.page.pageSize = -1;
    }

    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    return this.http.get<PaginationResponse<ModuleSet>>('/api/module_set', {params: params})
      .pipe(map((resp: PaginationResponse<ModuleSet>) => {
        resp.results.forEach(e => {
          e.lastUpdateTimestamp = new Date(e.lastUpdateTimestamp);
        });
        return resp;
      }));
  }

  discardModuleSet(moduleSetId: number): Observable<any> {
    return this.http.delete<any>('/api/module_set/' + moduleSetId);
  }

  discardModuleSetRelease(moduleSetReleaseId: number): Observable<any> {
    return this.http.delete<any>('/api/module_set_release/' + moduleSetReleaseId);
  }

  getModuleSet(moduleSetId: number): Observable<ModuleSet> {
    return this.http.get<ModuleSet>('/api/module_set/' + moduleSetId);
  }

  getModuleSetRelease(moduleSetReleaseId: number): Observable<ModuleSetRelease> {
    return this.http.get<ModuleSetRelease>('/api/module_set_release/' + moduleSetReleaseId);
  }

  createModuleSet(moduleSet: ModuleSet): Observable<ModuleSet> {
    return this.http.put<ModuleSet>('/api/module_set', {
      name: moduleSet.name,
      description: moduleSet.description
    });
  }

  createModuleSetRelease(moduleSetRelease: ModuleSetRelease): Observable<ModuleSetRelease> {
    return this.http.put<ModuleSetRelease>('/api/module_set_release', {
      releaseId: moduleSetRelease.releaseId,
      moduleSetId: moduleSetRelease.moduleSetId
    });
  }

  updateModuleSet(moduleSet: ModuleSet): Observable<any> {
    return this.http.post<any>('/api/module_set/' + moduleSet.moduleSetId, {
      name: moduleSet.name,
      description: moduleSet.description
    });
  }

  updateModuleSetRelease(moduleSetRelease: ModuleSetRelease): Observable<any> {
    return this.http.post<any>('/api/module_set_Release/' + moduleSetRelease.moduleSetReleaseId, {
      releaseId: moduleSetRelease.releaseId,
      moduleSetId: moduleSetRelease.moduleSetId
    });
  }

  getModuleSetModuleList(request: ModuleSetModuleListRequest): Observable<PaginationResponse<ModuleSetModule>> {
    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    if (request.filters.path) {
      params = params.set('path', request.filters.path);
    }
    if (request.filters.namespaceUri) {
      params = params.set('namespaceUri', request.filters.namespaceUri);
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

    const uri = '/api/module_set/' + request.moduleSetId + '/module';
    return this.http.get<PaginationResponse<ModuleSetModule>>(uri, {params});
  }

  getModuleSetReleaseList(request?: ModuleSetReleaseListRequest): Observable<PaginationResponse<ModuleSetRelease>> {
    if (!request) {
      request = new ModuleSetReleaseListRequest();
      request.page.pageIndex = -1;
      request.page.pageSize = -1;
    }

    let params = new HttpParams()
      .set('sortActive', request.page.sortActive)
      .set('sortDirection', request.page.sortDirection)
      .set('pageIndex', '' + request.page.pageIndex)
      .set('pageSize', '' + request.page.pageSize);

    if (request.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', request.updaterLoginIds.join(','));
    }
    if (request.updatedDate.start) {
      params = params.set('updateStart', '' + request.updatedDate.start.getTime());
    }
    if (request.updatedDate.end) {
      params = params.set('updateEnd', '' + request.updatedDate.end.getTime());
    }
    if (request.filters.keyword) {
      params = params.set('keyword', request.filters.keyword);
    }
    return this.http.get<PaginationResponse<ModuleSetRelease>>('/api/module_set_release_list', {params: params})
      .pipe(map((resp: PaginationResponse<ModuleSetRelease>) => {
        resp.results.forEach(e => {
          e.lastUpdateTimestamp = new Date(e.lastUpdateTimestamp);
        });
        return resp;
      }));
  }
}
