import {AbieFlatNode, AsbiepFlatNode, BbiepFlatNode, BbieScFlatNode, BieFlatNode} from '../../domain/bie-flat-tree';
import {CcGraphNode} from '../../../cc-management/domain/core-component-node';
import {BieUpliftSourceFlatNode} from './bie-uplift';

function cc(type: string, manifestId: number, componentType = 'Concrete'): CcGraphNode {
  return {type, manifestId, componentType} as CcGraphNode;
}

function association(parent: BieFlatNode, id: number, group = false): AsbiepFlatNode {
  const node = new AsbiepFlatNode();
  node.parent = parent;
  node.asccNode = cc('ASCC', id);
  node.asccpNode = cc('ASCCP', id + 1);
  node.accNode = cc('ACC', id + 2, group ? 'SemanticGroup' : 'Concrete');
  return node;
}

function property(parent: BieFlatNode): BbiepFlatNode {
  const node = new BbiepFlatNode();
  node.parent = parent;
  node.bccNode = cc('BCC', 30);
  node.bccpNode = cc('BCCP', 31);
  node.bdtNode = cc('DT', 32);
  return node;
}

function upliftPath(node: BieFlatNode): string {
  return new BieUpliftSourceFlatNode(node).upliftPath;
}

describe('BieUpliftSourceFlatNode group occurrence paths', () => {
  let root: AbieFlatNode;
  let group: AsbiepFlatNode;
  const groupPath = 'ASCCP-1>ACC-2>ASCC-10>ASCCP-11>ACC-12';

  beforeEach(() => {
    root = new AbieFlatNode();
    root.asccpNode = cc('ASCCP', 1);
    root.accNode = cc('ACC', 2);
    group = association(root, 10, true);
  });

  it('retains group ownership for a BBIE even though its display ABIE skips the group', () => {
    expect(group.isGroup).toBe(true);
    expect(group.abiePath).toBe(root.path);
    expect(upliftPath(property(group))).toBe(groupPath + '>BCC-30');
  });

  it('retains group ownership for an ASBIE and its descendants', () => {
    const child = association(group, 20);
    expect(upliftPath(child)).toBe(groupPath + '>ASCC-20');
    expect(upliftPath(property(child))).toBe(groupPath + '>ASCC-20>ASCCP-21>ACC-22>BCC-30');
  });

  it('retains every nested group', () => {
    const nested = association(group, 20, true);
    expect(upliftPath(property(nested))).toBe(groupPath + '>ASCC-20>ASCCP-21>ACC-22>BCC-30');
  });

  it('retains the owner group and inherited ACCs without repeating the boundary', () => {
    const child = property(group);
    child.intermediateAccNodes = [cc('ACC', 12), cc('ACC', 13), cc('ACC', 14)];
    expect(upliftPath(child)).toBe(groupPath + '>ACC-13>ACC-14>BCC-30');
  });

  it('retains groups in supplementary component paths', () => {
    const supplementary = new BbieScFlatNode();
    supplementary.parent = property(group);
    supplementary.bdtScNode = cc('DT_SC', 33);
    expect(upliftPath(supplementary)).toBe(groupPath + '>BCC-30>BCCP-31>DT-32>DT_SC-33');
  });

  it('retains a group below a reused occurrence without duplicating the occurrence prefix', () => {
    const reused = association(root, 20);
    reused.reused = true;
    group.parent = reused;
    expect(upliftPath(property(group))).toBe(
      'ASCCP-1>ACC-2>ASCC-20>ASCCP-21>ACC-22>ASCC-10>ASCCP-11>ACC-12>BCC-30');
  });

  it('retains a group above a reused occurrence', () => {
    const reused = association(group, 20);
    reused.reused = true;
    expect(upliftPath(property(reused))).toBe(groupPath + '>ASCC-20>ASCCP-21>ACC-22>BCC-30');
  });
});
