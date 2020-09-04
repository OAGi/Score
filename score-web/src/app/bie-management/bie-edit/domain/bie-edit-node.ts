import {HttpParams} from '@angular/common/http';
import {base64Encode, md5, sha256} from '../../../common/utility';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';


export class BieEditNode {
  topLevelAsbiepId: number;
  releaseId: number;
  type: string;
  guid: string;
  name: string;
  used: boolean;
  required: boolean;
  locked: boolean;
  derived: boolean;
  hasChild: boolean;

  releaseNum: string;
  topLevelAsbiepState: string;
  ownerLoginId: string;

  constructor(obj?: BieEditNode) {
    this.topLevelAsbiepId = obj && obj.topLevelAsbiepId || 0;
    this.releaseId = obj && obj.releaseId || 0;
    this.type = obj && obj.type || '';
    this.guid = obj && obj.guid || '';
    this.name = obj && obj.name || '';
    this.used = obj && obj.used || false;
    this.required = obj && obj.required || false;
    this.locked = obj && obj.locked || false;
    this.derived = obj && obj.derived || false;
    this.hasChild = obj && obj.hasChild || false;
  }
}


export class BieEditAbieNode extends BieEditNode {
  asbiepId: number;
  abieId: number;
  asccpId: number;
  accId: number;
  businessContexts: BusinessContext[] = [];

  access: string;
  topLevelAsbiepState: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'abie') {
      const abie = obj as BieEditAbieNode;

      this.asbiepId = abie.asbiepId;
      this.abieId = abie.abieId;
      this.asccpId = abie.asccpId;
      this.accId = abie.accId;
      this.businessContexts = abie.businessContexts;

      this.access = abie.access;
      this.topLevelAsbiepState = abie.topLevelAsbiepState;
    }
  }
}

export class BieEditAsbiepNode extends BieEditNode {
  asbieId: number;
  asccId: number;
  asbiepId: number;
  asccpId: number;
  abieId: number;
  accId: number;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'asbiep') {
      const asbiep = obj as BieEditAsbiepNode;

      this.asbieId = asbiep.asbieId;
      this.asccId = asbiep.asccId;
      this.asbiepId = asbiep.asbiepId;
      this.asccpId = asbiep.asccpId;
      this.abieId = asbiep.abieId;
      this.accId = asbiep.accId;
    }
  }
}

export class BieEditBbiepNode extends BieEditNode {
  bbieId: number;
  bccId: number;
  bbiepId: number;
  bccpId: number;
  bdtId: number;
  attribute: boolean;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'bbiep') {
      const bbiep = obj as BieEditBbiepNode;

      this.bbieId = bbiep.bbieId;
      this.bccId = bbiep.bccId;
      this.bbiepId = bbiep.bbiepId;
      this.bccpId = bbiep.bccpId;
      this.bdtId = bbiep.bdtId;
      this.attribute = bbiep.attribute;
    }
  }
}

export class BieEditBbieScNode extends BieEditNode {
  bbieScId: number;
  dtScId: number;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'bbie_sc') {
      const bbieSc = obj as BieEditBbieScNode;

      this.bbieScId = bbieSc.bbieScId;
      this.dtScId = bbieSc.dtScId;
    }
  }
}

export interface BieEditNodeDetail {
  topLevelAsbiepId: number;
  releaseId: number;
  type: string;
  guid: string;
  name: string;
  used: boolean;
  required: boolean;
  locked: boolean;
  derived: boolean;
  hasChild: boolean;
  releaseNum: string;
  topLevelAsbiepState: string;
  ownerLoginId: string;
}

export interface CardinalityAware {
  bieCardinalityMin: number;
  bieCardinalityMax: number;
  ccCardinalityMin: number;
  ccCardinalityMax: number;
}

export class BieEditAbieNodeDetail
  extends BieEditAbieNode
  implements BieEditNodeDetail {

  version: string;
  status: string;
  remark: string;
  bizTerm: string;
  contextDefinition: string;

  accDen: string;
  typeDefinition: string;
  asccpDen: string;
  componentDefinition: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'abie') {
      const abieDetail = obj as BieEditAbieNodeDetail;

      this.version = abieDetail.version;
      this.status = abieDetail.status;
      this.remark = abieDetail.remark;
      this.bizTerm = abieDetail.bizTerm;
      this.contextDefinition = abieDetail.contextDefinition;
    }
  }
}

export class BieEditAsbiepNodeDetail
  extends BieEditAsbiepNode
  implements BieEditNodeDetail, CardinalityAware {

  bieCardinalityMin: number;
  bieCardinalityMax: number;
  ccCardinalityMin: number;
  ccCardinalityMax: number;
  ccNillable: boolean;
  bieNillable: boolean;

  contextDefinition: string;
  associationDefinition: string;
  componentDefinition: string;
  typeDefinition: string;

  accDen: string;
  asccDen: string;
  asccpDen: string;

  bizTerm: string;
  remark: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'asbiep') {
      const asbiepDetail = obj as BieEditAsbiepNodeDetail;

      this.bieCardinalityMin = asbiepDetail.bieCardinalityMin;
      this.bieCardinalityMax = asbiepDetail.bieCardinalityMax;
      this.ccCardinalityMin = asbiepDetail.ccCardinalityMin;
      this.ccCardinalityMax = asbiepDetail.ccCardinalityMax;

      this.ccNillable = asbiepDetail.ccNillable;
      this.bieNillable = asbiepDetail.bieNillable;

      this.contextDefinition = asbiepDetail.contextDefinition;
      this.associationDefinition = asbiepDetail.associationDefinition;
      this.componentDefinition = asbiepDetail.componentDefinition;
      this.typeDefinition = asbiepDetail.typeDefinition;

      this.accDen = asbiepDetail.accDen;
      this.asccDen = asbiepDetail.asccDen;
      this.asccpDen = asbiepDetail.asccpDen;

      this.remark = asbiepDetail.remark;
      this.bizTerm = asbiepDetail.bizTerm;
    }
  }
}

export class BieEditBbiepNodeDetail
  extends BieEditBbiepNode
  implements BieEditNodeDetail, CardinalityAware {

  ccCardinalityMin: number;
  bieCardinalityMin: number;
  ccCardinalityMax: number;
  bieCardinalityMax: number;

  ccNillable: boolean;
  bieNillable: boolean;
  ccFixedValue: string;
  bieFixedValue: string;
  ccDefaultValue: string;
  bieDefaultValue: string;

  fixedOrDefault: string;
  bizTerm: string;
  remark: string;
  bdtDen: string;

  bdtPriRestriId: number;
  codeListId: number;
  agencyIdListId: number;

  primitiveType: PrimitiveType;
  primitiveTypes: PrimitiveType[];

  xbtList: Xbt[];
  codeLists: CodeList[];
  agencyIdLists: AgencyIdList[];

  contextDefinition: string;
  associationDefinition: string;
  componentDefinition: string;

  example: string;

  bccDen: string;
  bccpDen: string;

  bbiepRemark: string;
  bbiepBizTerm: string;
  bbiepDefinition: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'bbiep') {
      const bbiepDetail = obj as BieEditBbiepNodeDetail;

      this.bieCardinalityMin = bbiepDetail.bieCardinalityMin;
      this.bieCardinalityMax = bbiepDetail.bieCardinalityMax;
      this.ccCardinalityMin = bbiepDetail.ccCardinalityMin;
      this.ccCardinalityMax = bbiepDetail.ccCardinalityMax;

      this.ccNillable = bbiepDetail.ccNillable;
      this.bieNillable = bbiepDetail.bieNillable;
      this.ccFixedValue = bbiepDetail.ccFixedValue;
      this.bieFixedValue = bbiepDetail.bieFixedValue;
      this.ccDefaultValue = bbiepDetail.ccDefaultValue;
      this.bieDefaultValue = bbiepDetail.bieDefaultValue;
      this.bizTerm = bbiepDetail.bizTerm;
      this.remark = bbiepDetail.remark;
      this.bdtDen = bbiepDetail.bdtDen;

      this.bdtPriRestriId = bbiepDetail.bdtPriRestriId;
      this.codeListId = bbiepDetail.codeListId;
      this.agencyIdListId = bbiepDetail.agencyIdListId;

      this.primitiveType = bbiepDetail.primitiveType;
      this.primitiveTypes = bbiepDetail.primitiveTypes;

      this.xbtList = bbiepDetail.xbtList;
      this.codeLists = bbiepDetail.codeLists;
      this.agencyIdLists = bbiepDetail.agencyIdLists;

      this.contextDefinition = bbiepDetail.contextDefinition;
      this.associationDefinition = bbiepDetail.associationDefinition;
      this.componentDefinition = bbiepDetail.componentDefinition;
      this.example = bbiepDetail.example;
    }
  }
}

export class BieEditBbieScNodeDetail
  extends BieEditBbieScNode
  implements BieEditNodeDetail, CardinalityAware {

  ccCardinalityMin: number;
  bieCardinalityMin: number;
  ccCardinalityMax: number;
  bieCardinalityMax: number;

  ccFixedValue: string;
  bieFixedValue: string;
  ccDefaultValue: string;
  bieDefaultValue: string;
  fixedOrDefault: string;
  bizTerm: string;
  remark: string;

  dtScPriRestriId: number;
  codeListId: number;
  agencyIdListId: number;

  primitiveType: PrimitiveType;
  primitiveTypes: PrimitiveType[];

  xbtList: Xbt[];
  codeLists: CodeList[];
  agencyIdLists: AgencyIdList[];

  contextDefinition: string;
  componentDefinition: string;

  example: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'bbie_sc') {
      const bbieScDetail = obj as BieEditBbieScNodeDetail;

      this.bieCardinalityMin = bbieScDetail.bieCardinalityMin;
      this.bieCardinalityMax = bbieScDetail.bieCardinalityMax;
      this.ccCardinalityMin = bbieScDetail.ccCardinalityMin;
      this.ccCardinalityMax = bbieScDetail.ccCardinalityMax;

      this.ccFixedValue = bbieScDetail.ccFixedValue;
      this.bieFixedValue = bbieScDetail.bieFixedValue;
      this.ccDefaultValue = bbieScDetail.ccDefaultValue;
      this.bieDefaultValue = bbieScDetail.bieDefaultValue;
      this.bizTerm = bbieScDetail.bizTerm;
      this.remark = bbieScDetail.remark;

      this.dtScPriRestriId = bbieScDetail.dtScPriRestriId;
      this.codeListId = bbieScDetail.codeListId;
      this.agencyIdListId = bbieScDetail.agencyIdListId;

      this.primitiveType = bbieScDetail.primitiveType;
      this.primitiveTypes = bbieScDetail.primitiveTypes;

      this.xbtList = bbieScDetail.xbtList;
      this.codeLists = bbieScDetail.codeLists;
      this.agencyIdLists = bbieScDetail.agencyIdLists;

      this.contextDefinition = bbieScDetail.contextDefinition;
      this.componentDefinition = bbieScDetail.componentDefinition;
      this.example = bbieScDetail.example;
    }
  }
}

export enum PrimitiveType {
  Primitive = 'Primitive',
  Code = 'Code',
  Agency = 'Agency',
}

export class Primitive {
  readonly id: number;
  readonly name: string;

  constructor(id: number, name: string) {
    this.id = id;
    this.name = name;
  }
}

export class Xbt {
  priRestriId: number;
  xbtId: number;
  xbtName: string;
}

export class CodeList {
  codeListId: number;
  codeListName: string;
}

export class AgencyIdList {
  agencyIdListId: number;
  agencyIdListName: string;
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

function hashCode(node: BieEditNode) {
  if (node.type === 'abie') {
    const str = JSON.stringify({
      topLevelAsbiepId: (node as BieEditAbieNodeDetail).topLevelAsbiepId,
      releaseId: (node as BieEditAbieNodeDetail).releaseId,
      type: (node as BieEditAbieNodeDetail).type,
      guid: (node as BieEditAbieNodeDetail).guid,
      name: (node as BieEditAbieNodeDetail).name,

      asbiepId: (node as BieEditAbieNodeDetail).asbiepId,
      abieId: (node as BieEditAbieNodeDetail).abieId,
      asccpId: (node as BieEditAbieNodeDetail).asccpId,
      accId: (node as BieEditAbieNodeDetail).accId,

      version: (node as BieEditAbieNodeDetail).version || '',
      status: (node as BieEditAbieNodeDetail).status || '',
      remark: (node as BieEditAbieNodeDetail).remark || '',
      bizTerm: (node as BieEditAbieNodeDetail).bizTerm || '',
      asbiepDefinition: (node as BieEditAbieNodeDetail).contextDefinition || '',
    });
    return md5(str);
  }

  if (node.type === 'asbiep') {
    const str = JSON.stringify({
      topLevelAsbiepId: (node as BieEditAsbiepNodeDetail).topLevelAsbiepId,
      releaseId: (node as BieEditAsbiepNodeDetail).releaseId,
      type: (node as BieEditAsbiepNodeDetail).type,
      guid: (node as BieEditAsbiepNodeDetail).guid,
      name: (node as BieEditAsbiepNodeDetail).name,

      asbieId: (node as BieEditAsbiepNodeDetail).asbieId,
      asccId: (node as BieEditAsbiepNodeDetail).asccId,
      asbiepId: (node as BieEditAsbiepNodeDetail).asbiepId,
      asccpId: (node as BieEditAsbiepNodeDetail).asccpId,
      abieId: (node as BieEditAsbiepNodeDetail).abieId,
      accId: (node as BieEditAsbiepNodeDetail).accId,

      used: (node as BieEditAsbiepNodeDetail).used || false,
      required: (node as BieEditAsbiepNodeDetail).required || false,

      bieCardinalityMin: (node as BieEditAsbiepNodeDetail).bieCardinalityMin,
      bieCardinalityMax: (node as BieEditAsbiepNodeDetail).bieCardinalityMax,

      ccNillable: (node as BieEditAsbiepNodeDetail).ccNillable || false,
      bieNillable: (node as BieEditAsbiepNodeDetail).bieNillable || false,

      contextDefinition: (node as BieEditAsbiepNodeDetail).contextDefinition || '',

      remark: (node as BieEditAsbiepNodeDetail).remark || '',
      bizTerm: (node as BieEditAsbiepNodeDetail).bizTerm || '',
    });
    return md5(str);
  }

  if (node.type === 'bbiep') {
    const str = JSON.stringify({
      topLevelAsbiepId: (node as BieEditBbiepNodeDetail).topLevelAsbiepId,
      releaseId: (node as BieEditBbiepNodeDetail).releaseId,
      type: (node as BieEditBbiepNodeDetail).type,
      guid: (node as BieEditBbiepNodeDetail).guid,
      name: (node as BieEditBbiepNodeDetail).name,

      bbieId: (node as BieEditBbiepNodeDetail).bbieId,
      bccId: (node as BieEditBbiepNodeDetail).bccId,
      bbiepId: (node as BieEditBbiepNodeDetail).bbiepId,
      bccpId: (node as BieEditBbiepNodeDetail).bccpId,
      bdtId: (node as BieEditBbiepNodeDetail).bdtId,
      attribute: (node as BieEditBbiepNodeDetail).attribute,

      used: (node as BieEditBbiepNodeDetail).used || false,
      required: (node as BieEditBbiepNodeDetail).required || false,

      bieCardinalityMin: (node as BieEditBbiepNodeDetail).bieCardinalityMin,
      bieCardinalityMax: (node as BieEditBbiepNodeDetail).bieCardinalityMax,

      ccNillable: (node as BieEditBbiepNodeDetail).ccNillable || false,
      bieNillable: (node as BieEditBbiepNodeDetail).bieNillable || false,
      ccFixedValue: (node as BieEditBbiepNodeDetail).ccFixedValue || '',
      bieFixedValue: (node as BieEditBbiepNodeDetail).bieFixedValue || '',
      ccDefaultValue: (node as BieEditBbiepNodeDetail).ccDefaultValue || '',
      bieDefaultValue: (node as BieEditBbiepNodeDetail).bieDefaultValue || '',
      bizTerm: (node as BieEditBbiepNodeDetail).bizTerm || '',
      remark: (node as BieEditBbiepNodeDetail).remark || '',

      bdtPriRestriId: (node as BieEditBbiepNodeDetail).bdtPriRestriId || 0,
      codeListId: (node as BieEditBbiepNodeDetail).codeListId || 0,
      agencyIdListId: (node as BieEditBbiepNodeDetail).agencyIdListId || 0,

      contextDefinition: (node as BieEditBbiepNodeDetail).contextDefinition || '',
      example: (node as BieEditBbiepNodeDetail).example || '',

      bbiepBizTerm: (node as BieEditBbiepNodeDetail).bbiepBizTerm || '',
      bbiepRemark: (node as BieEditBbiepNodeDetail).bbiepRemark || '',
      bbiepDefinition: (node as BieEditBbiepNodeDetail).bbiepDefinition || '',
    });
    return md5(str);
  }

  if (node.type === 'bbie_sc') {
    const str = JSON.stringify({
      topLevelAsbiepId: (node as BieEditBbieScNodeDetail).topLevelAsbiepId,
      releaseId: (node as BieEditBbieScNodeDetail).releaseId,
      type: (node as BieEditBbieScNodeDetail).type,
      guid: (node as BieEditBbieScNodeDetail).guid,
      name: (node as BieEditBbieScNodeDetail).name,

      bbieScId: (node as BieEditBbieScNodeDetail).bbieScId,
      dtScId: (node as BieEditBbieScNodeDetail).dtScId,

      used: (node as BieEditBbieScNodeDetail).used || false,
      required: (node as BieEditBbieScNodeDetail).required || false,

      bieCardinalityMin: (node as BieEditBbieScNodeDetail).bieCardinalityMin,
      bieCardinalityMax: (node as BieEditBbieScNodeDetail).bieCardinalityMax,

      ccFixedValue: (node as BieEditBbieScNodeDetail).ccFixedValue || '',
      bieFixedValue: (node as BieEditBbieScNodeDetail).bieFixedValue || '',
      ccDefaultValue: (node as BieEditBbieScNodeDetail).ccDefaultValue || '',
      bieDefaultValue: (node as BieEditBbieScNodeDetail).bieDefaultValue || '',
      bizTerm: (node as BieEditBbieScNodeDetail).bizTerm || '',
      remark: (node as BieEditBbieScNodeDetail).remark || '',

      dtScPriRestriId: (node as BieEditBbieScNodeDetail).dtScPriRestriId || 0,
      codeListId: (node as BieEditBbieScNodeDetail).codeListId || 0,
      agencyIdListId: (node as BieEditBbieScNodeDetail).agencyIdListId || 0,

      contextDefinition: (node as BieEditBbieScNodeDetail).contextDefinition || '',
      example: (node as BieEditBbieScNodeDetail).example || '',
    });
    return md5(str);
  }

  return md5(JSON.stringify(node));
}

/** Flat node with expandable and level information */
export class DynamicBieFlatNode {
  private $hashCode;

  constructor(public item: BieEditNode,
              public level = 0,
              public isLoading = false,
              public isNullObject = false,
              public isDetail = false) {
    this.reset();
  }

  get key() {
    return this.$hashCode;
  }

  get expandable() {
    return this.item.hasChild;
  }

  get hashCode() {
    return (this.isDetail) ? hashCode(this.item) : md5(JSON.stringify(this.item));
  }

  toHttpParams(): HttpParams {
    const params: HttpParams = Object.getOwnPropertyNames(this.item)
      .reduce((p, key) => {
        if (key !== 'name' && key !== 'hasChild') {
          return p.set(key, this.item[key]);
        }
        return p;
      }, new HttpParams());
    const data = base64Encode(params.toString());
    return new HttpParams().set('data', data);
  }

  isChanged() {
    return this.$hashCode !== this.hashCode;
  }

  reset() {
    this.$hashCode = this.hashCode;
  }

  get isAsbiep(): boolean {
    return this.item.type === 'asbiep';
  }

  get isEditable(): boolean {
    return !this.item.locked;
  }
}
