import {Observable} from 'rxjs';
import {SEARCH_FUNCTIONS} from './search-expression';
import {Continuation, PredicateTail, RANGE_ATTRIBUTES, predicateTail} from './search-grammar';

/**
 * The axes that are worth spelling out: `child::` is what no axis means, `self::` is `.`, `parent::` is `..` and `descendant::`
 * is `//`, so those are offered in their short forms (see `upSuggestions`) and `/`, `//`.
 */
const AXIS_DETAILS: [string, string][] = [
  ['descendant-or-self', 'the node itself and the nodes below'],
  ['ancestor', 'the nodes above, in a predicate: *[ancestor::Name]'],
  ['ancestor-or-self', 'the node itself and the nodes above, in a predicate'],
  ['following-sibling', 'the siblings after the node'],
  ['preceding-sibling', 'the siblings before the node']
];

/** An attribute of a node that a search predicate may ask about. */
export interface SearchAttribute {
  name: string;
  /** `text` is free text or an enum, `number` compares numerically, `flag` is true/false. */
  type: 'text' | 'number' | 'flag';
  /**
   * Where the value is looked up: `graph` attributes travel with the search graph and are judged in the browser (the
   * default), `server` attributes are searched by the backend and only the matches reach the graph. Text that differs
   * per place and is long (remarks, examples) is `server`; see the hybrid tree search plan.
   */
  source?: 'graph' | 'server';
  /** Whether a node can have several values of it; a comparison then holds when any one of them satisfies it. */
  multiple?: boolean;
  description: string;
  /** The values worth offering after `name =`, when the attribute has a fixed set of them. */
  values?: string[];
  /** Whether the values are written without quotes (`cardinality = 0..1`). */
  bare?: boolean;
  /**
   * Looks up the values worth offering after `name =` for the text typed so far, when there are too many to list (or
   * they live on the server); the search bar waits for them and then offers them like `values`.
   */
  valueSource?: (text: string) => Observable<string[]>;
}

/** One completion: replace `[from, to)` of the expression with `insert`. */
export interface SearchSuggestion {
  label: string;
  insert: string;
  detail: string;
  type: 'attribute' | 'function' | 'operator' | 'keyword' | 'value' | 'snippet';
  /** How many characters before the end of `insert` the caret goes (inside the parentheses of `count()`); the end when absent. */
  caretBack?: number;
}

export interface SearchSuggestions {
  from: number;
  to: number;
  items: SearchSuggestion[];
}

const NO_SUGGESTIONS: SearchSuggestions = {from: 0, to: 0, items: []};

const COMPARISONS: { op: string, detail: string, flag: boolean }[] = [
  {op: '=', detail: 'equals', flag: true},
  {op: '!=', detail: 'differs from', flag: true},
  {op: '<', detail: 'less than', flag: false},
  {op: '<=', detail: 'at most', flag: false},
  {op: '>', detail: 'greater than', flag: false},
  {op: '>=', detail: 'at least', flag: false}
];

const FUNCTION_DETAILS: Record<string, string> = {
  'contains': 'contains(attribute, "text"): the value has the text in it',
  'starts-with': 'starts-with(attribute, "text"): the value begins with the text',
  'ends-with': 'ends-with(attribute, "text"): the value ends with the text',
  'text': 'the text the node shows in the tree, e.g. contains(text(), "id")',
  'position': 'the place among the siblings the step selects (1 is first); [2] is [position() = 2]',
  'last': 'how many siblings the step selects; [last()] is the last one',
  'count': 'count(path): how many nodes the path selects, e.g. count(*) > 3'
};

/** What an argument of a function is: an attribute (or `text()`), a quoted text, or a path. */
interface FunctionParameter {
  type: 'value' | 'text' | 'path';
  detail: string;
}

const TEXT_FUNCTION_PARAMETERS: FunctionParameter[] = [
  {type: 'value', detail: 'the attribute (or text()) whose value is searched'},
  {type: 'text', detail: 'the text to look for in it'}
];

/** The arguments each function takes, in order; a function without any is completed with its parentheses. */
const FUNCTION_PARAMETERS: Record<string, FunctionParameter[]> = {
  ...Object.fromEntries(SEARCH_FUNCTIONS.map(f => [f, TEXT_FUNCTION_PARAMETERS])),
  'text': [],
  'position': [],
  'last': [],
  'count': [{type: 'path', detail: 'the path whose nodes are counted: * for every child, or e.g. Code, ../Code, descendant-or-self::Code'}]
};

/** What the field offers while it is empty: examples of the expressions it understands. */
export function searchSyntaxHints(attributes: SearchAttribute[]): SearchSuggestion[] {
  const flag = attributes.find(a => a.type === 'flag');
  const hints: SearchSuggestion[] = [
    {label: '*', insert: '*', detail: 'any node; Parent/Child searches a path, a leading / anchors it at the root, // reaches any depth', type: 'snippet'}
  ];
  if (flag) {
    hints.push({label: `*[${flag.name} = true]`, insert: `*[${flag.name} = true]`, detail: 'nodes by attribute', type: 'snippet'});
  }
  if (attributes.length > 0) {
    const example = '*[contains(text(), "id")]';
    hints.push({label: example, insert: example, detail: 'text() is the text the node shows; combine conditions with and, or, not', type: 'snippet'});
  }
  hints.push(
    {label: '*[../Code]', insert: '*[../Code]', detail: 'a node whose parent has a child with Code in its name (so it and its siblings); also ancestor::, following-sibling::, preceding-sibling::', type: 'snippet'},
    {label: '*[2]', insert: '*[2]', detail: 'the 2nd of the siblings; also position() and last()', type: 'snippet'}
  );
  return hints;
}

/**
 * What to offer at the caret of a search expression: attribute names, functions, comparison operators, `and`/`or`,
 * and the values an attribute can take, depending on where in a `[...]` predicate the caret is. Outside a predicate
 * only the start of one (`[`) and `*` are worth offering. Nothing is offered inside a quoted text, except the values
 * of an attribute with a fixed set of them.
 */
export function suggestSearch(expression: string, caret: number, attributes: SearchAttribute[]): SearchSuggestions {
  const before = expression.substring(0, caret);
  if (!expression.trim() && attributes.length > 0) {
    return {from: 0, to: expression.length, items: searchSyntaxHints(attributes)};
  }
  const context = readContext(before);
  const attributeOf = (name: string | undefined) => attributes.find(a => a.name.toLowerCase() === name?.toLowerCase());

  if (!context.predicate) {
    return stepSuggestions(before, caret, attributes);
  }

  const {prefix, word, wordStart, quote, quoteChar} = context;
  const tail = predicateTail(prefix);
  if (!tail) {
    return NO_SUGGESTIONS;
  }
  const last = tail.tokens[tail.tokens.length - 1];
  if (quote) {
    const searched = searchedByText(tail.tokens, attributes);
    const values = last?.type === 'Op' ? valueSuggestions(attributeOf(tail.tokens[tail.tokens.length - 2]?.text), false, quoteChar)
      // the text a function looks for: the values that the function would find the typed text in
      : searched ? valueSuggestions(searched.attribute, false, quoteChar).filter(v => fits(searched.function, word, v.label)) : [];
    return rank(word, wordStart, caret, values);
  }
  return rank(word, wordStart, caret, predicateSuggestions(tail, attributes, expression.substring(caret)));
}

/** What the grammar lets follow the predicate so far, as suggestions; what a name or a value stands for comes from the attributes. */
function predicateSuggestions(tail: PredicateTail, attributes: SearchAttribute[], after: string): SearchSuggestion[] {
  const {tokens, next, complete} = tail;
  const last = tokens[tokens.length - 1];
  const beforeLast = tokens[tokens.length - 2];
  const attributeOf = (name: string | undefined) => attributes.find(a => a.name.toLowerCase() === name?.toLowerCase());
  const afterOp = last?.type === 'Op';
  const inPath = ['Slash', 'SlashSlash', 'DoubleColon'].includes(last?.type);
  const items: SearchSuggestion[] = [];

  // the values of an attribute that is looked up are offered inside the quotes only: until then the grammar goes on
  if (afterOp && !attributeOf(beforeLast?.text)?.valueSource) {
    const values = valueSuggestions(attributeOf(beforeLast?.text), true);
    if (values.length > 0) {
      return values;
    }
  }
  // a comparison is made with a value, never with a path: after an operator only values and functions make sense
  if (next.has('name') && !inPath && !afterOp) {
    // an attribute that the condition joined by `and` already compares with `=` is not offered again: a second equality on it
    // can only repeat the first or contradict it
    const compared = equalityComparedByAnd(tokens);
    items.push(...attributeSuggestions(attributes).filter(a => !compared.has(a.label.toLowerCase())));
  }
  if (next.has('call')) {
    items.push(...functionSuggestions());
  }
  if (next.has('axis') && !afterOp) {
    items.push(...axisSuggestions());
  }
  if (next.has('any') && !afterOp) {
    items.push({label: '*', insert: '*', detail: 'any node', type: 'snippet'});
  }
  if (last?.type === 'DotDot' && beforeLast?.type === 'NumberLit') {
    return [{label: '*', insert: '*', detail: 'no upper limit, e.g. 1..*', type: 'snippet'}];
  }
  if (next.has('parent') && !afterOp) {
    items.push(last?.type === 'NumberLit' ?
      {label: '..', insert: '..', detail: 'a range, e.g. 0..1 or 1..*', type: 'snippet'} : upSuggestions()[0]);
  }
  if (next.has('self') && !afterOp) {
    items.push(upSuggestions()[1]);
  }
  if (next.has('name') && !afterOp && !inPath) {
    items.push({label: 'not', insert: 'not ', detail: 'negate the condition', type: 'keyword'});
  }
  const known = last?.type === 'Ident' ? attributeOf(last.text) : undefined;
  if (next.has('op') && (known || last?.type === 'RParen')) {
    items.push(...operatorSuggestions(known));
  }
  if (!next.has('name')) {
    for (const [type, label, detail] of JOINS) {
      if (next.has(type)) {
        items.push({label, insert: label + ' ', detail, type: 'keyword'});
      }
    }
  }
  // a path goes on after a step (a name after / or ::, *, . or ..), not after an attribute, a value or a keyword
  const afterStep = ['Star', 'Dot', 'DotDot', 'RBracket'].includes(last?.type) && beforeLast?.type !== 'DotDot'
    || last?.type === 'Ident' && ['Slash', 'SlashSlash', 'DoubleColon'].includes(beforeLast?.type);
  if (afterStep) {
    if (next.has('slash')) {
      items.push({label: '/', insert: '/', detail: 'go on to a child node', type: 'operator'});
    }
    if (next.has('descendant')) {
      items.push({label: '//', insert: '//', detail: 'go on to a node at any depth below', type: 'operator'});
    }
    if (next.has('predicate')) {
      items.push({label: '[', insert: '[', detail: 'one more condition on this node', type: 'operator'});
    }
  }
  if (next.has('comma')) {
    items.push({label: ',', insert: ', ', detail: 'the next argument', type: 'operator'});
  }
  // the `)` of `count()` is there already when the caret is between the parentheses
  if (next.has('closeParen') && !after.trimStart().startsWith(')')) {
    items.push({label: ')', insert: ')', detail: 'end of the arguments or of the group', type: 'operator'});
  }
  if (complete && !next.has('name') || next.has('closeBracket')) {
    items.push({label: ']', insert: ']', detail: 'end of the condition', type: 'operator'});
  }
  const call = last?.type === 'LParen' || last?.type === 'Comma' ? enclosingCall(tokens) : undefined;
  const parameter = call ? FUNCTION_PARAMETERS[call.name.toLowerCase()]?.[call.index] : undefined;
  return parameter ? argumentSuggestions(parameter, items, tokens, call.index, attributes) : items;
}

/** The attributes compared with `=` in the conditions joined by `and` that lead up to the end of the tokens (an `or` or an opening bracket ends them). */
function equalityComparedByAnd(tokens: {type: string, text: string}[]): Set<string> {
  const compared = new Set<string>();
  let depth = 0;
  for (let i = tokens.length - 1; i >= 0; i--) {
    const {type, text} = tokens[i];
    if (type === 'RParen' || type === 'RBracket') {
      depth++;
    } else if (type === 'LParen' || type === 'LBracket') {
      if (depth-- === 0) {
        break;
      }
    } else if (depth === 0) {
      if (type === 'Ident' && text.toLowerCase() === 'or') {
        break;
      }
      if (type === 'Op' && text === '=' && tokens[i - 1]?.type === 'Ident') {
        compared.add(tokens[i - 1].text.toLowerCase());
      }
    }
  }
  return compared;
}

/** The function call whose argument the end of the tokens begins: its name and the place of the argument (from 0). */
function enclosingCall(tokens: {type: string, text: string}[]): {name: string, index: number} | undefined {
  let depth = 0;
  let index = 0;
  for (let i = tokens.length - 1; i >= 0; i--) {
    const type = tokens[i].type;
    if (type === 'RParen' || type === 'RBracket') {
      depth++;
    } else if (type === 'LBracket') {
      if (depth-- === 0) {
        return undefined;
      }
    } else if (type === 'LParen') {
      if (depth-- === 0) {
        return tokens[i - 1]?.type === 'Ident' ? {name: tokens[i - 1].text, index} : undefined;
      }
    } else if (type === 'Comma' && depth === 0) {
      index++;
    }
  }
  return undefined;
}

/** The attribute a text function (`contains`, `starts-with`, `ends-with`) searches, when the tokens end where its text begins. */
function searchedByText(tokens: {type: string, text: string}[], attributes: SearchAttribute[]): { attribute: SearchAttribute, function: string } | undefined {
  if (tokens[tokens.length - 1]?.type !== 'Comma') {
    return undefined;
  }
  const call = enclosingCall(tokens);
  if (!call || call.index !== 1 || !SEARCH_FUNCTIONS.includes(call.name.toLowerCase())) {
    return undefined;
  }
  const attribute = attributes.find(a => a.name.toLowerCase() === tokens[tokens.length - 2]?.text.toLowerCase());
  return attribute ? {attribute, function: call.name.toLowerCase()} : undefined;
}

/** Whether a function finds the typed text in the value: at its start, at its end, or anywhere in it. */
function fits(searchFunction: string, typed: string, value: string): boolean {
  const text = value.toLowerCase();
  const wanted = typed.toLowerCase();
  return searchFunction === 'starts-with' ? text.startsWith(wanted) : searchFunction === 'ends-with' ? text.endsWith(wanted) : text.includes(wanted);
}

/** What may be written where a function's argument begins: only what the argument is (a path, an attribute, a text). */
function argumentSuggestions(parameter: FunctionParameter, items: SearchSuggestion[], tokens: {type: string, text: string}[],
                             index: number, attributes: SearchAttribute[]): SearchSuggestion[] {
  switch (parameter.type) {
    case 'path':
      return items.filter(i => i.label.endsWith('::') || ['*', '..', '.'].includes(i.label))
        .map(i => i.label === '*' ? {...i, detail: parameter.detail} : i);
    case 'value':
      return items.filter(i => i.type === 'attribute' || i.label === 'text()');
    default: {
      // the text a function looks for: the values of the attribute it searches, when it has a fixed set of them
      const searched = attributes.find(a => a.name.toLowerCase() === tokens[tokens.length - 2]?.text.toLowerCase());
      const values = searched?.type === 'text' ? valueSuggestions({...searched, bare: false}, true).map(v => ({...v, insert: v.insert.trimEnd()})) : [];
      return [{label: '"text"', insert: '""', detail: parameter.detail, type: 'snippet', caretBack: 1}, ...values];
    }
  }
}

const JOINS: [Continuation, string, string][] = [
  ['and', 'and', 'both conditions hold'],
  ['or', 'or', 'either condition holds']
];

interface Context {
  /** Whether the caret is inside `[...]`. */
  predicate: boolean;
  /** What the predicate holds before the word being typed (before the quote, inside a quoted text). */
  prefix: string;
  /** The word being typed (what a completion replaces). */
  word: string;
  wordStart: number;
  /** Whether the caret is inside a quoted text. */
  quote: boolean;
  /** The character that opened the quoted text (`"` or `'`), which is the one that closes it. */
  quoteChar: string;
}

/** Reads the part of the expression before the caret: whether it ends in a predicate, and the word being typed. */
function readContext(before: string): Context {
  let depth = 0;
  let quote = '';
  let quoteStart = -1;
  let predicateStart = -1;
  for (let i = 0; i < before.length; i++) {
    const c = before[i];
    if (quote) {
      if (c === quote) {
        quote = '';
      }
    } else if (depth > 0 && (c === '"' || c === '\'')) {
      quote = c;
      quoteStart = i;
    } else if (c === '[') {
      if (depth++ === 0) {
        predicateStart = i + 1;
      }
    } else if (c === ']' && depth > 0) {
      depth--;
    }
  }
  if (depth === 0) {
    return {predicate: false, prefix: '', word: '', wordStart: before.length, quote: false, quoteChar: ''};
  }
  if (quote) {
    return {
      predicate: true, prefix: before.substring(predicateStart, quoteStart), word: before.substring(quoteStart + 1),
      wordStart: quoteStart + 1, quote: true, quoteChar: quote
    };
  }
  const body = before.substring(predicateStart);
  const word = /[A-Za-z0-9_-]*$/.exec(body)[0];
  return {predicate: true, prefix: body.substring(0, body.length - word.length), word, wordStart: before.length - word.length, quote: false, quoteChar: ''};
}

function stepSuggestions(before: string, caret: number, attributes: SearchAttribute[]): SearchSuggestions {
  const stepStart = before.lastIndexOf('/') + 1;
  const step = before.substring(stepStart);
  if (step.trim().endsWith(']')) {
    return {from: caret, to: caret, items: [
      {label: '/', insert: '/', detail: 'go on to a child node', type: 'operator'},
      {label: '//', insert: '//', detail: 'go on to a node at any depth below', type: 'operator'},
      {label: '[', insert: '[', detail: 'one more condition on this node', type: 'operator'}
    ]};
  }
  const axisTyped = /^\s*[A-Za-z-]+::/.test(step);
  if (axisTyped) {
    return {from: stepStart + step.indexOf('::') + 2, to: caret, items: [{label: '*', insert: '*', detail: 'any node', type: 'snippet'}]};
  }
  if (!step.trim()) {
    const items: SearchSuggestion[] = [{label: '*', insert: '*', detail: 'any node', type: 'snippet'}];
    if (attributes.length > 0) {
      items.push({label: '*[', insert: '*[', detail: 'any node that satisfies a condition', type: 'snippet'});
    }
    if (stepStart > 0) {
      items.push(...axisSuggestions(), {label: '..', insert: '..', detail: 'the parent of the step before', type: 'snippet'});
    }
    return {from: stepStart, to: caret, items};
  }
  if (attributes.length === 0) {
    return NO_SUGGESTIONS;
  }
  return {from: caret, to: caret, items: [
    {label: '[', insert: '[', detail: 'add a condition on the node\'s attributes', type: 'operator'},
    {label: '/', insert: '/', detail: 'go on to a child node', type: 'operator'},
    {label: '//', insert: '//', detail: 'go on to a node at any depth below', type: 'operator'}
  ]};
}

function attributeSuggestions(attributes: SearchAttribute[]): SearchSuggestion[] {
  return attributes.map(a => ({
    label: a.name, insert: a.name + ' ', detail: a.description + (a.type === 'flag' ? ' (true/false)' : '') + (a.source === 'server' ? ' [searched on the server, saved text only]' : ''), type: 'attribute' as const
  }));
}

function functionSuggestions(): SearchSuggestion[] {
  return Object.entries(FUNCTION_PARAMETERS).map(([name, parameters]) => ({
    label: name + '()', insert: name + '()', detail: FUNCTION_DETAILS[name] ?? name, type: 'function' as const,
    // a function that takes arguments is completed with its parentheses and the caret goes between them
    caretBack: parameters.length > 0 ? 1 : undefined
  }));
}

function axisSuggestions(): SearchSuggestion[] {
  return AXIS_DETAILS.map(([axis, detail]) => ({label: axis + '::', insert: axis + '::', detail, type: 'keyword' as const}));
}

/** The short ways to go to the node itself and to its parent. */
function upSuggestions(): SearchSuggestion[] {
  return [
    {label: '..', insert: '..', detail: 'the parent (parent::*), e.g. ../Code: a child of the parent with Code in its name', type: 'snippet'},
    {label: '.', insert: '.', detail: 'the node itself (self::*), e.g. .//Code (a descendant)', type: 'snippet'}
  ];
}

function operatorSuggestions(attribute: SearchAttribute | undefined): SearchSuggestion[] {
  const equalityOnly = attribute?.type === 'flag' || RANGE_ATTRIBUTES.includes(attribute?.name.toLowerCase());
  return COMPARISONS
    .filter(c => c.flag || !equalityOnly)
    .map(c => ({label: c.op, insert: c.op + ' ', detail: c.detail, type: 'operator' as const}));
}

function valueSuggestions(attribute: SearchAttribute | undefined, quoted: boolean, closing = '"'): SearchSuggestion[] {
  if (!attribute) {
    return [];
  }
  if (attribute.type === 'flag') {
    return ['true', 'false'].map(v => ({label: v, insert: v + ' ', detail: attribute.name, type: 'value' as const}));
  }
  return (attribute.values ?? []).flatMap(v => {
    // a text has no escape for its own quote: inside a quoted text only the values without the closing quote can go on
    if (!quoted) {
      return v.includes(closing) ? [] : [{label: v, insert: `${v}${closing}`, detail: attribute.name, type: 'value' as const}];
    }
    const quote = attribute.bare ? '' : ['"', '\''].find(q => !v.includes(q));
    return quote === undefined ? [] : [{label: v, insert: `${quote}${v}${quote} `, detail: attribute.name, type: 'value' as const}];
  });
}

/** What the search bar has to look up before it can offer the values at the caret: the attribute and the text typed of its value. */
export interface ValueLookup {
  attribute: SearchAttribute;
  text: string;
}

/** The lookup the caret asks for (inside the quotes after `attribute =`, or in the text of `contains(attribute, ...)`), when there is one. */
export function valueLookup(expression: string, caret: number, attributes: SearchAttribute[]): ValueLookup | undefined {
  const context = readContext(expression.substring(0, caret));
  const tail = context.predicate ? predicateTail(context.prefix) : undefined;
  const tokens = tail?.tokens ?? [];
  // only a text can come in the argument of a function, so its values are offered as soon as it begins; after an
  // operator the grammar still allows more than a text, so they wait for the quote
  const searched = searchedByText(tokens, attributes)?.attribute;
  const name = context.quote && tokens[tokens.length - 1]?.type === 'Op' ? tokens[tokens.length - 2]?.text.toLowerCase() : undefined;
  const attribute = (name ? attributes.find(a => a.name.toLowerCase() === name) : undefined) ?? searched;
  return attribute?.valueSource ? {attribute, text: context.word} : undefined;
}

/** The attributes with the values found for one of them, ready for `suggestSearch`. */
export function withValues(attributes: SearchAttribute[], attribute: SearchAttribute, values: string[]): SearchAttribute[] {
  return attributes.map(a => a === attribute ? {...a, values} : a);
}

/** Keeps the suggestions that start with (then contain) what has been typed, and leaves out one that is already complete. */
function rank(word: string, wordStart: number, caret: number, items: SearchSuggestion[]): SearchSuggestions {
  const typed = word.toLowerCase();
  const starting = items.filter(i => i.label.toLowerCase().startsWith(typed));
  const containing = typed ? items.filter(i => !i.label.toLowerCase().startsWith(typed) && i.label.toLowerCase().includes(typed)) : [];
  const matches = [...starting, ...containing].filter(i => !(typed && i.label.toLowerCase() === typed && i.insert === i.label));
  return {from: wordStart, to: caret, items: matches};
}

const COMPONENT_TYPES = ['Base', 'Semantics', 'Extension', 'SemanticGroup', 'UserExtensionGroup', 'Embedded', 'OAGIS10Nouns',
  'OAGIS10BODs', 'BOD', 'Verb', 'Noun', 'Choice', 'AttributeGroup'];

/** The columns of the core component a node shows; a component has only its own type's (a test on another's is false). */
const COMPONENT_COLUMNS: SearchAttribute[] = [
  {name: 'object_class_term', type: 'text', description: 'ACC / DT_SC: the object class term'},
  {name: 'property_term', type: 'text', description: 'ASCCP / BCCP / DT_SC: the property term'},
  {name: 'den', type: 'text', description: 'ACC / ASCCP / BCCP / DT: the dictionary entry name'},
  {name: 'data_type_term', type: 'text', description: 'DT: the data type term'},
  {name: 'representation_term', type: 'text', description: 'DT_SC: the representation term'},
  {name: 'qualifier', type: 'text', description: 'DT: the qualifier'},
  {
    name: 'component_type', type: 'text', description: 'ACC: the component type (Extension, SemanticGroup, ...)',
    values: COMPONENT_TYPES
  },
  {name: 'entity_type', type: 'text', description: 'BCCP: whether the BCC is an Element or an Attribute', values: ['Element', 'Attribute']},
  {
    name: 'cardinality', type: 'text', bare: true, values: ['Optional', 'Required', 'Prohibited', '0..1', '1..1', '0..*', '1..*'],
    description: 'ASCC / BCC / DT_SC (BIE: ASBIE / BBIE / BBIE_SC): min..max; = and != only'
  },
  {name: 'cardinality_min', type: 'text', description: 'the minimum of the cardinality, e.g. cardinality_min = 1'},
  {name: 'cardinality_max', type: 'text', description: 'the maximum of the cardinality (unbounded is above any number), e.g. cardinality_max < 10'},
  {name: 'state', type: 'text', description: 'the state of the component (WIP, Draft, Published, ...)'},
  {name: 'guid', type: 'text', description: 'the GUID of the component'},
  {name: 'deprecated', type: 'flag', description: 'the component is deprecated'},
  {name: 'tag', type: 'text', multiple: true, description: 'the tags of the component, e.g. tag = "Core"'}
];

/** The columns of the core components that only the trees of core components show (the BIE trees show them of the BIE). */
const CC_ONLY_COLUMNS: SearchAttribute[] = [
  {name: 'object_class_qualifier', type: 'text', description: 'ACC: the object class qualifier'},
  {name: 'abstract', type: 'flag', description: 'ACC: the component is abstract'},
  {name: 'reusable', type: 'flag', description: 'ASCCP: the component is reusable'},
  {name: 'commonly_used', type: 'flag', description: 'DT: the data type is commonly used'},
  {
    name: 'nillable', type: 'flag',
    description: 'ASCCP / BCCP / BCC: the component, or the association that ties the node to its parent, is nillable'
  }
];

/** The definitions of the core components a row of the component tree (or the model browser) shows. */
const CC_DEFINITION_COLUMNS: SearchAttribute[] = [
  {
    name: 'definition', type: 'text', source: 'server',
    description: 'the definition of what the node is: its ASCC or BCC when it is tied to its parent by one, else its component; only the ones that have one are found, and = and != compare the whole text'
  },
  {name: 'definition_source', type: 'text', source: 'server', description: 'the source of that definition'},
  {
    name: 'association_definition', type: 'text', source: 'server',
    description: 'ASCCP / BCCP: the definition of the ASCC or BCC that ties it to its parent'
  },
  {name: 'component_definition', type: 'text', source: 'server', description: 'ASCCP / BCCP: the definition of the ASCCP or BCCP'},
  {name: 'type_definition', type: 'text', source: 'server', description: 'BCCP: the definition of its data type'},
  {
    name: 'default_value', type: 'text', source: 'server',
    description: 'BCC / BCCP / DT_SC: the default value (the BCC\'s when the node is tied to its parent by one)'
  },
  {
    name: 'fixed_value', type: 'text', source: 'server',
    description: 'BCC / BCCP / DT_SC: the fixed value (the BCC\'s when the node is tied to its parent by one)'
  },
  {name: 'content_component_definition', type: 'text', source: 'server', description: 'DT: the definition of the content component'}
];

/** The attributes of the nodes of a core component tree (the ACC, ASCCP, BCCP, DT and extension screens). */
export const COMPONENT_SEARCH_ATTRIBUTES: SearchAttribute[] = [
  {name: 'type', type: 'text', description: 'the type of node', values: ['ACC', 'ASCCP', 'BCCP', 'DT', 'DT_SC']},
  ...COMPONENT_COLUMNS,
  ...CC_ONLY_COLUMNS,
  ...CC_DEFINITION_COLUMNS
];

/** The attributes of the nodes of a tree that shows core components the way a BIE is shown (the model browser). */
export const MODEL_BROWSER_SEARCH_ATTRIBUTES: SearchAttribute[] = [
  {name: 'type', type: 'text', description: 'the type of node', values: ['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC']},
  ...COMPONENT_COLUMNS,
  ...CC_ONLY_COLUMNS,
  ...CC_DEFINITION_COLUMNS
];

/** The attributes of the nodes of a BIE tree. */
export const BIE_SEARCH_ATTRIBUTES: SearchAttribute[] = [
  {name: 'type', type: 'text', description: 'the type of node', values: ['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC']},
  {name: 'display_name', type: 'text', description: 'ASBIEP / BBIEP / BBIE_SC: the display name set on the BIE'},
  {
    name: 'definition', type: 'text', source: 'server',
    description: 'the definition set on the BIE (its Context Definition); only the BIEs that have one are found, and = and != compare the whole text'
  },
  {name: 'remark', type: 'text', source: 'server', description: 'the remark set on the BIE'},
  {name: 'example', type: 'text', source: 'server', description: 'BBIEP / BBIE_SC: the example set on the BIE'},
  {name: 'fixed_value', type: 'text', source: 'server', description: 'BBIEP / BBIE_SC: the fixed value set on the BIE'},
  {name: 'default_value', type: 'text', source: 'server', description: 'BBIEP / BBIE_SC: the default value set on the BIE'},
  {
    name: 'business_term', type: 'text', source: 'server', multiple: true,
    description: 'the business terms of the BIE: the assigned ones when Business Term is enabled, else the business term text set on it'
  },
  {
    name: 'value_domain_restriction', type: 'text', source: 'server', values: ['Primitive', 'Code List', 'Agency ID List'],
    description: 'BBIEP / BBIE_SC: the type of value domain the BIE restricts its value to'
  },
  {
    name: 'value_domain', type: 'text', source: 'server',
    description: 'BBIEP / BBIE_SC: the name of the primitive, code list or agency ID list the BIE restricts its value to; without value_domain_restriction all three are searched'
  },
  {name: 'association_definition', type: 'text', source: 'server', description: 'ASBIEP / BBIEP: the definition of the ASCC or BCC that ties it to its parent'},
  {name: 'component_definition', type: 'text', source: 'server', description: 'ABIE / ASBIEP / BBIEP / BBIE_SC: the definition of the ASCCP, BCCP or DT_SC it profiles'},
  {name: 'type_definition', type: 'text', source: 'server', description: 'ABIE / ASBIEP: the definition of its ACC'},
  ...COMPONENT_COLUMNS.map(column => column.name === 'deprecated' ?
    {...column, description: 'the BIE or the core component it profiles is deprecated'} : column),
  {
    name: 'nillable', type: 'flag',
    description: 'ASBIEP / BBIEP: the BIE is nillable, or the core component (or the association that ties the node to its parent) is'
  },
  {name: 'used', type: 'flag', description: 'the node is used in the BIE'},
  {name: 'inherited', type: 'flag', description: 'the node is inherited'},
  {name: 'reused', type: 'flag', description: 'the node is a reused BIE'}
];
