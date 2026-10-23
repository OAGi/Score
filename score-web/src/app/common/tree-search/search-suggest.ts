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
  kind: 'text' | 'flag';
  description: string;
  /** The values worth offering after `name =`, when the attribute has a fixed set of them. */
  values?: string[];
  /** Whether the values are written without quotes (`cardinality = 0..1`). */
  bare?: boolean;
}

/** One completion: replace `[from, to)` of the expression with `insert`. */
export interface SearchSuggestion {
  label: string;
  insert: string;
  detail: string;
  kind: 'attribute' | 'function' | 'operator' | 'keyword' | 'value' | 'snippet';
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
  kind: 'value' | 'text' | 'path';
  detail: string;
}

const TEXT_FUNCTION_PARAMETERS: FunctionParameter[] = [
  {kind: 'value', detail: 'the attribute (or text()) whose value is searched'},
  {kind: 'text', detail: 'the text to look for in it'}
];

/** The arguments each function takes, in order; a function without any is completed with its parentheses. */
const FUNCTION_PARAMETERS: Record<string, FunctionParameter[]> = {
  ...Object.fromEntries(SEARCH_FUNCTIONS.map(f => [f, TEXT_FUNCTION_PARAMETERS])),
  'text': [],
  'position': [],
  'last': [],
  'count': [{kind: 'path', detail: 'the path whose nodes are counted: * for every child, or e.g. Code, ../Code, descendant-or-self::Code'}]
};

/** What the field offers while it is empty: examples of the expressions it understands. */
export function searchSyntaxHints(attributes: SearchAttribute[]): SearchSuggestion[] {
  const flag = attributes.find(a => a.kind === 'flag');
  const hints: SearchSuggestion[] = [
    {label: '*', insert: '*', detail: 'any node; Parent/Child searches a path, a leading / anchors it at the root, // reaches any depth', kind: 'snippet'}
  ];
  if (flag) {
    hints.push({label: `*[${flag.name} = true]`, insert: `*[${flag.name} = true]`, detail: 'nodes by attribute', kind: 'snippet'});
  }
  if (attributes.length > 0) {
    const example = '*[contains(text(), "id")]';
    hints.push({label: example, insert: example, detail: 'text() is the text the node shows; combine conditions with and, or, not', kind: 'snippet'});
  }
  hints.push(
    {label: '*[../Code]', insert: '*[../Code]', detail: 'a node whose parent has a child with Code in its name (so it and its siblings); also ancestor::, following-sibling::, preceding-sibling::', kind: 'snippet'},
    {label: '*[2]', insert: '*[2]', detail: 'the 2nd of the siblings; also position() and last()', kind: 'snippet'}
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

  const {prefix, word, wordStart, quote} = context;
  const tail = predicateTail(prefix);
  if (!tail) {
    return NO_SUGGESTIONS;
  }
  const last = tail.tokens[tail.tokens.length - 1];
  if (quote) {
    const values = last?.type === 'Op' ? valueSuggestions(attributeOf(tail.tokens[tail.tokens.length - 2]?.text), false) : [];
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

  if (afterOp) {
    const values = valueSuggestions(attributeOf(beforeLast?.text), true);
    if (values.length > 0) {
      return values;
    }
  }
  // a comparison is made with a value, never with a path: after an operator only values and functions make sense
  if (next.has('name') && !inPath && !afterOp) {
    items.push(...attributeSuggestions(attributes));
  }
  if (next.has('call')) {
    items.push(...functionSuggestions());
  }
  if (next.has('axis') && !afterOp) {
    items.push(...axisSuggestions());
  }
  if (next.has('any') && !afterOp) {
    items.push({label: '*', insert: '*', detail: 'any node', kind: 'snippet'});
  }
  if (last?.type === 'DotDot' && beforeLast?.type === 'NumberLit') {
    return [{label: '*', insert: '*', detail: 'no upper limit, e.g. 1..*', kind: 'snippet'}];
  }
  if (next.has('parent') && !afterOp) {
    items.push(last?.type === 'NumberLit' ?
      {label: '..', insert: '..', detail: 'a range, e.g. 0..1 or 1..*', kind: 'snippet'} : upSuggestions()[0]);
  }
  if (next.has('self') && !afterOp) {
    items.push(upSuggestions()[1]);
  }
  if (next.has('name') && !afterOp && !inPath) {
    items.push({label: 'not', insert: 'not ', detail: 'negate the condition', kind: 'keyword'});
  }
  const known = last?.type === 'Ident' ? attributeOf(last.text) : undefined;
  if (next.has('op') && (known || last?.type === 'RParen')) {
    items.push(...operatorSuggestions(known));
  }
  if (!next.has('name')) {
    for (const [kind, label, detail] of JOINS) {
      if (next.has(kind)) {
        items.push({label, insert: label + ' ', detail, kind: 'keyword'});
      }
    }
  }
  // a path goes on after a step (a name after / or ::, *, . or ..), not after an attribute, a value or a keyword
  const afterStep = ['Star', 'Dot', 'DotDot', 'RBracket'].includes(last?.type) && beforeLast?.type !== 'DotDot'
    || last?.type === 'Ident' && ['Slash', 'SlashSlash', 'DoubleColon'].includes(beforeLast?.type);
  if (afterStep) {
    if (next.has('slash')) {
      items.push({label: '/', insert: '/', detail: 'go on to a child node', kind: 'operator'});
    }
    if (next.has('descendant')) {
      items.push({label: '//', insert: '//', detail: 'go on to a node at any depth below', kind: 'operator'});
    }
    if (next.has('predicate')) {
      items.push({label: '[', insert: '[', detail: 'one more condition on this node', kind: 'operator'});
    }
  }
  if (next.has('comma')) {
    items.push({label: ',', insert: ', ', detail: 'the next argument', kind: 'operator'});
  }
  // the `)` of `count()` is there already when the caret is between the parentheses
  if (next.has('closeParen') && !after.trimStart().startsWith(')')) {
    items.push({label: ')', insert: ')', detail: 'end of the arguments or of the group', kind: 'operator'});
  }
  if (complete && !next.has('name') || next.has('closeBracket')) {
    items.push({label: ']', insert: ']', detail: 'end of the condition', kind: 'operator'});
  }
  const call = last?.type === 'LParen' || last?.type === 'Comma' ? enclosingCall(tokens) : undefined;
  const parameter = call ? FUNCTION_PARAMETERS[call.name.toLowerCase()]?.[call.index] : undefined;
  return parameter ? argumentSuggestions(parameter, items, tokens, call.index, attributes) : items;
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

/** What may be written where a function's argument begins: only what the argument is (a path, an attribute, a text). */
function argumentSuggestions(parameter: FunctionParameter, items: SearchSuggestion[], tokens: {type: string, text: string}[],
                             index: number, attributes: SearchAttribute[]): SearchSuggestion[] {
  switch (parameter.kind) {
    case 'path':
      return items.filter(i => i.label.endsWith('::') || ['*', '..', '.'].includes(i.label))
        .map(i => i.label === '*' ? {...i, detail: parameter.detail} : i);
    case 'value':
      return items.filter(i => i.kind === 'attribute' || i.label === 'text()');
    default: {
      // the text a function looks for: the values of the attribute it searches, when it has a fixed set of them
      const searched = attributes.find(a => a.name.toLowerCase() === tokens[tokens.length - 2]?.text.toLowerCase());
      const values = (searched?.values ?? []).map(v => ({label: v, insert: `"${v}"`, detail: searched.name, kind: 'value' as const}));
      return [{label: '"text"', insert: '""', detail: parameter.detail, kind: 'snippet', caretBack: 1}, ...values];
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
    return {predicate: false, prefix: '', word: '', wordStart: before.length, quote: false};
  }
  if (quote) {
    return {
      predicate: true, prefix: before.substring(predicateStart, quoteStart), word: before.substring(quoteStart + 1),
      wordStart: quoteStart + 1, quote: true
    };
  }
  const body = before.substring(predicateStart);
  const word = /[A-Za-z0-9_-]*$/.exec(body)[0];
  return {predicate: true, prefix: body.substring(0, body.length - word.length), word, wordStart: before.length - word.length, quote: false};
}

function stepSuggestions(before: string, caret: number, attributes: SearchAttribute[]): SearchSuggestions {
  const stepStart = before.lastIndexOf('/') + 1;
  const step = before.substring(stepStart);
  if (step.trim().endsWith(']')) {
    return {from: caret, to: caret, items: [
      {label: '/', insert: '/', detail: 'go on to a child node', kind: 'operator'},
      {label: '//', insert: '//', detail: 'go on to a node at any depth below', kind: 'operator'},
      {label: '[', insert: '[', detail: 'one more condition on this node', kind: 'operator'}
    ]};
  }
  const axisTyped = /^\s*[A-Za-z-]+::/.test(step);
  if (axisTyped) {
    return {from: stepStart + step.indexOf('::') + 2, to: caret, items: [{label: '*', insert: '*', detail: 'any node', kind: 'snippet'}]};
  }
  if (!step.trim()) {
    const items: SearchSuggestion[] = [{label: '*', insert: '*', detail: 'any node', kind: 'snippet'}];
    if (attributes.length > 0) {
      items.push({label: '*[', insert: '*[', detail: 'any node that satisfies a condition', kind: 'snippet'});
    }
    if (stepStart > 0) {
      items.push(...axisSuggestions(), {label: '..', insert: '..', detail: 'the parent of the step before', kind: 'snippet'});
    }
    return {from: stepStart, to: caret, items};
  }
  if (attributes.length === 0) {
    return NO_SUGGESTIONS;
  }
  return {from: caret, to: caret, items: [
    {label: '[', insert: '[', detail: 'add a condition on the node\'s attributes', kind: 'operator'},
    {label: '/', insert: '/', detail: 'go on to a child node', kind: 'operator'},
    {label: '//', insert: '//', detail: 'go on to a node at any depth below', kind: 'operator'}
  ]};
}

function attributeSuggestions(attributes: SearchAttribute[]): SearchSuggestion[] {
  return attributes.map(a => ({
    label: a.name, insert: a.name + ' ', detail: a.description + (a.kind === 'flag' ? ' (true/false)' : ''), kind: 'attribute' as const
  }));
}

function functionSuggestions(): SearchSuggestion[] {
  return Object.entries(FUNCTION_PARAMETERS).map(([name, parameters]) => ({
    label: name + '()', insert: name + '()', detail: FUNCTION_DETAILS[name] ?? name, kind: 'function' as const,
    // a function that takes arguments is completed with its parentheses and the caret goes between them
    caretBack: parameters.length > 0 ? 1 : undefined
  }));
}

function axisSuggestions(): SearchSuggestion[] {
  return AXIS_DETAILS.map(([axis, detail]) => ({label: axis + '::', insert: axis + '::', detail, kind: 'keyword' as const}));
}

/** The short ways to go to the node itself and to its parent. */
function upSuggestions(): SearchSuggestion[] {
  return [
    {label: '..', insert: '..', detail: 'the parent (parent::*), e.g. ../Code: a child of the parent with Code in its name', kind: 'snippet'},
    {label: '.', insert: '.', detail: 'the node itself (self::*), e.g. .//Code (a descendant)', kind: 'snippet'}
  ];
}

function operatorSuggestions(attribute: SearchAttribute | undefined): SearchSuggestion[] {
  const equalityOnly = attribute?.kind === 'flag' || RANGE_ATTRIBUTES.includes(attribute?.name.toLowerCase());
  return COMPARISONS
    .filter(c => c.flag || !equalityOnly)
    .map(c => ({label: c.op, insert: c.op + ' ', detail: c.detail, kind: 'operator' as const}));
}

function valueSuggestions(attribute: SearchAttribute | undefined, quoted: boolean): SearchSuggestion[] {
  if (!attribute) {
    return [];
  }
  if (attribute.kind === 'flag') {
    return ['true', 'false'].map(v => ({label: v, insert: v + ' ', detail: attribute.name, kind: 'value' as const}));
  }
  return (attribute.values ?? []).map(v => ({
    label: v, insert: quoted ? (attribute.bare ? `${v} ` : `"${v}" `) : `${v}"`, detail: attribute.name, kind: 'value' as const
  }));
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

/** The columns of the core component a node shows; a component has only its own kind's (a test on another's is false). */
const COMPONENT_COLUMNS: SearchAttribute[] = [
  {name: 'object_class_term', kind: 'text', description: 'ACC / DT_SC: the object class term'},
  {name: 'property_term', kind: 'text', description: 'ASCCP / BCCP / DT_SC: the property term'},
  {name: 'den', kind: 'text', description: 'ACC / ASCCP / BCCP / DT: the dictionary entry name'},
  {name: 'data_type_term', kind: 'text', description: 'DT: the data type term'},
  {name: 'representation_term', kind: 'text', description: 'DT_SC: the representation term'},
  {name: 'qualifier', kind: 'text', description: 'DT: the qualifier'},
  {
    name: 'component_type', kind: 'text', description: 'ACC: the component type (Extension, SemanticGroup, ...)',
    values: COMPONENT_TYPES
  },
  {name: 'entity_type', kind: 'text', description: 'BCCP: whether the BCC is an Element or an Attribute', values: ['Element', 'Attribute']},
  {
    name: 'cardinality', kind: 'text', bare: true, values: ['Optional', 'Required', 'Prohibited', '0..1', '1..1', '0..*', '1..*'],
    description: 'ASCC / BCC / DT_SC (BIE: ASBIE / BBIE / BBIE_SC): min..max; = and != only'
  },
  {name: 'cardinality_min', kind: 'text', description: 'the minimum of the cardinality, e.g. cardinality_min = 1'},
  {name: 'cardinality_max', kind: 'text', description: 'the maximum of the cardinality (unbounded is above any number), e.g. cardinality_max < 10'},
  {name: 'state', kind: 'text', description: 'the state of the component (WIP, Draft, Published, ...)'},
  {name: 'guid', kind: 'text', description: 'the GUID of the component'},
  {name: 'deprecated', kind: 'flag', description: 'the component is deprecated'}
];

/** The attributes of the nodes of a core component tree (the ACC, ASCCP, BCCP, DT and extension screens). */
export const COMPONENT_SEARCH_ATTRIBUTES: SearchAttribute[] = [
  {name: 'type', kind: 'text', description: 'the kind of node', values: ['ACC', 'ASCCP', 'BCCP', 'DT', 'DT_SC']},
  ...COMPONENT_COLUMNS
];

/** The attributes of the nodes of a tree that shows core components the way a BIE is shown (the model browser). */
export const MODEL_BROWSER_SEARCH_ATTRIBUTES: SearchAttribute[] = [
  {name: 'type', kind: 'text', description: 'the kind of node', values: ['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC']},
  ...COMPONENT_COLUMNS
];

/** The attributes of the nodes of a BIE tree. */
export const BIE_SEARCH_ATTRIBUTES: SearchAttribute[] = [
  {name: 'type', kind: 'text', description: 'the kind of node', values: ['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC']},
  {name: 'display_name', kind: 'text', description: 'ASBIEP / BBIEP / BBIE_SC: the display name set on the BIE'},
  ...COMPONENT_COLUMNS,
  {name: 'used', kind: 'flag', description: 'the node is used in the BIE'},
  {name: 'inherited', kind: 'flag', description: 'the node is inherited'},
  {name: 'reused', kind: 'flag', description: 'the node is a reused BIE'}
];
