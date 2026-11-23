/** A node as the search tells places apart: a node can stand for another one (a wrapper), and a place has a path. */
export interface IdentifiableNode {
  parent?: IdentifiableNode;
  /** The node this one stands for (a wrapped node), or the node itself. */
  self?: IdentifiableNode;
}

/**
 * What makes two nodes the same place: the node a wrapper stands for, never the wrapper (the parent of a wrapper is the
 * wrapped node's own parent). A node that wraps nothing is its own identity.
 */
export function identityOfNode(node: IdentifiableNode): object {
  let raw = node;
  let next = raw?.self;
  while (next && next !== raw) {
    raw = next;
    next = raw.self;
  }
  return raw;
}

/** The text a node keeps about where it stands; trees that have no such text leave it out. */
function pathOf(node: object | undefined, key: 'queryPath' | 'hashPath'): string | undefined {
  const path = (node as Record<string, unknown> | undefined)?.[key];
  return typeof path === 'string' && path ? path : undefined;
}

/** Whether two nodes are the same place: the same identity, or the same path (the tree may have made another node for it). */
export function isSameNode(a: IdentifiableNode, b: IdentifiableNode): boolean {
  if (a === b) {
    return true;
  }
  if (!a || !b) {
    return false;
  }
  if (identityOfNode(a) === identityOfNode(b)) {
    return true;
  }
  const path = pathOf(a, 'queryPath');
  return !!path && path === pathOf(b, 'queryPath');
}

/**
 * The row of the list a node is shown in, or -1. The node itself is looked for first, then the node it stands for, and
 * then a place by its path, which only counts when it names one row.
 */
export function findRowIndex<N extends IdentifiableNode>(rows: readonly N[], node: N): number {
  const direct = rows.indexOf(node);
  if (direct > -1) {
    return direct;
  }
  if (!node) {
    return -1;
  }
  const identity = identityOfNode(node);
  const wrapped = rows.findIndex(row => !!row && identityOfNode(row) === identity);
  if (wrapped > -1) {
    return wrapped;
  }
  for (const key of ['queryPath', 'hashPath'] as const) {
    const path = pathOf(node, key);
    if (path) {
      const found = rows.flatMap((row, index) => pathOf(row, key) === path ? [index] : []);
      if (found.length === 1) {
        return found[0];
      }
    }
  }
  return -1;
}
