import {parseSearchExpression} from './search-expression';
import {PathPattern} from './path-pattern';
import {predicateTail} from './search-grammar';
import {BIE_SEARCH_ATTRIBUTES, COMPONENT_SEARCH_ATTRIBUTES, MODEL_BROWSER_SEARCH_ATTRIBUTES, SearchAttribute, suggestSearch} from './search-suggest';

/**
 * Every combination of the parts of a search expression, each judged against an answer worked out here without the parser:
 * comparisons, cardinalities, boolean trees, paths, and what the field suggests (every suggestion, followed to the end).
 */

type Node = {name: string, [attribute: string]: string | number | boolean | undefined};

/** Whether a node, alone in its tree, is selected by an expression that asks about its attributes only. */
function selects(expr: string, node: Node): boolean {
  const pattern = new PathPattern(expr);
  if (pattern.error) {
    throw new Error(expr + ': ' + pattern.error.message);
  }
  const mask = pattern.stepAll([[node.name]], [(attribute: string) => node[attribute]]);
  return pattern.isMatch(mask);
}

function errorOf(expr: string): string | undefined {
  return parseSearchExpression(expr).error?.message;
}

// ---- comparisons -----------------------------------------------------------------------------------------------------

const OPERATORS = ['=', '!=', '<', '<=', '>', '>='] as const;
type Operator = typeof OPERATORS[number];

const NODE: Node = {name: 'Code', type: 'ACC', state: 'Published', used: true, deprecated: false, qty: 5, ratio: 2.5, guid: '6f3a', empty: ''};

/** How a value is written and what it is: a number, a flag or a text. */
const VALUES: {written: string, value: string | number | boolean}[] = [
  {written: 'ACC', value: 'ACC'}, {written: 'acc', value: 'acc'}, {written: '"ACC"', value: 'ACC'}, {written: '\'ACC\'', value: 'ACC'},
  {written: 'ASCCP', value: 'ASCCP'}, {written: '"Published"', value: 'Published'}, {written: '"published"', value: 'published'},
  {written: '5', value: 5}, {written: '5.0', value: 5}, {written: '4', value: 4}, {written: '6', value: 6}, {written: '2.5', value: 2.5},
  {written: '-1', value: -1}, {written: '"5"', value: '5'}, {written: 'true', value: true}, {written: 'false', value: false},
  {written: 'TRUE', value: true}, {written: '"6f3a"', value: '6f3a'}, {written: '6f3a', value: '6f3a'}, {written: '6', value: 6}
];

const ATTRIBUTES: string[] = ['type', 'state', 'used', 'deprecated', 'qty', 'ratio', 'guid', 'empty', 'missing'];

/** The order of two values as the search defines it: numbers by number, anything else as text without regard to case. */
function oracle(attribute: string, operator: Operator, value: string | number | boolean): boolean {
  const actual = NODE[attribute];
  if (actual === undefined) {
    return false;
  }
  // what a GUID is compared with is text, even when it is a number
  const wanted = attribute === 'guid' ? String(value) : value;
  const isNumber = (x: unknown) => typeof x === 'number';
  const numeric = (x: unknown) => typeof x === 'number' || typeof x === 'string' && /^-?\d+(\.\d+)?$/.test(x);
  let order: number;
  if (isNumber(actual) && numeric(wanted) || isNumber(wanted) && numeric(actual)) {
    order = Math.sign(Number(actual) - Number(wanted));
  } else {
    const a = String(actual).toLowerCase();
    const b = String(wanted).toLowerCase();
    order = a < b ? -1 : a > b ? 1 : 0;
  }
  switch (operator) {
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

describe('every attribute compared with every operator and every value', () => {
  for (const attribute of ATTRIBUTES) {
    for (const operator of OPERATORS) {
      it(`${attribute} ${operator} ...`, () => {
        for (const {written, value} of VALUES) {
          for (const spacing of [' ', '']) {
            const expr = `*[${attribute}${spacing}${operator}${spacing}${written}]`;
            expect(selects(expr, NODE), expr).toBe(oracle(attribute, operator, value));
          }
        }
      });
    }
  }

  it('a flag alone is its truth, and a missing or empty attribute is false', () => {
    expect(selects('*[used]', NODE)).toBe(true);
    expect(selects('*[deprecated]', NODE)).toBe(false);
    expect(selects('*[missing]', NODE)).toBe(false);
    expect(selects('*[empty]', NODE)).toBe(false);
    expect(selects('*[qty]', NODE)).toBe(true);
    expect(selects('*[qty]', {...NODE, qty: 0})).toBe(false);
  });

  it('a word that starts with a digit is text, and a guid is text whatever it looks like', () => {
    for (const guid of ['6f3a', '0123', '00000000000000000000000000000001', '1e5', '12ab-9']) {
      const node = {...NODE, guid};
      expect(selects(`*[guid = ${guid}]`, node), guid).toBe(true);
      expect(selects(`*[guid = "${guid}"]`, node), guid).toBe(true);
      expect(selects(`*[guid != ${guid}]`, node), guid).toBe(false);
    }
    expect(selects('*[guid = 1]', {...NODE, guid: '00000000000000000000000000000001'})).toBe(false);
  });
});

// ---- cardinality -----------------------------------------------------------------------------------------------------

const RANGES: {written: string, min: number, max: number}[] = [
  {written: '0..1', min: 0, max: 1}, {written: '1..1', min: 1, max: 1}, {written: '0..0', min: 0, max: 0}, {written: '0..*', min: 0, max: Infinity},
  {written: '1..*', min: 1, max: Infinity}, {written: '0..unbounded', min: 0, max: Infinity}, {written: '1..UNBOUNDED', min: 1, max: Infinity},
  {written: '0..∞', min: 0, max: Infinity}, {written: '2..5', min: 2, max: 5}, {written: '"0..1"', min: 0, max: 1},
  {written: '"1..*"', min: 1, max: Infinity}, {written: '"1 .. 2"', min: 1, max: 2}, {written: 'Optional', min: 0, max: 1},
  {written: 'optional', min: 0, max: 1}, {written: 'Required', min: 1, max: 1}, {written: 'REQUIRED', min: 1, max: 1},
  {written: 'Prohibited', min: 0, max: 0}, {written: '"Optional"', min: 0, max: 1}
];

const CARDINALITIES: {min: number, max: number}[] = [{min: 0, max: 1}, {min: 1, max: 1}, {min: 0, max: 0}, {min: 0, max: Infinity},
  {min: 1, max: Infinity}, {min: 2, max: 5}, {min: 1, max: 2}];

describe('every cardinality written every way, against every cardinality', () => {
  for (const range of RANGES) {
    it(`cardinality = / != ${range.written}`, () => {
      for (const actual of CARDINALITIES) {
        const node = {...NODE, cardinality_min: actual.min, cardinality_max: actual.max};
        const same = actual.min === range.min && actual.max === range.max;
        expect(selects(`*[cardinality = ${range.written}]`, node), `= ${range.written} on ${actual.min}..${actual.max}`).toBe(same);
        expect(selects(`*[cardinality != ${range.written}]`, node), `!= ${range.written} on ${actual.min}..${actual.max}`).toBe(!same);
        expect(selects(`*[not(cardinality = ${range.written})]`, node)).toBe(!same);
      }
    });
  }

  it('is false for a node that has no cardinality, whichever way it is compared', () => {
    expect(selects('*[cardinality = 0..1]', NODE)).toBe(false);
    expect(selects('*[cardinality != 0..1]', NODE)).toBe(false);
    expect(selects('*[not(cardinality = 0..1)]', NODE)).toBe(true);
  });

  for (const operator of ['<', '<=', '>', '>=']) {
    for (const range of ['0..1', 'Optional', '"0..1"', '1..*']) {
      it(`rejects cardinality ${operator} ${range}`, () => {
        expect(errorOf(`*[cardinality ${operator} ${range}]`)).toContain('= and != only');
      });
    }
  }

  for (const bad of ['5', '0..', '..1', 'Maybe', '"Maybe"', '"0..x"', 'x..1', '0..x', '0...1', 'Optional..1', '"0..1"..2']) {
    it(`rejects cardinality = ${bad}`, () => {
      expect(errorOf(`*[cardinality = ${bad}]`), bad).toBeDefined();
    });
  }

  it('compares the minimum and the maximum as numbers, an unbounded maximum being above every number', () => {
    for (const actual of CARDINALITIES) {
      const node = {...NODE, cardinality_min: actual.min, cardinality_max: actual.max};
      for (const operator of OPERATORS) {
        for (const bound of [0, 1, 2, 5, 10]) {
          expect(selects(`*[cardinality_min ${operator} ${bound}]`, node), `min ${operator} ${bound}`).toBe(oracleNumber(actual.min, operator, bound));
          expect(selects(`*[cardinality_max ${operator} ${bound}]`, node), `max ${operator} ${bound}`).toBe(oracleNumber(actual.max, operator, bound));
        }
      }
      expect(selects('*[cardinality_max = unbounded]', node)).toBe(actual.max === Infinity);
      expect(selects('*[cardinality_max = "*"]', node)).toBe(actual.max === Infinity);
    }
  });

  function oracleNumber(actual: number, operator: Operator, bound: number): boolean {
    switch (operator) {
      case '=':
        return actual === bound;
      case '!=':
        return actual !== bound;
      case '<':
        return actual < bound;
      case '<=':
        return actual <= bound;
      case '>':
        return actual > bound;
      default:
        return actual >= bound;
    }
  }
});

// ---- boolean trees ---------------------------------------------------------------------------------------------------

type Tree = {atom: number} | {not: Tree, style: number} | {and: [Tree, Tree]} | {or: [Tree, Tree]};

const ATOMS = ['used', 'type = ACC', 'deprecated', 'qty > 9'];

const FACTS: Node[] = [
  {name: 'A', used: true, type: 'ACC', deprecated: false, qty: 5},
  {name: 'B', used: false, type: 'ACC', deprecated: true, qty: 12},
  {name: 'C', used: true, type: 'BCC', deprecated: true, qty: 12},
  {name: 'D', used: false, type: 'BCC', deprecated: false, qty: 0}
];

function trees(depth: number): Tree[] {
  const atoms: Tree[] = ATOMS.map((_, atom) => ({atom}));
  if (depth === 0) {
    return atoms;
  }
  const smaller = trees(depth - 1);
  const built: Tree[] = [...smaller];
  smaller.forEach(t => [0, 1, 2].forEach(style => built.push({not: t, style})));
  for (const a of smaller) {
    for (const b of smaller) {
      built.push({and: [a, b]}, {or: [a, b]});
    }
  }
  return built;
}

function write(tree: Tree): string {
  if ('atom' in tree) {
    return ATOMS[tree.atom];
  }
  if ('not' in tree) {
    const inner = write(tree.not);
    return [`not (${inner})`, `not(${inner})`, `not ((${inner}))`][tree.style];
  }
  return 'and' in tree ? `(${write(tree.and[0])} and ${write(tree.and[1])})` : `(${write(tree.or[0])} or ${write(tree.or[1])})`;
}

function truth(tree: Tree, fact: Node): boolean {
  if ('atom' in tree) {
    return [fact.used === true, fact.type === 'ACC', fact.deprecated === true, (fact.qty as number) > 9][tree.atom];
  }
  if ('not' in tree) {
    return !truth(tree.not, fact);
  }
  return 'and' in tree ? truth(tree.and[0], fact) && truth(tree.and[1], fact) : truth(tree.or[0], fact) || truth(tree.or[1], fact);
}

describe('every boolean tree of the conditions', () => {
  it('selects what the tree says, for every parenthesization and every kind of not', () => {
    const all = trees(2);
    expect(all.length).toBeGreaterThan(300);
    for (const tree of all) {
      const expr = `*[${write(tree)}]`;
      for (const fact of FACTS) {
        expect(selects(expr, fact), `${expr} on ${fact.name}`).toBe(truth(tree, fact));
      }
    }
  });

  it('selects what the tree says when the same conditions are written without parentheses where the precedence allows', () => {
    for (const [a, b, c] of [[0, 1, 2], [3, 1, 0], [2, 2, 1], [0, 0, 0]]) {
      for (const fact of FACTS) {
        const t = (i: number) => truth({atom: i}, fact);
        expect(selects(`*[${ATOMS[a]} or ${ATOMS[b]} and ${ATOMS[c]}]`, fact)).toBe(t(a) || t(b) && t(c));
        expect(selects(`*[${ATOMS[a]} and ${ATOMS[b]} or ${ATOMS[c]}]`, fact)).toBe(t(a) && t(b) || t(c));
        expect(selects(`*[not ${ATOMS[a]} and ${ATOMS[b]}]`, fact)).toBe(!t(a) && t(b));
        expect(selects(`*[not ${ATOMS[a]} or ${ATOMS[b]}]`, fact)).toBe(!t(a) || t(b));
        expect(selects(`*[${ATOMS[a]} and not ${ATOMS[b]} or not ${ATOMS[c]}]`, fact)).toBe(t(a) && !t(b) || !t(c));
        expect(selects(`*[${ATOMS[a]}][${ATOMS[b]}][${ATOMS[c]}]`, fact)).toBe(t(a) && t(b) && t(c));
      }
    }
  });

  it('reads the keywords in any case and any spacing', () => {
    for (const [and, or, not] of [['and', 'or', 'not'], ['AND', 'OR', 'NOT'], ['And', 'Or', 'Not']]) {
      for (const fact of FACTS) {
        expect(selects(`*[used ${and} type = ACC ${or} ${not} deprecated]`, fact))
          .toBe(truth({and: [{atom: 0}, {atom: 1}]}, fact) || !truth({atom: 2}, fact));
        expect(selects(`*[  used   ${and}   type=ACC  ]`, fact)).toBe(truth({and: [{atom: 0}, {atom: 1}]}, fact));
      }
    }
  });
});

// ---- paths -----------------------------------------------------------------------------------------------------------

const AXES = ['child', 'descendant', 'descendant-or-self', 'self', 'parent', 'ancestor', 'ancestor-or-self', 'following-sibling', 'preceding-sibling'];

describe('every path of axes and separators', () => {
  /** What the rules of the grammar say about `A <separator> <axis>::B` (a bare step is `A/B`): an error, or none. */
  const cases: [string, string | undefined][] = [
    ['A/B', undefined], ['/A/B', undefined], ['//A', undefined], ['A//B', undefined], ['/A//B', undefined], ['A/*', undefined],
    ['A/..', undefined], ['/A/..', 'The root has no parent'], ['A/B/..', undefined], ['A/B/../C', undefined], ['A/.', undefined], ['./A', undefined],
    ['A/child::B', undefined], ['A/descendant::B', undefined], ['A/descendant-or-self::B', undefined], ['A/self::B', undefined],
    ['A/parent::B', undefined], ['A/B/parent::C', undefined], ['A/ancestor::B', undefined], ['A/ancestor-or-self::B', undefined],
    ['/A/ancestor::B', 'first step'], ['A/B/ancestor::C', 'first step'], ['A//ancestor::B', '// must be followed by a name'],
    ['A/following-sibling::B', undefined], ['A/preceding-sibling::B', undefined], ['/A/following-sibling::B', 'The root has no siblings'],
    ['/A/preceding-sibling::B', 'The root has no siblings'], ['A//parent::B', '// must be followed by a name'],
    ['parent::A', 'cannot start'], ['ancestor::A', 'cannot start'], ['following-sibling::A', 'cannot start'], ['child::A', undefined],
    ['descendant::A', undefined], ['self::A', undefined], ['A/B[2]/following-sibling::C', 'cannot follow'], ['A/B[1]/..', undefined],
    ['A/nosuch::B', 'Unknown axis nosuch'], ['A/child::', undefined], ['A//', undefined], ['A/[used]', undefined]
  ];

  for (const [expr, expected] of cases) {
    it(`${expr} is ${expected === undefined ? 'a path' : 'an error'}`, () => {
      const error = errorOf(expr);
      if (expected === undefined || expr === 'A/child::' || expr === 'A//') {
        // a step without a name is a step on any node: written so, it is not an error
        expect(error, expr).toBeUndefined();
      } else {
        expect(error, expr).toContain(expected);
      }
    });
  }

  it('never throws, whatever the axes, separators and predicates are put together from', () => {
    const separators = ['/', '//'];
    const predicates = ['', '[used]', '[1]', '[last()]', '[type = ACC]', '[child::X]', '[../X]'];
    let count = 0;
    for (const lead of ['', '/', '//']) {
      for (const first of ['A', '*', '.', '..']) {
        for (const sep of separators) {
          for (const axis of ['', ...AXES.map(a => a + '::')]) {
            for (const predicate of predicates) {
              const expr = `${lead}${first}${sep}${axis}B${predicate}`;
              const parsed = parseSearchExpression(expr);
              count++;
              if (parsed.error) {
                expect(parsed.steps, expr).toEqual([]);
                expect(parsed.error.position, expr).toBeGreaterThanOrEqual(0);
                expect(parsed.error.position, expr).toBeLessThanOrEqual(expr.length);
              } else {
                expect(parsed.steps.length, expr).toBeGreaterThan(0);
              }
            }
          }
        }
      }
    }
    expect(count).toBeGreaterThan(1000);
  });
});

// ---- no input breaks the parser --------------------------------------------------------------------------------------

describe('no sequence of tokens breaks the parser', () => {
  const PIECES = ['*', '[', ']', '(', ')', ',', '/', '//', '..', '.', '::', '=', '!=', '<', 'used', 'cardinality', 'and', 'not', '0', '0..1',
    '"x"', '\'', '6f3a', 'count', 'contains', 'child', 'ancestor', ' ', '-', '?'];

  function everySequence(length: number, prefix: string[] = []): string[][] {
    return length === 0 ? [prefix] : PIECES.flatMap(p => everySequence(length - 1, [...prefix, p]));
  }

  it('answers every sequence of up to three tokens with steps or with an error, never with an exception', () => {
    let count = 0;
    for (const length of [1, 2, 3]) {
      for (const sequence of everySequence(length)) {
        const expr = '*[' + sequence.join('') + ']';
        for (const text of [expr, sequence.join('')]) {
          const parsed = parseSearchExpression(text);
          count++;
          if (parsed.error) {
            expect(parsed.steps, text).toEqual([]);
            expect(parsed.error.position, text).toBeGreaterThanOrEqual(0);
            expect(parsed.error.position, text).toBeLessThanOrEqual(text.length);
          }
          expect(() => new PathPattern(text), text).not.toThrow();
        }
      }
    }
    expect(count).toBeGreaterThan(30000);
  }, 120_000);

  it('answers a predicate the same way every time', () => {
    for (const sequence of everySequence(2)) {
      const text = '*[' + sequence.join('') + ']';
      expect(parseSearchExpression(text).error?.message, text).toBe(parseSearchExpression(text).error?.message);
    }
  });

  it('parses every prefix of a long expression as far as it goes, and reports the rest', () => {
    const long = '/Sender[used and (type = ACC or contains(den, "id")) and not cardinality = 0..1]//Code[count(child::*[position() = last()]) >= 2]/..';
    expect(errorOf(long)).toBeUndefined();
    for (let end = 1; end < long.length; end++) {
      const parsed = parseSearchExpression(long.substring(0, end));
      if (parsed.error) {
        expect(parsed.error.position).toBeLessThanOrEqual(end);
      }
    }
  });
});

// ---- the suggestions, followed ---------------------------------------------------------------------------------------

describe('every suggestion, followed to the end', () => {
  const SETS: [string, SearchAttribute[]][] = [['a component tree', COMPONENT_SEARCH_ATTRIBUTES], ['a BIE tree', BIE_SEARCH_ATTRIBUTES],
    ['the model browser', MODEL_BROWSER_SEARCH_ATTRIBUTES]];

  for (const [name, attributes] of SETS) {
    it(`leads to no dead end and no error in ${name}`, () => {
      const seen = new Set<string>();
      let frontier = [{text: '*[', caret: 2}];
      let states = 0;
      const closed: string[] = [];
      for (let depth = 0; depth < 5 && states < 600; depth++) {
        const next: typeof frontier = [];
        for (const state of frontier) {
          const key = state.text.substring(0, state.caret) + '|' + state.text.substring(state.caret);
          if (seen.has(key) || states >= 600) {
            continue;
          }
          seen.add(key);
          states++;
          const before = state.text.substring(0, state.caret);
          const suggestions = suggestSearch(state.text, state.caret, attributes);
          const inQuote = (before.match(/"/g) ?? []).length % 2 === 1;
          const tail = inQuote ? undefined : predicateTail(before.substring(before.indexOf('[') + 1));

          // the text before the caret is one the parser can still go on from
          if (!inQuote) {
            expect(tail, before).toBeDefined();
          }
          const labels = new Set<string>();
          for (const item of suggestions.items) {
            expect(labels.has(item.label), `${before}: ${item.label} twice`).toBe(false);
            labels.add(item.label);
            const text = state.text.substring(0, suggestions.from) + item.insert + state.text.substring(suggestions.to);
            const caret = suggestions.from + item.insert.length - (item.caretBack ?? 0);
            if (item.insert === ']') {
              closed.push(text);
              continue;
            }
            // what is offered is what the parser accepts
            const inserted = text.substring(0, caret);
            if ((inserted.match(/"/g) ?? []).length % 2 === 0) {
              expect(predicateTail(inserted.substring(inserted.indexOf('[') + 1)), `${before} + ${item.label}`).toBeDefined();
            }
            next.push({text, caret});
          }
          // somewhere to go: a text that is not a condition yet, outside quotes, always has a suggestion, but for a word being typed
          if (tail && !tail.complete && suggestions.items.length === 0) {
            expect(/[A-Za-z0-9_-]$/.test(before), `${before} is a dead end`).toBe(true);
          }
        }
        frontier = next;
      }
      expect(states).toBeGreaterThan(300);
      expect(closed.length).toBeGreaterThan(30);
      for (const text of closed) {
        expect(errorOf(text), text).toBeUndefined();
      }
    }, 120_000);
  }

  it('offers only a name that the typed letters begin or are in, from every prefix of every attribute', () => {
    for (const attribute of BIE_SEARCH_ATTRIBUTES) {
      for (let end = 1; end <= attribute.name.length; end++) {
        const typed = attribute.name.substring(0, end);
        const items = suggestSearch('*[' + typed, 2 + end, BIE_SEARCH_ATTRIBUTES).items;
        for (const item of items) {
          expect(item.label.toLowerCase(), `${typed} -> ${item.label}`).toContain(typed.toLowerCase());
        }
        if (typed === attribute.name) {
          continue;
        }
        expect(items.map(i => i.label), typed).toContain(attribute.name);
      }
    }
  });

  it('offers the same suggestions wherever the caret is in a text it has read to the same point', () => {
    const expression = '*[type = ACC and used]';
    for (let caret = 2; caret < expression.length; caret++) {
      const here = suggestSearch(expression, caret, BIE_SEARCH_ATTRIBUTES);
      const alone = suggestSearch(expression.substring(0, caret), caret, BIE_SEARCH_ATTRIBUTES);
      expect(here.items.map(i => i.label), expression.substring(0, caret)).toEqual(alone.items.map(i => i.label));
    }
  });

  it('offers a value in every attribute that has a fixed set of them, after every operator, with and without quotes', () => {
    for (const attribute of BIE_SEARCH_ATTRIBUTES.filter(a => a.values && a.values.length > 0)) {
      for (const operator of ['=', '!=']) {
        const labels = suggestSearch(`*[${attribute.name} ${operator} `, `*[${attribute.name} ${operator} `.length, BIE_SEARCH_ATTRIBUTES).items.map(i => i.label);
        expect(labels, `${attribute.name} ${operator}`).toEqual(attribute.values);
      }
      const inside = `*[${attribute.name} = "`;
      // a quoted text is a value too, even where the bare word is the usual way of writing it (cardinality = "Optional")
      expect(suggestSearch(inside, inside.length, BIE_SEARCH_ATTRIBUTES).items.map(i => i.label), `${attribute.name} in quotes`).toEqual(
        attribute.values);
    }
  });
});
