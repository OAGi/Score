import {SEARCH_FUNCTIONS} from './search-expression';

const AXIS_DETAILS: [string, string][] = [
  ['child', 'the children (what / goes to)'],
  ['descendant', 'the nodes below at any depth (what // goes to)'],
  ['descendant-or-self', 'the node itself and the nodes below'],
  ['self', 'the node itself'],
  ['parent', 'the parent (.. for short)'],
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
  'ends-with': 'ends-with(attribute, "text"): the value ends with the text'
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
    {label: '*[parent::*]', insert: '*[parent::*]', detail: 'by parent, ancestor::, child::, descendant::, following-sibling::, preceding-sibling::', kind: 'snippet'},
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

  const {tokens, word, wordStart, quote} = context;
  if (quote) {
    const values = valueSuggestions(attributeOf(tokens[tokens.length - 2]?.text), false);
    return rank(word, wordStart, caret, values);
  }

  const last = tokens[tokens.length - 1];
  let items: SearchSuggestion[];
  if (last?.kind === 'op') {
    const values = valueSuggestions(attributeOf(tokens[tokens.length - 2]?.text), true);
    items = values.length > 0 ? values : attributeSuggestions(attributes);
  } else if (last?.text === ':' && tokens[tokens.length - 2]?.text === ':') {
    items = [{label: '*', insert: '*', detail: 'any node', kind: 'snippet'}];
  } else if (last?.text === '/' || last?.text === '//') {
    items = [{label: '*', insert: '*', detail: 'any node', kind: 'snippet'}, ...axisSuggestions(), ...upSuggestions()];
  } else if (!last || isOperatorWord(last) || ['(', ',', '['].includes(last.text)) {
    items = [
      ...attributeSuggestions(attributes),
      ...functionSuggestions(),
      ...axisSuggestions(),
      ...upSuggestions(),
      {label: 'not', insert: 'not ', detail: 'negate the condition', kind: 'keyword'}
    ];
  } else if (last.kind === 'ident' && attributeOf(last.text)) {
    items = [...operatorSuggestions(attributeOf(last.text)), ...joinSuggestions()];
  } else {
    items = joinSuggestions();
  }
  return rank(word, wordStart, caret, items);
}

interface Lexeme {
  kind: 'ident' | 'string' | 'number' | 'op' | 'punct';
  text: string;
}

interface Context {
  /** Whether the caret is inside `[...]`. */
  predicate: boolean;
  /** The complete lexemes of the predicate before the word being typed. */
  tokens: Lexeme[];
  /** The word being typed (what a completion replaces). */
  word: string;
  wordStart: number;
  /** Whether the caret is inside a quoted text. */
  quote: boolean;
}

/** Reads the part of the expression before the caret: where in a predicate it ends and what has been written so far. */
function readContext(before: string): Context {
  let depth = 0;
  let quote = '';
  let predicateStart = -1;
  for (let i = 0; i < before.length; i++) {
    const c = before[i];
    if (quote) {
      if (c === quote) {
        quote = '';
      }
    } else if (depth > 0 && (c === '"' || c === '\'')) {
      quote = c;
    } else if (c === '[') {
      if (depth++ === 0) {
        predicateStart = i + 1;
      }
    } else if (c === ']' && depth > 0) {
      depth--;
    }
  }
  if (depth === 0) {
    return {predicate: false, tokens: [], word: '', wordStart: before.length, quote: false};
  }

  const body = before.substring(predicateStart);
  const tokens: Lexeme[] = [];
  let i = 0;
  let word = '';
  let wordStart = before.length;
  let inQuote = false;
  while (i < body.length) {
    const c = body[i];
    if (/\s/.test(c)) {
      i++;
    } else if (c === '"' || c === '\'') {
      const end = body.indexOf(c, i + 1);
      if (end < 0) {
        inQuote = true;
        word = body.substring(i + 1);
        wordStart = predicateStart + i + 1;
        i = body.length;
      } else {
        tokens.push({kind: 'string', text: body.substring(i + 1, end)});
        i = end + 1;
      }
    } else if (/[0-9]/.test(c) || (c === '-' && /[0-9]/.test(body[i + 1] || ''))) {
      const m = /^-?[0-9]+(\.[0-9]+)?/.exec(body.substring(i));
      tokens.push({kind: 'number', text: m[0]});
      i += m[0].length;
    } else if (/[A-Za-z_]/.test(c)) {
      const m = /^[A-Za-z_][A-Za-z0-9_-]*/.exec(body.substring(i));
      i += m[0].length;
      if (i >= body.length) {
        // the word under the caret: a completion replaces it
        word = m[0];
        wordStart = predicateStart + i - m[0].length;
      } else {
        tokens.push({kind: 'ident', text: m[0]});
      }
    } else if (c === '!' || c === '<' || c === '>' || c === '=') {
      const two = body.substr(i, 2);
      const op = two === '!=' || two === '<=' || two === '>=' ? two : c;
      tokens.push({kind: 'op', text: op});
      i += op.length;
    } else {
      tokens.push({kind: 'punct', text: c});
      i++;
    }
  }
  return {predicate: true, tokens, word, wordStart, quote: inQuote};
}

function isOperatorWord(token: Lexeme | string): boolean {
  const text = (typeof token === 'string' ? token : token.text).toLowerCase();
  return text === 'and' || text === 'or' || text === 'not';
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
  return [
    ...SEARCH_FUNCTIONS.map(f => ({label: f, insert: f + '(', detail: FUNCTION_DETAILS[f] ?? f, kind: 'function' as const})),
    {label: 'text()', insert: 'text()', detail: 'the text the node shows in the tree, e.g. contains(text(), "id")', kind: 'function'},
    {label: 'position()', insert: 'position()', detail: 'the place among the siblings the step selects (1 is first); [2] is [position() = 2]', kind: 'function'},
    {label: 'last()', insert: 'last()', detail: 'how many siblings the step selects; [last()] is the last one', kind: 'function'},
    {label: 'count(', insert: 'count(', detail: 'count(path): how many nodes the path selects, e.g. count(child::*) > 3', kind: 'function'}
  ];
}

function axisSuggestions(): SearchSuggestion[] {
  return AXIS_DETAILS.map(([axis, detail]) => ({label: axis + '::', insert: axis + '::', detail, kind: 'keyword' as const}));
}

/** The short ways to go to the node itself and to its parent. */
function upSuggestions(): SearchSuggestion[] {
  return [
    {label: '..', insert: '..', detail: 'the parent, e.g. ../Code', kind: 'snippet'},
    {label: '.', insert: '.', detail: 'the node itself, e.g. .//Code (a descendant)', kind: 'snippet'}
  ];
}

function operatorSuggestions(attribute: SearchAttribute | undefined): SearchSuggestion[] {
  const flagOnly = attribute?.kind === 'flag';
  return COMPARISONS
    .filter(c => c.flag || !flagOnly)
    .map(c => ({label: c.op, insert: c.op + ' ', detail: c.detail, kind: 'operator' as const}));
}

function joinSuggestions(): SearchSuggestion[] {
  return [
    {label: 'and', insert: 'and ', detail: 'both conditions hold', kind: 'keyword'},
    {label: 'or', insert: 'or ', detail: 'either condition holds', kind: 'keyword'},
    {label: ']', insert: ']', detail: 'end of the condition', kind: 'operator'}
  ];
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
