import {PageRequest, WhoAndWhen} from '../../basis/basis';
import {ParamMap} from '@angular/router';
import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../common/utility';
import {ScoreUser} from '../../authentication/domain/auth';
import {BusinessContextSummary} from '../../context-management/business-context/domain/business-context';
import {LibrarySummary} from '../../library-management/domain/library';
import {ReleaseSummary} from '../../release-management/domain/release';


export class AsbieBbieListEntry {

  type: string;

  library: LibrarySummary;
  release: ReleaseSummary;

  bieId: number;
  guid: string;
  topLevelAsbiepId: number;

  den: string;
  propertyTerm: string;
  version: string;
  status: string;
  bizTerm: string;
  remark: string;
  businessContextList: BusinessContextSummary[];
  state: string;
  access: string;

  owner: ScoreUser;
  ownedByDeveloper: boolean;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

}

export class BusinessTermListRequest {
  filters: {
    businessTerm: string;
    externalReferenceUri: string;
    externalReferenceId: string;
    definition: string;
    bieId: number;
    bieType: string;
    searchByCC: string;
    typeCode: string;
    primaryIndicator: boolean;
  };
  updaterUsernameList: string[] = [];
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

    this.updaterUsernameList = (params.get('updaterUsernameList')) ? Array.from(params.get('updaterUsernameList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      businessTerm: params.get('businessTerm') || '',
      externalReferenceUri: params.get('externalReferenceUri') || '',
      externalReferenceId: params.get('externalReferenceId') || '',
      definition: params.get('definition') || '',
      bieType: params.get('bieType') || '',
      bieId: Number(params.get('bieId')),
      searchByCC: params.get('searchByCC') || '',
      typeCode: params.get('typeCode') || '',
      primaryIndicator: Boolean(params.get('primaryIndicator')) || false
    };
  }

  toParams(): HttpParams {
    let params = new HttpParams()
        .set('sortActive', this.page.sortActive)
        .set('sortDirection', this.page.sortDirection)
        .set('pageIndex', '' + this.page.pageIndex)
        .set('pageSize', '' + this.page.pageSize);

    if (this.updaterUsernameList && this.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', this.updaterUsernameList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updateStart', '' + this.updatedDate.start.getTime());
    }
    if (this.updatedDate.end) {
      params = params.set('updateEnd', '' + this.updatedDate.end.getTime());
    }
    if (this.filters.businessTerm && this.filters.businessTerm.length > 0) {
      params = params.set('businessTerm', '' + this.filters.businessTerm);
    }
    if (this.filters.definition && this.filters.definition.length > 0) {
      params = params.set('definition', '' + this.filters.definition);
    }
    if (this.filters.bieId) {
      params = params.set('bieId', '' + this.filters.bieId);
    }
    if (this.filters.searchByCC) {
      params = params.set('searchByCC', '' + this.filters.searchByCC);
    }
    if (this.filters.primaryIndicator) {
      params = params.set('primaryIndicator', '' + this.filters.primaryIndicator);
    }
    if (this.filters.typeCode) {
      params = params.set('typeCode', '' + this.filters.typeCode);
    }
    return params;
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }
}

export class AssignedBtListRequest {
  filters: {
    businessTerm: string;
    externalReferenceUri: string;
    bieTypes: string[];
    bieId: number;
    bieDen: string;
    searchByCC: string;
    typeCode: string;
    primaryIndicator: boolean;
  };
  updaterUsernameList: string[] = [];
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

    this.updaterUsernameList = (params.get('updaterUsernameList')) ? Array.from(params.get('updaterUsernameList').split(',')) : [];
    this.updatedDate = {
      start: (params.get('updatedDateStart')) ? new Date(params.get('updatedDateStart')) : null,
      end: (params.get('updatedDateEnd')) ? new Date(params.get('updatedDateEnd')) : null
    };
    this.filters = {
      businessTerm: params.get('businessTerm') || '',
      externalReferenceUri: params.get('externalReferenceUri') || '',
      bieTypes: (params.get('bieTypes')) ? Array.from(params.get('bieTypes').split(',')) : [],
      bieDen: params.get('bieDen') || '',
      bieId: Number(params.get('bieId')),
      searchByCC: params.get('searchByCC') || '',
      typeCode: params.get('typeCode') || '',
      primaryIndicator: Boolean(params.get('primaryIndicator')) || false
    };
  }

  toQuery(): string {
    const params = this.toParams();
    const str = base64Encode(params.toString());
    return (str) ? 'q=' + str : undefined;
  }

  toParams() {
    let params = new HttpParams()
        .set('sortActive', this.page.sortActive)
        .set('sortDirection', this.page.sortDirection)
        .set('pageIndex', '' + this.page.pageIndex)
        .set('pageSize', '' + this.page.pageSize);

    if (this.updaterUsernameList && this.updaterUsernameList.length > 0) {
      params = params.set('updaterUsernameList', this.updaterUsernameList.join(','));
    }
    if (this.updatedDate.start) {
      params = params.set('updateStart', '' + this.updatedDate.start.getTime());
    }
    if (this.updatedDate.end) {
      params = params.set('updateEnd', '' + this.updatedDate.end.getTime());
    }
    if (this.filters.businessTerm && this.filters.businessTerm.length > 0) {
      params = params.set('businessTerm', '' + this.filters.businessTerm);
    }
    if (this.filters.externalReferenceUri && this.filters.externalReferenceUri.length > 0) {
      params = params.set('externalReferenceUri', '' + this.filters.externalReferenceUri);
    }
    if (this.filters.bieTypes && this.filters.bieTypes.length > 0) {
      params = params.set('bieTypes', this.filters.bieTypes.join(','));
    }
    if (this.filters.bieId) {
      params = params.set('bieId', '' + this.filters.bieId);
    }
    if (this.filters.bieDen) {
      params = params.set('bieDen', '' + this.filters.bieDen);
    }
    if (this.filters.searchByCC) {
      params = params.set('searchByCC', '' + this.filters.searchByCC);
    }
    if (this.filters.primaryIndicator) {
      params = params.set('primaryIndicator', '' + this.filters.primaryIndicator);
    }
    if (this.filters.typeCode) {
      params = params.set('typeCode', '' + this.filters.typeCode);
    }
    return params;
  }
}

export class BusinessTermListEntry {
  businessTermId: number;
  guid: string;
  businessTerm: string;
  comment: string;
  definition: string;
  externalReferenceUri: string;
  externalReferenceId: string;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  used: boolean;
}

export class BusinessTermDetails {
  businessTermId: number;
  guid: string;
  businessTerm: string;
  comment: string;
  definition: string;
  externalReferenceUri: string;
  externalReferenceId: string;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  used: boolean;

}

export class AssignedBusinessTermListEntry {
  assignedBizTermId: number;
  primaryIndicator: boolean;
  typeCode: string;
  bieId: number;
  den: string;
  bieType: string;
  businessTermId: number;
  guid: string;
  businessTerm: string;
  comment: string;
  externalReferenceUri: string;
  externalReferenceId: string;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class AssignedBusinessTermDetails {
  assignedBizTermId: number;
  primaryIndicator: boolean;
  typeCode: string;
  bieId: number;
  den: string;
  bieType: string;
  businessTermId: number;
  guid: string;
  businessTerm: string;
  comment: string;
  externalReferenceUri: string;
  externalReferenceId: string;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class PostAssignBusinessTerm {
  primaryIndicator: boolean;
  typeCode: string;
  biesToAssign: BieToAssign[];
  businessTermId: number;
}

export class BieToAssign {
  bieId: number;
  bieType: string;
}



