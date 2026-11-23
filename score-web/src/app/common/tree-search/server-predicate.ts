import {CstElement, CstNode, IToken} from 'chevrotain';
import {Ident, NumberLit, ParseError, StringLit, parsePredicate, scanBrackets} from './search-grammar';

/** How a server predicate compares the stored text with its value. */
export type ServerOperator = 'EQUALS' | 'NOT_EQUALS' | 'CONTAINS' | 'STARTS_WITH' | 'ENDS_WITH';

/**
 * One condition of a search expression that is answered by the backend, because the text it asks about is too heavy to
 * travel with the search graph (see `SearchAttribute.source`).
 */
export interface ServerPredicate {
  /** What the backend calls the predicate; the same condition written twice is one predicate. */
  id: string;
  /** The attribute of the graph that says, for a place, whether the predicate holds there. */
  flag: string;
  attribute: string;
  operator: ServerOperator;
  value: string;
}

export interface ServerSplit {
  /** The expression with every server condition replaced by the test of its flag; the one the graph search runs. */
  expression: string;
  predicates: ServerPredicate[];
}

const TEXT_FUNCTIONS: Record<string, ServerOperator> = {
  'contains': 'CONTAINS',
  'starts-with': 'STARTS_WITH',
  'ends-with': 'ENDS_WITH'
};

/** The name of the graph attribute that holds the answer of the predicate with the given id. */
export function serverFlagName(id: string): string {
  return 'srv_' + id;
}

/**
 * Splits an expression into what the graph search can judge and what the backend has to: every condition on a server
 * attribute (`remark contains "x"`, `contains(remark, "x")`, `remark = "x"`) is replaced by the test of a flag that the
 * answer of the backend sets on the places it holds for, so `and`, `or` and `not` mix both kinds as they always did.
 *
 * An expression that does not parse is returned as it is, to be reported by the ordinary parse. A server attribute
 * that is used in a way the backend cannot answer (ordered with `<`, or not compared with anything) is a `ParseError`.
 */
export function splitServerPredicates(expression: string, serverAttributes: ReadonlySet<string>): ServerSplit {
  const unchanged: ServerSplit = {expression, predicates: []};
  if (serverAttributes.size === 0) {
    return unchanged;
  }
  const predicates: ServerPredicate[] = [];
  const idOf = (attribute: string, operator: ServerOperator, value: string): ServerPredicate => {
    const known = predicates.find(p => p.attribute === attribute && p.operator === operator && p.value === value);
    if (known) {
      return known;
    }
    const id = String(predicates.length);
    const added = {id, flag: serverFlagName(id), attribute, operator, value};
    predicates.push(added);
    return added;
  };

  let brackets: {open: number, close: number}[];
  try {
    brackets = scanBrackets(expression).brackets;
  } catch (e) {
    if (e instanceof ParseError) {
      return unchanged;
    }
    throw e;
  }
  let result = '';
  let copied = 0;
  for (const bracket of brackets) {
    const inner = expression.substring(bracket.open + 1, bracket.close);
    const base = bracket.open + 1;
    let cst: CstNode;
    try {
      cst = parsePredicate(inner, base, bracket.close);
    } catch (e) {
      if (e instanceof ParseError) {
        return unchanged;
      }
      throw e;
    }
    const edits = serverEdits(cst, base, serverAttributes, idOf);
    if (edits.length === 0) {
      continue;
    }
    let text = inner;
    for (const edit of edits.sort((a, b) => b.start - a.start)) {
      text = text.substring(0, edit.start) + edit.text + text.substring(edit.end);
    }
    result += expression.substring(copied, base) + text;
    copied = bracket.close;
  }
  if (predicates.length === 0) {
    return unchanged;
  }
  return {expression: result + expression.substring(copied), predicates};
}

/**
 * The text the way the backend's collation sees it when case does not matter: upper and lower case letters, and a letter
 * with an accent and without one, are the same (`café` is `CAFE`). The accents are taken off through the decomposed form
 * and the text is composed again, so that what is not an accent (a Hangul syllable) stays as it was.
 */
function foldedText(text: string): string {
  return text.normalize('NFD').replace(/\p{M}/gu, '').normalize('NFC').toLowerCase();
}

/**
 * Whether the text a node holds satisfies the predicate (what the backend would answer, for a value not yet saved). A
 * case sensitive predicate compares the texts as they are, as the backend compares binary strings.
 */
export function satisfiesServerPredicate(text: string | undefined, predicate: ServerPredicate, caseSensitive: boolean): boolean {
  if (text === undefined || text === null) {
    return false;
  }
  const fold = (s: string) => caseSensitive ? s : foldedText(s);
  const stored = fold(text);
  const wanted = fold(predicate.value);
  switch (predicate.operator) {
    case 'EQUALS':
      return stored === wanted;
    case 'NOT_EQUALS':
      return stored !== wanted;
    case 'CONTAINS':
      return stored.includes(wanted);
    case 'STARTS_WITH':
      return stored.startsWith(wanted);
    default:
      return stored.endsWith(wanted);
  }
}

// ---- reading the expression ------------------------------------------------------------------------------------------

interface Edit {
  start: number;
  end: number;
  text: string;
}

function serverEdits(root: CstNode, base: number, serverAttributes: ReadonlySet<string>,
                     idOf: (attribute: string, operator: ServerOperator, value: string) => ServerPredicate): Edit[] {
  const edits: Edit[] = [];
  const isServer = (token: IToken | undefined): token is IToken =>
    !!token && token.tokenType === Ident && serverAttributes.has(token.image.toLowerCase());

  const visit = (cst: CstNode) => {
    if (cst.name === 'comparison' && !cst.children['cardinality']) {
      const left = soleToken(cst.children['left'][0] as CstNode);
      if (isServer(left)) {
        edits.push(comparisonEdit(cst, left, base, idOf));
        return;
      }
    } else if (cst.name === 'functionCall') {
      const edit = functionEdit(cst, isServer, idOf);
      if (edit) {
        edits.push(edit);
        return;
      }
    }
    for (const children of Object.values(cst.children)) {
      for (const child of children) {
        if (!isToken(child)) {
          visit(child);
        }
      }
    }
  };
  visit(root);
  return edits;
}

function comparisonEdit(cst: CstNode, attribute: IToken, base: number,
                        idOf: (attribute: string, operator: ServerOperator, value: string) => ServerPredicate): Edit {
  const name = attribute.image.toLowerCase();
  const op = cst.children['op']?.[0] as IToken | undefined;
  if (!op) {
    throw new ParseError(name + ' must be compared with a value, e.g. ' + name + ' = "text" or contains(' + name + ', "text")',
      base + attribute.startOffset);
  }
  if (op.image !== '=' && op.image !== '!=') {
    throw new ParseError(name + ' can be compared with = and != only; use contains(' + name + ', "text") to look for a part',
      base + op.startOffset);
  }
  const right = cst.children['right'][0] as CstNode;
  const bare = right.children['bare']?.[0] as IToken | undefined;
  const value = bare ? bare.image : literalOf(soleToken(right));
  if (value === undefined) {
    throw new ParseError(name + ' can be compared with a text only', base + op.startOffset);
  }
  const predicate = idOf(name, op.image === '=' ? 'EQUALS' : 'NOT_EQUALS', value);
  const [start, end] = spanOf(cst);
  return {start, end, text: predicate.flag};
}

function functionEdit(cst: CstNode, isServer: (token: IToken | undefined) => token is IToken,
                      idOf: (attribute: string, operator: ServerOperator, value: string) => ServerPredicate): Edit | undefined {
  const fn = (cst.children['name'][0] as IToken).image.toLowerCase();
  const operator = TEXT_FUNCTIONS[fn];
  const args = (cst.children['arg'] ?? []) as CstNode[];
  if (!operator || args.length !== 2) {
    return undefined;
  }
  const attribute = soleToken(args[0]);
  if (!isServer(attribute)) {
    return undefined;
  }
  const value = literalOf(soleToken(args[1]));
  if (value === undefined) {
    return undefined;
  }
  const predicate = idOf(attribute.image.toLowerCase(), operator, value);
  const [start, end] = spanOf(cst);
  return {start, end, text: predicate.flag};
}

/** The text a string, a number or a word spells; undefined for anything else (a path, a function). */
function literalOf(token: IToken | undefined): string | undefined {
  if (!token) {
    return undefined;
  }
  if (token.tokenType === StringLit) {
    return token.image.substring(1, token.image.length - 1);
  }
  return token.tokenType === NumberLit || token.tokenType === Ident ? token.image : undefined;
}

function isToken(element: CstElement): element is IToken {
  return 'image' in element;
}

/** The only token a node is made of, if it is made of one token and nothing else. */
function soleToken(cst: CstNode | undefined): IToken | undefined {
  if (!cst) {
    return undefined;
  }
  const all = Object.values(cst.children).flat();
  if (all.length !== 1) {
    return undefined;
  }
  return isToken(all[0]) ? all[0] : soleToken(all[0]);
}

/** Where the node's text begins and ends in the predicate. */
function spanOf(cst: CstNode): [number, number] {
  let start = Infinity;
  let end = -1;
  const visit = (node: CstNode) => {
    for (const child of Object.values(node.children).flat()) {
      if (isToken(child)) {
        start = Math.min(start, child.startOffset);
        end = Math.max(end, child.startOffset + child.image.length);
      } else {
        visit(child);
      }
    }
  };
  visit(cst);
  return [start, end];
}
