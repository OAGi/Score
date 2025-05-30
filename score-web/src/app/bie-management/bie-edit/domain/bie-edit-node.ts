import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {
  AbieDetail,
  AsbieDetail,
  AsbiepDetail,
  BbieDetail,
  BbiepDetail,
  BbieScDetail,
  BieEditNodeDetail,
  ChangeListener
} from '../../domain/bie-flat-tree';
import {hashCode4Array} from '../../../common/utility';

export class BieEditNode {
  ccManifestId: number;
  topLevelAsbiepId: number;
  libraryId: number;
  libraryName: string;
  releaseId: number;
  releaseNum: string;
  type: string;
  ccType: string;
  bieType: string;
  guid: string;
  name: string;
  displayName: string;
  required: boolean;
  locked: boolean;
  reused: boolean;
  topLevelAsbiepState: string;
  inverseMode: boolean;
  deprecated: boolean;
  basedTopLevelAsbiepId: number;
  private $hashCode: number;
  private _version: string;
  private _status: string;
  loginId: string;

  listeners: ChangeListener<BieEditNode>[] = [];

  constructor(obj?: BieEditNode) {
    this.topLevelAsbiepId = obj && obj.topLevelAsbiepId || 0;
    this.libraryId = obj && obj.libraryId || 0;
    this.releaseId = obj && obj.releaseId || 0;
    this.type = obj && obj.type || '';
    this.ccType = obj && obj.ccType || '';
    this.bieType = obj && obj.bieType || '';
    this.guid = obj && obj.guid || '';
    this.name = obj && obj.name || '';
    this.displayName = obj && obj.displayName || '';
    this.required = obj && obj.required || false;
    this.locked = obj && obj.locked || false;
    this.topLevelAsbiepState = obj && obj.topLevelAsbiepState || '';
    this.inverseMode = obj && obj.inverseMode || false;
    this.deprecated = obj && obj.deprecated || false;
    this.basedTopLevelAsbiepId = obj && obj.basedTopLevelAsbiepId || 0;
    this.libraryName = obj && obj.libraryName || '';
    this.releaseNum = obj && obj.releaseNum || '';
    this.loginId = obj && obj.loginId || '';
    this.version = obj && obj.version || '';
    this.status = obj && obj.status || '';
  }

  get version(): string {
    return this._version;
  }

  set version(value: string) {
    this._version = value;
    this.listeners.forEach(e => e.onChange(this, 'version', value));
  }

  get status(): string {
    return this._status;
  }

  set status(value: string) {
    this._status = value;
    this.listeners.forEach(e => e.onChange(this, 'status', value));
  }

  get hashCode(): number {
    return hashCode4Array(this.version, this.status);
  }

  reset(): void {
    this.$hashCode = this.hashCode;
  }

  get isChanged(): boolean {
    return this.$hashCode !== this.hashCode;
  }
}

export class BieEditAbieNode extends BieEditNode {
  asbiepId: number;
  abieId: number;
  asccpManifestId: number;
  accManifestId: number;
  businessContexts: BusinessContext[] = [];

  access: string;
  deprecatedReason: string;
  deprecatedRemark: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type.toUpperCase() === 'ABIE') {
      const abie = obj as BieEditAbieNode;

      this.asbiepId = abie.asbiepId;
      this.abieId = abie.abieId;
      this.asccpManifestId = abie.asccpManifestId;
      this.accManifestId = abie.accManifestId;
      this.businessContexts = abie.businessContexts;

      this.access = abie.access;
      this.topLevelAsbiepState = abie.topLevelAsbiepState;
      this.deprecated = abie.deprecated;
      this.deprecatedReason = abie.deprecatedReason;
      this.deprecatedRemark = abie.deprecatedRemark;
      this.inverseMode = abie.inverseMode;
    }
  }

  get hashCode(): number {
    return hashCode4Array(super.hashCode, this.inverseMode);
  }
}

export enum ValueDomainType {
  Primitive = 'Primitive',
  Code = 'Code',
  Agency = 'Agency',
}

export class ValueDomain {
  readonly id: number;
  readonly name: string;
  readonly state: string;
  readonly deprecated: boolean;
  readonly versionId: string;

  constructor(id: number, name: string, state?: string, versionId?: string, deprecated?: boolean) {
    this.id = id;
    this.name = name;
    this.state = state;
    this.versionId = versionId;
    this.deprecated = deprecated;
  }
}

export class DtAwdPri {
  dtAwdPriId: number;
  default: boolean;
  xbtManifestId: number;
  xbtId: number;
  xbtName: string;
  codeListManifestId: number;
  agencyIdListManifestId: number;
}

export class DtScAwdPri {
  dtScAwdPriId: number;
  default: boolean;
  xbtManifestId: number;
  xbtId: number;
  xbtName: string;
  codeListManifestId: number;
  agencyIdListManifestId: number;
}

export class CodeList {
  codeListId: number;
  codeListManifestId: number;
  basedCodeListManifestId: number;
  codeListName: string;
  state: string;
  deprecated: boolean;
  versionId: string;
}

export class AgencyIdList {
  agencyIdListManifestId: number;
  basedAgencyIdListManifestId: number;
  agencyIdListName: string;
  state: string;
  deprecated: boolean;
  versionId: string;
}

export class BieEditUpdateRequest {
  node: BieEditNodeDetail;
  children: BieEditUpdateRequest[];
}

export class BieEditUpdateResponse {
  abieNodeResult: boolean;
  asbiepNodeResults: Map<string, BieEditAsbiepUpdateResponse>;
  bbiepNodeResults: Map<string, BieEditBbiepUpdateResponse>;
  bbieScNodeResults: Map<string, BieEditBbieScUpdateResponse>;
}

export class BieEditAsbiepUpdateResponse {
  asbieId: number;
  asbiepId: number;
  abieId: number;
}

export class BieEditBbiepUpdateResponse {
  bbieId: number;
  bbiepId: number;
}

export class BieEditBbieScUpdateResponse {
  bbieScId: number;
}

export class BieEditCreateExtensionResponse {
  canEdit: boolean;
  canView: boolean;
  extensionId: number;
}

export class BieDetailRequest {
  topLevelAsbiepId: number;
  hashPath: string;
  ccType: string;
  bieType: string;
  ccManifestId: number;

  get id() {
    return this.ccType + '-' + this.ccManifestId;
  }
}

export class BieDetailUpdateRequest {
  topLevelAsbiepDetail: BieEditAbieNode;
  abieDetails: AbieDetail[];
  asbieDetails: AsbieDetail[];
  asbiepDetails: AsbiepDetail[];

  bbieDetails: BbieDetail[];
  bbiepDetails: BbiepDetail[];
  bbieScDetails: BbieScDetail[];

  constructor() {
    this.abieDetails = [];
    this.asbieDetails = [];
    this.asbiepDetails = [];
    this.bbieDetails = [];
    this.bbiepDetails = [];
    this.bbieScDetails = [];
  }

  get length(): number {
    return this.abieDetails.length +
      this.asbieDetails.length +
      this.asbiepDetails.length +
      this.bbieDetails.length +
      this.bbiepDetails.length +
      this.bbieScDetails.length;
  }

  get json(): any {
    return {
      topLevelAsbiepDetail: this.topLevelAsbiepDetail ? {
        version: this.topLevelAsbiepDetail.version,
        status: this.topLevelAsbiepDetail.status,
        inverseMode: this.topLevelAsbiepDetail.inverseMode
      } : {},
      abieDetails: this.abieDetails.map(e => e.json),
      asbieDetails: this.asbieDetails.map(e => e.json),
      asbiepDetails: this.asbiepDetails.map(e => e.json),
      bbieDetails: this.bbieDetails.map(e => e.json),
      bbiepDetails: this.bbiepDetails.map(e => e.json),
      bbieScDetails: this.bbieScDetails.map(e => e.json)
    };
  }
}


export class BieDetailUpdateResponse {
  abieDetailMap: Map<string, AbieDetail>;
  asbieDetailMap: Map<string, AsbieDetail>;
  asbiepDetailMap: Map<string, AsbiepDetail>;
  bbieDetailMap: Map<string, BbieDetail>;
  bbiepDetailMap: Map<string, BbiepDetail>;
  bbieScDetailMap: Map<string, BbieScDetail>;
}

export class UsedBie {
  used: boolean;
  bieId: number;
  hashPath: string;
  type: string;
  manifestId: number;
  ownerTopLevelAsbiepId: number;
  displayName: string;
  cardinalityMin: number;
  cardinalityMax: number;
  deprecated: boolean;

  constructor(obj) {
    this.used = obj && obj.used;
    this.bieId = obj && obj.bieId || 0;
    this.hashPath = obj && obj.hashPath || '';
    this.type = obj && obj.type || '';
    this.manifestId = obj && obj.manifestId || 0;
    this.ownerTopLevelAsbiepId = obj && obj.ownerTopLevelAsbiepId || 0;
    this.displayName = obj && obj.displayName || '';
    this.cardinalityMin = obj && obj.cardinalityMin || 0;
    this.cardinalityMax = obj && obj.cardinalityMax || 0;
    this.deprecated = obj && obj.deprecated || false;
  }
}

export class RefBie {
  asbieId: number;
  basedAsccManifestId: number;
  hashPath: string;
  topLevelAsbiepId: number;
  basedTopLevelAsbiepId: number;
  refTopLevelAsbiepId: number;
  refBasedTopLevelAsbiepId: number;
  refInverseMode: boolean;
}
