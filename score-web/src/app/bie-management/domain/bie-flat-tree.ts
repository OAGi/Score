import {FlatTreeControlOptions} from '@angular/cdk/tree';
import {Observable} from 'rxjs';
import {CcGraph, CcGraphNode} from '../../cc-management/domain/core-component-node';
import {hashCode4String, sha256} from '../../common/utility';
import {
  DataSourceSearcher,
  ExpressionEvaluator,
  FlatNode,
  FlatNodeFlattener,
  FlatNodeFlattenerListener,
  FlatNodeImpl,
  getKey,
  PathLikeExpressionEvaluator,
  VSFlatTreeControl,
  VSFlatTreeDataSource
} from '../../common/flat-tree';
import {finalize} from 'rxjs/operators';


export interface BieFlatNode extends FlatNode {
  type: string;
  bieType: string;
  topLevelAsbiepId: number;

  used: Observable<boolean> | boolean;
  required?: boolean;
  locked?: boolean;
  derived?: boolean;
  isGroup: boolean;
  isChanged: boolean;
  isCycle: boolean;

  path: string;
  hashPath: string;

  detail: BieEditNodeDetail;

  parents: BieFlatNode[];

  addChangeListener(listener: ChangeListener<BieFlatNode>);
  fireChangeEvent(propertyName: string, val: any);
  reset();
}

export interface ChangeListener<T> {
  onChange(entity: T, propertyName: string, val: any);
}

export abstract class BieFlatNodeImpl extends FlatNodeImpl implements BieFlatNode {
  changeListeners: ChangeListener<BieFlatNode>[] = [];
  $hashCode?: number;

  bieType: string;
  topLevelAsbiepId: number;

  _used: Observable<boolean> | boolean;
  required?: boolean;
  locked?: boolean;
  _derived?: boolean;
  isCycle: boolean = false;

  _detail: BieEditNodeDetail;

  get type(): string {
    return this.bieType;
  }

  get isGroup(): boolean {
    return false;
  }

  abstract get path(): string;

  abstract get hashPath(): string;

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

  set used(used: Observable<boolean> | boolean) {
    this._used = used;
    if (used instanceof Observable) {
      return;
    }

    if (this.$hashCode === undefined) {
      this.$hashCode = (used) ? 1 : 0;
    }

    if (used) {
      if (this.parent) {
        if (!(this.parent as BieFlatNode).derived && (this.parent as BieFlatNode).used !== used) {
          (this.parent as BieFlatNode).used = used;
        }
        this.parent.children.filter(e => e !== this).forEach(child => {
          if (!(child as BieFlatNode).locked && (child as BieFlatNode).required && (child as BieFlatNode).used !== used) {
            (child as BieFlatNode).used = used;
          }
        });
      }
      this.children.filter(e => e !== this).forEach(child => {
        if (!(child as BieFlatNode).locked && (child as BieFlatNode).required && (child as BieFlatNode).used !== used) {
          (child as BieFlatNode).used = used;
        }
      });
    } else {
      this.children.forEach(child => {
        if (!(child as BieFlatNode).locked && (child as BieFlatNode).used !== used) {
          (child as BieFlatNode).used = used;
        }
      });
    }
    this.fireChangeEvent('used', this._used);
  }

  get used(): Observable<boolean> | boolean {
    if (this.isGroup) {
      return (this.parent as BieFlatNode).used;
    }
    if (this._used instanceof Observable) {
      this._used.subscribe(val => {
        this.used = val;
        this.$hashCode = (val) ? 1 : 0;
      });
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

  get derived(): boolean {
    return this._derived || false;
  }

  set derived(derived: boolean) {
    this._derived = derived;
  }

  reset() {
    this.$hashCode = this.hashCode;
    if (this._detail) {
      this._detail.reset();
    }
    this.fireChangeEvent('reset', this);
  }

  get hashCode() {
    return (this.used) ? 1 : 0;
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

  addChangeListener(listener: ChangeListener<BieFlatNode>) {
    if (listener) {
      this.changeListeners.push(listener);
    }
  }

  fireChangeEvent(propertyName: string, val: any) {
    this.changeListeners.forEach(listener => {
      listener.onChange(this, propertyName, val);
    });
  }
}

export class AbieFlatNode extends BieFlatNodeImpl {
  asbiepId: number;
  abieId: number;
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
}

export class AsbiepFlatNode extends AbieFlatNode {
  asbieId: number;
  private _asbiePath: string;
  private _asbieHashPath: string;

  intermediateAccNodes: CcGraphNode[];
  asccNode: CcGraphNode;

  constructor() {
    super();
    this.bieType = 'ASBIEP';
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

  get asbiePath(): string {
    if (!this._asbiePath) {
      let arr;
      if (this.intermediateAccNodes && this.intermediateAccNodes.length > 0) {
        arr = [(this.parent as AsbiepFlatNode).asbiepPath, this.intermediateAccNodes.map(e => getKey(e)).join('>')];
      } else {
        arr = [(this.parent as BieFlatNode).path,];
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
      if (this.derived) {
        this._asbiepPath = 'ASCCP-' + this.asccpNode.manifestId;
      } else {
        this._asbiepPath = [this.asbiePath, 'ASCCP-' + this.asccpNode.manifestId].join('>');
      }
    }
    return this._asbiepPath;
  }

  get derived(): boolean {
    return this._derived || false;
  }

  set derived(derived: boolean) {
    this._derived = derived;
    this._asbiePath = undefined;
  }
}

export class BbiepFlatNode extends BieFlatNodeImpl {
  bbieId: number;
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

  constructor() {
    super();
    this.bieType = 'BBIEP';
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
        arr = [(this.parent as BieFlatNode).path,];
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
      this._bdtPath = [this.bbiepPath, 'BDT-' + this.bdtNode.manifestId].join('>');
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
}

export class BbieScFlatNode extends BieFlatNodeImpl {
  bbieId: number;
  bbieScId: number;
  private _bbieScPath: string;
  private _bbieScHashPath: string;

  bccNode: CcGraphNode;
  bdtScNode: CcGraphNode;
  bdtNode: CcGraphNode;

  constructor() {
    super();
    this.bieType = 'BBIE_SC';
  }

  get bbieScPath(): string {
    if (!this._bbieScPath) {
      this._bbieScPath = [(this.parent as BieFlatNode).path, 'BDT_SC-' + this.bdtScNode.manifestId].join('>');
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
}

export class WrappedBieFlatNode implements BieFlatNode {
  _node: BieFlatNode;

  constructor(node: BieFlatNode) {
    this._node = node;
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

  get used(): Observable<boolean> | boolean {
    if (this._node.used instanceof Observable) {
      this._node.used.subscribe(val => {
        this.used = val;
      });
    }
    return this._node.used;
  }

  set used(used: Observable<boolean> | boolean) {
    this._node.used = used;
  }

  get derived(): boolean {
    return this._node.derived;
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

  fireChangeEvent(propertyName: string, val: any) {
    this._node.fireChangeEvent(propertyName, val);
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
}

export class AbieDetail {
  private _node: AbieFlatNode | AsbiepFlatNode;

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
    return ((this.abieId) ? this.abieId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.version) ? hashCode4String(this.version) : 0) +
      ((!!this.status) ? hashCode4String(this.status) : 0) +
      ((!!this.remark) ? hashCode4String(this.remark) : 0) +
      ((!!this.bizTerm) ? hashCode4String(this.bizTerm) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0);
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

  asbieId: number;
  guid: string;
  seqKey: number;
  private _definition: string;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _nillable: boolean;
  private _remark: string;

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

  get remark(): string {
    return this._remark;
  }

  set remark(value: string) {
    this._remark = value;
    this._node.fireChangeEvent('remark', value);
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
      this.asbieId = obj.asbieId;
      this.guid = obj.guid;
      this.definition = obj.definition;
      this.cardinalityMin = obj.cardinalityMin;
      this.cardinalityMax = obj.cardinalityMax;
      this.nillable = obj.nillable;
      this.remark = obj.remark;
      this.seqKey = 0;
    }
  }

  get hashCode(): number {
    return ((this.asbieId) ? this.asbieId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      (((this.nillable) ? 1 : 0)) +
      ((!!this.remark) ? hashCode4String(this.remark) : 0) +
      ((this.seqKey) ? this.seqKey : 0);
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
      this.asbiepId = obj.asbiepId;
      this.guid = obj.guid;
      this.roleOfAbieId = obj.roleOfAbieId;
      this.remark = obj.remark;
      this.bizTerm = obj.bizTerm;
      this.definition = obj.definition;
    }
  }

  get hashCode(): number {
    return ((this.asbiepId) ? this.asbiepId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((this.roleOfAbieId) ? this.roleOfAbieId : 0) +
      ((!!this.remark) ? hashCode4String(this.remark) : 0) +
      ((!!this.bizTerm) ? hashCode4String(this.bizTerm) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0);
  }

  get json(): any {
    return {
      asbiepId: this.asbiepId,
      guid: this.guid,
      roleOfAbieId: this.roleOfAbieId,
      remark: this.remark,
      bizTerm: this.bizTerm,
      definition: this.definition,
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

  bbieId: number;
  guid: string;
  seqKey: number;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _nillable: boolean;
  private _remark: string;
  private _definition: string;
  private _defaultValue: string;
  private _fixedValue: string;
  fixedOrDefault: string;
  private _example: string;

  valueDomainType: string;
  private _bdtPriRestriId: number;
  private _codeListId: number;
  private _agencyIdListId: number;

  constructor(node: BbiepFlatNode) {
    this._node = node;
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

  get bdtPriRestriId(): number {
    return this._bdtPriRestriId;
  }

  set bdtPriRestriId(value: number) {
    this._bdtPriRestriId = value;
    this._node.fireChangeEvent('bdtPriRestriId', value);
  }

  get codeListId(): number {
    return this._codeListId;
  }

  set codeListId(value: number) {
    this._codeListId = value;
    this._node.fireChangeEvent('codeListId', value);
  }

  get agencyIdListId(): number {
    return this._agencyIdListId;
  }

  set agencyIdListId(value: number) {
    this._agencyIdListId = value;
    this._node.fireChangeEvent('agencyIdListId', value);
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
      this.bbieId = obj.bbieId;
      this.guid = obj.guid;
      this.remark = obj.remark;
      this.definition = obj.definition;
      this.cardinalityMin = obj.cardinalityMin;
      this.cardinalityMax = obj.cardinalityMax;
      this.definition = obj.definition;

      this.nillable = obj.nillable;
      this.remark = obj.remark;
      if (!(obj.defaultValue == null && obj.fixedValue == null)) {
        this.defaultValue = obj.defaultValue;
        this.fixedValue = obj.fixedValue;
      }
      this.example = obj.example;
      if (obj.agencyIdListId) {
        this.agencyIdListId = obj.agencyIdListId;
        this.valueDomainType = 'Agency';
      } else if (obj.codeListId) {
        this.codeListId = obj.codeListId;
        this.valueDomainType = 'Code';
      } else {
        this.bdtPriRestriId = obj.bdtPriRestriId;
        this.valueDomainType = 'Primitive';
      }
      this.seqKey = 0;
    }
  }

  get hashCode(): number {
    return ((this.bbieId) ? this.bbieId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((this.nillable) ? 1 : 0) +
      ((!!this.remark) ? hashCode4String(this.remark) : 0) +
      ((!!this.example) ? hashCode4String(this.example) : 0) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0) +
      ((this.bdtPriRestriId) ? this.bdtPriRestriId : 0) +
      ((this.codeListId) ? this.codeListId : 0) +
      ((this.agencyIdListId) ? this.agencyIdListId : 0) +
      ((this.seqKey) ? this.seqKey : 0);
  }

  get json(): any {
    return {
      bbieId: this.bbieId,
      guid: this.guid,
      definition: this.definition,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      nillable: this.nillable,
      remark: this.remark,
      example: this.example,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      bdtPriRestriId: this.bdtPriRestriId,
      codeListId: this.codeListId,
      agencyIdListId: this.agencyIdListId,
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
      this.bbiepId = obj.bbiepId;
      this.guid = obj.guid;
      this.definition = obj.definition;
      this.remark = obj.remark;
      this.bizTerm = obj.bizTerm;
    }
  }

  get hashCode(): number {
    return ((this.bbiepId) ? this.bbiepId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((!!this.remark) ? hashCode4String(this.remark) : 0) +
      ((!!this.bizTerm) ? hashCode4String(this.bizTerm) : 0);
  }

  get json(): any {
    return {
      bbiepId: this.bbiepId,
      guid: this.guid,
      definition: this.definition,
      remark: this.remark,
      bizTerm: this.bizTerm,
      basedBccpManifestId: this.basedBccpManifestId,
      path: this.path,
      hashPath: this.hashPath
    };
  }
}

export class BbieScDetail {
  private _node: BbieScFlatNode;

  bbieScId: number;
  guid: string;
  private _cardinalityMin: number;
  private _cardinalityMax: number;
  private _remark: string;
  private _bizTerm: string;
  private _definition: string;
  private _defaultValue: string;
  private _fixedValue: string;
  fixedOrDefault: string;
  private _example: string;

  valueDomainType: string;
  private _bdtScPriRestriId: number;
  private _codeListId: number;
  private _agencyIdListId: number;

  constructor(node: BbieScFlatNode) {
    this._node = node;
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

  get bdtScPriRestriId(): number {
    return this._bdtScPriRestriId;
  }

  set bdtScPriRestriId(value: number) {
    this._bdtScPriRestriId = value;
    this._node.fireChangeEvent('bdtScPriRestriId', value);
  }

  get codeListId(): number {
    return this._codeListId;
  }

  set codeListId(value: number) {
    this._codeListId = value;
    this._node.fireChangeEvent('codeListId', value);
  }

  get agencyIdListId(): number {
    return this._agencyIdListId;
  }

  set agencyIdListId(value: number) {
    this._agencyIdListId = value;
    this._node.fireChangeEvent('agencyIdListId', value);
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
      this.bbieScId = obj.bbieScId;
      this.guid = obj.guid;
      this.cardinalityMin = obj.cardinalityMin;
      this.cardinalityMax = obj.cardinalityMax;

      this.definition = obj.definition;
      this.bizTerm = obj.bizTerm;
      this.remark = obj.remark;
      this.defaultValue = obj.defaultValue;
      this.fixedValue = obj.fixedValue;
      this.example = obj.example;
      if (obj.agencyIdListId) {
        this.agencyIdListId = obj.agencyIdListId;
        this.valueDomainType = 'Agency';
      } else if (obj.codeListId) {
        this.codeListId = obj.codeListId;
        this.valueDomainType = 'Code';
      } else {
        this.bdtScPriRestriId = obj.bdtScPriRestriId;
        this.valueDomainType = 'Primitive';
      }
    }
  }

  get hashCode(): number {
    return ((this.bbieScId) ? this.bbieScId : 0) +
      ((this.guid) ? hashCode4String(this.guid) : 0) +
      ((!!this.definition) ? hashCode4String(this.definition) : 0) +
      ((this.cardinalityMin) ? this.cardinalityMin : 0) +
      ((this.cardinalityMax) ? this.cardinalityMax : 0) +
      ((!!this.bizTerm) ? hashCode4String(this.bizTerm) : 0) +
      ((!!this.remark) ? hashCode4String(this.remark) : 0) +
      ((!!this.example) ? hashCode4String(this.example) : 0) +
      ((!!this.defaultValue) ? hashCode4String(this.defaultValue) : 0) +
      ((!!this.fixedValue) ? hashCode4String(this.fixedValue) : 0) +
      ((this.bdtScPriRestriId) ? this.bdtScPriRestriId : 0) +
      ((this.codeListId) ? this.codeListId : 0) +
      ((this.agencyIdListId) ? this.agencyIdListId : 0);
  }

  get json(): any {
    return {
      bbieScId: this.bbieScId,
      guid: this.guid,
      definition: this.definition,
      cardinalityMin: this.cardinalityMin,
      cardinalityMax: this.cardinalityMax,
      bizTerm: this.bizTerm,
      remark: this.remark,
      example: this.example,
      defaultValue: this.defaultValue,
      fixedValue: this.fixedValue,
      bdtScPriRestriId: this.bdtScPriRestriId,
      codeListId: this.codeListId,
      agencyIdListId: this.agencyIdListId,
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

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.abie = new AbieDetail(node as AsbiepFlatNode);
    this.asbie = new AsbieDetail(node as AsbiepFlatNode);
    this.asbiep = new AsbiepDetail(node as AsbiepFlatNode);
    this.asbie.seqKey = 0;
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

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.bbie = new BbieDetail(node as BbiepFlatNode);
    this.bbiep = new BbiepDetail(node as BbiepFlatNode);
    this.bdt = new BdtDetail(node as BbiepFlatNode);
    this.bbie.seqKey = 0;
    this.reset();
  }

  get hashCode(): number {
    return this.bbie.hashCode + this.bbiep.hashCode;
  }

  get isValid() {
    if (!(this.bbie.bdtPriRestriId || this.bbie.codeListId || this.bbie.agencyIdListId)) {
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

  constructor(node: BieFlatNode) {
    super();
    this._node = node;
    this.bbieSc = new BbieScDetail(node as BbieScFlatNode);
    this.bdt = new BdtDetail(node as BbieScFlatNode);
    this.reset();
  }

  get hashCode(): number {
    return this.bbieSc.hashCode;
  }

  get isValid() {
    if (!(this.bbieSc.bdtScPriRestriId || this.bbieSc.codeListId || this.bbieSc.agencyIdListId)) {
      return false;
    }
    return true;
  }

  reset() {
    super.reset();
    this._node.fireChangeEvent('reset', this);
  }
}


export class VSBieFlatTreeControl<T extends BieFlatNode> extends VSFlatTreeControl<T> {
  private _hideUnused: boolean;

  constructor(hideUnused?: boolean | undefined, isExpandable?: (dataNode: T) => boolean, options?: FlatTreeControlOptions<T, T> | undefined) {
    super((isExpandable) ? isExpandable : node => {
      if (!node.children || node.children.length === 0) {
        return false;
      }
      return (hideUnused) ?
        node.children.filter(e => (e as BieFlatNode).isGroup ? this.isExpandable(e as T) : (e as BieFlatNode).used)
          .length > 0 : true;
    }, options);

    this._hideUnused = hideUnused || false;
  }

  expand(dataNode: T, reset?: boolean) {
    if (!dataNode || !this.isExpandable(dataNode) || this.isExpanded(dataNode)) {
      return;
    }
    if (this.hideUnused && !dataNode.isGroup && !dataNode.used) {
      return;
    }
    super.expand(dataNode, reset);
  }

  get hideUnused(): boolean {
    return this._hideUnused;
  }

  set hideUnused(hideUnused: boolean) {
    this._hideUnused = hideUnused;
    this.dataSource.resetData();
  }
}

export class VSBieFlatTreeDataSource<T extends BieFlatNode> extends VSFlatTreeDataSource<T> {

  constructor(treeControl: VSBieFlatTreeControl<T>, data: T[]) {
    super(treeControl, data);
  }

  get cachedData(): T[] {
    return ((this.treeControl as VSBieFlatTreeControl<T>).hideUnused) ?
      super.cachedData.filter(e => e.used) : super.cachedData;
  }

  set cachedData(data: T[]) {
    super.cachedData = data;
  }

  dataFilter(node: T): boolean {
    if (node.level === 0) {
      return true;
    }
    if (!this.treeControl) {
      return false;
    }
    const a = this.treeControl.isExpanded(node) || this.treeControl.isExpanded(node.parent as T);
    const b = (this.treeControl as VSBieFlatTreeControl<T>).hideUnused ? node.used as boolean : true;
    return a && b;
  }
}

export class Association {

  intermediateAccNodes: CcGraphNode[] = [];
  fromAccNode: CcGraphNode;
  assocNode: CcGraphNode;

}

export class BieFlatNodeFlattener implements FlatNodeFlattener<BieFlatNode> {
  private _ccGraph: CcGraph;
  private _topLevelAsbiepId: number;
  private _asccpManifestId: number;
  private _listeners: FlatNodeFlattenerListener<BieFlatNode>[] = [];
  private _validState: string[];

  constructor(ccGraph: CcGraph, asccpManifestId: number,
              topLevelAsbiepId?: number) {
    this._ccGraph = ccGraph;
    this._asccpManifestId = asccpManifestId;
    this._topLevelAsbiepId = topLevelAsbiepId;
    this._validState = ['Published', 'Production'];
  }

  addListener(listener: FlatNodeFlattenerListener<BieFlatNode>) {
    this._listeners.push(listener);
  }

  toAbieNode(key: string): AbieFlatNode {
    const node = new AbieFlatNode();
    node.asccpNode = this._ccGraph.graph.nodes[key];
    node.accNode = this.getChildren(node.asccpNode)[0];
    node.name = node.asccpNode.propertyTerm;
    node.level = 0;
    node.used = true;
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
    const usable = this._validState.indexOf(node.asccNode.state) > -1
      && this._validState.indexOf(node.asccpNode.state) > -1
      && this._validState.indexOf(node.accNode.state) > -1;
    if (parent.derived || !usable) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.topLevelAsbiepId = parent.topLevelAsbiepId;
    if (!node.isGroup) {
      node.isCycle = this.detectCycle(node);
    }
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
    const usable = this._validState.indexOf(node.bccNode.state) > -1
      && this._validState.indexOf(node.bccpNode.state) > -1
      && this._validState.indexOf(node.bdtNode.state) > -1;
    if (parent.derived || !usable) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.topLevelAsbiepId = parent.topLevelAsbiepId;
    return node;
  }

  toBbieScNode(bccNode: CcGraphNode, bdtScNode: CcGraphNode, parent: BieFlatNode) {
    const node = new BbieScFlatNode();
    node.bccNode = bccNode;
    node.bdtScNode = this._ccGraph.graph.nodes[getKey(bdtScNode)];
    node.required = node.bdtScNode.cardinalityMin > 0;
    node.name = node.bdtScNode.propertyTerm + ' ' + node.bdtScNode.representationTerm;
    node.level = parent.level + 1;
    node.parent = parent;
    const usable = this._validState.indexOf(node.bccNode.state) > -1
      && this._validState.indexOf(node.bdtScNode.state) > -1;
    if (parent.derived || !usable) {
      node.locked = true;
    } else {
      node.locked = parent.locked;
    }
    node.topLevelAsbiepId = parent.topLevelAsbiepId;
    return node;
  }

  fireEvent(node: BieFlatNode) {
    this._listeners.forEach(listener => {
      listener.onFlatten(node);
    });
  }

  flatten(): BieFlatNode[] {
    const node = this.toAbieNode('ASCCP-' + this._asccpManifestId);
    node.topLevelAsbiepId = this._topLevelAsbiepId;
    this.fireEvent(node);

    const nodes = [node,];
    this._doFlatten(nodes, node);
    return nodes;
  }

  _doFlatten(nodes: BieFlatNode[], node: BieFlatNode) {
    let children = [];
    if (node instanceof AbieFlatNode || node instanceof AsbiepFlatNode) {
      children = this.getAssociations(node.accNode);
      node.children = children.map((e: Association) => {
        if (e.assocNode.type === 'ASCC') {
          const asbiepNode: AsbiepFlatNode = this.toAsbiepNode(e, node as AsbiepFlatNode);
          this.afterAsbiepFlatNode(asbiepNode);
          return asbiepNode;
        } else {
          const bbiepNode: BbiepFlatNode = this.toBbiepNode(e, node as AsbiepFlatNode);
          this.afterBbiepFlatNode(bbiepNode);
          return bbiepNode;
        }
      });

      node.children.map(e => e as BieFlatNode).forEach(e => {
        if (!e.isGroup) {
          nodes.push(e);
          this.fireEvent(e);
        }
        if (e.isCycle) {
          return;
        }
        this._doFlatten(nodes, e);
      });
    } else if (node instanceof BbiepFlatNode) {
      children = this.getChildren(node.bdtNode);
      node.children = children.map(e => {
        const bbieScNode = this.toBbieScNode(node.bccNode, e, node);
        this.afterBbieScFlatNode(bbieScNode);
        return bbieScNode;
      });

      node.children.map(e => e as BieFlatNode)
        .forEach(e => {
          nodes.push(e);
          this.fireEvent(e);
        });
    }
  }

  detectCycle(node: AsbiepFlatNode): boolean {
    const asccpManifestId = node.asccpNode.manifestId;
    let cur = node.parent;
    while (cur) {
      if ((cur as AbieFlatNode).asccpNode.manifestId === asccpManifestId) {
        return true;
      }
      cur = cur.parent;
    }
    return false;
  }

  afterAsbiepFlatNode(node: AsbiepFlatNode) {
  }

  afterBbiepFlatNode(node: BbiepFlatNode) {
  }

  afterBbieScFlatNode(node: BbieScFlatNode) {
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

      case 'BDT':
        return targets.map(e => nodes[e]).filter(e => e.cardinalityMax > 0);

      case 'BDT_SC':
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
}

export class BiePathLikeExpressionEvaluator extends PathLikeExpressionEvaluator<BieFlatNode> {
  protected next(node: BieFlatNode): BieFlatNode {
    let next = node.parent as BieFlatNode;
    while (next && (next as BieFlatNode).isGroup) {
      next = next.parent as BieFlatNode;
    }
    return next;
  }
}

export class BieDataSourceSearcher extends DataSourceSearcher<BieFlatNode> {
  protected getEvaluator(expr: string): ExpressionEvaluator<BieFlatNode> {
    return new BiePathLikeExpressionEvaluator(expr, false);
  }
}
