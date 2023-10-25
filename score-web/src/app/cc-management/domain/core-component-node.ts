import {AgencyIdList} from '../../agency-id-list-management/domain/agency-id-list';
import {ChangeListener} from '../../bie-management/domain/bie-flat-tree';
import {CodeListForList} from '../../code-list-management/domain/code-list';
import {compare, emptyToUndefined, hashCode, hashCode4Array, hashCode4String, toCamelCase, } from '../../common/utility';
import {AccFlatNode, AsccpFlatNode, BccpFlatNode, CcFlatNode, DtFlatNode, DtScFlatNode} from './cc-flat-tree';
import {ShortTag} from '../../tag-management/domain/tag';

export class CcNode {
  type: string;
  releaseId: number;
  guid: string;
  name: string;
  hasChild: boolean;
  state: string;
  access: string;
  manifestId: number;
  revisionNum: number;
  $hashCode: string;

  listeners: ChangeListener<CcNode>[] = [];

  constructor(obj?: CcNode) {
    this.releaseId = obj && obj.releaseId || 0;
    this.type = obj && obj.type || '';
    this.guid = obj && obj.guid || '';
    this.name = obj && obj.name || '';
    this.hasChild = obj && obj.hasChild || false;
    this.revisionNum = obj && obj.revisionNum || 0;
    this.manifestId = obj && obj.manifestId || 0;
    this.access = obj && obj.access || '';
    this.state = obj && obj.state || '';
  }


  get hashCode(): string {
    return hashCode(this);
  }

  reset(): void {
    this.$hashCode = this.hashCode;
  }

  get isChanged(): boolean {
    return this.$hashCode !== this.hashCode;
  }
}

export class CcAccNode extends CcNode {
  accId: number;
  guid: string;
  objectClassTerm: string;
  group: boolean;
  den: string;
  state: string;
  definition: string;
  oagisComponentType: number;
  abstracted: boolean;
  deprecated: boolean;
  componentType: number;
  basedAccManifestId: number;
  hasExtension: boolean;
  accType: string;
}

export class CcAsccpNode extends CcNode {
  asccpId: number;
  asccId: number;
  asccManifestId: number;
  roleOfAccId: number;
  seqKey: number;
  asccpType: string;
}

export class CcBccpNode extends CcNode {
  bccpId: number;
  bccId: number;
  btdId: number;
  bccManifestId: number;
  seqKey: number;
  attribute: boolean;
}

export class CcBdtNode extends CcNode {
  btdId: number;
}

export class CcAsccNode extends CcNode {
  asccId: number;
  cardinalityMin: number;
  cardinalityMax: number;
  deprecated: boolean;
}

export class CcBccNode extends CcNode {
  bccId: number;
  cardinalityMin: number;
  cardinalityMax: number;
  deprecated: boolean;
  defaultValue: string;
  fixedValue: string;
  entityType: string;
}


export class CcBdtScNode extends CcNode {
  bdtScId: number;
}

export abstract class CcNodeDetail {
  _node: CcFlatNode;
  type: string;

  $hashCode: number;

  constructor(node: CcFlatNode) {
    this._node = node;
  }

  get isChanged(): boolean {
    return this.$hashCode !== this.hashCode;
  }

  reset() {
    this.$hashCode = this.hashCode;
  }

  abstract get hashCode(): number;
}

export class CcAccNodeDetail extends CcNodeDetail {
  accId: number;
  manifestId: number;
  guid: string;
  private _objectClassTerm: string;
  private _group: boolean;
  private _definition: string;
  private _definitionSource: string;
  private _oagisComponentType: number;
  private _abstracted: boolean;
  private _deprecated: boolean;
  private _namespaceId: number;

  private _state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;

  replacementAccManifestId: number;
  replacement: CcAccNodeDetail;

  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  constructor(node: AccFlatNode, obj: any) {
    super(node);

    this.accId = obj.accId;
    this.manifestId = obj.manifestId;
    this.guid = obj.guid;
    this.objectClassTerm = obj.objectClassTerm;
    this.group = obj.group;
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;
    this.oagisComponentType = obj.oagisComponentType;
    this.abstracted = obj.abstracted;
    this.deprecated = obj.deprecated;
    this.replacementAccManifestId = obj.replacementAccManifestId;
    this.replacement = obj.replacement;
    this.namespaceId = obj.namespaceId;
    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;

    this.reset();
  }

  get json(): any {
    return {
      accId: this.accId,
      manifestId: this.manifestId,
      guid: this.guid,
      type: this.type,
      objectClassTerm: this.objectClassTerm,
      oagisComponentType: this.oagisComponentType,
      abstracted: this.abstracted,
      deprecated: this.deprecated,
      replacementAccManifestId: this.replacementAccManifestId,
      replacement: (this.replacement) ? this.replacement.json : undefined,
      definition: this.definition,
      definitionSource: this.definitionSource,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      sinceManifestId: this.sinceManifestId,
      sinceReleaseId: this.sinceReleaseId,
      sinceReleaseNum: this.sinceReleaseNum,
      lastChangedManifestId: this.lastChangedManifestId,
      lastChangedReleaseId: this.lastChangedReleaseId,
      lastChangedReleaseNum: this.lastChangedReleaseNum,
    };
  }

  get hashCode(): number {
    return ((this.accId) ? this.accId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.type) ? hashCode4String(this.type) : 0) +
      ((this.objectClassTerm) ? hashCode4String(this.objectClassTerm) : 0) +
      ((this.oagisComponentType) ? this.oagisComponentType : 0) +
      ((this.abstracted) ? 1231 : 1237) +
      ((this.deprecated) ? 1231 : 1237) +
      ((this.replacementAccManifestId) ? this.replacementAccManifestId : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((this.namespaceId) ? this.namespaceId : 0) +
      ((!!this.owner) ? hashCode4String(this.owner) : 0) +
      ((this.releaseId) ? this.releaseId : 0) +
      ((!!this.releaseNum) ? hashCode4String(this.releaseNum) : 0) +
      ((this.revisionNum) ? this.revisionNum : 0) +
      ((this.revisionTrackingNum) ? this.revisionTrackingNum : 0);
  }

  get objectClassTerm(): string {
    return this._objectClassTerm;
  }

  set objectClassTerm(value: string) {
    value = toCamelCase(emptyToUndefined(value));
    this._objectClassTerm = value;
    this._node.fireChangeEvent('objectClassTerm', value);
  }

  get den(): string {
    return this.objectClassTerm + '. Details';
  }

  get group(): boolean {
    return this._group;
  }

  set group(value: boolean) {
    this._group = value;
    this._node.fireChangeEvent('group', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }

  get oagisComponentType(): number {
    return this._oagisComponentType;
  }

  set oagisComponentType(value: number) {
    this._oagisComponentType = value;
    if (this._oagisComponentType === Base.value) {
      this.abstracted = true;
    } else if (this._oagisComponentType === Extension.value || this._oagisComponentType === SemanticGroup.value) {
      this.abstracted = false;
    }
    this._node.fireChangeEvent('oagisComponentType', value);
  }

  get abstracted(): boolean {
    return this._abstracted;
  }

  set abstracted(value: boolean) {
    this._abstracted = value;
    this._node.fireChangeEvent('abstracted', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get namespaceId(): number {
    return this._namespaceId;
  }

  set namespaceId(value: number) {
    this._namespaceId = value;
    this._node.fireChangeEvent('namespaceId', value);
  }

  get state(): string {
    return this._state;
  }

  set state(value: string) {
    this._state = value;
    this._node.fireChangeEvent('state', value);
  }
}

export class CcState {
  constructor(public value: number, public name: string) {
  }
}

export class OagisComponentType {
  constructor(public value: number, public name: string) {
  }
}

export const Base: OagisComponentType = new OagisComponentType(0, 'Base (Abstract)');
export const Semantics: OagisComponentType = new OagisComponentType(1, 'Semantics');
export const Extension: OagisComponentType = new OagisComponentType(2, 'Extension');
export const SemanticGroup: OagisComponentType = new OagisComponentType(3, 'Semantic Group');
export const UserExtensionGroup: OagisComponentType = new OagisComponentType(4, 'User Extension Group');
export const Embedded: OagisComponentType = new OagisComponentType(5, 'Embedded');
export const OAGIS10Nouns: OagisComponentType = new OagisComponentType(6, 'OAGIS10 Nouns');
export const OAGIS10BODs: OagisComponentType = new OagisComponentType(7, 'OAGIS10 BODs');
export const BOD: OagisComponentType = new OagisComponentType(8, 'BOD');
export const Verb: OagisComponentType = new OagisComponentType(9, 'Verb');
export const Noun: OagisComponentType = new OagisComponentType(10, 'Noun');
export const Choice: OagisComponentType = new OagisComponentType(11, 'Choice');
export const AttributeGroup: OagisComponentType = new OagisComponentType(12, 'Attribute Group');

export const OagisComponentTypes: OagisComponentType[] = [
  Base, Semantics, Extension, SemanticGroup, UserExtensionGroup,
  Embedded, OAGIS10Nouns, OAGIS10BODs, Choice, AttributeGroup
];

export const OagisEntities: OagisComponentType[] = [
  BOD, Noun, Verb
];

export const OagisComponentTypeMap = {
  0: Base,
  1: Semantics,
  2: Extension,
  3: SemanticGroup,
  4: UserExtensionGroup,
  5: Embedded,
  6: OAGIS10Nouns,
  7: OAGIS10BODs,
  8: BOD,
  9: Verb,
  10: Noun,
  11: Choice,
  12: AttributeGroup
};

class AsccDetail {
  private _node: CcFlatNode;
  manifestId: number;
  asccId: number;
  guid: string;
  den: string;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _deprecated: boolean;
  private _definition: string;
  private _definitionSource: string;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;

  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  constructor(node: CcFlatNode, obj: any) {
    this._node = node;

    this.manifestId = obj.manifestId;
    this.asccId = obj.asccId;
    this.guid = obj.guid;
    this.den = obj.den;
    this.cardinalityMin = obj.cardinalityMin;
    this.cardinalityMax = obj.cardinalityMax;
    this.deprecated = obj.deprecated;
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;

    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;
  }

  get json(): any {
    return {
      asccId: this.asccId,
      guid: this.guid,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      deprecated: this.deprecated,
      definition: this.definition,
      definitionSource: this.definitionSource,
      manifestId: this.manifestId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      sinceManifestId: this.sinceManifestId,
      sinceReleaseId: this.sinceReleaseId,
      sinceReleaseNum: this.sinceReleaseNum,
      lastChangedManifestId: this.lastChangedManifestId,
      lastChangedReleaseId: this.lastChangedReleaseId,
      lastChangedReleaseNum: this.lastChangedReleaseNum,
    };
  }

  get hashCode(): number {
    return ((this.asccId) ? this.asccId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.deprecated) ? 1231 : 1237) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((!!this.owner) ? hashCode4String(this.owner) : 0) +
      ((this.releaseId) ? this.releaseId : 0) +
      ((!!this.releaseNum) ? hashCode4String(this.releaseNum) : 0) +
      ((this.revisionNum) ? this.revisionNum : 0) +
      ((this.revisionTrackingNum) ? this.revisionTrackingNum : 0);
  }

  get cardinalityMin(): number {
    return this._cardinalityMin;
  }

  set cardinalityMin(value: number) {
    this._cardinalityMin = value;
    this._node.cardinalityMin = value;
    this._node.fireChangeEvent('cardinalityMin', value);
  }

  get cardinalityMax(): number {
    return this._cardinalityMax;
  }

  set cardinalityMax(value: number) {
    this._cardinalityMax = value;
    this._node.cardinalityMax = value;
    this._node.fireChangeEvent('cardinalityMax', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }
}

class AsccpDetail {
  private _node: CcFlatNode;
  manifestId: number;
  asccpId: number;
  guid: string;
  private _propertyTerm: string;
  private _namespaceId: number;
  private _reusable: boolean;
  private _nillable: boolean;
  private _deprecated: boolean;
  private _definition: string;
  private _definitionSource: string;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;
  _den: string;

  replacementAsccpManifestId: number;
  replacement: AsccpDetail;

  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  constructor(node: CcFlatNode, obj: any) {
    this._node = node;
    this.manifestId = obj.manifestId;
    this.asccpId = obj.asccpId;
    this.guid = obj.guid;
    this.propertyTerm = obj.propertyTerm;
    this.reusable = obj.reusable;
    this.nillable = obj.nillable;
    this.deprecated = obj.deprecated;
    this.namespaceId = obj.namespaceId;
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;
    this._den = obj.den;

    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.replacementAsccpManifestId = obj.replacementAsccpManifestId;
    this.replacement = obj.replacement;

    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;
  }

  get json(): any {
    return {
      asccpId: this.asccpId,
      manifestId: this.manifestId,
      guid: this.guid,
      propertyTerm: this.propertyTerm,
      reusable: this.reusable,
      nillable: this.nillable,
      deprecated: this.deprecated,
      replacementAsccpManifestId: this.replacementAsccpManifestId,
      replacement: (!!this.replacement) ? this.replacement.json : undefined,
      definition: this.definition,
      definitionSource: this.definitionSource,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      sinceManifestId: this.sinceManifestId,
      sinceReleaseId: this.sinceReleaseId,
      sinceReleaseNum: this.sinceReleaseNum,
      lastChangedManifestId: this.lastChangedManifestId,
      lastChangedReleaseId: this.lastChangedReleaseId,
      lastChangedReleaseNum: this.lastChangedReleaseNum,
    };
  }

  get hashCode(): number {
    return ((this.asccpId) ? this.asccpId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.propertyTerm) ? hashCode4String(this.propertyTerm) : 0) +
      ((this.reusable) ? 1231 : 1237) +
      ((this.nillable) ? 1231 : 1237) +
      ((this.deprecated) ? 1231 : 1237) +
      ((this.replacementAsccpManifestId) ? this.replacementAsccpManifestId : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((this.namespaceId) ? this.namespaceId : 0) +
      ((!!this.state) ? hashCode4String(this.state) : 0) +
      ((!!this.owner) ? hashCode4String(this.owner) : 0) +
      ((this.releaseId) ? this.releaseId : 0) +
      ((!!this.releaseNum) ? hashCode4String(this.releaseNum) : 0) +
      ((this.revisionNum) ? this.revisionNum : 0) +
      ((this.revisionTrackingNum) ? this.revisionTrackingNum : 0);
  }

  get propertyTerm(): string {
    return this._propertyTerm;
  }

  set propertyTerm(value: string) {
    value = toCamelCase(emptyToUndefined(value));
    this._propertyTerm = value;
    this._node.fireChangeEvent('propertyTerm', value);
  }

  get den(): string {
    if (this.roleOfAccNode && this.roleOfAccNode.accNode) {
      return this.propertyTerm + '. ' + this.roleOfAccNode.accNode.objectClassTerm;
    } else {
      return this._den ? this._den : this.propertyTerm;
    }

  }

  set den(val: string) { // do nothing
  }

  get roleOfAccNode(): AccFlatNode {
    return this._node.children[0] as AccFlatNode;
  }

  get namespaceId(): number {
    return this._namespaceId;
  }

  set namespaceId(value: number) {
    this._namespaceId = value;
    this._node.fireChangeEvent('namespaceId', value);
  }

  get reusable(): boolean {
    return this._reusable;
  }

  set reusable(value: boolean) {
    this._reusable = value;
    this._node.fireChangeEvent('reusable', value);
  }

  get nillable(): boolean {
    return this._nillable;
  }

  set nillable(value: boolean) {
    this._nillable = value;
    this._node.fireChangeEvent('nillable', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }
}

export class CcAsccpNodeDetail extends CcNodeDetail {
  ascc: AsccDetail;
  asccp: AsccpDetail;

  constructor(node: AsccpFlatNode, obj: any) {
    super(node);
    if (obj.ascc) {
      this.ascc = new AsccDetail(node, obj.ascc);
    }
    if (obj.asccp) {
      this.asccp = new AsccpDetail(node, obj.asccp);
    }

    this.reset();
  }

  get hashCode(): number {
    return this.asccp.hashCode + ((this.ascc) ? this.ascc.hashCode : 0);
  }
}

export class EntityType {
  constructor(public value: number, public name: string) {
  }
}

export const Attribute: EntityType = new EntityType(0, 'Attribute');
export const Element: EntityType = new EntityType(1, 'Element');

export const EntityTypes: EntityType[] = [
  Attribute, Element
];

class BccDetail {
  private _node: CcFlatNode;
  manifestId: number;
  bccId: number;
  guid: string;
  den: string;
  private _entityType: number;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _nillable: boolean;
  private _deprecated: boolean;
  private _defaultValue: string;
  private _fixedValue: string;
  private _fixedOrDefault: string;
  private _definition: string;
  private _definitionSource: string;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;

  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  constructor(node: BccpFlatNode, obj: any) {
    this._node = node;

    this.manifestId = obj.manifestId;
    this.bccId = obj.bccId;
    this.guid = obj.guid;
    this.den = obj.den;
    this.entityType = obj.entityType;
    this.cardinalityMin = obj.cardinalityMin;
    this.cardinalityMax = obj.cardinalityMax;
    this.deprecated = obj.deprecated;
    this.nillable = obj.nillable;
    this.defaultValue = obj.defaultValue;
    this.fixedValue = obj.fixedValue;
    this.fixedOrDefault = this.defaultValue ? 'default' : this.fixedValue ? 'fixed' : 'none';
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;

    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;
  }

  get json(): any {
    return {
      bccId: this.bccId,
      manifestId: this.manifestId,
      guid: this.guid,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      deprecated: this.deprecated,
      nillable: this.nillable,
      entityType: this.entityType,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      definition: this.definition,
      definitionSource: this.definitionSource,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      sinceManifestId: this.sinceManifestId,
      sinceReleaseId: this.sinceReleaseId,
      sinceReleaseNum: this.sinceReleaseNum,
      lastChangedManifestId: this.lastChangedManifestId,
      lastChangedReleaseId: this.lastChangedReleaseId,
      lastChangedReleaseNum: this.lastChangedReleaseNum,
    };
  }

  get hashCode(): number {
    return ((this.bccId) ? this.bccId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.nillable) ? 1231 : 1237) +
      ((this.deprecated) ? 1231 : 1237) +
      ((this.entityType) ? this.entityType : 0) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((!!this.owner) ? hashCode4String(this.owner) : 0) +
      ((this.releaseId) ? this.releaseId : 0) +
      ((!!this.releaseNum) ? hashCode4String(this.releaseNum) : 0) +
      ((this.revisionNum) ? this.revisionNum : 0) +
      ((this.revisionTrackingNum) ? this.revisionTrackingNum : 0);
  }

  get entityType(): number {
    return this._entityType;
  }

  set entityType(value: number) {
    this._entityType = value;
    this._node.fireChangeEvent('entityType', value);
  }

  get cardinalityMin(): number {
    return this._cardinalityMin;
  }

  set cardinalityMin(value: number) {
    this._cardinalityMin = value;
    this._node.cardinalityMin = value;
    this._node.fireChangeEvent('cardinalityMin', value);
  }

  get cardinalityMax(): number {
    return this._cardinalityMax;
  }

  set cardinalityMax(value: number) {
    this._cardinalityMax = value;
    this._node.cardinalityMax = value;
    this._node.fireChangeEvent('cardinalityMax', value);
  }

  get nillable(): boolean {
    return this._nillable;
  }

  set nillable(value: boolean) {
    this._nillable = value;
    this._node.fireChangeEvent('nillable', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get defaultValue(): string {
    return this._defaultValue;
  }

  set defaultValue(value: string) {
    value = emptyToUndefined(value);
    this._defaultValue = value;
    this._node.fireChangeEvent('defaultValue', value);
  }

  get fixedValue(): string {
    return this._fixedValue;
  }

  set fixedValue(value: string) {
    value = emptyToUndefined(value);
    this._fixedValue = value;
    this._node.fireChangeEvent('fixedValue', value);
  }

  get fixedOrDefault(): string {
    return this._fixedOrDefault;
  }

  set fixedOrDefault(value: string) {
    this._fixedOrDefault = value;
    if (value === 'fixed') {
      this.defaultValue = null;
      if (this.nillable) {
        this.nillable = false;
      }
    } else if (value === 'default') {
      this.fixedValue = null;
    } else {
      this.fixedValue = null;
      this.defaultValue = null;
    }
    this._node.fireChangeEvent('fixedOrDefault', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }
}

class BccpDetail {
  private _node: BccpFlatNode;

  manifestId: number;
  bccpId: number;
  guid: string;
  private _propertyTerm: string;
  private _namespaceId: number;
  private _nillable: boolean;
  private _deprecated: boolean;
  private _defaultValue: string;
  private _fixedValue: string;
  private _fixedOrDefault: string;
  private _definition: string;
  private _definitionSource: string;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;

  replacementBccpManifestId: number;
  replacement: BccpDetail;

  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  constructor(node: BccpFlatNode, obj: any) {
    this._node = node;

    this.manifestId = obj.manifestId;
    this.bccpId = obj.bccpId;
    this.guid = obj.guid;
    this.propertyTerm = obj.propertyTerm;
    this.nillable = obj.nillable;
    this.deprecated = obj.deprecated;
    this.replacementBccpManifestId = obj.replacementBccpManifestId;
    this.replacement = obj.replacement;
    this.namespaceId = obj.namespaceId;
    this.defaultValue = obj.defaultValue;
    this.fixedValue = obj.fixedValue;
    this.fixedOrDefault = this.defaultValue ? 'default' : this.fixedValue ? 'fixed' : 'none';
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;

    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;
  }

  get json(): any {
    return {
      bccpId: this.bccpId,
      manifestId: this.manifestId,
      guid: this.guid,
      propertyTerm: this.propertyTerm,
      nillable: this.nillable,
      deprecated: this.deprecated,
      replacementBccpManifestId: this.replacementBccpManifestId,
      replacement: (!!this.replacement) ? this.replacement.json : undefined,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      definition: this.definition,
      definitionSource: this.definitionSource,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      sinceManifestId: this.sinceManifestId,
      sinceReleaseId: this.sinceReleaseId,
      sinceReleaseNum: this.sinceReleaseNum,
      lastChangedManifestId: this.lastChangedManifestId,
      lastChangedReleaseId: this.lastChangedReleaseId,
      lastChangedReleaseNum: this.lastChangedReleaseNum,
    };
  }

  get hashCode(): number {
    return ((this.bccpId) ? this.bccpId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.propertyTerm) ? hashCode4String(this.propertyTerm) : 0) +
      ((this.nillable) ? 1231 : 1237) +
      ((this.deprecated) ? 1231 : 1237) +
      ((this.replacementBccpManifestId) ? this.replacementBccpManifestId : 0) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((this.namespaceId) ? this.namespaceId : 0) +
      ((!!this.state) ? hashCode4String(this.state) : 0) +
      ((!!this.owner) ? hashCode4String(this.owner) : 0) +
      ((this.releaseId) ? this.releaseId : 0) +
      ((!!this.releaseNum) ? hashCode4String(this.releaseNum) : 0) +
      ((this.revisionNum) ? this.revisionNum : 0) +
      ((this.revisionTrackingNum) ? this.revisionTrackingNum : 0);
  }

  get propertyTerm(): string {
    return this._propertyTerm;
  }

  set propertyTerm(value: string) {
    value = toCamelCase(emptyToUndefined(value));
    this._propertyTerm = value;
    this._node.fireChangeEvent('propertyTerm', value);
  }

  get den(): string {
    return (this.propertyTerm + '. ' + this._node.bdtNode.den).replace('. Type', '');
  }

  set den(val: string) { // do nothing
  }

  get namespaceId(): number {
    return this._namespaceId;
  }

  set namespaceId(value: number) {
    this._namespaceId = value;
    this._node.fireChangeEvent('namespaceId', value);
  }

  get nillable(): boolean {
    return this._nillable;
  }

  set nillable(value: boolean) {
    this._nillable = value;
    if (value && this.fixedOrDefault === 'fixed') {
      this.fixedOrDefault = 'none';
    }
    this._node.fireChangeEvent('nillable', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get defaultValue(): string {
    return this._defaultValue;
  }

  set defaultValue(value: string) {
    value = emptyToUndefined(value);
    this._defaultValue = value;
    this._node.fireChangeEvent('defaultValue', value);
  }

  get fixedValue(): string {
    return this._fixedValue;
  }

  set fixedValue(value: string) {
    value = emptyToUndefined(value);
    this._fixedValue = value;
    this._node.fireChangeEvent('fixedValue', value);
  }

  get fixedOrDefault(): string {
    return this._fixedOrDefault;
  }

  set fixedOrDefault(value: string) {
    this._fixedOrDefault = value;
    if (value === 'fixed') {
      this.defaultValue = null;
      if (this.nillable) {
        this.nillable = false;
      }
    } else if (value === 'default') {
      this.fixedValue = null;
    } else {
      this.fixedValue = null;
      this.defaultValue = null;
    }
    this._node.fireChangeEvent('fixedOrDefault', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }
}

class BdtDetail {
  private _node: BccpFlatNode;

  manifestId: number;
  bdtId: number;
  guid: string;
  dataTypeTerm: string;
  representationTerm: string;
  qualifier: string;
  facetMinLength: number;
  facetMaxLength: number;
  facetPattern: string;
  facetMinInclusive: string;
  facetMinExclusive: string;
  facetMaxInclusive: string;
  facetMaxExclusive: string;
  namespaceId: number;
  den: string;
  definition: string;
  definitionSource: string;
  hasNoSc: boolean;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;

  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  bdtPriRestriList: CcBdtPriRestri[];

  constructor(node: BccpFlatNode, obj: any) {
    this._node = node;

    this.manifestId = obj.manifestId;
    this.bdtId = obj.bdtId;
    this.guid = obj.guid;
    this.dataTypeTerm = obj.dataTypeTerm;
    this.representationTerm = obj.representationTerm;
    this.qualifier = obj.qualifier;
    this.facetMinLength = obj.facetMinLength;
    this.facetMaxLength = obj.facetMaxLength;
    this.facetPattern = obj.facetPattern;
    this.facetMinInclusive = obj.facetMinInclusive;
    this.facetMinExclusive = obj.facetMinExclusive;
    this.facetMaxInclusive = obj.facetMaxInclusive;
    this.facetMaxExclusive = obj.facetMaxExclusive;
    this.namespaceId = obj.namespaceId;
    this.den = obj.den;
    this.definition = obj.definition;
    this.definitionSource = obj.definitionSource;
    this.hasNoSc = obj.hasNoSc;

    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;

    this.bdtPriRestriList = obj.bdtPriRestriList;
  }
}

export class CcBccpNodeDetail extends CcNodeDetail {
  bcc: BccDetail;
  bccp: BccpDetail;
  bdt: BdtDetail;

  constructor(node: BccpFlatNode, obj: any) {
    super(node);
    if (obj.bcc) {
      this.bcc = new BccDetail(node, obj.bcc);
    }
    if (obj.bccp) {
      this.bccp = new BccpDetail(node, obj.bccp);
    }
    if (obj.bdt) {
      this.bdt = new BdtDetail(node, obj.bdt);
    }
    this.reset();
  }

  get hashCode(): number {
    return this.bccp.hashCode + ((this.bcc) ? this.bcc.hashCode : 0);
  }
}

export class CcBdtPriRestri {
  parent: CcDtNodeDetail | CcBdtScNodeDetail;
  bdtPriRestriId: number;
  bdtScPriRestriId: number;
  type: string;
  cdtAwdPriId: number;
  cdtAwdPriXpsTypeMapId: number;
  cdtScAwdPriId: number;
  cdtScAwdPriXpsTypeMapId: number;
  primitiveName: string;
  xbtId: number;
  xbtName: string;
  codeListManifestId: number;
  codeListName: string;
  agencyIdListManifestId: number;
  agencyIdListName: string;
  _default: boolean;
  isSc: boolean;
  inherited: boolean;

  _selectedCodeList: CodeListForList;
  get selectedCodeList(): CodeListForList {
    return this._selectedCodeList;
  }

  set selectedCodeList(val: CodeListForList) {
    this._selectedCodeList = val;
    if (!!val) {
      // Changed from 'Agency ID List' to 'Code List'
      this.type = 'CodeList';

      if (!!this.agencyIdListManifestId) {
        // If the Agency ID List was a default, it changes to 'token'.
        if (this.parent.defaultValueDomain.name === this.agencyIdListName) {
          this.parent.defaultValueDomain = 'token';
        }

        this.selectedAgencyIdList = undefined;
        this.agencyIdListManifestId = undefined;
        this.agencyIdListName = undefined;
      }

      this.codeListManifestId = val.codeListManifestId;
      this.codeListName = val.codeListName;
    } else {
      this.codeListManifestId = undefined;
      this.codeListName = undefined;
    }
  }

  _selectedAgencyIdList: AgencyIdList;
  get selectedAgencyIdList(): AgencyIdList {
    return this._selectedAgencyIdList;
  }

  set selectedAgencyIdList(val: AgencyIdList) {
    this._selectedAgencyIdList = val;
    if (!!val) {
      // Changed from 'Code List' to 'Agency ID List'
      this.type = 'AgencyIdList';

      if (!!this.codeListManifestId) {
        // If the Code List was a default, it changes to 'token'.
        if (this.parent.defaultValueDomain.name === this.codeListName) {
          this.parent.defaultValueDomain = 'token';
        }

        this.selectedCodeList = undefined;
        this.codeListManifestId = undefined;
        this.codeListName = undefined;
      }

      this.agencyIdListManifestId = val.agencyIdListManifestId;
      this.agencyIdListName = val.name;
    } else {
      this.agencyIdListManifestId = undefined;
      this.agencyIdListName = undefined;
    }
  }

  $hashCode: number;

  constructor(detail: CcDtNodeDetail | CcBdtScNodeDetail, obj: CcBdtPriRestri, dtPrimitiveAware: DtPrimitiveAware) {
    this.parent = detail;
    this.type = obj.type;
    this.bdtPriRestriId = obj.bdtPriRestriId;
    this.bdtScPriRestriId = obj.bdtScPriRestriId;
    this.cdtAwdPriId = obj.cdtAwdPriId;
    this.cdtAwdPriXpsTypeMapId = obj.cdtAwdPriXpsTypeMapId;
    this.cdtScAwdPriId = obj.cdtScAwdPriId;
    this.cdtScAwdPriXpsTypeMapId = obj.cdtScAwdPriXpsTypeMapId;
    this.isSc = !!this.cdtAwdPriId;

    this.primitiveName = obj.primitiveName;
    this.xbtId = obj.xbtId;
    this.xbtName = obj.xbtName;
    this.codeListManifestId = obj.codeListManifestId;
    this.codeListName = obj.codeListName;
    this.agencyIdListManifestId = obj.agencyIdListManifestId;
    this.agencyIdListName = obj.agencyIdListName;
    this._default = obj.default;
    this.inherited = obj.inherited;

    if (this.isCodeList) {
      this.selectedCodeList = dtPrimitiveAware.codeLists.filter(e => e.codeListManifestId === this.codeListManifestId)[0];
    }

    if (this.isAgencyIdList) {
      this.selectedAgencyIdList = dtPrimitiveAware.agencyIdLists.filter(e => e.agencyIdListManifestId === this.agencyIdListManifestId)[0];
    }

    this.$hashCode = this.hashCode;
  }

  get id(): number {
    return this.isSc ? this.cdtScAwdPriId : this.cdtAwdPriId;
  }

  get name(): string {
    return this.isPrimitive ? this.primitiveName : this.isCodeList ? this.codeListName : this.agencyIdListName;
  }

  get isPrimitive(): boolean {
    return this.type === 'Primitive';
  }

  get isCodeList(): boolean {
    return this.type === 'CodeList';
  }

  get isAgencyIdList(): boolean {
    return this.type === 'AgencyIdList';
  }

  get hashCode(): number {
    const hash = hashCode4Array(this.cdtScAwdPriId, this.cdtAwdPriId, this.type, this.primitiveName, this.default,
      this.xbtId, this.xbtName,
      (this.selectedCodeList) ? {
        codeListManifestId: this.selectedCodeList.codeListManifestId,
        codeListName: this.selectedCodeList.codeListName
      } : undefined, (this.selectedAgencyIdList) ? {
        agencyIdListManifestId: this.selectedAgencyIdList.agencyIdListManifestId,
        agencyIdListValueManifestId: this.selectedAgencyIdList.agencyIdListValueManifestId,
        name: this.selectedAgencyIdList.name
      } : undefined);
    return hash;
  }

  get isChanged(): boolean {
    return this.$hashCode !== this.hashCode;
  }

  get json(): any {
    return {
      type: this.type,
      bdtPriRestriId: this.bdtPriRestriId,
      bdtScPriRestriId: this.bdtScPriRestriId,
      cdtAwdPriId: this.cdtAwdPriId,
      cdtAwdPriXpsTypeMapId: this.cdtAwdPriXpsTypeMapId,
      cdtScAwdPriId: this.cdtScAwdPriId,
      cdtScAwdPriXpsTypeMapId: this.cdtScAwdPriXpsTypeMapId,
      primitiveName: this.primitiveName,
      codeListManifestId: this.isCodeList ? this.selectedCodeList.codeListManifestId : null,
      agencyIdListManifestId: this.isAgencyIdList ? this.selectedAgencyIdList.agencyIdListManifestId : null,
      xbtId: this.xbtId,
      xbtName: this.xbtName,
      default: this.default,
    };
  }

  get default(): boolean {
    return this._default;
  }

  set default(value) {
    this._default = value;
  }
}

export class XbtForList {
  xbtId: number;
  manifestId: number;
  name: string;
  deprecated: boolean;
}

export class CcXbt {
  xbtId: number;
  xbtName: string;
  default: boolean;
  restriId: number;

  constructor(obj: any) {
    this.xbtId = obj.xbtId;
    this.xbtName = obj.xbtName ? obj.xbtName : obj.name;
    this.default = obj.default;
    this.restriId = obj.restriId;
  }

  get hashCode(): number {
    return hashCode4Array(this.xbtId, this.xbtName, this.default, this.restriId);
  }
}

export interface DtPrimitiveAware {
  codeLists: CodeListForList[];
  agencyIdLists: AgencyIdList[];
  xbtList: CcXbt[];
}

export interface ValueDomainEntity {
  type: string;
  id: number;
  name: string;
  default: boolean;
}

export class CcDtNodeDetail extends CcNodeDetail {
  bdtId: number;
  private _definition: string;
  private _definitionSource: string;

  private _dataTypeTerm: string;
  private _representationTerm: string;
  private _qualifier: string;
  basedBdtId: number;
  basedBdtManifestId: number;
  basedBdtDen: string;
  basedBdtState: string;
  private _facetMinLength: number;
  private _facetMaxLength: number;
  private _facetPattern: string;
  private _facetMinInclusive: string;
  private _facetMinExclusive: string;
  private _facetMaxInclusive: string;
  private _facetMaxExclusive: string;
  private _sixDigitId: string;
  private _contentComponentDefinition: string;
  commonlyUsed: boolean;
  private _namespaceId: number;
  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;
  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;
  spec: string;

  bdtPriRestriList: CcBdtPriRestri[];
  bdtPriRestriListByGroup: any[];

  // To display value domains on UI
  primitiveMap: Map<string, ValueDomainEntity[]>;
  codeListList: ValueDomainEntity[];
  agencyIdListList: ValueDomainEntity[];

  constructor(node: DtFlatNode, obj: any) {
    super(node);

    this.bdtId = obj.bdtId;
    this.guid = obj.guid;
    this._definition = obj.definition;
    this._definitionSource = obj.definitionSource;
    this._representationTerm = obj.representationTerm;
    this._dataTypeTerm = obj.dataTypeTerm;
    this._qualifier = obj.qualifier;
    this.basedBdtId = obj.basedBdtId;
    this.basedBdtManifestId = obj.basedBdtManifestId;
    this.basedBdtDen = obj.basedBdtDen;
    this.basedBdtState = obj.basedBdtState;
    this._facetMinLength = obj.facetMinLength;
    this._facetMaxLength = obj.facetMaxLength;
    this._facetPattern = obj.facetPattern;
    this._facetMinInclusive = obj.facetMinInclusive;
    this._facetMinExclusive = obj.facetMinExclusive;
    this._facetMaxInclusive = obj.facetMaxInclusive;
    this._facetMaxExclusive = obj.facetMaxExclusive;
    this._sixDigitId = obj.sixDigitId;
    this._contentComponentDefinition = obj.contentComponentDefinition;
    this.commonlyUsed = obj.commonlyUsed;
    this._namespaceId = obj.namespaceId;
    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;
    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;
    this.spec = obj.spec;

    this.bdtPriRestriList = obj.bdtPriRestriList;
    this.bdtPriRestriListByGroup = [];
    this.primitiveMap = new Map<string, ValueDomainEntity[]>();
    this.codeListList = [];
    this.agencyIdListList = [];

    this.reset();
  }

  update(dtPrimitiveAware: DtPrimitiveAware) {
    if (this.bdtPriRestriList.length > 0) {
      if (this.bdtPriRestriList[0] instanceof CcBdtPriRestri) {
        return;
      }
    }
    this.bdtPriRestriList = this.bdtPriRestriList.map(row => new CcBdtPriRestri(this, row, dtPrimitiveAware));
    this.updateValueDomainGroup();
    this.reset();
  }

  updateValueDomainGroup() {
    this.primitiveMap = new Map<string, ValueDomainEntity[]>();
    const primitiveList = [];
    this.codeListList = [];
    this.agencyIdListList = [];

    const bdtPriRestriList = this.bdtPriRestriList;
    for (const valueDomain of bdtPriRestriList) {
      if (valueDomain.type === 'Primitive') {
        if (!this.primitiveMap.has(valueDomain.primitiveName)) {
          this.primitiveMap.set(valueDomain.primitiveName, []);
        }
        const domainEntity = new class implements ValueDomainEntity {
          get self(): CcBdtPriRestri {
            return valueDomain;
          }

          get type(): string {
            return valueDomain.type;
          }

          get id(): number {
            return valueDomain.xbtId;
          }

          set id(id: number) {
            valueDomain.xbtId = id;
          }

          get name(): string {
            return valueDomain.xbtName;
          }

          set name(name: string) {
            valueDomain.xbtName = name;
          }

          get default(): boolean {
            return valueDomain.default;
          }

          set default(isDefault: boolean) {
            valueDomain.default = isDefault;
          }
        };
        primitiveList.push(domainEntity);
        this.primitiveMap.get(valueDomain.primitiveName).push(domainEntity);
      }
      if (valueDomain.type === 'CodeList') {
        this.codeListList.push(new class implements ValueDomainEntity {
          get self(): CcBdtPriRestri {
            return valueDomain;
          }

          get type(): string {
            return valueDomain.type;
          }

          get id(): number {
            return (valueDomain.selectedCodeList) ? valueDomain.selectedCodeList.codeListManifestId : undefined;
          }

          set id(id: number) {
            if (valueDomain.selectedCodeList) {
              valueDomain.selectedCodeList.codeListManifestId = id;
            }
          }

          get name(): string {
            return (valueDomain.selectedCodeList) ? valueDomain.selectedCodeList.codeListName : undefined;
          }

          set name(name: string) {
            if (valueDomain.selectedCodeList) {
              valueDomain.selectedCodeList.codeListName = name;
            }
          }

          get default(): boolean {
            return valueDomain.default;
          }

          set default(isDefault: boolean) {
            valueDomain.default = isDefault;
          }
        });
      }
      if (valueDomain.type === 'AgencyIdList') {
        this.agencyIdListList.push(new class implements ValueDomainEntity {
          get self(): CcBdtPriRestri {
            return valueDomain;
          }

          get type(): string {
            return valueDomain.type;
          }

          get id(): number {
            return (valueDomain.selectedAgencyIdList) ? valueDomain.selectedAgencyIdList.agencyIdListManifestId : undefined;
          }

          set id(id: number) {
            if (valueDomain.selectedAgencyIdList) {
              valueDomain.selectedAgencyIdList.agencyIdListManifestId = id;
            }
          }

          get name(): string {
            return (valueDomain.selectedAgencyIdList) ? valueDomain.selectedAgencyIdList.name : undefined;
          }

          set name(name: string) {
            if (valueDomain.selectedAgencyIdList) {
              valueDomain.selectedAgencyIdList.name = name;
            }
          }

          get default(): boolean {
            return valueDomain.default;
          }

          set default(isDefault: boolean) {
            valueDomain.default = isDefault;
          }
        });
      }
    }

    this.bdtPriRestriListByGroup = [];
    if (primitiveList.length > 0) {
      this.bdtPriRestriListByGroup.push({
        label: 'Primitive',
        list: primitiveList.sort((a, b) => {
          const aName = a.self.primitiveName + ' - ' + a.name;
          const bName = b.self.primitiveName + ' - ' + b.name;
          return compare(aName, bName);
        })
      });
    }
    if (this.codeListList.length > 0) {
      this.bdtPriRestriListByGroup.push({
        label: 'Code List',
        list: this.codeListList
      });
    }
    if (this.agencyIdListList.length > 0) {
      this.bdtPriRestriListByGroup.push({
        label: 'Agency ID List',
        list: this.agencyIdListList
      });
    }
  }

  get valueDomains(): any[] {
    let valueDomains = [];
    for (const primitiveName of this.primitiveMap.keys()) {
      valueDomains.push({
        type: 'Primitive',
        name: primitiveName,
        xbtList: this.primitiveMap.get(primitiveName).map(e => e.name).sort((a, b) => compare(a, b))
      });
    }
    valueDomains = valueDomains.sort((a, b) => compare(a.name, b.name));

    const userCodeLists = [];
    for (const codeList of this.codeListList.sort((a, b) => compare(a.name, b.name))) {
      const bdtPriRestri = this.bdtPriRestriList.filter(e => e.type === 'CodeList' && e.codeListName === codeList.name)[0];
      if (!bdtPriRestri.inherited) {
        userCodeLists.push({
          type: 'CodeList',
          name: codeList.name,
          bdtPriRestri
        });
      } else {
        valueDomains.push({
          type: 'CodeList',
          name: codeList.name,
          bdtPriRestri
        });
      }
    }
    valueDomains.push(...userCodeLists);

    const userAgencyIdLists = [];
    for (const agencyIdList of this.agencyIdListList.sort((a, b) => compare(a.name, b.name))) {
      const bdtPriRestri = this.bdtPriRestriList.filter(e => e.type === 'AgencyIdList' && e.agencyIdListName === agencyIdList.name)[0];
      if (!bdtPriRestri.inherited) {
        userAgencyIdLists.push({
          type: 'AgencyIdList',
          name: agencyIdList.name,
          bdtPriRestri
        });
      } else {
        valueDomains.push({
          type: 'AgencyIdList',
          name: agencyIdList.name,
          bdtPriRestri
        });
      }
    }
    valueDomains.push(...userAgencyIdLists);

    return valueDomains;
  }

  get defaultValueDomain(): any {
    for (const r of this.primitiveMap.values()) {
      for (const e of r) {
        if (e.default) {
          return e;
        }
      }
    }
    for (const e of this.codeListList) {
      if (e.default) {
        return e;
      }
    }
    for (const e of this.agencyIdListList) {
      if (e.default) {
        return e;
      }
    }
    return undefined;
  }

  set defaultValueDomain(val: any) {
    for (const r of this.primitiveMap.values()) {
      for (const e of r) {
        e.default = (typeof val === 'string') ? (e.name === val) : (e === val);
      }
    }
    for (const e of this.codeListList) {
      e.default = (typeof val === 'string') ? (e.name === val) : (e === val);
    }
    for (const e of this.agencyIdListList) {
      e.default = (typeof val === 'string') ? (e.name === val) : (e === val);
    }
    this._node.fireChangeEvent('defaultValueDomain', val);
  }

  get json(): any {
    return {
      bdtId: this.bdtId,
      manifestId: this.manifestId,
      guid: this.guid,
      definition: this.definition,
      definitionSource: this.definitionSource,
      representationTerm: this.representationTerm,
      dataTypeTerm: this.dataTypeTerm,
      qualifier: this._qualifier,
      basedBdtId: this.basedBdtId,
      basedBdtManifestId: this.basedBdtManifestId,
      basedBdtDen: this.basedBdtDen,
      basedBdtState: this.basedBdtState,
      facetMinLength: this._facetMinLength,
      facetMaxLength: this._facetMaxLength,
      facetPattern: this._facetPattern,
      facetMinInclusive: this._facetMinInclusive,
      facetMinExclusive: this._facetMinExclusive,
      facetMaxInclusive: this._facetMaxInclusive,
      facetMaxExclusive: this._facetMaxExclusive,
      sixDigitId: this._sixDigitId,
      contentComponentDefinition: this.contentComponentDefinition,
      commonlyUsed: this.commonlyUsed,
      namespaceId: this._namespaceId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      bdtPriRestriList: this.bdtPriRestriList.map(e => e.json),
      defaultValueDomain: this.defaultValueDomain
    };
  }

  get hashCode(): number {
    return hashCode4Array(this.bdtId, this.manifestId, this.guid, this.representationTerm, this.dataTypeTerm, this.qualifier,
      this.basedBdtId, this.basedBdtManifestId, this.basedBdtDen, this.basedBdtState,
      this._facetMinLength, this._facetMaxLength, this._facetPattern,
      this._facetMinInclusive, this._facetMinExclusive, this._facetMaxInclusive, this._facetMaxExclusive,
      this._sixDigitId, this.contentComponentDefinition, this.commonlyUsed,
      this._namespaceId, this.definition, this.definitionSource, this.state, this.releaseId, this.releaseNum, this.revisionNum,
      this.revisionTrackingNum, this.bdtPriRestriList);
  }

  get manifestId(): number {
    return (this._node as DtFlatNode).dtNode.manifestId;
  }

  set manifestId(value: number) {
    (this._node as DtFlatNode).dtNode.manifestId = value;
  }

  get guid(): string {
    return (this._node as DtFlatNode).dtNode.guid;
  }

  set guid(value: string) {
    (this._node as DtFlatNode).dtNode.guid = value;
  }

  get den(): string {
    if (this.qualifier) {
      return this.qualifier + '_ ' + this.dataTypeTerm + '. Type';
    }
    return this.dataTypeTerm + '. Type';
  }

  set den(val: string) { // do nothing
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }

  get dataTypeTerm(): string {
    return this._dataTypeTerm;
  }

  set dataTypeTerm(value: string) {
    value = emptyToUndefined(value);
    this._dataTypeTerm = value;
    this._node.fireChangeEvent('dataTypeTerm', value);
  }

  get representationTerm(): string {
    return this._representationTerm;
  }

  set representationTerm(value: string) {
    value = emptyToUndefined(value);
    this._representationTerm = value;
    this._node.fireChangeEvent('representationTerm', value);
  }

  get contentComponentDefinition(): string {
    return this._contentComponentDefinition;
  }

  set contentComponentDefinition(value: string) {
    value = emptyToUndefined(value);
    this._contentComponentDefinition = value;
    this._node.fireChangeEvent('contentComponentDefinition', value);
  }

  get qualifier(): string {
    return this._qualifier;
  }

  get qualifierList(): string[] {
    if (this._qualifier) {
      return this._qualifier.split('_ ');
    }
    return [];
  }

  set qualifier(value: string) {
    value = emptyToUndefined(value);
    this._qualifier = value;
    this._node.fireChangeEvent('qualifier', value);
  }

  get facetMinLength(): number {
    return this._facetMinLength;
  }

  set facetMinLength(value: number) {
    this._facetMinLength = value;
    this._node.fireChangeEvent('facetMinLength', value);
  }

  get facetMaxLength(): number {
    return this._facetMaxLength;
  }

  set facetMaxLength(value: number) {
    this._facetMaxLength = value;
    this._node.fireChangeEvent('facetMaxLength', value);
  }

  get facetPattern(): string {
    return this._facetPattern;
  }

  set facetPattern(value: string) {
    this._facetPattern = value;
    this._node.fireChangeEvent('facetPattern', value);
  }

  get facetMinInclusive(): string {
    return this._facetMinInclusive;
  }

  set facetMinInclusive(value: string) {
    this._facetMinInclusive = value;
    this._node.fireChangeEvent('facetMinInclusive', value);
  }

  get facetMinExclusive(): string {
    return this._facetMinExclusive;
  }

  set facetMinExclusive(value: string) {
    this._facetMinExclusive = value;
    this._node.fireChangeEvent('facetMinExclusive', value);
  }

  get facetMaxInclusive(): string {
    return this._facetMaxInclusive;
  }

  set facetMaxInclusive(value: string) {
    this._facetMaxInclusive = value;
    this._node.fireChangeEvent('facetMaxInclusive', value);
  }

  get facetMaxExclusive(): string {
    return this._facetMaxExclusive;
  }

  set facetMaxExclusive(value: string) {
    this._facetMaxExclusive = value;
    this._node.fireChangeEvent('facetMaxExclusive', value);
  }

  get sixDigitId(): string {
    return this._sixDigitId;
  }

  set sixDigitId(value: string) {
    this._sixDigitId = value;
    this._node.fireChangeEvent('sixDigitId', value);
  }

  get namespaceId(): number {
    return this._namespaceId;
  }

  set namespaceId(value: number) {
    this._namespaceId = value;
    this._node.fireChangeEvent('namespaceId', value);
  }
}

export class CcBdtScNodeDetail extends CcNodeDetail {
  bdtScId: number;
  private _definition: string;
  private _definitionSource: string;
  private _defaultValue: string;
  private _fixedValue: string;
  fixedOrDefault: string;
  private _facetMinLength: number;
  private _facetMaxLength: number;
  private _facetPattern: string;
  private _facetMinInclusive: string;
  private _facetMinExclusive: string;
  private _facetMaxInclusive: string;
  private _facetMaxExclusive: string;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;
  sinceManifestId: number;
  sinceReleaseId: number;
  sinceReleaseNum: string;
  lastChangedManifestId: number;
  lastChangedReleaseId: number;
  lastChangedReleaseNum: string;

  private _propertyTerm: string;
  private _representationTerm: string;
  objectClassTerm: string;
  spec: string;
  basedDtScId: number;

  prevCardinalityMin: number;
  prevCardinalityMax: number;
  baseCardinalityMin: number;
  baseCardinalityMax: number;

  bdtScPriRestriList: CcBdtPriRestri[];
  bdtScPriRestriListByGroup: any[];
  isCardinalityEditable: boolean;
  allowedCardinalities: any[];

  // To display value domains on UI
  primitiveMap: Map<string, ValueDomainEntity[]>;
  codeListList: ValueDomainEntity[];
  agencyIdListList: ValueDomainEntity[];

  constructor(node: DtScFlatNode, obj: any) {
    super(node);

    this.bdtScId = obj.bdtScId;
    this.guid = obj.guid;
    this._definition = obj.definition;
    this._definitionSource = obj.definitionSource;
    this._defaultValue = obj.defaultValue;
    this._fixedValue = obj.fixedValue;
    this._facetMinLength = obj.facetMinLength;
    this._facetMaxLength = obj.facetMaxLength;
    this._facetPattern = obj.facetPattern;
    this._facetMinInclusive = obj.facetMinInclusive;
    this._facetMinExclusive = obj.facetMinExclusive;
    this._facetMaxInclusive = obj.facetMaxInclusive;
    this._facetMaxExclusive = obj.facetMaxExclusive;
    this.cardinalityMax = obj.cardinalityMax;
    this.cardinalityMin = obj.cardinalityMin;
    this.prevCardinalityMin = obj.prevCardinalityMin;
    this.prevCardinalityMax = obj.prevCardinalityMax;
    this.baseCardinalityMin = obj.baseCardinalityMin;
    this.baseCardinalityMax = obj.baseCardinalityMax;

    this.isCardinalityEditable = true;

    if (this.prevCardinalityMin === null && this.baseCardinalityMin === null) {
      this.allowedCardinalities = [
        {value: 'Prohibited', disabled: true},
        {value: 'Optional', disabled: false},
        {value: 'Required', disabled: false}
      ];
    } else {
      if (this.prevCardinalityMin === 0 && this.prevCardinalityMax === 1) {
        this.isCardinalityEditable = true;
      } else {
        this.isCardinalityEditable = this.baseCardinalityMin !== this.baseCardinalityMax;
      }
      this.allowedCardinalities = [
        {value: 'Prohibited', disabled: false},
        {value: 'Optional', disabled: false},
        {value: 'Required', disabled: false}
      ];
    }

    this.fixedOrDefault = this.defaultValue ? 'default' : this.fixedValue ? 'fixed' : 'none';
    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;
    this.sinceManifestId = obj.sinceManifestId;
    this.sinceReleaseId = obj.sinceReleaseId;
    this.sinceReleaseNum = obj.sinceReleaseNum;
    this.lastChangedManifestId = obj.lastChangedManifestId;
    this.lastChangedReleaseId = obj.lastChangedReleaseId;
    this.lastChangedReleaseNum = obj.lastChangedReleaseNum;
    this._propertyTerm = obj.propertyTerm;
    this._representationTerm = obj.representationTerm;
    this.objectClassTerm = obj.objectClassTerm;
    this.basedDtScId = obj.basedDtScId;
    this.spec = obj.spec;

    this.bdtScPriRestriList = obj.bdtScPriRestriList;
    this.bdtScPriRestriListByGroup = [];

    this.reset();
  }

  update(dtPrimitiveAware: DtPrimitiveAware) {
    if (this.bdtScPriRestriList.length > 0) {
      if (this.bdtScPriRestriList[0] instanceof CcBdtPriRestri) {
        return;
      }
    }
    this.bdtScPriRestriList = this.bdtScPriRestriList.map(row => new CcBdtPriRestri(this, row, dtPrimitiveAware));
    this.updateValueDomainGroup();
    this.reset();
  }

  updateValueDomainGroup() {
    this.primitiveMap = new Map<string, ValueDomainEntity[]>();
    const primitiveList = [];
    this.codeListList = [];
    this.agencyIdListList = [];

    const bdtScPriRestriList = this.bdtScPriRestriList;
    for (const valueDomain of bdtScPriRestriList) {
      if (valueDomain.type === 'Primitive') {
        if (!this.primitiveMap.has(valueDomain.primitiveName)) {
          this.primitiveMap.set(valueDomain.primitiveName, []);
        }
        const domainEntity = new class implements ValueDomainEntity {
          get self(): CcBdtPriRestri {
            return valueDomain;
          }

          get type(): string {
            return valueDomain.type;
          }

          get id(): number {
            return valueDomain.xbtId;
          }

          set id(id: number) {
            valueDomain.xbtId = id;
          }

          get name(): string {
            return valueDomain.xbtName;
          }

          set name(name: string) {
            valueDomain.xbtName = name;
          }

          get default(): boolean {
            return valueDomain.default;
          }

          set default(isDefault: boolean) {
            valueDomain.default = isDefault;
          }
        };
        primitiveList.push(domainEntity);
        this.primitiveMap.get(valueDomain.primitiveName).push(domainEntity);
      }
      if (valueDomain.type === 'CodeList') {
        this.codeListList.push(new class implements ValueDomainEntity {
          get self(): CcBdtPriRestri {
            return valueDomain;
          }

          get type(): string {
            return valueDomain.type;
          }

          get id(): number {
            return (valueDomain.selectedCodeList) ? valueDomain.selectedCodeList.codeListManifestId : undefined;
          }

          set id(id: number) {
            if (valueDomain.selectedCodeList) {
              valueDomain.selectedCodeList.codeListManifestId = id;
            }
          }

          get name(): string {
            return (valueDomain.selectedCodeList) ? valueDomain.selectedCodeList.codeListName : undefined;
          }

          set name(name: string) {
            if (valueDomain.selectedCodeList) {
              valueDomain.selectedCodeList.codeListName = name;
            }
          }

          get default(): boolean {
            return valueDomain.default;
          }

          set default(isDefault: boolean) {
            valueDomain.default = isDefault;
          }
        });
      }
      if (valueDomain.type === 'AgencyIdList') {
        this.agencyIdListList.push(new class implements ValueDomainEntity {
          get self(): CcBdtPriRestri {
            return valueDomain;
          }

          get type(): string {
            return valueDomain.type;
          }

          get id(): number {
            return (valueDomain.selectedAgencyIdList) ? valueDomain.selectedAgencyIdList.agencyIdListManifestId : undefined;
          }

          set id(id: number) {
            if (valueDomain.selectedAgencyIdList) {
              valueDomain.selectedAgencyIdList.agencyIdListManifestId = id;
            }
          }

          get name(): string {
            return (valueDomain.selectedAgencyIdList) ? valueDomain.selectedAgencyIdList.name : undefined;
          }

          set name(name: string) {
            if (valueDomain.selectedAgencyIdList) {
              valueDomain.selectedAgencyIdList.name = name;
            }
          }

          get default(): boolean {
            return valueDomain.default;
          }

          set default(isDefault: boolean) {
            valueDomain.default = isDefault;
          }
        });
      }
    }

    this.bdtScPriRestriListByGroup = [];
    if (primitiveList.length > 0) {
      this.bdtScPriRestriListByGroup.push({
        label: 'Primitive',
        list: primitiveList.sort((a, b) => {
          const aName = a.self.primitiveName + ' - ' + a.name;
          const bName = b.self.primitiveName + ' - ' + b.name;
          return compare(aName, bName);
        })
      });
    }
    if (this.codeListList.length > 0) {
      this.bdtScPriRestriListByGroup.push({
        label: 'Code List',
        list: this.codeListList
      });
    }
    if (this.agencyIdListList.length > 0) {
      this.bdtScPriRestriListByGroup.push({
        label: 'Agency ID List',
        list: this.agencyIdListList
      });
    }
  }

  get valueDomains(): any[] {
    let valueDomains = [];
    for (const primitiveName of this.primitiveMap.keys()) {
      valueDomains.push({
        type: 'Primitive',
        name: primitiveName,
        xbtList: this.primitiveMap.get(primitiveName).map(e => e.name).sort((a, b) => compare(a, b))
      });
    }
    valueDomains = valueDomains.sort((a, b) => compare(a.name, b.name));

    const userCodeLists = [];
    for (const codeList of this.codeListList.sort((a, b) => compare(a.name, b.name))) {
      const bdtScPriRestri = this.bdtScPriRestriList.filter(e => e.type === 'CodeList' && e.codeListName === codeList.name)[0];
      if (!bdtScPriRestri.inherited) {
        userCodeLists.push({
          type: 'CodeList',
          name: codeList.name,
          bdtScPriRestri
        });
      } else {
        valueDomains.push({
          type: 'CodeList',
          name: codeList.name,
          bdtScPriRestri
        });
      }
    }
    valueDomains.push(...userCodeLists);

    const userAgencyIdLists = [];
    for (const agencyIdList of this.agencyIdListList.sort((a, b) => compare(a.name, b.name))) {
      const bdtScPriRestri = this.bdtScPriRestriList.filter(e => e.type === 'AgencyIdList' && e.agencyIdListName === agencyIdList.name)[0];
      if (!bdtScPriRestri.inherited) {
        userAgencyIdLists.push({
          type: 'AgencyIdList',
          name: agencyIdList.name,
          bdtScPriRestri
        });
      } else {
        valueDomains.push({
          type: 'AgencyIdList',
          name: agencyIdList.name,
          bdtScPriRestri
        });
      }
    }
    valueDomains.push(...userAgencyIdLists);

    return valueDomains;
  }

  get defaultValueDomain(): any {
    for (const r of this.primitiveMap.values()) {
      for (const e of r) {
        if (e.default) {
          return e;
        }
      }
    }
    for (const e of this.codeListList) {
      if (e.default) {
        return e;
      }
    }
    for (const e of this.agencyIdListList) {
      if (e.default) {
        return e;
      }
    }
    return undefined;
  }

  set defaultValueDomain(val: any) {
    for (const r of this.primitiveMap.values()) {
      for (const e of r) {
        e.default = (typeof val === 'string') ? (e.name === val) : (e === val);
      }
    }
    for (const e of this.codeListList) {
      e.default = (typeof val === 'string') ? (e.name === val) : (e === val);
    }
    for (const e of this.agencyIdListList) {
      e.default = (typeof val === 'string') ? (e.name === val) : (e === val);
    }
    this._node.fireChangeEvent('defaultValueDomain', val);
  }

  get propertyTerm(): string {
    return this._propertyTerm;
  }

  set propertyTerm(value: string) {
    value = toCamelCase(emptyToUndefined(value));
    this._propertyTerm = value;
    (this._node as DtScFlatNode).dtScNode.propertyTerm = value;
    this._node.fireChangeEvent('propertyTerm', value);
  }

  get representationTerm(): string {
    return this._representationTerm;
  }

  set representationTerm(value: string) {
    this._representationTerm = value;
    (this._node as DtScFlatNode).dtScNode.representationTerm = value;
    this._node.fireChangeEvent('representationTerm', value);
  }

  get json(): any {
    return {
      bdtScId: this.bdtScId,
      manifestId: this.manifestId,
      guid: this.guid,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      facetMinLength: this.facetMinLength,
      facetMaxLength: this.facetMaxLength,
      facetPattern: this.facetPattern,
      facetMinInclusive: this.facetMinInclusive,
      facetMinExclusive: this.facetMinExclusive,
      facetMaxInclusive: this.facetMaxInclusive,
      facetMaxExclusive: this.facetMaxExclusive,
      cardinalityMax: this.cardinalityMax,
      cardinalityMin: this.cardinalityMin,
      definition: this.definition,
      definitionSource: this.definitionSource,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
      bdtScPriRestriList: this.bdtScPriRestriList.map(e => e.json),
      propertyTerm: this.propertyTerm,
      representationTerm: this.representationTerm,
      objectClassTerm: this.objectClassTerm,
      sinceManifestId: this.sinceManifestId,
      sinceReleaseId: this.sinceReleaseId,
      sinceReleaseNum: this.sinceReleaseNum,
      lastChangedManifestId: this.lastChangedManifestId,
      lastChangedReleaseId: this.lastChangedReleaseId,
      lastChangedReleaseNum: this.lastChangedReleaseNum,
    };
  }

  get hashCode(): number {
    return hashCode4Array(this.bdtScId, this.manifestId, this.guid, this.representationTerm, this.objectClassTerm, this.propertyTerm,
      this.representationTerm, this.cardinalityMin, this.cardinalityMax, this.defaultValue, this.fixedValue,
      this.facetMinLength, this.facetMaxLength, this.facetPattern,
      this.facetMinInclusive, this.facetMinExclusive, this.facetMaxInclusive, this.facetMaxExclusive,
      this.definition, this.definitionSource, this.state, this.releaseId, this.releaseNum, this.revisionNum,
      this.revisionTrackingNum, this.bdtScPriRestriList);
  }

  get manifestId(): number {
    return (this._node as DtScFlatNode).dtScNode.manifestId;
  }

  set manifestId(value: number) {
    (this._node as DtScFlatNode).dtScNode.manifestId = value;
  }

  get guid(): string {
    return (this._node as DtScFlatNode).dtScNode.guid;
  }

  set guid(value: string) {
    (this._node as DtScFlatNode).dtScNode.guid = value;
  }

  get cardinalityMin(): number {
    return (this._node as DtScFlatNode).dtScNode.cardinalityMin;
  }

  set cardinalityMin(value: number) {
    (this._node as DtScFlatNode).dtScNode.cardinalityMin = value;
    this._node.fireChangeEvent('cardinalityMin', value);
  }

  get cardinalityMax(): number {
    return (this._node as DtScFlatNode).dtScNode.cardinalityMax;
  }

  set cardinalityMax(value: number) {
    (this._node as DtScFlatNode).dtScNode.cardinalityMax = value;
    this._node.fireChangeEvent('cardinalityMax', value);
  }


  get cardinality(): string {
    if (this.cardinalityMin === 0 && this.cardinalityMax === 0) {
      return 'Prohibited';
    } else if (this.cardinalityMin === 0 && this.cardinalityMax === 1) {
      return 'Optional';
    }
    return 'Required';
  }

  set cardinality(value: string) {
    if (value === 'Prohibited') {
      this.cardinalityMin = 0;
      this.cardinalityMax = 0;
    } else if (value === 'Optional') {
      this.cardinalityMin = 0;
      this.cardinalityMax = 1;
    } else {
      this.cardinalityMin = 1;
      this.cardinalityMax = 1;
    }
  }

  get defaultValue(): string {
    return this._defaultValue;
  }

  set defaultValue(value: string) {
    this._defaultValue = value;
    this._node.fireChangeEvent('defaultValue', value);
  }

  get fixedValue(): string {
    return this._fixedValue;
  }

  set fixedValue(value: string) {
    this._fixedValue = value;
    this._node.fireChangeEvent('fixedValue', value);
  }

  get facetMinLength(): number {
    return this._facetMinLength;
  }

  set facetMinLength(value: number) {
    this._facetMinLength = value;
    this._node.fireChangeEvent('facetMinLength', value);
  }

  get facetMaxLength(): number {
    return this._facetMaxLength;
  }

  set facetMaxLength(value: number) {
    this._facetMaxLength = value;
    this._node.fireChangeEvent('facetMaxLength', value);
  }

  get facetPattern(): string {
    return this._facetPattern;
  }

  set facetPattern(value: string) {
    this._facetPattern = value;
    this._node.fireChangeEvent('facetPattern', value);
  }

  get facetMinInclusive(): string {
    return this._facetMinInclusive;
  }

  set facetMinInclusive(value: string) {
    this._facetMinInclusive = value;
    this._node.fireChangeEvent('facetMinInclusive', value);
  }

  get facetMinExclusive(): string {
    return this._facetMinExclusive;
  }

  set facetMinExclusive(value: string) {
    this._facetMinExclusive = value;
    this._node.fireChangeEvent('facetMinExclusive', value);
  }

  get facetMaxInclusive(): string {
    return this._facetMaxInclusive;
  }

  set facetMaxInclusive(value: string) {
    this._facetMaxInclusive = value;
    this._node.fireChangeEvent('facetMaxInclusive', value);
  }

  get facetMaxExclusive(): string {
    return this._facetMaxExclusive;
  }

  set facetMaxExclusive(value: string) {
    this._facetMaxExclusive = value;
    this._node.fireChangeEvent('facetMaxExclusive', value);
  }

  get den(): string {
    return (this._node as DtScFlatNode).den;
  }

  set den(val: string) { // do nothing
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
    this._node.fireChangeEvent('definitionSource', value);
  }

  get hasTokenValueDomain(): boolean {
    return this.bdtScPriRestriList.filter(e => e.name === 'Token').length > 0;
  }
}

export class CcNodeUpdateResponse {
  type: string;
  manifestId: number;
  state: string;
  access: string;
}

export class CcCreateResponse {
  manifestId: number;
}

export class CcGraph {
  graph: {
    nodes: { [key: string]: CcGraphNode; };
    edges: { [key: string]: CcGraphEdge; };
  };
}

export class CcGraphNode {
  type: string;
  manifestId: number;
  state: string;
  deprecated: boolean;
  guid: string;
  objectClassTerm?: string;
  den?: string;
  propertyTerm?: string;
  dataTypeTerm?: string;
  representationTerm?: string;
  associationManifestId?: number;
  componentType?: string;
  entityType?: string;
  cardinalityMin?: number;
  cardinalityMax?: number;
  basedDtScId?: number;
  locked?: boolean;
  tagList?: ShortTag[];
}

export class CcGraphEdge {
  targets: string[];
}

export class CcGraphSearch {
  paths: string[];
  query: string;
}

export class CcRevisionResponse {
  type: string;
  ccId: number;
  isDeprecated: boolean;
  isNillable: boolean;
  isAbstract: boolean;
  hasBaseCc: boolean;
  name: string;
  fixedValue: string;
  isReusable: boolean;
  associations: Map<string, CcNode>[];
}

export class Comment {
  commentId: number;
  text: string;
  loginId: string;
  timestamp: number[];
  hidden: boolean;
  prevCommentId: number;
  isNew: boolean;
  isEditing: boolean;
  textTemp: string;

  constructor() {
    this.isEditing = false;
    this.isNew = false;
    this.textTemp = this.text;
  }
}

export class CcId {
  type: string;
  manifestId: number;

  constructor(type: string, manifestId: number) {
    this.type = type;
    this.manifestId = manifestId;
  }

  get id() {
    return this.type + '-' + this.manifestId;
  }
}

export class CcSeqUpdateRequest {
  item: CcId;
  after: CcId;
}

export class BodCreateResponse {
  manifestIdList: number[];
}
