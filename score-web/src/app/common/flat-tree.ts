import {CcGraph, CcGraphNode} from '../cc-management/domain/core-component-node';

export interface FlatNode {
  type: string;
  name: string;
  level: number;
  expanded: boolean;
  expandable: boolean;

  parent?: FlatNode;
  children: FlatNode[];

  getChildren(options?: any | undefined): FlatNode[];
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

  getChildren(options?: any | undefined): FlatNode[] {
    return this.children;
  }
}

export function getKey(node: CcGraphNode): string {
  return node.type.toUpperCase() + '-' + node.manifestId;
}

export function next(ccGraph: CcGraph, node: CcGraphNode): CcGraphNode {
  return ccGraph.graph.nodes[ccGraph.graph.edges[getKey(node)].targets[0]];
}


export interface ExpressionEvaluator<T extends FlatNode> {
  range(data: T[], current: T): T[];
  eval(node: T): boolean;
  keywordForHighlight(): string;
}

export class ExactMatchExpressionEvaluator<T extends FlatNode> implements ExpressionEvaluator<T> {
  private _expr: string;
  private _caseSensitive: boolean;

  constructor(expr: string, caseSensitive?: boolean) {
    this._expr = expr.replace(/([a-z])([A-Z])/g, '$1 $2').trim();
    this._caseSensitive = caseSensitive || false;
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
  private _root = false;
  private _tokens: string[];
  private _caseSensitive: boolean;

  constructor(expr: string, caseSensitive?: boolean) {
    if (expr.startsWith('/')) {
      this._root = true;
      expr = expr.substring(1);
    }
    this._tokens = expr.split('/');
    this._caseSensitive = caseSensitive || false;
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
    for (const token of Object.assign([], this._tokens).reverse()) {
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
