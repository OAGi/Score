import {ChangeListener} from '../../bie-management/domain/bie-flat-tree';
import {TreeGraphAdapter} from '../../common/tree-search/graph-tree-search';
import {SearchDag, SearchDagBuilder} from '../../common/tree-search/search-dag';
import {TreeNodeSearcher, TreeSearchVisit} from '../../common/tree-search/tree-node-searcher';
import {FlatNode, getKey, next} from '../../common/flat-tree';
import {
  CcAccNodeInfo,
  CcAsccpNodeInfo,
  CcBccpNodeInfo,
  CcDtNodeInfo,
  CcDtScNodeInfo,
  CcGraph,
  CcGraphNode,
  CcNodeInfo
} from './core-component-node';
import {CcNodeService} from './core-component-node.service';
import {CollectionViewer, DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable} from 'rxjs';
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
  detail: CcNodeInfo;
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

  detail: CcNodeInfo;

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
      return (this.detail as CcAccNodeInfo).den;
    }
    return this.accNode.objectClassTerm + '. Details';
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcAccNodeInfo).objectClassTerm = val;
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
    return this.detail ? (this.detail as CcAccNodeInfo).library.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcAccNodeInfo).release.releaseId : undefined;
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
      return (this.detail as CcAsccpNodeInfo).asccp.propertyTerm;
    }
    return this.asccpNode.propertyTerm;
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcAsccpNodeInfo).asccp.propertyTerm = val;
    }
    this.asccpNode.propertyTerm = val;
  }

  get manifestId(): number {
    return this.asccpNode.manifestId;
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcAsccpNodeInfo).asccp.library.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcAsccpNodeInfo).asccp.release.releaseId : undefined;
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
    if (!!this.detail && !!(this.detail as CcAsccpNodeInfo).ascc && !!(this.detail as CcAsccpNodeInfo).ascc.cardinalityMin) {
      return (this.detail as CcAsccpNodeInfo).ascc.cardinalityMin;
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
    if (!!this.detail && !!(this.detail as CcAsccpNodeInfo).ascc && !!(this.detail as CcAsccpNodeInfo).ascc.cardinalityMax) {
      return (this.detail as CcAsccpNodeInfo).ascc.cardinalityMax;
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
      return (this.detail as CcBccpNodeInfo).bccp.propertyTerm;
    }
    return this.bccpNode.propertyTerm;
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcBccpNodeInfo).bccp.propertyTerm = val;
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
    return this.detail ? (this.detail as CcBccpNodeInfo).bccp.library.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcBccpNodeInfo).bccp.release.releaseId : undefined;
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
    if (!!this.detail && !!(this.detail as CcBccpNodeInfo).bcc && !!(this.detail as CcBccpNodeInfo).bcc.cardinalityMin) {
      return (this.detail as CcBccpNodeInfo).bcc.cardinalityMin;
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
    if (!!this.detail && !!(this.detail as CcBccpNodeInfo).bcc && !!(this.detail as CcBccpNodeInfo).bcc.cardinalityMax) {
      return (this.detail as CcBccpNodeInfo).bcc.cardinalityMax;
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
      return (this.detail as CcDtNodeInfo).den;
    }
    return this.dtNode.den;
  }

  set name(val: string) {
    if (this.detail) {
      (this.detail as CcDtNodeInfo).den = val;
    }
    this.dtNode.den = val;
  }

  get manifestId(): number {
    return this.dtNode.manifestId;
  }

  get libraryId(): number {
    return this.detail ? (this.detail as CcDtNodeInfo).library.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcDtNodeInfo).release.releaseId : undefined;
  }

  get den(): string {
    return this.dtNode.den;
  }

  get basedManifestId(): number {
    return this.detail ? (this.detail as CcDtNodeInfo).basedBdtManifestId : undefined;
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
    return this.detail ? (this.detail as CcDtScNodeInfo).library.libraryId : undefined;
  }

  get releaseId(): number {
    return this.detail ? (this.detail as CcDtScNodeInfo).release.releaseId : undefined;
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
    return !(this.dtScNode.basedDtScManifestId > 0);
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

  getChildren(node?: T): T[] {
    if (!node) {
      return [];
    }

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
        if (nodes[target]) {
          children.push(this.toAccNode(nodes[target], node));
        }
      } else if (target.startsWith('ASCC-')) {
        const asccpNode = this.toAsccpNode(nodes[target], node);
        if (asccpNode.isUserExtensionGroup) {
          children.push(...this.getUserExtensionGroupChildren(asccpNode, node));
        } else {
          children.push(asccpNode);
        }
      } else if (target.startsWith('BCC-')) {
        children.push(this.toBccpNode(nodes[target], node));
      } else if (target.startsWith('DT-')) {
        const bdtScEdges = edges[target];
        if (bdtScEdges) {
          bdtScEdges.targets.map(e => nodes[e]).filter(e => e && e.cardinalityMax > 0).forEach(e => {
            children.push(this.toDtScNode(e, node));
          });
        }
      }
    });
    return children;
  }

  protected getUserExtensionGroupChildren(asccpNode: AsccpFlatNode, parent: T): T[] {
    // UEG associations are flattened into their target ACC's children. A partially
    // populated graph can omit that target while an extension is being updated.
    const uegAccNode = this.getChildren(asccpNode as unknown as T)[0];
    if (!uegAccNode) {
      return [];
    }

    return this.getChildren(uegAccNode).map(e => {
      e.level = parent.level + 1;
      e.parent = parent;
      return e;
    });
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

  /**
   * The tree as a `SearchDag` (see `ModelBrowserNodeDatabase.searchGraph`), for searching the components instead of
   * the millions of nodes they unfold into.
   *
   * The components and their children are taken from the CC graph (through fresh nodes, as the tree's own nodes can be
   * edited). The nodes the tree has already built are then compared with them: a node whose name (its loaded details)
   * or children differ gets a record of its own, a "clone" showing the same component, so that a search sees the tree
   * as it is on screen. The result is built anew for every search.
   *
   * Undefined when the tree cannot be described this way, in which case it has to be searched by walking it.
   */
  searchGraph(root: T): {dag: SearchDag, recordOf: (node: T) => number} | undefined {
    const builder = new SearchDagBuilder();
    const clones = new WeakMap<object, number>();
    const hideProhibited = !!this.dataSource && this.dataSource.hideProhibited;
    const shown = (nodes: T[]): T[] => hideProhibited ? nodes.filter(e => !e.inhibited) : nodes;
    const representatives: T[] = [];
    let cloneCount = 0;

    const keyOf = (node: T): string => {
      const key = this.searchKey(node);
      if (key === undefined) {
        throw new Error('A node of an unknown kind cannot be searched through its graph.');
      }
      return key;
    };

    const plain = (node: T): number => {
      const {index, created} = builder.record(keyOf(node), [this.plainName(node)], keyOf(node),
        {name: this.plainName(node), type: node.type});
      if (created) {
        representatives.push(node);
      }
      return index;
    };

    const place = (node: T): {record: number, clone: boolean} => {
      const key = keyOf(node);
      const plainIndex = builder.indexOf(key);
      if (plainIndex === undefined) {
        throw new Error('A node the graph does not know cannot be searched through its graph.');
      }
      // A node that was found to have no children (loadChildren marks it not expandable) is as materialized as one with some.
      const materialized = node.getChildren().length > 0 || (node as unknown as CcFlatNodeImpl)._expandable === false;
      const children = materialized ? shown(node.children as T[]).map(place) : undefined;
      const plainChildren = builder.childrenOf(plainIndex);
      const sameChildren = !children || (children.length === plainChildren.length
        && children.every((child, i) => child.record === plainChildren[i]));
      if (node.name === this.plainName(node) && sameChildren) {
        return {record: plainIndex, clone: false};
      }
      const {index} = builder.record('@' + (cloneCount++), [node.name], key, {name: node.name, type: node.type});
      builder.setChildren(index, children ? children.map(child => child.record) : plainChildren);
      clones.set(node, index);
      return {record: index, clone: true};
    };

    try {
      plain(root);
      for (let i = 0; i < representatives.length; i++) {
        const node = representatives[i];
        builder.setChildren(builder.indexOf(keyOf(node)), shown(this.getChildren(node)).map(child => plain(child)));
      }
      place(root);
    } catch (e) {
      return undefined;
    }

    return {
      dag: builder.build(),
      recordOf: node => {
        const clone = clones.get(node);
        if (clone !== undefined) {
          return clone;
        }
        const key = this.searchKey(node);
        return key === undefined ? -1 : (builder.indexOf(key) ?? -1);
      }
    };
  }

  /** What makes two nodes the same component: they show the same thing and so have the same children. */
  private searchKey(node: T): string | undefined {
    if (node instanceof AccFlatNode) {
      return 'ACC-' + node.accNode.manifestId;
    }
    if (node instanceof AsccpFlatNode) {
      return 'ASCCP-' + node.asccpNode.manifestId;
    }
    if (node instanceof BccpFlatNode) {
      // an attribute has no supplementary components below it, the same property as an element does
      return 'BCCP-' + node.bccpNode.manifestId + (node.bccNode?.entityType === 'Attribute' ? '#attribute' : '');
    }
    if (node instanceof DtFlatNode) {
      return 'DT-' + node.dtNode.manifestId;
    }
    if (node instanceof DtScFlatNode) {
      return 'SC-' + node.dtScNode.manifestId;
    }
    return undefined;
  }

  /** The name a node of the component has before its details are loaded (which can replace it). */
  private plainName(node: T): string {
    if (node instanceof AccFlatNode) {
      return node.accNode.objectClassTerm + '. Details';
    }
    if (node instanceof AsccpFlatNode) {
      return node.asccpNode.propertyTerm;
    }
    if (node instanceof BccpFlatNode) {
      return node.bccpNode.propertyTerm;
    }
    if (node instanceof DtFlatNode) {
      return node.dtNode.den;
    }
    return node.name;
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
    if (!this.applyToggle(node, expand)) {
      return;
    }

    // notify the change
    this.dataChange.next(this.data);
    node.expanded = expand;
  }

  /**
   * Expands the node and every collapsed ancestor, announcing the change once. Expanding them one by one announces
   * each, and every announcement re-renders the rows on screen.
   */
  expandTo(node: T) {
    const chain: T[] = [];
    for (let n = node; n; n = n.parent as T) {
      if (!this.isExpanded(n)) {
        chain.push(n);
      }
    }
    const expanded = chain.reverse().filter(n => {
      const applied = this.applyToggle(n, true);
      if (applied) {
        n.expanded = true;
      }
      return applied;
    });
    if (expanded.length > 0) {
      this.dataChange.next(this.data);
    }
  }

  /** Changes the rows for the toggle without announcing it; false when there is nothing to change. */
  private applyToggle(node: T, expand: boolean): boolean {
    if (!node) {
      return false;
    }

    let children = this._database.children(node);
    if (this.hideProhibited) {
      children = children.filter(e => !e.inhibited);
    }
    const index = this.data.map(e => e.hashPath).indexOf(node.hashPath);
    if (!children || index < 0) {
      // If no children, or cannot find the node, no op
      return false;
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
    return true;
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
  extends TreeNodeSearcher<T, CcFlatNodeDataSource<T>>
  implements ChangeListener<CcFlatNodeDataSource<T>> {

  constructor(dataSource: CcFlatNodeDataSource<T>,
              private database: CcFlatNodeDatabase<T>) {
    super(dataSource);
    dataSource.addListener(this);
  }

  protected override graphAdapter(): TreeGraphAdapter<T> {
    let graph: ReturnType<CcFlatNodeDatabase<T>['searchGraph']>;
    return {
      dag: () => {
        graph = this.database.searchGraph(this.dataSource.data[0]);
        return graph?.dag;
      },
      recordOf: node => graph ? graph.recordOf(node) : -1,
      namesOf: node => [node.name],
      attributesOf: node => ({name: node.name, type: node.type}),
      childrenOf: node => this.database.children(node)
    };
  }

  onChange(entity: CcFlatNodeDataSource<T>, propertyName: string, val: any) {
    if (propertyName === 'hideProhibited') {
      this.resetSearch();
    }
  }

  protected override visit(node: T): TreeSearchVisit<T> {
    const expanded = node.expandable;
    if (expanded) {
      this.database.loadChildren(node);
    }
    return {children: node.children as T[], expanded};
  }

  protected visibleIndex(node: T): number {
    return this.dataSource.data.map(e => e.hashPath).indexOf(node.hashPath);
  }

}
