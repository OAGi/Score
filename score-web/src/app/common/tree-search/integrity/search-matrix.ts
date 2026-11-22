import {Rows} from './cc-tree-oracle';

/**
 * Every combination of what the search grammar offers, as an expression and the meaning it has on the rows of a node
 * (the meaning each attribute is documented to have, worked out here without the parser): the attributes with every
 * operator and every way a value is written, `contains`, `starts-with` and `ends-with`, flags bare, compared and negated,
 * numbers and `cardinality`, and the boolean trees (`and`, `or`, `not`, parentheses) of them. The same expressions are
 * typed into the search bar of every screen, and the count each finds is held against what the oracle counts.
 */

export type Test = (rows: Rows) => boolean;

export interface Case {
  /** The kind of case, for the report. */
  group: string;
  /** The condition, to be written between the brackets of `//*[...]`. */
  condition: string;
  test: Test;
}

export interface Attributes {
  text: string[];
  flag: string[];
  number: string[];
  tag?: string;
  cardinality?: boolean;
  /** Text attributes the backend answers, which cannot be asked bare (`attribute`, `not(attribute)`): only compared. */
  serverText?: string[];
}

const asText = (v: Rows[string]): string | undefined => v === undefined || Array.isArray(v) ? undefined : String(v);

/** How a text is written in a search: quoted either way, or bare when it is a plain word. */
export function written(value: string): string[] {
  const forms: string[] = [];
  if (!value.includes('"')) {
    forms.push('"' + value + '"');
  }
  if (!value.includes('\'')) {
    forms.push('\'' + value + '\'');
  }
  return forms;
}

function variants(value: string): string[] {
  return [...new Set([value, value.toLowerCase(), value.toUpperCase()])];
}

const lower = (s: string) => s.toLowerCase();

/** The cases for the attributes, from the values the data has (`values`: the distinct texts and numbers of each attribute). */
export function attributeCases(attributes: Attributes, values: Record<string, (string | number)[]>): Case[] {
  const cases: Case[] = [];
  const add = (group: string, condition: string, test: Test) => cases.push({group, condition, test});

  for (const name of attributes.text) {
    const samples = (values[name] ?? []).map(String).filter(v => !v.includes('"') && !v.includes('\'') && v.length > 0).slice(0, 4);
    for (const sample of samples) {
      for (const form of variants(sample)) {
        for (const quoted of written(form).slice(0, 1)) {
          add('text =', `${name} = ${quoted}`, r => { const t = asText(r[name]); return t !== undefined && lower(t) === lower(sample); });
          add('text !=', `${name} != ${quoted}`, r => { const t = asText(r[name]); return t !== undefined && lower(t) !== lower(sample); });
          add('text spaced', `${name}=${quoted}`, r => { const t = asText(r[name]); return t !== undefined && lower(t) === lower(sample); });
          add('not =', `not(${name} = ${quoted})`, r => { const t = asText(r[name]); return !(t !== undefined && lower(t) === lower(sample)); });
        }
      }
      if (/^[A-Za-z_][A-Za-z0-9_-]*$/.test(sample)) {
        add('text bare', `${name} = ${sample}`, r => { const t = asText(r[name]); return t !== undefined && lower(t) === lower(sample); });
      }
      const pieces = new Set<string>([sample.substring(0, 1), sample.substring(0, 3), sample.substring(Math.floor(sample.length / 2)), sample.slice(-3), sample]);
      for (const piece of pieces) {
        if (piece.includes('"')) {
          continue;
        }
        for (const form of [piece, piece.toUpperCase()]) {
          add('contains', `contains(${name}, "${form}")`, r => { const t = asText(r[name]); return t !== undefined && lower(t).includes(lower(piece)); });
          add('contains', `contains(${name}, '${form}')`, r => { const t = asText(r[name]); return t !== undefined && lower(t).includes(lower(piece)); });
          add('not contains', `not(contains(${name}, "${form}"))`, r => { const t = asText(r[name]); return !(t !== undefined && lower(t).includes(lower(piece))); });
          add('starts-with', `starts-with(${name}, "${form}")`, r => { const t = asText(r[name]); return t !== undefined && lower(t).startsWith(lower(piece)); });
          add('ends-with', `ends-with(${name}, "${form}")`, r => { const t = asText(r[name]); return t !== undefined && lower(t).endsWith(lower(piece)); });
        }
      }
    }
    const bare = !(attributes.serverText ?? []).includes(name);
    bare && add('text present', `${name}`, r => { const t = asText(r[name]); return t !== undefined && t !== ''; });
    bare && add('text absent', `not(${name})`, r => { const t = asText(r[name]); return !(t !== undefined && t !== ''); });
    add('text empty', `${name} = ""`, r => asText(r[name]) === '');
    add('text no match', `${name} = "no such value"`, () => false);
    add('text no match !=', `${name} != "no such value"`, r => asText(r[name]) !== undefined);
    add('contains empty', `contains(${name}, "")`, r => asText(r[name]) !== undefined);
    add('contains miss', `contains(${name}, "zzzqqq")`, () => false);
  }

  for (const name of attributes.flag) {
    add('flag bare', `${name}`, r => r[name] === true);
    add('flag not', `not(${name})`, r => r[name] !== true);
    add('flag = true', `${name} = true`, r => r[name] === true);
    add('flag = false', `${name} = false`, r => r[name] === false);
    add('flag != true', `${name} != true`, r => r[name] === false);
    add('flag != false', `${name} != false`, r => r[name] === true);
    add('flag = TRUE', `${name} = TRUE`, r => r[name] === true);
    add('flag not =', `not(${name} = true)`, r => r[name] !== true);
  }

  for (const name of attributes.number) {
    for (const n of [0, 1, 2, 5, 10, -1]) {
      const number = (r: Rows) => { const x = r[name]; return typeof x === 'number' ? x : undefined; };
      add('number =', `${name} = ${n}`, r => number(r) === n);
      add('number !=', `${name} != ${n}`, r => number(r) !== undefined && number(r) !== n);
      add('number <', `${name} < ${n}`, r => number(r) !== undefined && number(r) < n);
      add('number <=', `${name} <= ${n}`, r => number(r) !== undefined && number(r) <= n);
      add('number >', `${name} > ${n}`, r => number(r) !== undefined && number(r) > n);
      add('number >=', `${name} >= ${n}`, r => number(r) !== undefined && number(r) >= n);
    }
    add('number bare', `${name}`, r => typeof r[name] === 'number' && r[name] !== 0);
    add('number decimal', `${name} >= 0.5`, r => typeof r[name] === 'number' && (r[name] as number) >= 0.5);
    add('number quoted', `${name} = "1"`, r => r[name] === 1);
  }

  if (attributes.tag) {
    const tag = attributes.tag;
    const has = (r: Rows, v: string) => Array.isArray(r[tag]) && (r[tag] as string[]).some(t => lower(t) === lower(v));
    for (const sample of (values[tag] ?? []).map(String).slice(0, 5)) {
      add('tag =', `${tag} = "${sample}"`, r => has(r, sample));
      add('tag = lower', `${tag} = "${sample.toLowerCase()}"`, r => has(r, sample));
      add('tag !=', `${tag} != "${sample}"`, r => Array.isArray(r[tag]) && (r[tag] as string[]).some(t => lower(t) !== lower(sample)));
      add('tag contains', `contains(${tag}, "${sample.substring(0, 3)}")`, r => Array.isArray(r[tag]) && (r[tag] as string[]).some(t => lower(t).includes(lower(sample.substring(0, 3)))));
    }
    add('tag present', `${tag}`, r => Array.isArray(r[tag]) && (r[tag] as string[]).length > 0);
    add('tag absent', `not(${tag})`, r => !(Array.isArray(r[tag]) && (r[tag] as string[]).length > 0));
  }

  if (attributes.cardinality) {
    const range = (r: Rows) => typeof r['cardinality_min'] === 'number' && typeof r['cardinality_max'] === 'number' ?
      [r['cardinality_min'] as number, r['cardinality_max'] as number] : undefined;
    const ranges: [string, number, number][] = [['0..1', 0, 1], ['1..1', 1, 1], ['0..*', 0, Infinity], ['1..*', 1, Infinity], ['0..unbounded', 0, Infinity],
      ['2..5', 2, 5], ['0..0', 0, 0], ['Optional', 0, 1], ['Required', 1, 1], ['optional', 0, 1]];
    for (const [spelled, lo, hi] of ranges) {
      add('cardinality =', `cardinality = ${spelled}`, r => { const c = range(r); return !!c && c[0] === lo && c[1] === hi; });
      add('cardinality !=', `cardinality != ${spelled}`, r => { const c = range(r); return !!c && !(c[0] === lo && c[1] === hi); });
      add('cardinality quoted', `cardinality = "${spelled}"`, r => { const c = range(r); return !!c && c[0] === lo && c[1] === hi; });
    }
  }
  return cases;
}

/** A small deterministic generator, so that a run can be repeated. */
function random(seed: number): () => number {
  let state = seed;
  return () => {
    state = (state * 1664525 + 1013904223) % 4294967296;
    return state / 4294967296;
  };
}

/** Boolean trees over the cases: `and`, `or`, `not` and parentheses, in every shape of two and three operands. */
export function booleanCases(atoms: Case[], count: number, seed = 1817): Case[] {
  const next = random(seed);
  const pick = () => atoms[Math.floor(next() * atoms.length)];
  const result: Case[] = [];
  for (let i = 0; i < count; i++) {
    const a = pick();
    const b = pick();
    const c = pick();
    const shape = i % 10;
    let condition: string;
    let test: Test;
    switch (shape) {
      case 0: condition = `${a.condition} and ${b.condition}`; test = r => a.test(r) && b.test(r); break;
      case 1: condition = `${a.condition} or ${b.condition}`; test = r => a.test(r) || b.test(r); break;
      case 2: condition = `not(${a.condition}) and ${b.condition}`; test = r => !a.test(r) && b.test(r); break;
      case 3: condition = `(${a.condition} or ${b.condition}) and ${c.condition}`; test = r => (a.test(r) || b.test(r)) && c.test(r); break;
      case 4: condition = `${a.condition} or ${b.condition} and ${c.condition}`; test = r => a.test(r) || b.test(r) && c.test(r); break;
      case 5: condition = `not(${a.condition} or ${b.condition})`; test = r => !(a.test(r) || b.test(r)); break;
      case 6: condition = `not(${a.condition} and ${b.condition})`; test = r => !(a.test(r) && b.test(r)); break;
      case 7: condition = `${a.condition} and ${b.condition} and ${c.condition}`; test = r => a.test(r) && b.test(r) && c.test(r); break;
      case 8: condition = `not(not(${a.condition}))`; test = r => a.test(r); break;
      default: condition = `(${a.condition}) and not(${b.condition} or ${c.condition})`; test = r => a.test(r) && !(b.test(r) || c.test(r)); break;
    }
    result.push({group: 'boolean ' + shape, condition, test});
  }
  return result;
}
