import {CcFlatNode, CcFlatNodeDatabase, CcFlatNodeDataSource} from './cc-flat-tree';
import {CcGraph} from './core-component-node';
import {findNodeByLocation} from './cc-node-navigator';
import {ValidationLocation, ValidationPathSegment} from '../../validation/validation';

/**
 * The addresses the backend puts in a violation (AccValidationTest) must be the ones this tree gives its nodes; the
 * graph and the addresses here are the ones of that test, and a change on one side has to be made on the other.
 */
function graph(userExtensionGroup: boolean): CcGraph {
  const flags = {deprecated: false, state: 'WIP'};
  return {
    graph: {
      nodes: {
        'ACC-1': {type: 'ACC', manifestId: 1, objectClassTerm: 'Root', ...flags},
        'ACC-2': {type: 'ACC', manifestId: 2, objectClassTerm: 'Base', ...flags},
        'ACC-3': {type: 'ACC', manifestId: 3, objectClassTerm: 'Group', ...flags},
        'ASCC-10': {type: 'ASCC', manifestId: 10, cardinalityMin: 0, cardinalityMax: 1, ...flags},
        'ASCCP-100': {
          type: 'ASCCP', manifestId: 100, propertyTerm: userExtensionGroup ? 'Root User Extension Group' : 'Group', ...flags
        },
        'BCC-20': {type: 'BCC', manifestId: 20, entityType: 'Element', cardinalityMin: 0, cardinalityMax: 1, ...flags},
        'BCCP-200': {type: 'BCCP', manifestId: 200, propertyTerm: 'Name', ...flags},
        'BCC-30': {type: 'BCC', manifestId: 30, entityType: 'Element', cardinalityMin: 0, cardinalityMax: 1, ...flags},
        'BCCP-300': {type: 'BCCP', manifestId: 300, propertyTerm: 'Name', ...flags},
        'DT-7': {type: 'DT', manifestId: 7, den: 'Text. Type', ...flags}
      },
      edges: {
        'ACC-1': {targets: ['ACC-2', 'ASCC-10']},
        'ACC-2': {targets: ['BCC-20']},
        'ASCC-10': {targets: ['ASCCP-100']},
        'ASCCP-100': {targets: ['ACC-3']},
        'ACC-3': {targets: ['BCC-30']},
        'BCC-20': {targets: ['BCCP-200']},
        'BCC-30': {targets: ['BCCP-300']},
        'BCCP-200': {targets: ['DT-7']},
        'BCCP-300': {targets: ['DT-7']}
      }
    }
  } as unknown as CcGraph;
}

function dataSourceOf(ccGraph: CcGraph): CcFlatNodeDataSource<CcFlatNode> {
  const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'ACC', 1);
  const dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, {} as any);
  dataSource.init();
  return dataSource;
}

function location(address: string): ValidationLocation {
  const path: ValidationPathSegment[] = address.split('>').map(part => {
    const dash = part.indexOf('-');
    return {type: part.substring(0, dash), manifestId: part.substring(dash + 1)};
  });
  return {label: address, path};
}

describe('findNodeByLocation', () => {
  it('finds a property under a group, and one under the base ACC, where the tree has them', () => {
    const dataSource = dataSourceOf(graph(false));

    const inGroup = findNodeByLocation(dataSource, location('ACC-1>ASCC-10>ASCCP-100>ACC-3>BCC-30>BCCP-300'));
    const inBase = findNodeByLocation(dataSource, location('ACC-1>ACC-2>BCC-20>BCCP-200'));

    expect(inGroup?.manifestId).toBe(300);
    expect(inGroup?.type).toBe('BCCP');
    expect(inBase?.manifestId).toBe(200);
    expect(inBase?.path).toBe('ACC-1>ACC-2>BCC-20>BCCP-200');
  });

  it('finds the root', () => {
    expect(findNodeByLocation(dataSourceOf(graph(false)), location('ACC-1'))?.manifestId).toBe(1);
  });

  it('gives the children of a user extension group to the ACC that has it', () => {
    const dataSource = dataSourceOf(graph(true));

    expect(findNodeByLocation(dataSource, location('ACC-1>BCC-30>BCCP-300'))?.manifestId).toBe(300);
    expect(findNodeByLocation(dataSource, location('ACC-1>ASCC-10>ASCCP-100>ACC-3>BCC-30>BCCP-300'))).toBeUndefined();
  });

  it('answers nothing for a node the tree no longer has', () => {
    const dataSource = dataSourceOf(graph(false));

    expect(findNodeByLocation(dataSource, location('ACC-1>ACC-2>BCC-99>BCCP-200'))).toBeUndefined();
    expect(findNodeByLocation(dataSource, location('ACC-7>BCC-20>BCCP-200'))).toBeUndefined();
  });

  it('lets the tree open the way to a node that is found', () => {
    const dataSource = dataSourceOf(graph(false));
    const node = findNodeByLocation(dataSource, location('ACC-1>ASCC-10>ASCCP-100>ACC-3>BCC-30>BCCP-300'))!;

    dataSource.expandTo(node.parent as CcFlatNode);

    expect(dataSource.data.some(n => n.path === node.path)).toBe(true);
  });
});
