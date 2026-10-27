import {locationAddress, ValidationLocation} from '../../validation/validation';
import {CcFlatNode, CcFlatNodeDataSource} from './cc-flat-tree';

/**
 * The node of the tree that a validation location is about, found by its address (ids, not names): every node knows the
 * address of its own, so the walk goes down from the root through the child whose address the location's starts with.
 * Nothing is opened or selected; undefined when the tree no longer has the node (it changed since the validation).
 */
export function findNodeByLocation(dataSource: CcFlatNodeDataSource<CcFlatNode>, location: ValidationLocation): CcFlatNode | undefined {
  const target = locationAddress(location);
  let current: CcFlatNode | undefined = dataSource.data[0];
  while (current && current.path !== target) {
    if (!target.startsWith(current.path + '>')) {
      return undefined;
    }
    dataSource.database.loadChildren(current);
    const parent: CcFlatNode = current;
    current = (parent.children as CcFlatNode[]).find(child => target === child.path || target.startsWith(child.path + '>'));
  }
  return current;
}
