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
