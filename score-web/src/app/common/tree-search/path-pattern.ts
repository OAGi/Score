/** The most path steps a pattern may have; the matcher state of a path is a bit mask with one bit per step. */
export const MAX_PATH_PATTERN_STEPS = 20;

/** `ReleaseID` -> `Release ID`: the way a keyword is spelled out before it is compared with a name. */
export function splitCamelCase(token: string): string {
  return token.replace(/([a-z])([A-Z])/g, '$1 $2').trim();
}

/** Whether a node name contains the (already split) token, ignoring blanks in the token as well. */
export function nameContainsToken(name: string, token: string, caseSensitive: boolean): boolean {
  if (!name) {
    return false;
  }
  const tokenNoSpace = token.replace(/\s+/g, '');
  if (caseSensitive) {
    return name.indexOf(token) > -1 || name.indexOf(tokenNoSpace) > -1;
  }
  const nameLower = name.toLowerCase();
  return nameLower.indexOf(token.toLowerCase()) > -1 || nameLower.indexOf(tokenNoSpace.toLowerCase()) > -1;
}

/** Whether a node name is the token (exactly, apart from case unless asked for). */
export function nameEqualsToken(name: string, token: string, caseSensitive: boolean): boolean {
  if (!name) {
    return false;
  }
  return caseSensitive ? name === token : name.toLowerCase() === token.toLowerCase();
}

/** How strictly a pattern is read. */
export interface PathPatternMode {
  /** Steps must equal the node names instead of being contained in them. */
  exact?: boolean;
  /** The first step must match the root: the expression is a full path, not the end of one. */
  anchored?: boolean;
}

/**
 * An xpath-like search expression: `a/b/c` matches a node named `c` whose parent is named `b` and whose grandparent is
 * named `a` (structural nodes such as groups are not part of the chain). A leading `/` anchors the search at the root.
 *
 * The match state of a path is a bit mask: bit `j` is set when the last `j` nodes of the path match the first `j`
 * steps. Feeding the node names top-down through `step` therefore tells, at every node, whether the whole expression
 * matches there (`isMatch`) without looking back at the ancestors again.
 */
export class PathPattern {

  readonly rooted: boolean;
  /** The steps, camel-case split. */
  readonly tokens: string[];

  constructor(expr: string, readonly caseSensitive = false, readonly mode: PathPatternMode = {}) {
    this.rooted = expr.startsWith('/');
    this.tokens = (this.rooted ? expr.substring(1) : expr).split('/').map(splitCamelCase);
  }

  /** Whether the mask based matching can represent this expression. */
  get supported(): boolean {
    return this.tokens.length <= MAX_PATH_PATTERN_STEPS;
  }

  /** The mask before any node of the path has been seen (an anchored pattern may only start right here). */
  get initialMask(): number {
    return this.mode.anchored ? 1 : 0;
  }

  /** The mask after the path continues with a node that has one of the given names. */
  step(mask: number, ...names: string[]): number {
    const matches = this.mode.exact ? nameEqualsToken : nameContainsToken;
    let next = 0;
    for (let j = 0; j < this.tokens.length; j++) {
      const reachable = j === 0 ? (!this.mode.anchored || (mask & 1) !== 0) : (mask & (1 << j)) !== 0;
      if (reachable && names.some(name => matches(name, this.tokens[j], this.caseSensitive))) {
        next |= 1 << (j + 1);
      }
    }
    return next;
  }

  /** Whether the node that produced the mask is where the whole expression matches. */
  isMatch(mask: number): boolean {
    return (mask & (1 << this.tokens.length)) !== 0;
  }

  /** The mask after a whole path of node names, root first. */
  stepAll(names: string[][]): number {
    return names.reduce((mask, alternatives) => this.step(mask, ...alternatives), this.initialMask);
  }
}
