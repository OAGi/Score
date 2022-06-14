import {BieEditFlatNodeFlattener} from '../bie-management/bie-edit/bie-edit.component';
import {BbiepFlatNode} from '../bie-management/domain/bie-flat-tree';
import {AsccpFlatNode, BccpFlatNode, CcFlatNode, CcFlatNodeFlattener} from '../cc-management/domain/cc-flat-tree';
import {CcGraph, CcGraphNode} from '../cc-management/domain/core-component-node';
import {FlatTreeControl, FlatTreeControlOptions} from '@angular/cdk/tree';
import {CollectionViewer, DataSource, SelectionChange} from '@angular/cdk/collections';
import {BehaviorSubject, empty, Observable} from 'rxjs';

export interface FlatNode {
  type: string;
  name: string;
  level: number;
  expanded: boolean;
  expandable: boolean;

  parent?: FlatNode;
  children: FlatNode[];
}

export class FlatNodeImpl implements FlatNode {
  private _type: string;
  name: string;
  level: number;
  _expanded: boolean;

  parent?: FlatNode;
  children: FlatNode[] = [];

  get type(): string {
    return this._type;
  }

  set type(value: string) {
    this._type = value;
  }

  get expanded(): boolean {
    return this._expanded || false;
  }

  set expanded(expanded: boolean) {
    this._expanded = expanded;
  }

  get expandable(): boolean {
    return this.children && this.children.length > 0;
  }
}

export function getKey(node: CcGraphNode): string {
  return node.type.toUpperCase() + '-' + node.manifestId;
}

export function next(ccGraph: CcGraph, node: CcGraphNode): CcGraphNode {
  return ccGraph.graph.nodes[ccGraph.graph.edges[getKey(node)].targets[0]];
}

export interface FlatNodeFlattener<T extends FlatNode> {
  addListener(listener: FlatNodeFlattenerListener<T>);
  flatten(): T[];
}

export interface FlatNodeFlattenerListener<T extends FlatNode> {
  onFlatten(node: T);
}

export class VSFlatTreeControl<T extends FlatNode> extends FlatTreeControl<T> {
  dataSource: VSFlatTreeDataSource<T>;

  ccFlattener: CcFlatNodeFlattener;

  constructor(isExpandable?: (dataNode: T) => boolean, options?: FlatTreeControlOptions<T, T> | undefined, flattener?: CcFlatNodeFlattener) {
    super(t => t.level, (isExpandable) ? isExpandable : t => t.expandable, options);
    this.ccFlattener = flattener;
  }

  isExpanded(dataNode: T): boolean {
    if (!dataNode) {
      return false;
    }
    return dataNode.expanded;
  }

  toggle(dataNode: T) {
    if (!this.isExpandable(dataNode)) {
      return;
    }

    if (this.isExpanded(dataNode)) {
      this.collapse(dataNode);
    } else {
      this.expand(dataNode);
    }
  }

  expand(dataNode: T, reset?: boolean) {
    if (!dataNode || !this.isExpandable(dataNode) || this.isExpanded(dataNode)) {
      return;
    }

    dataNode.expanded = true;

    if (!!this.ccFlattener && dataNode.children.length === 0) {
      this.ccFlattener.expand(dataNode as unknown as CcFlatNode);

      const index = this.dataSource.cachedData.indexOf(dataNode);
      this.dataSource.cachedData.splice(index + 1, 0, ...dataNode.children as T[]);
    }

    if (dataNode.parent) {
      this.expand(dataNode.parent as T, false);
    }

    if (reset === undefined || reset) {
      this.dataSource.resetData();
    }
  }

  collapse(dataNode: T, reset?: boolean) {
    if (!dataNode || !this.isExpandable(dataNode) || !this.isExpanded(dataNode)) {
      return;
    }

    dataNode.expanded = false;
    this.collapseDescendants(dataNode, reset);
  }

  expandDescendants(dataNode: T, level?: number) {
    if (!dataNode) {
      return;
    }

    level = level || 0;

    const cachedData = this.dataSource.cachedData;
    const start = cachedData.indexOf(dataNode);
    const end = (dataNode.level === 0) ? cachedData.length :
      cachedData.findIndex((e, index) => index > start && e.level === dataNode.level);
    cachedData.slice(start, end).filter(e => this.isExpandable(e))
      .filter(e => (level > 0) ? e.level <= dataNode.level + level : true)
      .forEach(e => this.expand(e, false));

    this.dataSource.resetData();
  }

  collapseDescendants(dataNode: T, reset?: boolean) {
    if (!dataNode) {
      return;
    }

    const cachedData = this.dataSource.cachedData;
    const start = cachedData.indexOf(dataNode);
    const end = (dataNode.level === 0) ? cachedData.length :
      cachedData.findIndex((e, index) => index > start && e.level === dataNode.level);
    cachedData.slice(start, end).filter(e => this.isExpandable(e)).forEach(e => e.expanded = false);

    if (reset === undefined || reset) {
      this.dataSource.resetData();
    }
  }
}


export class VSFlatTreeDataSource<T extends FlatNode> implements DataSource<T> {

  private _cachedData: T[];
  private _data: T[];
  private _dataStream: BehaviorSubject<T[]>;

  treeControl: VSFlatTreeControl<T>;

  constructor(treeControl: VSFlatTreeControl<T>, data: T[]) {
    this._cachedData = data;
    this._dataStream = new BehaviorSubject<T[]>(this.data);

    this.treeControl = treeControl;
    this.treeControl.dataSource = this;
  }

  get cachedData(): T[] {
    return this._cachedData;
  }

  set cachedData(data: T[]) {
    this._cachedData = data;
    this.resetData();
  }

  get data(): T[] {
    if (!this._data) {
      this._data = this._cachedData.filter(e => this.dataFilter(e));
    }
    return this._data;
  }

  set data(data: T[]) {
    this._data = data;
  }

  dataFilter(node: T): boolean {
    if (node.level === 0) {
      return true;
    }
    if (!this.treeControl) {
      return false;
    }
    return this.treeControl.isExpanded(node) || this.treeControl.isExpanded(node.parent as T);
  }

  connect(collectionViewer: CollectionViewer): Observable<T[]> {
    this.treeControl.expansionModel.changed.subscribe(change => {
      if ((change as SelectionChange<T>).added ||
        (change as SelectionChange<T>).removed) {
        this.handleTreeControl(change as SelectionChange<T>);
      }
    });

    return this._dataStream;
  }

  disconnect(collectionViewer: CollectionViewer): void {
  }

  handleTreeControl(change: SelectionChange<T>) {
    if (change.added) {
      change.added.forEach(node => this.treeControl.toggle(node));
    }
    if (change.removed) {
      change.removed.slice().reverse().forEach(node => this.treeControl.toggle(node));
    }
  }

  resetData() {
    this.data = undefined;
    this._dataStream.next(this.data);
  }
}


export interface ExpressionEvaluator<T extends FlatNode> {
  range(data: T[], current: T): T[];
  eval(node: T): boolean;
  keywordForHighlight(): string;
}

export class ExactMatchExpressionEvaluator<T extends FlatNode> implements ExpressionEvaluator<T> {
  private _expr: string;
  private _caseSensitive: boolean;
  private _excludeSCs: boolean;

  constructor(expr: string, caseSensitive?: boolean, excludeSCs?: boolean) {
    this._expr = expr.replace(/([a-z])([A-Z])/g, '$1 $2').trim();
    this._caseSensitive = caseSensitive || false;
    this._excludeSCs = excludeSCs || false;
  }

  range(data: T[], current: T): T[] {
    const start = data.indexOf(current);
    let end = -1;
    if (current.level > 0) {
      end = data.findIndex((e, index) => index > start && e.level === current.level);
    }
    end = end === -1 ? data.length : end;
    return data.slice(start, end);
  }

  eval(node: T): boolean {
    if (this._excludeSCs && node.type.toUpperCase().indexOf("SC") > -1) {
      return false;
    }
    if (this._caseSensitive) {
      return node.name.indexOf(this._expr) > -1;
    } else {
      return node.name.toLowerCase().indexOf(this._expr.toLowerCase()) > -1;
    }
  }

  keywordForHighlight(): string {
    return this._expr;
  }
}

export class PathLikeExpressionEvaluator<T extends FlatNode> implements ExpressionEvaluator<T> {
  private _root: boolean = false;
  private _tokens: string[];
  private _caseSensitive: boolean;
  private _excludeSCs: boolean;

  constructor(expr: string, caseSensitive?: boolean, excludeSCs?: boolean) {
    if (expr.startsWith('/')) {
      this._root = true;
      expr = expr.substring(1);
    }
    this._tokens = expr.split('/');
    this._caseSensitive = caseSensitive || false;
    this._excludeSCs = excludeSCs;
  }

  range(data: T[], current: T): T[] {
    if (this._root) {
      current = data[0];
    }
    const start = data.indexOf(current);
    let end = -1;
    if (current.level > 0) {
      end = data.findIndex((e, index) => index > start && e.level === current.level);
    }
    end = end === -1 ? data.length : end;
    return data.slice(start, end);
  }

  eval(node: T): boolean {
    let cur = node;
    for (let token of Object.assign([], this._tokens).reverse()) {
      if (!cur) {
        return false;
      }
      if (!this.doEval(cur, this.split(token))) {
        return false;
      }
      cur = this.next(cur);
    }
    return true;
  }

  keywordForHighlight(): string {
    return this.split(Object.assign([], this._tokens).reverse()[0]);
  }

  private split(token: string): string {
    return token.replace(/([a-z])([A-Z])/g, '$1 $2').trim();
  }

  protected doEval(node: T, token: string): boolean {
    if (this._excludeSCs && node.type.toUpperCase().indexOf("_SC") > -1) {
      return false;
    }
    if (this._caseSensitive) {
      return node.name.indexOf(token) > -1;
    } else {
      return node.name.toLowerCase().indexOf(token.toLowerCase()) > -1;
    }
  }

  protected next(node: T): T {
    return node.parent as T;
  }
}

export class DataSourceSearcher<T extends FlatNode> {

  private dataSource: VSFlatTreeDataSource<T>;

  searchKeyword: string = '';
  inputKeyword: string = '';
  searchResult: T[] = [];
  searchIndex: number = 0;
  isSearching: boolean = false;
  searchPrefix: string = '';
  excludeSCs: boolean = true;

  constructor(dataSource: VSFlatTreeDataSource<T>, excludeSCs?: boolean) {
    this.dataSource = dataSource;
    this.excludeSCs = excludeSCs || false;
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
      subscriber.next(this.getNodeIndex(this.searchResult[this.searchIndex]));
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
    if (this.searchKeyword !== inputKeyword || force) {
      const data = this.dataSource.cachedData;
      const evaluator = this.getEvaluator(inputKeyword);
      const range = evaluator.range(data, selectedNode);
      this.searchResult = range.filter(e => evaluator.eval(e));
      this.searchKeyword = evaluator.keywordForHighlight();
      if (this.searchResult.length > 0) {
        this.dataSource.treeControl.collapse(selectedNode);
        this.searchResult.forEach(e => {
          // @ts-ignore
          this.dataSource.treeControl.expand(e.parent, false);
        });
        this.dataSource.resetData();
        this.searchIndex = 0;
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

    return new Observable(subscriber => {
      subscriber.next(this.getNodeIndex(this.searchResult[this.searchIndex]));
      subscriber.complete();
    });
  }

  resetSearch() {
    this.inputKeyword = undefined;
    this.searchKeyword = undefined;
    this.searchResult = [];
    this.searchIndex = 0;
    this.isSearching = false;
    this.searchPrefix = '';
  }

  getNodeIndex(node: T) {
    return this.dataSource.data.indexOf(node);
  }

  protected getEvaluator(expr: string): ExpressionEvaluator<T> {
    return new PathLikeExpressionEvaluator(expr, false, this.excludeSCs);
  }
}
