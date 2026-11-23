import {componentsOf, DagSearchResult, DagSearchScope} from './dag-path-search';
import {DagContextHost, DagNodeContext} from './dag-node-context';
import {GraphSearchSession, openGraphSearchSession} from './graph-search-session';
import {PathPattern} from './path-pattern';
import {SearchDag, sameSearchDag} from './search-dag';
import {AttrLookup, AttrValue} from './search-expression';
import {IdentifiableNode, identityOfNode} from './tree-node-identity';

/** The node shape a tree must offer to be searched through its DAG. */
export interface GraphSearchNode extends IdentifiableNode {
  parent?: GraphSearchNode;
  self?: GraphSearchNode;
}

/**
 * What a tree tells the graph search about itself. The tree stays the authority on what its nodes are called, what
 * their children are and in which order they come; the DAG is only a snapshot of that.
 */
export interface TreeGraphAdapter<N extends GraphSearchNode> {

  /**
   * The DAG of the tree as it is now. The same object is returned until the tree's structure changes, which is how
   * the search knows whether it has to hand over a new one.
   */
  dag(): SearchDag | undefined;

  /** The record of a node in `dag()`, or -1 when the tree has no such component. */
  recordOf(node: N): number;

  /** The names a search matches the node by, or undefined for a structural node (a group) that paths skip. */
  namesOf(node: N): string[] | undefined;

  /**
   * What a predicate may ask about the node (`used`, `displayName`, ...), or undefined when it has no attributes.
   * Must agree with the values the DAG's records carry (`SearchDag.attributes`).
   */
  attributesOf?(node: N): Record<string, AttrValue> | undefined;

  /** The node's children as the tree shows them (loading them if needed). */
  childrenOf(node: N): N[];
}

/** The graph a tree builds of itself: the DAG, and the record each of its nodes is in. */
export interface TreeSearchGraph<N> {
  dag: SearchDag;
  recordOf(node: N): number;
}

/** What a tree supplies to be searched; every tree is described to the search through `describeTree`, the same way. */
export interface TreeGraphSource<N extends GraphSearchNode, G extends TreeSearchGraph<N>> {
  /** The graph of the tree as it is now, or undefined when the tree cannot be described. */
  graph(): G | undefined;

  namesOf(node: N): string[] | undefined;

  /** What a predicate may ask about the node; `graph` is the one the latest `dag()` was made from. */
  attributesOf(node: N, graph: G | undefined): Record<string, AttrValue>;

  childrenOf(node: N): N[];
}

/**
 * The adapter the search reads a tree through. The DAG of a tree is made again whenever the search asks for it, and what
 * the graph says about nodes (their records, what it flags on them) is the one of the latest DAG.
 */
export function describeTree<N extends GraphSearchNode, G extends TreeSearchGraph<N>>(source: TreeGraphSource<N, G>): TreeGraphAdapter<N> {
  let graph: G | undefined;
  return {
    dag: () => {
      graph = source.graph();
      return graph?.dag;
    },
    recordOf: node => graph ? graph.recordOf(node) : -1,
    namesOf: node => source.namesOf(node),
    attributesOf: node => source.attributesOf(node, graph),
    childrenOf: node => source.childrenOf(node)
  };
}

/** Why a tree could not be searched through its DAG, so that it was walked instead. */
export type GraphRefusal = 'unsupported-expression' | 'no-dag' | 'scope-not-in-graph';

/**
 * One tree's searching through its DAG: it prepares the search (the DAG, the pattern and where the search starts),
 * runs it in the search session, and turns the answers (counts and child-index paths) back into tree nodes. Only the
 * nodes on the way to a match are ever built.
 */
export class GraphTreeSearch<N extends GraphSearchNode> {

  private session: GraphSearchSession | undefined;
  private loadedDag: SearchDag | undefined;
  private pattern: PathPattern | undefined;
  private scopeNode: N | undefined;
  private scope: DagSearchScope | undefined;
  private expr: string | undefined;
  private caseSensitive = false;
  private highlighted = new WeakMap<object, boolean>();
  /** Why the latest `prepare` returned false; undefined after one that went through. */
  refusal: GraphRefusal | undefined;

  constructor(private readonly adapter: TreeGraphAdapter<N>) {
  }

  /**
   * Gets a search ready; false when the tree cannot be searched this way (the expression has too many steps, or the
   * tree is unknown to the DAG), in which case nothing has changed.
   */
  prepare(expr: string, caseSensitive: boolean, scopeNode: N): boolean {
    const pattern = new PathPattern(expr, caseSensitive);
    const dag = this.adapter.dag();
    this.refusal = !pattern.supported ? 'unsupported-expression' : !dag ? 'no-dag' : undefined;
    if (this.refusal) {
      return false;
    }
    const scope = this.scopeOf(pattern, dag, scopeNode);
    if (!scope) {
      this.refusal = 'scope-not-in-graph';
      return false;
    }
    if (!this.session) {
      this.session = openGraphSearchSession();
    }
    if (dag !== this.loadedDag) {
      this.session.load(dag);
      this.loadedDag = dag;
    }
    this.pattern = pattern;
    this.scopeNode = scopeNode;
    this.scope = scope;
    this.expr = expr;
    this.caseSensitive = caseSensitive;
    this.highlighted = new WeakMap();
    return true;
  }

  /** The node the latest search started from. */
  get startedFrom(): N | undefined {
    return this.scopeNode;
  }

  /**
   * Whether the tree, as it is now, shows a search something else than the DAG the latest search ran on: a value it asks
   * about (a definition, whether a node is used, ...) or the structure has changed since, so that the same search would
   * find other nodes. The tree is described anew for the answer, which is what a search does first anyway.
   */
  isStale(): boolean {
    if (!this.loadedDag) {
      return false;
    }
    const dag = this.adapter.dag();
    return !dag || !sameSearchDag(dag, this.loadedDag);
  }

  /** The text a result list highlights: the last step of the expression. */
  get highlightKeyword(): string {
    return this.pattern.highlight;
  }

  /** Whether the results are marked as a whole (the expression names nothing to mark in them). */
  get marksWholeNode(): boolean {
    return !!this.pattern?.marksWholeNode;
  }

  run(onProgress: (visited: number) => void): Promise<DagSearchResult> {
    return this.session.search(this.expr, this.caseSensitive, this.scope, onProgress);
  }

  /** The n-th match (0-based) of the latest search, in the order the tree shows its nodes. */
  async nodeAt(k: number): Promise<N | undefined> {
    const path = await this.session.locate(k);
    // the search may have been cleared while the answer was on its way
    return path && this.scopeNode ? this.resolve(path) : undefined;
  }

  /** Whether the node is one of the latest search's matches. */
  isMatch(node: N): boolean {
    if (!this.pattern || !this.scopeNode) {
      return false;
    }
    const identity = this.identityOf(node);
    let known = this.highlighted.get(identity);
    if (known === undefined) {
      known = this.insideScope(node) && this.pattern.isMatch(this.maskOf(node));
      this.highlighted.set(identity, known);
    }
    return known;
  }

  clear() {
    this.pattern = undefined;
    this.scopeNode = undefined;
    this.scope = undefined;
    this.highlighted = new WeakMap();
  }

  close() {
    this.session?.close();
    this.session = undefined;
    this.loadedDag = undefined;
    this.clear();
  }

  /** The path from the root down to the node, structural nodes left out. */
  private chainOf(node: N): N[] {
    const chain: N[] = [];
    for (let n = node; n; n = n.parent as N) {
      if (this.adapter.namesOf(n)) {
        chain.push(n);
      }
    }
    return chain.reverse();
  }

  private maskOf(node: N): number {
    return this.stepChain(this.pattern, this.chainOf(node));
  }

  private stepChain(pattern: PathPattern, chain: N[], dag: SearchDag = this.loadedDag): number {
    if (pattern.needsContext) {
      return this.stepContexts(pattern, chain, dag);
    }
    const lookups: (AttrLookup | undefined)[] = pattern.hasPredicates ?
      chain.map(n => this.adapter.attributesOf?.(n)).map(values => values && (attribute => values[attribute])) : undefined;
    return pattern.stepAll(chain.map(n => this.adapter.namesOf(n)), lookups);
  }

  private insideScope(node: N): boolean {
    const scope = this.identityOf(this.scopeNode);
    for (let n = node; n; n = n.parent as N) {
      if (this.identityOf(n) === scope) {
        return true;
      }
    }
    return false;
  }

  private identityOf(node: N): object {
    return identityOfNode(node);
  }

  /**
   * The matcher state after the chain of nodes, each judged with its surroundings: the contexts are made from the DAG,
   * so a node's children, siblings and position are what the search itself sees.
   */
  private stepContexts(pattern: PathPattern, chain: N[], dag: SearchDag): number {
    const host = new ChainHost(dag, pattern);
    let mask = pattern.initialMask;
    let parent: DagNodeContext | undefined;
    let parentNode: N | undefined;
    chain.forEach((node, i) => {
      const record = this.adapter.recordOf(node);
      const ctx = parent ?
        parent.children()[this.indexAmongChildren(dag, this.adapter.recordOf(parentNode), record, parentNode, node)] as DagNodeContext
        : new DagNodeContext(host, record, undefined, 0, false, mask);
      mask = pattern.stepContext(mask, ctx);
      ctx.setStateBelow(mask);
      parent = ctx;
      parentNode = node;
    });
    return mask;
  }

  /** The place of the node among the DAG's children of its parent's record (which of several equal records it is). */
  private indexAmongChildren(dag: SearchDag, parentRecord: number, record: number, parentNode: N, node: N): number {
    const shown = this.adapter.childrenOf(parentNode);
    let occurrence = 0;
    const own = this.identityOf(node);
    for (const sibling of shown) {
      if (this.identityOf(sibling) === own) {
        break;
      }
      if (this.adapter.recordOf(sibling) === record) {
        occurrence++;
      }
    }
    const first = dag.childStart[parentRecord];
    for (let i = first; i < dag.childStart[parentRecord + 1]; i++) {
      if (dag.childList[i] === record && occurrence-- === 0) {
        return i - first;
      }
    }
    return 0;
  }

  private scopeOf(pattern: PathPattern, dag: SearchDag, scopeNode: N): DagSearchScope | undefined {
    const chain = this.chainOf(scopeNode);
    const records = chain.map(n => this.adapter.recordOf(n));
    if (chain.length === 0 || records.some(r => r < 0)) {
      return undefined;
    }
    return {
      ancestors: records.slice(0, -1),
      record: records[records.length - 1],
      mask: this.stepChain(pattern, chain, dag)
    };
  }

  /**
   * Follows a path of child indexes (as numbered in the DAG snapshot) from the scope node. The tree's own children
   * are matched to the snapshot's by record, not by position, because the tree may have been re-ordered since.
   */
  private resolve(path: number[]): N | undefined {
    const dag = this.loadedDag;
    let node = this.scopeNode;
    for (const index of path) {
      const parentRecord = this.adapter.recordOf(node);
      const first = dag.childStart[parentRecord];
      const wanted = dag.childList[first + index];
      let occurrence = 0;
      for (let i = first; i < first + index; i++) {
        if (dag.childList[i] === wanted) {
          occurrence++;
        }
      }
      node = this.adapter.childrenOf(node).filter(child => this.adapter.recordOf(child) === wanted)[occurrence];
      if (!node) {
        return undefined;
      }
    }
    return node;
  }
}


/** What the contexts of a chain of nodes ask of the page's thread: no budget to speak of, and nothing is above the chain. */
class ChainHost implements DagContextHost {

  private readonly components: ReturnType<typeof componentsOf>;
  private readonly remembered = new Map<string, boolean>();
  private steps = 0;

  constructor(readonly dag: SearchDag, private readonly pattern: PathPattern) {
    this.components = componentsOf(dag);
  }

  identityOf(record: number): number {
    return this.components.identityOf[record];
  }

  isCyclic(identity: number): boolean {
    return this.components.cyclic[identity] === 1;
  }

  onPath(): boolean {
    return false;
  }

  spend(): boolean {
    return ++this.steps < 5_000_000;
  }

  recall(test: number, record: number): boolean | undefined {
    return this.remembered.get(test + ':' + record);
  }

  remember(test: number, record: number, found: boolean) {
    this.remembered.set(test + ':' + record, found);
  }

  flag(mask: number, k: number): boolean {
    return this.pattern.ancestorFlag(mask, k);
  }
}
