import {describe, expect, it} from 'vitest';
import {BACKEND, ENABLED, backend, lit, sql} from './integrity-env';

/**
 * The text of a BIE that the backend searches (`BieSearchAttribute`) against an independent SQL count: for every
 * attribute, operator and type of value, the places (table and hash path) the backend answers are exactly the rows whose
 * column satisfies the condition. `business_term` is both the `biz_term` text of the nodes that have one and the terms
 * assigned to an ASBIE or a BBIE, whoever asks.
 */

type Operator = 'EQUALS' | 'NOT_EQUALS' | 'CONTAINS' | 'STARTS_WITH' | 'ENDS_WITH';

/** What each attribute reads, written down from the data model: the table and the column. */
const COLUMNS: Record<string, [string, string][]> = {
  definition: [['asbiep', 'definition'], ['asbie', 'definition'], ['bbie', 'definition'], ['bbie_sc', 'definition']],
  remark: [['asbiep', 'remark'], ['bbiep', 'remark'], ['bbie_sc', 'remark']],
  example: [['bbie', 'example'], ['bbie_sc', 'example']],
  fixed_value: [['bbie', 'fixed_value'], ['bbie_sc', 'fixed_value']],
  default_value: [['bbie', 'default_value'], ['bbie_sc', 'default_value']],
  business_term: [['abie', 'biz_term'], ['asbiep', 'biz_term'], ['bbiep', 'biz_term'], ['bbie_sc', 'biz_term']]
};

/** The terms assigned to an ASBIE or a BBIE: the table, the link to its assigned terms, and the way to the business term. */
const ASSIGNED_TERMS: [string, string, string, string, string][] = [
  ['asbie', 'asbie_bizterm', 'asbie_id', 'ascc_bizterm', 'ascc_bizterm_id'],
  ['bbie', 'bbie_bizterm', 'bbie_id', 'bcc_bizterm', 'bcc_bizterm_id']
];

function condition(column: string, operator: Operator, value: string, caseSensitive: boolean): string {
  const text = caseSensitive ? `(${column} collate utf8mb3_bin)` : column;
  const wanted = caseSensitive ? `(${lit(value)} collate utf8mb3_bin)` : lit(value);
  const length = `char_length(${wanted})`;
  switch (operator) {
    case 'EQUALS':
      return `${column} is not null and ${text} = ${wanted} and char_length(${column}) = ${length}`;
    case 'NOT_EQUALS':
      return `${column} is not null and not (${text} = ${wanted} and char_length(${column}) = ${length})`;
    case 'CONTAINS':
      return `${column} is not null and (${length} = 0 or locate(${wanted}, ${text}) > 0)`;
    case 'STARTS_WITH':
      return `${column} is not null and char_length(${column}) >= ${length} and left(${text}, ${length}) = ${wanted}`;
    default:
      return `${column} is not null and (${length} = 0 or char_length(${column}) >= ${length} and right(${text}, ${length}) = ${wanted})`;
  }
}

function expected(owner: number, attribute: string, operator: Operator, value: string, caseSensitive: boolean): string[] {
  const keys = new Set<string>();
  const unstorable = [...value].some(c => c.codePointAt(0) > 0xFFFF);
  for (const [table, column] of COLUMNS[attribute]) {
    // the definition of the BIE itself is the one of the root ASBIEP; any other ASBIEP's is the ASBIE's
    const root = attribute === 'definition' && table === 'asbiep'
      ? ` and t.asbiep_id = c.asbiep_id` : '';
    const from = root ? `${table} c join top_level_asbiep t on t.asbiep_id = c.asbiep_id` : `${table} c`;
    const where = root ? `c.owner_top_level_asbiep_id = ${owner}${root}` : `c.owner_top_level_asbiep_id = ${owner}`;
    const test = unstorable ? (operator === 'NOT_EQUALS' ? `c.${column} is not null` : 'false')
      : condition('c.' + column, operator, value, caseSensitive);
    for (const [hashPath] of sql(`select distinct c.hash_path from ${from} where ${where} and (${test})`)) {
      keys.add(table + '|' + hashPath);
    }
  }
  if (attribute === 'business_term') {
    const test = unstorable ? (operator === 'NOT_EQUALS' ? 'b.business_term is not null' : 'false')
      : condition('b.business_term', operator, value, caseSensitive);
    for (const [table, link, id, assigned, assignedId] of ASSIGNED_TERMS) {
      const from = `${table} c join ${link} l on l.${id} = c.${id} join ${assigned} x on x.${assignedId} = l.${assignedId} ` +
        'join business_term b on b.business_term_id = x.business_term_id';
      for (const [hashPath] of sql(`select distinct c.hash_path from ${from} where c.owner_top_level_asbiep_id = ${owner} and (${test})`)) {
        keys.add(table + '|' + hashPath);
      }
    }
  }
  return [...keys].sort();
}

interface Answer {
  matches: Record<string, {table: string, hashPath: string, path: string}[]>;
}

async function asked(owner: number, attribute: string, operator: Operator, value: string, caseSensitive: boolean): Promise<string[]> {
  const answer = await backend<Answer>('/profile_bie/' + owner + '/search-nodes', {
    predicates: [{id: 'p', attribute, operator, value, caseSensitive}]
  });
  return [...new Set((answer.matches['p'] ?? []).map(k => k.table + '|' + k.hashPath))].sort();
}

describe.skipIf(!ENABLED)('BIE server text search against SQL (' + BACKEND + ')', () => {
  /** BIEs with a lot of text and a few others (the ids are those of the data, picked by what they hold). */
  /** The candidates the user can open (a BIE that is not found has nothing to search). */
  async function visibleOwners(): Promise<number[]> {
    const visible: number[] = [];
    const candidates = [...new Set([
      ...sql('select owner_top_level_asbiep_id from bbie where definition is not null or example is not null group by 1 order by count(*) desc limit 3').map(r => Number(r[0])),
      ...sql('select top_level_asbiep_id from top_level_asbiep order by md5(top_level_asbiep_id) limit 8').map(r => Number(r[0]))
    ])];
    for (const owner of candidates) {
      try {
        await asked(owner, 'remark', 'CONTAINS', 'x', false);
        visible.push(owner);
      } catch (e) {
        // not found for this user
      }
    }
    return visible.slice(0, 6);
  }

  function values(owner: number, attribute: string): string[] {
    const [table, column] = COLUMNS[attribute][0];
    const sample = sql(`select left(replace(replace(replace(${column}, char(10), ' '), char(13), ' '), char(9), ' '), 12) from ${table} ` +
      `where owner_top_level_asbiep_id = ${owner} and ${column} is not null and char_length(${column}) > 3 limit 1`)[0]?.[0];
    return [...new Set([sample ?? 'The', (sample ?? 'The').toUpperCase(), (sample ?? 'The').toLowerCase(), 'a', 'e', '', '%', '_', '!', '\'', '"',
      'no such text anywhere ☃x', '😀', ' ', 'true', '0', '1'])];
  }

  for (const attribute of Object.keys(COLUMNS)) {
    it(`${attribute}`, async () => {
      const mismatches: string[] = [];
      let compared = 0;
      let nonEmpty = 0;
      const owners = await visibleOwners();
      expect(owners.length).toBeGreaterThan(2);
      for (const owner of owners) {
        for (const operator of ['CONTAINS', 'EQUALS', 'NOT_EQUALS', 'STARTS_WITH', 'ENDS_WITH'] as Operator[]) {
          for (const value of values(owner, attribute)) {
            for (const caseSensitive of [false, true]) {
              const want = expected(owner, attribute, operator, value, caseSensitive);
              const got = await asked(owner, attribute, operator, value, caseSensitive);
              compared++;
              nonEmpty += want.length > 0 ? 1 : 0;
              if (JSON.stringify(want) !== JSON.stringify(got)) {
                mismatches.push(`BIE ${owner} ${operator} ${JSON.stringify(value)} cs=${caseSensitive}: SQL ${want.length} backend ${got.length}`);
              }
            }
          }
        }
      }
      expect(compared).toBeGreaterThan(100);
      // an attribute that none of the BIEs searched has set (e.g. default values) is compared on its absence only
      if (COLUMNS[attribute].some(([table, column]) => sql(`select count(*) from ${table} where ${column} is not null and owner_top_level_asbiep_id in (${owners.join(',')})`)[0][0] !== '0')) {
        expect(nonEmpty, 'nothing to compare').toBeGreaterThan(0);
      }
      expect(mismatches.slice(0, 40)).toEqual([]);
    }, 900_000);
  }
});
