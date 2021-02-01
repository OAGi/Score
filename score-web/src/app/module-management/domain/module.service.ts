import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PageResponse} from '../../basis/basis';
import {map} from 'rxjs/operators';
import {ModuleSet, ModuleSetListRequest, ModuleSetModule, ModuleSetModuleListRequest} from './module';

@Injectable()
export class ModuleService {

  constructor(private http: HttpClient) {
  }

  getModuleSetList(request?: ModuleSetListRequest): Observable<PageResponse<ModuleSet>> {
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
    return this.http.get<PageResponse<ModuleSet>>('/api/module_set', {params: params})
      .pipe(map((resp: PageResponse<ModuleSet>) => {
        resp.list.forEach(e => {
          e.lastUpdateTimestamp = new Date(e.lastUpdateTimestamp);
        });
        return resp;
      }));
  }

  discard(moduleSetId: number): Observable<any> {
    return this.http.delete<any>('/api/module_set/' + moduleSetId);
  }

  getModuleSet(moduleSetId: number): Observable<ModuleSet> {
    return this.http.get<ModuleSet>('/api/module_set/' + moduleSetId);
  }

  createModuleSet(moduleSet: ModuleSet): Observable<ModuleSet> {
    return this.http.put<ModuleSet>('/api/module_set', {
      name: moduleSet.name,
      description: moduleSet.description
    });
  }

  updateModuleSet(moduleSet: ModuleSet): Observable<any> {
    return this.http.post<any>('/api/module_set/' + moduleSet.moduleSetId, {
      name: moduleSet.name,
      description: moduleSet.description
    });
  }

  getModuleSetModuleList(request: ModuleSetModuleListRequest): Observable<PageResponse<ModuleSetModule>> {
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
    return this.http.get<PageResponse<ModuleSetModule>>(uri, {params});
  }
}
