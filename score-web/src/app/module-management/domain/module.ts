import {HttpParams} from '@angular/common/http';
import {ParamMap} from '@angular/router';
import {ScoreUser} from '../../authentication/domain/auth';
import {PageRequest, WhoAndWhen} from '../../basis/basis';
import {base64Decode, base64Encode} from '../../common/utility';
import {LibrarySummary} from '../../library-management/domain/library';
import {ReleaseSummary} from '../../release-management/domain/release';

export class SimpleModule {
  moduleId: number;
  module: string;
}

export class Module {
  moduleId: number;
  name: string;
  namespaceId: number;
  parentModuleId: number;
  versionNum: string;
  path: string;
}

export class ModuleElement {
  moduleId: number;
  name: string;
  namespaceUri: string;
  parentModuleId: number;
  namespaceId: number;
  versionNum: string;
  directory: boolean;
  moduleSetId: number;
  path: string;
  children: ModuleElement[];
}

export class ModuleSetListRequest {
  library: LibrarySummary = new LibrarySummary();
  filters: {
    name: string;
    description: string;
  };
  updaterLoginIdList: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
    this.page.sortActive = params.get('sortActive');
    if (!this.page.sortActive) {
      this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    }
    this.page.sortDirection = params.get('sortDirection');
    if (!this.page.sortDirection) {
      this.page.sortDirection = (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    }
    if (params.get('pageIndex')) {
      this.page.pageIndex = Number(params.get('pageIndex'));
    } else {
      this.page.pageIndex = (defaultPageRequest) ? defaultPageRequest.pageIndex : 0;
    }
    if (params.get('pageSize')) {
      this.page.pageSize = Number(params.get('pageSize'));
    } else {
      this.page.pageSize = (defaultPageRequest) ? defaultPageRequest.pageSize : 0;
    }

    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      name: params.get('name') || '',
      description: params.get('description') || '',
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.name && this.filters.name.length > 0) {
      params = params.set('name', '' + this.filters.name);
    }
    if (this.filters.description && this.filters.description.length > 0) {
      params = params.set('description', '' + this.filters.description);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class ModuleSetListEntry {
  moduleSetId: number;
  libraryId: number;
  guid: string;
  name: string;
  description: string;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ModuleSetSummary {
  moduleSetId: number;
  libraryId: number;
  guid: string;
  name: string;
  description: string;
}

export class ModuleSet {
  moduleSetId: number;
  libraryId: number;
  guid: string;
  name: string;
  description: string;
  lastUpdateTimestamp: Date;
  lastUpdatedBy: ScoreUser;
  createModuleSetRelease: boolean;
  targetReleaseId: number;
  targetModuleSetReleaseId: number;
}

export class ModuleSetMetadata {
  numberOfDirectories = 0;
  numberOfFiles = 0;
}

export class ModuleSetModuleListRequest {
  filters: {
    path: string;
    namespaceUri: string;
  };
  moduleSetId: number;
  updaterLoginIdList: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
    this.page.sortActive = params.get('sortActive');
    if (!this.page.sortActive) {
      this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    }
    this.page.sortDirection = params.get('sortDirection');
    if (!this.page.sortDirection) {
      this.page.sortDirection = (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    }
    if (params.get('pageIndex')) {
      this.page.pageIndex = Number(params.get('pageIndex'));
    } else {
      this.page.pageIndex = (defaultPageRequest) ? defaultPageRequest.pageIndex : 0;
    }
    if (params.get('pageSize')) {
      this.page.pageSize = Number(params.get('pageSize'));
    } else {
      this.page.pageSize = (defaultPageRequest) ? defaultPageRequest.pageSize : 0;
    }

    this.moduleSetId = Number(params.get('moduleSetId'));
    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      path: params.get('path') || '',
      namespaceUri: params.get('namespaceUri') || '',
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    params = params.set('moduleSetId', '' + this.moduleSetId);
    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.path && this.filters.path.length > 0) {
      params = params.set('path', '' + this.filters.path);
    }
    if (this.filters.namespaceUri && this.filters.namespaceUri.length > 0) {
      params = params.set('namespaceUri', '' + this.filters.namespaceUri);
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class ModuleSetModule {
  moduleId: number;
  path: string;
  namespaceId: number;
  namespaceUri: string;
  lastUpdateTimestamp: Date;
  lastUpdatedBy: ScoreUser;
  assigned: boolean;
}

export class ModuleSetReleaseSummary {
  library: LibrarySummary;
  release: ReleaseSummary;
  moduleSet: ModuleSetSummary;

  moduleSetReleaseId: number;
  name: string;
  description: string;
  isDefault: boolean;
}

export class ModuleSetReleaseListEntry {
  moduleSetReleaseId: number;
  moduleSetId: number;
  libraryId: number;
  releaseId: number;
  releaseNum: string;
  name: string;
  description: string;
  isDefault: boolean;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ModuleSetReleaseDetails {
  library: LibrarySummary;
  release: ReleaseSummary;
  moduleSet: ModuleSetSummary;

  moduleSetReleaseId: number;
  name: string;
  description: string;
  isDefault: boolean;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ModuleSetRelease {
  moduleSetReleaseId: number;

  moduleSetReleaseName: string;
  moduleSetReleaseDescription: string;

  /* moduleSet */
  moduleSetId: number;
  moduleSetName: string;

  /* release */
  releaseId: number;
  releaseNum: string;

  /* library */
  libraryId: number;

  isDefault: boolean;
  createdBy: ScoreUser;
  lastUpdatedBy: ScoreUser;
  creationTimestamp: Date;
  lastUpdateTimestamp: Date;
}

export class ModuleSetReleaseListRequest {
  library: LibrarySummary = new LibrarySummary();
  filters: {
    name: string;
  };
  releaseId: number;
  isDefault: boolean;
  updaterLoginIdList: string[] = [];
  updatedDate: {
    start: Date,
    end: Date,
  };
  page: PageRequest = new PageRequest();

  constructor(paramMap?: ParamMap, defaultPageRequest?: PageRequest) {
    const q = (paramMap) ? paramMap.get('q') : undefined;
    const params = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
    this.page.sortActive = params.get('sortActive');
    if (!this.page.sortActive) {
      this.page.sortActive = (defaultPageRequest) ? defaultPageRequest.sortActive : '';
    }
    this.page.sortDirection = params.get('sortDirection');
    if (!this.page.sortDirection) {
      this.page.sortDirection = (defaultPageRequest) ? defaultPageRequest.sortDirection : '';
    }
    if (params.get('pageIndex')) {
      this.page.pageIndex = Number(params.get('pageIndex'));
    } else {
      this.page.pageIndex = (defaultPageRequest) ? defaultPageRequest.pageIndex : 0;
    }
    if (params.get('pageSize')) {
      this.page.pageSize = Number(params.get('pageSize'));
    } else {
      this.page.pageSize = (defaultPageRequest) ? defaultPageRequest.pageSize : 0;
    }

    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    if (params.get('releaseId')) {
      this.releaseId = Number(params.get('releaseId'));
    }
    if (params.get('isDefault')) {
      this.isDefault = 'true' === params.get('isDefault');
    }
    this.filters = {
      name: params.get('name') || '',
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.name && this.filters.name.length > 0) {
      params = params.set('name', '' + this.filters.name);
    }
    if (this.releaseId) {
      params.set('releaseId', '' + this.releaseId);
    }
    if (this.isDefault !== undefined) {
      params.set('isDefault', (this.isDefault) ? 'true' : 'false');
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }

}

export class ModuleSetReleaseValidateResponse {
  results: Map<string, string>;
  requestId: string;
  progress: number;
  length: number;
  done: boolean;
}

export interface Tile {
  elements: ModuleElement[];
  current: ModuleElement;
}
