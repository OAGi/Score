import {CollectionViewer, DataSource, SelectionChange} from '@angular/cdk/collections';
import {ChangeListener} from '../../../bie-management/domain/bie-flat-tree';
import {ExpressionEvaluator, FlatNode, getKey, PathLikeExpressionEvaluator} from '../../../common/flat-tree';
import {BehaviorSubject, empty, forkJoin, Observable} from 'rxjs';
import {ModelBrowserService} from './model-browser.service';
import {CcAccNodeDetail, CcAsccpNodeDetail, CcGraph, CcGraphNode} from '../../domain/core-component-node';
import {hashCode4String, sha256} from '../../../common/utility';

export interface ModelBrowserNode extends FlatNode {

  self: ModelBrowserNode;
  ccType: string;
  deprecated: boolean;
  ccDeprecated: boolean;
  rootNode: ModelBrowserNode;

  required?: boolean;
  locked?: boolean;
  derived?: boolean;

  isGroup: boolean;
  isChanged: boolean;
  isCycle: boolean;
  isChoice: boolean;

  cardinalityMin: number;
  cardinalityMax: number;

  path: string;
  hashPath: string;

  detail: ModelBrowserNodeDetail;

  parents: ModelBrowserNode[];

  showCopyLinkIcon: boolean;
  queryPath: string;

  addChangeListener(listener: ChangeListener<ModelBrowserNode>);

  removeChangeListener(listener: ChangeListener<ModelBrowserNode>);

  fireChangeEvent(propertyName: string, val: any);

  reset();

}

export abstract class ModelBrowserNodeImpl implements ModelBrowserNode {

  get self(): ModelBrowserNode {
    return this;
  }

  get type(): string {
    return this.ccType;
  }

  set type(value: string) {
    this.ccType = value;
  }

  get ccType(): string {
    return this._ccType;
  }

  set ccType(value: string) {
    this._ccType = value;
  }

  get isGroup(): boolean {
    return false;
  }

  get isChoice(): boolean {
    return false;
  }

  abstract get path(): string;

  abstract get hashPath(): string;

  abstract get cardinalityMin(): number;

  abstract get cardinalityMax(): number;

  abstract get ccDeprecated(): boolean;

  get parents(): ModelBrowserNode[] {
    let node: ModelBrowserNode = this;
    const result: ModelBrowserNode[] = [node];
    while (node.parent) {
      if (!(node.parent as ModelBrowserNode).isGroup) {
        result.push(node.parent as ModelBrowserNode);
      }
      node = node.parent as ModelBrowserNode;
    }
    return result.reverse();
  }

  get expanded(): boolean {
    return this._expanded || false;
  }

  set expanded(expanded: boolean) {
    this._expanded = expanded;
    this.children.filter(e => (e as ModelBrowserNode).isGroup)
      .filter(e => e.expanded !== expanded)
      .forEach(child => {
        child.expanded = expanded;
      });
  }

  get derived(): boolean {
    return this._derived || false;
  }

  set derived(derived: boolean) {
    this._derived = derived;
  }

  get hashCode() {
    let hashCode = 7;
    hashCode = 31 * hashCode + hashCode4String(this.hashPath);
    return hashCode;
  }

  get isChanged(): boolean {
    if (this.$hashCode === undefined) {
      return false;
    }
    if (this.locked) {
      return false;
    }
    return (this.$hashCode !== this.hashCode) || ((this._detail) ? this._detail.isChanged : false);
  }

  get detail(): ModelBrowserNodeDetail {
    if (this._detail === undefined) {
      if (this.ccType === 'ACC') {
        this._detail = new ModelBrowserAccNodeDetail(this);
      } else if (this.ccType === 'ASCCP') {
        this._detail = new ModelBrowserAsccpNodeDetail(this);
      } else if (this.ccType === 'BCCP') {
        this._detail = new ModelBrowserBccpNodeDetail(this);
      } else {
        this._detail = new ModelBrowserBdtScNodeDetail(this);
      }
    }
    return this._detail;
  }

  set detail(detail: ModelBrowserNodeDetail) {
    this._detail = detail;
  }

  changeListeners: ChangeListener<ModelBrowserNode>[] = [];
  $hashCode?: number;

  name: string;
  level: number;

  _ccType: string;
  _expanded = false;
  _expandable: boolean = undefined;
  required?: boolean;
  locked?: boolean;
  _derived?: boolean;
  isCycle = false;

  parent?: ModelBrowserNode;
  _children: ModelBrowserNode[] = [];
  _detail: ModelBrowserNodeDetail;

  deprecated: boolean;
  rootNode: ModelBrowserAccNode;

  dataSource: ModelBrowserNodeDataSource<any>;

  showCopyLinkIcon = false;

  get queryPath(): string {
    let parent = this.parent;
    while (!!parent && parent.isGroup) {
      parent = parent.parent as ModelBrowserNode;
    }

    if (!!parent) {
      return [parent.queryPath,
        this.name.replace(new RegExp(' ', 'g'), '')].join('/');
    }
    return this.name.replace(new RegExp(' ', 'g'), '');
  }

  reset() {
    this.$hashCode = this.hashCode;
    if (this._detail) {
      this._detail.reset();
    }
    this.fireChangeEvent('reset', this);
  }

  addChangeListener(listener: ChangeListener<ModelBrowserNode>) {
    if (listener && this.changeListeners.indexOf(listener) === -1) {
      this.changeListeners.push(listener);
    }
  }

  removeChangeListener(listener: ChangeListener<ModelBrowserNode>) {
    if (!!listener && this.changeListeners.indexOf(listener) > -1) {
      this.changeListeners.splice(this.changeListeners.indexOf(listener), 1);
    }
  }

  fireChangeEvent(propertyName: string, val: any) {
    this.changeListeners.forEach(listener => {
      listener.onChange(this, propertyName, val);
    });
  }

  getChildren(options?: any | undefined): ModelBrowserNode[] {
    return this._children;
  }

  get children(): ModelBrowserNode[] {
    return this.getChildren();
  }

  set children(children: ModelBrowserNode[]) {
    this._children = children;
  }

  get expandable(): boolean {
    if (this._expandable !== undefined) {
      return this._expandable;
    }
    if (this._children.length === 0) {
      this.dataSource.database.loadChildren(this);
    }
    this._expandable = this._children.length !== 0;
    // for 'hideUnused'
    this._expandable = this.dataSource.database.children(this).length > 0;
    return this._expandable;
  }

  set expandable(expandable: boolean) {
    this._expandable = expandable;
  }
}

export class ModelBrowserAccNode extends ModelBrowserNodeImpl {

  protected _asccpPath: string;
  private _path: string;
  private _asccpHashPath: string;
  private _hashPath: string;

  asccpNode: CcGraphNode;
  accNode: CcGraphNode;

  constructor() {
    super();
    this.ccType = 'ACC';
  }

  get accManifestId(): number {
    return this.accNode.manifestId;
  }

  get asccpManifestId(): number {
    return this.asccpNode.manifestId;
  }

  get asccpPath(): string {
    if (!this._asccpPath) {
      this._asccpPath = 'ASCCP-' + this.asccpNode.manifestId;
    }
    return this._asccpPath;
  }

  get asccpHashPath(): string {
    if (!this._asccpHashPath) {
      this._asccpHashPath = sha256(this.asccpPath);
    }
    return this._asccpHashPath;
  }

  get accPath(): string {
    return (this.isGroup) ? (this.parent as ModelBrowserAccNode).accPath : this.path;
  }

  get accHashPath(): string {
    return (this.isGroup) ? (this.parent as ModelBrowserAccNode).accHashPath : this.hashPath;
  }

  get path(): string {
    if (!this._path) {
      this._path = [this.asccpPath, 'ACC-' + this.accNode.manifestId].join('>');
    }
    return this._path;
  }

  get hashPath(): string {
    if (!this._hashPath) {
      this._hashPath = sha256(this.path);
    }
    return this._hashPath;
  }

  get cardinalityMin(): number {
    return 1;
  }

  get cardinalityMax(): number {
    return 1;
  }

  get ccDeprecated(): boolean {
    return this.asccpNode.deprecated || this.accNode.deprecated;
  }
}

export class ModelBrowserAsccpNode extends ModelBrowserAccNode {
  private _asccPath: string;
  private _asccHashPath: string;

  intermediateAccNodes: CcGraphNode[];
  asccNode: CcGraphNode;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor() {
    super();
    this.ccType = 'ASCCP';
  }

  get asccManifestId(): number {
    return this.asccNode.manifestId;
  }

  get type(): string {
    if (this.name === 'Extension') {
      return this.ccType + '-EXTENSION';
    }
    return this.ccType;
  }

  get isGroup(): boolean {
    return this.accNode.componentType.endsWith('Group');
  }

  get isChoice(): boolean {
    return this.accNode.componentType.endsWith('Choice');
  }

  get asccPath(): string {
    if (!this._asccPath) {
      let arr;
      if (this.intermediateAccNodes && this.intermediateAccNodes.length > 0) {
        arr = [(this.parent as ModelBrowserAsccpNode).asccpPath, this.intermediateAccNodes.map(e => getKey(e)).join('>')];
      } else {
        arr = [(this.parent as ModelBrowserNode).path,];
      }
      arr.push('ASCC-' + this.asccNode.manifestId);
      this._asccPath = arr.join('>');
    }
    return this._asccPath;
  }

  get asccHashPath(): string {
    if (!this._asccHashPath) {
      this._asccHashPath = sha256(this.asccPath);
    }
    return this._asccHashPath;
  }

  get asccpPath(): string {
    if (!this._asccpPath) {
      if (this.derived) {
        this._asccpPath = 'ASCCP-' + this.asccpNode.manifestId;
      } else {
        this._asccpPath = [this.asccPath, 'ASCCP-' + this.asccpNode.manifestId].join('>');
      }
    }
    return this._asccpPath;
  }

  get derived(): boolean {
    return this._derived || false;
  }

  set derived(derived: boolean) {
    this._derived = derived;
    this._asccPath = undefined;
  }

  get cardinalityMin(): number {
    if (!!this._detail && !!(this._detail as ModelBrowserAsccpNodeDetail).ascc.cardinalityMin) {
      return (this._detail as ModelBrowserAsccpNodeDetail).ascc.cardinalityMin;
    }
    if (this._cardinalityMin === undefined) {
      return this.asccNode.cardinalityMin;
    }
    return this._cardinalityMin;
  }

  set cardinalityMin(cardinalityMin: number) {
    this._cardinalityMin = cardinalityMin;
  }

  get cardinalityMax(): number {
    if (!!this._detail && !!(this._detail as ModelBrowserAsccpNodeDetail).ascc.cardinalityMax) {
      return (this._detail as ModelBrowserAsccpNodeDetail).ascc.cardinalityMax;
    }
    if (this._cardinalityMax === undefined) {
      return this.asccNode.cardinalityMax;
    }
    return this._cardinalityMax;
  }

  set cardinalityMax(cardinalityMax: number) {
    this._cardinalityMax = cardinalityMax;
  }

  get ccDeprecated(): boolean {
    return this.asccpNode.deprecated || this.accNode.deprecated || this.asccNode.deprecated;
  }
}

export class ModelBrowserBccpNode extends ModelBrowserNodeImpl {

  private _bccPath: string;
  private _bccpPath: string;
  private _bdtPath: string;
  private _bccHashPath: string;
  private _bccpHashPath: string;
  private _bdtHashPath: string;

  intermediateAccNodes: CcGraphNode[];

  bccNode: CcGraphNode;
  bccpNode: CcGraphNode;
  bdtNode: CcGraphNode;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor() {
    super();
    this.ccType = 'BCCP';
  }

  get bccManifestId(): number {
    return this.bccNode.manifestId;
  }

  get bccpManifestId(): number {
    return this.bccpNode.manifestId;
  }

  get bdtManifestId(): number {
    return this.bdtNode.manifestId;
  }

  get type(): string {
    if (this.entityType === 'Attribute') {
      return this.ccType + '-ATTRIBUTE';
    }
    return this.ccType;
  }

  get entityType(): string {
    return this.bccNode.entityType;
  }

  get bccPath(): string {
    if (!this._bccPath) {
      let arr;
      if (this.intermediateAccNodes && this.intermediateAccNodes.length > 0) {
        arr = [(this.parent as ModelBrowserAsccpNode).asccpPath, this.intermediateAccNodes.map(e => getKey(e)).join('>')];
      } else {
        arr = [(this.parent as ModelBrowserNode).path,];
      }
      arr.push('BCC-' + this.bccNode.manifestId);
      this._bccPath = arr.join('>');
    }
    return this._bccPath;
  }

  get bccHashPath(): string {
    if (!this._bccHashPath) {
      this._bccHashPath = sha256(this.bccPath);
    }
    return this._bccHashPath;
  }

  get bccpPath(): string {
    if (!this._bccpPath) {
      this._bccpPath = [this.bccPath, 'BCCP-' + this.bccpNode.manifestId].join('>');
    }
    return this._bccpPath;
  }

  get bccpHashPath(): string {
    if (!this._bccpHashPath) {
      this._bccpHashPath = sha256(this.bccpPath);
    }
    return this._bccpHashPath;
  }

  get bdtPath(): string {
    if (!this._bdtPath) {
      this._bdtPath = [this.bccpPath, 'DT-' + this.bdtNode.manifestId].join('>');
    }
    return this._bdtPath;
  }

  get bdtHashPath(): string {
    if (!this._bdtHashPath) {
      this._bdtHashPath = sha256(this.bdtPath);
    }
    return this._bdtHashPath;
  }

  get path(): string {
    return this.bdtPath;
  }

  get hashPath(): string {
    return this.bdtHashPath;
  }

  get cardinalityMin(): number {
    if (!!this._detail && !!(this._detail as ModelBrowserBccpNodeDetail).bcc.cardinalityMin) {
      return (this._detail as ModelBrowserBccpNodeDetail).bcc.cardinalityMin;
    }
    if (this._cardinalityMin === undefined) {
      return this.bccNode.cardinalityMin;
    }
    return this._cardinalityMin;
  }

  set cardinalityMin(cardinalityMin: number) {
    this._cardinalityMin = cardinalityMin;
  }

  get cardinalityMax(): number {
    if (!!this._detail && !!(this._detail as ModelBrowserBccpNodeDetail).bcc.cardinalityMax) {
      return (this._detail as ModelBrowserBccpNodeDetail).bcc.cardinalityMax;
    }
    if (this._cardinalityMax === undefined) {
      return this.bccNode.cardinalityMax;
    }
    return this._cardinalityMax;
  }

  set cardinalityMax(cardinalityMax: number) {
    this._cardinalityMax = cardinalityMax;
  }

  get ccDeprecated(): boolean {
    return this.bccpNode.deprecated || this.bdtNode.deprecated || this.bccNode.deprecated;
  }
}

export class ModelBrowserBdtScNode extends ModelBrowserNodeImpl {

  private _bdtScPath: string;
  private _bdtScHashPath: string;

  bccNode: CcGraphNode;
  bdtScNode: CcGraphNode;
  bdtNode: CcGraphNode;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor() {
    super();
    this.ccType = 'BDT_SC';
  }

  get bdtManifestId(): number {
    return this.bdtNode.manifestId;
  }

  get bdtScManifestId(): number {
    return this.bdtScNode.manifestId;
  }

  get bdtScPath(): string {
    if (!this._bdtScPath) {
      this._bdtScPath = [(this.parent as ModelBrowserNode).path, 'DT_SC-' + this.bdtScNode.manifestId].join('>');
    }
    return this._bdtScPath;
  }

  get bdtScHashPath(): string {
    if (!this._bdtScHashPath) {
      this._bdtScHashPath = sha256(this.bdtScPath);
    }
    return this._bdtScHashPath;
  }

  get path(): string {
    return this.bdtScPath;
  }

  get hashPath(): string {
    return this.bdtScHashPath;
  }

  get cardinalityMin(): number {
    if (!!this._detail && !!(this._detail as ModelBrowserBdtScNodeDetail).bdtSc.cardinalityMin) {
      return (this._detail as ModelBrowserBdtScNodeDetail).bdtSc.cardinalityMin;
    }
    if (this._cardinalityMin === undefined) {
      return this.bdtScNode.cardinalityMin;
    }
    return this._cardinalityMin;
  }

  set cardinalityMin(cardinalityMin: number) {
    this._cardinalityMin = cardinalityMin;
  }

  get cardinalityMax(): number {
    if (!!this._detail && !!(this._detail as ModelBrowserBdtScNodeDetail).bdtSc.cardinalityMax) {
      return (this._detail as ModelBrowserBdtScNodeDetail).bdtSc.cardinalityMax;
    }
    if (this._cardinalityMax === undefined) {
      return this.bdtScNode.cardinalityMax;
    }
    return this._cardinalityMax;
  }

  set cardinalityMax(cardinalityMax: number) {
    this._cardinalityMax = cardinalityMax;
  }

  get ccDeprecated(): boolean {
    return this.bdtScNode.deprecated;
  }
}

export class AccDetail {

  private _node: ModelBrowserAccNode;

  accManifestId: number;
  guid: string;
  objectClassTerm: string;
  den: string;
  definition: string;
  state: string;

  constructor(node: ModelBrowserAccNode) {
    this._node = node;
  }

  get manifestId(): number {
    return this._node.accManifestId;
  }

  get path(): string {
    return this._node.accPath;
  }

  get hashPath(): string {
    return this._node.accHashPath;
  }

  get hashCode(): number {
    return ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.objectClassTerm) ? hashCode4String(this.objectClassTerm) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0);
  }

}

export class AsccDetail {

  private _node: ModelBrowserAsccpNode;

  asccManifestId: number;
  guid: string;
  den: string;
  definition: string;
  cardinalityMin: number;
  cardinalityMax: number;
  deprecated: boolean;

  constructor(node: ModelBrowserAsccpNode) {
    this._node = node;
  }

  get manifestId(): number {
    return this._node.asccManifestId;
  }

  get path(): string {
    return this._node.asccPath;
  }

  get hashPath(): string {
    return this._node.asccHashPath;
  }

  get toAsccpPath(): string {
    return this._node.asccpPath;
  }

  get toAsccpHashPath(): string {
    return this._node.asccpHashPath;
  }

  get hashCode(): number {
    return ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0);
  }

}

export class AsccpDetail {

  private _node: ModelBrowserAccNode | ModelBrowserAsccpNode;

  asccpManifestId: number;
  asccManifestId: number;
  releaseNum: string;
  owner: string;
  guid: string;
  propertyTerm: string;
  den: string;
  definition: string;
  state: string;
  nillable: boolean;
  deprecated: boolean;

  constructor(node: ModelBrowserAccNode | ModelBrowserAsccpNode) {
    this._node = node;
  }

  get manifestId(): number {
    return this._node.asccpManifestId;
  }

  get path(): string {
    return this._node.asccpPath;
  }

  get hashPath(): string {
    return this._node.asccpHashPath;
  }

  get roleOfAccPath(): string {
    return this._node.accPath;
  }

  get roleOfAccHashPath(): string {
    return this._node.accHashPath;
  }

  get hashCode(): number {
    return ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.propertyTerm) ? hashCode4String(this.propertyTerm) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0);
  }

}

export class BccDetail {

  private _node: ModelBrowserBccpNode;

  bccManifestId: number;
  guid: string;
  den: string;
  definition: string;
  nillable: boolean;
  deprecated: boolean;
  cardinalityMin: number;
  cardinalityMax: number;
  defaultValue: string;
  fixedValue: string;

  constructor(node: ModelBrowserBccpNode) {
    this._node = node;
  }

  get manifestId(): number {
    return this._node.bccNode.manifestId;
  }

  get path(): string {
    return this._node.bccPath;
  }

  get hashPath(): string {
    return this._node.bccHashPath;
  }

  get fromAccPath(): string {
    return (this._node.parent as ModelBrowserAsccpNode).accPath;
  }

  get fromAccHashPath(): string {
    return (this._node.parent as ModelBrowserAsccpNode).accHashPath;
  }

  get toBccpPath(): string {
    return this._node.bccpPath;
  }

  get toBccpHashPath(): string {
    return this._node.bccpHashPath;
  }

  get fixedOrDefault(): string {
    if (!!this.fixedValue) {
      return 'fixed';
    } else if (!!this.defaultValue) {
      return 'default';
    } else {
      return 'none';
    }
  }

  get hashCode(): number {
    return ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.nillable) ? 1231 : 1237) +
      ((this.deprecated) ? 1231 : 1237) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0);
  }

}

export class BccpDetail {

  private _node: ModelBrowserBccpNode;

  bccpManifestId: number;
  guid: string;
  propertyTerm: string;
  den: string;
  definition: string;
  state: string;
  nillable: boolean;
  defaultValue: string;
  fixedValue: string;

  constructor(node: ModelBrowserBccpNode) {
    this._node = node;
  }

  get manifestId(): number {
    return this._node.bccpNode.manifestId;
  }

  get path(): string {
    return this._node.bccpPath;
  }

  get hashPath(): string {
    return this._node.bccpHashPath;
  }

  get hashCode(): number {
    return ((this.manifestId) ? this.manifestId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.propertyTerm) ? hashCode4String(this.propertyTerm) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0);
  }

}

export class BdtDetail {
  private _node: ModelBrowserBccpNode | ModelBrowserBdtScNode;

  guid: string;
  cardinalityMin: number;
  cardinalityMax: number;
  propertyTerm: string;
  representationTerm: string;
  definition: string;
  state: string;
  den: string;
  bdtPriRestriList: any[];

  constructor(node: ModelBrowserBccpNode | ModelBrowserBdtScNode) {
    this._node = node;
  }

  get bdtManifestId(): number {
    return this._node.bdtNode.manifestId;
  }

  get valueDomainType(): string {
    return 'Primitive';
  }

  get defaultBdtPriRestri(): any {
    if (!this.bdtPriRestriList) {
      return {};
    }
    return this.bdtPriRestriList.filter(e => e.default)[0];
  }

}

export class BdtScDetail {

  private _node: ModelBrowserBdtScNode;

  dtScManifestId: number;
  cardinalityMin: number;
  cardinalityMax: number;
  propertyTerm: string;
  representationTerm: string;
  definition: string;
  state: string;
  fixedValue: string;
  defaultValue: string;
  deprecated: boolean;
  bdtScPriRestriList: any[];

  constructor(node: ModelBrowserBdtScNode) {
    this._node = node;
  }

  get manifestId(): number {
    return this._node.bdtScNode.manifestId;
  }

  get fixedOrDefault(): string {
    if (!!this.fixedValue) {
      return 'fixed';
    } else if (!!this.defaultValue) {
      return 'default';
    } else {
      return 'none';
    }
  }

  get valueDomainType(): string {
    return 'Primitive';
  }

  get defaultBdtScPriRestri(): any {
    if (!this.bdtScPriRestriList) {
      return {};
    }
    return this.bdtScPriRestriList.filter(e => e.default)[0];
  }

  get path(): string {
    return this._node.bdtScPath;
  }

  get hashPath(): string {
    return this._node.bdtScHashPath;
  }

  get bccHashPath(): string {
    return (this._node.parent as ModelBrowserBccpNode).bccHashPath;
  }

  get hashCode(): number {
    return ((this.manifestId) ? this.manifestId : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.deprecated) ? 1231 : 1237) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0);
  }

}

export abstract class ModelBrowserNodeDetail {
  $hashCode: number;
  isLoaded: boolean;

  get isChanged() {
    return this.$hashCode !== this.hashCode;
  }

  reset() {
    this.$hashCode = this.hashCode;
  }

  abstract get hashCode(): number;
}

export class ModelBrowserAccNodeDetail extends ModelBrowserNodeDetail {
  private _node: ModelBrowserNode;

  acc: AccDetail;
  asccp: AsccpDetail;

  constructor(node: ModelBrowserNode) {
    super();
    this._node = node;
    this.acc = new AccDetail(node as ModelBrowserAccNode);
    this.asccp = new AsccpDetail(node as ModelBrowserAccNode);
    this.reset();
  }

  get hashCode(): number {
    return this.asccp.hashCode + this.acc.hashCode;
  }

  updateAcc(acc: any) {
    this.acc.accManifestId = acc.manifestId;
    this.acc.guid = acc.guid;
    this.acc.objectClassTerm = acc.objectClassTerm;
    this.acc.den = acc.den;
    this.acc.definition = acc.definition;
    this.acc.state = acc.state;
  }

  updateAsccp(asccp: any) {
    this.asccp.asccpManifestId = asccp.manifestId;
    this.asccp.releaseNum = asccp.releaseNum;
    this.asccp.owner = asccp.owner;
    this.asccp.guid = asccp.guid;
    this.asccp.propertyTerm = asccp.propertyTerm;
    this.asccp.den = asccp.den;
    this.asccp.definition = asccp.definition;
    this.asccp.state = asccp.state;
    this.asccp.nillable = asccp.nillable;
    this.asccp.deprecated = asccp.deprecated;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }

}

export class ModelBrowserAsccpNodeDetail extends ModelBrowserNodeDetail {
  private _node: ModelBrowserNode;

  acc: AccDetail;
  ascc: AsccDetail;
  asccp: AsccpDetail;

  constructor(node: ModelBrowserNode, obj?: any) {
    super();
    this._node = node;
    this.asccp = new AsccpDetail(node as ModelBrowserAsccpNode);
    this.acc = new AccDetail(node as ModelBrowserAsccpNode);
    this.ascc = new AsccDetail(node as ModelBrowserAsccpNode);
    this.ascc.deprecated = node.deprecated;
    this.reset();
  }

  get hashCode(): number {
    return this.ascc.hashCode + this.asccp.hashCode + this.acc.hashCode;
  }

  updateAcc(acc: any) {
    this.acc.accManifestId = acc.manifestId;
    this.acc.guid = acc.guid;
    this.acc.objectClassTerm = acc.objectClassTerm;
    this.acc.den = acc.den;
    this.acc.definition = acc.definition;
    this.acc.state = acc.state;
  }

  updateAsccp(asccp: any) {
    this.asccp.asccpManifestId = asccp.manifestId;
    this.asccp.releaseNum = asccp.releaseNum;
    this.asccp.owner = asccp.owner;
    this.asccp.guid = asccp.guid;
    this.asccp.propertyTerm = asccp.propertyTerm;
    this.asccp.den = asccp.den;
    this.asccp.definition = asccp.definition;
    this.asccp.state = asccp.state;
    this.asccp.nillable = asccp.nillable;
    this.asccp.deprecated = asccp.deprecated;
  }

  updateAscc(ascc: any) {
    this.ascc.asccManifestId = ascc.manifestId;
    this.ascc.guid = ascc.guid;
    this.ascc.den = ascc.den;
    this.ascc.definition = ascc.definition;
    this.ascc.cardinalityMin = ascc.cardinalityMin;
    this.ascc.cardinalityMax = ascc.cardinalityMax;
    this.ascc.deprecated = ascc.deprecated;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }
}

export class ModelBrowserBccpNodeDetail extends ModelBrowserNodeDetail {
  private _node: ModelBrowserNode;

  bcc: BccDetail;
  bccp: BccpDetail;
  bdt: BdtDetail;

  constructor(node: ModelBrowserNode, obj?: any) {
    super();
    this._node = node;
    this.bccp = new BccpDetail(node as ModelBrowserBccpNode);
    this.bcc = new BccDetail(node as ModelBrowserBccpNode);
    this.bdt = new BdtDetail(node as ModelBrowserBccpNode);
    this.bcc.deprecated = node.deprecated;
    this.reset();
  }

  get hashCode(): number {
    return this.bcc.hashCode + this.bccp.hashCode;
  }

  updateBdt(bdt: any) {
    this.bdt.guid = bdt.guid;
    this.bdt.cardinalityMin = bdt.cardinalityMin;
    this.bdt.cardinalityMax = bdt.cardinalityMax;
    this.bdt.propertyTerm = bdt.propertyTerm;
    this.bdt.representationTerm = bdt.representationTerm;
    this.bdt.definition = bdt.definition;
    this.bdt.state = bdt.state;
    this.bdt.den = bdt.den;
    this.bdt.bdtPriRestriList = bdt.bdtPriRestriList;
  }

  updateBccp(bccp: any) {
    this.bccp.bccpManifestId = bccp.manifestId;
    this.bccp.guid = bccp.guid;
    this.bccp.propertyTerm = bccp.propertyTerm;
    this.bccp.den = bccp.den;
    this.bccp.definition = bccp.definition;
    this.bccp.state = bccp.state;
    this.bccp.nillable = bccp.nillable;
    this.bccp.defaultValue = bccp.defaultValue;
    this.bccp.fixedValue = bccp.fixedValue;
  }

  updateBcc(bcc: any) {
    this.bcc.bccManifestId = bcc.manifestId;
    this.bcc.guid = bcc.guid;
    this.bcc.den = bcc.den;
    this.bcc.definition = bcc.definition;
    this.bcc.nillable = bcc.nillable;
    this.bcc.deprecated = bcc.deprecated;
    this.bcc.cardinalityMin = bcc.cardinalityMin;
    this.bcc.cardinalityMax = bcc.cardinalityMax;
    this.bcc.defaultValue = bcc.defaultValue;
    this.bcc.fixedValue = bcc.fixedValue;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }
}

export class ModelBrowserBdtScNodeDetail extends ModelBrowserNodeDetail {
  private _node: ModelBrowserNode;

  bdtSc: BdtScDetail;
  bdt: BdtDetail;

  constructor(node: ModelBrowserNode, obj?: any) {
    super();
    this._node = node;
    this.bdt = new BdtDetail(node as ModelBrowserBdtScNode);
    this.bdtSc = new BdtScDetail(node as ModelBrowserBdtScNode);
    this.bdtSc.deprecated = node.deprecated;
    this.reset();
  }

  get hashCode(): number {
    return this.bdtSc.hashCode;
  }

  updateBdt(bdt: any) {
    this.bdt.guid = bdt.guid;
    this.bdt.cardinalityMin = bdt.cardinalityMin;
    this.bdt.cardinalityMax = bdt.cardinalityMax;
    this.bdt.propertyTerm = bdt.propertyTerm;
    this.bdt.representationTerm = bdt.representationTerm;
    this.bdt.definition = bdt.definition;
    this.bdt.state = bdt.state;
    this.bdt.den = bdt.den;
    this.bdt.bdtPriRestriList = bdt.bdtPriRestriList;
  }

  updateBdtSc(bdtSc: any) {
    this.bdtSc.dtScManifestId = bdtSc.manifestId;
    this.bdtSc.cardinalityMin = bdtSc.cardinalityMin;
    this.bdtSc.cardinalityMax = bdtSc.cardinalityMax;
    this.bdtSc.propertyTerm = bdtSc.propertyTerm;
    this.bdtSc.representationTerm = bdtSc.representationTerm;
    this.bdtSc.definition = bdtSc.definition;
    this.bdtSc.state = bdtSc.state;
    this.bdtSc.fixedValue = bdtSc.fixedValue;
    this.bdtSc.defaultValue = bdtSc.defaultValue;
    this.bdtSc.deprecated = bdtSc.deprecated;
    this.bdtSc.bdtScPriRestriList = bdtSc.bdtScPriRestriList;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }
}

export class Association {

  intermediateAccNodes: CcGraphNode[] = [];
  fromAccNode: CcGraphNode;
  assocNode: CcGraphNode;

}

export class ModelBrowserNodeDataSource<T extends ModelBrowserNode> implements DataSource<T>, ChangeListener<T> {

  dataChange = new BehaviorSubject<T[]>([]);
  _listeners: ChangeListener<ModelBrowserNodeDataSource<T>>[] = [];

  _hideUnused = false;
  _hideCardinality = false;

  get data(): T[] {
    return this.dataChange.value;
  }

  set data(value: T[]) {
    value.forEach(e => e.addChangeListener(this));
    this.dataChange.next(value);
  }

  addListener(listener: ChangeListener<ModelBrowserNodeDataSource<T>>) {
    if (!!listener && this._listeners.indexOf(listener) === -1) {
      this._listeners.push(listener);
    }
  }

  init() {
    this.data = [this._database.rootNode as unknown as T,];

    // pre-expanding nodes to recognize required elements.
    let nodes = [this.data[0],];
    while (nodes.length > 0) {
      const node = nodes.shift();
      if (node.required && node.expandable && node.children.length === 0) {
        this._database.loadChildren(node);
      }
      nodes = node.children.concat(nodes) as T[];
    }
  }

  onChange(entity: T, propertyName: string, val: any) {
    if (!!this.delegatedListeners) {
      this.delegatedListeners.forEach(e => e.onChange(entity, propertyName, val));
    }
  }

  get hideCardinality(): boolean {
    return this._hideCardinality;
  }

  set hideCardinality(hideCardinality: boolean) {
    if (this._hideCardinality === hideCardinality) {
      return;
    }

    this._hideCardinality = hideCardinality;
    this._listeners.forEach(e => e.onChange(this, 'hideCardinality', hideCardinality));
  }

  constructor(
    private _database: ModelBrowserNodeDatabase<T>,
    private service: ModelBrowserService,
    private delegatedListeners?: ChangeListener<T>[]
  ) {
    _database.dataSource = this;
  }

  get database(): ModelBrowserNodeDatabase<T> {
    return this._database;
  }

  isExpanded(node: T): boolean {
    if (!node) {
      return false;
    }
    return node.expanded;
  }

  getLevel(node: T): number {
    return node.level;
  }

  isExpandable(node: T): boolean {
    if (!node) {
      return false;
    }
    return node.expandable;
  }

  toggle(node: T) {
    if (!node) {
      return;
    }
    if (this.isExpanded(node)) {
      this.collapse(node);
    } else {
      this.expand(node);
    }
  }

  expand(node: T) {
    if (!node) {
      return;
    }
    if (node.parent && !this.isExpanded(node.parent as T)) {
      this.expand(node.parent as T);
    }
    if (this.isExpanded(node)) {
      return;
    }
    this.toggleNode(node, true);
  }

  expandDescendants(node: T, level?: number) {
    this.expand(node);

    if (level > 0) {
      node.children.forEach(e => this.expandDescendants(e as T, level - 1));
    }
  }

  collapse(node: T) {
    if (!node) {
      return;
    }
    if (!this.isExpanded(node)) {
      return;
    }
    this.toggleNode(node, false);
    this.collapseDescendants(node);
  }

  collapseDescendants(dataNode: T) {
    if (!!dataNode.children) {
      dataNode.children.forEach(e => this.collapse(e as T));
    }
  }

  connect(collectionViewer: CollectionViewer): Observable<T[]> {
    return this.dataChange;
  }

  disconnect(collectionViewer: CollectionViewer): void {
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<T>) {
    if (change.added) {
      change.added.forEach(node => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed
        .slice()
        .reverse()
        .forEach(node => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: T, expand: boolean) {
    if (!node) {
      return;
    }

    let children = this._database.children(node);
    let index = this.data.map(e => e.hashPath).indexOf(node.hashPath);

    if (!children || index < 0) {
      // If no children, or cannot find the node, no op
      return;
    }

    if (expand) {
      children.map(e => e as T).forEach(e => {
        e.expanded = false;
        e.addChangeListener(this);
      });
      this.data.splice(index + 1, 0, ...children);
    } else {
      let count = 0;
      for (
        let i = index + 1;
        i < this.data.length && this.data[i].level > node.level;
        i++, count++
      ) {
      }
      this.data.splice(index + 1, count).forEach(e => {
        e.expanded = false;
        e.removeChangeListener(this);
      });

      // Too many nodes in CC tree.
      if (node.ccType === 'BCCP') {
        if (children.filter(e => !e.isChanged).length === 0) {
          node.children = [];
        }
      }
    }

    // notify the change
    this.dataChange.next(this.data);
    node.expanded = expand;
  }

  loadDetail(node: T, callbackFn?) {
    if (node.detail.isLoaded) {
      return callbackFn && callbackFn(node);
    }

    switch (node.ccType.toUpperCase()) {
      case 'ACC':
        const accNode = (node as unknown as ModelBrowserAccNode);
        forkJoin([
          this.service.getAccDetail(accNode.accManifestId),
          this.service.getAsccpDetail(accNode.asccpManifestId)
        ]).subscribe(([ccAcc, ccAsccp]) => {
          const detail = (node.detail as ModelBrowserAccNodeDetail);
          detail.updateAcc(ccAcc);
          detail.updateAsccp(ccAsccp.asccp);
          detail.reset();
          detail.isLoaded = true;
          return callbackFn && callbackFn(node);
        });
        break;

      case 'ASCCP':
        const asccpNode = (node as unknown as ModelBrowserAsccpNode);
        forkJoin([
          this.service.getAccDetail(asccpNode.accManifestId),
          this.service.getAsccpDetail(asccpNode.asccpManifestId, asccpNode.asccManifestId)
        ]).subscribe(([ccAcc, ccAsccp]) => {
          const detail = (node.detail as ModelBrowserAsccpNodeDetail);
          detail.updateAcc(ccAcc);
          detail.updateAsccp(ccAsccp.asccp);
          detail.updateAscc(ccAsccp.ascc);
          detail.reset();
          detail.isLoaded = true;
          return callbackFn && callbackFn(node);
        });
        break;

      case 'BCCP':
        const bccpNode = (node as unknown as ModelBrowserBccpNode);
        forkJoin([
          this.service.getBccpDetail(bccpNode.bccpManifestId, bccpNode.bdtManifestId, bccpNode.bccManifestId)
        ]).subscribe(([ccBccp]) => {
          const detail = (node.detail as ModelBrowserBccpNodeDetail);
          detail.updateBdt(ccBccp.bdt);
          detail.updateBccp(ccBccp.bccp);
          detail.updateBcc(ccBccp.bcc);
          detail.reset();
          detail.isLoaded = true;
          return callbackFn && callbackFn(node);
        });
        break;

      case 'BDT_SC':
        const bdtScNode = (node as unknown as ModelBrowserBdtScNode);
        forkJoin([
          this.service.getBdtDetail(bdtScNode.bdtManifestId),
          this.service.getBdtScDetail(bdtScNode.bdtScManifestId)
        ]).subscribe(([ccBdt, ccBdtSc]) => {
          const detail = (node.detail as ModelBrowserBdtScNodeDetail);
          detail.updateBdt(ccBdt);
          detail.updateBdtSc(ccBdtSc);
          detail.reset();
          detail.isLoaded = true;
          return callbackFn && callbackFn(node);
        });
        break;
    }
  }

}

export class ModelBrowserNodeDatabase<T extends ModelBrowserNode> {

  private _ccGraph: CcGraph;
  private _type: string;
  private _manifestId: number;

  dataSource: ModelBrowserNodeDataSource<T>;

  constructor(ccGraph: CcGraph, type: string, manifestId: number) {
    this._ccGraph = ccGraph;
    this._type = type;
    this._manifestId = manifestId;
  }

  children(node: T): T[] {
    if (!node.expandable) {
      return [];
    }

    const nodes = [];
    const attributes = [];
    node.children.map(e => e as T).forEach(e => {
      const _node = e.self; // in case of it is WrappedBieFlatNode
      if (_node.isGroup) {
        if ((_node as ModelBrowserAsccpNode).accNode.componentType === 'AttributeGroup') {
          attributes.push(...this.children(e));
        } else {
          nodes.push(...this.children(e));
        }
      } else {
        if (_node instanceof ModelBrowserBccpNode && (_node as ModelBrowserBccpNode).bccNode.entityType === 'Attribute') {
          attributes.push(e);
        } else {
          nodes.push(e);
        }
      }
    });
    const children = attributes.concat(nodes);
    return children;
  }

  loadChildren(node: T) {
    if (node.children.length > 0) {
      return;
    }

    let children = [];
    const nodeCcType = node.ccType;
    const _node = node.self; // in case of it is WrappedBieFlatNode
    if (nodeCcType === 'ACC' || nodeCcType === 'ASCCP') {
      children = this.getAssociations((_node as ModelBrowserAsccpNode).accNode);
      node.children = children.map((e: Association) => {
        if (e.assocNode.type === 'ASCC') {
          const asccpNode: ModelBrowserAsccpNode = this.toAsccpNode(e, _node as ModelBrowserAsccpNode);
          asccpNode.reset();
          return asccpNode;
        } else {
          const bccpNode: ModelBrowserBccpNode = this.toBccpNode(e, _node as ModelBrowserAsccpNode);
          bccpNode.reset();
          return bccpNode;
        }
      });

      node.children.map(e => e as T).forEach(e => {
        if (e.isGroup) {
          this.loadChildren(e);
        }
      });
    } else if (nodeCcType === 'BCCP') {
      children = this.getChildren((_node as ModelBrowserBccpNode).bdtNode);
      node.children = children.map(e => {
        const bdtScNode = this.toBdtScNode((_node as ModelBrowserBccpNode).bccNode, e, node);
        bdtScNode.reset();
        return bdtScNode;
      }).sort((a, b) => a.name.localeCompare(b.name));
    }
  }

  getChildren(node: CcGraphNode): CcGraphNode[] {
    const nodes = this._ccGraph.graph.nodes;
    const edges = this._ccGraph.graph.edges;

    const edge = edges[getKey(node)];
    const targets = (!!edge) ? edge.targets : [];
    if (!targets || targets.length === 0) {
      return [];
    }

    switch (node.type) {
      case 'ACC':
        return this.getAssociations(node).map(e => e.assocNode);

      case 'ASCC':
      case 'BCC':
      case 'ASCCP':
      case 'BCCP':
        return [nodes[targets[0]],];

      case 'DT':
        return targets.map(e => nodes[e]).filter(e => e.cardinalityMax > 0);

      case 'DT_SC':
        return [];
    }
  }

  getAssociations(node: CcGraphNode, intermediates?: CcGraphNode[]): Association[] {
    if (!node || node.type !== 'ACC') {
      return [];
    }

    const nodes = this._ccGraph.graph.nodes;
    const edges = this._ccGraph.graph.edges;

    const edge = edges[getKey(node)];
    const targets = (!!edge) ? edge.targets : [];
    if (!targets || targets.length === 0) {
      return [];
    }

    let children: Association[] = [];
    let startIdx = 0;
    if (targets[0].startsWith('ACC')) {
      const basedAcc = nodes[targets[0]];
      if (!intermediates) {
        intermediates = [node];
      }
      children = this.getAssociations(basedAcc, intermediates.concat(basedAcc));
      startIdx = 1;
    }

    const attributes: Association[] = [];
    for (const target of targets.slice(startIdx)) {
      const child = new Association();
      child.intermediateAccNodes = (intermediates || []);
      child.fromAccNode = node;
      child.assocNode = nodes[target];

      if (child.assocNode.type === 'BCC' && child.assocNode.entityType === 'Attribute') {
        attributes.push(child);
        continue;
      }
      children.push(child);
    }

    let lastAttrIndex = 0;
    for (const child of children) {
      if (child.assocNode.type === 'BCC' && child.assocNode.entityType === 'Attribute') {
        lastAttrIndex += 1;
      } else {
        break;
      }
    }

    return children.slice(0, lastAttrIndex).concat(attributes).concat(
      children.slice(lastAttrIndex, children.length)
    );
  }

  get rootNode(): T {
    const node = this.toAccNode('ASCCP-' + this._manifestId);
    return node as unknown as T;
  }

  toAccNode(key: string): ModelBrowserAccNode {
    const node = new ModelBrowserAccNode();
    node.asccpNode = this._ccGraph.graph.nodes[key];
    node.accNode = this.getChildren(node.asccpNode)[0];
    node.name = node.asccpNode.propertyTerm;
    node.level = 0;
    node.required = true;
    node.deprecated = node.asccpNode.deprecated || node.accNode.deprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  toAsccpNode(ascc: Association, parent: ModelBrowserAsccpNode) {
    const node = new ModelBrowserAsccpNode();
    node.asccNode = ascc.assocNode;
    node.required = node.asccNode.cardinalityMin > 0;
    node.asccpNode = this.getChildren(node.asccNode)[0];
    node.accNode = this.getChildren(node.asccpNode)[0];
    node.name = node.asccpNode.propertyTerm;
    if (parent.isGroup) {
      node.level = parent.level;
      node.parent = parent;
    } else {
      node.level = parent.level + 1;
      node.parent = parent;
    }
    node.intermediateAccNodes = ascc.intermediateAccNodes;
    if (parent.derived) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    if (!node.isGroup) {
      node.isCycle = this.detectCycle(node);
    }

    node.deprecated = node.ccDeprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  toBccpNode(bcc: Association, parent: ModelBrowserAsccpNode) {
    const node = new ModelBrowserBccpNode();
    node.bccNode = bcc.assocNode;
    node.required = node.bccNode.cardinalityMin > 0;
    node.bccpNode = this.getChildren(node.bccNode)[0];
    node.bdtNode = this.getChildren(node.bccpNode)[0];
    node.name = node.bccpNode.propertyTerm;
    if (parent.isGroup) {
      node.level = parent.level;
      node.parent = parent;
    } else {
      node.level = parent.level + 1;
      node.parent = parent;
    }
    node.intermediateAccNodes = bcc.intermediateAccNodes;
    if (parent.derived) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }

    node.deprecated = node.ccDeprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  toBdtScNode(bccNode: CcGraphNode, bdtScNode: CcGraphNode, parent: T) {
    const node = new ModelBrowserBdtScNode();
    node.bccNode = bccNode;
    node.bdtNode = this._ccGraph.graph.nodes[getKey((parent as unknown as ModelBrowserBccpNode).bdtNode)];
    node.bdtScNode = this._ccGraph.graph.nodes[getKey(bdtScNode)];
    node.required = node.bdtScNode.cardinalityMin > 0;
    node.name = node.bdtScNode.propertyTerm + ' ' + node.bdtScNode.representationTerm;
    node.level = parent.level + 1;
    node.parent = parent;
    if (parent.derived) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.deprecated = node.bdtScNode.deprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  detectCycle(node: ModelBrowserAsccpNode): boolean {
    const asccpManifestId = node.asccpNode.manifestId;
    let cur = node.parent;
    while (cur) {
      if ((cur as ModelBrowserAccNode).asccpNode.manifestId === asccpManifestId) {
        return true;
      }
      cur = cur.parent as ModelBrowserNode;
    }
    return false;
  }
}

export class ModelBrowserNodeDataSourceSearcher<T extends ModelBrowserNode>
  implements ChangeListener<ModelBrowserNodeDataSource<T>> {

  searchKeyword = '';
  _inputKeyword = '';
  selectedNode: T;
  searchResult: T[] = [];
  searchedData: T[];
  fullSearched = false;
  searchedItemCount = 0;
  searchIndex = 0;
  isSearching = false;
  searchPrefix = '';

  constructor(private dataSource: ModelBrowserNodeDataSource<T>,
              private database: ModelBrowserNodeDatabase<T>) {
    dataSource.addListener(this);
  }

  get inputKeyword(): string {
    return this._inputKeyword;
  }

  set inputKeyword(inputKeyword: string) {
    if (inputKeyword !== this._inputKeyword) {
      this.resetSearch();
    }
    this._inputKeyword = inputKeyword;
  }

  onChange(entity: ModelBrowserNodeDataSource<T>, propertyName: string, val: any) {
  }

  prev(node: T): T {
    const index = this.getNodeIndex(node);
    if (index > 0) {
      return this.dataSource.data[index - 1];
    }
    return node;
  }

  next(node: T): T {
    const index = this.getNodeIndex(node);
    if (index < this.dataSource.data.length - 1) {
      return this.dataSource.data[index + 1];
    }
    return node;
  }

  go(val: number): Observable<number> {
    this.searchIndex += val;
    if (this.searchResult.length <= this.searchIndex) {
      this.searchIndex = 0;
    }
    if (this.searchIndex < 0) {
      this.searchIndex = this.searchResult.length - 1;
    }

    return new Observable(subscriber => {
      if (!!this.searchResult && this.searchResult.length > 0) {
        subscriber.next(this.getNodeIndex(this.searchResult[this.searchIndex]));
      } else {
        subscriber.next(-1);
      }
      subscriber.complete();
    });
  }

  search(inputKeyword: string, selectedNode: T, backward?: boolean, force?: boolean): Observable<number> {
    if (this.isSearching) {
      return empty();
    }
    if (!inputKeyword || inputKeyword.length === 0) {
      this.resetSearch();
      return empty();
    }

    this.isSearching = true;
    if (!this.fullSearched || force) {
      const searchResult = [];
      const evaluator = this.getEvaluator(inputKeyword);

      const threshold = 100;
      let expandingLimit = 1000;
      let data = (!this.searchedData || this.searchedData.length === 0) ?
        ((this.inputKeyword.charAt(0) === '/') ? [this.dataSource.data[0],] : [selectedNode,]) :
        this.searchedData;
      while (searchResult.length < threshold && expandingLimit > 0 && data.length > 0) {
        const item = data.shift();
        this.searchedItemCount++;
        if (!item.isGroup && evaluator.eval(item)) {
          searchResult.push(item);
        }
        if (item.expandable) {
          expandingLimit--;
        }

        if (item.children.length > 0) {
          data = data.concat(item.children as T[]);
        }
      }

      if (data.length === 0) {
        this.fullSearched = true;
      } else {
        this.searchedData = data;
      }

      this.searchResult = this.searchResult.concat(searchResult);
      this.searchKeyword = evaluator.keywordForHighlight();
      if (searchResult.length > 0) {
        searchResult.forEach(e => this.dataSource.expand(e));
        this.searchResult = this.sort(this.searchResult);
      }
    } else {
      if (backward) {
        this.searchIndex -= 1;
      } else {
        this.searchIndex += 1;
      }
      if (this.searchResult.length <= this.searchIndex) {
        this.searchIndex = 0;
      }
      if (this.searchIndex < 0) {
        this.searchIndex = this.searchResult.length - 1;
      }
    }

    this.isSearching = false;

    return new Observable(subscriber => {
      if (!!this.searchResult && this.searchResult.length > 0) {
        subscriber.next(this.getNodeIndex(this.searchResult[this.searchIndex]));
      } else {
        subscriber.next(-1);
      }
      subscriber.complete();
    });
  }

  sort(searchResult: T[]): T[] {
    return searchResult.sort((a, b) => {
      const aIdx = this.dataSource.data.indexOf(a);
      const bIdx = this.dataSource.data.indexOf(b);
      return aIdx - bIdx;
    });
  }

  resetSearch() {
    this.searchKeyword = undefined;
    this.selectedNode = undefined;
    this.searchResult = [];
    this.searchedData = [];
    this.fullSearched = false;
    this.searchedItemCount = 0;
    this.searchIndex = 0;
    this.isSearching = false;
    this.searchPrefix = '';
  }

  getNodeIndex(node: T) {
    if (!this.dataSource.isExpanded(node)) {
      this.dataSource.expand(node);
    }
    return this.dataSource.data.indexOf(node);
  }

  protected getEvaluator(expr: string): ExpressionEvaluator<T> {
    return new ModelBrowserNodePathExpressionEvaluator(expr, false);
  }

}

export class ModelBrowserNodePathExpressionEvaluator<T extends ModelBrowserNode> extends PathLikeExpressionEvaluator<T> {
  protected next(node: T): T {
    let next = node.parent as T;
    while (next && next.isGroup) {
      next = next.parent as T;
    }
    return next;
  }
}
