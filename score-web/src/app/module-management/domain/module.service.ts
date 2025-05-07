import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {PageResponse} from '../../basis/basis';
import {AssignableMap, AssignableNode} from '../../release-management/domain/release';
import {
  ModuleElement,
  ModuleSet,
  ModuleSetListEntry,
  ModuleSetListRequest,
  ModuleSetMetadata,
  ModuleSetRelease,
  ModuleSetReleaseDetails,
  ModuleSetReleaseListEntry,
  ModuleSetReleaseListRequest,
  ModuleSetReleaseSummary,
  ModuleSetReleaseValidateResponse,
  ModuleSetSummary
} from './module';

@Injectable()
export class ModuleService {

  constructor(private http: HttpClient) {
  }

  getModuleSetSummaries(libraryId: number): Observable<ModuleSetSummary[]> {
    let params = new HttpParams()
        .set('libraryId', '' + libraryId);
    return this.http.get<ModuleSetSummary[]>('/api/module-sets/summaries', {params});
  }

  getModuleSetList(request?: ModuleSetListRequest): Observable<PageResponse<ModuleSetListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId)
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (!!request.updatedDate.start || !!request.updatedDate.end) {
      params = params.set('lastUpdatedOn',
          '[' + (!!request.updatedDate.start ? request.updatedDate.start.getTime() : '') + '~' +
          (!!request.updatedDate.end ? request.updatedDate.end.getTime() : '') + ']');
    }
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.filters.description) {
      params = params.set('description', request.filters.description);
    }
    return this.http.get<PageResponse<ModuleSetListEntry>>('/api/module-sets', {params}).pipe(
        map((res: PageResponse<ModuleSetListEntry>) => ({
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

  getModuleSet(moduleSetId: number): Observable<ModuleSet> {
    return this.http.get<ModuleSet>('/api/module-sets/' + moduleSetId);
  }

  getModuleSetMetadata(moduleSetId: number): Observable<ModuleSetMetadata> {
    return this.http.get<ModuleSetMetadata>('/api/module-sets/' + moduleSetId + '/metadata');
  }

  getModules(moduleSetId: number): Observable<any> {
    const url = '/api/module-sets/' + moduleSetId + '/modules';
    return this.http.get<any>(url);
  }

  createModuleSet(moduleSet: ModuleSet): Observable<ModuleSet> {
    return this.http.post<ModuleSet>('/api/module-sets', {
      libraryId: moduleSet.libraryId,
      name: moduleSet.name,
      description: moduleSet.description,
      createModuleSetRelease: moduleSet.createModuleSetRelease,
      targetReleaseId: moduleSet.targetReleaseId,
      targetModuleSetReleaseId: moduleSet.targetModuleSetReleaseId,
    });
  }

  updateModuleSet(moduleSet: ModuleSet): Observable<any> {
    return this.http.put<any>('/api/module-sets/' + moduleSet.moduleSetId, {
      name: moduleSet.name,
      description: moduleSet.description
    });
  }

  discardModuleSet(moduleSetId: number): Observable<any> {
    return this.http.delete<any>('/api/module-sets/' + moduleSetId);
  }

  getModule(moduleSetId: number, moduleId: number): Observable<any> {
    return this.http.get<any>('/api/module-sets/' + moduleSetId + '/modules/' + moduleId, {});
  }

  createModule(module: ModuleElement): Observable<any> {
    return this.http.post<any>('/api/module-sets/' + module.moduleSetId + '/modules', module);
  }

  updateModule(element: ModuleElement): Observable<any> {
    return this.http.put<any>('/api/module-sets/' + element.moduleSetId + '/modules/' + element.moduleId, {
      moduleId: element.moduleId,
      name: element.name,
      namespaceId: element.namespaceId,
      versionNum: element.versionNum,
    });
  }

  discardModule(element: ModuleElement): Observable<any> {
    return this.http.delete<any>('/api/module-sets/' + element.moduleSetId + '/modules/' + element.moduleId);
  }

  copyModule(element: ModuleElement, moduleSetId: number, copySubModules: boolean, parentModuleId: number): Observable<any> {
    return this.http.post<any>('/api/module-sets/' + moduleSetId + '/modules/' + parentModuleId + '/copy', {
      targetModuleId: element.moduleId,
      copySubModules,
      moduleSetId,
      parentModuleId
    });
  }

  /* for Module Set Release */

  getModuleSetReleaseDetails(moduleSetReleaseId: number): Observable<ModuleSetReleaseDetails> {
    return this.http.get<ModuleSetReleaseDetails>('/api/module-set-releases/' + moduleSetReleaseId);
  }

  createModuleSetRelease(moduleSetRelease: ModuleSetRelease, basedModuleSetReleaseId?: number): Observable<any> {
    const params = {
      name: moduleSetRelease.moduleSetReleaseName,
      description: moduleSetRelease.moduleSetReleaseDescription,
      releaseId: moduleSetRelease.releaseId,
      moduleSetId: moduleSetRelease.moduleSetId,
      isDefault: moduleSetRelease.isDefault,
      baseModuleSetReleaseId: undefined
    };

    if (basedModuleSetReleaseId) {
      params.baseModuleSetReleaseId = basedModuleSetReleaseId;
    }
    return this.http.post<any>('/api/module-set-releases', params);
  }

  updateModuleSetRelease(moduleSetRelease: ModuleSetReleaseDetails): Observable<any> {
    return this.http.put<any>('/api/module-set-releases/' + moduleSetRelease.moduleSetReleaseId, {
      name: moduleSetRelease.name,
      description: moduleSetRelease.description,
      releaseId: moduleSetRelease.release.releaseId,
      moduleSetId: moduleSetRelease.moduleSet.moduleSetId,
      isDefault: moduleSetRelease.isDefault
    });
  }

  discardModuleSetRelease(moduleSetReleaseId: number): Observable<any> {
    return this.http.delete<any>('/api/module-set-releases/' + moduleSetReleaseId);
  }

  getModuleSetReleaseSummaries(libraryId: number,
                               releaseId?: number, name?: string): Observable<ModuleSetReleaseSummary[]> {
    let params = new HttpParams().set('libraryId', libraryId);
    if (!!releaseId) {
      params = params.set('releaseId', releaseId);
    }
    if (!!name) {
      params = params.set('name', name);
    }
    return this.http.get<ModuleSetReleaseSummary[]>('/api/module-set-releases/summaries', {params});
  }

  getModuleSetReleaseList(request?: ModuleSetReleaseListRequest): Observable<PageResponse<ModuleSetReleaseListEntry>> {
    let params = new HttpParams()
        .set('libraryId', '' + request.library.libraryId)
        .set('pageIndex', '' + request.page.pageIndex)
        .set('pageSize', '' + request.page.pageSize);

    if (!!request.page.sortActive && !!request.page.sortDirection) {
      params = params.set('orderBy', ((request.page.sortDirection === 'desc') ? '-' : '+') + request.page.sortActive);
    }
    if (request.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', request.updaterLoginIdList.join(','));
    }
    if (!!request.updatedDate.start || !!request.updatedDate.end) {
      params = params.set('lastUpdatedOn',
          '[' + (!!request.updatedDate.start ? request.updatedDate.start.getTime() : '') + '~' +
          (!!request.updatedDate.end ? request.updatedDate.end.getTime() : '') + ']');
    }
    if (request.filters.name) {
      params = params.set('name', request.filters.name);
    }
    if (request.releaseId) {
      params = params.set('releaseId', request.releaseId);
    }
    if (request.isDefault) {
      params = params.set('isDefault', request.isDefault);
    }
    return this.http.get<PageResponse<ModuleSetReleaseListEntry>>('/api/module-set-releases', {params}).pipe(
        map((res: PageResponse<ModuleSetReleaseListEntry>) => ({
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

  validate(moduleSetReleaseId: number): Observable<ModuleSetReleaseValidateResponse> {
    return this.http.get<ModuleSetReleaseValidateResponse>('/api/module-set-releases/' + moduleSetReleaseId + '/validate');
  }

  progressValidation(moduleSetReleaseId: number, requestId: string): Observable<ModuleSetReleaseValidateResponse> {
    return this.http.get<ModuleSetReleaseValidateResponse>('/api/module-set-releases/' + moduleSetReleaseId + '/validate/' + requestId);
  }

  export(moduleSetReleaseId: number): Observable<HttpResponse<Blob>> {
    return this.http.get('/api/module-set-releases/' + moduleSetReleaseId + '/export', {
      observe: 'response',
      responseType: 'blob'
    });
  }

  getAssignable(moduleSetReleaseId: number): Observable<AssignableMap> {
    return this.http.get<AssignableMap>('/api/module-set-releases/' + moduleSetReleaseId + '/assignable');
  }

  getAssigned(moduleSetReleaseId: number, moduleId: number): Observable<AssignableMap> {
    return this.http.get<AssignableMap>('/api/module-set-releases/' + moduleSetReleaseId + '/assigned?moduleId=' + moduleId);
  }

  createAssign(moduleSetRelease: ModuleSetReleaseDetails, moduleId: number, nodes: AssignableNode[]): Observable<any> {
    return this.http.post<any>('/api/module-set-releases/' + moduleSetRelease.moduleSetReleaseId + '/assign', {
      nodes: nodes.map(node => {
        if ('ACC' === node.type) {
          return {accManifestId: node.manifestId};
        }
        else if ('ASCCP' === node.type) {
          return {asccpManifestId: node.manifestId};
        }
        else if ('BCCP' === node.type) {
          return {bccpManifestId: node.manifestId};
        }
        else if ('DT' === node.type) {
          return {dtManifestId: node.manifestId};
        }
        else if ('CODE_LIST' === node.type) {
          return {codeListManifestId: node.manifestId};
        }
        else if ('AGENCY_ID_LIST' === node.type) {
          return {agencyIdListManifestId: node.manifestId};
        }
        else if ('XBT' === node.type) {
          return {xbtManifestId: node.manifestId};
        }
      }),
      moduleId,
      moduleSetId: moduleSetRelease.moduleSet.moduleSetId,
      moduleSetReleaseId: moduleSetRelease.moduleSetReleaseId,
      releaseId: moduleSetRelease.release.releaseId
    });
  }

  discardAssign(moduleSetRelease: ModuleSetReleaseDetails, moduleId: number, nodes: AssignableNode[]): Observable<any> {
    return this.http.post<any>('/api/module-set-releases/' + moduleSetRelease.moduleSetReleaseId + '/unassign', {
      nodes: nodes.map(node => {
        if ('ACC' === node.type) {
          return {accManifestId: node.manifestId};
        }
        else if ('ASCCP' === node.type) {
          return {asccpManifestId: node.manifestId};
        }
        else if ('BCCP' === node.type) {
          return {bccpManifestId: node.manifestId};
        }
        else if ('DT' === node.type) {
          return {dtManifestId: node.manifestId};
        }
        else if ('CODE_LIST' === node.type) {
          return {codeListManifestId: node.manifestId};
        }
        else if ('AGENCY_ID_LIST' === node.type) {
          return {agencyIdListManifestId: node.manifestId};
        }
        else if ('XBT' === node.type) {
          return {xbtManifestId: node.manifestId};
        }
      }),
      moduleId,
      moduleSetId: moduleSetRelease.moduleSet.moduleSetId,
      moduleSetReleaseId: moduleSetRelease.moduleSetReleaseId,
      releaseId: moduleSetRelease.release.releaseId
    });
  }
}
