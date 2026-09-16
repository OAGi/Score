import {CcFlatNode, CcFlatNodeDatabase} from './cc-flat-tree';
import {CcGraph} from './core-component-node';

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
