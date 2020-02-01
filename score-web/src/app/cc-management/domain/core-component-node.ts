import {hashCode} from '../../common/utility';

export class CcNode {
  type: string;
  releaseId: number;
  guid: string;
  name: string;
  hasChild: boolean;
  state: string;
  access: string;
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
}

export class CcAsccpNode extends CcNode {
  asccpId: number;
  asccId: number;
  roleOfAccId: number;
  seqKey: number;
}

export class CcBccpNode extends CcNode {
  bccpId: number;
  bccId: number;
  seqKey: number;
  attribute: boolean;
}

export class CcBdtScNode extends CcNode {
  bdtScId: number;
}

export class CcNodeDetail {
  type: string;
  releaseId: number;
}

export class CcAccNodeDetail extends CcNodeDetail {
  accId: number;
  guid: string;
  objectClassTerm: string;
  group: boolean;
  den: string;
  state: string;
  definition: string;
  definitionSource: string;
  oagisComponentType: number;
  abstracted: boolean;
  deprecated: boolean;
  $hashCode: string;
}

export class CcState {
  constructor(public value: number, public name: string) {
  }
}

const Editing: CcState = new CcState(1, 'Editing');
const Candidate: CcState = new CcState(2, 'Candidate');
const Published: CcState = new CcState(3, 'Published');

export const CcStates: CcState[] = [
  Editing, Candidate, Published
];

export class OagisComponentType {
  constructor(public value: number, public name: string) {
  }
}

const Base: OagisComponentType = new OagisComponentType(0, 'Base');
const Semantics: OagisComponentType = new OagisComponentType(1, 'Semantics');
const Extension: OagisComponentType = new OagisComponentType(2, 'Extension');
const SemanticGroup: OagisComponentType = new OagisComponentType(3, 'Semantic Group');
const UserExtensionGroup: OagisComponentType = new OagisComponentType(4, 'User Extension Group');
const Embedded: OagisComponentType = new OagisComponentType(5, 'Embedded');
const OAGIS10Nouns: OagisComponentType = new OagisComponentType(6, 'OAGIS10 Nouns');
const OAGIS10BODs: OagisComponentType = new OagisComponentType(7, 'OAGIS10 BODs');

export const OagisComponentTypes: OagisComponentType[] = [
  Base, Semantics, Extension, SemanticGroup, UserExtensionGroup,
  Embedded, OAGIS10Nouns, OAGIS10BODs
];

export class CcAsccpNodeDetail extends CcNodeDetail {
  ascc: {
    asccId: number;
    guid: string;
    den: string;
    cardinalityMin: number;
    cardinalityMax: number;
    deprecated: boolean;
    definition: string;
    definitionSource: string;
    revisionNum: number;
    $hashCode: string;
  };
  asccp: {
    asccpId: number;
    guid: string;
    propertyTerm: string;
    den: string;
    reusable: boolean;
    deprecated: boolean;
    definition: string;
    definitionSource: string;
    $hashCode: string;
  };
}

export class EntityType {
  constructor(public value: number, public name: string) {
  }
}

const Attribute: EntityType = new EntityType(0, 'Attribute');
const Element: EntityType = new EntityType(1, 'Element');

export const EntityTypes: EntityType[] = [
  Attribute, Element
];

export class CcBccpNodeDetail extends CcNodeDetail {
  bcc: {
    bccId: number;
    guid: string;
    den: string;
    entityType: number;
    cardinalityMin: number;
    cardinalityMax: number;
    nillable: boolean;
    deprecated: boolean;
    defaultValue: string;
    definition: string;
    definitionSource: string;
    revisionNum: number;
    $hashCode: string;
  };
  bccp: {
    bccpId: number;
    guid: string;
    propertyTerm: string;
    den: string;
    nillable: boolean;
    deprecated: boolean;
    defaultValue: string;
    definition: string;
    definitionSource: string;
    $hashCode: string;
  };
  bdt: {
    bdtId: number;
    guid: string;
    dataTypeTerm: string;
    qualifier: string;
    den: string;
    definition: string;
    definitionSource: string;
    defaultValue: string;
    fixedValue: string;
    $hashCode: string;
  };
}

export class CcBdtScNodeDetail extends CcNodeDetail {
  bdtScId: number;
  guid: string;
  den: string;
  cardinalityMin: number;
  cardinalityMax: number;
  definition: string;
  definitionSource: string;
  defaultValue: string;
  fixedValue: string;
  $hashCode: string;
}

/** Flat node with expandable and level information */
export class DynamicCcFlatNode {
  private $hashCode;

  constructor(public item: CcNode,
              public level = 0,
              public isLoading = false,
              public isNullObject = false) {
    this.reset();
  }

  get expandable() {
    return this.item.hasChild;
  }

  get guid() {
    return this.item.type + this.item.guid;
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

export class CcEditUpdateResponse {
  accNodeResult: boolean;
  asccpNodeResults: Map<string, CcEditAsccpUpdateResponse>;
  bccpNodeResults: Map<string, CcEditBccpUpdateResponse>;
  bdtScNodeResults: Map<string, CcEditBdtScUpdateResponse>;
}

export class CcEditAsccpUpdateResponse {
  asccpId: number;
  asccId: number;
  accId: number;
}

export class CcEditBccpUpdateResponse {
  bccpId: number;
  bccId: number;
}

export class CcEditBdtScUpdateResponse {
  bdtScId: number;
}
