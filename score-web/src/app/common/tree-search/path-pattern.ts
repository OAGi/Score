import {AttrLookup, parseSearchExpression, SearchExpression, SearchExpressionError, SearchStep, splitCamelCase} from './search-expression';

/** The most path steps a pattern may have; the matcher state of a path is a bit mask with one bit per step. */
export const MAX_PATH_PATTERN_STEPS = 20;

export {splitCamelCase};

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
 * A step may carry predicates on the node's attributes (`Code[used = true]`, `*[contains(displayName, "id")]`), see
 * `parseSearchExpression`; a predicate judges the one node alone, which is what keeps the matching a walk down the
 * path (and so searchable on a DAG, where a record stands for every node showing the same component).
 *
 * The match state of a path is a bit mask: bit `j` is set when the last `j` nodes of the path match the first `j`
 * steps. Feeding the node names top-down through `step` therefore tells, at every node, whether the whole expression
 * matches there (`isMatch`) without looking back at the ancestors again.
 */
export class PathPattern {

  readonly rooted: boolean;
  /** The steps' names, camel-case split. */
  readonly tokens: string[];
  readonly steps: SearchStep[];
  /** Why the expression cannot be searched, when it is malformed. */
  readonly error?: SearchExpressionError;

  constructor(expr: string, readonly caseSensitive = false, readonly mode: PathPatternMode = {}) {
    const parsed: SearchExpression = parseSearchExpression(expr);
    this.rooted = parsed.rooted;
    this.steps = parsed.steps;
    this.error = parsed.error;
    this.tokens = parsed.steps.map(step => step.text);
  }

  /** Whether the mask based matching can represent this expression. */
  get supported(): boolean {
    return !this.error && this.tokens.length <= MAX_PATH_PATTERN_STEPS;
  }

  /** Whether any step needs the attributes of the nodes (and not just their names). */
  get hasPredicates(): boolean {
    return this.steps.some(step => step.predicates.length > 0);
  }

  /** The text a result list highlights: the last step's name, or else what its predicates look for. */
  get highlight(): string {
    const last = this.steps[this.steps.length - 1];
    return last ? (last.text || last.literals[0] || '') : '';
  }

  /** The mask before any node of the path has been seen (an anchored pattern may only start right here). */
  get initialMask(): number {
    return this.mode.anchored ? 1 : 0;
  }

  /** The mask after the path continues with a node that has one of the given names. */
  step(mask: number, ...names: string[]): number {
    return this.stepWith(mask, names);
  }

  /** Like `step`, for a node whose attributes the steps' predicates may ask about. */
  stepWith(mask: number, names: readonly string[], attributes?: AttrLookup): number {
    const matches = this.mode.exact ? nameEqualsToken : nameContainsToken;
    let next = 0;
    for (let j = 0; j < this.tokens.length; j++) {
      const reachable = j === 0 ? (!this.mode.anchored || (mask & 1) !== 0) : (mask & (1 << j)) !== 0;
      if (!reachable) {
        continue;
      }
      const step = this.steps[j];
      if (!(step.any || names.some(name => matches(name, this.tokens[j], this.caseSensitive)))) {
        continue;
      }
      if (step.predicates.length === 0 || (!!attributes && step.predicates.every(p => p(attributes, this.caseSensitive)))) {
        next |= 1 << (j + 1);
      }
    }
    return next;
  }

  /** Whether the node that produced the mask is where the whole expression matches. */
  isMatch(mask: number): boolean {
    return (mask & (1 << this.tokens.length)) !== 0;
  }

  /** The mask after a whole path of node names, root first (with the nodes' attributes, when predicates need them). */
  stepAll(names: string[][], attributes?: (AttrLookup | undefined)[]): number {
    return names.reduce((mask, alternatives, i) => this.stepWith(mask, alternatives, attributes?.[i]), this.initialMask);
  }
}
