import {PageRequest, WhoAndWhen} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';
import {LibrarySummary} from '../../library-management/domain/library';

export class ReleaseSummary {
  releaseId: number;
  releaseNum: string;
  state: string;
  workingRelease: boolean;
}

export class ReleaseListEntry {
  releaseId: number;
  guid: string;
  releaseNum: string;
  state: string;
  namespaceId: number;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class ReleaseDetails {
  releaseId: number;
  releaseNum: string;
  releaseNote: string;
  releaseLicense: string;
  state: string;
  libraryId: number;
  libraryName: string;
  namespaceId: number;
  latestRelease: boolean;
}

export class ReleaseListRequest {
  library: LibrarySummary = new LibrarySummary();
  filters: {
    releaseNum: string;
  };
  excludes: string[] = [];
  states: string[] = [];
  namespaces: number[] = [];
  creatorLoginIdList: string[] = [];
  createdDate: {
    start: Date,
    end: Date,
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

    this.excludes = [];
    this.states = (params.get('states')) ? Array.from(params.get('states').split(',')) : [];
    this.namespaces = (params.get('namespaces')) ? Array.from(params.get('namespaces').split(',')).map(e => Number(e)) : [];

    this.creatorLoginIdList = (params.get('creatorLoginIdList')) ? Array.from(params.get('creatorLoginIdList').split(',')) : [];
    this.createdDate = {
      start: (params.get('createdDateStart')) ? new Date(params.get('createdDateStart')) : null,
      end: (params.get('createdDateEnd')) ? new Date(params.get('createdDateEnd')) : null
    };

    this.updaterLoginIdList = (params.get('updaterLoginIdList')) ? Array.from(params.get('updaterLoginIdList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };

    this.filters = {
      releaseNum: params.get('releaseNum') || ''
    };
  }

  toQuery(): string {
    let params = new HttpParams()
      .set('sortActive', this.page.sortActive)
      .set('sortDirection', this.page.sortDirection)
      .set('pageIndex', '' + this.page.pageIndex)
      .set('pageSize', '' + this.page.pageSize);

    if (this.states && this.states.length > 0) {
      params = params.set('states', this.states.join(','));
    }
    if (this.creatorLoginIdList && this.creatorLoginIdList.length > 0) {
      params = params.set('creatorLoginIdList', this.creatorLoginIdList.join(','));
    }
    if (this.createdDate.start) {
      params = params.set('createdDateStart', '' + this.createdDate.start.toUTCString());
    }
    if (this.createdDate.end) {
      params = params.set('createdDateEnd', '' + this.createdDate.end.toUTCString());
    }
    if (this.updaterLoginIdList && this.updaterLoginIdList.length > 0) {
      params = params.set('updaterLoginIdList', this.updaterLoginIdList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updatedDateStart', '' + this.updatedDate.start.toUTCString());
    }
    if (this.updatedDate.end) {
      params = params.set('updatedDateEnd', '' + this.updatedDate.end.toUTCString());
    }
    if (this.filters.releaseNum && this.filters.releaseNum.length > 0) {
      params = params.set('releaseNum', '' + this.filters.releaseNum);
    }
    if (this.namespaces && this.namespaces.length > 0) {
      params = params.set('namespaces', this.namespaces.map(e => '' + e).join(','));
    }
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class ReleaseResponse {
  release: ReleaseDetails;
  status: string;
  statusMessage: string;
}

export const WorkingRelease = {
  releaseNum: 'Working',
  // @TODO: this must be retrieved from server.
  releaseId: 2,
  state: '',
  working: true
};

export class AssignableMap {
  assignableAccManifestMap: Map<number, AssignableNode>;
  assignableAsccpManifestMap: Map<number, AssignableNode>;
  assignableBccpManifestMap: Map<number, AssignableNode>;
  assignableDtManifestMap: Map<number, AssignableNode>;
  assignableCodeListManifestMap: Map<number, AssignableNode>;
  assignableAgencyIdListManifestMap: Map<number, AssignableNode>;
  assignableXbtManifestMap: Map<number, AssignableNode>;

  assignedAccManifestMap: Map<number, AssignableNode>;
  assignedAsccpManifestMap: Map<number, AssignableNode>;
  assignedBccpManifestMap: Map<number, AssignableNode>;
  assignedDtManifestMap: Map<number, AssignableNode>;
  assignedCodeListManifestMap: Map<number, AssignableNode>;
  assignedAgencyIdListManifestMap: Map<number, AssignableNode>;
  assignedXbtManifestMap: Map<number, AssignableNode>;
}

export class AssignableNode {
  manifestId: number;
  den: string;
  type: string;
  state: string;
  revision: number;
  ownerUsername: string;
  timestamp: Date;
  visible = true;
  errors: ValidationMessage[];
}

export class AssignableList {
  assignableList: AssignableNode[];
  assignedList: AssignableNode[];

  constructor() {
    this.assignableList = [];
    this.assignedList = [];
  }
}

export class ReleaseValidationRequest {
  assignedAccComponentManifestIds: number[] = [];
  assignedAsccpComponentManifestIds: number[] = [];
  assignedBccpComponentManifestIds: number[] = [];
  assignedCodeListComponentManifestIds: number[] = [];
  assignedAgencyIdListComponentManifestIds: number[] = [];
  assignedDtComponentManifestIds: number[] = [];
}

export class ValidationMessage {
  level: string;
  message: string;
  code: string;
}

export class ReleaseValidationResponse {
  succeed: boolean;
  statusMapForAcc: Map<number, ValidationMessage[]>;
  statusMapForAsccp: Map<number, ValidationMessage[]>;
  statusMapForBccp: Map<number, ValidationMessage[]>;
  statusMapForCodeList: Map<number, ValidationMessage[]>;
  statusMapForAgencyIdList: Map<number, ValidationMessage[]>;
  statusMapForDt: Map<number, ValidationMessage[]>;
}
