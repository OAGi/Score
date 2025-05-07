import {AgencyIdList, AgencyIdListSummary} from '../../agency-id-list-management/domain/agency-id-list';
import {CodeListSummary} from '../../code-list-management/domain/code-list';
import {compare, emptyToUndefined, hashCode4Array,} from '../../common/utility';
import {AccFlatNode, AsccpFlatNode, BccpFlatNode, CcFlatNode, DtFlatNode, DtScFlatNode} from './cc-flat-tree';
import {ShortTag} from '../../tag-management/domain/tag';
import {LibrarySummary} from '../../library-management/domain/library';
import {ReleaseSummary} from '../../release-management/domain/release';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {LogSummary} from '../../log-management/domain/log';
import {ScoreUser} from '../../authentication/domain/auth';
import {Cardinality, Definition, ValueConstraint, WhoAndWhen} from '../../basis/basis';

export class AiGenerationResponse {
  generation: string;
}

export class AccDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  accManifestId: number;
  accId: number;
  guid: string;
  type: string;

  based: AccSummary;
  replacement: AccSummary;
  since: AccSummary;
  lastChanged: AccSummary;

  den: string;
  objectClassTerm: string;
  objectClassQualifier: string;
  componentType: string;
  definition: Definition;
  namespace: NamespaceSummary;
  isAbstract: boolean;
  isGroup: boolean;
  hasExtension: boolean;
  hasChild: boolean;
  deprecated: boolean;
  state: string;
  access: string;

  associations: (AsccSummary | BccSummary)[];

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevAccManifestId: number;
  nextAccManifestId: number;
}

export class AccSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  accManifestId: number;
  accId: number;
  guid: string;
  type: string;

  basedAccManifestId: number;

  den: string;
  objectClassTerm: string;
  objectClassQualifier: string;
  componentType: number;
  definition: Definition;
  namespaceId: number;
  isAbstract: boolean;
  deprecated: boolean;
  state: string;

  owner: ScoreUser;
}

export class AsccDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  asccManifestId: number;
  asccId: number;
  guid: string;

  fromAcc: AccSummary;
  toAsccp: AsccpSummary;
  seqKeyId: number;
  replacement: AsccSummary;
  since: AsccSummary;
  lastChanged: AsccSummary;

  den: string;
  cardinality: Cardinality;
  deprecated: boolean;
  state: string;
  definition: Definition;

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevAsccManifestId: number;
  nextAsccManifestId: number;
}

export class AsccSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  asccManifestId: number;
  asccId: number;
  guid: string;

  fromAccManifestId: number;
  toAsccpManifestId: number;
  seqKeyId: number;

  den: string;
  cardinality: Cardinality;
  deprecated: boolean;
  state: string;
  definition: Definition;

  owner: ScoreUser;

  prevAsccManifestId: number;
  nextAsccManifestId: number;
}

export class BccDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  bccManifestId: number;
  bccId: number;
  guid: string;

  fromAcc: AccSummary;
  toBccp: BccpSummary;
  seqKeyId: number;
  replacement: BccSummary;
  since: BccSummary;
  lastChanged: BccSummary;

  entityType: string;
  den: string;
  cardinality: Cardinality;
  deprecated: boolean;
  nillable: boolean;
  state: string;
  valueConstraint: ValueConstraint;
  definition: Definition;

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevBccManifestId: number;
  nextBccManifestId: number;
}

export class BccSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  bccManifestId: number;
  bccId: number;
  guid: string;

  fromAccManifestId: number;
  toBccpManifestId: number;
  seqKeyId: number;

  entityType: string;
  den: string;
  cardinality: Cardinality;
  deprecated: boolean;
  nillable: boolean;
  valueConstraint: ValueConstraint;
  definition: Definition;

  owner: ScoreUser;

  prevBccManifestId: number;
  nextBccManifestId: number;
}

export class AsccpDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  asccpManifestId: number;
  asccpId: number;
  guid: string;
  type: string;
  roleOfAcc: AccSummary;
  replacement: AsccpSummary;
  since: AsccpSummary;
  lastChanged: AsccpSummary;

  den: string;
  propertyTerm: string;
  reusable: boolean;
  deprecated: boolean;
  nillable: boolean;
  state: string;
  namespace: NamespaceSummary;
  definition: Definition;
  access: string;

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevAsccpManifestId: number;
  nextAsccpManifestId: number;
}

export class AsccpSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  asccpManifestId: number;
  asccpId: number;
  guid: string;
  type: string;
  roleOfAccManifestId: number;

  den: string;
  propertyTerm: string;
  namespaceId: number;
  reusable: boolean;
  deprecated: boolean;
  nillable: boolean;
  state: string;
  definition: Definition;

  owner: ScoreUser;
}

export class BccpDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  bccpManifestId: number;
  bccpId: number;
  guid: string;
  dt: DtSummary;
  replacement: BccpSummary;
  since: BccpSummary;
  lastChanged: BccpSummary;

  den: string;
  propertyTerm: string;
  representationTerm: string;
  namespace: NamespaceSummary;
  deprecated: boolean;
  nillable: boolean;
  state: string;
  valueConstraint: ValueConstraint;
  definition: Definition;
  access: string;

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevBccpManifestId: number;
  nextBccpManifestId: number;
}

export class BccpSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  bccpManifestId: number;
  bccpId: number;
  guid: string;
  dtManifestId: number;

  den: string;
  propertyTerm: string;
  representationTerm: string;
  namespaceId: number;
  deprecated: boolean;
  nillable: boolean;
  state: string;
  valueConstraint: ValueConstraint;
  definition: Definition;

  owner: ScoreUser;
}

export class DtDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  dtManifestId: number;
  dtId: number;
  guid: string;
  based: DtSummary;
  replacement: DtSummary;
  since: DtSummary;
  lastChanged: DtSummary;

  den: string;
  dataTypeTerm: string;
  qualifier: string;
  representationTerm: string;
  sixDigitId: string;
  commonlyUsed: boolean;
  hasChild: boolean;
  deprecated: boolean;
  state: string;
  namespace: NamespaceSummary;
  contentComponentDefinition: string;
  definition: Definition;
  dtAwdPriList: DtAwdPriDetails[];
  access: string;

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevDtManifestId: number;
  nextDtManifestId: number;
}

export class DtAwdPriDetails {
  dtAwdPriId: number;
  release: ReleaseSummary;
  dtId: number;
  xbt: XbtSummary;
  codeList: CodeListSummary;
  agencyIdList: AgencyIdListSummary;
  isDefault: boolean;
  inherited: boolean;
}

export class DtSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  dtManifestId: number;
  dtId: number;
  guid: string;
  basedDtManifestId: number;

  den: string;
  dataTypeTerm: string;
  qualifier: string;
  representationTerm: string;
  sixDigitId: string;
  commonlyUsed: boolean;
  deprecated: boolean;
  state: string;
  namespaceId: number;
  contentComponentDefinition: string;
  definition: Definition;
  dtAwdPriList: DtAwdPriSummary[];

  owner: ScoreUser;
}

export class DtAwdPriSummary {
  dtAwdPriId: number;
  releaseId: number;
  dtId: number;
  xbtManifestId: number;
  cdtPriName: string;
  xbtName: string;
  codeListManifestId: number;
  codeListName: string;
  agencyIdListManifestId: number;
  agencyIdListName: string;
  isDefault: boolean;
  inherited: boolean;
}

export class DtScDetails {
  library: LibrarySummary;
  release: ReleaseSummary;

  dtScManifestId: number;
  dtScId: number;
  guid: string;

  ownerDt: DtSummary;
  based: DtScSummary;
  replacement: DtScSummary;
  since: DtScSummary;
  lastChanged: DtScSummary;

  objectClassTerm: string;
  propertyTerm: string;
  representationTerm: string;
  cardinality: Cardinality;
  prevCardinality: Cardinality; // @TODO: consider eliminating for model simplification.
  deprecated: boolean;
  state: string;
  valueConstraint: ValueConstraint;
  definition: Definition;
  dtScAwdPriList: DtScAwdPriDetails[];

  log: LogSummary;

  owner: ScoreUser;
  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  prevDtScManifestId: number;
  nextDtScManifestId: number;
}

export class DtScAwdPriDetails {
  dtScAwdPriId: number;
  release: ReleaseSummary;
  dtScId: number;
  xbt: XbtSummary;
  codeList: CodeListSummary;
  agencyIdList: AgencyIdListSummary;
  isDefault: boolean;
  inherited: boolean;
}

export class DtScSummary {
  library: LibrarySummary;
  release: ReleaseSummary;

  dtScManifestId: number;
  dtScId: number;
  guid: string;

  ownerDtManifestId: number;
  basedDtScManifestId: number;

  objectClassTerm: string;
  propertyTerm: string;
  representationTerm: string;
  cardinality: Cardinality;
  deprecated: boolean;
  state: string;
  valueConstraint: ValueConstraint;
  definition: Definition;
  dtScAwdPriList: DtScAwdPriSummary[];

  owner: ScoreUser;
}

export class DtScAwdPriSummary {
  dtScAwdPriId: number;
  releaseId: number;
  dtScId: number;
  xbtManifestId: number;
  cdtPriName: string;
  xbtName: string;
  codeListManifestId: number;
  codeListName: string;
  agencyIdListManifestId: number;
  agencyIdListName: string;
  isDefault: boolean;
  inherited: boolean;
}

export class XbtSummary {
  xbtManifestId: number;
  xbtId: number;
  cdtPriName: string;
  name: string;
}

export abstract class CcNodeInfo {
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

export class CcAccNodeInfo extends CcNodeInfo {
  accId: number;
  manifestId: number;
  guid: string;
  private _objectClassTerm: string;
  private _group: boolean;
  private _definition: string;
  private _definitionSource: string;
  private _oagisComponentType: string;
  private _abstracted: boolean;
  private _deprecated: boolean;
  private _namespaceId: number;

  private _state: string;

  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: AccSummary;
  since: AccSummary;
  lastChanged: AccSummary;

  constructor(node: AccFlatNode, obj: AccDetails) {
    super(node);

    this.accId = obj.accId;
    this.manifestId = obj.accManifestId;
    this.guid = obj.guid;
    this.objectClassTerm = obj.objectClassTerm;
    this.group = obj.isGroup;
    this.definition = (!!obj.definition) ? obj.definition.content : undefined;
    this.definitionSource = (!!obj.definition) ? obj.definition.source : undefined;
    this.oagisComponentType = obj.componentType;
    this.abstracted = obj.isAbstract;
    this.deprecated = obj.deprecated;
    this.replacement = obj.replacement;
    this.namespaceId = (!!obj.namespace) ? obj.namespace.namespaceId : undefined;
    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;

    this.reset();
  }

  get json(): any {
    return {
      accManifestId: this.manifestId,
      accId: this.accId,
      guid: this.guid,
      type: this.type,
      objectClassTerm: this.objectClassTerm,
      componentType: this.oagisComponentType,
      definition: this.definition,
      definitionSource: this.definitionSource,
      isAbstract: this.abstracted,
      deprecated: this.deprecated,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner.loginId
    };
  }

  get hashCode(): number {
    return hashCode4Array(
      this.accId, this.manifestId, this.guid, this.type,
      this.objectClassTerm, this.oagisComponentType,
      this.abstracted, this.deprecated,
      this.namespaceId,
      this.definition, this.definitionSource,
      this.owner.loginId
    );
  }

  get objectClassTerm(): string {
    return this._objectClassTerm;
  }

  set objectClassTerm(value: string) {
    value = emptyToUndefined(value);
    this._objectClassTerm = value;
    this._node.fireChangeEvent('objectClassTerm', value);
  }

  get den(): string {
    return ((this.objectClassTerm) ? this.objectClassTerm : '') + '. Details';
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

  get oagisComponentType(): string {
    return this._oagisComponentType;
  }

  set oagisComponentType(value: string) {
    this._oagisComponentType = value;
    if (this._oagisComponentType === 'Base') {
      this.abstracted = true;
    } else if (this._oagisComponentType === 'Extension' || this._oagisComponentType === 'SemanticGroup') {
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
  constructor(public value: string, public numericValue: number, public name: string) {
  }
}

export const Base: OagisComponentType = new OagisComponentType('Base', 0, 'Base (Abstract)');
export const Semantics: OagisComponentType = new OagisComponentType('Semantics', 1, 'Semantics');
export const Extension: OagisComponentType = new OagisComponentType('Extension', 2, 'Extension');
export const SemanticGroup: OagisComponentType = new OagisComponentType('SemanticGroup', 3, 'Semantic Group');
export const UserExtensionGroup: OagisComponentType = new OagisComponentType('UserExtensionGroup', 4, 'User Extension Group');
export const Embedded: OagisComponentType = new OagisComponentType('Embedded', 5, 'Embedded');
export const OAGIS10Nouns: OagisComponentType = new OagisComponentType('OAGIS10Nouns', 6, 'OAGIS10 Nouns');
export const OAGIS10BODs: OagisComponentType = new OagisComponentType('OAGIS10BODs', 7, 'OAGIS10 BODs');
export const BOD: OagisComponentType = new OagisComponentType('BOD', 8, 'BOD');
export const Verb: OagisComponentType = new OagisComponentType('Verb', 9, 'Verb');
export const Noun: OagisComponentType = new OagisComponentType('Noun', 10, 'Noun');
export const Choice: OagisComponentType = new OagisComponentType('Choice', 11, 'Choice');
export const AttributeGroup: OagisComponentType = new OagisComponentType('AttributeGroup', 12, 'Attribute Group');

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

class AsccNodeInfo {
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
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: AsccSummary;
  since: AsccSummary;
  lastChanged: AsccSummary;

  constructor(node: CcFlatNode, obj: AsccDetails) {
    this._node = node;

    this.manifestId = obj.asccManifestId;
    this.asccId = obj.asccId;
    this.guid = obj.guid;
    this.den = obj.den;
    this.cardinalityMin = obj.cardinality.min;
    this.cardinalityMax = obj.cardinality.max;
    this.deprecated = obj.deprecated;
    this.definition = (!!obj.definition) ? obj.definition.content : undefined;
    this.definitionSource = (!!obj.definition) ? obj.definition.source : undefined;

    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;
  }

  get json(): any {
    return {
      asccManifestId: this.manifestId,
      asccId: this.asccId,
      guid: this.guid,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      deprecated: this.deprecated,
      definition: this.definition,
      definitionSource: this.definitionSource,
      state: this.state,
      owner: this.owner.loginId,
    };
  }

  get hashCode(): number {
    return hashCode4Array(
      this.asccId, this.manifestId, this.guid,
      this.cardinalityMin, this.cardinalityMax,
      this.deprecated,
      this.definition, this.definitionSource,
      this.owner.loginId
    );
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

class AsccpNodeInfo {
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
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: AsccpSummary;
  since: AsccpSummary;
  lastChanged: AsccpSummary;

  _den: string;

  constructor(node: CcFlatNode, obj: AsccpDetails) {
    this._node = node;
    this.manifestId = obj.asccpManifestId;
    this.asccpId = obj.asccpId;
    this.guid = obj.guid;
    this.propertyTerm = obj.propertyTerm;
    this.reusable = obj.reusable;
    this.nillable = obj.nillable;
    this.deprecated = obj.deprecated;
    this.namespaceId = (!!obj.namespace) ? obj.namespace.namespaceId : undefined;
    this.definition = (!!obj.definition) ? obj.definition.content : undefined;
    this.definitionSource = (!!obj.definition) ? obj.definition.source : undefined;
    this._den = obj.den;

    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;
  }

  get json(): any {
    return {
      asccpManifestId: this.manifestId,
      asccpId: this.asccpId,
      guid: this.guid,
      propertyTerm: this.propertyTerm,
      reusable: this.reusable,
      nillable: this.nillable,
      deprecated: this.deprecated,
      definition: this.definition,
      definitionSource: this.definitionSource,
      namespaceId: this.namespaceId,
      state: this.state,
      owner: this.owner.loginId,
    };
  }

  get hashCode(): number {
    return hashCode4Array(
      this.asccpId, this.manifestId, this.guid,
      this.propertyTerm,
      this.reusable, this.nillable, this.deprecated,
      this.definition, this.definitionSource,
      this.namespaceId,
      this.owner.loginId
    );
  }

  get propertyTerm(): string {
    return this._propertyTerm;
  }

  set propertyTerm(value: string) {
    value = emptyToUndefined(value);
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

export class CcAsccpNodeInfo extends CcNodeInfo {
  ascc: AsccNodeInfo;
  asccp: AsccpNodeInfo;

  constructor(node: AsccpFlatNode, ascc: AsccDetails, asccp: AsccpDetails) {
    super(node);
    if (ascc) {
      this.ascc = new AsccNodeInfo(node, ascc);
    }
    if (asccp) {
      this.asccp = new AsccpNodeInfo(node, asccp);
    }

    this.reset();
  }

  get hashCode(): number {
    return hashCode4Array(this.asccp, this.ascc);
  }
}

export class EntityType {
  constructor(public value: string, public numericValue: number, public name: string) {
  }
}

export const Attribute: EntityType = new EntityType('Attribute', 0, 'Attribute');
export const Element: EntityType = new EntityType('Element', 1, 'Element');

export const EntityTypes: EntityType[] = [
  Attribute, Element
];

class BccNodeInfo {
  private _node: CcFlatNode;
  manifestId: number;
  bccId: number;
  guid: string;
  den: string;
  private _entityType: string;
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
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: BccSummary;
  since: BccSummary;
  lastChanged: BccSummary;

  constructor(node: BccpFlatNode, obj: BccDetails) {
    this._node = node;

    this.manifestId = obj.bccManifestId;
    this.bccId = obj.bccId;
    this.guid = obj.guid;
    this.den = obj.den;
    this.entityType = obj.entityType;
    this.cardinalityMin = obj.cardinality.min;
    this.cardinalityMax = obj.cardinality.max;
    this.deprecated = obj.deprecated;
    this.nillable = obj.nillable;
    this.defaultValue = (!!obj.valueConstraint) ? obj.valueConstraint.defaultValue : undefined;
    this.fixedValue = (!!obj.valueConstraint) ? obj.valueConstraint.fixedValue : undefined;
    this.fixedOrDefault = this.fixedValue ? 'fixed' : this.defaultValue ? 'default' : 'none';
    this.definition = (!!obj.definition) ? obj.definition.content : undefined;
    this.definitionSource = (!!obj.definition) ? obj.definition.source : undefined;

    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;
  }

  get json(): any {
    return {
      bccManifestId: this.manifestId,
      bccId: this.bccId,
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
      owner: this.owner.loginId
    };
  }

  get hashCode(): number {
    return hashCode4Array(
      this.bccId, this.manifestId, this.guid,
      this.cardinalityMin, this.cardinalityMax,
      this.nillable, this.deprecated, this.entityType,
      this.defaultValue, this.fixedValue,
      this.definition, this.definitionSource,
      this.owner.loginId
    );
  }

  get entityType(): string {
    return this._entityType;
  }

  set entityType(value: string) {
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

class BccpNodeInfo {
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
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: BccpSummary;
  since: BccpSummary;
  lastChanged: BccpSummary;

  constructor(node: BccpFlatNode, obj: BccpDetails) {
    this._node = node;

    this.manifestId = obj.bccpManifestId;
    this.bccpId = obj.bccpId;
    this.guid = obj.guid;
    this.propertyTerm = obj.propertyTerm;
    this.nillable = obj.nillable;
    this.deprecated = obj.deprecated;
    this.namespaceId = (!!obj.namespace) ? obj.namespace.namespaceId : undefined;
    this.replacement = obj.replacement;
    this.defaultValue = (!!obj.valueConstraint) ? obj.valueConstraint.defaultValue : undefined;
    this.fixedValue = (!!obj.valueConstraint) ? obj.valueConstraint.fixedValue : undefined;
    this.fixedOrDefault = this.fixedValue ? 'fixed' : this.defaultValue ? 'default' : 'none';
    this.definition = (!!obj.definition) ? obj.definition.content : undefined;
    this.definitionSource = (!!obj.definition) ? obj.definition.source : undefined;

    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;
  }

  get json(): any {
    return {
      bccpManifestId: this.manifestId,
      bccpId: this.bccpId,
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
      owner: this.owner.loginId
    };
  }

  get hashCode(): number {
    return hashCode4Array(
      this.bccpId, this.manifestId, this.guid,
      this.propertyTerm,
      this.nillable, this.deprecated,
      this.defaultValue, this.fixedValue,
      this.definition, this.definitionSource,
      this.namespaceId,
      this.owner.loginId
    );
  }

  get propertyTerm(): string {
    return this._propertyTerm;
  }

  set propertyTerm(value: string) {
    value = emptyToUndefined(value);
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

class DtNodeInfo {
  private _node: BccpFlatNode;

  manifestId: number;
  bdtId: number;
  guid: string;
  dataTypeTerm: string;
  representationTerm: string;
  qualifier: string;
  namespaceId: number;
  den: string;
  definition: string;
  definitionSource: string;
  hasNoSc: boolean;

  state: string;
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: DtSummary;
  since: DtSummary;
  lastChanged: DtSummary;

  bdtPriRestriList: CcBdtPriRestri[];

  constructor(node: BccpFlatNode, obj: DtDetails) {
    this._node = node;

    this.manifestId = obj.dtManifestId;
    this.bdtId = obj.dtId;
    this.guid = obj.guid;
    this.dataTypeTerm = obj.dataTypeTerm;
    this.representationTerm = obj.representationTerm;
    this.qualifier = obj.qualifier;
    this.namespaceId = (!!obj.namespace) ? obj.namespace.namespaceId : undefined;
    this.replacement = obj.replacement;
    this.den = obj.den;
    this.definition = (!!obj.definition) ? obj.definition.content : undefined;
    this.definitionSource = (!!obj.definition) ? obj.definition.source : undefined;
    this.hasNoSc = !obj.hasChild;

    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;

    this.bdtPriRestriList = [];//obj.bdtPriRestriList;
  }
}

export class CcBccpNodeInfo extends CcNodeInfo {
  bcc: BccNodeInfo;
  bccp: BccpNodeInfo;
  bdt: DtNodeInfo;

  constructor(node: BccpFlatNode, bcc: BccDetails, bccp: BccpDetails, dt: DtDetails) {
    super(node);
    if (bcc) {
      this.bcc = new BccNodeInfo(node, bcc);
    }
    if (bccp) {
      this.bccp = new BccpNodeInfo(node, bccp);
    }
    if (dt) {
      this.bdt = new DtNodeInfo(node, dt);
    }
    this.reset();
  }

  get hashCode(): number {
    return hashCode4Array(this.bccp, this.bcc);
  }
}

export class CcBdtPriRestri {
  parent: CcDtNodeInfo | CcDtScNodeInfo;
  bdtPriRestriId: number;
  bdtScPriRestriId: number;
  type: string;
  cdtAwdPriId: number;
  cdtAwdPriXpsTypeMapId: number;
  cdtScAwdPriId: number;
  cdtScAwdPriXpsTypeMapId: number;
  primitiveName: string;
  xbtManifestId: number;
  xbtId: number;
  xbtName: string;
  codeListManifestId: number;
  codeListName: string;
  agencyIdListManifestId: number;
  agencyIdListName: string;
  _default: boolean;
  isSc: boolean;
  inherited: boolean;

  _selectedCodeList: CodeListSummary;
  get selectedCodeList(): CodeListSummary {
    return this._selectedCodeList;
  }

  set selectedCodeList(val: CodeListSummary) {
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
      this.codeListName = val.name;
    } else {
      this.codeListManifestId = undefined;
      this.codeListName = undefined;
    }
  }

  _selectedAgencyIdList: AgencyIdListSummary;
  get selectedAgencyIdList(): AgencyIdListSummary {
    return this._selectedAgencyIdList;
  }

  set selectedAgencyIdList(val: AgencyIdListSummary) {
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

  constructor(detail: CcDtNodeInfo | CcDtScNodeInfo, obj: CcBdtPriRestri, dtPrimitiveAware: DtPrimitiveAware) {
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
    this.xbtManifestId = obj.xbtManifestId;
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
        codeListName: this.selectedCodeList.name
      } : undefined, (this.selectedAgencyIdList) ? {
        agencyIdListManifestId: this.selectedAgencyIdList.agencyIdListManifestId,
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
      xbtManifestId: this.xbtManifestId,
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
  codeLists: CodeListSummary[];
  agencyIdLists: AgencyIdListSummary[];
  xbtList: XbtSummary[];
}

export interface ValueDomainEntity {
  type: string;
  id: number;
  name: string;
  default: boolean;
}

export class CcDtNodeInfo extends CcNodeInfo {
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
  private _sixDigitId: string;
  private _contentComponentDefinition: string;
  commonlyUsed: boolean;
  private _namespaceId: number;
  state: string;
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: DtSummary;
  since: DtSummary;
  lastChanged: DtSummary;

  dtAwdPriList: DtAwdPriDetails[];
  dtAwdPriListByGroup: any[];

  // To display value domains on UI
  primitiveMap: Map<string, ValueDomainEntity[]>;
  codeListList: ValueDomainEntity[];
  agencyIdListList: ValueDomainEntity[];

  constructor(node: DtFlatNode, obj: DtDetails) {
    super(node);

    this.bdtId = obj.dtId;
    this.guid = obj.guid;
    this._definition = (!!obj.definition) ? obj.definition.content : undefined;
    this._definitionSource = (!!obj.definition) ? obj.definition.source : undefined;
    this._representationTerm = obj.representationTerm;
    this._dataTypeTerm = obj.dataTypeTerm;
    this._qualifier = obj.qualifier;
    this.basedBdtId = (obj.based) ? obj.based.dtId : undefined;
    this.basedBdtManifestId = (obj.based) ? obj.based.dtManifestId : undefined;
    this.basedBdtDen = (obj.based) ? obj.based.den : undefined;
    this.basedBdtState = (obj.based) ? obj.based.state : undefined;
    this._sixDigitId = obj.sixDigitId;
    this._contentComponentDefinition = obj.contentComponentDefinition;
    this.commonlyUsed = obj.commonlyUsed;
    this._namespaceId = (obj.namespace) ? obj.namespace.namespaceId : undefined;
    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;

    this.dtAwdPriList = obj.dtAwdPriList;
    this.dtAwdPriListByGroup = [];
    this.primitiveMap = new Map<string, ValueDomainEntity[]>();
    this.codeListList = [];
    this.agencyIdListList = [];

    this.reset();
  }

  update(dtPrimitiveAware: DtPrimitiveAware) {
    // if (this.dtAwdPriList.length > 0) {
    //   if (this.dtAwdPriList[0] instanceof CcBdtPriRestri) {
    //     return;
    //   }
    // }
    // this.dtAwdPriList = this.dtAwdPriList.map(row => new CcBdtPriRestri(this, row, dtPrimitiveAware));
    this.updateValueDomainGroup();
    this.reset();
  }

  updateValueDomainGroup() {
    this.primitiveMap = new Map<string, ValueDomainEntity[]>();
    const primitiveList = [];
    this.codeListList = [];
    this.agencyIdListList = [];

    const dtAwdPriList = this.dtAwdPriList;
    for (const dtAwdPri of dtAwdPriList) {
      if (!!dtAwdPri.xbt) {
        if (!this.primitiveMap.has(dtAwdPri.xbt.cdtPriName)) {
          this.primitiveMap.set(dtAwdPri.xbt.cdtPriName, []);
        }
        const domainEntity = new class implements ValueDomainEntity {
          get self(): DtAwdPriDetails {
            return dtAwdPri;
          }

          get type(): string {
            return 'Primitive';
          }

          get id(): number {
            return dtAwdPri.xbt.xbtManifestId;
          }

          set id(id: number) {
            dtAwdPri.xbt.xbtManifestId = id;
          }

          get name(): string {
            return dtAwdPri.xbt.name;
          }

          set name(name: string) {
            dtAwdPri.xbt.name = name;
          }

          get default(): boolean {
            return dtAwdPri.isDefault;
          }

          set default(isDefault: boolean) {
            dtAwdPri.isDefault = isDefault;
          }
        };
        primitiveList.push(domainEntity);
        this.primitiveMap.get(dtAwdPri.xbt.cdtPriName).push(domainEntity);
      }
      if (!!dtAwdPri.codeList) {
        this.codeListList.push(new class implements ValueDomainEntity {
          get self(): DtAwdPriDetails {
            return dtAwdPri;
          }

          get type(): string {
            return 'Code List';
          }

          get id(): number {
            return dtAwdPri.codeList.codeListManifestId;
          }

          set id(id: number) {
            dtAwdPri.codeList.codeListManifestId = id;
          }

          get name(): string {
            return dtAwdPri.codeList.name;
          }

          set name(name: string) {
            dtAwdPri.codeList.name = name;
          }

          get default(): boolean {
            return dtAwdPri.isDefault;
          }

          set default(isDefault: boolean) {
            dtAwdPri.isDefault = isDefault;
          }
        });
      }
      if (!!dtAwdPri.agencyIdList) {
        this.agencyIdListList.push(new class implements ValueDomainEntity {
          get self(): DtAwdPriDetails {
            return dtAwdPri;
          }

          get type(): string {
            return 'Agency ID List';
          }

          get id(): number {
            return dtAwdPri.agencyIdList.agencyIdListManifestId;
          }

          set id(id: number) {
            dtAwdPri.agencyIdList.agencyIdListManifestId = id;
          }

          get name(): string {
            return dtAwdPri.agencyIdList.name;
          }

          set name(name: string) {
            dtAwdPri.agencyIdList.name = name;
          }

          get default(): boolean {
            return dtAwdPri.isDefault;
          }

          set default(isDefault: boolean) {
            dtAwdPri.isDefault = isDefault;
          }
        });
      }
    }

    this.dtAwdPriListByGroup = [];
    if (primitiveList.length > 0) {
      this.dtAwdPriListByGroup.push({
        label: 'Primitive',
        list: primitiveList.sort((a, b) => {
          const aName = a.self.xbt.cdtPriName + ' - ' + a.name;
          const bName = b.self.xbt.cdtPriName + ' - ' + b.name;
          return compare(aName, bName);
        })
      });
    }
    if (this.codeListList.length > 0) {
      this.dtAwdPriListByGroup.push({
        label: 'Code List',
        list: this.codeListList
      });
    }
    if (this.agencyIdListList.length > 0) {
      this.dtAwdPriListByGroup.push({
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
        xbtList: this.primitiveMap.get(primitiveName)
      });
    }
    valueDomains = valueDomains.sort((a, b) => compare(a.name, b.name));

    const userCodeLists = [];
    for (const codeList of this.codeListList.sort((a, b) => compare(a.name, b.name))) {
      const dtAwdPri = this.dtAwdPriList.filter(e => !!e.codeList && e.codeList.name === codeList.name)[0];
      if (!dtAwdPri.inherited) {
        userCodeLists.push({
          type: 'Code List',
          name: dtAwdPri.codeList.name,
          dtAwdPri
        });
      } else {
        valueDomains.push({
          type: 'Code List',
          name: dtAwdPri.codeList.name,
          dtAwdPri
        });
      }
    }
    valueDomains.push(...userCodeLists);

    const userAgencyIdLists = [];
    for (const agencyIdList of this.agencyIdListList.sort((a, b) => compare(a.name, b.name))) {
      const dtAwdPri = this.dtAwdPriList.filter(e => !!e.agencyIdList && e.agencyIdList.name === agencyIdList.name)[0];
      if (!dtAwdPri.inherited) {
        userAgencyIdLists.push({
          type: 'Agency ID List',
          name: dtAwdPri.agencyIdList.name,
          dtAwdPri
        });
      } else {
        valueDomains.push({
          type: 'Agency ID List',
          name: dtAwdPri.agencyIdList.name,
          dtAwdPri
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
      dtManifestId: this.manifestId,
      bdtId: this.bdtId,
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
      sixDigitId: this._sixDigitId,
      contentComponentDefinition: this.contentComponentDefinition,
      commonlyUsed: this.commonlyUsed,
      namespaceId: this._namespaceId,
      state: this.state,
      owner: this.owner.loginId,
      dtAwdPriList: this.dtAwdPriList.map(e => {
        return {
          dtAwdPriId: e.dtAwdPriId,
          xbtManifestId: (!!e.xbt) ? e.xbt.xbtManifestId : undefined,
          codeListManifestId: (!!e.codeList) ? e.codeList.codeListManifestId : undefined,
          agencyIdListManifestId: (!!e.agencyIdList) ? e.agencyIdList.agencyIdListManifestId : undefined,
          isDefault: e.isDefault
        };
      }),
      defaultValueDomain: this.defaultValueDomain
    };
  }

  get hashCode(): number {
    return hashCode4Array(this.bdtId, this.manifestId, this.guid, this.representationTerm, this.dataTypeTerm, this.qualifier,
        this.basedBdtId, this.basedBdtManifestId, this.basedBdtDen, this.basedBdtState,
        this._sixDigitId, this.contentComponentDefinition, this.commonlyUsed,
        this._namespaceId, this.definition, this.definitionSource, this.dtAwdPriList);
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

export class CcDtScNodeInfo extends CcNodeInfo {
  bdtScId: number;
  private _definition: string;
  private _definitionSource: string;
  private _defaultValue: string;
  private _fixedValue: string;
  private _fixedOrDefault: string;

  state: string;
  owner: ScoreUser;

  library: LibrarySummary;
  release: ReleaseSummary;
  log: LogSummary;

  replacement: DtScSummary;
  since: DtScSummary;
  lastChanged: DtScSummary;

  private _propertyTerm: string;
  private _representationTerm: string;
  objectClassTerm: string;
  based: DtScSummary;

  prevCardinalityMin: number;
  prevCardinalityMax: number;
  baseCardinalityMin: number;
  baseCardinalityMax: number;

  dtScAwdPriList: DtScAwdPriDetails[];
  dtScAwdPriListByGroup: any[];
  isCardinalityEditable: boolean;
  allowedCardinalities: any[];

  // To display value domains on UI
  primitiveMap: Map<string, ValueDomainEntity[]>;
  codeListList: ValueDomainEntity[];
  agencyIdListList: ValueDomainEntity[];

  constructor(node: DtScFlatNode, obj: DtScDetails) {
    super(node);

    this.bdtScId = obj.dtScId;
    this.guid = obj.guid;
    this._definition = (!!obj.definition) ? obj.definition.content : undefined;
    this._definitionSource = (!!obj.definition) ? obj.definition.source : undefined;
    this._defaultValue = (!!obj.valueConstraint) ? obj.valueConstraint.defaultValue : undefined;
    this._fixedValue = (!!obj.valueConstraint) ? obj.valueConstraint.fixedValue : undefined;
    this.cardinalityMax = obj.cardinality.max;
    this.cardinalityMin = obj.cardinality.min;
    if (obj.prevCardinality) {
      this.prevCardinalityMin = obj.prevCardinality.min;
      this.prevCardinalityMax = obj.prevCardinality.max;
    }
    if (obj.based) {
      this.baseCardinalityMin = obj.based.cardinality.min;
      this.baseCardinalityMax = obj.based.cardinality.max;
    }

    this.isCardinalityEditable = true;

    if (!this.prevCardinalityMin && !this.baseCardinalityMin) {
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
    this._propertyTerm = obj.propertyTerm;
    this._representationTerm = obj.representationTerm;
    this.objectClassTerm = obj.objectClassTerm;
    this.based = obj.based;
    this.state = obj.state;
    this.owner = obj.owner;

    this.library = obj.library;
    this.release = obj.release;
    this.log = obj.log;
    this.since = obj.since;
    this.lastChanged = obj.lastChanged;

    this.dtScAwdPriList = obj.dtScAwdPriList;
    this.dtScAwdPriListByGroup = [];

    this.reset();
  }

  update(dtPrimitiveAware: DtPrimitiveAware) {
    // if (this.dtScAwdPriList.length > 0) {
    //   if (this.dtScAwdPriList[0] instanceof DtScAwdPriDetails) {
    //     return;
    //   }
    // }
    // this.dtScAwdPriList = this.dtScAwdPriList.map(row => new CcBdtPriRestri(this, row, dtPrimitiveAware));
    this.updateValueDomainGroup();
    this.reset();
  }

  updateValueDomainGroup() {
    this.primitiveMap = new Map<string, ValueDomainEntity[]>();
    const primitiveList = [];
    this.codeListList = [];
    this.agencyIdListList = [];

    const dtScAwdPriList = this.dtScAwdPriList;
    for (const dtScAwdPri of dtScAwdPriList) {
      if (!!dtScAwdPri.xbt) {
        if (!this.primitiveMap.has(dtScAwdPri.xbt.cdtPriName)) {
          this.primitiveMap.set(dtScAwdPri.xbt.cdtPriName, []);
        }
        const domainEntity = new class implements ValueDomainEntity {
          get self(): DtScAwdPriDetails {
            return dtScAwdPri;
          }

          get type(): string {
            return 'Primitive';
          }

          get id(): number {
            return dtScAwdPri.xbt.xbtManifestId;
          }

          set id(id: number) {
            dtScAwdPri.xbt.xbtManifestId = id;
          }

          get name(): string {
            return dtScAwdPri.xbt.name;
          }

          set name(name: string) {
            dtScAwdPri.xbt.name = name;
          }

          get default(): boolean {
            return dtScAwdPri.isDefault;
          }

          set default(isDefault: boolean) {
            dtScAwdPri.isDefault = isDefault;
          }
        };
        primitiveList.push(domainEntity);
        this.primitiveMap.get(dtScAwdPri.xbt.cdtPriName).push(domainEntity);
      }
      if (!!dtScAwdPri.codeList) {
        this.codeListList.push(new class implements ValueDomainEntity {
          get self(): DtScAwdPriDetails {
            return dtScAwdPri;
          }

          get type(): string {
            return 'Code List';
          }

          get id(): number {
            return dtScAwdPri.codeList.codeListManifestId;
          }

          set id(id: number) {
            dtScAwdPri.codeList.codeListManifestId = id;
          }

          get name(): string {
            return dtScAwdPri.codeList.name;
          }

          set name(name: string) {
            dtScAwdPri.codeList.name = name;
          }

          get default(): boolean {
            return dtScAwdPri.isDefault;
          }

          set default(isDefault: boolean) {
            dtScAwdPri.isDefault = isDefault;
          }
        });
      }
      if (!!dtScAwdPri.agencyIdList) {
        this.agencyIdListList.push(new class implements ValueDomainEntity {
          get self(): DtScAwdPriDetails {
            return dtScAwdPri;
          }

          get type(): string {
            return 'Agency ID List';
          }

          get id(): number {
            return dtScAwdPri.agencyIdList.agencyIdListManifestId;
          }

          set id(id: number) {
            dtScAwdPri.agencyIdList.agencyIdListManifestId = id;
          }

          get name(): string {
            return dtScAwdPri.agencyIdList.name;
          }

          set name(name: string) {
            dtScAwdPri.agencyIdList.name = name;
          }

          get default(): boolean {
            return dtScAwdPri.isDefault;
          }

          set default(isDefault: boolean) {
            dtScAwdPri.isDefault = isDefault;
          }
        });
      }
    }

    this.dtScAwdPriListByGroup = [];
    if (primitiveList.length > 0) {
      this.dtScAwdPriListByGroup.push({
        label: 'Primitive',
        list: primitiveList.sort((a, b) => {
          const aName = a.self.xbt.cdtPriName + ' - ' + a.name;
          const bName = b.self.xbt.cdtPriName + ' - ' + b.name;
          return compare(aName, bName);
        })
      });
    }
    if (this.codeListList.length > 0) {
      this.dtScAwdPriListByGroup.push({
        label: 'Code List',
        list: this.codeListList
      });
    }
    if (this.agencyIdListList.length > 0) {
      this.dtScAwdPriListByGroup.push({
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
        xbtList: this.primitiveMap.get(primitiveName)
      });
    }
    valueDomains = valueDomains.sort((a, b) => compare(a.name, b.name));

    const userCodeLists = [];
    for (const codeList of this.codeListList.sort((a, b) => compare(a.name, b.name))) {
      const dtScAwdPri = this.dtScAwdPriList.filter(e => !!e.codeList && e.codeList.name === codeList.name)[0];
      if (!dtScAwdPri.inherited) {
        userCodeLists.push({
          type: 'Code List',
          name: codeList.name,
          dtScAwdPri
        });
      } else {
        valueDomains.push({
          type: 'Code List',
          name: codeList.name,
          dtScAwdPri
        });
      }
    }
    valueDomains.push(...userCodeLists);

    const userAgencyIdLists = [];
    for (const agencyIdList of this.agencyIdListList.sort((a, b) => compare(a.name, b.name))) {
      const dtScAwdPri = this.dtScAwdPriList.filter(e => !!e.agencyIdList && e.agencyIdList.name === agencyIdList.name)[0];
      if (!dtScAwdPri.inherited) {
        userAgencyIdLists.push({
          type: 'Agency ID List',
          name: agencyIdList.name,
          dtScAwdPri
        });
      } else {
        valueDomains.push({
          type: 'Agency ID List',
          name: agencyIdList.name,
          dtScAwdPri
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
    value = emptyToUndefined(value);
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
      dtScManifestId: this.manifestId,
      bdtScId: this.bdtScId,
      guid: this.guid,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      cardinalityMax: this.cardinalityMax,
      cardinalityMin: this.cardinalityMin,
      definition: this.definition,
      definitionSource: this.definitionSource,
      state: this.state,
      owner: this.owner.loginId,
      dtScAwdPriList: this.dtScAwdPriList.map(e => {
        return {
          dtScAwdPriId: e.dtScAwdPriId,
          xbtManifestId: (!!e.xbt) ? e.xbt.xbtManifestId : undefined,
          codeListManifestId: (!!e.codeList) ? e.codeList.codeListManifestId : undefined,
          agencyIdListManifestId: (!!e.agencyIdList) ? e.agencyIdList.agencyIdListManifestId : undefined,
          isDefault: e.isDefault
        };
      }),
      propertyTerm: this.propertyTerm,
      representationTerm: this.representationTerm,
      objectClassTerm: this.objectClassTerm,
    };
  }

  get hashCode(): number {
    return hashCode4Array(this.bdtScId, this.manifestId, this.guid, this.representationTerm, this.objectClassTerm, this.propertyTerm,
        this.representationTerm, this.cardinalityMin, this.cardinalityMax, this.defaultValue, this.fixedValue,
        this.definition, this.definitionSource, this.state, this.dtScAwdPriList);
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

  get fixedOrDefault(): string {
    return this._fixedOrDefault;
  }

  set fixedOrDefault(value: string) {
    this._fixedOrDefault = value;
    if (value === 'fixed') {
      this.defaultValue = null;
    } else if (value === 'default') {
      this.fixedValue = null;
    } else {
      this.fixedValue = null;
      this.defaultValue = null;
    }
    this._node.fireChangeEvent('fixedOrDefault', value);
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
    return this.dtScAwdPriList.filter(e => !!e.xbt && e.xbt.name === 'token').length > 0;
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
  basedDtScManifestId?: number;
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

export class CommentRecord {
  commentId: number;
  text: string;

  hidden: boolean;
  prevCommentId: number;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;
}

export class Comment {
  commentId: number;
  text: string;
  timestamp: Date;
  hidden: boolean;
  prevCommentId: number;
  isNew: boolean;
  isEditing: boolean;
  textTemp: string;

  created: WhoAndWhen;
  lastUpdated: WhoAndWhen;

  constructor() {
    this.isEditing = false;
    this.isNew = false;
    this.textTemp = this.text;
  }
}

export class AsccpOrBccpManifestId {
  asccpManifestId: number;
  bccpManifestId: number;

  constructor(asccpManifestId: number, bccpManifestId: number) {
    this.asccpManifestId = asccpManifestId;
    this.bccpManifestId = bccpManifestId;
  }

  get id() {
    return (this.asccpManifestId) ? ('ASCCP-' + this.asccpManifestId) : ('BCCP-' + this.bccpManifestId);
  }
}

export class CcSeqUpdateRequest {
  item: AsccpOrBccpManifestId;
  after: AsccpOrBccpManifestId;
}

export class BodCreateResponse {
  manifestIdList: number[];
}

export class VerbCreateResponse {
  basedVerbAsccpManifestId: number;
}

export class VerifyAppendAssociationResponse {
  warn: boolean;
  message: string;
}
