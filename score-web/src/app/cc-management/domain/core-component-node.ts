import {ChangeListener} from '../../bie-management/domain/bie-flat-tree';
import {emptyToUndefined, hashCode, hashCode4String, toCamelCase,} from '../../common/utility';
import {DynamicCcDataSource} from '../tree-detail/tree-component';
import {AccFlatNode, AsccpFlatNode, BccpFlatNode, BdtScFlatNode, CcFlatNode} from './cc-flat-tree';

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
    this.namespaceId = obj.namespaceId;
    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

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
      definition: this.definition,
      definitionSource: this.definitionSource,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
    }
  }

  get hashCode(): number {
    return ((this.accId) ? this.accId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.type) ? hashCode4String(this.type) : 0) +
      ((this.objectClassTerm) ? hashCode4String(this.objectClassTerm) : 0) +
      ((this.oagisComponentType) ? this.oagisComponentType : 0) +
      ((this.abstracted) ? 1 : 0) +
      ((this.deprecated) ? 1 : 0) +
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

export const OagisComponentTypes: OagisComponentType[] = [
  Base, Semantics, Extension, SemanticGroup, UserExtensionGroup,
  Embedded, OAGIS10Nouns, OAGIS10BODs
];

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
    }
  }

  get hashCode(): number {
    return ((this.asccId) ? this.asccId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.deprecated) ? 1 : 0) +
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
    this._node.fireChangeEvent('cardinalityMin', value);
  }

  get cardinalityMax(): number {
    return this._cardinalityMax;
  }

  set cardinalityMax(value: number) {
    this._cardinalityMax = value;
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

    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;
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
      definition: this.definition,
      definitionSource: this.definitionSource,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum,
    }
  }

  get hashCode(): number {
    return ((this.asccpId) ? this.asccpId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.propertyTerm) ? hashCode4String(this.propertyTerm) : 0) +
      ((this.reusable) ? 1 : 0) +
      ((this.nillable) ? 1 : 0) +
      ((this.deprecated) ? 1 : 0) +
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
    return this.propertyTerm + '. ' + this.roleOfAccNode.accNode.objectClassTerm;
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
    }
  }

  get hashCode(): number {
    return ((this.bccId) ? this.bccId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.nillable) ? 1 : 0) +
      ((this.deprecated) ? 1 : 0) +
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
    this._node.fireChangeEvent('cardinalityMin', value);
  }

  get cardinalityMax(): number {
    return this._cardinalityMax;
  }

  set cardinalityMax(value: number) {
    this._cardinalityMax = value;
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

  constructor(node: BccpFlatNode, obj: any) {
    this._node = node;

    this.manifestId = obj.manifestId;
    this.bccpId = obj.bccpId;
    this.guid = obj.guid;
    this.propertyTerm = obj.propertyTerm;
    this.nillable = obj.nillable;
    this.deprecated = obj.deprecated;
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
  }

  get json(): any {
    return {
      bccpId: this.bccpId,
      manifestId: this.manifestId,
      guid: this.guid,
      propertyTerm: this.propertyTerm,
      nillable: this.nillable,
      deprecated: this.deprecated,
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
    }
  }

  get hashCode(): number {
    return ((this.bccpId) ? this.bccpId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.propertyTerm) ? hashCode4String(this.propertyTerm) : 0) +
      ((this.nillable) ? 1 : 0) +
      ((this.deprecated) ? 1 : 0) +
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
    return this.propertyTerm + '. ' + this._node.bdtNode.dataTypeTerm;
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
  qualifier: string;
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

  constructor(node: BccpFlatNode, obj: any) {
    this._node = node;

    this.manifestId = obj.manifestId;
    this.bdtId = obj.bdtId;
    this.guid = obj.guid;
    this.dataTypeTerm = obj.dataTypeTerm;
    this.qualifier = obj.qualifier;
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

export class CcBdtScNodeDetail extends CcNodeDetail {
  bdtScId: number;
  private _definition: string;
  private _definitionSource: string;
  defaultValue: string;
  fixedValue: string;
  fixedOrDefault: string;

  state: string;
  owner: string;
  releaseId: number;
  releaseNum: string;
  revisionId: number;
  revisionNum: number;
  revisionTrackingNum: number;

  constructor(node: BdtScFlatNode, obj: any) {
    super(node);

    this.bdtScId = obj.bdtScId;
    this.guid = obj.guid;
    this._definition = obj._definition;
    this._definitionSource = obj._definitionSource;
    this.defaultValue = obj.defaultValue;
    this.fixedValue = obj.fixedValue;
    this.fixedOrDefault = this.defaultValue ? 'default' : this.fixedValue ? 'fixed' : 'none';
    this.state = obj.state;
    this.owner = obj.owner;
    this.releaseId = obj.releaseId;
    this.releaseNum = obj.releaseNum;
    this.revisionId = obj.revisionId;
    this.revisionNum = obj.revisionNum;
    this.revisionTrackingNum = obj.revisionTrackingNum;

    this.reset();
  }

  get json(): any {
    return {
      bdtScId: this.bdtScId,
      manifestId: this.manifestId,
      guid: this.guid,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      definition: this.definition,
      definitionSource: this.definitionSource,
      state: this.state,
      owner: this.owner,
      releaseId: this.releaseId,
      releaseNum: this.releaseNum,
      revisionNum: this.revisionNum,
      revisionTrackingNum: this.revisionTrackingNum
    }
  }

  get hashCode(): number {
    return ((this.bdtScId) ? this.bdtScId : 0) +
      ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.definitionSource) ? hashCode4String(this.definitionSource) : 0) +
      ((!!this.state) ? hashCode4String(this.state) : 0) +
      ((!!this.owner) ? hashCode4String(this.owner) : 0) +
      ((this.releaseId) ? this.releaseId : 0) +
      ((!!this.releaseNum) ? hashCode4String(this.releaseNum) : 0) +
      ((this.revisionNum) ? this.revisionNum : 0) +
      ((this.revisionTrackingNum) ? this.revisionTrackingNum : 0);
  }

  get manifestId(): number {
    return (this._node as BdtScFlatNode).bdtScNode.manifestId;
  }

  set manifestId(value: number) {
    (this._node as BdtScFlatNode).bdtScNode.manifestId = value;
  }

  get guid(): string {
    return (this._node as BdtScFlatNode).bdtScNode.guid;
  }

  set guid(value: string) {
    (this._node as BdtScFlatNode).bdtScNode.guid = value;
  }

  get cardinalityMin(): number {
    return (this._node as BdtScFlatNode).bdtScNode.cardinalityMin;
  }

  set cardinalityMin(value: number) {
    (this._node as BdtScFlatNode).bdtScNode.cardinalityMin = value;
  }

  get cardinalityMax(): number {
    return (this._node as BdtScFlatNode).bdtScNode.cardinalityMax;
  }

  set cardinalityMax(value: number) {
    (this._node as BdtScFlatNode).bdtScNode.cardinalityMax = value;
  }

  get den(): string {
    return (this._node as BdtScFlatNode).den;
  }

  set den(val: string) { // do nothing
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    value = emptyToUndefined(value);
    this._definition = value;
  }

  get definitionSource(): string {
    return this._definitionSource;
  }

  set definitionSource(value: string) {
    value = emptyToUndefined(value);
    this._definitionSource = value;
  }
}

export class CcNodeUpdateResponse {
  type: string;
  manifestId: number;
  state: string;
  access: string;
}

/** Flat node with expandable and level information */
export class CcFlatNode2 {
  private $hashCode;
  children: CcFlatNode2[] = undefined;

  constructor(private dataSource: DynamicCcDataSource,
              public item: CcNode,
              public level = 0,
              public pos = 0,
              public isLoading = false,
              public isNullObject = false,
              public path = '') {
    if (!path) {
      this.path = this.id;
    }
    this.reset();
  }

  get id() {
    return this.item.type + '-' + this.item.manifestId;
  }

  get expandable() {
    if (this.children === undefined) {
      return this.dataSource.getChildren(this).length > 0;
    } else {
      return this.children.length > 0;
    }
  }

  get key() {
    return this.$hashCode;
  }

  get guid() {
    return this.item.guid;
  }

  get hashCode() {
    return hashCode(this.item);
  }

  isChanged() {
    return this.$hashCode !== this.hashCode;
  }

  reset() {
    this.$hashCode = hashCode(this.item);
  }
}

export class CcCreateResponse {
  manifestId: number;
}

export class CcGraph {
  accManifestId: number;
  asccpManifestId: number;
  graph: {
    nodes: { [key: string]: CcGraphNode; };
    edges: { [key: string]: CcGraphEdge; };
  };
}

export class CcGraphNode {
  type: string;
  manifestId: number;
  state: string;
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
  locked?: boolean;
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

export class CcId{
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

export class CcSeqUpdateRequest{
  item: CcId;
  after: CcId;
}
