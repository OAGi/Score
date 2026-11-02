import {Observable, Subject} from 'rxjs';

/**
 * How a feature takes rows out of a tree of any kind of node (core components, BIEs, ...): it answers, for a node,
 * whether the node stays. Unlike a {@link TreeNodeDecorator}, which only adds to a row, a filter changes which rows
 * exist, so the data source of the tree consults it through a {@link TreeFilterSet}.
 */
export interface TreeNodeFilter<N> {
  readonly id: string;
  /** Whether the filter is on; change it through {@link TreeFilterSet.setActive} so the tree learns of it. */
  active: boolean;
  /** Whether the node stays by itself. */
  accepts(node: N): boolean;
  /**
   * Whether a node that is not accepted stays all the same when a node below it is (the way to an accepted node is
   * kept). Only the children that are already loaded are looked at, so a filter never makes the tree load a branch.
   */
  keepAncestors?: boolean;
}

/** A filter that was switched on or off. */
export interface TreeFilterChange<N> {
  filter: TreeNodeFilter<N>;
  active: boolean;
}

export interface TreeFilterSetOptions<N> {
  /** The children of a node that are already loaded, all of them and without loading any. */
  childrenOf: (node: N) => N[];
  /** What tells one node from another where the tree reuses a node (a wrapper and what it wraps); the node by default. */
  identityOf?: (node: N) => unknown;
  /** A node that stays whatever the filters say (a search result the user has to be able to see). */
  exempt?: (node: N) => boolean;
}

/**
 * The filters of one tree. A node is shown when every filter that is on lets it stay. The tree asks it
 * ({@link visibleChildren}) wherever it lists the children of a node, and learns from {@link changes$} that the rows
 * have to be rebuilt.
 */
export class TreeFilterSet<N> {

  private readonly filters: TreeNodeFilter<N>[] = [];
  private readonly changes = new Subject<TreeFilterChange<N>>();

  constructor(private readonly options: TreeFilterSetOptions<N>) {
  }

  /** Announces each filter that is switched on or off. */
  get changes$(): Observable<TreeFilterChange<N>> {
    return this.changes.asObservable();
  }

  /** Whether any filter is on. */
  get active(): boolean {
    return this.filters.some(e => e.active);
  }

  add(filter: TreeNodeFilter<N>) {
    if (this.filters.some(e => e.id === filter.id)) {
      throw new Error('The tree already has a filter "' + filter.id + '".');
    }
    this.filters.push(filter);
  }

  /** Switches a filter on or off; false (and no announcement) when it already is as asked. */
  setActive(id: string, active: boolean): boolean {
    const filter = this.filters.find(e => e.id === id);
    if (!filter) {
      throw new Error('The tree has no filter "' + id + '".');
    }
    if (filter.active === active) {
      return false;
    }
    filter.active = active;
    this.changes.next({filter, active});
    return true;
  }

  isActive(id: string): boolean {
    return !!this.filters.find(e => e.id === id)?.active;
  }

  /** Whether the node is shown. */
  visible(node: N): boolean {
    return !!node && (!!this.options.exempt?.(node) || this.passes(node));
  }

  /** Whether every filter that is on lets the node stay, the exemption aside. */
  passes(node: N): boolean {
    if (!node) {
      return false;
    }
    return this.filters.every(filter => !filter.active || this.satisfies(filter, node));
  }

  /**
   * Whether a filter lets the node stay, whether the filter is on or not: the node is accepted, or the filter keeps
   * the way to an accepted node below it.
   */
  satisfies(filter: TreeNodeFilter<N>, node: N): boolean {
    return this.satisfiesWith(filter, node, new Set<unknown>());
  }

  /** The children that are shown; all of them while no filter is on. */
  visibleChildren<C extends N>(children: C[]): C[] {
    return this.active ? children.filter(child => this.visible(child)) : children;
  }

  private satisfiesWith(filter: TreeNodeFilter<N>, node: N, visited: Set<unknown>): boolean {
    if (!node) {
      return false;
    }
    if (filter.accepts(node)) {
      return true;
    }
    if (!filter.keepAncestors) {
      return false;
    }
    // A reused place can lead back to itself, and two places can share a path, so the node itself is what counts.
    const identity = this.options.identityOf ? this.options.identityOf(node) : node;
    if (visited.has(identity)) {
      return false;
    }
    visited.add(identity);
    return this.options.childrenOf(node).some(child => this.satisfiesWith(filter, child, visited));
  }
}
