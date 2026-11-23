import {AccFlatNode, AsccpFlatNode, BccpFlatNode, CcFlatNode, CcFlatNodeDatabase} from './cc-flat-tree';
import {CcAccNodeInfo, CcAsccpNodeInfo, CcBccpNodeInfo, CcGraph, CcGraphNode} from './core-component-node';
import {DagPathSearch} from '../../common/tree-search/dag-path-search';
import {PathPattern} from '../../common/tree-search/path-pattern';
import {CcServerMarks} from './cc-server-search';
import {ServerPredicate} from '../../common/tree-search/server-predicate';

function graphWithUserExtensionAssociationWithoutTargetAcc(): CcGraph {
  return {
    graph: {
      nodes: {
        'ACC-1': {
          type: 'ACC', manifestId: 1, objectClassTerm: 'Extension', deprecated: false, state: 'Published'
        },
        'ASCC-2': {
          type: 'ASCC', manifestId: 2, deprecated: false, state: 'Published'
        },
        'ASCCP-3': {
          type: 'ASCCP', manifestId: 3, propertyTerm: 'Enterprise Unit User Extension Group', deprecated: false, state: 'Published'
        }
      },
      edges: {
        'ACC-1': {targets: ['ASCC-2']},
        'ASCC-2': {targets: ['ASCCP-3']}
      }
    }
  };
}

describe('CcFlatNodeDatabase', () => {
  it('does not throw when a user extension group has no target ACC', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(
      graphWithUserExtensionAssociationWithoutTargetAcc(), 'ACC', 1
    );

    expect(() => database.rootNode).not.toThrow();
    expect(database.rootNode.children).toEqual([]);
  });

  it('returns no children for an absent node', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(
      graphWithUserExtensionAssociationWithoutTargetAcc(), 'ACC', 1
    );

    expect(database.getChildren()).toEqual([]);
  });

  it('ignores a dangling ACC target in the graph', () => {
    const graph = graphWithUserExtensionAssociationWithoutTargetAcc();
    graph.graph.edges['ACC-1'] = {targets: ['ACC-404']};
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph, 'ACC', 1);

    expect(() => database.rootNode).not.toThrow();
    expect(database.rootNode.children).toEqual([]);
  });
});

describe('CcFlatNodeDatabase.searchGraph', () => {
  /**
   * Root -> Release (an ASCCP whose ACC Item has the BCCP Identifier) and Root -> Identifier (the same BCCP, whose
   * data type has the supplementary component Scheme Identifier).
   */
  function graph(): CcGraph {
    const flags = {deprecated: false, state: 'Published'};
    return {
      graph: {
        nodes: {
          'ACC-1': {type: 'ACC', manifestId: 1, objectClassTerm: 'Root', ...flags},
          'ASCC-2': {type: 'ASCC', manifestId: 2, cardinalityMin: 0, cardinalityMax: 1, ...flags},
          'ASCCP-3': {type: 'ASCCP', manifestId: 3, propertyTerm: 'Release', ...flags},
          'ACC-4': {type: 'ACC', manifestId: 4, objectClassTerm: 'Item', ...flags},
          'BCC-5': {type: 'BCC', manifestId: 5, entityType: 'Element', cardinalityMin: 0, cardinalityMax: 1, ...flags},
          'BCC-9': {type: 'BCC', manifestId: 9, entityType: 'Element', cardinalityMin: 0, cardinalityMax: 1, ...flags},
          'BCCP-6': {type: 'BCCP', manifestId: 6, propertyTerm: 'Identifier', ...flags},
          'DT-7': {type: 'DT', manifestId: 7, den: 'Identifier. Type', ...flags},
          'DT_SC-8': {
            type: 'DT_SC', manifestId: 8, propertyTerm: 'Scheme Identifier', representationTerm: 'Identifier',
            objectClassTerm: 'Identifier', cardinalityMin: 0, cardinalityMax: 1, ...flags
          }
        },
        edges: {
          'ACC-1': {targets: ['ASCC-2', 'BCC-9']},
          'ASCC-2': {targets: ['ASCCP-3']},
          'ASCCP-3': {targets: ['ACC-4']},
          'ACC-4': {targets: ['BCC-5']},
          'BCC-5': {targets: ['BCCP-6']},
          'BCC-9': {targets: ['BCCP-6']},
          'BCCP-6': {targets: ['DT-7']},
          'DT-7': {targets: ['DT_SC-8']}
        }
      }
    } as unknown as CcGraph;
  }

  /** The reference: every node of the unfolded tree that the pattern matches, judged by the chain down to it (the tree here has no cycle). */
  function walkedMatches(database: CcFlatNodeDatabase<CcFlatNode>, root: CcFlatNode, expr: string): number {
    const pattern = new PathPattern(expr);
    const matches = (node: CcFlatNode): boolean => {
      const chain: CcFlatNode[] = [];
      for (let n = node; n; n = n.parent as CcFlatNode) {
        chain.unshift(n);
      }
      return pattern.isMatch(pattern.stepAll(chain.map(n => [n.name])));
    };
    const walk = (node: CcFlatNode): number =>
      (matches(node) ? 1 : 0) + database.getChildren(node).reduce((sum, child) => sum + walk(child), 0);
    return walk(root);
  }

  function found(database: CcFlatNodeDatabase<CcFlatNode>, root: CcFlatNode, expr: string, marks?: CcServerMarks): number {
    const searchGraph = database.searchGraph(root, marks);
    const pattern = new PathPattern(expr);
    const chain: CcFlatNode[] = [];
    for (let n = root; n; n = n.parent as CcFlatNode) {
      chain.unshift(n);
    }
    return new DagPathSearch(searchGraph.dag, pattern).search({
      ancestors: chain.slice(0, -1).map(n => searchGraph.recordOf(n)),
      record: searchGraph.recordOf(root),
      mask: pattern.stepAll(chain.map(n => [n.name]))
    }).total;
  }

  it('flags a node by the definition of what it is: its association when it has one, else its component', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);
    const root = database.rootNode;
    const predicate = (id: string, attribute: string): ServerPredicate =>
      ({id, flag: 'srv_' + id, attribute, operator: 'CONTAINS', value: 'x'});
    const none = {accs: [], asccs: [], asccps: [], bccs: [], bccps: [], dts: [], dtScs: []};
    const marks = new CcServerMarks([predicate('0', 'definition'), predicate('1', 'association_definition'),
      predicate('2', 'component_definition'), predicate('3', 'type_definition')], {
      matches: {
        '0': {...none, asccs: [2], bccs: [9], accs: [4]},
        '1': {...none, asccs: [2], bccs: [9]},
        '2': {...none, asccps: [3], bccps: [6]},
        '3': {...none, dts: [7]}
      }
    }, false);
    const count = (expr: string) => found(database, root, expr, marks);

    // an ASCCP that is tied to its parent by an ASCC: the ASCC's definition is the one it is about
    expect(count('Release[srv_0]')).toBe(1);
    expect(count('Release[srv_1]')).toBe(1);
    expect(count('Release[srv_2]')).toBe(1);
    // a BCCP appears twice: under a BCC that has the text and under one that has not
    expect(count('Identifier[type = BCCP and srv_0]')).toBe(1);
    expect(count('Identifier[type = BCCP and not(srv_0)]')).toBe(1);
    expect(count('Identifier[type = BCCP and srv_2]')).toBe(2);
    expect(count('Identifier[type = BCCP and srv_3]')).toBe(2);
    // an ACC has no association and no component of its own to ask for
    expect(count('Item[srv_0]')).toBe(1);
    expect(count('Item[srv_1]')).toBe(0);
    expect(count('Item[srv_2]')).toBe(0);
  });

  it('puts no definition text into the graph when the backend has not been asked', () => {
    const withDefinitions = graph();
    (withDefinitions.graph.nodes as any)['ASCC-2'].definition = 'The association';
    const database = new CcFlatNodeDatabase<CcFlatNode>(withDefinitions, 'ACC', 1);

    expect(found(database, database.rootNode, "Release[definition = 'The association']")).toBe(0);
    expect(found(database, database.rootNode, 'Release')).toBe(1);
  });

  it('counts what walking the tree finds, for single names and for paths', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);
    const root = database.rootNode;

    for (const expr of ['Identifier', 'Release', 'Item', 'Release/Item', 'Item/Identifier', 'Identifier/Scheme', 'Details', 'Nothing']) {
      expect(found(database, root, expr)).toBe(walkedMatches(database, root, expr));
    }
    expect(found(database, root, 'Identifier')).toBe(4);
  });

  it('gives a node whose details were loaded with another name a record of its own', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);
    const root = database.rootNode;
    const release = root.children[0] as any;
    release.detail = {asccp: {propertyTerm: 'Shown'}};

    expect(found(database, root, 'Shown')).toBe(1);
    expect(found(database, root, 'Release')).toBe(0);
    expect(found(database, root, 'Identifier')).toBe(4);
  });

  it('judges a node with an unsaved definition by that text instead of what the backend has saved', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);
    const root = database.rootNode;
    const release = root.children[0] as any;
    release.detail = {isChanged: true, asccp: {propertyTerm: 'Release', definition: 'Component'},
      ascc: {definition: 'Brand new association text'}};
    const none = {accs: [], asccs: [], asccps: [], bccs: [], bccps: [], dts: [], dtScs: []};
    const predicate = (value: string): ServerPredicate => ({id: '0', flag: 'srv_0', attribute: 'definition', operator: 'CONTAINS', value});
    const marks = (value: string, saved: number[]) =>
      new CcServerMarks([predicate(value)], {matches: {'0': {...none, asccs: saved}}}, false);

    expect(found(database, root, 'Release[srv_0]', marks('brand new', []))).toBe(1);
    expect(found(database, root, 'Release[srv_0]', marks('old text', [2]))).toBe(0);
    // what is not edited keeps the answer of the backend
    expect(found(database, root, 'Identifier[type = BCCP and srv_0]', marks('x', []))).toBe(0);
  });

  it('follows the children a node has actually been given', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);
    const root = database.rootNode;
    const item = root.children[0].children[0];
    item.children = [];
    item.expandable = false; // as loadChildren leaves a node that has none

    // the Identifier below Item is gone, together with its supplementary component
    expect(found(database, root, 'Identifier')).toBe(2);
    expect(found(database, root, 'Scheme')).toBe(1);
  });

  it('leaves out prohibited supplementary components only when they are hidden', () => {
    const data = graph();
    (data.graph.nodes['DT_SC-8'] as any).cardinalityMax = 0;
    const database = new CcFlatNodeDatabase<CcFlatNode>(data, 'ACC', 1);

    // a prohibited supplementary component is not part of the tree at all (see getChildren)
    expect(found(database, database.rootNode, 'Scheme')).toBe(walkedMatches(database, database.rootNode, 'Scheme'));
  });
});

describe('CcFlatNodeDatabase.searchGraph and what the tree shows', () => {
  /**
   * Root ACC -> ASCCP P (role of ACC A) -> ACC A -> ASCCP Q (role of ACC B) -> ACC B -> ASCCP R (role of ACC A) -> ACC A -> ASCCP Q.
   * ACC A comes twice along the path (below P and below R), and the screens expand both, as only an ASCCP that is among
   * its own ancestors is not expanded: Q below the second A is the cycle.
   */
  function graph(): CcGraph {
    const flags = {deprecated: false, state: 'Published'};
    return {
      graph: {
        nodes: {
          'ACC-1': {type: 'ACC', manifestId: 1, objectClassTerm: 'Root', ...flags},
          'ASCC-10': {type: 'ASCC', manifestId: 10, cardinalityMin: 0, cardinalityMax: 1, ...flags},
          'ASCCP-11': {type: 'ASCCP', manifestId: 11, propertyTerm: 'P', ...flags},
          'ACC-2': {type: 'ACC', manifestId: 2, objectClassTerm: 'A', ...flags},
          'ASCC-12': {type: 'ASCC', manifestId: 12, cardinalityMin: 0, cardinalityMax: 1, ...flags},
          'ASCCP-13': {type: 'ASCCP', manifestId: 13, propertyTerm: 'Q', ...flags},
          'ACC-3': {type: 'ACC', manifestId: 3, objectClassTerm: 'B', ...flags},
          'ASCC-14': {type: 'ASCC', manifestId: 14, cardinalityMin: 0, cardinalityMax: 1, ...flags},
          'ASCCP-15': {type: 'ASCCP', manifestId: 15, propertyTerm: 'R', ...flags},
          'BCC-16': {type: 'BCC', manifestId: 16, entityType: 'Element', cardinalityMin: 0, cardinalityMax: 1, deprecated: true, state: 'Published'},
          'BCCP-17': {type: 'BCCP', manifestId: 17, propertyTerm: 'Leaf', ...flags},
          'DT-18': {type: 'DT', manifestId: 18, den: 'Leaf. Type', ...flags}
        },
        edges: {
          'ACC-1': {targets: ['ASCC-10']},
          'ASCC-10': {targets: ['ASCCP-11']},
          'ASCCP-11': {targets: ['ACC-2']},
          'ACC-2': {targets: ['ASCC-12', 'BCC-16']},
          'ASCC-12': {targets: ['ASCCP-13']},
          'ASCCP-13': {targets: ['ACC-3']},
          'ACC-3': {targets: ['ASCC-14']},
          'ASCC-14': {targets: ['ASCCP-15']},
          'ASCCP-15': {targets: ['ACC-2']},
          'BCC-16': {targets: ['BCCP-17']},
          'BCCP-17': {targets: ['DT-18']}
        }
      }
    } as unknown as CcGraph;
  }

  function count(database: CcFlatNodeDatabase<CcFlatNode>, expr: string): number {
    const root = database.rootNode;
    const searchGraph = database.searchGraph(root);
    const pattern = new PathPattern(expr);
    return new DagPathSearch(searchGraph.dag, pattern).search({
      ancestors: [], record: searchGraph.recordOf(root),
      mask: pattern.stepAll([[root.name]], [attribute => searchGraph.dag.attributes?.[attribute]?.[searchGraph.recordOf(root)]])
    }).total;
  }

  /** The tree as the screens unfold it: an ASCCP that is among its ancestors is shown, not expanded. */
  function screenSize(database: CcFlatNodeDatabase<CcFlatNode>): number {
    const walk = (node: CcFlatNode): number => 1 + (node instanceof AsccpFlatNode && node.isCycle ? 0
      : database.getChildren(node).reduce((sum, child) => sum + walk(child), 0));
    return walk(database.rootNode);
  }

  it('expands an ACC each time it comes along a path, as the tree does, until an ASCCP repeats', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);

    expect(count(database, '//*')).toBe(screenSize(database));
    // A comes below P and below R; the second Q is the cycle (shown, not expanded)
    expect(count(database, '//*[type = ACC and object_class_term = "A"]')).toBe(2);
    expect(count(database, '//*[type = ASCCP and property_term = "Q"]')).toBe(2);
    expect(count(database, '//*[type = BCCP]')).toBe(2);
  });

  it('judges a node deprecated by what the tree shows: its association too', () => {
    const database = new CcFlatNodeDatabase<CcFlatNode>(graph(), 'ACC', 1);

    // the BCC is deprecated, its BCCP is not: the node is shown as deprecated
    expect(count(database, '//*[deprecated]')).toBe(2);
    expect(count(database, '//*[type = BCCP and not(deprecated)]')).toBe(0);
  });
});

describe('CcFlatNode with a cleared Property Term', () => {
  const graphNode = {deprecated: false} as unknown as CcGraphNode;

  it('has an empty name and query path for a BCCP whose Property Term is cleared', () => {
    const node = new BccpFlatNode({...graphNode, propertyTerm: undefined} as CcGraphNode, undefined);
    node.detail = {bccp: {propertyTerm: undefined}} as unknown as CcBccpNodeInfo;

    expect(node.name).toBe('');
    expect(() => node.queryPath).not.toThrow();
    expect(node.queryPath).toBe('');
  });

  it('has an empty name and query path for an ASCCP whose Property Term is cleared', () => {
    const node = new AsccpFlatNode({...graphNode, propertyTerm: undefined} as CcGraphNode);
    node.detail = {asccp: {propertyTerm: undefined}} as unknown as CcAsccpNodeInfo;

    expect(node.name).toBe('');
    expect(() => node.queryPath).not.toThrow();
  });
});
