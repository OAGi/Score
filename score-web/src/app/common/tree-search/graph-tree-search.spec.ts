import {FlatNode} from '../flat-tree';
import {TreeGraphAdapter} from './graph-tree-search';
import {SearchDag, SearchDagBuilder} from './search-dag';
import {TreeNodeSearcher, TreeSearchDataSource} from './tree-node-searcher';

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
  /** What the tree says when it cannot describe itself. */
  withoutDag = false;
  asked: string[][] = [];
  serverError: Error | undefined;

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
      dag: () => this.withoutDag ? undefined : dag,
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

  protected override serverAttributes(): ReadonlySet<string> {
    return new Set(['remark']);
  }

  protected override async resolveServerPredicates(predicates: {value: string}[]): Promise<void> {
    this.asked.push(predicates.map(p => p.value));
    if (this.serverError) {
      throw this.serverError;
    }
  }
}

function drain(searcher: GraphSearcher, keyword: string, force = false, from?: Node): Promise<number> {
  return new Promise((resolve, reject) => searcher.search(keyword, from, false, force)
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

    expect([second, third, wrapped].map(i => (dataSource.data[i].parent as Node).key)).toEqual(['a', 'b', 'a']);
    expect(searcher.searchIndex).toBe(0);
  });

  it('follows a path of steps', async () => {
    const {searcher, dataSource} = setup(shape, names);
    const index = await drain(searcher, 'Beta/Alpha/Release');

    expect(searcher.resultCount).toBe(1);
    expect(dataSource.data[index].key).toBe('x');
    expect((dataSource.data[index].parent.parent as Node).key).toBe('b');
  });

  it('marks the matches that are on screen, and only those', async () => {
    const {searcher, dataSource} = setup(shape, names);
    await drain(searcher, 'Alpha/Identifier');

    const y = dataSource.data.find(n => n.key === 'y' && (n.parent as Node).key === 'a');
    const otherY = dataSource.data.find(n => n.key === 'y' && (n.parent as Node).key === 'b');
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

  it('matches the names of a path by what they contain, from the root as anywhere else', async () => {
    const {searcher} = setup(shape, names);

    // the root and the step after it are contained in the names, and the path begins at the root
    expect(await drain(searcher, '/roo/Alp/Identifier')).toBeGreaterThanOrEqual(0);
    expect(searcher.resultCount).toBe(1);

    expect(await drain(searcher, '/Alp/Identifier', true)).toBe(-1);
    expect(searcher.resultCount).toBe(0);

    // without the leading slash the path may begin at any node
    expect(await drain(searcher, 'Alp/Identifier', true)).toBeGreaterThanOrEqual(0);
    expect(searcher.resultCount).toBe(2);
  });

  it('says why a search was refused: too many steps for the matcher', async () => {
    const {searcher} = setup(shape, names);

    await drain(searcher, 'Identifier');
    expect(searcher.graphRefusal).toBeUndefined();

    // more steps that reach any depth than the matcher state has bits for
    expect(await drain(searcher, '//a'.repeat(17), true)).toBe(-1);
    expect(searcher.graphRefusal).toBe('unsupported-expression');
    expect(searcher.expressionError).toBe('This expression is too complex to search here');
    expect(searcher.isSearching).toBe(false);

    searcher.resetSearch();
    expect(searcher.graphRefusal).toBeUndefined();
  });

  it('says so when the tree cannot describe itself, and leaves the search ready for the next one', async () => {
    const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const {searcher} = setup(shape, names);
    searcher.withoutDag = true;

    expect(await drain(searcher, 'Identifier')).toBe(-1);
    expect(searcher.graphRefusal).toBe('no-dag');
    expect(searcher.expressionError).toBe('The tree cannot be searched');
    expect(searcher.isSearching).toBe(false);
    expect(error).toHaveBeenCalledTimes(1);

    searcher.withoutDag = false;
    expect(await drain(searcher, 'Identifier', true)).toBeGreaterThanOrEqual(0);
    expect(searcher.expressionError).toBeUndefined();
    error.mockRestore();
  });

  it('searches from the root when the node it was asked to start from is no longer in the tree', async () => {
    const {searcher, forest} = setup(shape, names);
    const hidden = forest.make('hidden');

    expect(await drain(searcher, 'Identifier', false, hidden)).toBeGreaterThanOrEqual(0);
    expect(searcher.graphRefusal).toBeUndefined();
    expect(searcher.resultCount).toBe(3);
  });

  it('does not ask the server about an expression that has no server attribute', async () => {
    const {searcher} = setup(shape, names);
    await drain(searcher, 'Identifier');

    expect(searcher.asked).toEqual([]);
  });

  it('asks the server once for the conditions on server attributes, and searches on with the flags it set', async () => {
    const {searcher} = setup(shape, names);

    await drain(searcher, '//*[remark = "x" or contains(remark, "y")]');

    expect(searcher.asked).toEqual([['x', 'y']]);
    expect(searcher.expressionError).toBeUndefined();
    expect(searcher.isSearching).toBe(false);
  });

  it('reports a server that fails and leaves the search ready for the next one', async () => {
    const {searcher} = setup(shape, names);
    searcher.serverError = new Error('down');

    expect(await drain(searcher, '//*[remark = "x"]')).toBe(-1);
    expect(searcher.expressionError).toBe('The server could not search this expression');
    expect(searcher.isSearching).toBe(false);

    searcher.serverError = undefined;
    expect(await drain(searcher, 'Identifier')).toBeGreaterThanOrEqual(0);
  });

  it('refuses a server attribute that is ordered or has no value to compare with', async () => {
    const {searcher} = setup(shape, names);

    expect(await drain(searcher, '//*[remark < "x"]')).toBe(-1);
    expect(searcher.expressionError).toContain('= and != only');
    expect(searcher.asked).toEqual([]);
  });
});

/** A tree whose components have a definition that can be edited, as the BIE tree's nodes have. */
class EditableSearcher extends TreeNodeSearcher<Node, TestDataSource> {
  readonly definitions: {[key: string]: string} = {};
  dagBuilt = 0;

  constructor(dataSource: TestDataSource, private readonly forest: Forest, private readonly shape: {[key: string]: string[]}) {
    super(dataSource);
  }

  protected override graphAdapter(): TreeGraphAdapter<Node> {
    let builder: SearchDagBuilder;
    const attributes = (key: string) => ({definition: this.definitions[key] ?? ''});
    return {
      dag: () => {
        this.dagBuilt++;
        builder = new SearchDagBuilder();
        const keys = [...new Set([...Object.keys(this.shape), ...Object.values(this.shape).flat()])];
        keys.forEach(key => builder.record(key, [this.forest.make(key).name], key, attributes(key)));
        Object.entries(this.shape).forEach(([key, children]) => builder.setChildren(builder.indexOf(key), children.map(c => builder.indexOf(c))));
        return builder.build();
      },
      recordOf: node => builder?.indexOf(node.key) ?? -1,
      namesOf: node => [node.name],
      attributesOf: node => attributes(node.key),
      childrenOf: node => this.forest.childrenOf(node)
    };
  }

  protected visibleIndex(node: Node): number {
    return this.dataSource.data.indexOf(node);
  }
}

describe('TreeNodeSearcher on a graph whose values are edited', () => {
  const shape = {root: ['a', 'b'], a: ['x'], b: ['y'], x: [], y: []};
  const expression = "*[definition != '']";

  function editable() {
    const forest = new Forest(shape);
    const root = forest.make('root');
    const searcher = new EditableSearcher(new TestDataSource(forest, root), forest, shape);
    searcher.definitions['a'] = 'The first';
    searcher.definitions['y'] = 'The last';
    return {searcher, root};
  }

  const search = (searcher: EditableSearcher, force = false) => new Promise<number>((resolve, reject) =>
    searcher.search(expression, undefined, false, force).subscribe({next: resolve, error: reject, complete: () => resolve(undefined)}));

  it('steps through the matches while nothing a search asks about has changed', async () => {
    const {searcher} = editable();
    await search(searcher);
    expect(searcher.resultCount).toBe(2);

    await search(searcher);

    expect(searcher.resultCount).toBe(2);
    expect(searcher.searchIndex).toBe(1);
  });

  it('searches again, from where it started, when an edit has changed what a search finds', async () => {
    const {searcher} = editable();
    await search(searcher);
    searcher.definitions['a'] = '';

    const index = await search(searcher);

    expect(searcher.resultCount).toBe(1);
    expect(searcher.searchIndex).toBe(0);
    expect(index).toBeGreaterThanOrEqual(0);
    searcher.definitions['x'] = 'Added';
    await search(searcher);
    expect(searcher.resultCount).toBe(2);
  });

  it('searches again when the arrows are used after an edit', async () => {
    const {searcher} = editable();
    await search(searcher);
    searcher.definitions['y'] = '';

    await new Promise<void>(resolve => searcher.go(1).subscribe({complete: resolve}));

    expect(searcher.resultCount).toBe(1);
    expect(searcher.searchIndex).toBe(0);
  });

  it('does not describe the tree again while a search is running or before there is one', async () => {
    const {searcher} = editable();
    const before = searcher.dagBuilt;
    searcher.go(1);
    expect(searcher.dagBuilt).toBe(before);
  });
});
