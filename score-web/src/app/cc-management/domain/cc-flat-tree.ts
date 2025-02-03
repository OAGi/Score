import {ChangeListener} from '../../bie-management/domain/bie-flat-tree';
import {ExpressionEvaluator, FlatNode, getKey, next, PathLikeExpressionEvaluator} from '../../common/flat-tree';
import {
  CcAccNodeDetail,
  CcAsccpNodeDetail,
  CcBccpNodeDetail,
  CcBdtScNodeDetail,
  CcDtNodeDetail,
  CcGraph,
  CcGraphNode,
  CcNodeDetail
} from './core-component-node';
import {CcNodeService} from './core-component-node.service';
import {CollectionViewer, DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, empty, Observable} from 'rxjs';
import {sha256} from '../../common/utility';
import {ShortTag} from '../../tag-management/domain/tag';

export interface CcFlatNode extends FlatNode {
  type: string;
  libraryId: number;
  releaseId: number;
  guid: string;
  state: string;
  deprecated: boolean;
  access: string;
  manifestId: number;
  revisionNum: number;

  cardinalityMin: number;
  cardinalityMax: number;

  tagList: ShortTag[];

  isCycle: boolean;
  isChanged: boolean;
  detail: CcNodeDetail;
  parents: CcFlatNode[];

  path: string;
  hashPath: string;

  inhibited: boolean;

  typeClass: string;

  dataSource: CcFlatNodeDataSource<CcFlatNode>;

  showCopyLinkIcon: boolean;
  queryPath: string;

  addChangeListener(listener: ChangeListener<CcFlatNode>);
  removeChangeListener(listener: ChangeListener<CcFlatNode>);
  fireChangeEvent(propertyName: string, val: any);
  reset();
}

export abstract class CcFlatNodeImpl implements CcFlatNode {

  abstract get guid(): string;
  abstract get name(): string;
  abstract get type(): string;
  abstract get typeClass(): string;
  abstract get manifestId(): number;
  abstract get libraryId(): number;
  abstract get releaseId(): number;

  abstract get path(): string;
  abstract get hashPath(): string;

  abstract get cardinalityMin(): number;
  abstract get cardinalityMax(): number;

  abstract get tagList(): ShortTag[];

  get expanded(): boolean {
    return this._expanded || false;
  }

  set expanded(expanded: boolean) {
    this._expanded = expanded;
  }

  get hasExtension(): boolean {
    return this._hasExtension(this);
  }

  get parents(): CcFlatNode[] {
    let node: CcFlatNode = this;
    const result: CcFlatNode[] = [node];
    while (node.parent) {
      result.push(node.parent as CcFlatNode);
      node = node.parent as CcFlatNode;
    }
    return result.reverse();
  }

  get isChanged(): boolean {
    return (!!this.detail) ? this.detail.isChanged : false;
  }

  get hashCode(): number {
    return (!!this.detail) ? this.detail.hashCode : 0;
  }

  get inhibited() {
    return false;
  }

  level: number;
  _expanded: boolean;
  _expandable: boolean = undefined;

  parent?: FlatNode;
  _children: CcFlatNode[] = [];

  changeListeners: ChangeListener<CcFlatNode>[] = [];

  state: string;
  access: string;
  revisionNum: number;
  isCycle = false;

  detail: CcNodeDetail;

  deprecated: boolean;

  dataSource: CcFlatNodeDataSource<CcFlatNode>;

  showCopyLinkIcon = false;

  get queryPath(): string {
    const parent = this.parent as CcFlatNode;
    if (!!parent) {
      return [parent.queryPath,
        this.name.replace(new RegExp(' ', 'g'), '')].join('/');
    }
    return this.name.replace(new RegExp(' ', 'g'), '');
  }

  _hasExtension(node: CcFlatNode): boolean {
    if (!node || !node.children) {
      return false;
    }
    for (const child of node.children) {
      if (child instanceof AsccpFlatNode) {
        if (child.asccpNode.propertyTerm === 'Extension') {
          return true;
        }
      }
    }
    return false;
  }

  addChangeListener(listener: ChangeListener<CcFlatNode>) {
    if (!!listener && this.changeListeners.indexOf(listener) === -1) {
      this.changeListeners.push(listener);
    }
  }

  removeChangeListener(listener: ChangeListener<CcFlatNode>) {
    if (!!listener && this.changeListeners.indexOf(listener) > -1) {
      this.changeListeners.splice(this.changeListeners.indexOf(listener), 1);
    }
  }

  fireChangeEvent(propertyName: string, val: any) {
    this.changeListeners.forEach(listener => {
      listener.onChange(this, propertyName, val);
    });
  }

  reset() {
    if (!!this.detail) {
      this.detail.reset();
    }
  }

  getChildren(options?: any | undefined): CcFlatNode[] {
    if (!!options && options.hideProhibited) {
      return this._children.filter(e => !e.inhibited);
    }
    return this._children;
  }

  get children(): CcFlatNode[] {
    return this.getChildren({
      hideProhibited: !!this.dataSource && this.dataSource.hideProhibited
    });
  }

  set children(children: CcFlatNode[]) {
    this._children = children;
  }

  get expandable(): boolean {
    if (this._expandable !== undefined) {
      return this._expandable;
    }
    if (this._children.length === 0) {
      this.dataSource.database.loadChildren(this);
    }
    return this.children.length > 0;
  }

  set expandable(expandable: boolean) {
    this._expandable = expandable;
  }
}

export class AccFlatNode extends CcFlatNodeImpl {
  accNode: CcGraphNode;
  private _path: string;
  private _hashPath: string;

  constructor(accNode: CcGraphNode) {
    super();
    this.accNode = accNode;
    this.deprecated = accNode.deprecated;
  }

  get type(): string {
    return 'ACC';
  }

  get typeClass(): string {
    return this.type;
  }

  get guid(): string {
    return this.accNode.guid;
  }

  get name(): string {
    if (this.detail) {
      return (this.detail as CcAccNodeDetail).den;
    }
    return this.accNode.objectClassTerm + '. Details';
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcAccNodeDetail).objectClassTerm = val;
    }
    this.accNode.objectClassTerm = val;
  }

  get den(): string {
    return this.name;
  }

  get manifestId(): number {
    return this.accNode.manifestId;
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcAccNodeDetail).libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcAccNodeDetail).releaseId : undefined;
  }

  get accManifestId(): number {
    return this.manifestId;
  }

  get path(): string {
    if (!this._path) {
      this._path = 'ACC-' + this.accNode.manifestId;
      if (!!this.parent) {
        this._path = [(this.parent as CcFlatNode).path, this._path].join('>');
      }
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
    return undefined;
  }

  get cardinalityMax(): number {
    return undefined;
  }

  set tagList(tagList: ShortTag[]) {
    this.accNode.tagList = tagList;
  }

  get tagList(): ShortTag[] {
    return this.accNode.tagList;
  }
}

export class AsccpFlatNode extends CcFlatNodeImpl {
  asccpNode: CcGraphNode;
  asccNode?: CcGraphNode;
  private _path: string;
  private _hashPath: string;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor(asccpNode: CcGraphNode,
              asccNode?: CcGraphNode) {
    super();
    this.asccpNode = asccpNode;
    this.asccNode = asccNode;
    this.deprecated = asccpNode.deprecated || (!!asccNode && asccNode.deprecated);
    this.expandable = true;
  }

  get type(): string {
    return 'ASCCP';
  }

  get typeClass(): string {
    return this.type;
  }

  get guid(): string {
    return this.asccpNode.guid;
  }

  get name(): string {
    if (this.detail) {
      return (this.detail as CcAsccpNodeDetail).asccp.propertyTerm;
    }
    return this.asccpNode.propertyTerm;
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcAsccpNodeDetail).asccp.propertyTerm = val;
    }
    this.asccpNode.propertyTerm = val;
  }

  get manifestId(): number {
    return this.asccpNode.manifestId;
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcAsccpNodeDetail).asccp.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcAsccpNodeDetail).asccp.releaseId : undefined;
  }

  get asccManifestId(): number {
    if (this.asccNode) {
      return this.asccNode.manifestId;
    }
    return undefined;
  }

  get asccpManifestId(): number {
    return this.manifestId;
  }

  get isUserExtensionGroup(): boolean {
    return this.asccpNode.propertyTerm.endsWith('User Extension Group');
  }

  get path(): string {
    if (!this._path) {
      this._path = 'ASCCP-' + this.asccpNode.manifestId;
      if (!!this.parent) {
        this._path = [(this.parent as CcFlatNode).path,
          ('ASCC-' + this.asccNode.manifestId), this._path].join('>');
      }
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
    if (!!this.detail && !!(this.detail as CcAsccpNodeDetail).ascc && !!(this.detail as CcAsccpNodeDetail).ascc.cardinalityMin) {
      return (this.detail as CcAsccpNodeDetail).ascc.cardinalityMin;
    }
    if (this._cardinalityMin === undefined) {
      if (!!this.asccNode) {
        return this.asccNode.cardinalityMin;
      } else {
        return undefined;
      }
    }
    return this._cardinalityMin;
  }

  set cardinalityMin(cardinalityMin: number) {
    this._cardinalityMin = cardinalityMin;
  }

  get cardinalityMax(): number {
    if (!!this.detail && !!(this.detail as CcAsccpNodeDetail).ascc && !!(this.detail as CcAsccpNodeDetail).ascc.cardinalityMax) {
      return (this.detail as CcAsccpNodeDetail).ascc.cardinalityMax;
    }
    if (this._cardinalityMax === undefined) {
      if (!!this.asccNode) {
        return this.asccNode.cardinalityMax;
      } else {
        return undefined;
      }
    }
    return this._cardinalityMax;
  }

  set cardinalityMax(cardinalityMax: number) {
    this._cardinalityMax = cardinalityMax;
  }

  set tagList(tagList: ShortTag[]) {
    this.asccpNode.tagList = tagList;
  }

  get tagList(): ShortTag[] {
    return this.asccpNode.tagList;
  }
}

export class BccpFlatNode extends CcFlatNodeImpl {
  bccNode?: CcGraphNode;
  bccpNode: CcGraphNode;
  bdtNode: CcGraphNode;
  private _path: string;
  private _hashPath: string;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor(bccpNode: CcGraphNode,
              bdtNode: CcGraphNode,
              bccNode?: CcGraphNode) {
    super();
    this.bccpNode = bccpNode;
    this.bdtNode = bdtNode;
    this.bccNode = bccNode;
    this.deprecated = bccpNode.deprecated || (!!bdtNode && bdtNode.deprecated) || (!!bccNode && bccNode.deprecated);
  }

  get type(): string {
    return 'BCCP';
  }

  get typeClass(): string {
    if (this.bccNode && this.bccNode.entityType === 'Attribute') {
      return this.type + '-attribute';
    }
    return this.type;
  }

  get guid(): string {
    return this.bccpNode.guid;
  }

  get name(): string {
    if (this.detail) {
      return (this.detail as CcBccpNodeDetail).bccp.propertyTerm;
    }
    return this.bccpNode.propertyTerm;
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcBccpNodeDetail).bccp.propertyTerm = val;
    }
    this.bccpNode.propertyTerm = val;
  }

  get entityType(): string {
    return this.bccNode ? this.bccNode.entityType : undefined;
  }

  get manifestId(): number {
    return this.bccpNode.manifestId;
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcBccpNodeDetail).bccp.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcBccpNodeDetail).bccp.releaseId : undefined;
  }

  get bccManifestId(): number {
    if (this.bccNode) {
      return this.bccNode.manifestId;
    }
    return undefined;
  }

  get bccpManifestId(): number {
    return this.manifestId;
  }

  get bdtManifestId(): number {
    if (this.bdtNode) {
      return this.bdtNode.manifestId;
    }
    return undefined;
  }

  get path(): string {
    if (!this._path) {
      this._path = 'BCCP-' + this.bccpNode.manifestId;
      if (!!this.parent) {
        this._path = [(this.parent as CcFlatNode).path,
          ('BCC-' + this.bccNode.manifestId), this._path].join('>');
      }
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
    if (!!this.detail && !!(this.detail as CcBccpNodeDetail).bcc && !!(this.detail as CcBccpNodeDetail).bcc.cardinalityMin) {
      return (this.detail as CcBccpNodeDetail).bcc.cardinalityMin;
    }
    if (this._cardinalityMin === undefined) {
      if (!!this.bccNode) {
        return this.bccNode.cardinalityMin;
      } else {
        return undefined;
      }
    }
    return this._cardinalityMin;
  }

  set cardinalityMin(cardinalityMin: number) {
    this._cardinalityMin = cardinalityMin;
  }

  get cardinalityMax(): number {
    if (!!this.detail && !!(this.detail as CcBccpNodeDetail).bcc && !!(this.detail as CcBccpNodeDetail).bcc.cardinalityMax) {
      return (this.detail as CcBccpNodeDetail).bcc.cardinalityMax;
    }
    if (this._cardinalityMax === undefined) {
      if (!!this.bccNode) {
        return this.bccNode.cardinalityMax;
      } else {
        return undefined;
      }
    }
    return this._cardinalityMax;
  }

  set cardinalityMax(cardinalityMax: number) {
    this._cardinalityMax = cardinalityMax;
  }

  set tagList(tagList: ShortTag[]) {
    this.bccpNode.tagList = tagList;
  }

  get tagList(): ShortTag[] {
    return this.bccpNode.tagList;
  }
}

export class DtFlatNode extends CcFlatNodeImpl {
  dtNode: CcGraphNode;
  private _path: string;
  private _hashPath: string;

  constructor(dtNode: CcGraphNode) {
    super();
    this.dtNode = dtNode;
    this.deprecated = dtNode.deprecated;
  }

  get type(): string {
    return 'DT';
  }

  get typeClass(): string {
    return this.type;
  }

  get guid(): string {
    return this.dtNode.guid;
  }

  get name(): string {
    if (this.detail) {
      return (this.detail as CcDtNodeDetail).den;
    }
    return this.dtNode.den;
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcDtNodeDetail).den = val;
    }
    this.dtNode.den = val;
  }

  get manifestId(): number {
    return this.dtNode.manifestId;
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcDtNodeDetail).libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcDtNodeDetail).releaseId : undefined;
  }

  get den(): string {
    return this.dtNode.den;
  }

  get basedManifestId(): number {
    return this.detail ? (this.detail as CcDtNodeDetail).basedBdtManifestId : undefined;
  }

  get path(): string {
    if (!this._path) {
      this._path = 'DT-' + this.dtNode.manifestId;
      if (!!this.parent) {
        this._path = [(this.parent as CcFlatNode).path, this._path].join('>');
      }
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
    return undefined;
  }

  get cardinalityMax(): number {
    return undefined;
  }

  set tagList(tagList: ShortTag[]) {
    this.dtNode.tagList = tagList;
  }

  get tagList(): ShortTag[] {
    return this.dtNode.tagList;
  }
}

export class DtScFlatNode extends CcFlatNodeImpl {
  dtScNode: CcGraphNode;
  private _path: string;
  private _hashPath: string;

  private _cardinalityMin: number = undefined;
  private _cardinalityMax: number = undefined;

  constructor(dtScNode: CcGraphNode) {
    super();
    this.dtScNode = dtScNode;
  }

  get type(): string {
    return 'DT_SC';
  }

  get typeClass(): string {
    return this.type;
  }

  get guid(): string {
    return this.dtScNode.guid;
  }

  get name(): string {
    const propertyTerm = this.dtScNode.propertyTerm || '';
    const middle = propertyTerm.replace(this.dtScNode.representationTerm, '').trim();
    if (middle && this.dtScNode.objectClassTerm !== middle) {
      return this.dtScNode.objectClassTerm + '. '
        + middle + '. '
        + this.dtScNode.representationTerm;
    }
    return this.dtScNode.objectClassTerm + '. '
      + this.dtScNode.representationTerm;
  }

  set name(val: string) {
    throw Error('Unsupported operation');
  }

  get den(): string {
    return this.name;
  }

  set den(val: string) {
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcBdtScNodeDetail).libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcBdtScNodeDetail).releaseId : undefined;
  }

  get manifestId(): number {
    return this.dtScNode.manifestId;
  }

  get bdtScManifestId(): number {
    return this.manifestId;
  }

  get inhibited(): boolean {
    return this.dtScNode.cardinalityMin === 0 && this.dtScNode.cardinalityMax === 0;
  }

  get removeAble(): boolean {
    return !(this.dtScNode.basedDtScId > 0);
  }

  get path(): string {
    if (!this._path) {
      this._path = 'DT_SC-' + this.dtScNode.manifestId;
      if (!!this.parent) {
        this._path = [(this.parent as CcFlatNode).path, this._path].join('>');
      }
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
    if (this._cardinalityMin === undefined) {
      return this.dtScNode.cardinalityMin;
    }
    return this._cardinalityMin;
  }

  set cardinalityMin(cardinalityMin: number) {
    this._cardinalityMin = cardinalityMin;
  }

  get cardinalityMax(): number {
    if (this._cardinalityMax === undefined) {
      return this.dtScNode.cardinalityMax;
    }
    return this._cardinalityMax;
  }

  set cardinalityMax(cardinalityMax: number) {
    this._cardinalityMax = cardinalityMax;
  }

  set tagList(tagList: ShortTag[]) {
  }

  get tagList(): ShortTag[] {
    return [];
  }
}

export class CcFlatNodeDatabase<T extends CcFlatNode> {

  dataSource: CcFlatNodeDataSource<T>;
  _ccGraph: CcGraph;
  private _type: string;
  private _manifestId: number;

  constructor(ccGraph: CcGraph, type: string, manifestId: number) {
    this._ccGraph = ccGraph;
    this._type = type;
    this._manifestId = manifestId;
  }

  get key(): string {
    return this._type.toUpperCase() + '-' + this._manifestId;
  }

  get rootNode(): T {
    let node;
    if (this._type === 'ACC') {
      const accNode = this._ccGraph.graph.nodes[this.key];
      node = new AccFlatNode(accNode);
    } else if (this._type === 'ASCCP') {
      const asccpNode = this._ccGraph.graph.nodes[this.key];
      node = new AsccpFlatNode(asccpNode);
    } else if (this._type === 'BCCP') {
      const bccpNode = this._ccGraph.graph.nodes[this.key];
      const bdtNode = next(this._ccGraph, bccpNode);
      node = new BccpFlatNode(bccpNode, bdtNode);
    } else if (this._type === 'DT') {
      const bdtNode = this._ccGraph.graph.nodes[this.key];
      node = new DtFlatNode(bdtNode);
    }
    node.level = 0;
    node.dataSource = this.dataSource;

    this.loadChildren(node);

    return node;
  }

  children(node: T): T[] {
    if (!node.expandable) {
      return [];
    }
    return node.children as T[];
  }

  loadChildren(node: T) {
    if (node.children.length > 0) {
      return;
    }

    node.children = this.getChildren(node);
    if (node.children.length === 0) {
      node.expandable = false;
    }
    node.children.map(e => e as T).forEach(e => {
      if (e instanceof AsccpFlatNode) {
        if (!e.isCycle) {
          this.loadChildren(e);
        }
      }
    });
  }

  getChildren(node: T): T[] {
    const nodes = this._ccGraph.graph.nodes;
    const edges = this._ccGraph.graph.edges;

    if (node instanceof BccpFlatNode) {
      if ((node as BccpFlatNode).bccNode && (node as BccpFlatNode).bccNode.entityType === 'Attribute') {
        return [];
      }
    }

    const edge = edges[getKey(node)];
    const targets = (!!edge) ? edge.targets : [];
    if (!targets || targets.length === 0) {
      return [];
    }

    const children = [];

    if (node instanceof DtFlatNode) {
      targets.forEach(target => {
        children.push(this.toDtScNode(nodes[target], node));
      });
      return children;
    }

    targets.forEach(target => {
      if (target.startsWith('ACC-')) {
        children.push(this.toAccNode(nodes[target], node));
      } else if (target.startsWith('ASCC-')) {
        const asccpNode = this.toAsccpNode(nodes[target], node);
        if (asccpNode.isUserExtensionGroup) {
          const uegAccNode = this.getChildren(asccpNode as unknown as T)[0];
          children.push(...this.getChildren(uegAccNode).map(e => {
            e.level = node.level + 1;
            e.parent = node;
            return e;
          }));
        } else {
          children.push(asccpNode);
        }
      } else if (target.startsWith('BCC-')) {
        children.push(this.toBccpNode(nodes[target], node));
      } else if (target.startsWith('DT-')) {
        const bdtScEdges = edges[target];
        if (bdtScEdges) {
          bdtScEdges.targets.map(e => nodes[e]).filter(e => e.cardinalityMax > 0).forEach(e => {
            children.push(this.toDtScNode(e, node));
          });
        }
      }
    });
    return children;
  }

  toAccNode(accNode: CcGraphNode, parent: CcFlatNode): AccFlatNode {
    const node = new AccFlatNode(accNode);
    node.state = accNode.state;
    node.deprecated = accNode.deprecated;
    node.level = parent.level + 1;
    node.parent = parent;
    node.dataSource = this.dataSource;
    return node;
  }

  toAsccpNode(asccNode: CcGraphNode, parent: CcFlatNode): AsccpFlatNode {
    const asccpNode = next(this._ccGraph, asccNode);
    const node = new AsccpFlatNode(asccpNode, asccNode);
    node.state = asccpNode.state;
    node.deprecated = asccpNode.deprecated || asccNode.deprecated;
    node.level = parent.level + 1;
    node.parent = parent;
    node.isCycle = this.detectCycle(node);
    node.dataSource = this.dataSource;
    return node;
  }

  toBccpNode(bccNode: CcGraphNode, parent: CcFlatNode): BccpFlatNode {
    const bccpNode = next(this._ccGraph, bccNode);
    const bdtNode = next(this._ccGraph, bccpNode);
    const node = new BccpFlatNode(bccpNode, bdtNode, bccNode);
    node.deprecated = bccpNode.deprecated || bccNode.deprecated || bdtNode.deprecated;
    node.state = bccpNode.state;
    node.level = parent.level + 1;
    node.parent = parent;
    node.dataSource = this.dataSource;
    return node;
  }

  toDtScNode(bdtScNode: CcGraphNode, parent: CcFlatNode): DtScFlatNode {
    const node = new DtScFlatNode(bdtScNode);
    node.deprecated = bdtScNode.deprecated;
    node.state = bdtScNode.state;
    node.level = parent.level + 1;
    node.parent = parent;
    node.dataSource = this.dataSource;
    return node;
  }

  detectCycle(node: AsccpFlatNode): boolean {
    const asccpManifestId = node.asccpNode.manifestId;
    let cur = node.parent;
    while (cur) {
      if (cur instanceof AsccpFlatNode && (cur as AsccpFlatNode).asccpManifestId === asccpManifestId) {
        return true;
      }
      cur = cur.parent;
    }
    return false;
  }
}

export class CcFlatNodeDataSource<T extends CcFlatNode> implements DataSource<T>, ChangeListener<T> {

  listeners: ChangeListener<DataSource<T>>[] = [];
  dataChange = new BehaviorSubject<T[]>([]);

  _hideCardinality = false;
  _hideProhibited = false;

  addListener(listener: ChangeListener<CcFlatNodeDataSource<T>>) {
    if (!!listener && this.listeners.indexOf(listener) === -1) {
      this.listeners.push(listener);
    }
  }

  get data(): T[] {
    return this.dataChange.value;
  }

  set data(value: T[]) {
    value.forEach(e => e.addChangeListener(this));
    this.dataChange.next(value);
    this.listeners.forEach(e => e.onChange(this, 'data', value));
  }

  init() {
    this.data = [this._database.rootNode as unknown as T, ];
  }

  getChanged(): T[] {
    let nodes = [this.data[0], ];
    const changedNodes = [];
    while (nodes.length > 0) {
      const node = nodes.shift();
      if (node.isChanged) {
        changedNodes.push(node);
      }
      const children = node.getChildren();
      if (children && children.length > 0) {
        nodes = children.concat(nodes) as T[];
      }
    }
    return changedNodes;
  }

  resetChanged() {
    this.getChanged().forEach(e => {
      e.reset();
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
    this.listeners.forEach(e => e.onChange(this, 'hideCardinality', hideCardinality));
  }

  get hideProhibited(): boolean {
    return this._hideProhibited;
  }

  set hideProhibited(hideProhibited: boolean) {
    if (this._hideProhibited === hideProhibited) {
      return;
    }

    this._hideProhibited = hideProhibited;
    if (hideProhibited) {
      this.data = this.data.filter(e => !e.inhibited);
    } else {
      this.data.forEach(e => {
        (e as CcFlatNode).expandable = undefined;
      });
      const expandedData = this.data.filter(e => this.isExpanded(e));
      this.collapse(this.data[0] as T);
      this.data = [this.data[0], ];

      for (const item of expandedData) {
        this.expand(item as T);
      }
    }

    this.listeners.forEach(e => e.onChange(this, 'hideProhibited', hideProhibited));
  }

  insertNodes(nodes: T[], siblingIndex: number) {
    if (!nodes || nodes.length === 0) {
      return;
    }
    const data = this.data;
    let start;
    if (siblingIndex === -1 || siblingIndex >= this.data[0].children.length) {
      start = data.length;
    } else {
      if (this.data[0].children.length === 0) {
        start = 1;
      } else {
        start = data.indexOf(this.data[0].children[siblingIndex] as T);
      }
    }

    const rootChildren = this.data[0].children;
    rootChildren.splice(siblingIndex, 0, nodes[0]);
    data[0].children = rootChildren;
    nodes[0].parent = this.data[0];

    const head = data.slice(0, start);
    const tail = data.slice(start, data.length);
    this.data = head.concat(nodes).concat(tail);
  }

  removeNodes(siblingIndex: number) {
    const data = this.data;
    let start;
    start = data.indexOf(this.data[0].children[siblingIndex] as T);
    let end = data.findIndex((e, index) => index > start && e.level === 1);
    if (end === -1) {
      end = data.length;
    }

    const rootChildren = this.data[0].children;
    data[0].children = rootChildren;
    rootChildren.splice(siblingIndex, 1);

    data.splice(start, end - start);
    this.data = data;
  }

  getNodesByLevelAndIndex(nodes: T[], siblingIndex: number) {
    const siblings = nodes.filter(e => e.level === 1);
    if (siblings.length < siblingIndex) {
      return [];
    }
    if (siblingIndex === -1) {
      siblingIndex = siblings.length - 1;
    }
    const start = nodes.indexOf(siblings[siblingIndex]);
    let end = nodes.findIndex((e, index) => index > start && e.level === 1);
    if (end === -1) {
      end = nodes.length;
    }
    return nodes.slice(start, end);
  }

  constructor(
    private _database: CcFlatNodeDatabase<T>,
    private service: CcNodeService,
    private delegatedListeners?: ChangeListener<T>[]
  ) {
    _database.dataSource = this;
  }

  connect(collectionViewer: CollectionViewer): Observable<readonly T[]> {
    return this.dataChange;
  }

  disconnect(collectionViewer: CollectionViewer): void {
  }

  get database(): CcFlatNodeDatabase<T> {
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

  toggleNode(node: T, expand: boolean) {
    if (!node) {
      return;
    }

    let children = this._database.children(node);
    if (this.hideProhibited) {
      children = children.filter(e => !e.inhibited);
    }
    const index = this.data.map(e => e.hashPath).indexOf(node.hashPath);
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
    }

    // notify the change
    this.dataChange.next(this.data);
    node.expanded = expand;
  }

  loadDetail(node: T, callbackFn?) {
    if (node.detail) {
      return callbackFn && callbackFn(node);
    }

    this.service.getDetail(node).subscribe(detail => {
      node.detail = detail;
      return callbackFn && callbackFn(detail);
    });
  }
}

export class CcFlatNodeDataSourceSearcher<T extends CcFlatNode>
  implements ChangeListener<CcFlatNodeDataSource<T>> {

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

  constructor(private dataSource: CcFlatNodeDataSource<T>,
              private database: CcFlatNodeDatabase<T>) {
    this.dataSource.addListener(this);
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

  onChange(entity: CcFlatNodeDataSource<T>, propertyName: string, val: any) {
    if (propertyName === 'hideProhibited') {
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
        if (evaluator.eval(item)) {
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
    return this.dataSource.data.map(e => e.hashPath).indexOf(node.hashPath);
  }

  protected getEvaluator(expr: string): ExpressionEvaluator<T> {
    return new PathLikeExpressionEvaluator(expr, false);
  }

}
