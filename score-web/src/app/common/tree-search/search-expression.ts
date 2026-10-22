import {
  TEXT_ATTRIBUTE, AttrValue, Axis, CompiledPath, CompiledPredicate, CompiledStep, DOWNWARD_AXES, Env, Frame, NameTest, NodeContext, passesTests, pathCount,
  pathExists
} from './search-eval';

export type {AttrLookup, AttrValue} from './search-eval';

/** A malformed search expression: what is wrong and where (an offset into the expression). */
export interface SearchExpressionError {
  message: string;
  position: number;
}

/** A condition on the ancestors of a node (`ancestor::Name[...]/rest`), judged at each ancestor as the search passes it. */
export interface AncestorTest {
  step: CompiledStep;
  /** What must be selected from the ancestor too, when the test goes on below it. */
  rest?: CompiledPath;
  orSelf: boolean;
}

export interface SearchExpression {
  /** Whether the expression starts with `/`: it is searched from the root of the tree. */
  rooted: boolean;
  /** Whether the first step must be the root of the tree itself (`/name`, as opposed to `//name` or `name`). */
  anchored: boolean;
  /** The steps, each coming after the node the one before selected; only axes that lead downward. */
  steps: CompiledStep[];
  ancestorTests: AncestorTest[];
  /** Whether judging a node needs more than its names and attributes. */
  needsContext: boolean;
  error?: SearchExpressionError;
}

/** The functions a predicate may call (the text ones take two arguments). */
export const SEARCH_FUNCTIONS = ['contains', 'starts-with', 'ends-with'];

/** The axes a step or a predicate path may name. */
export const SEARCH_AXES: Axis[] = ['child', 'descendant', 'descendant-or-self', 'self', 'parent', 'ancestor', 'ancestor-or-self',
  'following-sibling', 'preceding-sibling'];

type Node = (frame: Frame, env: Env) => AttrValue;

interface Token {
  kind: 'ident' | 'string' | 'number' | 'op' | 'punct';
  text: string;
  position: number;
}

class ParseError extends Error {
  constructor(message: string, readonly position: number) {
    super(message);
  }
}

/** `ReleaseID` -> `Release ID`: the way a keyword is spelled out before it is compared with a name. */
export function splitCamelCase(token: string): string {
  return token.replace(/([a-z])([A-Z])/g, '$1 $2').trim();
}

/** What the parse of one expression collects along the way. */
class CompileState {
  readonly ancestorTests: AncestorTest[] = [];
  private nextPath = 1;
  /** How many paths deep the predicate being parsed is. */
  depth = 0;
  /** Whether the part being parsed is evaluated beyond the reach of the flags (below a parent or ancestor step). */
  restricted = false;
  /** Set by what the predicate being parsed does; read and reset by it. */
  positional = false;
  impure = false;
  ancestral = false;
  local = true;

  pathId(): number {
    return this.nextPath++;
  }
}

/**
 * Parses a search expression. It is an XPath-like location path: `[/ or //]step(/step or //step)*`, where a step is
 * `[axis::]name` (a name, or `*`; `..` is `parent::*` and `.` is `self::*`) followed by any number of `[predicate]`.
 *
 * - `/` goes to a child and `//` to a descendant at any depth; a leading `/` starts at the root of the tree
 *   (`/Root/Child`), a leading `//` anywhere in it, and no leading slash anywhere below the selected node.
 * - A predicate combines comparisons (`used = true`, `type = ACC`, `cardinalityMax > 1`), functions (`contains(name,
 *   "id")`, `position()`, `last()`, `count(path)`), paths that must select something (`child::Code`, `../Code`,
 *   `following-sibling::*[type = BBIEP]`, `ancestor::Sender`) and a number (`[2]` is `[position() = 2]`) with `and`,
 *   `or`, `not` and parentheses; a bare attribute (`[used]`) tests that it is true. What a comparison is made against
 *   may be a bare word: `type = ACC`.
 * - The axes that go back in the path itself are rewritten into predicates: `Sender/..` is `*[child::Sender]`.
 *
 * An expression without `[`, `::`, `//` or `..` is read the way it always was: its steps are split on `/` and are
 * names only. An error does not throw: the expression comes back with `error` set and no steps.
 */
export function parseSearchExpression(expr: string): SearchExpression {
  const rooted = expr.startsWith('/');
  const body = rooted ? expr.substring(1) : expr;
  const offset = rooted ? 1 : 0;
  if (isPlain(expr, body)) {
    const steps = body.split('/').map(plainStep);
    steps[0].anywhere = !rooted;
    return {rooted, anchored: rooted, steps, ancestorTests: [], needsContext: false};
  }
  const state = new CompileState();
  try {
    const raw = splitSteps(body, offset).reduce(readSteps(state), {steps: [] as RawStep[], pending: '' as '' | '//'}).steps;
    const {steps, anchored} = assemble(raw, rooted, state);
    return {
      rooted, anchored, steps, ancestorTests: state.ancestorTests,
      needsContext: state.ancestorTests.length > 0 || steps.some(s => !s.local)
    };
  } catch (e) {
    if (e instanceof ParseError) {
      return {rooted, anchored: false, steps: [], ancestorTests: [], needsContext: false, error: {message: e.message, position: e.position}};
    }
    throw e;
  }
}

function isPlain(expr: string, body: string): boolean {
  return body.indexOf('[') < 0 && body.indexOf('::') < 0 && expr.indexOf('//') < 0 && !/(^|\/)\.\.?(\/|$)/.test(body);
}

function plainStep(text: string): CompiledStep {
  return step('child', false, [nameTest(text)]);
}

function nameTest(name: string): NameTest {
  const trimmed = name.trim().replace(/^(["'])(.*)\1$/, '$2');
  return {token: splitCamelCase(trimmed), any: trimmed === '*' || trimmed === ''};
}

function step(axis: Axis, anywhere: boolean, tests: NameTest[], predicates: CompiledPredicate[] = [], flags: Partial<CompiledStep> = {}): CompiledStep {
  return {axis, anywhere, tests, predicates, positional: false, pure: true, ancestral: false, local: true, literals: [], ...flags};
}

// ---- reading the steps of the path -----------------------------------------------------------------------------------

interface RawStep {
  /** `//` when the step was written after one. */
  sep: '/' | '//';
  axis?: Axis;
  test: NameTest;
  predicates: CompiledPredicate[];
  flags: Partial<CompiledStep>;
  position: number;
}

/** Cuts the expression at the `/` that are outside brackets and quotes (`//` leaves an empty piece between). */
function splitSteps(body: string, offset: number): {text: string, position: number}[] {
  const steps: {text: string, position: number}[] = [];
  let depth = 0;
  let quote = '';
  let start = 0;
  for (let i = 0; i < body.length; i++) {
    const c = body[i];
    if (quote) {
      if (c === quote) {
        quote = '';
      }
    } else if (depth > 0 && (c === '"' || c === '\'')) {
      quote = c;
    } else if (c === '[') {
      depth++;
    } else if (c === ']') {
      if (--depth < 0) {
        throw new ParseError('Unexpected ]', offset + i);
      }
    } else if (c === '/' && depth === 0) {
      steps.push({text: body.substring(start, i), position: offset + start});
      start = i + 1;
    }
  }
  if (quote) {
    throw new ParseError('Unterminated string', offset + body.length);
  }
  if (depth > 0) {
    throw new ParseError('Missing ]', offset + body.length);
  }
  steps.push({text: body.substring(start), position: offset + start});
  return steps;
}

/** Turns the pieces of the expression into raw steps, an empty piece being the `//` of the step after it. */
function readSteps(state: CompileState) {
  return (acc: {steps: RawStep[], pending: '' | '//'}, piece: {text: string, position: number}, i: number, all: unknown[]) => {
    const last = i === all.length - 1;
    if (piece.text.trim() === '' && !last) {
      if (acc.pending) {
        throw new ParseError('Unexpected /', piece.position);
      }
      return {steps: acc.steps, pending: '//' as const};
    }
    acc.steps.push(readStep(piece.text, piece.position, acc.pending ? '//' : '/', state));
    return {steps: acc.steps, pending: '' as const};
  };
}

function readStep(text: string, position: number, sep: '/' | '//', state: CompileState): RawStep {
  const open = text.indexOf('[');
  const head = (open < 0 ? text : text.substring(0, open)).trim();
  let axis: Axis | undefined;
  let name = head;
  const axisMatch = /^([A-Za-z-]+)\s*::(.*)$/.exec(head);
  if (axisMatch) {
    axis = SEARCH_AXES.find(a => a === axisMatch[1].toLowerCase());
    if (!axis) {
      throw new ParseError('Unknown axis ' + axisMatch[1], position);
    }
    name = axisMatch[2];
  } else if (head === '..') {
    axis = 'parent';
    name = '*';
  } else if (head === '.') {
    axis = 'self';
    name = '*';
  }
  const raw: RawStep = {sep, axis, test: nameTest(name), predicates: [], flags: stepFlags(), position};
  let at = open < 0 ? text.length : open;
  while (at < text.length) {
    const close = closingBracket(text, at);
    const inner = text.substring(at + 1, close);
    const compiled = new PredicateParser(tokenize(inner, position + at + 1), position + close, state).parse();
    raw.predicates.push(compiled.predicate);
    mergeFlags(raw.flags, compiled.flags);
    at = close + 1;
    if (at < text.length && text[at] !== '[') {
      throw new ParseError('Expected [ or / after ]', position + at);
    }
  }
  return raw;
}

function stepFlags(): Partial<CompiledStep> {
  return {positional: false, pure: true, ancestral: false, local: true, literals: []};
}

function mergeFlags(into: Partial<CompiledStep>, from: Partial<CompiledStep>) {
  into.positional = !!into.positional || !!from.positional;
  into.pure = into.pure !== false && from.pure !== false;
  into.ancestral = !!into.ancestral || !!from.ancestral;
  into.local = into.local !== false && from.local !== false;
  into.literals = [...(into.literals ?? []), ...(from.literals ?? [])];
}

/** The `]` that closes the `[` at `open`, with nested brackets and quoted text skipped. */
function closingBracket(text: string, open: number): number {
  let quote = '';
  let depth = 0;
  for (let i = open; i < text.length; i++) {
    const c = text[i];
    if (quote) {
      if (c === quote) {
        quote = '';
      }
    } else if (c === '"' || c === '\'') {
      quote = c;
    } else if (c === '[') {
      depth++;
    } else if (c === ']' && --depth === 0) {
      return i;
    }
  }
  throw new ParseError('Missing ]', text.length);
}

// ---- turning raw steps into the steps the search runs ----------------------------------------------------------------

function toStep(raw: RawStep, axis: Axis, anywhere: boolean): CompiledStep {
  return step(axis, anywhere, [raw.test], raw.predicates, raw.flags);
}

function assemble(raw: RawStep[], rooted: boolean, state: CompileState): {steps: CompiledStep[], anchored: boolean} {
  const out: CompiledStep[] = [];
  let anchored = false;
  raw.forEach((r, i) => {
    if (i === 0) {
      if (r.axis !== undefined && !['child', 'descendant', 'descendant-or-self', 'self'].includes(r.axis)) {
        throw new ParseError('A path cannot start with the ' + r.axis + ' axis', r.position);
      }
      anchored = rooted && r.sep === '/' && (r.axis === undefined || r.axis === 'child');
      out.push(toStep(r, 'child', !anchored));
      return;
    }
    const axis = r.axis ?? 'child';
    if (r.sep === '//') {
      if (axis !== 'child' && axis !== 'descendant') {
        throw new ParseError('// must be followed by a name', r.position);
      }
      out.push(toStep(r, 'descendant', true));
    } else if (axis === 'child' || axis === 'descendant' || axis === 'descendant-or-self') {
      if (axis !== 'child' && r.flags.positional) {
        throw new ParseError('A position cannot be asked on the ' + axis + ' axis', r.position);
      }
      out.push(toStep(r, axis, false));
    } else if (axis === 'self') {
      mergeSelf(out, r);
    } else {
      rewriteReverse(out, r, axis, anchored && out.length === 1, state);
    }
  });
  if (out.length === 0) {
    throw new ParseError('Empty path', 0);
  }
  if (!anchored) {
    out[0].anywhere = true;
  }
  return {steps: out, anchored};
}

/** `step/self::name[...]` is the step that is also that name and passes those predicates. */
function mergeSelf(out: CompiledStep[], r: RawStep) {
  const last = out[out.length - 1];
  if (r.flags.positional || last.positional && !r.test.any) {
    throw new ParseError('self:: cannot ask for a position here', r.position);
  }
  if (!r.test.any) {
    last.tests.push(r.test);
  }
  last.predicates.push(...r.predicates);
  last.pure = last.pure && r.flags.pure !== false;
  last.ancestral = last.ancestral || !!r.flags.ancestral;
  last.local = last.local && r.flags.local !== false;
  last.literals.push(...(r.flags.literals ?? []));
}

/** A path predicate: the node must select something by the steps. */
function pathPredicate(steps: CompiledStep[], state: CompileState): CompiledPredicate {
  const path = pathOf(steps, state);
  return (frame, env) => pathExists(path, frame.ctx, env);
}

function pathOf(steps: CompiledStep[], state: CompileState): CompiledPath {
  return {steps, pure: steps.every(s => s.pure && DOWNWARD_AXES.includes(s.axis)), id: state.pathId()};
}

/**
 * Rewrites a step that goes back (up or sideways) into what the walk down the tree can answer: the node it selects is
 * the one the step before it came from, so that step becomes a condition on that node.
 *   `P/S/..`                   -> `P[child::S]`
 *   `P//S/..`                  -> `P/descendant-or-self::*[child::S]`
 *   `S/ancestor::T`            -> `//T[descendant::S]`
 *   `P/S/following-sibling::T` -> `P/T[preceding-sibling::S]`
 */
function rewriteReverse(out: CompiledStep[], r: RawStep, axis: Axis, firstAnchored: boolean, state: CompileState) {
  const s = out.pop();
  if (!s) {
    throw new ParseError('A path cannot start with the ' + axis + ' axis', r.position);
  }
  if (r.flags.positional) {
    throw new ParseError('A position cannot be asked on the ' + axis + ' axis in a path', r.position);
  }
  if (s.ancestral) {
    throw new ParseError('A condition on the ancestors cannot be moved into another condition', r.position);
  }
  const wasFirst = out.length === 0;
  const asStep = (axisOfS: Axis): CompiledStep => ({...s, axis: axisOfS, anywhere: false});
  /** The node the reverse step reaches: that name and those predicates, and the condition that stands for the step before. */
  const reached = (base: CompiledStep, condition: CompiledPredicate): CompiledStep => ({
    ...base,
    tests: [...base.tests, ...(r.test.any ? [] : [r.test])],
    predicates: [...base.predicates, ...r.predicates, condition],
    pure: base.pure && r.flags.pure !== false && s.pure,
    ancestral: base.ancestral || !!r.flags.ancestral,
    local: false,
    literals: [...base.literals, ...(r.flags.literals ?? [])]
  });
  const anywhere = (condition: CompiledPredicate) => reached(step('descendant', true, []), condition);

  switch (axis) {
    case 'parent': {
      if (wasFirst && firstAnchored) {
        throw new ParseError('The root has no parent', r.position);
      }
      if (s.axis === 'descendant-or-self') {
        throw new ParseError('parent:: cannot follow this step', r.position);
      }
      const condition = pathPredicate([asStep('child')], state);
      if (wasFirst) {
        out.push(anywhere(condition));
      } else if (s.axis === 'child') {
        const previous = out[out.length - 1];
        if (previous.positional && !r.test.any) {
          throw new ParseError('parent:: cannot narrow a step that asks for a position', r.position);
        }
        out[out.length - 1] = reached(previous, condition);
      } else {
        out.push(reached(step('descendant-or-self', false, []), condition));
      }
      return;
    }
    case 'ancestor':
    case 'ancestor-or-self': {
      if (!wasFirst || firstAnchored) {
        throw new ParseError(axis + ':: can follow the first step of a path only; ask for it in a predicate: [' + axis + '::Name]', r.position);
      }
      if (s.positional) {
        throw new ParseError('A position cannot be asked on the step before ' + axis + '::', r.position);
      }
      out.push(anywhere(pathPredicate([asStep(axis === 'ancestor' ? 'descendant' : 'descendant-or-self')], state)));
      return;
    }
    default: {
      if (wasFirst && firstAnchored) {
        throw new ParseError('The root has no siblings', r.position);
      }
      if (s.positional || s.axis === 'descendant-or-self') {
        throw new ParseError(axis + ':: cannot follow this step', r.position);
      }
      const opposite: Axis = axis === 'following-sibling' ? 'preceding-sibling' : 'following-sibling';
      const condition = pathPredicate([asStep(opposite)], state);
      out.push(reached(step(s.axis, s.anywhere, []), condition));
    }
  }
}

// ---- predicates ---------------------------------------------------------------------------------------------------

function tokenize(text: string, offset: number): Token[] {
  const tokens: Token[] = [];
  let i = 0;
  while (i < text.length) {
    const c = text[i];
    if (/\s/.test(c)) {
      i++;
    } else if (c === '"' || c === '\'') {
      const end = text.indexOf(c, i + 1);
      if (end < 0) {
        throw new ParseError('Unterminated string', offset + i);
      }
      tokens.push({kind: 'string', text: text.substring(i + 1, end), position: offset + i});
      i = end + 1;
    } else if (/[0-9]/.test(c) || (c === '-' && /[0-9]/.test(text[i + 1] || ''))) {
      const m = /^-?[0-9]+(\.[0-9]+)?/.exec(text.substring(i));
      tokens.push({kind: 'number', text: m[0], position: offset + i});
      i += m[0].length;
    } else if (/[A-Za-z_]/.test(c)) {
      const m = /^[A-Za-z_][A-Za-z0-9_-]*/.exec(text.substring(i));
      tokens.push({kind: 'ident', text: m[0], position: offset + i});
      i += m[0].length;
    } else if (c === '∞') {
      tokens.push({kind: 'ident', text: c, position: offset + i});
      i++;
    } else if (c === '!' || c === '<' || c === '>' || c === '=') {
      const two = text.substr(i, 2);
      const op = two === '!=' || two === '<=' || two === '>=' ? two : c;
      if (op === '!') {
        throw new ParseError('Unexpected !', offset + i);
      }
      tokens.push({kind: 'op', text: op, position: offset + i});
      i += op.length;
    } else if (text.startsWith('::', i) || text.startsWith('//', i) || text.startsWith('..', i)) {
      tokens.push({kind: 'punct', text: text.substr(i, 2), position: offset + i});
      i += 2;
    } else if ('(),[]/.*'.includes(c)) {
      tokens.push({kind: 'punct', text: c, position: offset + i});
      i++;
    } else {
      throw new ParseError('Unexpected ' + c, offset + i);
    }
  }
  return tokens;
}

interface Compiled {
  predicate: CompiledPredicate;
  flags: Partial<CompiledStep>;
}

/** Recursive descent over `or > and > not > comparison > operand`; every node compiles to a closure. */
class PredicateParser {

  private at = 0;
  /** The operands that are paths: they can be tested or counted, but not compared. */
  private readonly paths = new WeakSet<Node>();
  /** The nodes whose value is a count, which a predicate takes for a position. */
  private readonly numbers = new WeakSet<Node>();
  /** The literal strings the predicate looks for. */
  private readonly literals: string[] = [];

  constructor(private readonly tokens: Token[], private readonly end: number, private readonly state: CompileState) {
  }

  parse(): Compiled {
    if (this.tokens.length === 0) {
      throw new ParseError('Empty predicate', this.end);
    }
    const before = {...this.state};
    this.state.positional = false;
    this.state.impure = false;
    this.state.ancestral = false;
    this.state.local = true;
    let predicate: CompiledPredicate;
    if (this.tokens.length === 1 && this.tokens[0].kind === 'number') {
      const wanted = Number(this.tokens[0].text);
      this.state.positional = true;
      this.state.local = false;
      predicate = frame => frame.position === wanted;
    } else {
      const node = this.or();
      if (this.at < this.tokens.length) {
        throw new ParseError('Unexpected ' + this.tokens[this.at].text, this.tokens[this.at].position);
      }
      // a number is a position (`[last()]`), anything else is true or false
      predicate = this.numbers.has(node) ?
        (frame, env) => node(frame, env) === frame.position : (frame, env) => truthy(node(frame, env));
    }
    const flags: Partial<CompiledStep> = {
      positional: this.state.positional, pure: !this.state.impure, ancestral: this.state.ancestral, local: this.state.local,
      literals: this.literals
    };
    this.state.positional = before.positional;
    this.state.impure = before.impure || this.state.impure;
    this.state.ancestral = before.ancestral || this.state.ancestral;
    this.state.local = before.local && this.state.local;
    return {predicate, flags};
  }

  private peek(offset = 0): Token | undefined {
    return this.tokens[this.at + offset];
  }

  private isWord(word: string): boolean {
    const t = this.peek();
    return !!t && t.kind === 'ident' && t.text.toLowerCase() === word;
  }

  private position(): number {
    return this.peek()?.position ?? this.end;
  }

  private or(): Node {
    let left = this.and();
    while (this.isWord('or')) {
      this.at++;
      const a = left;
      const b = this.and();
      left = (f, e) => truthy(a(f, e)) || truthy(b(f, e));
    }
    return left;
  }

  private and(): Node {
    let left = this.not();
    while (this.isWord('and')) {
      this.at++;
      const a = left;
      const b = this.not();
      left = (f, e) => truthy(a(f, e)) && truthy(b(f, e));
    }
    return left;
  }

  private not(): Node {
    if (this.isWord('not') && this.peek(1)?.text !== '(') {
      this.at++;
      const inner = this.not();
      return (f, e) => !truthy(inner(f, e));
    }
    return this.comparison();
  }

  private comparison(): Node {
    const first = this.peek();
    const after = this.peek(1);
    if (first?.kind === 'ident' && first.text.toLowerCase() === 'cardinality' && after?.kind === 'op') {
      return this.cardinalityComparison(after);
    }
    const left = this.operand();
    const t = this.peek();
    if (t && t.kind === 'op') {
      this.at++;
      const right = this.comparisonValue();
      if (this.paths.has(left) || this.paths.has(right)) {
        throw new ParseError('A path can only be tested for existence, or counted with count()', t.position);
      }
      const op = t.text;
      return (f, e) => compare(op, left(f, e), right(f, e), e.caseSensitive);
    }
    return left;
  }

  /**
   * `cardinality = 0..1` is `cardinality_min = 0 and cardinality_max = 1` (and `!=` is its negation); the right side may be
   * a range (`1..*`, `0..unbounded`, `"0..1"`) or a name for one: Optional (0..1), Required (1..1), Prohibited (0..0).
   */
  private cardinalityComparison(op: Token): Node {
    if (op.text !== '=' && op.text !== '!=') {
      throw new ParseError('cardinality can be compared with = and != only; use cardinality_min and cardinality_max for the rest', op.position);
    }
    this.at += 2;
    const {min, max} = this.cardinalityRange();
    const equal = op.text === '=';
    return (f: Frame) => {
      const actualMin = f.ctx.attr('cardinality_min');
      const actualMax = f.ctx.attr('cardinality_max');
      return actualMin !== undefined && actualMax !== undefined && ((actualMin === min && actualMax === max) === equal);
    };
  }

  private cardinalityRange(): {min: number, max: number} {
    const t = this.peek();
    if (!t) {
      throw new ParseError('Unexpected end of predicate', this.end);
    }
    this.at++;
    if (t.kind === 'ident') {
      const named = CARDINALITY_NAMES[t.text.toLowerCase()];
      if (!named) {
        throw new ParseError('Unknown cardinality ' + t.text + ': write a range such as 0..1, or Optional, Required or Prohibited', t.position);
      }
      return named;
    }
    if (t.kind === 'string') {
      const m = /^\s*(\d+)\s*\.\.\s*(\d+|\*|unbounded|∞)\s*$/i.exec(t.text) ?? undefined;
      const named = CARDINALITY_NAMES[t.text.trim().toLowerCase()];
      if (named) {
        return named;
      }
      if (!m) {
        throw new ParseError('Unknown cardinality "' + t.text + '": write a range such as 0..1, or Optional, Required or Prohibited', t.position);
      }
      return {min: Number(m[1]), max: /^\d+$/.test(m[2]) ? Number(m[2]) : Infinity};
    }
    if (t.kind === 'number' && this.peek()?.text === '..') {
      this.at++;
      const upper = this.peek();
      if (upper && (upper.kind === 'number' || upper.text === '*' || upper.kind === 'ident' && /^(unbounded|∞)$/i.test(upper.text))) {
        this.at++;
        return {min: Number(t.text), max: upper.kind === 'number' ? Number(upper.text) : Infinity};
      }
    }
    throw new ParseError('A cardinality is a range such as 0..1 or 1..*, or Optional, Required or Prohibited', t.position);
  }

  /** What a comparison is made against: a bare word (`type = ACC`) is the text it spells, not an attribute. */
  private comparisonValue(): Node {
    const t = this.peek();
    if (t && t.kind === 'ident' && !this.startsPath(t) && this.peek(1)?.text !== '('
      && !['true', 'false'].includes(t.text.toLowerCase())) {
      this.at++;
      this.literals.push(t.text);
      return () => t.text;
    }
    return this.operand();
  }

  private operand(): Node {
    const t = this.peek();
    if (!t) {
      throw new ParseError('Unexpected end of predicate', this.end);
    }
    if (this.startsPath(t)) {
      return this.pathOperand(false);
    }
    this.at++;
    if (t.kind === 'string') {
      this.literals.push(t.text);
      return () => t.text;
    }
    if (t.kind === 'number') {
      const value = Number(t.text);
      return () => value;
    }
    if (t.kind === 'punct' && t.text === '(') {
      const inner = this.or();
      this.expect(')');
      return inner;
    }
    if (t.kind === 'ident') {
      const word = t.text.toLowerCase();
      if (this.peek()?.text === '(') {
        return this.call(t);
      }
      if (word === 'true' || word === 'false') {
        const value = word === 'true';
        return () => value;
      }
      const attribute = t.text;
      return frame => frame.ctx.attr(attribute);
    }
    throw new ParseError('Unexpected ' + t.text, t.position);
  }

  /** Whether the token begins a path inside a predicate (`child::x`, `../x`, `.//x`, `*`, `name/x`). */
  private startsPath(t: Token): boolean {
    if (t.kind === 'punct') {
      return ['.', '..', '*', '/', '//'].includes(t.text);
    }
    if (t.kind !== 'ident') {
      return false;
    }
    const next = this.tokens[this.tokens.indexOf(t) + 1]?.text;
    return next === '::' || next === '/' || next === '//';
  }

  private call(name: Token): Node {
    this.expect('(');
    const fn = name.text.toLowerCase();
    if (fn === 'count') {
      const first = this.peek();
      if (!first || !this.startsPath(first)) {
        throw new ParseError('count() takes a path', this.position());
      }
      const counted = this.pathOperand(true);
      this.expect(')');
      return counted;
    }
    const args: Node[] = [];
    if (this.peek()?.text !== ')') {
      do {
        args.push(this.or());
      } while (this.accept(','));
    }
    this.expect(')');
    switch (fn) {
      case 'position':
      case 'last':
        if (args.length !== 0) {
          throw new ParseError(name.text + '() takes no argument', name.position);
        }
        this.state.positional = true;
        this.state.local = false;
        return this.numeric(fn === 'position' ? frame => frame.position : frame => frame.size);
      case 'text':
        if (args.length !== 0) {
          throw new ParseError('text() takes no argument', name.position);
        }
        return frame => frame.ctx.attr(TEXT_ATTRIBUTE) ?? frame.ctx.names[0];
      case 'true':
      case 'false':
        return () => fn === 'true';
      case 'not':
        if (args.length !== 1) {
          throw new ParseError('not() takes one argument', name.position);
        }
        return (f, e) => !truthy(args[0](f, e));
    }
    const test = TEXT_FUNCTIONS[fn];
    if (!test) {
      throw new ParseError('Unknown function ' + name.text, name.position);
    }
    if (args.length !== 2) {
      throw new ParseError(name.text + '() takes two arguments', name.position);
    }
    return (f, e) => {
      const a = args[0](f, e);
      const b = args[1](f, e);
      return a !== undefined && b !== undefined && test(fold(a, e.caseSensitive), fold(b, e.caseSensitive));
    };
  }

  /** `axis::name[...]/...` as a test that the path selects something (or, for `count()`, the number of nodes it selects). */
  private pathOperand(counting: boolean): Node {
    const first = this.peek();
    if (first.text === '/' || first.text === '//') {
      throw new ParseError('A path in a predicate starts from the node it judges; start it with ./ or ../', first.position);
    }
    this.state.local = false;
    const restrictedBefore = this.state.restricted;
    const steps: CompiledStep[] = [];
    let ancestor: {step: CompiledStep, orSelf: boolean} | undefined;
    let sep: '/' | '//' = '/';
    for (;;) {
      const read = this.readPathStep(sep);
      if (read.axis === 'ancestor' || read.axis === 'ancestor-or-self') {
        if (steps.length > 0 || ancestor || counting || this.state.depth > 0 || this.state.restricted) {
          throw new ParseError(read.axis + ':: can only start a path in a condition on the node itself', read.position);
        }
        ancestor = {step: read.step, orSelf: read.axis === 'ancestor-or-self'};
      } else {
        steps.push(read.step);
      }
      if (read.axis === 'parent' || ancestor) {
        this.state.restricted = true;
      }
      const t = this.peek();
      if (t && t.kind === 'punct' && (t.text === '/' || t.text === '//')) {
        this.at++;
        sep = t.text;
        continue;
      }
      break;
    }
    this.state.restricted = restrictedBefore;
    if (ancestor) {
      return this.ancestorNode(ancestor, steps);
    }
    const path = pathOf(steps, this.state);
    if (!path.pure) {
      this.state.impure = true;
    }
    steps.forEach(s => this.literals.push(...s.literals));
    if (counting) {
      return this.numeric((f, e) => pathCount(path, f.ctx, e));
    }
    const node: Node = (f, e) => pathExists(path, f.ctx, e);
    this.paths.add(node);
    return node;
  }

  /** One step of a path inside a predicate; the predicates nested in it are parsed here. */
  private readPathStep(sep: '/' | '//'): {step: CompiledStep, axis: Axis, position: number} {
    const t = this.peek();
    let axis: Axis | undefined;
    if (t.kind === 'ident' && this.peek(1)?.text === '::') {
      axis = SEARCH_AXES.find(a => a === t.text.toLowerCase());
      if (!axis) {
        throw new ParseError('Unknown axis ' + t.text, t.position);
      }
      this.at += 2;
    }
    const nameToken = this.peek();
    if (!nameToken) {
      throw new ParseError('Unexpected end of predicate', this.end);
    }
    let test: NameTest;
    if (nameToken.kind === 'ident' || nameToken.kind === 'string') {
      test = nameTest(nameToken.text);
    } else if (nameToken.text === '*') {
      test = nameTest('*');
    } else if (nameToken.text === '.' || nameToken.text === '..') {
      axis = nameToken.text === '.' ? 'self' : 'parent';
      test = nameTest('*');
    } else {
      throw new ParseError('Unexpected ' + nameToken.text, nameToken.position);
    }
    this.at++;
    const resolved = sep === '//' ? this.descendantAxis(axis, nameToken) : (axis ?? 'child');
    if (this.state.restricted && !DOWNWARD_AXES.includes(resolved)) {
      throw new ParseError('The ' + resolved + ' axis cannot be used here', nameToken.position);
    }
    const predicates: CompiledPredicate[] = [];
    const flags = stepFlags();
    while (this.peek()?.kind === 'punct' && this.peek().text === '[') {
      const close = this.matchingBracket();
      const inner = this.tokens.slice(this.at + 1, close);
      const closeToken = this.tokens[close];
      this.at = close + 1;
      const wasRestricted = this.state.restricted;
      this.state.depth++;
      this.state.restricted = wasRestricted || resolved === 'parent' || resolved === 'ancestor' || resolved === 'ancestor-or-self';
      const compiled = new PredicateParser(inner, closeToken.position, this.state).parse();
      this.state.restricted = wasRestricted;
      this.state.depth--;
      predicates.push(compiled.predicate);
      mergeFlags(flags, compiled.flags);
    }
    if (flags.positional && (resolved === 'descendant' || resolved === 'ancestor' || resolved === 'ancestor-or-self')) {
      throw new ParseError('A position cannot be asked on the ' + resolved + ' axis', nameToken.position);
    }
    if (flags.ancestral) {
      throw new ParseError('A condition on the ancestors cannot be nested in another condition', nameToken.position);
    }
    return {step: step(resolved, false, [test], predicates, flags), axis: resolved, position: nameToken.position};
  }

  private descendantAxis(axis: Axis | undefined, at: Token): Axis {
    if (axis === undefined || axis === 'child' || axis === 'descendant') {
      return 'descendant';
    }
    if (axis === 'descendant-or-self') {
      return axis;
    }
    throw new ParseError('// must be followed by a name', at.position);
  }

  private matchingBracket(): number {
    let depth = 0;
    for (let i = this.at; i < this.tokens.length; i++) {
      if (this.tokens[i].kind === 'punct' && this.tokens[i].text === '[') {
        depth++;
      } else if (this.tokens[i].kind === 'punct' && this.tokens[i].text === ']' && --depth === 0) {
        return i;
      }
    }
    throw new ParseError('Missing ]', this.end);
  }

  /** `ancestor::Name[...]/rest`: the test is judged at each ancestor as the search passes it, and read from a flag. */
  private ancestorNode(ancestor: {step: CompiledStep, orSelf: boolean}, rest: CompiledStep[]): Node {
    const test: AncestorTest = {
      step: ancestor.step, orSelf: ancestor.orSelf, rest: rest.length > 0 ? pathOf(rest, this.state) : undefined
    };
    const k = this.state.ancestorTests.push(test) - 1;
    this.state.ancestral = true;
    this.state.impure = true;
    const node: Node = (f, e) => f.ctx.ancestorFlag(k) || ancestor.orSelf && ancestorTestHolds(test, f.ctx, e);
    this.paths.add(node);
    return node;
  }

  private numeric(node: Node): Node {
    this.numbers.add(node);
    return node;
  }

  private accept(text: string): boolean {
    if (this.peek()?.text === text) {
      this.at++;
      return true;
    }
    return false;
  }

  private expect(text: string) {
    if (!this.accept(text)) {
      throw new ParseError('Expected ' + text, this.position());
    }
  }
}

/** Whether a node itself satisfies an ancestor test (what each ancestor is asked as the search passes it). */
export function ancestorTestHolds(test: AncestorTest, ctx: NodeContext, env: Env): boolean {
  if (!passesTests(ctx.names, test.step.tests, env) || !test.step.predicates.every(p => p({ctx, position: 1, size: 1}, env))) {
    return false;
  }
  return !test.rest || pathExists(test.rest, ctx, env);
}

const CARDINALITY_NAMES: Record<string, {min: number, max: number}> = {
  optional: {min: 0, max: 1}, required: {min: 1, max: 1}, prohibited: {min: 0, max: 0}
};

const TEXT_FUNCTIONS: Record<string, (a: string, b: string) => boolean> = {
  'contains': (a, b) => a.indexOf(b) > -1,
  'starts-with': (a, b) => a.startsWith(b),
  'ends-with': (a, b) => a.endsWith(b)
};

/** What a count is compared with to mean no upper bound. */
const UNBOUNDED = /^(\*|unbounded|∞|infinity)$/i;

function truthy(value: AttrValue): boolean {
  return value !== undefined && value !== false && value !== '' && value !== 0;
}

function fold(value: AttrValue, caseSensitive: boolean): string {
  const text = String(value);
  return caseSensitive ? text : text.toLowerCase();
}

/**
 * Compares two values; a comparison with a missing value is false (so is `!=`: an attribute a node does not have
 * is not "different"). Flags compare with `true`/`false`, numbers numerically, anything else as text.
 */
function compare(op: string, a: AttrValue, b: AttrValue, caseSensitive: boolean): boolean {
  if (a === undefined || b === undefined) {
    return false;
  }
  let order: number;
  const x0 = typeof a === 'string' && UNBOUNDED.test(a) && typeof b === 'number' ? Infinity : a;
  const y0 = typeof b === 'string' && UNBOUNDED.test(b) && typeof a === 'number' ? Infinity : b;
  if (typeof x0 === 'number' && typeof y0 === 'number') {
    order = x0 < y0 ? -1 : x0 > y0 ? 1 : 0;
  } else if (typeof a === 'number' && /^-?\d+(\.\d+)?$/.test(String(b)) || typeof b === 'number' && /^-?\d+(\.\d+)?$/.test(String(a))) {
    const n = Number(a) - Number(b);
    order = n < 0 ? -1 : n > 0 ? 1 : 0;
  } else {
    const x = fold(a, caseSensitive);
    const y = fold(b, caseSensitive);
    order = x < y ? -1 : x > y ? 1 : 0;
  }
  switch (op) {
    case '=':
      return order === 0;
    case '!=':
      return order !== 0;
    case '<':
      return order < 0;
    case '<=':
      return order <= 0;
    case '>':
      return order > 0;
    default:
      return order >= 0;
  }
}
