/** What a node tells a search predicate about itself: a string, a number, a flag, or nothing at all. */
export type AttrValue = string | number | boolean | undefined;

/** Reads the attribute of the node a predicate is judging. */
export type AttrLookup = (attribute: string) => AttrValue;

/** The attribute a node keeps its text under: what `text()` answers, and what a plain keyword search matches. */
export const TEXT_ATTRIBUTE = '$text';

export type Axis = 'child' | 'descendant' | 'descendant-or-self' | 'self' | 'parent' | 'ancestor' | 'ancestor-or-self'
  | 'following-sibling' | 'preceding-sibling';

/** The axes that only lead into the node's own subtree (or stay on the node). */
export const DOWNWARD_AXES: readonly Axis[] = ['child', 'descendant', 'descendant-or-self', 'self'];

/**
 * A node as a search expression sees it: its names and attributes, and its surroundings (the tree it belongs to is
 * only reached through these). A context knows its parent but not the parent's own surroundings, and it never knows
 * its ancestors beyond that: what an `ancestor::` test needs from them is carried down as flags (`ancestorFlag`).
 */
export interface NodeContext {
  readonly names: readonly string[];

  attr(attribute: string): AttrValue;

  /** The children as the tree shows them (none for a node whose component is already among its ancestors). */
  children(): NodeContext[];

  /** The parent, which can be asked about its attributes and its subtree only (not about its own parent or siblings). */
  parent(): NodeContext | undefined;

  /** The nodes that share the parent, this one included, and this one's place among them. */
  siblings(): {list: NodeContext[], index: number};

  /** Whether a strict ancestor satisfies the expression's ancestor test `k`. */
  ancestorFlag(k: number): boolean;

  /** False once the search has spent its budget (what is asked after that is answered as "nothing there"). */
  spend(): boolean;

  /** What an earlier evaluation of a test that depends on this node and its subtree alone found, if it is remembered. */
  recall(test: number): boolean | undefined;

  remember(test: number, found: boolean): void;
}

/** How names are matched and what case means; fixed for a whole search. */
export interface Env {
  caseSensitive: boolean;
  /** Names must equal the step's name instead of containing it. */
  exact: boolean;
}

/** The node a predicate judges and where it stands among the nodes the step selected (`position()`, `last()`). */
export interface Frame {
  ctx: NodeContext;
  position: number;
  size: number;
}

export type CompiledPredicate = (frame: Frame, env: Env) => boolean;

/** A name a step looks for; all of a step's tests must hold (a rewritten step can carry more than one). */
export interface NameTest {
  /** The name as written, camel-case split; empty for `*`. */
  token: string;
  /** The name as written, for a node whose own name has camel case in it (`newDerivedDT_ Code. Type`). */
  raw?: string;
  any: boolean;
}

export interface CompiledStep {
  axis: Axis;
  /**
   * The step was written with `//` (or begins a relative path): it reaches any depth below the previous node, and
   * `[n]` counts among the node's siblings, as in XPath's `//name[n]`.
   */
  anywhere: boolean;
  tests: NameTest[];
  predicates: CompiledPredicate[];
  /** Whether a predicate asks for the position or the size of the selection (so it cannot judge a node alone). */
  positional: boolean;
  /** Whether the predicates need no more than the node's own names and attributes. */
  local: boolean;
  /** Whether the predicates only look at the node and its subtree. */
  pure: boolean;
  /** Whether a predicate asks about the node's ancestors (answered from flags that the search carries down). */
  ancestral: boolean;
  /** The literal strings the predicates look for: what a result list highlights when the step has no name. */
  literals: string[];
}

export interface CompiledPath {
  steps: CompiledStep[];
  /** Whether the answer depends only on the node and its subtree, so that it can be remembered per component. */
  pure: boolean;
  /** Identifies the path for what is remembered about it. */
  id: number;
}

/** The part of a name test that cares about case and splitting: does the name hold the token? */
function holds(name: string, token: string, env: Env): boolean {
  if (!name) {
    return false;
  }
  const tokenNoSpace = token.replace(/\s+/g, '');
  if (env.exact) {
    return env.caseSensitive ? name === token : name.toLowerCase() === token.toLowerCase();
  }
  if (env.caseSensitive) {
    return name.indexOf(token) > -1 || name.indexOf(tokenNoSpace) > -1;
  }
  const lower = name.toLowerCase();
  return lower.indexOf(token.toLowerCase()) > -1 || lower.indexOf(tokenNoSpace.toLowerCase()) > -1;
}

export function passesTests(names: readonly string[], tests: NameTest[], env: Env): boolean {
  return tests.every(test => test.any || names.some(name => holds(name, test.token, env)
    || test.raw !== undefined && test.raw !== test.token && holds(name, test.raw, env)));
}

/** The nodes an axis leads to from a node, nearest first for the reversed axes. */
function* axisNodes(axis: Axis, from: NodeContext): Generator<NodeContext> {
  switch (axis) {
    case 'child':
      yield* from.children();
      return;
    case 'descendant':
    case 'descendant-or-self':
      if (axis === 'descendant-or-self') {
        yield from;
      }
      for (const child of from.children()) {
        if (!from.spend()) {
          return;
        }
        yield* axisNodes('descendant-or-self', child);
      }
      return;
    case 'self':
      yield from;
      return;
    case 'parent': {
      const parent = from.parent();
      if (parent) {
        yield parent;
      }
      return;
    }
    case 'following-sibling': {
      const {list, index} = from.siblings();
      yield* list.slice(index + 1);
      return;
    }
    case 'preceding-sibling': {
      const {list, index} = from.siblings();
      yield* list.slice(0, index).reverse();
      return;
    }
    default:
      throw new Error('The ' + axis + ' axis cannot be walked here.');
  }
}

/** Judges one node of a selection with the step's predicates, as the node `position` of `size`. */
function passes(step: CompiledStep, ctx: NodeContext, position: number, size: number, env: Env): boolean {
  return step.predicates.every(predicate => predicate({ctx, position, size}, env));
}

/** The nodes of the selection that survive the step's predicates one after the other (each counts the survivors so far). */
function filterPositional(step: CompiledStep, nodes: NodeContext[], env: Env): NodeContext[] {
  let selection = nodes.filter(node => passesTests(node.names, step.tests, env));
  for (const predicate of step.predicates) {
    const size = selection.length;
    selection = selection.filter((node, i) => predicate({ctx: node, position: i + 1, size}, env));
  }
  return selection;
}

/** The nodes a step selects from a node, in axis order. */
function* selectStep(step: CompiledStep, from: NodeContext, env: Env): Generator<NodeContext> {
  if (step.positional) {
    yield* filterPositional(step, [...axisNodes(step.axis, from)], env);
    return;
  }
  for (const node of axisNodes(step.axis, from)) {
    if (passesTests(node.names, step.tests, env) && passes(step, node, 1, 1, env)) {
      yield node;
    }
  }
}

/** Walks the steps from a node; stops as soon as `found` says so. */
function walk(steps: CompiledStep[], at: number, from: NodeContext, env: Env, found: (node: NodeContext) => boolean): boolean {
  for (const node of selectStep(steps[at], from, env)) {
    if (at === steps.length - 1 ? found(node) : walk(steps, at + 1, node, env, found)) {
      return true;
    }
  }
  return false;
}

/** Whether a node passes a step that judges nodes one by one (no position asked). */
function accepts(step: CompiledStep, ctx: NodeContext, env: Env): boolean {
  return passesTests(ctx.names, step.tests, env) && passes(step, ctx, 1, 1, env);
}

/** The key under which what is found below a node for a step of a path is remembered. */
function below(path: CompiledPath, at: number): number {
  return path.id * 128 + at * 2 + 1;
}

/**
 * Whether some node strictly below `from` is selected by the steps from `at` on, the step at `at` reaching any depth.
 * What a component has below it is the same wherever it stands, so it is worked out once per component.
 */
function existsBelow(path: CompiledPath, at: number, from: NodeContext, env: Env): boolean {
  const key = below(path, at);
  const known = from.recall(key);
  if (known !== undefined) {
    return known;
  }
  const step = path.steps[at];
  const last = at === path.steps.length - 1;
  let found = false;
  for (const child of from.children()) {
    if (!from.spend()) {
      return false;
    }
    if (accepts(step, child, env) && (last || reach(path, at + 1, child, env)) || existsBelow(path, at, child, env)) {
      found = true;
      break;
    }
  }
  from.remember(key, found);
  return found;
}

/** Whether the steps from `at` on select anything from the node. */
function reach(path: CompiledPath, at: number, from: NodeContext, env: Env): boolean {
  const step = path.steps[at];
  if (path.pure && !step.positional && (step.axis === 'descendant' || step.axis === 'descendant-or-self')) {
    const last = at === path.steps.length - 1;
    return step.axis === 'descendant-or-self' && accepts(step, from, env) && (last || reach(path, at + 1, from, env))
      || existsBelow(path, at, from, env);
  }
  for (const node of selectStep(step, from, env)) {
    if (at === path.steps.length - 1 || reach(path, at + 1, node, env)) {
      return true;
    }
  }
  return false;
}

/** Whether the path selects anything from the node. */
export function pathExists(path: CompiledPath, from: NodeContext, env: Env): boolean {
  const key = path.id * 128 + 127;
  const known = path.pure ? from.recall(key) : undefined;
  if (known !== undefined) {
    return known;
  }
  const found = reach(path, 0, from, env);
  if (path.pure && (found || from.spend())) {
    from.remember(key, found);
  }
  return found;
}

/** How many nodes the path selects from the node. */
export function pathCount(path: CompiledPath, from: NodeContext, env: Env): number {
  let count = 0;
  walk(path.steps, 0, from, env, () => {
    count++;
    return false;
  });
  return count;
}

/**
 * Whether the node is one of those the step selects from its context node. The node is judged among its siblings
 * (what `[n]` counts), which is the same for every context node a step can come from (the parent, or any ancestor).
 */
export function stepAccepts(step: CompiledStep, ctx: NodeContext, env: Env): boolean {
  if (!passesTests(ctx.names, step.tests, env)) {
    return false;
  }
  if (!step.positional) {
    return passes(step, ctx, 1, 1, env);
  }
  const {list, index} = ctx.siblings();
  return filterPositional(step, list, env).includes(list[index]);
}

/** A node context over a plain attribute lookup: all a predicate needs when it only asks about one node. */
export class SimpleNodeContext implements NodeContext {

  names: readonly string[] = [];
  lookup: AttrLookup = () => undefined;

  attr(attribute: string): AttrValue {
    return this.lookup(attribute);
  }

  children(): NodeContext[] {
    throw new Error('This search expression needs the surroundings of the nodes.');
  }

  parent(): NodeContext | undefined {
    return this.children()[0];
  }

  siblings(): {list: NodeContext[], index: number} {
    return {list: this.children(), index: 0};
  }

  ancestorFlag(): boolean {
    return false;
  }

  spend(): boolean {
    return true;
  }

  recall(): boolean | undefined {
    return undefined;
  }

  remember() {
  }
}
