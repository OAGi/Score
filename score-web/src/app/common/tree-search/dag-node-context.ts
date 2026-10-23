import {AttrValue, NodeContext} from './search-eval';
import {SearchDag} from './search-dag';

/** What a node of a `SearchDag` needs from the search that is looking at it. */
export interface DagContextHost {
  readonly dag: SearchDag;

  identityOf(record: number): number;

  /** Whether the component is in a cycle (what is below it can then depend on what is above it). */
  isCyclic(identity: number): boolean;

  /** Whether the component is among the ancestors of the nodes being judged, which makes it a leaf where it shows again. */
  onPath(identity: number): boolean;

  /** Takes one step of the search's budget; false once it is spent. */
  spend(): boolean;

  recall(test: number, record: number): boolean | undefined;

  remember(test: number, record: number, found: boolean): void;

  /** Whether the mask says an ancestor passed the ancestor test `k`. */
  flag(mask: number, k: number): boolean;
}

/**
 * A node of the tree a `SearchDag` unfolds into, as a search expression sees it. A record stands for the component
 * wherever it shows, so a context is made for one place: it knows the parent it was reached from (and, from that,
 * its siblings), and `incoming` is the matcher state of that parent, which carries what the ancestors tell.
 */
export class DagNodeContext implements NodeContext {

  private kids: DagNodeContext[] | undefined;
  /** The matcher state the children are stepped from, once it is known (until then, the state this node was stepped from). */
  private below: number | undefined;

  /**
   * @param up the context this one was reached from; undefined for the root of the walk (or when it is not known)
   * @param index the place among the parent's children, -1 when the node is only a parent to look down from
   * @param leaf whether the component is already among the ancestors, which leaves the node without children
   * @param incoming the matcher state the node is stepped from (the parent's), or of the children when it is a parent
   */
  constructor(private readonly host: DagContextHost, readonly record: number, private readonly up: DagNodeContext | undefined,
              private readonly index: number, private readonly leaf: boolean, private readonly incoming: number) {
  }

  get names(): readonly string[] {
    return this.host.dag.names[this.record];
  }

  attr(attribute: string): AttrValue {
    return this.host.dag.attributes?.[attribute]?.[this.record];
  }

  children(): NodeContext[] {
    if (this.kids) {
      return this.kids;
    }
    if (this.leaf) {
      return this.kids = [];
    }
    const {childStart, childList} = this.host.dag;
    const first = childStart[this.record];
    this.kids = [];
    for (let i = first; i < childStart[this.record + 1]; i++) {
      const record = childList[i];
      const identity = this.host.identityOf(record);
      this.kids.push(new DagNodeContext(this.host, record, this, i - first,
        this.host.onPath(identity) || this.chainHas(identity), this.below ?? this.incoming));
    }
    return this.kids;
  }

  /** Tells the matcher state of the node itself, which is what its children are stepped from. */
  setStateBelow(mask: number): this {
    this.below = mask;
    this.kids = undefined;
    return this;
  }

  parent(): NodeContext | undefined {
    return this.up;
  }

  siblings(): {list: NodeContext[], index: number} {
    if (this.up) {
      return {list: this.up.children(), index: this.index};
    }
    if (this.index === 0) {
      return {list: [this], index: 0};
    }
    throw new Error('The siblings of this node are not known.');
  }

  ancestorFlag(k: number): boolean {
    return this.host.flag(this.incoming, k);
  }

  spend(): boolean {
    return this.host.spend();
  }

  recall(test: number): boolean | undefined {
    return this.memoizable ? this.host.recall(test, this.record) : undefined;
  }

  remember(test: number, found: boolean) {
    if (this.memoizable) {
      this.host.remember(test, this.record, found);
    }
  }

  /** What is below a component that is in no cycle is the same wherever the component shows. */
  private get memoizable(): boolean {
    return !this.leaf && !this.host.isCyclic(this.host.identityOf(this.record));
  }

  private chainHas(identity: number): boolean {
    for (let c: DagNodeContext | undefined = this; c; c = c.up) {
      if (this.host.identityOf(c.record) === identity) {
        return true;
      }
    }
    return false;
  }
}
