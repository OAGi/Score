/** What a node tells a search predicate about itself: a string, a number, a flag, or nothing at all. */
export type AttrValue = string | number | boolean | undefined;

/** Reads the attribute of the node a predicate is judging. */
export type AttrLookup = (attribute: string) => AttrValue;

/** A malformed search expression: what is wrong and where (an offset into the expression). */
export interface SearchExpressionError {
  message: string;
  position: number;
}

/** A compiled `[...]` predicate. */
export type Predicate = (lookup: AttrLookup, caseSensitive: boolean) => boolean;

/** One step of a search expression: `text[predicate][predicate]...`. */
export interface SearchStep {
  /** The name to look for as written (`*` and an empty name match any node). */
  text: string;
  /** Whether the step names no particular node (`*`, or nothing). */
  any: boolean;
  /** The step's predicates; all of them must hold. */
  predicates: Predicate[];
  /** The literal strings the predicates look for, which is what a result list highlights when the step has no name. */
  literals: string[];
}

export interface SearchExpression {
  rooted: boolean;
  steps: SearchStep[];
  error?: SearchExpressionError;
}

/** The attribute-free functions a predicate may call. */
export const SEARCH_FUNCTIONS = ['contains', 'starts-with', 'ends-with'];

type Operand = (lookup: AttrLookup) => AttrValue;
type Node = (lookup: AttrLookup, caseSensitive: boolean) => AttrValue;

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

/**
 * Parses a search expression: `[/]step(/step)*` where a step is a name (or `*`) followed by any number of
 * `[predicate]`. A predicate combines comparisons (`used = true`, `cardinalityMax > 1`) and functions
 * (`contains(displayName, "id")`) with `and`, `or`, `not` and parentheses; a bare attribute (`[used]`) tests that it
 * is true.
 *
 * An expression without `[` is read the way it always was: its steps are split on `/` and are names only. An error
 * does not throw: the expression comes back with `error` set and no steps.
 */
export function parseSearchExpression(expr: string): SearchExpression {
  const rooted = expr.startsWith('/');
  const body = rooted ? expr.substring(1) : expr;
  const offset = rooted ? 1 : 0;
  if (body.indexOf('[') < 0) {
    return {rooted, steps: body.split('/').map(plainStep)};
  }
  try {
    return {rooted, steps: splitSteps(body, offset).map(({text, position}) => parseStep(text, position))};
  } catch (e) {
    if (e instanceof ParseError) {
      return {rooted, steps: [], error: {message: e.message, position: e.position}};
    }
    throw e;
  }
}

function plainStep(text: string): SearchStep {
  const trimmed = text.trim();
  return {text: splitCamelCase(trimmed), any: trimmed === '*', predicates: [], literals: []};
}

/** Cuts the expression at the `/` that are outside brackets and quotes. */
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

function parseStep(text: string, position: number): SearchStep {
  const open = text.indexOf('[');
  if (open < 0) {
    return plainStep(text);
  }
  const step = plainStep(text.substring(0, open));
  let at = open;
  while (at < text.length) {
    const close = closingBracket(text, at);
    const inner = text.substring(at + 1, close);
    const parser = new PredicateParser(tokenize(inner, position + at + 1), position + close);
    step.predicates.push(parser.parse());
    step.literals.push(...parser.literals);
    at = close + 1;
    if (at < text.length && text[at] !== '[') {
      throw new ParseError('Expected [ or / after ]', position + at);
    }
  }
  return step;
}

function closingBracket(text: string, open: number): number {
  let quote = '';
  for (let i = open + 1; i < text.length; i++) {
    const c = text[i];
    if (quote) {
      if (c === quote) {
        quote = '';
      }
    } else if (c === '"' || c === '\'') {
      quote = c;
    } else if (c === ']') {
      return i;
    }
  }
  throw new ParseError('Missing ]', text.length);
}

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
    } else if (c === '!' || c === '<' || c === '>' || c === '=') {
      const two = text.substr(i, 2);
      const op = two === '!=' || two === '<=' || two === '>=' ? two : c;
      if (op === '!') {
        throw new ParseError('Unexpected !', offset + i);
      }
      tokens.push({kind: 'op', text: op, position: offset + i});
      i += op.length;
    } else if (c === '(' || c === ')' || c === ',') {
      tokens.push({kind: 'punct', text: c, position: offset + i});
      i++;
    } else {
      throw new ParseError('Unexpected ' + c, offset + i);
    }
  }
  return tokens;
}

/** Recursive descent over `or > and > not > comparison > operand`; every node compiles to a closure. */
class PredicateParser {

  readonly literals: string[] = [];
  private at = 0;

  constructor(private readonly tokens: Token[], private readonly end: number) {
  }

  parse(): Predicate {
    if (this.tokens.length === 0) {
      throw new ParseError('Empty predicate', this.end);
    }
    const node = this.or();
    if (this.at < this.tokens.length) {
      throw new ParseError('Unexpected ' + this.tokens[this.at].text, this.tokens[this.at].position);
    }
    return (lookup, caseSensitive) => truthy(node(lookup, caseSensitive));
  }

  private peek(): Token | undefined {
    return this.tokens[this.at];
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
      left = (l, cs) => truthy(a(l, cs)) || truthy(b(l, cs));
    }
    return left;
  }

  private and(): Node {
    let left = this.not();
    while (this.isWord('and')) {
      this.at++;
      const a = left;
      const b = this.not();
      left = (l, cs) => truthy(a(l, cs)) && truthy(b(l, cs));
    }
    return left;
  }

  private not(): Node {
    if (this.isWord('not') && this.tokens[this.at + 1]?.text !== '(') {
      this.at++;
      const inner = this.not();
      return (l, cs) => !truthy(inner(l, cs));
    }
    return this.comparison();
  }

  private comparison(): Node {
    const left = this.operand();
    const t = this.peek();
    if (t && t.kind === 'op') {
      this.at++;
      const right = this.operand();
      const op = t.text;
      return (l, cs) => compare(op, left(l, cs), right(l, cs), cs);
    }
    return left;
  }

  private operand(): Node {
    const t = this.peek();
    if (!t) {
      throw new ParseError('Unexpected end of predicate', this.end);
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
      return lookup => lookup(attribute);
    }
    throw new ParseError('Unexpected ' + t.text, t.position);
  }

  private call(name: Token): Node {
    this.expect('(');
    const args: Node[] = [];
    if (this.peek()?.text !== ')') {
      do {
        args.push(this.or());
      } while (this.accept(','));
    }
    this.expect(')');
    const fn = name.text.toLowerCase();
    if (fn === 'not') {
      if (args.length !== 1) {
        throw new ParseError('not() takes one argument', name.position);
      }
      return (l, cs) => !truthy(args[0](l, cs));
    }
    const test = TEXT_FUNCTIONS[fn];
    if (!test) {
      throw new ParseError('Unknown function ' + name.text, name.position);
    }
    if (args.length !== 2) {
      throw new ParseError(name.text + '() takes two arguments', name.position);
    }
    return (l, cs) => {
      const a = args[0](l, cs);
      const b = args[1](l, cs);
      return a !== undefined && b !== undefined && test(fold(a, cs), fold(b, cs));
    };
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

const TEXT_FUNCTIONS: Record<string, (a: string, b: string) => boolean> = {
  'contains': (a, b) => a.indexOf(b) > -1,
  'starts-with': (a, b) => a.startsWith(b),
  'ends-with': (a, b) => a.endsWith(b)
};

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
  if (typeof a === 'number' && typeof b === 'number') {
    order = a - b;
  } else if (typeof a === 'number' && /^-?\d+(\.\d+)?$/.test(String(b)) || typeof b === 'number' && /^-?\d+(\.\d+)?$/.test(String(a))) {
    order = Number(a) - Number(b);
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
