import {DagPathSearch, DagSearchScope} from './dag-path-search';
import {nameContainsToken, PathPattern} from './path-pattern';
import {SearchDag, SearchDagBuilder} from './search-dag';

/**
 * A small DAG from `name: [children]` adjacency; record i is the i-th entry. A name may carry alternatives
 * (`Name|Other`) and a record may stand for the same component as an earlier one (`identities[i] = that record`).
 */
function dagOf(adjacency: [string, number[]][], identities: {[record: number]: number} = {}): SearchDag {
  const builder = new SearchDagBuilder();
  adjacency.forEach(([name], i) => builder.record('r' + i, name.split('|'), 'r' + (identities[i] ?? i)));
  adjacency.forEach(([, children], i) => builder.setChildren(i, children));
  return builder.build();
}

function childrenOf(dag: SearchDag, record: number): number[] {
  return Array.from(dag.childList.subarray(dag.childStart[record], dag.childStart[record + 1]));
}

/** The reference: unfold the tree (no component below itself), evaluate every node against its real ancestor chain. */
function unfoldedMatches(dag: SearchDag, expr: string, scopeRecords: number[]): number[][] {
  const pattern = new PathPattern(expr);
  const matches: number[][] = [];
  const visit = (path: number[], indexPath: number[]) => {
    const names = path.map(r => dag.names[r]);
    const chain = [...names].reverse();
    const matched = pattern.tokens.length <= chain.length &&
      [...pattern.tokens].reverse().every((token, i) => chain[i].some(name => nameContainsToken(name, token, false)));
    if (matched) {
      matches.push(indexPath);
    }
    const record = path[path.length - 1];
    childrenOf(dag, record).forEach((child, i) => {
      if (path.some(r => dag.identity[r] === dag.identity[child])) {
        const childChain = [...names, dag.names[child]].reverse();
        const childMatched = pattern.tokens.length <= childChain.length &&
          [...pattern.tokens].reverse().every((token, j) => childChain[j].some(name => nameContainsToken(name, token, false)));
        if (childMatched) {
          matches.push([...indexPath, i]);
        }
        return;
      }
      visit([...path, child], [...indexPath, i]);
    });
  };
  visit(scopeRecords, []);
  return matches;
}

/** The reference size: how many nodes the unfolding (no component below itself) has below the scope node. */
function unfoldedSize(dag: SearchDag, records: number[]): number {
  const size = (path: number[]): number => childrenOf(dag, path[path.length - 1])
    .reduce((sum, child) => sum + 1 + (path.some(r => dag.identity[r] === dag.identity[child]) ? 0 : size([...path, child])), 0);
  return 1 + size(records);
}

function scopeOf(dag: SearchDag, pattern: PathPattern, records: number[]): DagSearchScope {
  return {
    ancestors: records.slice(0, -1),
    record: records[records.length - 1],
    mask: pattern.stepAll(records.map(r => dag.names[r]))
  };
}

function expectSameAsUnfolding(dag: SearchDag, expr: string, records: number[]) {
  const pattern = new PathPattern(expr);
  const scope = scopeOf(dag, pattern, records);
  const search = new DagPathSearch(dag, pattern);
  const result = search.search(scope);
  const expected = unfoldedMatches(dag, expr, records);

  expect(result.exact).toBe(true);
  expect(result.total).toBe(expected.length);
  expect(result.nodes).toBe(unfoldedSize(dag, records));
  expected.forEach((path, k) => expect(search.locate(k)).toEqual(path));
  expect(search.locate(expected.length)).toBeUndefined();
}

describe('PathPattern', () => {
  it('matches a name whose parent matches the previous step', () => {
    const pattern = new PathPattern('Release/Identifier');
    const afterRelease = pattern.step(pattern.initialMask, 'Release');
    expect(pattern.isMatch(pattern.step(afterRelease, 'Identifier'))).toBe(true);
    expect(pattern.isMatch(pattern.step(pattern.step(afterRelease, 'Other'), 'Identifier'))).toBe(false);
  });

  it('splits camel case and compares case-insensitively as a substring', () => {
    const pattern = new PathPattern('ReleaseID');
    expect(pattern.isMatch(pattern.step(0, 'The Release ID Value'))).toBe(true);
    expect(pattern.isMatch(pattern.step(0, 'release id'))).toBe(true);
    expect(pattern.isMatch(pattern.step(0, 'Release'))).toBe(false);
  });

  it('reads an exact pattern as whole names', () => {
    const pattern = new PathPattern('/Root/Header', false, {exact: true});
    expect(pattern.isMatch(pattern.step(pattern.step(pattern.initialMask, 'Root'), 'Header'))).toBe(true);
    expect(pattern.isMatch(pattern.step(pattern.step(pattern.initialMask, 'The Root'), 'Header'))).toBe(false);
    expect(pattern.isMatch(pattern.step(pattern.step(pattern.initialMask, 'Root'), 'Sub Header'))).toBe(false);
  });

  it('starts an anchored pattern at the root only', () => {
    const pattern = new PathPattern('/Header/Item', false, {anchored: true});
    const root = pattern.step(pattern.initialMask, 'Root');
    expect(pattern.isMatch(pattern.step(pattern.step(root, 'Header'), 'Item'))).toBe(false);
    const header = pattern.step(pattern.initialMask, 'Header');
    expect(pattern.isMatch(pattern.step(header, 'Item'))).toBe(true);
  });

  it('accepts any of the alternative names of a node', () => {
    const pattern = new PathPattern('Shown');
    expect(pattern.isMatch(pattern.step(0, 'Internal', 'Shown Name'))).toBe(true);
  });
});

describe('DagPathSearch', () => {
  // 0 Root -> 1 Release, 2 Header ; 1 Release -> 3 Identifier, 4 Date ; 2 Header -> 1 Release, 3 Identifier
  const shared = dagOf([
    ['Root', [1, 2]],
    ['Release', [3, 4]],
    ['Header', [1, 3]],
    ['Identifier', []],
    ['Date', []]
  ]);

  it('counts every place a shared component is shown', () => {
    const search = new DagPathSearch(shared, new PathPattern('Identifier'));
    const result = search.search(scopeOf(shared, new PathPattern('Identifier'), [0]));
    expect(result.total).toBe(3);
  });

  it('follows the path to the parent', () => {
    expectSameAsUnfolding(shared, 'Release/Identifier', [0]);
    expectSameAsUnfolding(shared, 'Header/Release', [0]);
    expectSameAsUnfolding(shared, 'Root/Header/Identifier', [0]);
  });

  it('finds nothing for a path that does not exist, without error', () => {
    expectSameAsUnfolding(shared, 'Date/Identifier', [0]);
  });

  it('searches only the subtree of the selected node, but sees the ancestors above it', () => {
    expectSameAsUnfolding(shared, 'Identifier', [0, 2]);
    expectSameAsUnfolding(shared, 'Header/Release', [0, 2]);
    expectSameAsUnfolding(shared, 'Release/Identifier', [0, 2, 1]);
    expectSameAsUnfolding(shared, 'Root/Header', [0, 2]);
  });

  it('counts the selected node itself when it matches', () => {
    expectSameAsUnfolding(shared, 'Release', [0, 1]);
  });

  it('does not expand a component below itself', () => {
    // 0 Root -> 1 Item ; 1 Item -> 2 Name, 1 Item (itself)
    const recursive = dagOf([['Root', [1]], ['Item', [2, 1]], ['Name', []]]);
    expectSameAsUnfolding(recursive, 'Item', [0]);
    expectSameAsUnfolding(recursive, 'Item/Name', [0]);
    expectSameAsUnfolding(recursive, 'Item/Item', [0]);
  });

  it('only finds an anchored path from the root', () => {
    // 0 Root -> 1 Header, 2 Other ; 1 Header -> 3 Item ; 2 Other -> 1 Header
    const dag = dagOf([['Root', [1, 2]], ['Header', [3]], ['Other', [1]], ['Item', []]]);
    const anchored = new PathPattern('/Root/Header/Item', false, {anchored: true, exact: true});
    const result = new DagPathSearch(dag, anchored).search({ancestors: [], record: 0, mask: anchored.stepAll([dag.names[0]])});
    expect(result.total).toBe(1);

    const unanchoredPartial = new PathPattern('/Header/Item', false, {anchored: true, exact: true});
    const none = new DagPathSearch(dag, unanchoredPartial).search({ancestors: [], record: 0, mask: unanchoredPartial.stepAll([dag.names[0]])});
    expect(none.total).toBe(0);
  });

  it('counts through a cycle that spans several components', () => {
    // 0 Root -> 1 A ; 1 A -> 2 B, 4 Leaf ; 2 B -> 3 C ; 3 C -> 1 A, 4 Leaf
    const cycle = dagOf([['Root', [1]], ['A', [2, 4]], ['B', [3]], ['C', [1, 4]], ['Leaf', []]]);
    for (const expr of ['Leaf', 'A/B', 'C/A', 'B/C/Leaf', 'A/B/C/A', 'Root/A', 'A']) {
      expectSameAsUnfolding(cycle, expr, [0]);
      expectSameAsUnfolding(cycle, expr, [0, 1, 2]);
    }
  });

  it('matches a place by its own alternative name, and shares what is below it', () => {
    // 0 Root -> 1 Plain, 2 Shown|Plain ; both Plain records show the component 1 ; both lead to 3 Leaf
    const places = dagOf([['Root', [1, 2]], ['Plain', [3]], ['Plain|Shown', [3]], ['Leaf', []]], {2: 1});
    expectSameAsUnfolding(places, 'Shown', [0]);
    expectSameAsUnfolding(places, 'Shown/Leaf', [0]);
    expectSameAsUnfolding(places, 'Plain/Leaf', [0]);
    expectSameAsUnfolding(places, 'Root/Shown', [0]);
  });

  it('treats a place and its component as one in the rule against expanding a component below itself', () => {
    // 0 Root -> 2 Item' (record 2 shows the component 1) ; 1 Item -> 3 Leaf, 1 Item (itself) ; 2 Item' -> 3 Leaf, 1 Item
    const clone = dagOf([['Root', [2]], ['Item', [3, 1]], ['Item|Special', [3, 1]], ['Leaf', []]], {2: 1});
    for (const expr of ['Item', 'Special', 'Item/Item', 'Special/Item', 'Leaf', 'Item/Leaf']) {
      expectSameAsUnfolding(clone, expr, [0]);
    }
  });

  it('gives a lower bound, not a wrong exact answer, when the budget runs out', () => {
    const search = new DagPathSearch(shared, new PathPattern('Identifier'), 2);
    const result = search.search(scopeOf(shared, new PathPattern('Identifier'), [0]));
    expect(result.exact).toBe(false);
    expect(result.total).toBeLessThanOrEqual(3);
  });

  it('agrees with the unfolded tree on random DAGs with cycles', () => {
    let seed = 12345;
    const random = (n: number) => {
      seed = (seed * 1103515245 + 12345) & 0x7fffffff;
      return seed % n;
    };
    const words = ['Alpha', 'Beta', 'Gamma', 'Delta'];
    for (let round = 0; round < 60; round++) {
      const size = 3 + random(6);
      const adjacency: [string, number[]][] = [];
      for (let i = 0; i < size; i++) {
        const children = new Set<number>();
        for (let c = random(4); c > 0; c--) {
          children.add(random(size));
        }
        adjacency.push([words[random(words.length)], [...children]]);
      }
      const dag = dagOf(adjacency);
      for (const expr of ['Alpha', 'Beta/Alpha', 'Gamma/Delta', 'Alpha/Beta/Gamma', 'delta']) {
        expectSameAsUnfolding(dag, expr, [0]);
      }
    }
  });
});
