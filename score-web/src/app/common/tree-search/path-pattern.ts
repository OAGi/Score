import {AttrLookup, parseSearchExpression, SearchExpression, SearchExpressionError, ancestorTestHolds, splitCamelCase} from './search-expression';
import {AttrValue, CompiledStep, Env, NodeContext, SimpleNodeContext, stepAccepts} from './search-eval';

/** The most bits a matcher state may take (it is a number that is shifted and masked as a 32-bit integer). */
const MAX_STATE_BITS = 30;

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
}

/**
 * A search expression (see `parseSearchExpression`) as a matcher that is fed the nodes of a path top-down. A step
 * names the nodes it accepts and may judge them by their attributes and surroundings; it follows the node the step
 * before it accepted as a child (`/`) or at any depth below (`//`).
 *
 * The matcher state is a bit mask. Bit `j` is set at a node when the first `j` steps are matched by the path that ends
 * at it (so the whole expression matches where bit `steps.length` is set, `isMatch`). Above those bits, one bit per
 * step that reaches any depth remembers that the step before it was matched by an ancestor, and one bit per ancestor
 * test remembers that an ancestor passed it. Whatever the nodes below need to know of the ones above is in the mask,
 * which is what lets a search remember its answer per component instead of per place in the tree.
 */
export class PathPattern {

  readonly rooted: boolean;
  readonly anchored: boolean;
  readonly steps: CompiledStep[];
  /** The steps' names, camel-case split. */
  readonly tokens: string[];
  /** Why the expression cannot be searched, when it is malformed. */
  readonly error?: SearchExpressionError;
  /** Whether judging a node needs its surroundings and not just its names and attributes. */
  readonly needsContext: boolean;
  /** How many bits the matcher state takes. */
  readonly stateBits: number;

  private readonly expression: SearchExpression;
  private readonly env: Env;
  /** Per step, the bit that remembers an ancestor matched the step before it (-1: the step does not reach any depth). */
  private readonly stickyBit: number[];
  /** Per ancestor test, the bit that remembers an ancestor passed it. */
  private readonly ancestorBit: number[];
  private readonly simple = new SimpleNodeContext();

  constructor(expr: string, readonly caseSensitive = false, readonly mode: PathPatternMode = {}) {
    this.expression = parseSearchExpression(expr);
    this.rooted = this.expression.rooted;
    this.anchored = this.expression.anchored;
    this.steps = this.expression.steps;
    this.error = this.expression.error;
    this.needsContext = this.expression.needsContext;
    this.tokens = this.steps.map(step => step.tests.map(test => test.token).join(' '));
    this.env = {caseSensitive, exact: !!mode.exact};
    let bits = this.steps.length + 1;
    this.stickyBit = this.steps.map((step, j) => j > 0 && (step.axis === 'descendant' || step.axis === 'descendant-or-self') ? bits++ : -1);
    this.ancestorBit = this.expression.ancestorTests.map(() => bits++);
    this.stateBits = bits;
  }

  /** Whether the mask based matching can represent this expression. */
  get supported(): boolean {
    return !this.error && this.stateBits <= MAX_STATE_BITS;
  }

  /** Whether any step judges the nodes by more than their names. */
  get hasPredicates(): boolean {
    return this.steps.some(step => step.predicates.length > 0 || step.tests.length > 1);
  }

  /** The text a result list marks in the names: the last step's name. */
  get highlight(): string {
    const last = this.steps[this.steps.length - 1];
    return last?.tests.find(test => !test.any)?.token ?? '';
  }

  /** Whether the last step names nothing to mark, so that a result is marked as a whole. */
  get marksWholeNode(): boolean {
    return !this.highlight;
  }

  /** The mask before any node of the path has been seen (an anchored pattern may only start right here). */
  get initialMask(): number {
    return this.anchored ? 1 : 0;
  }

  /** The mask after the path continues with a node that has one of the given names. */
  step(mask: number, ...names: string[]): number {
    return this.stepWith(mask, names);
  }

  /** Like `step`, for a node whose attributes the steps' predicates may ask about. */
  stepWith(mask: number, names: readonly string[], attributes?: AttrLookup): number {
    this.simple.names = names;
    this.simple.lookup = attributes ?? (() => undefined);
    return this.stepContext(mask, this.simple);
  }

  /** The mask after the path continues with the node (which tells everything about itself and its surroundings). */
  stepContext(mask: number, ctx: NodeContext): number {
    const steps = this.steps;
    let next = 0;
    for (let j = 0; j < steps.length; j++) {
      const step = steps[j];
      let reachable: boolean;
      if (j === 0) {
        reachable = !this.anchored || (mask & 1) !== 0;
      } else if (step.axis === 'child') {
        reachable = (mask & (1 << j)) !== 0;
      } else {
        reachable = (mask & (1 << this.stickyBit[j])) !== 0 || step.axis === 'descendant-or-self' && (next & (1 << j)) !== 0;
      }
      if (reachable && stepAccepts(step, ctx, this.env)) {
        next |= 1 << (j + 1);
      }
    }
    for (let j = 1; j < steps.length; j++) {
      const bit = this.stickyBit[j];
      if (bit >= 0 && ((mask & (1 << bit)) !== 0 || (next & (1 << j)) !== 0)) {
        next |= 1 << bit;
      }
    }
    this.expression.ancestorTests.forEach((test, k) => {
      const bit = this.ancestorBit[k];
      if ((mask & (1 << bit)) !== 0 || ancestorTestHolds(test, ctx, this.env)) {
        next |= 1 << bit;
      }
    });
    return next;
  }

  /** Whether an ancestor passed the ancestor test `k`, according to the mask of the node below it. */
  ancestorFlag(mask: number, k: number): boolean {
    return (mask & (1 << this.ancestorBit[k])) !== 0;
  }

  /** Whether the node that produced the mask is where the whole expression matches. */
  isMatch(mask: number): boolean {
    return (mask & (1 << this.steps.length)) !== 0;
  }

  /** The mask after a whole path of node names, root first (with the nodes' attributes, when predicates need them). */
  stepAll(names: string[][], attributes?: (AttrLookup | undefined)[]): number {
    return names.reduce((mask, alternatives, i) => this.stepWith(mask, alternatives, attributes?.[i]), this.initialMask);
  }

  /** The mask after a whole path of nodes, root first. */
  stepAllContexts(contexts: NodeContext[]): number {
    return contexts.reduce((mask, ctx) => this.stepContext(mask, ctx), this.initialMask);
  }
}

export type {AttrValue};
