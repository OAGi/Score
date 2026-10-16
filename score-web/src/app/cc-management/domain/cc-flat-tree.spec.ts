import {CcFlatNode, CcFlatNodeDatabase} from './cc-flat-tree';
import {CcGraph} from './core-component-node';
import {DagPathSearch} from '../../common/tree-search/dag-path-search';
import {PathLikeExpressionEvaluator} from '../../common/flat-tree';
import {PathPattern} from '../../common/tree-search/path-pattern';

function graphWithUserExtensionAssociationWithoutTargetAcc(): CcGraph {
  return {
    graph: {
      nodes: {
        'ACC-1': {
          type: 'ACC', manifestId: 1, objectClassTerm: 'Extension', deprecated: false
        },
        'ASCC-2': {
          type: 'ASCC', manifestId: 2, deprecated: false
        },
        'ASCCP-3': {
          type: 'ASCCP', manifestId: 3, propertyTerm: 'Enterprise Unit User Extension Group', deprecated: false
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

  /** The reference: every node of the unfolded tree that the evaluator matches (the tree here has no cycle). */
  function walkedMatches(database: CcFlatNodeDatabase<CcFlatNode>, root: CcFlatNode, expr: string): number {
    const evaluator = new PathLikeExpressionEvaluator<CcFlatNode>(expr, false);
    const walk = (node: CcFlatNode): number =>
      (evaluator.eval(node) ? 1 : 0) + database.getChildren(node).reduce((sum, child) => sum + walk(child), 0);
    return walk(root);
  }

  function found(database: CcFlatNodeDatabase<CcFlatNode>, root: CcFlatNode, expr: string): number {
    const searchGraph = database.searchGraph(root);
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
