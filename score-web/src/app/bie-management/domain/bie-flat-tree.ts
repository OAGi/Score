import {CcGraph, CcGraphNode} from '../../cc-management/domain/core-component-node';
import {hashCode4Array, hashCode4String, sha256} from '../../common/utility';
import {ExpressionEvaluator, FlatNode, getKey, PathLikeExpressionEvaluator} from '../../common/flat-tree';
import {BieDetailUpdateResponse, BieEditAbieNode, RefBie, UsedBie} from '../bie-edit/domain/bie-edit-node';
import {CollectionViewer, DataSource, SelectionChange} from '@angular/cdk/collections';
import {BehaviorSubject, empty, forkJoin, Observable} from 'rxjs';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';


export interface BieFlatNode extends FlatNode {

  self: BieFlatNode;
  bieId: number;
  bieType: string;
  displayName: string;
  topLevelAsbiepId: number;
  basedTopLevelAsbiepId: number;
  deprecated: boolean;
  ccDeprecated: boolean;
  rootNode: BieEditAbieNode;
  inverseMode: boolean;

  used: boolean | undefined;
  required?: boolean;
  locked?: boolean;
  reused?: boolean; // Is Reused?
  inherited?: boolean; // If true, basedTopLevelAsbiepId must not be null.

  isGroup: boolean;
  isChanged: boolean;
  isCycle: boolean;
  isChoice: boolean;

  cardinalityMin: number;
  cardinalityMax: number;

  path: string;
  hashPath: string;

  detail: BieEditNodeDetail;

  parents: BieFlatNode[];

  showCopyLinkIcon: boolean;
  queryPath: string;

  addChangeListener(listener: ChangeListener<BieFlatNode>);
  removeChangeListener(listener: ChangeListener<BieFlatNode>);

  fireChangeEvent(propertyName: string, val: any);
  reset();

}

export interface ChangeListener<T> {
  onChange(entity: T, propertyName: string, val: any);
}

export abstract class BieFlatNodeImpl implements BieFlatNode {

  get self(): BieFlatNode {
    return this;
  }

  get bieId(): number {
    return this._bieId;
  }

  set bieId(bieId: number) {
    this._bieId = bieId;
  }

  get type(): string {
    return this.bieType;
  }

  set type(value: string) {
    this.bieType = value;
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

  get parents(): BieFlatNode[] {
    let node: BieFlatNode = this;
    const result: BieFlatNode[] = [node];
    while (node.parent) {
      if (!(node.parent as BieFlatNode).isGroup) {
        result.push(node.parent as BieFlatNode);
      }
      node = node.parent as BieFlatNode;
    }
    return result.reverse();
  }

  set used(used: boolean) {
    if (this._used === used) {
      return;
    }

    this._used = used;
    if (this.$hashCode === undefined) {
      this.$hashCode = this.hashCode;
    }

    const children = this.children.map(e => e as BieFlatNode);
    if (used) {
      // this.parent.reused means it's reused
      if (!!this.parent && !this.parent.reused) {
        this.parent.used = used;

        this.parent.children.map(e => e as BieFlatNode).filter(e => !e.locked).forEach(e => {
          if (e.isGroup || e.required) {
            e.used = used;
          }
        });
      }

      children.filter(e => !e.locked).forEach(e => {
        if (e.isGroup || e.required) {
          e.used = used;
        }
      });
    } else {
      children.filter(e => !e.locked).forEach(e => {
        e.used = used;
      });

      if (children.filter(e => !e.required).filter(e => e.used).length === 0) {
        children.filter(e => e.required).forEach(e => e.used = used);
      }

      // this.parent.reused means it's reused
      if (!!this.parent && !this.parent.reused && this.parent.used === undefined) {
        if (this.parent.children.map(e => e as BieFlatNode)
          .filter(e => e.used === false).length === this.parent.children.length) {
          this.parent.used = false;
        } else {
          this.parent.used = true;
        }
      }
    }

    this.fireChangeEvent('used', this._used);
  }

  get used(): boolean | undefined {
    if (this.isGroup) {
      return (this.parent as BieFlatNode).used;
    }
    return this._used;
  }

  get expanded(): boolean {
    return this._expanded || false;
  }

  set expanded(expanded: boolean) {
    this._expanded = expanded;
    this.children.filter(e => (e as BieFlatNode).isGroup)
      .filter(e => e.expanded !== expanded)
      .forEach(child => {
        child.expanded = expanded;
      });
  }

  get reused(): boolean {
    return this._reused || false;
  }

  set reused(reused: boolean) {
    this._reused = reused;
  }

  get inherited(): boolean {
    return this._inherited || false;
  }

  set inherited(inherited: boolean) {
    this._inherited = inherited;
  }

  get hashCode() {
    let hashCode = 7;
    hashCode = 31 * hashCode + hashCode4String(this.hashPath);
    if (!!this.bieId) {
      hashCode = 31 * hashCode + this.bieId;
    }
    if (this._used === undefined) {
      hashCode = 31 * hashCode;
    } else {
      hashCode = 31 * hashCode + ((this._used) ? 1231 : 1237);
    }
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

  get detail(): BieEditNodeDetail {
    if (this._detail === undefined) {
      if (this.bieType === 'ABIE') {
        this._detail = new BieEditAbieNodeDetail(this);
      } else if (this.bieType === 'ASBIEP') {
        this._detail = new BieEditAsbiepNodeDetail(this);
      } else if (this.bieType === 'BBIEP') {
        this._detail = new BieEditBbiepNodeDetail(this);
      } else {
        this._detail = new BieEditBbieScNodeDetail(this);
      }
    }
    return this._detail;
  }

  changeListeners: ChangeListener<BieFlatNode>[] = [];
  $hashCode?: number;

  name: string;
  _displayName: string;

  get displayName(): string {
    return this._displayName;
  }

  set displayName(value: string) {
    this._displayName = value;
    this.fireChangeEvent('displayName', this._displayName);
  }

  level: number;
  bieType: string;
  topLevelAsbiepId: number;
  basedTopLevelAsbiepId: number;

  _bieId: number = undefined;
  _used: boolean = undefined;
  _expanded = false;
  _expandable: boolean = undefined;
  required?: boolean;
  locked?: boolean;
  _reused?: boolean;
  _inherited?: boolean;
  isCycle = false;

  parent?: BieFlatNode;
  _children: BieFlatNode[] = [];
  _detail: BieEditNodeDetail;

  deprecated: boolean;
  rootNode: BieEditAbieNode;

  get inverseMode(): boolean {
    if (!this.rootNode) {
      return false;
    }
    return this.rootNode.inverseMode;
  }

  dataSource: BieFlatNodeDataSource<any>;

  showCopyLinkIcon = false;

  get queryPath(): string {
    let parent = this.parent;
    while (!!parent && parent.isGroup) {
      parent = parent.parent as BieFlatNode;
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

  addChangeListener(listener: ChangeListener<BieFlatNode>) {
    if (listener && this.changeListeners.indexOf(listener) === -1) {
      this.changeListeners.push(listener);
    }
  }

  removeChangeListener(listener: ChangeListener<BieFlatNode>) {
    if (!!listener && this.changeListeners.indexOf(listener) > -1) {
      this.changeListeners.splice(this.changeListeners.indexOf(listener), 1);
    }
  }

  fireChangeEvent(propertyName: string, val: any) {
    this.changeListeners.forEach(listener => {
      listener.onChange(this, propertyName, val);
    });
  }

  getChildren(options?: any | undefined): BieFlatNode[] {
    if (!!options && options.hideUnused) {
      return this._children.filter(e => e.used);
    }
    return this._children;
  }

  get children(): BieFlatNode[] {
    return this.getChildren({
      hideUnused: !!this.dataSource && this.dataSource.hideUnused
    });
  }

  set children(children: BieFlatNode[]) {
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

export class AbieFlatNode extends BieFlatNodeImpl {
  asbiepId: number;
  protected _asbiepPath: string;
  private _path: string;
  private _asbiepHashPath: string;
  private _hashPath: string;

  asccpNode: CcGraphNode;
  accNode: CcGraphNode;

  constructor() {
    super();
    this.bieType = 'ABIE';
  }

  get abieId(): number {
    return this.bieId;
  }

  set abieId(abieId: number) {
    this.bieId = abieId;
  }

  get asbiepPath(): string {
    if (!this._asbiepPath) {
      this._asbiepPath = 'ASCCP-' + this.asccpNode.manifestId;
    }
    return this._asbiepPath;
  }

  get asbiepHashPath(): string {
    if (!this._asbiepHashPath) {
      this._asbiepHashPath = sha256(this.asbiepPath);
    }
    return this._asbiepHashPath;
  }

  get abiePath(): string {
    return (this.isGroup) ? (this.parent as AbieFlatNode).abiePath : this.path;
  }

  get abieHashPath(): string {
    return (this.isGroup) ? (this.parent as AbieFlatNode).abieHashPath : this.hashPath;
  }

  get path(): string {
    if (!this._path) {
      this._path = [this.asbiepPath, 'ACC-' + this.accNode.manifestId].join('>');
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

export class AsbiepFlatNode extends AbieFlatNode {
  private _asbiePath: string;
  private _asbieHashPath: string;

  intermediateAccNodes: CcGraphNode[];
  asccNode: CcGraphNode;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor() {
    super();
    this.bieType = 'ASBIEP';
  }

  get asbieId(): number {
    return this.bieId;
  }

  set asbieId(asbieId: number) {
    this.bieId = asbieId;
  }

  get type(): string {
    if (this.name === 'Extension') {
      return this.bieType + '-EXTENSION';
    }
    return this.bieType;
  }

  get isGroup(): boolean {
    return this.accNode.componentType.endsWith('Group');
  }

  get isChoice(): boolean {
    return this.accNode.componentType.endsWith('Choice');
  }

  get asbiePath(): string {
    if (!this._asbiePath) {
      let arr;
      if (this.intermediateAccNodes && this.intermediateAccNodes.length > 0) {
        arr = [(this.parent as AsbiepFlatNode).asbiepPath, this.intermediateAccNodes.map(e => getKey(e)).join('>')];
      } else {
        arr = [(this.parent as BieFlatNode).path, ];
      }
      arr.push('ASCC-' + this.asccNode.manifestId);
      this._asbiePath = arr.join('>');
    }
    return this._asbiePath;
  }

  get asbieHashPath(): string {
    if (!this._asbieHashPath) {
      this._asbieHashPath = sha256(this.asbiePath);
    }
    return this._asbieHashPath;
  }

  get asbiepPath(): string {
    if (!this._asbiepPath) {
      if (this.reused) {
        this._asbiepPath = 'ASCCP-' + this.asccpNode.manifestId;
      } else {
        this._asbiepPath = [this.asbiePath, 'ASCCP-' + this.asccpNode.manifestId].join('>');
      }
    }
    return this._asbiepPath;
  }

  get reused(): boolean {
    return this._reused || false;
  }

  set reused(reused: boolean) {
    this._reused = reused;
    this._asbiePath = undefined;
  }

  get cardinalityMin(): number {
    if (!!this._detail && !!(this._detail as BieEditAsbiepNodeDetail).asbie.cardinalityMin) {
      return (this._detail as BieEditAsbiepNodeDetail).asbie.cardinalityMin;
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
    if (!!this._detail && !!(this._detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax) {
      return (this._detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax;
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

export class BbiepFlatNode extends BieFlatNodeImpl {
  bbiepId: number;
  private _bbiePath: string;
  private _bbiepPath: string;
  private _bdtPath: string;
  private _bbieHashPath: string;
  private _bbiepHashPath: string;
  private _bdtHashPath: string;

  intermediateAccNodes: CcGraphNode[];

  bccNode: CcGraphNode;
  bccpNode: CcGraphNode;
  bdtNode: CcGraphNode;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor() {
    super();
    this.bieType = 'BBIEP';
  }

  get bbieId(): number {
    return this.bieId;
  }

  set bbieId(bbieId: number) {
    this.bieId = bbieId;
  }

  get type(): string {
    if (this.entityType === 'Attribute') {
      return this.bieType + '-ATTRIBUTE';
    }
    return this.bieType;
  }

  get entityType(): string {
    return this.bccNode.entityType;
  }

  get bbiePath(): string {
    if (!this._bbiePath) {
      let arr;
      if (this.intermediateAccNodes && this.intermediateAccNodes.length > 0) {
        arr = [(this.parent as AsbiepFlatNode).asbiepPath, this.intermediateAccNodes.map(e => getKey(e)).join('>')];
      } else {
        arr = [(this.parent as BieFlatNode).path, ];
      }
      arr.push('BCC-' + this.bccNode.manifestId);
      this._bbiePath = arr.join('>');
    }
    return this._bbiePath;
  }

  get bbieHashPath(): string {
    if (!this._bbieHashPath) {
      this._bbieHashPath = sha256(this.bbiePath);
    }
    return this._bbieHashPath;
  }

  get bbiepPath(): string {
    if (!this._bbiepPath) {
      this._bbiepPath = [this.bbiePath, 'BCCP-' + this.bccpNode.manifestId].join('>');
    }
    return this._bbiepPath;
  }

  get bbiepHashPath(): string {
    if (!this._bbiepHashPath) {
      this._bbiepHashPath = sha256(this.bbiepPath);
    }
    return this._bbiepHashPath;
  }

  get bdtPath(): string {
    if (!this._bdtPath) {
      this._bdtPath = [this.bbiepPath, 'DT-' + this.bdtNode.manifestId].join('>');
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
    if (!!this._detail && !!(this._detail as BieEditBbiepNodeDetail).bbie.cardinalityMin) {
      return (this._detail as BieEditBbiepNodeDetail).bbie.cardinalityMin;
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
    if (!!this._detail && !!(this._detail as BieEditBbiepNodeDetail).bbie.cardinalityMax) {
      return (this._detail as BieEditBbiepNodeDetail).bbie.cardinalityMax;
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

export class BbieScFlatNode extends BieFlatNodeImpl {
  bbieId: number;
  private _bbieScPath: string;
  private _bbieScHashPath: string;

  bccNode: CcGraphNode;
  bdtScNode: CcGraphNode;
  bdtNode: CcGraphNode;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor() {
    super();
    this.bieType = 'BBIE_SC';
  }

  get bbieScId(): number {
    return this.bieId;
  }

  set bbieScId(bbieScId: number) {
    this.bieId = bbieScId;
  }

  get bbieScPath(): string {
    if (!this._bbieScPath) {
      this._bbieScPath = [(this.parent as BieFlatNode).path, 'DT_SC-' + this.bdtScNode.manifestId].join('>');
    }
    return this._bbieScPath;
  }

  get bbieScHashPath(): string {
    if (!this._bbieScHashPath) {
      this._bbieScHashPath = sha256(this.bbieScPath);
    }
    return this._bbieScHashPath;
  }

  get path(): string {
    return this.bbieScPath;
  }

  get hashPath(): string {
    return this.bbieScHashPath;
  }

  get cardinalityMin(): number {
    if (!!this._detail && !!(this._detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMin) {
      return (this._detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMin;
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
    if (!!this._detail && !!(this._detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax) {
      return (this._detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax;
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

export class WrappedBieFlatNode implements BieFlatNode {
  _node: BieFlatNode;

  constructor(node: BieFlatNode) {
    this._node = node;
  }

  get self(): BieFlatNode {
    return this._node;
  }

  get bieId(): number {
    return this._node.bieId;
  }

  set bieId(bieId: number) {
    this._node.bieId = bieId;
  }

  get bieType(): string {
    return this._node.bieType;
  }

  get typeClass(): string {
    switch (this.type.toUpperCase()) {
      case 'ASBIEP':
        return (this._node as AsbiepFlatNode).type;
      case 'BBIEP':
        return (this._node as BbiepFlatNode).type;
      case 'BBIE_SC':
        return (this._node as BbieScFlatNode).type;
      default:
        return this._node.bieType;
    }
  }

  get topLevelAsbiepId(): number {
    return this._node.topLevelAsbiepId;
  }

  get basedTopLevelAsbiepId(): number {
    return this._node.basedTopLevelAsbiepId;
  }

  get required(): boolean {
    return this._node.required;
  }

  get path(): string {
    return this._node.path;
  }

  get hashPath(): string {
    return this._node.hashPath;
  }

  get detail(): BieEditNodeDetail {
    return this._node.detail;
  }

  get name(): string {
    return this._node.name;
  }

  set name(name: string) {
    this._node.name = name;
  }

  get displayName(): string {
    return this._node.displayName;
  }

  set displayName(displayName: string) {
    this._node.displayName = displayName;
  }

  get level(): number {
    return this._node.level;
  }

  set level(level: number) {
    this._node.level = level;
  }

  get expanded(): boolean {
    return this._node.expanded;
  }

  set expanded(expanded: boolean) {
    this._node.expanded = expanded;
  }

  get expandable(): boolean {
    return this._node.expandable;
  }

  set expandable(expandable: boolean) {
    this._node.expandable = expandable;
  }

  get parent(): FlatNode {
    return this._node.parent;
  }

  set parent(parent: FlatNode) {
    this._node.parent = parent;
  }

  get type(): string {
    return this._node.type;
  }

  get isGroup(): boolean {
    return this._node.isGroup;
  }

  get isCycle(): boolean {
    return this._node.isCycle;
  }

  get isChanged(): boolean {
    return this._node.isChanged;
  }

  get isChoice(): boolean {
    return this._node.isChoice;
  }

  getChildren(options?: any | undefined): FlatNode[] {
    return this._node.getChildren(options);
  }

  get children(): FlatNode[] {
    return this._node.children;
  }

  set children(children: FlatNode[]) {
    this._node.children = children;
  }

  get parents(): WrappedBieFlatNode[] {
    let node: WrappedBieFlatNode = this;
    const result: WrappedBieFlatNode[] = [node];
    while (node.parent) {
      if (!(node.parent as WrappedBieFlatNode).isGroup) {
        result.push(node.parent as WrappedBieFlatNode);
      }
      node = node.parent as WrappedBieFlatNode;
    }
    return result.reverse();
  }

  get used(): boolean {
    return this._node.used;
  }

  set used(used: boolean) {
    this._node.used = used;
  }

  get reused(): boolean {
    return this._node.reused;
  }

  get inherited(): boolean {
    return this._node.inherited;
  }

  get locked(): boolean {
    return this._node.locked;
  }

  reset() {
    this._node.reset();
  }

  addChangeListener(listener: ChangeListener<BieFlatNode>) {
    this._node.addChangeListener(listener);
  }

  removeChangeListener(listener: ChangeListener<BieFlatNode>) {
    this._node.removeChangeListener(listener);
  }

  fireChangeEvent(propertyName: string, val: any) {
    this._node.fireChangeEvent(propertyName, val);
  }

  get deprecated(): boolean {
    return this._node.deprecated;
  }

  get ccDeprecated(): boolean {
    return this._node.ccDeprecated;
  }

  get inverseMode(): boolean {
    return this._node.inverseMode;
  }

  get rootNode(): BieEditAbieNode {
    return this._node.rootNode;
  }

  get cardinalityMin(): number {
    return this._node.cardinalityMin;
  }

  get cardinalityMax(): number {
    return this._node.cardinalityMax;
  }

  get showCopyLinkIcon(): boolean {
    return this._node.showCopyLinkIcon;
  }

  set showCopyLinkIcon(showCopyLinkIcon: boolean) {
    this._node.showCopyLinkIcon = showCopyLinkIcon;
  }

  get queryPath(): string {
    return this._node.queryPath;
  }
}

export class AccDetail {
  accManifestId: number;
  guid: string;
  objectClassTerm: string;
  den: string;
  definition: string;
  state: string;
}

export class AsccDetail {
  asccManifestId: number;
  guid: string;
  den: string;
  definition: string;
  cardinalityMin: number;
  cardinalityMax: number;
}

export class AsccpDetail {
  asccpManifestId: number;
  guid: string;
  propertyTerm: string;
  den: string;
  definition: string;
  state: string;
  nillable: boolean;
}

export class BccDetail {
  bccManifestId: number;
  guid: string;
  den: string;
  definition: string;
  nillable: boolean;
  cardinalityMin: number;
  cardinalityMax: number;
  defaultValue: string;
  fixedValue: string;
  cdtPrimitives: string[];
}

export class BccpDetail {
  bccpManifestId: number;
  guid: string;
  propertyTerm: string;
  den: string;
  definition: string;
  state: string;
  nillable: boolean;
  defaultValue: string;
  fixedValue: string;
}

export class BdtDetail {
  private _node: BbiepFlatNode | BbieScFlatNode;

  guid: string;
  cardinalityMin: number;
  cardinalityMax: number;
  facetMinLength: number;
  facetMaxLength: number;
  facetPattern: string;
  propertyTerm: string;
  representationTerm: string;
  definition: string;
  state: string;
  den: string;

  constructor(node: BbiepFlatNode | BbieScFlatNode) {
    this._node = node;
  }

  get bdtManifestId(): number {
    return this._node.bdtNode.manifestId;
  }
}

export class BdtScDetail {
  dtScManifestId: number;
  cardinalityMin: number;
  cardinalityMax: number;
  propertyTerm: string;
  representationTerm: string;
  definition: string;
  state: string;
  defaultValue: string;
  fixedValue: string;
  fixedOrDefault: string;
  facetMinLength: number;
  facetMaxLength: number;
  facetPattern: string;
  cdtPrimitives: string[];
}

export class AbieDetail {
  private _node: AbieFlatNode | AsbiepFlatNode;

  ownerTopLevelAsbiepId: number;
  abieId: number;
  guid: string;
  den: string;

  private _version: string;
  private _status: string;
  private _remark: string;
  private _bizTerm: string;
  private _definition: string;

  constructor(node: AbieFlatNode | AsbiepFlatNode) {
    this._node = node;
  }

  get version(): string {
    return this._version;
  }

  set version(value: string) {
    this._version = value;
    this._node.fireChangeEvent('version', value);
  }

  get status(): string {
    return this._status;
  }

  set status(value: string) {
    this._status = value;
    this._node.fireChangeEvent('status', value);
  }

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
  }

  get bizTerm(): string {
    return this._bizTerm;
  }

  set bizTerm(value: string) {
    this._bizTerm = value;
    this._node.fireChangeEvent('bizTerm', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get path(): string {
    return this._node.abiePath;
  }

  get hashPath(): string {
    return this._node.abieHashPath;
  }

  get basedAccManifestId(): number {
    return this._node.accNode.manifestId;
  }

  update(obj?: AbieDetail) {
    if (obj) {
      this.ownerTopLevelAsbiepId = obj.ownerTopLevelAsbiepId;
      this.abieId = obj.abieId;
      this.guid = obj.guid;
      this.version = obj.version;
      this.status = obj.status;
      this.remark = obj.remark;
      this.bizTerm = obj.bizTerm;
      this.definition = obj.definition;
    }
  }

  get hashCode(): number {
    return hashCode4Array(
      this.abieId, this.guid,
      this.version, this.status, this.remark, this.bizTerm, this.definition
    );
  }

  get json(): any {
    return {
      abieId: this.abieId,
      guid: this.guid,
      version: this.version,
      status: this.status,
      remark: this.remark,
      bizTerm: this.bizTerm,
      definition: this.definition,
      basedAccManifestId: this.basedAccManifestId,
      path: this.path,
      hashPath: this.hashPath
    };
  }
}

export class AsbieDetail {
  private _node: AsbiepFlatNode;

  ownerTopLevelAsbiepId: number;
  asbieId: number;
  toAsbiepId: number;
  guid: string;
  seqKey: number;
  private _definition: string;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _nillable: boolean;
  private _remark: string;
  private _deprecated: boolean;

  constructor(node: AsbiepFlatNode) {
    this._node = node;
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
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

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get used(): boolean {
    return this._node.used as boolean;
  }

  get basedAsccManifestId(): number {
    return this._node.asccNode.manifestId;
  }

  get path(): string {
    return this._node.asbiePath;
  }

  get hashPath(): string {
    return this._node.asbieHashPath;
  }

  get fromAbiePath(): string {
    return (this._node.parent as AbieFlatNode).abiePath;
  }

  get fromAbieHashPath(): string {
    return (this._node.parent as AbieFlatNode).abieHashPath;
  }

  get toAsbiepPath(): string {
    return this._node.asbiepPath;
  }

  get toAsbiepHashPath(): string {
    return this._node.asbiepHashPath;
  }

  update(obj?: AsbieDetail) {
    if (obj) {
      this.ownerTopLevelAsbiepId = obj.ownerTopLevelAsbiepId;
      this.asbieId = obj.asbieId;
      this.toAsbiepId = obj.toAsbiepId;
      this.guid = obj.guid;
      this.definition = obj.definition;
      this.cardinalityMin = obj.cardinalityMin;
      this.cardinalityMax = obj.cardinalityMax;
      this.nillable = obj.nillable;
      this.remark = obj.remark;
      this.deprecated = obj.deprecated;
      this.seqKey = 0;
    }
  }

  get hashCode(): number {
    return hashCode4Array(
      this.asbieId, this.guid,
      this.definition, this.cardinalityMin, this.cardinalityMax,
      this.nillable, this.deprecated, this.remark,
      this.seqKey
    );
  }

  get json(): any {
    return {
      asbieId: this.asbieId,
      guid: this.guid,
      definition: this.definition,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      nillable: this.nillable,
      remark: this.remark,
      seqKey: this.seqKey,
      used: this.used,
      deprecated: this.deprecated,
      basedAsccManifestId: this.basedAsccManifestId,
      path: this.path,
      hashPath: this.hashPath,
      fromAbiePath: this.fromAbiePath,
      fromAbieHashPath: this.fromAbieHashPath,
      toAsbiepPath: this.toAsbiepPath,
      toAsbiepHashPath: this.toAsbiepHashPath
    };
  }
}

export class AsbiepDetail {
  private _node: AbieFlatNode | AsbiepFlatNode;

  ownerTopLevelAsbiepId: number;
  asbiepId: number;
  guid: string;
  roleOfAbieId: number;
  private _remark: string;
  private _bizTerm: string;
  private _definition: string;

  constructor(node: AbieFlatNode | AsbiepFlatNode) {
    this._node = node;
  }

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
  }

  get bizTerm(): string {
    return this._bizTerm;
  }

  set bizTerm(value: string) {
    this._bizTerm = value;
    this._node.fireChangeEvent('bizTerm', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get displayName(): string {
    return this._node.displayName;
  }

  set displayName(value: string) {
    this._node.displayName = value;
  }

  get basedAsccpManifestId(): number {
    return this._node.asccpNode.manifestId;
  }

  get path(): string {
    return this._node.asbiepPath;
  }

  get hashPath(): string {
    return this._node.asbiepHashPath;
  }

  get roleOfAbiePath(): string {
    return this._node.abiePath;
  }

  get roleOfAbieHashPath(): string {
    return this._node.abieHashPath;
  }

  update(obj?: AsbiepDetail) {
    if (obj) {
      this.ownerTopLevelAsbiepId = obj.ownerTopLevelAsbiepId;
      this.asbiepId = obj.asbiepId;
      this.guid = obj.guid;
      this.roleOfAbieId = obj.roleOfAbieId;
      this.remark = obj.remark;
      this.bizTerm = obj.bizTerm;
      this.definition = obj.definition;
      this.displayName = obj.displayName;
    }
  }

  get hashCode(): number {
    return hashCode4Array(
      this.asbiepId, this.guid, this.roleOfAbieId,
      this.remark, this.bizTerm, this.definition,
      ((!!this.displayName && this.displayName !== this._node.name) ? this.displayName : undefined)
    );
  }

  get json(): any {
    return {
      asbiepId: this.asbiepId,
      guid: this.guid,
      roleOfAbieId: this.roleOfAbieId,
      remark: this.remark,
      bizTerm: this.bizTerm,
      definition: this.definition,
      displayName: this.displayName,
      basedAsccpManifestId: this.basedAsccpManifestId,
      path: this.path,
      hashPath: this.hashPath,
      roleOfAbiePath: this.roleOfAbiePath,
      roleOfAbieHashPath: this.roleOfAbieHashPath
    };
  }
}

export class BbieDetail {
  private _node: BbiepFlatNode;

  ownerTopLevelAsbiepId: number;
  bbieId: number;
  guid: string;
  seqKey: number;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _facetMinLength: number;
  private _facetMaxLength: number;
  private _facetPattern: string;
  private _nillable: boolean;
  private _remark: string;
  private _definition: string;
  private _defaultValue: string;
  private _fixedValue: string;
  fixedOrDefault: string;
  private _example: string;
  private _deprecated: boolean;

  valueDomainType: string;
  private _bdtPriRestriId: number;
  private _codeListManifestId: number;
  private _agencyIdListManifestId: number;

  constructor(node: BbiepFlatNode) {
    this._node = node;
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

  get nillable(): boolean {
    return this._nillable;
  }

  set nillable(value: boolean) {
    this._nillable = value;
    this._node.fireChangeEvent('nillable', value);
  }

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
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

  get example(): string {
    return this._example;
  }

  set example(value: string) {
    this._example = value;
    this._node.fireChangeEvent('example', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get bdtPriRestriId(): number {
    return this._bdtPriRestriId;
  }

  set bdtPriRestriId(value: number) {
    this._bdtPriRestriId = value;
    this._node.fireChangeEvent('bdtPriRestriId', value);
  }

  get codeListManifestId(): number {
    return this._codeListManifestId;
  }

  set codeListManifestId(value: number) {
    this._codeListManifestId = value;
    this._node.fireChangeEvent('codeListManifestId', value);
  }

  get agencyIdListManifestId(): number {
    return this._agencyIdListManifestId;
  }

  set agencyIdListManifestId(value: number) {
    this._agencyIdListManifestId = value;
    this._node.fireChangeEvent('agencyIdListManifestId', value);
  }

  get used(): boolean {
    return this._node.used as boolean;
  }

  get basedBccManifestId(): number {
    return this._node.bccNode.manifestId;
  }

  get path(): string {
    return this._node.bbiePath;
  }

  get hashPath(): string {
    return this._node.bbieHashPath;
  }

  get fromAbiePath(): string {
    return (this._node.parent as AbieFlatNode).abiePath;
  }

  get fromAbieHashPath(): string {
    return (this._node.parent as AbieFlatNode).abieHashPath;
  }

  get toBbiepPath(): string {
    return this._node.bbiepPath;
  }

  get toBbiepHashPath(): string {
    return this._node.bbiepHashPath;
  }

  update(obj?: BbieDetail) {
    if (obj) {
      this.ownerTopLevelAsbiepId = obj.ownerTopLevelAsbiepId;
      this.bbieId = obj.bbieId;
      this.guid = obj.guid;
      this.remark = obj.remark;
      this.definition = obj.definition;
      this.cardinalityMin = obj.cardinalityMin;
      this.cardinalityMax = obj.cardinalityMax;
      this.facetMinLength = obj.facetMinLength;
      this.facetMaxLength = obj.facetMaxLength;
      this.facetPattern = obj.facetPattern;
      this.definition = obj.definition;
      this.deprecated = obj.deprecated;
      this.nillable = obj.nillable;
      this.remark = obj.remark;
      if (!(obj.defaultValue == null && obj.fixedValue == null)) {
        this.defaultValue = obj.defaultValue;
        this.fixedValue = obj.fixedValue;
      }
      this.example = obj.example;
      if (obj.agencyIdListManifestId) {
        this.agencyIdListManifestId = obj.agencyIdListManifestId;
        this.valueDomainType = 'Agency';
      } else if (obj.codeListManifestId) {
        this.codeListManifestId = obj.codeListManifestId;
        this.valueDomainType = 'Code';
      } else {
        this.bdtPriRestriId = obj.bdtPriRestriId;
        this.valueDomainType = 'Primitive';
      }
      this.seqKey = 0;
    }
  }

  get hashCode(): number {
    return hashCode4Array(
      this.bbieId, this.guid, this.definition, this.cardinalityMin, this.cardinalityMax,
      this.facetMinLength, this.facetMaxLength, this.facetPattern,
      this.nillable, this.deprecated,
      this.remark, this.example, this.defaultValue, this.fixedValue,
      this.bdtPriRestriId, this.codeListManifestId, this.agencyIdListManifestId,
      this.seqKey
    );
  }

  get json(): any {
    return {
      bbieId: this.bbieId,
      guid: this.guid,
      definition: this.definition,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      facetMinLength: this.facetMinLength,
      facetMaxLength: this.facetMaxLength,
      facetPattern: this.facetPattern,
      nillable: this.nillable,
      deprecated: this.deprecated,
      remark: this.remark,
      example: this.example,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      bdtPriRestriId: this.bdtPriRestriId,
      codeListManifestId: this.codeListManifestId,
      agencyIdListManifestId: this.agencyIdListManifestId,
      seqKey: this.seqKey,
      used: this.used,
      basedBccManifestId: this.basedBccManifestId,
      path: this.path,
      hashPath: this.hashPath,
      fromAbiePath: this.fromAbiePath,
      fromAbieHashPath: this.fromAbieHashPath,
      toBbiepPath: this.toBbiepPath,
      toBbiepHashPath: this.toBbiepHashPath
    };
  }
}

export class BbiepDetail {
  private _node: BbiepFlatNode;

  ownerTopLevelAsbiepId: number;
  bbiepId: number;
  guid: string;
  private _remark: string;
  private _bizTerm: string;
  private _definition: string;

  constructor(node: BbiepFlatNode) {
    this._node = node;
  }

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
  }

  get bizTerm(): string {
    return this._bizTerm;
  }

  set bizTerm(value: string) {
    this._bizTerm = value;
    this._node.fireChangeEvent('bizTerm', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get displayName(): string {
    return this._node.displayName;
  }

  set displayName(value: string) {
    this._node.displayName = value;
  }

  get path(): string {
    return this._node.bbiepPath;
  }

  get hashPath(): string {
    return this._node.bbiepHashPath;
  }

  get basedBccpManifestId(): number {
    return this._node.bccpNode.manifestId;
  }

  update(obj?: BbiepDetail) {
    if (obj) {
      this.ownerTopLevelAsbiepId = obj.ownerTopLevelAsbiepId;
      this.bbiepId = obj.bbiepId;
      this.guid = obj.guid;
      this.definition = obj.definition;
      this.remark = obj.remark;
      this.bizTerm = obj.bizTerm;
      this.displayName = obj.displayName;
    }
  }

  get hashCode(): number {
    return hashCode4Array(
      this.bbiepId, this.guid, this.definition, this.remark, this.bizTerm,
      ((!!this.displayName && this.displayName !== this._node.name) ? this.displayName : undefined)
    );
  }

  get json(): any {
    return {
      bbiepId: this.bbiepId,
      guid: this.guid,
      definition: this.definition,
      remark: this.remark,
      bizTerm: this.bizTerm,
      displayName: this.displayName,
      basedBccpManifestId: this.basedBccpManifestId,
      path: this.path,
      hashPath: this.hashPath
    };
  }
}

export class BbieScDetail {
  private _node: BbieScFlatNode;

  ownerTopLevelAsbiepId: number;
  bbieScId: number;
  guid: string;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _facetMinLength: number;
  private _facetMaxLength: number;
  private _facetPattern: string;
  private _remark: string;
  private _bizTerm: string;
  private _definition: string;
  private _defaultValue: string;
  private _fixedValue: string;
  fixedOrDefault: string;
  private _example: string;
  private _deprecated: boolean;

  valueDomainType: string;
  private _bdtScPriRestriId: number;
  private _codeListManifestId: number;
  private _agencyIdListManifestId: number;

  constructor(node: BbieScFlatNode) {
    this._node = node;
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

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
  }

  get bizTerm(): string {
    return this._bizTerm;
  }

  set bizTerm(value: string) {
    this._bizTerm = value;
    this._node.fireChangeEvent('bizTerm', value);
  }

  get definition(): string {
    return this._definition;
  }

  set definition(value: string) {
    this._definition = value;
    this._node.fireChangeEvent('definition', value);
  }

  get displayName(): string {
    return this._node.displayName;
  }

  set displayName(value: string) {
    this._node.displayName = value;
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

  get example(): string {
    return this._example;
  }

  set example(value: string) {
    this._example = value;
    this._node.fireChangeEvent('example', value);
  }

  get deprecated(): boolean {
    return this._deprecated;
  }

  set deprecated(value: boolean) {
    this._deprecated = value;
    this._node.deprecated = value;
    this._node.fireChangeEvent('deprecated', value);
  }

  get bdtScPriRestriId(): number {
    return this._bdtScPriRestriId;
  }

  set bdtScPriRestriId(value: number) {
    this._bdtScPriRestriId = value;
    this._node.fireChangeEvent('bdtScPriRestriId', value);
  }

  get codeListManifestId(): number {
    return this._codeListManifestId;
  }

  set codeListManifestId(value: number) {
    this._codeListManifestId = value;
    this._node.fireChangeEvent('codeListManifestId', value);
  }

  get agencyIdListManifestId(): number {
    return this._agencyIdListManifestId;
  }

  set agencyIdListManifestId(value: number) {
    this._agencyIdListManifestId = value;
    this._node.fireChangeEvent('agencyIdListManifestId', value);
  }

  get used(): boolean {
    return this._node.used as boolean;
  }

  get basedDtScManifestId(): number {
    return this._node.bdtScNode.manifestId;
  }

  get path(): string {
    return this._node.bbieScPath;
  }

  get hashPath(): string {
    return this._node.bbieScHashPath;
  }

  get bbieHashPath(): string {
    return (this._node.parent as BbiepFlatNode).bbieHashPath;
  }

  update(obj?: BbieScDetail) {
    if (obj) {
      this.ownerTopLevelAsbiepId = obj.ownerTopLevelAsbiepId;
      this.bbieScId = obj.bbieScId;
      this.guid = obj.guid;
      this.cardinalityMin = obj.cardinalityMin;
      this.cardinalityMax = obj.cardinalityMax;
      this.facetMinLength = obj.facetMinLength;
      this.facetMaxLength = obj.facetMaxLength;
      this.facetPattern = obj.facetPattern;

      this.definition = obj.definition;
      this.bizTerm = obj.bizTerm;
      this.remark = obj.remark;
      this.displayName = obj.displayName;
      this.defaultValue = obj.defaultValue;
      this.fixedValue = obj.fixedValue;
      this.example = obj.example;
      this.deprecated = obj.deprecated;
      if (obj.agencyIdListManifestId) {
        this.agencyIdListManifestId = obj.agencyIdListManifestId;
        this.valueDomainType = 'Agency';
      } else if (obj.codeListManifestId) {
        this.codeListManifestId = obj.codeListManifestId;
        this.valueDomainType = 'Code';
      } else {
        this.bdtScPriRestriId = obj.bdtScPriRestriId;
        this.valueDomainType = 'Primitive';
      }
    }
  }

  get hashCode(): number {
    return hashCode4Array(
      this.bbieScId, this.guid, this.definition, this.cardinalityMin, this.cardinalityMax,
      this.facetMinLength, this.facetMaxLength, this.facetPattern,
      this.bizTerm, this.remark,
      ((!!this.displayName && this.displayName !== this._node.name) ? this.displayName : undefined),
      this.example, this.deprecated, this.defaultValue, this.fixedValue,
      this.bdtScPriRestriId, this.codeListManifestId, this.agencyIdListManifestId
    );
  }

  get json(): any {
    return {
      bbieScId: this.bbieScId,
      guid: this.guid,
      definition: this.definition,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      facetMinLength: this.facetMinLength,
      facetMaxLength: this.facetMaxLength,
      facetPattern: this.facetPattern,
      bizTerm: this.bizTerm,
      remark: this.remark,
      displayName: this.displayName,
      example: this.example,
      deprecated: this.deprecated,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      bdtScPriRestriId: this.bdtScPriRestriId,
      codeListManifestId: this.codeListManifestId,
      agencyIdListManifestId: this.agencyIdListManifestId,
      used: this.used,
      basedDtScManifestId: this.basedDtScManifestId,
      path: this.path,
      hashPath: this.hashPath,
      bbieHashPath: this.bbieHashPath
    };
  }
}

export abstract class BieEditNodeDetail {
  $hashCode: number;
  isLoaded: boolean;

  get isChanged() {
    return this.$hashCode !== this.hashCode;
  }

  get isValid() {
    return false;
  }

  reset() {
    this.$hashCode = this.hashCode;
  }

  abstract get hashCode(): number;
}

export class BieEditAbieNodeDetail extends BieEditNodeDetail {
  private _node: BieFlatNode;

  acc: AccDetail;
  abie: AbieDetail;
  asccp: AsccpDetail;
  asbiep: AsbiepDetail;

  base?: BieEditAbieNodeDetail;

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.abie = new AbieDetail(node as AbieFlatNode);
    this.asbiep = new AsbiepDetail(node as AbieFlatNode);
    this.reset();
  }

  get hashCode(): number {
    return this.asbiep.hashCode + this.abie.hashCode;
  }

  get isValid() {
    return true;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }

}

export class BieEditAsbiepNodeDetail extends BieEditNodeDetail {
  private _node: BieFlatNode;

  acc: AccDetail;
  abie: AbieDetail;
  ascc: AsccDetail;
  asbie: AsbieDetail;
  asccp: AsccpDetail;
  asbiep: AsbiepDetail;

  base?: BieEditAsbiepNodeDetail;

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.abie = new AbieDetail(node as AsbiepFlatNode);
    this.asbie = new AsbieDetail(node as AsbiepFlatNode);
    this.asbiep = new AsbiepDetail(node as AsbiepFlatNode);
    this.asbie.seqKey = 0;
    this.asbie.deprecated = node.deprecated;
    this.reset();
  }

  get hashCode(): number {
    return this.asbie.hashCode + this.asbiep.hashCode + this.abie.hashCode;
  }

  get isValid() {
    return true;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }
}

export class BieEditBbiepNodeDetail extends BieEditNodeDetail {
  private _node: BieFlatNode;

  bcc: BccDetail;
  bccp: BccpDetail;
  bbie: BbieDetail;
  bbiep: BbiepDetail;
  bdt: BdtDetail;

  base?: BieEditBbiepNodeDetail;

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.bbie = new BbieDetail(node as BbiepFlatNode);
    this.bbiep = new BbiepDetail(node as BbiepFlatNode);
    this.bdt = new BdtDetail(node as BbiepFlatNode);
    this.bbie.seqKey = 0;
    this.bbie.deprecated = node.deprecated;
    this.reset();
  }

  get hashCode(): number {
    return this.bbie.hashCode + this.bbiep.hashCode;
  }

  get isValid() {
    if (!(this.bbie.bdtPriRestriId || this.bbie.codeListManifestId || this.bbie.agencyIdListManifestId)) {
      return false;
    }
    return true;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }
}

export class BieEditBbieScNodeDetail extends BieEditNodeDetail {
  private _node: BieFlatNode;

  bbieSc: BbieScDetail;
  bdtSc: BdtScDetail;
  bdt: BdtDetail;

  base?: BieEditBbieScNodeDetail;

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.bbieSc = new BbieScDetail(node as BbieScFlatNode);
    this.bdt = new BdtDetail(node as BbieScFlatNode);
    this.bbieSc.deprecated = node.deprecated;
    this.reset();
  }

  get hashCode(): number {
    return this.bbieSc.hashCode;
  }

  get isValid() {
    if (!(this.bbieSc.bdtScPriRestriId || this.bbieSc.codeListManifestId || this.bbieSc.agencyIdListManifestId)) {
      return false;
    }
    return true;
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

export class BiePathLikeExpressionEvaluator<T extends BieFlatNode> extends PathLikeExpressionEvaluator<T> {
  protected next(node: T): T {
    let next = node.parent as T;
    while (next && next.isGroup) {
      next = next.parent as T;
    }
    return next;
  }

  protected doEval(node: T, token: string): boolean {
    const result = super.doEval(node, token);
    if (result || !node.displayName) {
      return result;
    }
    if (this.caseSensitive) {
      return node.displayName.indexOf(token) > -1;
    } else {
      return node.displayName.toLowerCase().indexOf(token.toLowerCase()) > -1;
    }
  }
}

export class BieFlatNodeDatabase<T extends BieFlatNode> {

  private _ccGraph: CcGraph;
  private _topLevelAsbiepId: number;
  private _abieNode: BieEditAbieNode;
  private _validState: string[];

  private _usedAsbieMap = {};
  private _usedBbieMap = {};
  private _usedBbieScMap = {};

  private _baseUsedAsbieMap = {};
  private _baseUsedBbieMap = {};
  private _baseUsedBbieScMap = {};

  private _refBieList: RefBie[] = [];

  dataSource: BieFlatNodeDataSource<T>;

  constructor(ccGraph: CcGraph, abieNode: BieEditAbieNode, topLevelAsbiepId: number,
              usedBieList: UsedBie[], refBieList: RefBie[]) {
    this._ccGraph = ccGraph;
    this._abieNode = abieNode;
    this._topLevelAsbiepId = topLevelAsbiepId;
    this._validState = ['Published', 'Production'];

    this.setUsedBieList(usedBieList);
    this._refBieList = refBieList;
  }

  setUsedBieList(usedBieList: UsedBie[]) {
    this._usedAsbieMap = usedBieList.filter(e => e.type === 'ASBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._usedBbieMap = usedBieList.filter(e => e.type === 'BBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._usedBbieScMap = usedBieList.filter(e => e.type === 'BBIE_SC').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
  }

  setBaseUsedBieList(baseUsedBieList: UsedBie[]) {
    this._baseUsedAsbieMap = baseUsedBieList.filter(e => e.type === 'ASBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._baseUsedBbieMap = baseUsedBieList.filter(e => e.type === 'BBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._baseUsedBbieScMap = baseUsedBieList.filter(e => e.type === 'BBIE_SC').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
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
        if ((_node as AsbiepFlatNode).accNode.componentType === 'AttributeGroup') {
          attributes.push(...this.children(e));
        } else {
          nodes.push(...this.children(e));
        }
      } else {
        if (_node instanceof BbiepFlatNode && (_node as BbiepFlatNode).bccNode.entityType === 'Attribute') {
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
    const nodeBieType = node.bieType;
    const _node = node.self; // in case of it is WrappedBieFlatNode
    if (nodeBieType === 'ABIE' || nodeBieType === 'ASBIEP') {
      children = this.getAssociations((_node as AsbiepFlatNode).accNode);
      node.children = children.map((e: Association) => {
        if (e.assocNode.type === 'ASCC') {
          const asbiepNode: AsbiepFlatNode = this.toAsbiepNode(e, _node as AsbiepFlatNode);
          this.afterAsbiepFlatNode(asbiepNode);
          asbiepNode.reset();
          return asbiepNode;
        } else {
          const bbiepNode: BbiepFlatNode = this.toBbiepNode(e, _node as AsbiepFlatNode);
          this.afterBbiepFlatNode(bbiepNode);
          bbiepNode.reset();
          return bbiepNode;
        }
      });

      node.children.map(e => e as T).forEach(e => {
        if (e.isGroup) {
          this.loadChildren(e);
        }
      });
    } else if (nodeBieType === 'BBIEP') {
      children = this.getChildren((_node as BbiepFlatNode).bdtNode);
      node.children = children.map(e => {
        const bbieScNode = this.toBbieScNode((_node as BbiepFlatNode).bccNode, e, node);
        this.afterBbieScFlatNode(bbieScNode);
        bbieScNode.reset();
        return bbieScNode;
      }).sort((a, b) => a.name.localeCompare(b.name));
    }
  }

  afterAsbiepFlatNode(node: AsbiepFlatNode) {
    let reused = this._refBieList.filter(u => u.basedAsccManifestId === node.asccNode.manifestId);
    if (!!reused && reused.length > 0) {
      reused = reused.filter(u => u.hashPath === node.asbieHashPath);
    }
    node.reused = !!reused && reused.length > 0;
    if (node.reused) {
      node.topLevelAsbiepId = reused[0].refTopLevelAsbiepId;
      /*
       * If the Reuse BIE has a Base BIE, it retrieves the Base of the Reuse BIE;
       * otherwise, it retrieves the Base of the current BIE.
       */
      node.basedTopLevelAsbiepId = (reused[0].refBasedTopLevelAsbiepId ?
        reused[0].refBasedTopLevelAsbiepId : reused[0].basedTopLevelAsbiepId);
      node.rootNode = new BieEditAbieNode();
      node.rootNode.topLevelAsbiepId = reused[0].refTopLevelAsbiepId;
      node.rootNode.basedTopLevelAsbiepId = (reused[0].refBasedTopLevelAsbiepId ?
        reused[0].refBasedTopLevelAsbiepId : reused[0].basedTopLevelAsbiepId);
      node.rootNode.inverseMode = reused[0].refInverseMode;
    }

    // Check if it's reused.
    let used = this._usedAsbieMap[node.asccNode.manifestId];
    if (!!used && used.length > 0) {
      used = used.filter(u => {
        if (node.reused) {
          return u.ownerTopLevelAsbiepId === (node.parent as AbieFlatNode).topLevelAsbiepId &&
            u.hashPath === node.asbieHashPath;
        } else {
          return u.ownerTopLevelAsbiepId === node.topLevelAsbiepId &&
            u.hashPath === node.asbieHashPath;
        }
      });
      if (!!used && used.length > 0) {
        node.bieId = used[0].bieId;
        node.used = used[0].used;
        node.displayName = used[0].displayName;
        node.cardinalityMin = used[0].cardinalityMin;
        node.cardinalityMax = used[0].cardinalityMax;
        node.deprecated = node.ccDeprecated || used[0].deprecated;
      }
    }

    // Check if it's inherited
    let baseUsed = this._baseUsedAsbieMap[node.asccNode.manifestId];
    if (!!baseUsed && baseUsed.length > 0) {
      baseUsed = baseUsed.filter(u => {
        if (node.reused) {
          return u.ownerTopLevelAsbiepId === (node.parent as AbieFlatNode).basedTopLevelAsbiepId &&
            u.hashPath === node.asbieHashPath;
        } else {
          return u.ownerTopLevelAsbiepId === node.basedTopLevelAsbiepId &&
            u.hashPath === node.asbieHashPath;
        }
      });
      if (!!baseUsed && baseUsed.length > 0 && baseUsed[0].used) {
        node.inherited = true;
      }
    }

    if (!node.rootNode) {
      node.rootNode = node.parent.rootNode;
    }
  }

  afterBbiepFlatNode(node: BbiepFlatNode) {
    let used = this._usedBbieMap[node.bccNode.manifestId];
    if (!!used && used.length > 0) {
      used = used.filter(u => u.ownerTopLevelAsbiepId === node.topLevelAsbiepId && u.hashPath === node.bbieHashPath);

      if (!!used && used.length > 0) {
        node.bieId = used[0].bieId;
        node.used = used[0].used;
        node.displayName = used[0].displayName;
        node.cardinalityMin = used[0].cardinalityMin;
        node.cardinalityMax = used[0].cardinalityMax;
        node.deprecated = node.ccDeprecated || used[0].deprecated;
      }
    }

    // Check if it's inherited
    let baseUsed = this._baseUsedBbieMap[node.bccNode.manifestId];
    if (!!baseUsed && baseUsed.length > 0) {
      baseUsed = baseUsed.filter(u => u.ownerTopLevelAsbiepId === node.basedTopLevelAsbiepId && u.hashPath === node.bbieHashPath);
      if (!!baseUsed && baseUsed.length > 0 && baseUsed[0].used) {
        node.inherited = true;
      }
    }

    if (!node.rootNode) {
      node.rootNode = node.parent.rootNode;
    }
  }

  afterBbieScFlatNode(node: BbieScFlatNode) {
    let used = this._usedBbieScMap[node.bdtScNode.manifestId];
    if (!!used && used.length > 0) {
      used = used.filter(u => u.ownerTopLevelAsbiepId === node.topLevelAsbiepId && u.hashPath === node.hashPath);

      if (!!used && used.length > 0) {
        node.bieId = used[0].bieId;
        node.used = used[0].used;
        node.displayName = used[0].displayName;
        node.cardinalityMin = used[0].cardinalityMin;
        node.cardinalityMax = used[0].cardinalityMax;
        node.deprecated = node.ccDeprecated || used[0].deprecated;
      }
    }

    // Check if it's inherited
    let baseUsed = this._baseUsedBbieScMap[node.bdtScNode.manifestId];
    if (!!baseUsed && baseUsed.length > 0) {
      baseUsed = baseUsed.filter(u => u.ownerTopLevelAsbiepId === node.basedTopLevelAsbiepId && u.hashPath === node.hashPath);
      if (!!baseUsed && baseUsed.length > 0 && baseUsed[0].used) {
        node.inherited = true;
      }
    }

    if (!node.rootNode) {
      node.rootNode = (node.parent as BieFlatNodeImpl).rootNode;
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
        return [nodes[targets[0]], ];

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
    const node = this.toAbieNode('ASCCP-' + this._abieNode.asccpManifestId);
    node.topLevelAsbiepId = this._topLevelAsbiepId;
    node.basedTopLevelAsbiepId = this._abieNode.basedTopLevelAsbiepId;
    node.rootNode = this._abieNode;
    node.deprecated = this._abieNode.deprecated;
    node.inherited = (!!node.basedTopLevelAsbiepId);
    node.displayName = this._abieNode.displayName;
    return node as unknown as T;
  }

  toAbieNode(key: string): AbieFlatNode {
    const node = new AbieFlatNode();
    node.asccpNode = this._ccGraph.graph.nodes[key];
    node.accNode = this.getChildren(node.asccpNode)[0];
    node.name = node.asccpNode.propertyTerm;
    node.level = 0;
    node.used = true;
    node.required = true;
    node.deprecated = node.asccpNode.deprecated || node.accNode.deprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  toAsbiepNode(ascc: Association, parent: AsbiepFlatNode) {
    const node = new AsbiepFlatNode();
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
    const usable = this._validState.indexOf(parent.accNode.state) > -1
      && this._validState.indexOf(node.asccNode.state) > -1
      && this._validState.indexOf(node.asccpNode.state) > -1
      && this._validState.indexOf(node.accNode.state) > -1;
    if (parent.reused || !usable) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.topLevelAsbiepId = parent.topLevelAsbiepId;
    node.basedTopLevelAsbiepId = parent.basedTopLevelAsbiepId;
    if (!node.isGroup) {
      node.isCycle = this.detectCycle(node);
    }
    node.deprecated = node.ccDeprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  toBbiepNode(bcc: Association, parent: AsbiepFlatNode) {
    const node = new BbiepFlatNode();
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
    const usable = this._validState.indexOf(parent.accNode.state) > -1
      && this._validState.indexOf(node.bccNode.state) > -1
      && this._validState.indexOf(node.bccpNode.state) > -1
      && this._validState.indexOf(node.bdtNode.state) > -1;
    if (parent.reused || !usable) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.topLevelAsbiepId = parent.topLevelAsbiepId;
    node.basedTopLevelAsbiepId = parent.basedTopLevelAsbiepId;
    node.deprecated = node.ccDeprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  toBbieScNode(bccNode: CcGraphNode, bdtScNode: CcGraphNode, parent: T) {
    const node = new BbieScFlatNode();
    node.bccNode = bccNode;
    node.bdtScNode = this._ccGraph.graph.nodes[getKey(bdtScNode)];
    const bccpNode = this.getChildren(node.bccNode)[0];
    node.bdtNode = this.getChildren(bccpNode)[0];
    node.required = node.bdtScNode.cardinalityMin > 0;
    node.name = node.bdtScNode.propertyTerm + ' ' + node.bdtScNode.representationTerm;
    node.level = parent.level + 1;
    node.parent = parent;
    const usable = this._validState.indexOf(node.bccNode.state) > -1
      && this._validState.indexOf(node.bdtScNode.state) > -1;
    if (parent.reused || !usable) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.topLevelAsbiepId = parent.topLevelAsbiepId;
    node.basedTopLevelAsbiepId = parent.basedTopLevelAsbiepId;
    node.deprecated = node.bdtScNode.deprecated;
    node.dataSource = this.dataSource;
    return node;
  }

  detectCycle(node: AsbiepFlatNode): boolean {
    const asccpManifestId = node.asccpNode.manifestId;
    let cur = node.parent;
    while (cur) {
      if ((cur as AbieFlatNode).asccpNode.manifestId === asccpManifestId) {
        return true;
      }
      cur = cur.parent as BieFlatNode;
    }
    return false;
  }
}

export class BieFlatNodeDataSource<T extends BieFlatNode> implements DataSource<T>, ChangeListener<T> {

  dataChange = new BehaviorSubject<T[]>([]);
  _listeners: ChangeListener<BieFlatNodeDataSource<T>>[] = [];

  _hideUnused = false;
  _hideCardinality = false;

  get data(): T[] {
    return this.dataChange.value;
  }

  set data(value: T[]) {
    value.forEach(e => e.addChangeListener(this));
    this.dataChange.next(value);
  }

  addListener(listener: ChangeListener<BieFlatNodeDataSource<T>>) {
    if (!!listener && this._listeners.indexOf(listener) === -1) {
      this._listeners.push(listener);
    }
  }

  init() {
    this.data = [this._database.rootNode as unknown as T, ];

    // pre-expanding nodes to recognize required elements.
    let nodes = [this.data[0], ];
    while (nodes.length > 0) {
      const node = nodes.shift();
      if (node.required && node.expandable && node.children.length === 0) {
        this._database.loadChildren(node);
      }
      nodes = node.children.concat(nodes) as T[];
    }
  }

  update(updateResponse: BieDetailUpdateResponse) {
    this.data.forEach(node => {
      if (node.bieType === 'ABIE') {
        const abieNode = node as unknown as AbieFlatNode;
        const abieDetail = updateResponse.abieDetailMap[node.hashPath];
        if (abieDetail) {
          abieNode.abieId = abieDetail.abieId;
          if (abieNode.detail) {
            (abieNode.detail as BieEditAbieNodeDetail).abie.abieId = abieDetail.abieId;
          }
        }
      } else if (node.bieType === 'ASBIEP') {
        const asbiepNode = node as unknown as AsbiepFlatNode;
        const asbieDetail = updateResponse.asbieDetailMap[asbiepNode.asbieHashPath];
        if (asbieDetail) {
          asbiepNode.asbieId = asbieDetail.asbieId;
          if (asbiepNode.detail) {
            (asbiepNode.detail as BieEditAsbiepNodeDetail).asbie.asbieId = asbieDetail.asbieId;
          }
        }
        const asbiepDetail = updateResponse.asbiepDetailMap[asbiepNode.asbiepHashPath];
        if (asbiepDetail) {
          asbiepNode.asbiepId = asbieDetail.asbiepId;
          if (asbiepNode.detail) {
            (asbiepNode.detail as BieEditAsbiepNodeDetail).asbiep.asbiepId = asbieDetail.asbiepId;
          }
        }
      } else if (node.bieType === 'BBIEP') {
        const bbiepNode = node as unknown as BbiepFlatNode;
        const bbieDetail = updateResponse.bbieDetailMap[bbiepNode.bbieHashPath];
        if (bbieDetail) {
          bbiepNode.bbieId = bbieDetail.bbieId;
          if (bbiepNode.detail) {
            (bbiepNode.detail as BieEditBbiepNodeDetail).bbie.bbieId = bbieDetail.bbieId;
          }
        }
        const bbiepDetail = updateResponse.bbiepDetailMap[bbiepNode.bbiepHashPath];
        if (bbiepDetail) {
          bbiepNode.bbiepId = bbieDetail.bbiepId;
          if (bbiepNode.detail) {
            (bbiepNode.detail as BieEditBbiepNodeDetail).bbiep.bbiepId = bbieDetail.bbiepId;
          }
        }
      } else if (node.bieType === 'BBIE_SC') {
        const bbieScNode = node as unknown as BbieScFlatNode;
        const bbieScDetail = updateResponse.bbieScDetailMap[bbieScNode.bbieScHashPath];
        if (bbieScDetail) {
          bbieScNode.bbieScId = bbieScDetail.bbieScId;
          if (bbieScNode.detail) {
            (bbieScNode.detail as BieEditBbieScNodeDetail).bbieSc.bbieScId = bbieScDetail.bbieScId;
          }
        }
      }
    });
  }

  getChanged(): T[] {
    if (!this.data || this.data.length === 0) {
      return [];
    }

    let nodes = [this.data[0], ];
    const changedNodes = [];
    while (nodes.length > 0) {
      const node = nodes.shift();
      if (!node.isGroup && node.isChanged) {
        changedNodes.push(node);
      }
      const children = node.getChildren();
      if (children && children.length > 0) {
        nodes = children.concat(nodes) as T[];
      }
    }
    return changedNodes.filter(e => {
      if (!e.inverseMode && !e.bieId && !e.used) {
        return false;
      }
      return true;
    });
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

  get hideUnused(): boolean {
    return this._hideUnused;
  }

  set hideUnused(hideUnused: boolean) {
    if (this._hideUnused === hideUnused) {
      return;
    }

    this._hideUnused = hideUnused;
    this.data.forEach(e => {
      e.expandable = undefined;
    });
    if (hideUnused) {
      this.data = this.data.filter(e => e.used);
    } else {
      const expandedData = this.data.filter(e => this.isExpanded(e));
      this.collapse(this.data[0] as T);
      this.data = [this.data[0], ];

      for (const item of expandedData) {
        this.expand(item as T);
      }
    }

    this._listeners.forEach(e => e.onChange(this, 'hideUnused', hideUnused));
  }

  constructor(
    private _database: BieFlatNodeDatabase<T>,
    private service: BieEditService,
    private delegatedListeners?: ChangeListener<T>[]
  ) {
    _database.dataSource = this;
  }

  get database(): BieFlatNodeDatabase<T> {
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
    if (this.hideUnused) {
      children = children.filter(e => e.used);
    }
    let index;
    // Issue #1486
    // It shouldn't use 'hashPath' to handle with the case of two BIEs referred to the same reused BIE.
    if (node.reused) {
      const asbiepNode = node as unknown as AsbiepFlatNode;
      index = this.data.map(e => {
        if (e.bieType !== 'ASBIEP') {
          return '';
        }
        return (e as unknown as AsbiepFlatNode).asbiePath;
      }).indexOf(asbiepNode.asbiePath);
    } else {
      index = this.data.map(e => e.hashPath).indexOf(node.hashPath);
    }

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

      // Too many nodes in BIE tree.
      if (node.bieType === 'BBIEP') {
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

    switch (node.bieType.toUpperCase()) {
      case 'ABIE':
        const abieNode = (node as unknown as AbieFlatNode);
        const abieRequests = [
          this.service.getDetail(node.topLevelAsbiepId, 'ABIE',
            abieNode.accNode.manifestId, abieNode.abiePath),
          this.service.getDetail(node.topLevelAsbiepId, 'ASBIEP',
            abieNode.asccpNode.manifestId, abieNode.asbiepPath)
        ];
        if (node.inherited) {
          abieRequests.push(...[
            this.service.getDetail(node.basedTopLevelAsbiepId, 'ABIE',
              abieNode.accNode.manifestId, abieNode.abiePath),
            this.service.getDetail(node.basedTopLevelAsbiepId, 'ASBIEP',
              abieNode.asccpNode.manifestId, abieNode.asbiepPath)
          ]);
        }

        forkJoin(abieRequests).subscribe((
          [abieDetail, asbiepDetail, basedAbieDetail, basedAsbiepDetail]) => {
          (node.detail as BieEditAbieNodeDetail).acc = (abieDetail as unknown as BieEditAbieNodeDetail).acc;
          (node.detail as BieEditAbieNodeDetail).abie.update((abieDetail as unknown as BieEditAbieNodeDetail).abie);
          (node.detail as BieEditAbieNodeDetail).asccp = (asbiepDetail as unknown as BieEditAbieNodeDetail).asccp;
          (node.detail as BieEditAbieNodeDetail).asbiep.update((asbiepDetail as unknown as BieEditAbieNodeDetail).asbiep);

          if (basedAbieDetail && basedAsbiepDetail) {
            const basedNode = Object.create(node);

            (node.detail as BieEditAbieNodeDetail).base = new BieEditAbieNodeDetail(basedNode);
            (node.detail as BieEditAbieNodeDetail).base.acc = (basedAbieDetail as unknown as BieEditAbieNodeDetail).acc;
            (node.detail as BieEditAbieNodeDetail).base.abie.update((basedAbieDetail as unknown as BieEditAbieNodeDetail).abie);
            (node.detail as BieEditAbieNodeDetail).base.asccp = (basedAsbiepDetail as unknown as BieEditAbieNodeDetail).asccp;
            (node.detail as BieEditAbieNodeDetail).base.asbiep.update((basedAsbiepDetail as unknown as BieEditAbieNodeDetail).asbiep);
          }

          (node.detail as BieEditAbieNodeDetail).reset();
          node.detail.isLoaded = true;
          return callbackFn && callbackFn(node);
        });
        break;

      case 'ASBIEP':
        const asbiepNode = (node as unknown as AsbiepFlatNode);

        const doAfterAsbiep = (asbieDetail, asbiepDetail, abieDetail, basedAsbieDetail?, basedAsbiepDetail?, basedAbieDetail?) => {
          const storedCardinalityMax = (node.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax;
          const storedDeprecated = (node.detail as BieEditAsbiepNodeDetail).asbie.deprecated;
          (node.detail as BieEditAsbiepNodeDetail).ascc = (asbieDetail as unknown as BieEditAsbiepNodeDetail).ascc;
          (node.detail as BieEditAsbiepNodeDetail).asbie.update((asbieDetail as unknown as BieEditAsbiepNodeDetail).asbie);
          (node.detail as BieEditAsbiepNodeDetail).asccp = (asbiepDetail as unknown as BieEditAsbiepNodeDetail).asccp;
          (node.detail as BieEditAsbiepNodeDetail).asbiep.update((asbiepDetail as unknown as BieEditAsbiepNodeDetail).asbiep);
          (node.detail as BieEditAsbiepNodeDetail).acc = (abieDetail as unknown as BieEditAsbiepNodeDetail).acc;
          (node.detail as BieEditAsbiepNodeDetail).abie.update((abieDetail as unknown as BieEditAsbiepNodeDetail).abie);

          if (basedAsbieDetail && basedAsbiepDetail && basedAbieDetail) {
            const basedNode = Object.create(node);

            (node.detail as BieEditAsbiepNodeDetail).base = new BieEditAsbiepNodeDetail(basedNode);
            (node.detail as BieEditAsbiepNodeDetail).base.ascc = (basedAsbieDetail as unknown as BieEditAsbiepNodeDetail).ascc;
            (node.detail as BieEditAsbiepNodeDetail).base.asbie.update((basedAsbieDetail as unknown as BieEditAsbiepNodeDetail).asbie);
            (node.detail as BieEditAsbiepNodeDetail).base.asccp = (basedAsbiepDetail as unknown as BieEditAsbiepNodeDetail).asccp;
            (node.detail as BieEditAsbiepNodeDetail).base.asbiep.update((basedAsbiepDetail as unknown as BieEditAsbiepNodeDetail).asbiep);
            (node.detail as BieEditAsbiepNodeDetail).base.acc = (basedAbieDetail as unknown as BieEditAsbiepNodeDetail).acc;
            (node.detail as BieEditAsbiepNodeDetail).base.abie.update((basedAbieDetail as unknown as BieEditAsbiepNodeDetail).abie);
          }

          (node.detail as BieEditAsbiepNodeDetail).reset();
          node.detail.isLoaded = true;
          if (storedCardinalityMax !== undefined) {
            (node.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax = storedCardinalityMax;
          }
          if (storedDeprecated) {
            (node.detail as BieEditAsbiepNodeDetail).asbie.deprecated = storedDeprecated;
          }

          return callbackFn && callbackFn(node);
        };

        const asbiepRequests = [
          this.service.getDetail((node.reused) ? (node.parent as AsbiepFlatNode).topLevelAsbiepId : node.topLevelAsbiepId, 'ASBIE',
            asbiepNode.asccNode.manifestId, asbiepNode.asbiePath),
          this.service.getDetail(node.topLevelAsbiepId, 'ASBIEP',
            asbiepNode.asccpNode.manifestId, asbiepNode.asbiepPath),
          this.service.getDetail(node.topLevelAsbiepId, 'ABIE',
            asbiepNode.accNode.manifestId, asbiepNode.abiePath)
        ];

        if (node.inherited) {
          asbiepRequests.push(...[
            this.service.getDetail((node.reused) ? (node.parent as AsbiepFlatNode).basedTopLevelAsbiepId : node.basedTopLevelAsbiepId, 'ASBIE',
              asbiepNode.asccNode.manifestId, asbiepNode.asbiePath)
          ]);
        }

        forkJoin(asbiepRequests).subscribe((
          [asbieDetail, asbiepDetail, abieDetail, basedAsbieDetail]) => {

          if (!basedAsbieDetail) {
            return doAfterAsbiep(asbieDetail, asbiepDetail, abieDetail);
          } else {
            if ((asbieDetail as unknown as BieEditAsbiepNodeDetail).asbie.toAsbiepId ===
              (basedAsbieDetail as unknown as BieEditAsbiepNodeDetail).asbie.toAsbiepId) {

              return doAfterAsbiep(asbieDetail, asbiepDetail, abieDetail, basedAsbieDetail, asbiepDetail, abieDetail);
            } else {
              this.service.getDetailById(
                (basedAsbieDetail as unknown as BieEditAsbiepNodeDetail).asbie.toAsbiepId, 'ASBIEP').subscribe(
                basedAsbiepDetail => this.service.getDetailById(
                  (basedAsbiepDetail as unknown as BieEditAsbiepNodeDetail).asbiep.roleOfAbieId, 'ABIE').subscribe(
                  basedAbieDetail => {
                    return doAfterAsbiep(asbieDetail, asbiepDetail, abieDetail, basedAsbieDetail, basedAsbiepDetail, basedAbieDetail);
                  }
                )
              );
            }
          }
        });
        break;

      case 'BBIEP':
        const bbiepNode = (node as unknown as BbiepFlatNode);
        const bbiepRequests = [
          this.service.getDetail(node.topLevelAsbiepId, 'BBIE',
            bbiepNode.bccNode.manifestId, bbiepNode.bbiePath),
          this.service.getDetail(node.topLevelAsbiepId, 'BBIEP',
            bbiepNode.bccpNode.manifestId, bbiepNode.bbiepPath),
          this.service.getDetail(node.topLevelAsbiepId, 'DT',
            bbiepNode.bdtNode.manifestId, '')
        ];
        if (!!node.basedTopLevelAsbiepId) {
          bbiepRequests.push(...[
            this.service.getDetail(node.basedTopLevelAsbiepId, 'BBIE',
              bbiepNode.bccNode.manifestId, bbiepNode.bbiePath),
            this.service.getDetail(node.basedTopLevelAsbiepId, 'BBIEP',
              bbiepNode.bccpNode.manifestId, bbiepNode.bbiepPath),
            this.service.getDetail(node.basedTopLevelAsbiepId, 'DT',
              bbiepNode.bdtNode.manifestId, '')
          ]);
        }

        forkJoin(bbiepRequests).subscribe((
          [bbieDetail, bbiepDetail, bdtDetail, basedBbieDetail, basedBbiepDetail, basedBdtDetail]) => {
          const storedCardinalityMax = (node.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax;
          const storedDeprecated = (node.detail as BieEditBbiepNodeDetail).bbie.deprecated;
          (node.detail as BieEditBbiepNodeDetail).bcc = (bbieDetail as unknown as BieEditBbiepNodeDetail).bcc;
          (node.detail as BieEditBbiepNodeDetail).bbie.update((bbieDetail as unknown as BieEditBbiepNodeDetail).bbie);
          (node.detail as BieEditBbiepNodeDetail).bccp = (bbiepDetail as unknown as BieEditBbiepNodeDetail).bccp;
          (node.detail as BieEditBbiepNodeDetail).bbiep.update((bbiepDetail as unknown as BieEditBbiepNodeDetail).bbiep);
          (node.detail as BieEditBbiepNodeDetail).bdt = bdtDetail as unknown as BdtDetail;

          if (basedBbieDetail && basedBbiepDetail && basedBdtDetail) {
            const basedNode = Object.create(node);

            (node.detail as BieEditBbiepNodeDetail).base = new BieEditBbiepNodeDetail(basedNode);
            (node.detail as BieEditBbiepNodeDetail).base.bcc = (basedBbieDetail as unknown as BieEditBbiepNodeDetail).bcc;
            (node.detail as BieEditBbiepNodeDetail).base.bbie.update((basedBbieDetail as unknown as BieEditBbiepNodeDetail).bbie);
            (node.detail as BieEditBbiepNodeDetail).base.bccp = (basedBbiepDetail as unknown as BieEditBbiepNodeDetail).bccp;
            (node.detail as BieEditBbiepNodeDetail).base.bbiep.update((basedBbiepDetail as unknown as BieEditBbiepNodeDetail).bbiep);
            (node.detail as BieEditBbiepNodeDetail).base.bdt = basedBdtDetail as unknown as BdtDetail;
          }

          (node.detail as BieEditBbiepNodeDetail).reset();
          node.detail.isLoaded = true;
          if (storedCardinalityMax !== undefined) {
            (node.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax = storedCardinalityMax;
          }
          if (storedDeprecated) {
            (node.detail as BieEditBbiepNodeDetail).bbie.deprecated = storedDeprecated;
          }
          return callbackFn && callbackFn(node);
        });
        break;

      case 'BBIE_SC':
        const bbieScNode = (node as unknown as BbieScFlatNode);
        const bbieScRequests = [
          this.service.getDetail(node.topLevelAsbiepId, 'BBIE_SC',
            bbieScNode.bdtScNode.manifestId, bbieScNode.bbieScPath),
          this.service.getDetail(node.topLevelAsbiepId, 'DT',
            bbieScNode.bdtNode.manifestId, '')
        ];
        if (!!node.basedTopLevelAsbiepId) {
          bbieScRequests.push(...[
            this.service.getDetail(node.basedTopLevelAsbiepId, 'BBIE_SC',
              bbieScNode.bdtScNode.manifestId, bbieScNode.bbieScPath),
            this.service.getDetail(node.basedTopLevelAsbiepId, 'DT',
              bbieScNode.bdtNode.manifestId, '')
          ]);
        }
        forkJoin(bbieScRequests).subscribe((
          [bbieScDetail, bdtDetail, basedBbieScDetail, basedBdtDetail]) => {
          const storedCardinalityMax = (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax;
          const storedDeprecated = (node.detail as BieEditBbieScNodeDetail).bbieSc.deprecated;
          (node.detail as BieEditBbieScNodeDetail).bdtSc = (bbieScDetail as unknown as BieEditBbieScNodeDetail).bdtSc;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.update((bbieScDetail as unknown as BieEditBbieScNodeDetail).bbieSc);
          (node.detail as BieEditBbieScNodeDetail).bdt = bdtDetail as unknown as BdtDetail;

          if (basedBbieScDetail && basedBdtDetail) {
            const basedNode = Object.create(node);

            (node.detail as BieEditBbieScNodeDetail).base = new BieEditBbieScNodeDetail(basedNode);
            (node.detail as BieEditBbieScNodeDetail).base.bdtSc = (basedBbieScDetail as unknown as BieEditBbieScNodeDetail).bdtSc;
            (node.detail as BieEditBbieScNodeDetail).base.bbieSc.update((basedBbieScDetail as unknown as BieEditBbieScNodeDetail).bbieSc);
            (node.detail as BieEditBbieScNodeDetail).base.bdt = basedBdtDetail as unknown as BdtDetail;
          }

          (node.detail as BieEditBbieScNodeDetail).reset();
          node.detail.isLoaded = true;
          if (storedCardinalityMax !== undefined) {
            (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax = storedCardinalityMax;
          }
          if (storedDeprecated) {
            (node.detail as BieEditBbieScNodeDetail).bbieSc.deprecated = storedDeprecated;
          }
          return callbackFn && callbackFn(node);
        });
        break;
    }
  }
}

export class BieFlatNodeDataSourceSearcher<T extends BieFlatNode>
  implements ChangeListener<BieFlatNodeDataSource<T>> {

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

  constructor(private dataSource: BieFlatNodeDataSource<T>,
              private database: BieFlatNodeDatabase<T>) {
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

  onChange(entity: BieFlatNodeDataSource<T>, propertyName: string, val: any) {
    if (propertyName === 'hideUnused') {
      this.resetSearch();
    }
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
        ((this.inputKeyword.charAt(0) === '/') ? [this.dataSource.data[0], ] : [selectedNode, ]) :
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
    return new BiePathLikeExpressionEvaluator(expr, false);
  }

}
