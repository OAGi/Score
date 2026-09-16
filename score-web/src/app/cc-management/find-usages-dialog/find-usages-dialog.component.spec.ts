import {CcFlatNode} from '../domain/cc-flat-tree';
import {CcGraph} from '../domain/core-component-node';
import {FindUsagesCcFlatNodeDatabase, FindUsagesDialogComponent} from './find-usages-dialog.component';

function graphWithUserExtensionAssociationWithoutTargetAcc(): CcGraph {
  return {
    graph: {
      nodes: {
        'ACC-1': {
          type: 'ACC', manifestId: 1, objectClassTerm: 'Extension', deprecated: false
        },
        'ASCCP-3': {
          type: 'ASCCP', manifestId: 3, propertyTerm: 'Enterprise Unit User Extension Group', deprecated: false
        }
      },
      edges: {
        'ACC-1': {targets: ['ASCCP-3']}
      }
    }
  };
}

function graphWithUserExtensionAssociationWithTargetAcc(): CcGraph {
  const graph = graphWithUserExtensionAssociationWithoutTargetAcc();
  graph.graph.nodes['ACC-4'] = {
    type: 'ACC', manifestId: 4, objectClassTerm: 'Enterprise Unit User Extension Group', deprecated: false
  };
  graph.graph.nodes['ASCCP-5'] = {
    type: 'ASCCP', manifestId: 5, propertyTerm: 'Product Classification', deprecated: false
  };
  graph.graph.edges['ASCCP-3'] = {targets: ['ACC-4']};
  graph.graph.edges['ACC-4'] = {targets: ['ASCCP-5']};
  return graph;
}

describe('FindUsagesDialogComponent', () => {
  it('should be defined', () => {
    expect(FindUsagesDialogComponent).toBeTruthy();
  });

  it('does not throw when a user extension group has no target ACC', () => {
    const database = new FindUsagesCcFlatNodeDatabase<CcFlatNode>(
      graphWithUserExtensionAssociationWithoutTargetAcc(), 'ACC', 1
    );

    expect(() => database.rootNode).not.toThrow();
    expect(database.rootNode.children).toEqual([]);
  });

  it('flattens children from a user extension group target ACC', () => {
    const database = new FindUsagesCcFlatNodeDatabase<CcFlatNode>(
      graphWithUserExtensionAssociationWithTargetAcc(), 'ACC', 1
    );
    const root = database.rootNode;
    const child = root.children[0];

    expect(root.children).toHaveLength(1);
    expect(child.name).toBe('Product Classification');
    expect(child.level).toBe(1);
    expect(child.parent).toBe(root);
  });
});
