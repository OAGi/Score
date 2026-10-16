import {FlatNode} from '../flat-tree';
import {TreeGraphAdapter} from './graph-tree-search';
import {SearchDag, SearchDagBuilder} from './search-dag';
import {TreeNodeSearcher, TreeSearchDataSource, TreeSearchVisit} from './tree-node-searcher';

interface Node extends FlatNode {
  key: string;
  group?: boolean;
}

/** A tree of components; `shape` says which components are the children of which, in order. */
class Forest {
  readonly nodes: Node[] = [];
  readonly expanded = new Set<Node>();

  constructor(private readonly shape: {[key: string]: string[]}, private readonly names: {[key: string]: string} = {}) {
  }

  make(key: string, parent?: Node): Node {
    const node: Node = {
      key, name: this.names[key] ?? key, type: 'n', level: parent ? parent.level + 1 : 0, expanded: false,
      expandable: (this.shape[key] ?? []).length > 0, children: [], parent, getChildren: () => node.children
    };
    return node;
  }

  childrenOf(node: Node): Node[] {
    if (node.children.length === 0) {
      node.children = (this.shape[node.key] ?? []).map(key => this.make(key, node));
    }
    return node.children as Node[];
  }
}

class TestDataSource implements TreeSearchDataSource<Node> {
  data: Node[];

  constructor(private readonly forest: Forest, root: Node) {
    this.data = [root];
  }

  isExpanded(node: Node): boolean {
    return this.forest.expanded.has(node);
  }

  expand(node: Node) {
    if (node.parent && !this.isExpanded(node.parent as Node)) {
      this.expand(node.parent as Node);
    }
    if (this.isExpanded(node)) {
      return;
    }
    this.forest.expanded.add(node);
    const index = this.data.indexOf(node);
    this.data.splice(index + 1, 0, ...this.forest.childrenOf(node));
  }
}

class GraphSearcher extends TreeNodeSearcher<Node, TestDataSource> {
  /** Replaces what the tree reports as its structure, as when the sibling order changes after a search. */
  reorder: ((keys: string[]) => string[]) | undefined;

  constructor(dataSource: TestDataSource, private readonly forest: Forest, private readonly shape: {[key: string]: string[]}) {
    super(dataSource);
  }

  protected override graphAdapter(): TreeGraphAdapter<Node> {
    const builder = new SearchDagBuilder();
    Object.keys(this.shape).forEach(key => builder.record(key, [this.forest.make(key).name]));
    Object.values(this.shape).flat().forEach(key => builder.record(key, [this.forest.make(key).name]));
    Object.entries(this.shape).forEach(([key, children]) => builder.setChildren(builder.indexOf(key), children.map(c => builder.indexOf(c))));
    const dag = builder.build();
    return {
      dag: () => dag,
      recordOf: node => builder.indexOf(node.key) ?? -1,
      namesOf: node => node.group ? undefined : [node.name],
      childrenOf: node => {
        const children = this.forest.childrenOf(node);
        return this.reorder ? [...children].sort((a, b) => this.reorder(['a']).length - 1 && a.key.localeCompare(b.key) * -1) : children;
      }
    };
  }

  protected visibleIndex(node: Node): number {
    return this.dataSource.data.indexOf(node);
  }

  protected visit(): TreeSearchVisit<Node> {
    throw new Error('the node walk must not be used for a tree that has a graph');
  }
}

function drain(searcher: GraphSearcher, keyword: string, force = false): Promise<number> {
  return new Promise((resolve, reject) => searcher.search(keyword, undefined, false, force)
    .subscribe({next: resolve, error: reject, complete: () => resolve(undefined)}));
}

function setup(shape: {[key: string]: string[]}, names: {[key: string]: string} = {}) {
  const forest = new Forest(shape, names);
  const root = forest.make('root');
  const dataSource = new TestDataSource(forest, root);
  return {searcher: new GraphSearcher(dataSource, forest, shape), dataSource, forest, root};
}

describe('TreeNodeSearcher on a graph', () => {
  const shape = {root: ['a', 'b'], a: ['x', 'y'], b: ['a', 'y'], x: [], y: []};
  const names = {a: 'Alpha', b: 'Beta', x: 'Release', y: 'Identifier'};

  it('counts the matches without building the tree and opens the way to the first one', async () => {
    const {searcher, dataSource} = setup(shape, names);
    const index = await drain(searcher, 'Identifier');

    expect(searcher.resultCount).toBe(3);
    expect(searcher.fullSearched).toBe(true);
    // root, a (x, y), b (a (x, y), y): what the search covered although only five nodes were built
    expect(searcher.searchedUnit).toBe('nodes');
    expect(searcher.searchedItemCount).toBe(9);
    expect(dataSource.data.map(n => n.key)).toEqual(['root', 'a', 'x', 'y', 'b']);
    expect(dataSource.data[index].key).toBe('y');
  });

  it('steps through the matches in the order the tree shows them', async () => {
    const {searcher, dataSource} = setup(shape, names);
    await drain(searcher, 'Identifier');

    const second = await drain(searcher, 'Identifier');
    const third = await drain(searcher, 'Identifier');
    const wrapped = await drain(searcher, 'Identifier');

    expect([second, third, wrapped].map(i => dataSource.data[i].parent.key)).toEqual(['a', 'b', 'a']);
    expect(searcher.searchIndex).toBe(0);
  });

  it('follows a path of steps', async () => {
    const {searcher, dataSource} = setup(shape, names);
    const index = await drain(searcher, 'Beta/Alpha/Release');

    expect(searcher.resultCount).toBe(1);
    expect(dataSource.data[index].key).toBe('x');
    expect(dataSource.data[index].parent.parent.key).toBe('b');
  });

  it('marks the matches that are on screen, and only those', async () => {
    const {searcher, dataSource} = setup(shape, names);
    await drain(searcher, 'Alpha/Identifier');

    const y = dataSource.data.find(n => n.key === 'y' && n.parent.key === 'a');
    const otherY = dataSource.data.find(n => n.key === 'y' && n.parent.key === 'b') ?? dataSource.forestChild?.(y);
    expect(searcher.isSearchResult(y)).toBe(true);
    expect(searcher.isCurrentSearchIndex(y)).toBe(true);
    expect(otherY === undefined || !searcher.isSearchResult(otherY)).toBe(true);
    expect(searcher.isSearchResult(dataSource.data[0])).toBe(false);
  });

  it('reports no match without touching the tree', async () => {
    const {searcher, dataSource} = setup(shape, names);
    const index = await drain(searcher, 'Nothing');

    expect(index).toBe(-1);
    expect(searcher.resultCount).toBe(0);
    expect(searcher.fullSearched).toBe(true);
    expect(dataSource.data.length).toBe(1);
  });

  it('searches below the selected node only', async () => {
    const {searcher, forest, root} = setup(shape, names);
    const b = forest.childrenOf(root)[1];
    const index = await new Promise<number>(resolve => searcher.search('Identifier', b).subscribe(resolve));

    expect(searcher.resultCount).toBe(2);
    expect(index).toBeGreaterThan(0);
  });

  it('forgets an answer that arrives after the search was reset', async () => {
    const {searcher} = setup(shape, names);
    const pending = drain(searcher, 'Identifier');
    searcher.resetSearch();

    expect(await pending).toBeUndefined();
    expect(searcher.resultCount).toBe(0);
  });
});
