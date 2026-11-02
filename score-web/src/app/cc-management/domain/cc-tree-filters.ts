import type {CcFlatNode} from './cc-flat-tree';
import {TreeNodeFilter} from '../../common/tree-viewport/tree-filter';

export const HIDE_PROHIBITED_FILTER = 'hideProhibited';

/** "Hide prohibited": a node that cannot occur (cardinality 0..0) is taken out. */
export class CcHideProhibitedFilter implements TreeNodeFilter<CcFlatNode> {

  readonly id = HIDE_PROHIBITED_FILTER;
  active = false;

  accepts(node: CcFlatNode): boolean {
    return !node.prohibited;
  }
}
