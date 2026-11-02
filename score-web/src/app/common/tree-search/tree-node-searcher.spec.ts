import {ExpressionEvaluator, FlatNode} from '../flat-tree';
import {TreeNodeSearcher, TreeSearchDataSource, TreeSearchLimits, TreeSearchVisit} from './tree-node-searcher';

interface TestNode extends FlatNode {
  id: string;
}

function node(id: string, children: TestNode[] = []): TestNode {
  return {id, name: id, type: 'n', level: 0, expanded: false, expandable: children.length > 0, children, getChildren: () => children};
}

class TestSearcher extends TreeNodeSearcher<TestNode, TreeSearchDataSource<TestNode>> {
  visited: string[] = [];

  constructor(dataSource: TreeSearchDataSource<TestNode>, private limit: TreeSearchLimits) {
    super(dataSource);
  }

  protected override limits(): TreeSearchLimits {
    return this.limit;
  }

  protected visit(n: TestNode): TreeSearchVisit<TestNode> {
    this.visited.push(n.id);
    return {children: n.children as TestNode[], expanded: n.expandable};
  }

  protected visibleIndex(n: TestNode): number {
    return this.dataSource.data.indexOf(n);
  }

  protected getEvaluator(expr: string): ExpressionEvaluator<TestNode> {
    return {range: () => [], eval: n => n.id.startsWith(expr), keywordForHighlight: () => expr};
  }
}

function setup(limit: TreeSearchLimits) {
  // a
  // ├─ b ── d, hit1
  // └─ c ── hit2
  const hit1 = node('hit1');
  const hit2 = node('hit2');
  const root = node('a', [node('b', [node('d'), hit1]), node('c', [hit2])]);
  const all: TestNode[] = [];
  const walk = (n: TestNode) => {
    all.push(n);
    n.children.forEach(c => walk(c as TestNode));
  };
  walk(root);
  const dataSource: TreeSearchDataSource<TestNode> = {data: all, isExpanded: () => true, expand: () => undefined};
  return {searcher: new TestSearcher(dataSource, limit), root, hit1, hit2, all};
}

function run(searcher: TestSearcher, keyword: string, selected: TestNode, backward = false, force = false): Promise<number> {
  return new Promise(resolve => searcher.search(keyword, selected, backward, force).subscribe({
    next: resolve,
    complete: () => resolve(undefined)
  }));
}

describe('TreeNodeSearcher', () => {
  it('searches breadth-first and emits the index of the first match', async () => {
    const {searcher, root, all, hit1} = setup({threshold: 100, expandLimit: 100});
    const index = await run(searcher, 'hit', root);

    expect(searcher.visited).toEqual(['a', 'b', 'c', 'd', 'hit1', 'hit2']);
    expect(searcher.fullSearched).toBe(true);
    expect(searcher.searchResult.length).toBe(2);
    expect(index).toBe(all.indexOf(hit1));
  });

  it('resumes the queue on the next pass instead of starting over', async () => {
    const {searcher, root} = setup({threshold: 1, expandLimit: 100});
    await run(searcher, 'hit', root);
    expect(searcher.fullSearched).toBe(false);
    expect(searcher.searchResult.length).toBe(1);

    await run(searcher, 'hit', root, false, true);
    expect(searcher.fullSearched).toBe(true);
    expect(searcher.searchResult.length).toBe(2);
    expect(searcher.visited.filter(id => id === 'a').length).toBe(1);
  });

  it('stops at the expand limit and reports an incomplete search', async () => {
    const {searcher, root} = setup({threshold: 100, expandLimit: 1});
    await run(searcher, 'hit', root);

    expect(searcher.fullSearched).toBe(false);
    expect(searcher.searchResult.length).toBe(0);
  });

  it('steps through the matches once fully searched', async () => {
    const {searcher, root, all, hit1, hit2} = setup({threshold: 100, expandLimit: 100});
    await run(searcher, 'hit', root);

    expect(await run(searcher, 'hit', root)).toBe(all.indexOf(hit2));
    expect(await run(searcher, 'hit', root)).toBe(all.indexOf(hit1));
    expect(await run(searcher, 'hit', root, true)).toBe(all.indexOf(hit2));
  });

  it('drops the result of a pass that was reset meanwhile', async () => {
    const {searcher, root} = setup({threshold: 100, expandLimit: 100});
    const pending = run(searcher, 'hit', root);
    searcher.resetSearch();

    expect(await pending).toBeUndefined();
    expect(searcher.searchResult).toEqual([]);
    expect(searcher.isSearching).toBe(false);
  });
});
