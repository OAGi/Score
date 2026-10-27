import {describe, expect, it, vi} from 'vitest';
import {firstValueFrom} from 'rxjs';
import {
  AccFlatNode,
  BccpFlatNode,
  CcFlatNode,
  CcFlatNodeDatabase,
  CcFlatNodeDataSource,
  CcFlatNodeDataSourceSearcher,
  DtFlatNode,
  DtScFlatNode
} from './cc-flat-tree';
import {CcGraph} from './core-component-node';
import {DagPathSearch} from '../../common/tree-search/dag-path-search';
import {PathPattern} from '../../common/tree-search/path-pattern';

/**
 * A supplementary component has the cardinality 0..0 (prohibited), 0..1 (optional) or 1..1 (required), so these are the
 * only ones used here.
 *
 * What "Hide prohibited" does today, written down before it is moved to the shared tree filters. It only has an effect
 * on a tree whose root is a data type (the screen of a data type): the supplementary components with the cardinality
 * 0..0 are the nodes that are prohibited.
 */

type SupplementaryComponent = {id: number, term: string, min: number, max: number};

const ALPHA = {id: 2, term: 'Alpha', min: 0, max: 1};
const BETA_PROHIBITED = {id: 3, term: 'Beta', min: 0, max: 0};
const GAMMA = {id: 4, term: 'Gamma', min: 1, max: 1};
const DELTA_PROHIBITED = {id: 5, term: 'Delta', min: 0, max: 0};
const EPSILON = {id: 6, term: 'Epsilon', min: 0, max: 1};

const ALL = [ALPHA, BETA_PROHIBITED, GAMMA, DELTA_PROHIBITED, EPSILON];

/** The name a supplementary component gets in the tree. */
const nameOf = (sc: SupplementaryComponent) => 'Code. ' + sc.term + '. Text';

function scNodes(scs: SupplementaryComponent[]): Record<string, unknown> {
  const nodes: Record<string, unknown> = {};
  scs.forEach(sc => {
    nodes['DT_SC-' + sc.id] = {
      type: 'DT_SC', manifestId: sc.id, deprecated: false, state: 'Published',
      objectClassTerm: 'Code', propertyTerm: sc.term, representationTerm: 'Text',
      cardinalityMin: sc.min, cardinalityMax: sc.max
    };
  });
  return nodes;
}

/** A data type with the supplementary components. */
function dtGraph(scs: SupplementaryComponent[] = ALL): CcGraph {
  return {
    graph: {
      nodes: {
        'DT-1': {type: 'DT', manifestId: 1, den: 'Code. Type', deprecated: false, state: 'Published'},
        ...scNodes(scs)
      },
      edges: {'DT-1': {targets: scs.map(sc => 'DT_SC-' + sc.id)}}
    }
  } as unknown as CcGraph;
}

/** A core component whose BCCP has the data type, so that the supplementary components are reached from above. */
function accGraph(scs: SupplementaryComponent[] = ALL): CcGraph {
  const flags = {deprecated: false, state: 'Published'};
  return {
    graph: {
      nodes: {
        'ACC-10': {type: 'ACC', manifestId: 10, objectClassTerm: 'Root', ...flags},
        'BCC-11': {type: 'BCC', manifestId: 11, entityType: 'Element', cardinalityMin: 0, cardinalityMax: 1, ...flags},
        'BCCP-12': {type: 'BCCP', manifestId: 12, propertyTerm: 'Value', ...flags},
        'DT-1': {type: 'DT', manifestId: 1, den: 'Code. Type', ...flags},
        ...scNodes(scs)
      },
      edges: {
        'ACC-10': {targets: ['BCC-11']},
        'BCC-11': {targets: ['BCCP-12']},
        'BCCP-12': {targets: ['DT-1']},
        'DT-1': {targets: scs.map(sc => 'DT_SC-' + sc.id)}
      }
    }
  } as unknown as CcGraph;
}

function dtTree(scs: SupplementaryComponent[] = ALL) {
  const database = new CcFlatNodeDatabase<CcFlatNode>(dtGraph(scs), 'DT', 1);
  const dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, null as any);
  dataSource.init();
  return {database, dataSource, root: dataSource.data[0]};
}

const names = (nodes: {name: string}[]) => nodes.map(e => e.name);

describe('Hide prohibited: which node is prohibited', () => {
  it('is a supplementary component whose cardinality is 0..0', () => {
    const {root} = dtTree();
    const byName = (sc: SupplementaryComponent) => (root.getChildren() as CcFlatNode[]).find(e => e.name === nameOf(sc));

    expect(byName(ALPHA).inhibited).toBe(false);
    expect(byName(BETA_PROHIBITED).inhibited).toBe(true);
    expect(byName(GAMMA).inhibited).toBe(false);
    expect(byName(DELTA_PROHIBITED).inhibited).toBe(true);
    expect(byName(EPSILON).inhibited).toBe(false);
  });

  it('is read from the component of the graph, not from a cardinality edited on the node', () => {
    const {root} = dtTree();
    const alpha = (root.getChildren() as DtScFlatNode[]).find(e => e.name === nameOf(ALPHA));

    alpha.cardinalityMax = 0;

    expect(alpha.cardinalityMax).toBe(0);
    expect(alpha.inhibited).toBe(false);
  });

  it('is never a node of another kind', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(accGraph(), 'ACC', 10);
    const root = database.rootNode;
    const bccp = root.children[0] as CcFlatNode;

    expect(root).toBeInstanceOf(AccFlatNode);
    expect(bccp).toBeInstanceOf(BccpFlatNode);
    expect(root.inhibited).toBe(false);
    expect(bccp.inhibited).toBe(false);
    expect(dtTree().root).toBeInstanceOf(DtFlatNode);
    expect(dtTree().root.inhibited).toBe(false);
  });
});

describe('Hide prohibited: the children of a node', () => {
  it('shows every supplementary component of a data type while it is off', () => {
    const {dataSource, root} = dtTree();

    expect(dataSource.hideProhibited).toBe(false);
    expect(names(root.children)).toEqual(ALL.map(nameOf));
  });

  it('leaves out the prohibited ones of `children` while it is on, but not of `getChildren()`', () => {
    const {dataSource, root} = dtTree();

    dataSource.hideProhibited = true;

    expect(names(root.children)).toEqual([ALPHA, GAMMA, EPSILON].map(nameOf));
    expect(names(root.getChildren() as CcFlatNode[])).toEqual(ALL.map(nameOf));
    expect(names(root.getChildren({hideProhibited: true}) as CcFlatNode[])).toEqual([ALPHA, GAMMA, EPSILON].map(nameOf));
    expect(names(root.getChildren({hideProhibited: false}) as CcFlatNode[])).toEqual(ALL.map(nameOf));
  });

  it('never has a prohibited supplementary component below a core component, whether it is hidden or not', () => {
    // Below a core component the graph is taken with `cardinalityMax > 0`, so a prohibited one is not in it.
    for (const hide of [false, true]) {
      const database = new CcFlatNodeDatabase<CcFlatNode>(accGraph(), 'ACC', 10);
      const dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, null as any);
      dataSource.init();
      dataSource.hideProhibited = hide;
      const bccp = dataSource.data[0].children[0] as CcFlatNode;

      database.loadChildren(bccp);

      expect(names(bccp.children)).toEqual([ALPHA, GAMMA, EPSILON].map(nameOf));
    }
  });

  it('is expandable while a visible child is left, and not when only prohibited ones are', () => {
    const onlyProhibited = dtTree([BETA_PROHIBITED, DELTA_PROHIBITED]);

    expect(onlyProhibited.root.expandable).toBe(true);
    onlyProhibited.dataSource.hideProhibited = true;
    expect(onlyProhibited.root.expandable).toBe(false);
    onlyProhibited.dataSource.hideProhibited = false;
    expect(onlyProhibited.root.expandable).toBe(true);

    const mixed = dtTree();
    mixed.dataSource.hideProhibited = true;
    expect(mixed.root.expandable).toBe(true);
  });
});

describe('Hide prohibited: the rows', () => {
  it('lists the supplementary components under an expanded data type, the prohibited ones too while it is off', () => {
    const {dataSource, root} = dtTree();

    dataSource.expand(root);

    expect(names(dataSource.data)).toEqual(['Code. Type', ...ALL.map(nameOf)]);
    expect(dataSource.data.slice(1).every(e => e.level === 1)).toBe(true);
  });

  it('takes the prohibited rows out when it is switched on, and keeps the order of the others', () => {
    const {dataSource, root} = dtTree();
    dataSource.expand(root);

    dataSource.hideProhibited = true;

    expect(names(dataSource.data)).toEqual(['Code. Type', ...[ALPHA, GAMMA, EPSILON].map(nameOf)]);
    expect(root.expanded).toBe(true);
  });

  it('brings the prohibited rows back in their place when it is switched off, and keeps the data type expanded', () => {
    const {dataSource, root} = dtTree();
    dataSource.expand(root);
    dataSource.hideProhibited = true;

    dataSource.hideProhibited = false;

    expect(names(dataSource.data)).toEqual(['Code. Type', ...ALL.map(nameOf)]);
    expect(root.expanded).toBe(true);
    expect(dataSource.data.every(e => dataSource.isExpanded(e) === (e === root))).toBe(true);
  });

  it('leaves a collapsed data type collapsed, whichever way it is switched', () => {
    const {dataSource, root} = dtTree();

    dataSource.hideProhibited = true;
    expect(dataSource.data).toEqual([root]);
    expect(root.expanded).toBe(false);

    dataSource.hideProhibited = false;
    expect(dataSource.data).toEqual([root]);
    expect(root.expanded).toBe(false);
  });

  it('lists only the visible rows when a data type is expanded while it is on', () => {
    const {dataSource, root} = dtTree();
    dataSource.hideProhibited = true;

    dataSource.expand(root);
    expect(names(dataSource.data)).toEqual(['Code. Type', ...[ALPHA, GAMMA, EPSILON].map(nameOf)]);

    dataSource.collapse(root);
    expect(dataSource.data).toEqual([root]);

    dataSource.hideProhibited = false;
    dataSource.expand(root);
    expect(names(dataSource.data)).toEqual(['Code. Type', ...ALL.map(nameOf)]);
  });

  it('is applied by expandTo as well', () => {
    const {dataSource, root} = dtTree();
    dataSource.hideProhibited = true;
    const gamma = root.children.find(e => e.name === nameOf(GAMMA)) as CcFlatNode;

    dataSource.expandTo(gamma);

    expect(names(dataSource.data)).toEqual(['Code. Type', ...[ALPHA, GAMMA, EPSILON].map(nameOf)]);
    expect(root.expanded).toBe(true);
  });

  it('leaves out the prohibited rows of a node that knows no data source, as the data source lists them', () => {
    const {dataSource, root} = dtTree();
    dataSource.hideProhibited = true;
    root.dataSource = undefined;
    expect(root.children.length).toBe(ALL.length);

    dataSource.toggleNode(root, true);

    expect(names(dataSource.data)).toEqual(['Code. Type', ...[ALPHA, GAMMA, EPSILON].map(nameOf)]);
  });

  it('forgets whether a node is expandable when it is switched off, so that it is asked again', () => {
    const {dataSource, root} = dtTree();
    dataSource.hideProhibited = true;
    root.expandable = false;
    expect(root.expandable).toBe(false);

    dataSource.hideProhibited = false;

    expect(root.expandable).toBe(true);
  });

  it('does not change anything when it is set to what it already is', () => {
    const {dataSource, root} = dtTree();
    dataSource.expand(root);
    const rows = dataSource.data;
    const rebuilt = vi.fn();
    dataSource.dataChange.subscribe(rebuilt);
    rebuilt.mockClear();

    dataSource.hideProhibited = false;

    expect(dataSource.data).toBe(rows);
    expect(rebuilt).not.toHaveBeenCalled();
  });

  it('gives the same rows after being switched on and off again as it started with', () => {
    const {dataSource, root} = dtTree();
    dataSource.expand(root);
    const before = [...dataSource.data];

    for (let i = 0; i < 3; i++) {
      dataSource.hideProhibited = true;
      dataSource.hideProhibited = false;
    }

    expect(dataSource.data).toEqual(before);
  });

  it('shows a data type with nothing prohibited as it is', () => {
    const {dataSource, root} = dtTree([ALPHA, GAMMA]);
    dataSource.expand(root);
    dataSource.hideProhibited = true;

    expect(names(dataSource.data)).toEqual(['Code. Type', nameOf(ALPHA), nameOf(GAMMA)]);
  });
});

describe('Hide prohibited: the announcement', () => {
  function listen(dataSource: CcFlatNodeDataSource<CcFlatNode>) {
    const events: {name: string, value: unknown, rows: number}[] = [];
    dataSource.addListener({
      onChange: (entity: any, name: string, value: unknown) => events.push({name, value, rows: entity.data.length})
    } as any);
    return events;
  }

  it('is made once for each change, after the rows are rebuilt', () => {
    const {dataSource, root} = dtTree();
    dataSource.expand(root);
    const events = listen(dataSource);

    dataSource.hideProhibited = true;
    dataSource.hideProhibited = true;
    dataSource.hideProhibited = false;
    dataSource.hideProhibited = false;

    expect(events.filter(e => e.name === 'hideProhibited')).toEqual([
      {name: 'hideProhibited', value: true, rows: 4},
      {name: 'hideProhibited', value: false, rows: 6}
    ]);
  });

  it('resets the search of the searcher, once for each change', () => {
    const {database, dataSource} = dtTree();
    const searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(dataSource, database);
    const reset = vi.spyOn(searcher, 'resetSearch');

    dataSource.hideProhibited = true;
    expect(reset).toHaveBeenCalledTimes(1);
    dataSource.hideProhibited = true;
    expect(reset).toHaveBeenCalledTimes(1);
    dataSource.hideProhibited = false;
    expect(reset).toHaveBeenCalledTimes(2);
  });
});

describe('Hide prohibited: the search', () => {
  function found(database: CcFlatNodeDatabase<CcFlatNode>, root: CcFlatNode, expr: string): number {
    const graph = database.searchGraph(root);
    const pattern = new PathPattern(expr);
    return new DagPathSearch(graph.dag, pattern).search({
      ancestors: [],
      record: graph.recordOf(root),
      mask: pattern.stepAll([[root.name]])
    }).total;
  }

  it('finds a prohibited supplementary component only while it is shown', () => {
    const {database, dataSource, root} = dtTree();

    for (const term of ['Beta', 'Delta']) {
      dataSource.hideProhibited = false;
      expect(found(database, root, term)).toBe(1);
      dataSource.hideProhibited = true;
      expect(found(database, root, term)).toBe(0);
    }
  });

  it('finds the other supplementary components whether it is on or off', () => {
    const {database, dataSource, root} = dtTree();

    for (const hide of [false, true]) {
      dataSource.hideProhibited = hide;
      for (const term of ['Alpha', 'Gamma', 'Epsilon']) {
        expect(found(database, root, term)).toBe(1);
      }
    }
  });

  /** The row of the current match, or undefined when there is none (the searcher answers -1). */
  async function rowFound(searcher: CcFlatNodeDataSourceSearcher<CcFlatNode>, dataSource: CcFlatNodeDataSource<CcFlatNode>,
                          keyword: string, root: CcFlatNode): Promise<CcFlatNode | undefined> {
    const index = await firstValueFrom(searcher.search(keyword, root, false, true));
    return index < 0 ? undefined : dataSource.data[index];
  }

  it('searches the rows through the searcher: a hidden node is not found, a shown one is', async () => {
    const {database, dataSource, root} = dtTree();
    const searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(dataSource, database);
    dataSource.hideProhibited = true;

    expect(await rowFound(searcher, dataSource, 'Beta', root)).toBeUndefined();
    const gamma = await rowFound(searcher, dataSource, 'Gamma', root);
    expect(gamma?.name).toBe(nameOf(GAMMA));
    expect(searcher.isSearchResult(gamma)).toBe(true);
    expect(names(dataSource.data)).toEqual(['Code. Type', ...[ALPHA, GAMMA, EPSILON].map(nameOf)]);
  });

  it('finds a prohibited node once it is shown again, after a search that did not', async () => {
    const {database, dataSource, root} = dtTree();
    const searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(dataSource, database);
    dataSource.hideProhibited = true;
    expect(await rowFound(searcher, dataSource, 'Beta', root)).toBeUndefined();

    dataSource.hideProhibited = false;

    expect((await rowFound(searcher, dataSource, 'Beta', root))?.name).toBe(nameOf(BETA_PROHIBITED));
  });

  it('forgets the result of a search when it is switched, so that a hidden match is not kept', async () => {
    const {database, dataSource, root} = dtTree();
    const searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(dataSource, database);
    const beta = await rowFound(searcher, dataSource, 'Beta', root);
    expect(beta?.name).toBe(nameOf(BETA_PROHIBITED));
    expect(searcher.isSearchResult(beta)).toBe(true);

    dataSource.hideProhibited = true;

    expect(searcher.isSearchResult(beta)).toBe(false);
  });
});
