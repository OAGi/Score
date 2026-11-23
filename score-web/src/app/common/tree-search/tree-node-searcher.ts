import {SearchAttribute} from './search-suggest';
import {AsyncSubject, EMPTY, Observable, of, Subject} from 'rxjs';
import {FlatNode} from '../flat-tree';
import {GraphRefusal, GraphTreeSearch, TreeGraphAdapter} from './graph-tree-search';
import {parseSearchExpression} from './search-expression';
import {ParseError} from './search-grammar';
import {ServerPredicate, splitServerPredicates} from './server-predicate';
import {findRowIndex, isSameNode} from './tree-node-identity';

/** What the searcher needs from the tree data source: the visible (expanded) node list and its expansion state. */
export interface TreeSearchDataSource<N> {
  data: N[];

  isExpanded(node: N): boolean;

  expand(node: N): void;

  /** Expands the node and its ancestors with a single change announcement, when the tree can (it is much cheaper). */
  expandTo?(node: N): void;
}

const NO_ATTRIBUTES: ReadonlySet<string> = new Set();

declare const Zone: {current: ZoneLike; root: ZoneLike} | undefined;

interface ZoneLike {
  run<T>(callback: () => T): T;
}

/** A function that runs a task in the Angular zone this was called from, so a result that arrives later still renders. */
function zoneRunner(): (task: () => void) => void {
  const zone = typeof Zone === 'undefined' ? undefined : Zone.current;
  return task => zone ? zone.run(task) : task();
}

/**
 * The search shared by every node tree (CC, BIE, model browser).
 *
 * A tree describes itself as a DAG (`graphAdapter`) and is searched through that: the count of matches is exact and
 * immediate, nothing but the way to a match is built, and the work happens in a Web Worker. An expression the graph
 * cannot take (it has more steps than the matcher has state for) is refused with an error: no other way of searching
 * gives the same answer.
 *
 * Subclasses supply the tree-specific parts: the graph, how nodes are compared and located, and the conditions the
 * backend answers.
 */
export abstract class TreeNodeSearcher<N extends FlatNode, D extends TreeSearchDataSource<N>> {

  searchKeyword = '';
  /** Whether a result is marked as a whole, because the expression names nothing to mark in the names. */
  marksWholeNode = false;
  _inputKeyword = '';
  /** False while the number of matches may be too small, because the search stopped on its budget. */
  fullSearched = false;
  searchedItemCount = 0;
  /** What `searchedItemCount` counts. */
  searchedUnit = 'nodes';
  searchIndex = 0;
  isSearching = false;
  /** Why the keyword cannot be searched, when it is a malformed expression or one the search cannot take. */
  expressionError: string | undefined;
  /** The node attributes a search predicate may ask about (what the search box suggests); none for a name-only search. */
  readonly searchAttributes: SearchAttribute[] = [];
  /**
   * Emits (inside the Angular zone) whenever the counters above change on their own: about once a second while a
   * search runs, and when it ends or is reset. A view showing them listens to this to refresh.
   */
  readonly progress = new Subject<void>();
  /** Why the graph refused the latest search, undefined when it did not. */
  graphRefusal: GraphRefusal | undefined;

  private graph: GraphTreeSearch<N> | undefined;
  private graphResultCount = 0;
  private graphCurrentNode: N | undefined;

  /** Bumped by every reset, so a search still running for a stale keyword drops its result. */
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

  /** The position of the node in the visible list. */
  protected visibleIndex(node: N): number {
    return findRowIndex(this.dataSource.data, node);
  }

  /** How the tree describes itself for the search. */
  protected abstract graphAdapter(): TreeGraphAdapter<N>;

  /** Whether the search starts at the root instead of the selected node. */
  protected startsAtRoot(keyword: string, selectedNode: N | undefined): boolean {
    return keyword.charAt(0) === '/' || !selectedNode;
  }

  protected sameNode(a: N, b: N): boolean {
    return isSameNode(a, b);
  }

  /** The attributes the backend searches (lower case): `SearchAttribute.source` is `server`. None by default. */
  protected serverAttributes(): ReadonlySet<string> {
    return NO_ATTRIBUTES;
  }

  /**
   * Asks the backend which places satisfy the predicates and remembers the answer, so that the graph the tree describes
   * itself with (`graphAdapter().dag()` and `attributesOf`) has the flag of each predicate set on those places.
   */
  protected resolveServerPredicates(predicates: ServerPredicate[], caseSensitive: boolean): Promise<void> {
    return Promise.resolve();
  }

  /** Forgets what `resolveServerPredicates` remembered. */
  protected clearServerPredicates() {
  }

  /** The number of matches of the latest search (a lower bound while `fullSearched` is false). */
  get resultCount(): number {
    return this.graphResultCount;
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
    if (this.resultsStale) {
      return this.searchAgain();
    }
    this.searchIndex = this.wrapIndex(this.searchIndex + val);
    return this.currentMatch();
  }

  /**
   * Whether the finished search no longer describes the tree, because something a search asks about has changed since (a
   * definition being edited, a node being used, ...): stepping on would go through matches the tree no longer has.
   */
  private get resultsStale(): boolean {
    return this.fullSearched && !this.isSearching && !!this.graph?.isStale();
  }

  /** The finished search made again, from where it started, over the tree as it is now. */
  private searchAgain(): Observable<number> {
    return this.search(this._inputKeyword, this.graph.startedFrom, false, true);
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

  /** What to mark in a node's text: the keyword, or all of the text when the expression names nothing and the node is a result. */
  keywordFor(node: N, text: string): string {
    return this.marksWholeNode && this.isSearchResult(node) ? text : this.searchKeyword;
  }

  isSearchResult(node: N): boolean {
    return !!node && !!this.graph && this.graph.isMatch(node);
  }

  isCurrentSearchIndex(node: N): boolean {
    return !!node && !!this.graphCurrentNode && this.sameNode(this.graphCurrentNode, node);
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
      if (this.resultsStale) {
        return this.searchAgain();
      }
      this.searchIndex = this.wrapIndex(this.searchIndex + (backward ? -1 : 1));
      return this.currentMatch();
    }

    this._inputKeyword = inputKeyword;
    const error = parseSearchExpression(inputKeyword).error;
    if (error) {
      this.expressionError = error.message + ' (at ' + (error.position + 1) + ')';
      this.progress.next();
      return of(-1);
    }
    this.expressionError = undefined;
    let split;
    try {
      split = splitServerPredicates(inputKeyword, this.serverAttributes());
    } catch (e) {
      if (!(e instanceof ParseError)) {
        throw e;
      }
      this.expressionError = e.message + ' (at ' + (e.position + 1) + ')';
      this.progress.next();
      return of(-1);
    }
    const done = new AsyncSubject<number>();
    this.isSearching = true;
    if (split.predicates.length > 0) {
      this.runServerSearch(split.expression, split.predicates, inputKeyword, selectedNode, this.generation, done);
    } else {
      this.startSearch(inputKeyword, inputKeyword, selectedNode, done);
    }
    return done.asObservable();
  }

  /** Runs the search of the expression the graph can judge (`graphExpression`); `keyword` is what the user typed. */
  private startSearch(keyword: string, graphExpression: string, selectedNode: N, done: AsyncSubject<number>) {
    const graph = this.prepareGraphSearch(graphExpression, selectedNode);
    this.graphRefusal = graph ? undefined : this.graph.refusal;
    if (!graph) {
      this.isSearching = false;
      this.expressionError = this.graphRefusal === 'unsupported-expression' ?
        'This expression is too complex to search here' : 'The tree cannot be searched';
      if (this.graphRefusal !== 'unsupported-expression') {
        console.error('[tree-search] the tree could not be described for the search (' + this.graphRefusal + '): ' + keyword);
      }
      this.progress.next();
      done.next(-1);
      done.complete();
      return;
    }
    this.runGraphSearch(graph, keyword, this.generation, done);
  }

  /** Asks the backend for the server predicates, and then runs the graph search over the expression without them. */
  private async runServerSearch(graphExpression: string, predicates: ServerPredicate[], keyword: string, selectedNode: N,
                                generation: number, done: AsyncSubject<number>) {
    const inZone = zoneRunner();
    this.searchedUnit = 'components';
    this.progress.next();
    try {
      await this.resolveServerPredicates(predicates, false);
    } catch (error) {
      inZone(() => {
        if (generation === this.generation) {
          this.resetSearch();
          this.expressionError = 'The server could not search this expression';
          this.progress.next();
        }
        done.next(-1);
        done.complete();
      });
      return;
    }
    inZone(() => {
      if (generation !== this.generation) {
        done.complete();
        return;
      }
      this.startSearch(keyword, graphExpression, selectedNode, done);
    });
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
    this.marksWholeNode = false;
    this.fullSearched = false;
    this.searchedItemCount = 0;
    this.searchIndex = 0;
    this.isSearching = false;
    this.expressionError = undefined;
    this.graphRefusal = undefined;
    this.graphResultCount = 0;
    this.graphCurrentNode = undefined;
    this.searchedUnit = 'nodes';
    this.graph?.clear();
    this.clearServerPredicates();
    this.progress.next();
  }

  /**
   * Gets the graph search ready, or undefined when the graph refuses it (`graph.refusal` says why). A node the search
   * starts from that the tree no longer shows (the filters hid it since it was selected) is no place to search from: the
   * search starts at the root then, as when no node is selected.
   */
  private prepareGraphSearch(keyword: string, selectedNode: N): GraphTreeSearch<N> | undefined {
    this.graph ??= new GraphTreeSearch(this.graphAdapter());
    const root = this.dataSource.data[0];
    const from = this.startsAtRoot(keyword, selectedNode) ? root : selectedNode;
    if (this.graph.prepare(keyword, false, from)) {
      return this.graph;
    }
    if (this.graph.refusal === 'scope-not-in-graph' && from !== root && this.graph.prepare(keyword, false, root)) {
      return this.graph;
    }
    return undefined;
  }

  private async runGraphSearch(graph: GraphTreeSearch<N>, keyword: string, generation: number, done: AsyncSubject<number>) {
    const inZone = zoneRunner();
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
        // an expression that names nothing to highlight still marks the search as made
        this.searchKeyword = graph.highlightKeyword || keyword;
        this.marksWholeNode = graph.marksWholeNode;
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

  private wrapIndex(index: number): number {
    if (this.resultCount <= index) {
      return 0;
    }
    return index < 0 ? this.resultCount - 1 : index;
  }
}
