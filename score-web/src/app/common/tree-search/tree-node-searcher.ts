import {AsyncSubject, EMPTY, Observable, of, Subject} from 'rxjs';
import {ExpressionEvaluator, FlatNode} from '../flat-tree';
import {GraphTreeSearch, TreeGraphAdapter} from './graph-tree-search';

/** What the searcher needs from the tree data source: the visible (expanded) node list and its expansion state. */
export interface TreeSearchDataSource<N> {
  data: N[];

  isExpanded(node: N): boolean;

  expand(node: N): void;

  /** Expands the node and its ancestors with a single change announcement, when the tree can (it is much cheaper). */
  expandTo?(node: N): void;
}

export interface TreeSearchLimits {
  /** Stop once this many new matches are found in one pass. */
  threshold: number;
  /** Stop once this many nodes are expanded in one pass. */
  expandLimit: number;
}

export interface TreeSearchVisit<N> {
  /** The nodes to enqueue after the visited one. */
  children: N[];
  /** Whether the visit expanded the node and so counts against `TreeSearchLimits.expandLimit`. */
  expanded: boolean;
}

/** How long a search pass works before it hands the thread back to the browser (so the page stays responsive). */
const TIME_SLICE_MS = 12;
/** How often a running search publishes its progress (see `TreeNodeSearcher.progress`). */
const PROGRESS_INTERVAL_MS = 1000;

/**
 * Runs `task` in a later macrotask. Unlike `setTimeout` it is neither clamped to 4ms once nested nor throttled to
 * about once a second in a background tab, so a long search keeps going when the user switches tabs.
 */
function runLater(task: () => void) {
  const channel = new MessageChannel();
  channel.port1.onmessage = () => {
    channel.port1.close();
    task();
  };
  channel.port2.postMessage(undefined);
}

declare const Zone: {current: ZoneLike; root: ZoneLike} | undefined;

interface ZoneLike {
  run<T>(callback: () => T): T;
}

/**
 * Like `runLater`, but outside the Angular zone: the walk only touches the searcher's own state, and a change
 * detection pass after every slice would cost more than the slice itself.
 */
function runLaterOutsideAngular(task: () => void) {
  if (typeof Zone === 'undefined') {
    runLater(task);
  } else {
    Zone.root.run(() => runLater(task));
  }
}

/** A function that runs a task in the Angular zone this was called from, so a result that arrives later still renders. */
function zoneRunner(): (task: () => void) => void {
  const zone = typeof Zone === 'undefined' ? undefined : Zone.current;
  return task => zone ? zone.run(task) : task();
}

/**
 * The incremental breadth-first search shared by every node tree (CC, BIE, model browser).
 *
 * A pass walks the tree from the selected node (or the root, for a path keyword) until it finds `threshold`
 * matches or expands `expandLimit` nodes; the unvisited rest stays queued so the next pass ("search more") resumes there.
 * The walk runs in short time slices rather than in one block, so a deep tree does not freeze the page.
 *
 * Subclasses supply the tree-specific parts: how a node expands, what may match, how nodes are compared and located.
 *
 * A tree that can describe itself as a DAG (`graphAdapter`) is searched through that instead: the count of matches
 * is then exact and immediate, nothing but the way to a match is built, and the work happens in a Web Worker.
 */
export abstract class TreeNodeSearcher<N extends FlatNode, D extends TreeSearchDataSource<N>> {

  searchKeyword = '';
  _inputKeyword = '';
  /** The matches found by the node walk (a graph search does not list them: see `resultCount`). */
  searchResult: N[] = [];
  fullSearched = false;
  searchedItemCount = 0;
  /** What `searchedItemCount` counts. */
  searchedUnit = 'nodes';
  searchIndex = 0;
  isSearching = false;
  /** Matches found by the pass in progress, not yet part of `searchResult`. */
  pendingMatchCount = 0;
  /**
   * Emits (inside the Angular zone) whenever the counters above change on their own: about once a second while a
   * search runs, and when it ends or is reset. A view showing them listens to this to refresh.
   */
  readonly progress = new Subject<void>();

  private graph: GraphTreeSearch<N> | undefined;
  private usingGraph = false;
  private graphResultCount = 0;
  private graphCurrentNode: N | undefined;

  /** The unvisited nodes in breadth-first order, from `queueHead` on. */
  private queue: N[] = [];
  private queueHead = 0;
  /** Bumped by every reset, so a pass still running for a stale keyword drops its result. */
  private generation = 0;

  protected constructor(protected dataSource: D) {
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

  // ---- tree-specific hooks -------------------------------------------------------------------------------------

  protected abstract getEvaluator(expr: string): ExpressionEvaluator<N>;

  /** The position of the node in the visible list. */
  protected abstract visibleIndex(node: N): number;

  /** Loads the node's children if needed and returns the ones to search next. */
  protected abstract visit(node: N, keyword: string): TreeSearchVisit<N>;

  /** Whether the node itself can be a match (structural nodes such as groups cannot). */
  protected isMatchable(node: N): boolean {
    return true;
  }

  protected limits(keyword: string): TreeSearchLimits {
    return {threshold: 100, expandLimit: 10000};
  }

  /** Whether the search starts at the root instead of the selected node. */
  protected startsAtRoot(keyword: string, selectedNode: N | undefined): boolean {
    return keyword.charAt(0) === '/' || !selectedNode;
  }

  protected sameNode(a: N, b: N): boolean {
    return a === b;
  }

  /** How the tree describes itself for a graph search, or undefined when it can only be searched by walking it. */
  protected graphAdapter(): TreeGraphAdapter<N> | undefined {
    return undefined;
  }

  /** The number of matches of the latest search (a lower bound while `fullSearched` is false). */
  get resultCount(): number {
    return this.usingGraph ? this.graphResultCount : this.searchResult.length;
  }

  // ---- navigation ----------------------------------------------------------------------------------------------

  prev(node: N): N {
    const index = this.getNodeIndex(node);
    return index > 0 ? this.dataSource.data[index - 1] : node;
  }

  next(node: N): N {
    const index = this.getNodeIndex(node);
    return index < this.dataSource.data.length - 1 ? this.dataSource.data[index + 1] : node;
  }

  go(val: number): Observable<number> {
    this.searchIndex = this.wrapIndex(this.searchIndex + val);
    return this.currentMatch();
  }

  /** The node shown in a row. */
  nodeAt(index: number): N {
    return this.dataSource.data[index];
  }

  getNodeIndex(node: N): number {
    if (!this.dataSource.isExpanded(node)) {
      this.expandTo(node);
    }
    return this.visibleIndex(node);
  }

  private expandTo(node: N) {
    if (this.dataSource.expandTo) {
      this.dataSource.expandTo(node);
    } else {
      this.dataSource.expand(node);
    }
  }

  isSearchResult(node: N): boolean {
    if (this.usingGraph) {
      return !!node && this.graph.isMatch(node);
    }
    return !!node && this.searchResult.some(e => this.sameNode(e, node));
  }

  isCurrentSearchIndex(node: N): boolean {
    const current = this.usingGraph ? this.graphCurrentNode : this.searchResult[this.searchIndex];
    return !!node && !!current && this.sameNode(current, node);
  }

  // ---- search --------------------------------------------------------------------------------------------------

  /**
   * Searches (or, once everything is searched, steps to the next/previous match) and emits the visible index of the
   * current match, or -1 when there is none. A search already in progress is not restarted.
   */
  search(inputKeyword: string, selectedNode: N, backward?: boolean, force?: boolean): Observable<number> {
    if (this.isSearching) {
      return EMPTY;
    }
    if (!inputKeyword || inputKeyword.length === 0) {
      this.resetSearch();
      return EMPTY;
    }

    if (this.fullSearched && !force) {
      this.searchIndex = this.wrapIndex(this.searchIndex + (backward ? -1 : 1));
      return this.currentMatch();
    }

    this._inputKeyword = inputKeyword;
    const done = new AsyncSubject<number>();
    this.isSearching = true;
    const graph = this.prepareGraphSearch(inputKeyword, selectedNode);
    if (graph) {
      this.runGraphSearch(graph, inputKeyword, this.generation, done);
    } else {
      this.runPass(inputKeyword, selectedNode, this.generation, done);
    }
    return done.asObservable();
  }

  /** Releases what the searcher holds outside the page (its graph search session). */
  dispose() {
    this.resetSearch();
    this.graph?.close();
    this.graph = undefined;
  }

  resetSearch() {
    this.generation++;
    this.searchKeyword = undefined;
    this.searchResult = [];
    this.queue = [];
    this.queueHead = 0;
    this.fullSearched = false;
    this.searchedItemCount = 0;
    this.searchIndex = 0;
    this.isSearching = false;
    this.pendingMatchCount = 0;
    this.usingGraph = false;
    this.graphResultCount = 0;
    this.graphCurrentNode = undefined;
    this.searchedUnit = 'nodes';
    this.graph?.clear();
    this.progress.next();
  }

  private prepareGraphSearch(keyword: string, selectedNode: N): GraphTreeSearch<N> | undefined {
    const adapter = this.graphAdapter();
    if (!adapter) {
      return undefined;
    }
    this.graph ??= new GraphTreeSearch(adapter);
    const scopeNode = this.startsAtRoot(keyword, selectedNode) ? this.dataSource.data[0] : selectedNode;
    return this.graph.prepare(keyword, false, scopeNode) ? this.graph : undefined;
  }

  private async runGraphSearch(graph: GraphTreeSearch<N>, keyword: string, generation: number, done: AsyncSubject<number>) {
    const inZone = zoneRunner();
    this.usingGraph = true;
    this.searchedUnit = 'components';
    this.searchedItemCount = 0;
    this.progress.next();
    try {
      const result = await graph.run(visited => inZone(() => {
        if (generation === this.generation) {
          this.searchedItemCount = visited;
          this.progress.next();
        }
      }));
      const node = generation === this.generation && result.total > 0 ? await graph.nodeAt(0) : undefined;
      inZone(() => {
        if (generation !== this.generation) {
          done.complete();
          return;
        }
        this.graphResultCount = result.total;
        this.graphCurrentNode = node;
        this.fullSearched = result.exact;
        this.searchedItemCount = result.nodes;
        this.searchedUnit = 'nodes';
        this.searchKeyword = graph.highlightKeyword;
        this.searchIndex = 0;
        this.isSearching = false;
        this.progress.next();
        done.next(node ? this.getNodeIndex(node) : -1);
        done.complete();
      });
    } catch (error) {
      inZone(() => {
        if (generation === this.generation) {
          this.resetSearch();
        }
        done.error(error);
      });
    }
  }

  /** Emits the visible index of the match the search is on, or -1 when there is none. */
  private currentMatch(): Observable<number> {
    if (!this.usingGraph) {
      return of(this.currentIndex());
    }
    if (this.graphResultCount === 0) {
      return of(-1);
    }
    const inZone = zoneRunner();
    const generation = this.generation;
    const done = new AsyncSubject<number>();
    this.graph.nodeAt(this.searchIndex).then(node => inZone(() => {
      if (generation === this.generation) {
        this.graphCurrentNode = node;
        done.next(node ? this.getNodeIndex(node) : -1);
      }
      done.complete();
    }), error => inZone(() => done.error(error)));
    return done.asObservable();
  }

  private runPass(keyword: string, selectedNode: N, generation: number, done: AsyncSubject<number>) {
    const zone = typeof Zone === 'undefined' ? undefined : Zone.current;
    const evaluator = this.getEvaluator(keyword);
    const {threshold, expandLimit} = this.limits(keyword);
    const matches: N[] = [];
    let remainingExpands = expandLimit;

    if (this.queueHead >= this.queue.length) {
      this.queue = [this.startsAtRoot(keyword, selectedNode) ? this.dataSource.data[0] : selectedNode];
      this.queueHead = 0;
    }

    const inZone = (task: () => void) => zone ? zone.run(task) : task();
    let nextProgressAt = performance.now() + PROGRESS_INTERVAL_MS;

    const step = () => {
      if (generation !== this.generation) {
        inZone(() => done.complete());
        return;
      }
      const deadline = performance.now() + TIME_SLICE_MS;
      while (matches.length < threshold && remainingExpands > 0 && this.queueHead < this.queue.length) {
        const item = this.queue[this.queueHead++];
        this.searchedItemCount++;
        if (this.isMatchable(item) && evaluator.eval(item)) {
          matches.push(item);
        }
        const {children, expanded} = this.visit(item, keyword);
        if (expanded) {
          remainingExpands--;
        }
        for (const child of children) {
          this.queue.push(child);
        }
        if (performance.now() > deadline) {
          this.pendingMatchCount = matches.length;
          if (deadline > nextProgressAt) {
            nextProgressAt = deadline + PROGRESS_INTERVAL_MS;
            inZone(() => this.progress.next());
          }
          runLaterOutsideAngular(step);
          return;
        }
      }
      this.pendingMatchCount = matches.length;
      inZone(() => this.finishPass(evaluator, matches, done));
    };
    runLaterOutsideAngular(step);
  }

  private finishPass(evaluator: ExpressionEvaluator<N>, matches: N[], done: AsyncSubject<number>) {
    if (this.queueHead >= this.queue.length) {
      this.fullSearched = true;
      this.queue = [];
      this.queueHead = 0;
    } else if (this.queueHead > 0) {
      this.queue = this.queue.slice(this.queueHead);
      this.queueHead = 0;
    }

    this.searchKeyword = evaluator.keywordForHighlight();
    this.pendingMatchCount = 0;
    if (matches.length > 0) {
      // The tree only opens up once the pass is over, in one step together with the final count.
      matches.forEach(e => this.expandTo(e));
      this.searchResult = this.sortByVisibleOrder(this.searchResult.concat(matches));
    }
    this.isSearching = false;
    this.progress.next();

    done.next(this.currentIndex());
    done.complete();
  }

  private sortByVisibleOrder(nodes: N[]): N[] {
    return nodes
      .map(node => ({node, index: this.visibleIndex(node)}))
      .sort((a, b) => a.index - b.index)
      .map(e => e.node);
  }

  private wrapIndex(index: number): number {
    if (this.resultCount <= index) {
      return 0;
    }
    return index < 0 ? this.resultCount - 1 : index;
  }

  private currentIndex(): number {
    return this.searchResult.length > 0 ? this.getNodeIndex(this.searchResult[this.searchIndex]) : -1;
  }
}
