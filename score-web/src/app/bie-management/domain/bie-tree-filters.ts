import type {BieFlatNode} from './bie-flat-tree';
import {TreeNodeFilter} from '../../common/tree-viewport/tree-filter';

export const HIDE_UNUSED_FILTER = 'hideUnused';

/** "Hide unused": only what the BIE uses or inherits stays, and what leads to it. */
export class BieHideUnusedFilter implements TreeNodeFilter<BieFlatNode> {

  readonly id = HIDE_UNUSED_FILTER;
  active = false;
  readonly keepAncestors = true;

  /**
   * A node inherited from the base BIE is used here too (the base's rows, with their `used` flag, are copied into the
   * derived BIE, and an inherited node cannot be switched off), as are the root and a reused node. So `inherited` adds
   * nothing for data that is in sync, and is kept as a safeguard.
   */
  accepts(node: BieFlatNode): boolean {
    return !!node.used || !!node.inherited;
  }
}
