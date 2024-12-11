import {PageRequest} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';
import {Library} from '../../library-management/domain/library';

export class SimpleRelease {
  releaseId: number;
  releaseNum: string;
  state: string;
  workingRelease: boolean;
}

export class ReleaseList {
  releaseId: number;
  releaseNum: string;
  state: string;
  createdBy: string;
  creationTimestamp: Date;
  lastUpdatedBy: string;
  lastUpdateTimestamp: Date;
}

export class ReleaseDetail {
  releaseId: number;
  releaseNum: string;
  releaseNote: string;
  releaseLicense: string;
  state: string;
  libraryId: number;
  namespaceId: number;
  latestRelease: boolean;
}

export class ReleaseListRequest {
  library: Library = new Library();
  filters: {
    releaseNum: string;
  };
  excludes: string[] = [];
  states: string[] = [];
  namespaces: number[] = [];
  creatorLoginIds: string[] = [];
  createdDate: {
    start: Date,
    end: Date,
  };
  updaterLoginIds: string[] = [];
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

    this.creatorLoginIds = (params.get('creatorLoginIds')) ? Array.from(params.get('creatorLoginIds').split(',')) : [];
    this.createdDate = {
      start: (params.get('createdDateStart')) ? new Date(params.get('createdDateStart')) : null,
      end: (params.get('createdDateEnd')) ? new Date(params.get('createdDateEnd')) : null
    };

    this.updaterLoginIds = (params.get('updaterLoginIds')) ? Array.from(params.get('updaterLoginIds').split(',')) : [];
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
    if (this.creatorLoginIds && this.creatorLoginIds.length > 0) {
      params = params.set('creatorLoginIds', this.creatorLoginIds.join(','));
    }
    if (this.createdDate.start) {
      params = params.set('createdDateStart', '' + this.createdDate.start.toUTCString());
    }
    if (this.createdDate.end) {
      params = params.set('createdDateEnd', '' + this.createdDate.end.toUTCString());
    }
    if (this.updaterLoginIds && this.updaterLoginIds.length > 0) {
      params = params.set('updaterLoginIds', this.updaterLoginIds.join(','));
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
  release: ReleaseDetail;
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
  ownerUserId: string;
  state: string;
  timestamp: number[];
  revision: number;
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
