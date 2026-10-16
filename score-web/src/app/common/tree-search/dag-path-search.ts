import {PathPattern} from './path-pattern';
import {SearchDag} from './search-dag';

/** Where a search starts: the node the user selected (or the root) and what is already known about its ancestors. */
export interface DagSearchScope {
  /** The records of the node's ancestors, root first (the node itself excluded). */
  ancestors: number[];
  /** The node's own record. */
  record: number;
  /** The pattern's match state after the whole path down to and including the node (`PathPattern.stepAll`). */
  mask: number;
}

export interface DagSearchResult {
  /** The matches in the scope node's subtree, the node itself included. Only a lower bound unless `exact`. */
  total: number;
  /** False when the search stopped on its budget, so that `total` may be too small. */
  exact: boolean;
  /** How many nodes the tree has in the scope (the node itself included): what the search covered, without building it. */
  nodes: number;
  /** How many distinct records the search reached. */
  visited: number;
}

/** A search's default budget: how many parent-child steps it may take before it gives a lower bound instead. */
export const DEFAULT_DAG_SEARCH_BUDGET = 20_000_000;

/** What the "no component below itself" rule works on: components (identities), not the records standing in for them. */
interface Components {
  /** Per record, the identity of its component. */
  identityOf: Int32Array;
  identityCount: number;
  /** Per identity, its strongly connected component and whether that is a real cycle. */
  sccOf: Int32Array;
  cyclic: Uint8Array;
}

const componentCache = new WeakMap<SearchDag, Components>();

/**
 * Finds the identities and, among them, the strongly connected components (the graph is not acyclic when a component
 * can contain itself). Only identities inside a cycle make the number of matches below them depend on their
 * ancestors, so only they need more than their record and match state as a memo key.
 */
function componentsOf(dag: SearchDag): Components {
  const cached = componentCache.get(dag);
  if (cached) {
    return cached;
  }
  const records = dag.names.length;
  const identityOf = Int32Array.from({length: records}, (_, r) => dag.identity ? dag.identity[r] : r);
  const n = identityOf.reduce((max, id) => Math.max(max, id + 1), 0);
  const adjacent: number[][] = Array.from({length: n}, () => []);
  for (let r = 0; r < records; r++) {
    for (let i = dag.childStart[r]; i < dag.childStart[r + 1]; i++) {
      adjacent[identityOf[r]].push(identityOf[dag.childList[i]]);
    }
  }

  const sccOf = new Int32Array(n).fill(-1);
  const cyclic = new Uint8Array(n);
  const index = new Int32Array(n).fill(-1);
  const low = new Int32Array(n);
  const onStack = new Uint8Array(n);
  const stack: number[] = [];
  let counter = 0;
  let sccCount = 0;

  for (let root = 0; root < n; root++) {
    if (index[root] !== -1) {
      continue;
    }
    // iterative Tarjan: each frame is [identity, next neighbour position]
    const frames: [number, number][] = [[root, 0]];
    index[root] = low[root] = counter++;
    stack.push(root);
    onStack[root] = 1;
    while (frames.length > 0) {
      const frame = frames[frames.length - 1];
      const v = frame[0];
      if (frame[1] < adjacent[v].length) {
        const w = adjacent[v][frame[1]++];
        if (index[w] === -1) {
          index[w] = low[w] = counter++;
          stack.push(w);
          onStack[w] = 1;
          frames.push([w, 0]);
        } else if (onStack[w]) {
          low[v] = Math.min(low[v], index[w]);
        }
        continue;
      }
      frames.pop();
      if (frames.length > 0) {
        const parent = frames[frames.length - 1][0];
        low[parent] = Math.min(low[parent], low[v]);
      }
      if (low[v] === index[v]) {
        const members: number[] = [];
        let w: number;
        do {
          w = stack.pop();
          onStack[w] = 0;
          sccOf[w] = sccCount;
          members.push(w);
        } while (w !== v);
        sccCount++;
        if (members.length > 1 || adjacent[v].includes(v)) {
          members.forEach(m => cyclic[m] = 1);
        }
      }
    }
  }
  const result = {identityOf, identityCount: n, sccOf, cyclic};
  componentCache.set(dag, result);
  return result;
}

/**
 * Searches the tree a `SearchDag` unfolds into, for the nodes a `PathPattern` matches, without unfolding it.
 *
 * A component that shows up in many places is worked out once per match state: `count(record, mask)` is the number of
 * matches below a node of that record reached in that state, so the whole tree is counted in time proportional to the
 * DAG, and the n-th match (in the order the tree shows its nodes) is found by following the counts down a single path.
 *
 * A component is not expanded below itself: a node whose component is already among its ancestors is a node of the
 * tree (it can match) but has no children, which is how a recursive structure stays finite. (Records that stand for
 * one place of a component, see `SearchDag.identity`, count as that component.)
 */
export class DagPathSearch {

  private readonly identityOf: Int32Array;
  private readonly sccOf: Int32Array;
  private readonly cyclic: Uint8Array;
  private readonly onPath: Uint8Array;
  private readonly path: number[] = [];
  private readonly acyclicMemo = new Map<number, number>();
  private readonly cyclicMemo = new Map<string, number>();
  private readonly sizeMemo = new Map<number | string, number>();
  private readonly maskSpace: number;

  private scope: DagSearchScope | undefined;
  private steps = 0;
  private visited = 0;
  private reachedCount = 0;
  private readonly reached: Uint8Array;
  private truncated = false;

  constructor(private readonly dag: SearchDag,
              private readonly pattern: PathPattern,
              private readonly budget = DEFAULT_DAG_SEARCH_BUDGET,
              private readonly onProgress?: (visited: number) => void) {
    const {identityOf, identityCount, sccOf, cyclic} = componentsOf(dag);
    this.identityOf = identityOf;
    this.sccOf = sccOf;
    this.cyclic = cyclic;
    this.onPath = new Uint8Array(identityCount);
    this.reached = new Uint8Array(dag.names.length);
    this.maskSpace = 2 ** (pattern.tokens.length + 1);
  }

  search(scope: DagSearchScope): DagSearchResult {
    this.scope = scope;
    this.steps = 0;
    this.visited = 0;
    this.reachedCount = 0;
    this.reached.fill(0);
    this.truncated = false;
    const below = this.withPath(scope, () => this.count(scope.record, scope.mask));
    const total = (this.pattern.isMatch(scope.mask) ? 1 : 0) + below;
    const nodes = 1 + this.withPath(scope, () => this.size(scope.record));
    return {total, exact: !this.truncated, nodes, visited: this.reachedCount};
  }

  /**
   * The position of the match with the given 0-based number: for each level below the scope node, which child to
   * take (an index into that record's children). An empty path is the scope node itself.
   */
  locate(k: number): number[] | undefined {
    const scope = this.scope;
    if (!scope || k < 0) {
      return undefined;
    }
    this.steps = 0;
    this.truncated = false;
    if (this.pattern.isMatch(scope.mask)) {
      if (k === 0) {
        return [];
      }
      k--;
    }
    return this.withPath(scope, () => this.descend(scope.record, scope.mask, k));
  }

  private withPath<T>(scope: DagSearchScope, task: () => T): T {
    const pushed = [...scope.ancestors, scope.record];
    pushed.forEach(r => this.enter(r));
    try {
      return task();
    } finally {
      pushed.reverse().forEach(r => this.leave(r));
    }
  }

  /** The number of matches among the descendants of a node of `record` that is in match state `mask`. */
  private count(record: number, mask: number): number {
    const key = this.memoKey(record, mask);
    const memo = typeof key === 'number' ? this.acyclicMemo : this.cyclicMemo;
    const known = (memo as Map<number | string, number>).get(key);
    if (known !== undefined) {
      return known;
    }
    if (this.truncated) {
      return 0;
    }
    this.visited++;
    if (!this.reached[record]) {
      this.reached[record] = 1;
      this.reachedCount++;
    }
    if (this.onProgress && (this.visited & 0xfff) === 0) {
      this.onProgress(this.visited);
    }

    let total = 0;
    const {childStart, childList, names} = this.dag;
    for (let i = childStart[record]; i < childStart[record + 1]; i++) {
      if (++this.steps > this.budget) {
        this.truncated = true;
        return total;
      }
      const child = childList[i];
      const childMask = this.pattern.step(mask, ...names[child]);
      if (this.pattern.isMatch(childMask)) {
        total++;
      }
      if (!this.onPath[this.identityOf[child]]) {
        this.enter(child);
        total += this.count(child, childMask);
        this.leave(child);
      }
    }
    if (!this.truncated) {
      (memo as Map<number | string, number>).set(key, total);
    }
    return total;
  }

  /** The number of nodes below a node of `record`; unlike the matches, it does not depend on the match state. */
  private size(record: number): number {
    const key = this.memoKey(record, 0);
    const known = this.sizeMemo.get(key);
    if (known !== undefined) {
      return known;
    }
    if (this.truncated) {
      return 0;
    }
    let total = 0;
    const {childStart, childList} = this.dag;
    for (let i = childStart[record]; i < childStart[record + 1]; i++) {
      if (++this.steps > this.budget) {
        this.truncated = true;
        return total;
      }
      const child = childList[i];
      total++;
      if (!this.onPath[this.identityOf[child]]) {
        this.enter(child);
        total += this.size(child);
        this.leave(child);
      }
    }
    if (!this.truncated) {
      this.sizeMemo.set(key, total);
    }
    return total;
  }

  private descend(record: number, mask: number, k: number): number[] | undefined {
    const {childStart, childList, names} = this.dag;
    for (let i = childStart[record]; i < childStart[record + 1]; i++) {
      const child = childList[i];
      const childMask = this.pattern.step(mask, ...names[child]);
      if (this.pattern.isMatch(childMask)) {
        if (k === 0) {
          return [i - childStart[record]];
        }
        k--;
      }
      if (this.onPath[this.identityOf[child]]) {
        continue;
      }
      this.enter(child);
      const below = this.count(child, childMask);
      let found: number[] | undefined;
      if (k < below) {
        found = this.descend(child, childMask, k);
      }
      this.leave(child);
      if (k < below) {
        return found && [i - childStart[record], ...found];
      }
      k -= below;
    }
    return undefined;
  }

  private enter(record: number) {
    this.path.push(record);
    this.onPath[this.identityOf[record]]++;
  }

  private leave(record: number) {
    this.path.pop();
    this.onPath[this.identityOf[record]]--;
  }

  /**
   * What the count below a node depends on: its record and match state, and, for a record inside a cycle, which of
   * its own cycle's records are among the ancestors (they cannot be expanded again).
   */
  private memoKey(record: number, mask: number): number | string {
    const identity = this.identityOf[record];
    if (!this.cyclic[identity]) {
      return record * this.maskSpace + mask;
    }
    const scc = this.sccOf[identity];
    const ancestors = this.path.map(r => this.identityOf[r])
      .filter(id => id !== identity && this.sccOf[id] === scc).sort((a, b) => a - b);
    return record + ':' + mask + ':' + ancestors.join(',');
  }
}
