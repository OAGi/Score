import {Base64} from 'js-base64';
import {HttpParams} from '@angular/common/http';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {Md5} from 'ts-md5';


export class BieEditNode {
  topLevelAbieId: number;
  releaseId: number;
  type: string;
  guid: string;
  name: string;
  used: boolean;
  hasChild: boolean;

  constructor(obj?: BieEditNode) {
    this.topLevelAbieId = obj && obj.topLevelAbieId || 0;
    this.releaseId = obj && obj.releaseId || 0;
    this.type = obj && obj.type || '';
    this.guid = obj && obj.guid || '';
    this.name = obj && obj.name || '';
    this.used = obj && obj.used || false;
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
  topLevelAbieState: string;

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
      this.topLevelAbieState = abie.topLevelAbieState;
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
  topLevelAbieId: number;
  releaseId: number;
  type: string;
  guid: string;
  name: string;
  used: boolean;
  hasChild: boolean;
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
  definition: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'abie') {
      const abieDetail = obj as BieEditAbieNodeDetail;

      this.version = abieDetail.version;
      this.status = abieDetail.status;
      this.remark = abieDetail.remark;
      this.bizTerm = abieDetail.bizTerm;
      this.definition = abieDetail.definition;
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
  nillable: boolean;
  bizTerm: string;
  remark: string;

  contextDefinition: string;
  associationDefinition: string;
  componentDefinition: string;
  typeDefinition: string;

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'asbiep') {
      const asbiepDetail = obj as BieEditAsbiepNodeDetail;

      this.bieCardinalityMin = asbiepDetail.bieCardinalityMin;
      this.bieCardinalityMax = asbiepDetail.bieCardinalityMax;
      this.ccCardinalityMin = asbiepDetail.ccCardinalityMin;
      this.ccCardinalityMax = asbiepDetail.ccCardinalityMax;

      this.nillable = asbiepDetail.nillable;
      this.remark = asbiepDetail.remark;
      this.bizTerm = asbiepDetail.bizTerm;

      this.contextDefinition = asbiepDetail.contextDefinition;
      this.associationDefinition = asbiepDetail.associationDefinition;
      this.componentDefinition = asbiepDetail.componentDefinition;
      this.typeDefinition = asbiepDetail.typeDefinition;
    }
  }
}

export class BieEditBbiepNodeDetail
  extends BieEditBbiepNode
  implements BieEditNodeDetail, CardinalityAware {

  bieCardinalityMin: number;
  bieCardinalityMax: number;
  ccCardinalityMin: number;
  ccCardinalityMax: number;

  nillable: boolean;
  fixedValue: string;
  defaultValue: string;
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

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'bbiep') {
      const bbiepDetail = obj as BieEditBbiepNodeDetail;

      this.bieCardinalityMin = bbiepDetail.bieCardinalityMin;
      this.bieCardinalityMax = bbiepDetail.bieCardinalityMax;
      this.ccCardinalityMin = bbiepDetail.ccCardinalityMin;
      this.ccCardinalityMax = bbiepDetail.ccCardinalityMax;

      this.nillable = bbiepDetail.nillable;
      this.fixedValue = bbiepDetail.fixedValue;
      this.defaultValue = bbiepDetail.defaultValue;
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
    }
  }
}

export class BieEditBbieScNodeDetail
  extends BieEditBbieScNode
  implements BieEditNodeDetail, CardinalityAware {

  bieCardinalityMin: number;
  bieCardinalityMax: number;
  ccCardinalityMin: number;
  ccCardinalityMax: number;

  fixedValue: string;
  defaultValue: string;
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

  constructor(obj?: BieEditNode) {
    super(obj);

    if (obj && obj.type === 'bbie_sc') {
      const bbieScDetail = obj as BieEditBbieScNodeDetail;

      this.bieCardinalityMin = bbieScDetail.bieCardinalityMin;
      this.bieCardinalityMax = bbieScDetail.bieCardinalityMax;
      this.ccCardinalityMin = bbieScDetail.ccCardinalityMin;
      this.ccCardinalityMax = bbieScDetail.ccCardinalityMax;

      this.fixedValue = bbieScDetail.fixedValue;
      this.defaultValue = bbieScDetail.defaultValue;
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
    }
  }
}

export enum PrimitiveType {
  Primitive = 'Primitive',
  Code = 'Code',
  Agency = 'Agency',
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
  extensionId: number;
}

function hashCode(node: BieEditNode) {
  if (node.type === 'abie') {
    const str = JSON.stringify({
      topLevelAbieId: (node as BieEditAbieNodeDetail).topLevelAbieId,
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
      definition: (node as BieEditAbieNodeDetail).definition || '',
    });
    return <string>Md5.hashStr(str);
  }

  if (node.type === 'asbiep') {
    const str = JSON.stringify({
      topLevelAbieId: (node as BieEditAsbiepNodeDetail).topLevelAbieId,
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

      bieCardinalityMin: (node as BieEditAsbiepNodeDetail).bieCardinalityMin,
      bieCardinalityMax: (node as BieEditAsbiepNodeDetail).bieCardinalityMax,

      nillable: (node as BieEditAsbiepNodeDetail).nillable || false,
      bizTerm: (node as BieEditAsbiepNodeDetail).bizTerm || '',
      remark: (node as BieEditAsbiepNodeDetail).remark || '',

      contextDefinition: (node as BieEditAsbiepNodeDetail).contextDefinition || '',
    });
    return <string>Md5.hashStr(str);
  }

  if (node.type === 'bbiep') {
    const str = JSON.stringify({
      topLevelAbieId: (node as BieEditBbiepNodeDetail).topLevelAbieId,
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

      bieCardinalityMin: (node as BieEditBbiepNodeDetail).bieCardinalityMin,
      bieCardinalityMax: (node as BieEditBbiepNodeDetail).bieCardinalityMax,

      nillable: (node as BieEditBbiepNodeDetail).nillable || false,
      fixedValue: (node as BieEditBbiepNodeDetail).fixedValue || '',
      defaultValue: (node as BieEditBbiepNodeDetail).defaultValue || '',
      bizTerm: (node as BieEditBbiepNodeDetail).bizTerm || '',
      remark: (node as BieEditBbiepNodeDetail).remark || '',

      bdtPriRestriId: (node as BieEditBbiepNodeDetail).bdtPriRestriId || 0,
      codeListId: (node as BieEditBbiepNodeDetail).codeListId || 0,
      agencyIdListId: (node as BieEditBbiepNodeDetail).agencyIdListId || 0,

      contextDefinition: (node as BieEditBbiepNodeDetail).contextDefinition || '',
    });
    return <string>Md5.hashStr(str);
  }

  if (node.type === 'bbie_sc') {
    const str = JSON.stringify({
      topLevelAbieId: (node as BieEditBbieScNodeDetail).topLevelAbieId,
      releaseId: (node as BieEditBbieScNodeDetail).releaseId,
      type: (node as BieEditBbieScNodeDetail).type,
      guid: (node as BieEditBbieScNodeDetail).guid,
      name: (node as BieEditBbieScNodeDetail).name,

      bbieScId: (node as BieEditBbieScNodeDetail).bbieScId,
      dtScId: (node as BieEditBbieScNodeDetail).dtScId,

      used: (node as BieEditBbieScNodeDetail).used || false,

      bieCardinalityMin: (node as BieEditBbieScNodeDetail).bieCardinalityMin,
      bieCardinalityMax: (node as BieEditBbieScNodeDetail).bieCardinalityMax,

      fixedValue: (node as BieEditBbieScNodeDetail).fixedValue || '',
      defaultValue: (node as BieEditBbieScNodeDetail).defaultValue || '',
      bizTerm: (node as BieEditBbieScNodeDetail).bizTerm || '',
      remark: (node as BieEditBbieScNodeDetail).remark || '',

      dtScPriRestriId: (node as BieEditBbieScNodeDetail).dtScPriRestriId || 0,
      codeListId: (node as BieEditBbieScNodeDetail).codeListId || 0,
      agencyIdListId: (node as BieEditBbieScNodeDetail).agencyIdListId || 0,

      contextDefinition: (node as BieEditBbieScNodeDetail).contextDefinition || '',
    });
    return <string>Md5.hashStr(str);
  }

  return <string>Md5.hashStr(JSON.stringify(node));
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
    return (this.isDetail) ? hashCode(this.item) : <string>Md5.hashStr(JSON.stringify(this.item));
  }

  toHttpParams(): HttpParams {
    const params: HttpParams = Object.getOwnPropertyNames(this.item)
      .reduce((p, key) => {
        if (key !== 'name' && key !== 'hasChild') {
          return p.set(key, this.item[key]);
        }
        return p;
      }, new HttpParams());
    const data = Base64.encode(params.toString());
    return new HttpParams().set('data', data);
  }

  isChanged() {
    return this.$hashCode !== this.hashCode;
  }

  reset() {
    this.$hashCode = this.hashCode;
  }
}
