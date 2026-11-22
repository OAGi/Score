import {writeFileSync} from 'node:fs';
import {describe, it} from 'vitest';
import {ENABLED, lit, sql} from './integrity-env';

/**
 * The text of a BIE (what the backend searches) in every operator and function, each judged by an SQL count of the nodes of
 * the BIE tree whose row holds a matching text (`SEARCH_BIE_MATRIX_OUT`, `SEARCH_BIE_MATRIX_OWNERS` is `id,...`). The same
 * expressions are typed into the search bar of the screens that show the BIE tree. `fixed_value` and `default_value` are not here: a
 * place whose row keeps none says those of its core component, which `cc-matrix` holds with the rows of the BIE (fourth part of a root).
 */

/** For each attribute: the node kind, and the row (with the column) a node of that kind keeps its text in. `{c}` is the column test. */
const SOURCES: Record<string, {kind: string, from: string, key: string, column: string, where?: string}[]> = {
  definition: [
    {kind: 'ASBIEP', from: 'asbie c', key: 'c.asbie_id', column: 'c.definition'},
    {kind: 'BBIEP', from: 'bbie c', key: 'c.bbie_id', column: 'c.definition'},
    {kind: 'BBIE_SC', from: 'bbie_sc c', key: 'c.bbie_sc_id', column: 'c.definition'},
    {kind: 'ABIE', from: 'asbiep c join top_level_asbiep t on t.asbiep_id = c.asbiep_id', key: '0', column: 'c.definition'}
  ],
  remark: [
    {kind: 'ASBIEP', from: 'asbie c join asbiep p on p.asbiep_id = c.to_asbiep_id', key: 'c.asbie_id', column: 'p.remark'},
    {kind: 'BBIEP', from: 'bbie c join bbiep p on p.bbiep_id = c.to_bbiep_id', key: 'c.bbie_id', column: 'p.remark'},
    {kind: 'BBIE_SC', from: 'bbie_sc c', key: 'c.bbie_sc_id', column: 'c.remark'},
    {kind: 'ABIE', from: 'asbiep c join top_level_asbiep t on t.asbiep_id = c.asbiep_id', key: '0', column: 'c.remark'}
  ],
  example: [
    {kind: 'BBIEP', from: 'bbie c', key: 'c.bbie_id', column: 'c.example'},
    {kind: 'BBIE_SC', from: 'bbie_sc c', key: 'c.bbie_sc_id', column: 'c.example'}
  ]
};

type Op = 'eq' | 'ne' | 'contains' | 'starts' | 'ends';

function test(column: string, op: Op, value: string): string {
  const wanted = lit(value);
  const length = `char_length(${wanted})`;
  switch (op) {
    case 'eq': return `${column} is not null and ${column} = ${wanted} and char_length(${column}) = ${length}`;
    case 'ne': return `${column} is not null and not (${column} = ${wanted} and char_length(${column}) = ${length})`;
    case 'contains': return `${column} is not null and (${length} = 0 or locate(${wanted}, ${column}) > 0)`;
    case 'starts': return `${column} is not null and char_length(${column}) >= ${length} and left(${column}, ${length}) = ${wanted}`;
    default: return `${column} is not null and (${length} = 0 or char_length(${column}) >= ${length} and right(${column}, ${length}) = ${wanted})`;
  }
}

/** How many nodes of the BIE have text in the attribute that satisfies the operator, optionally only the nodes of a kind. */
function count(owner: number, attribute: string, op: Op, value: string, kinds?: string[]): number {
  const keys = new Set<string>();
  for (const s of SOURCES[attribute].filter(x => !kinds || kinds.includes(x.kind))) {
    const root = s.kind === 'ABIE' ? ` and t.asbiep_id = c.asbiep_id` : '';
    for (const [key] of sql(`select distinct ${s.key} from ${s.from} where c.owner_top_level_asbiep_id = ${owner}${root} and (${test(s.column, op, value)})`)) {
      keys.add(s.kind + key);
    }
  }
  return keys.size;
}

function samples(owner: number, attribute: string): string[] {
  const out = new Set<string>();
  for (const s of SOURCES[attribute]) {
    for (const [v] of sql(`select left(replace(replace(replace(${s.column}, char(10), ' '), char(13), ' '), char(9), ' '), 14) from ${s.from} ` +
      `where c.owner_top_level_asbiep_id = ${owner} and ${s.column} is not null and char_length(${s.column}) > 5 ${s.kind === 'ABIE' ? 'and t.asbiep_id = c.asbiep_id' : ''} order by md5(${s.key}) limit 3`)) {
      if (!v.includes('"') && !v.includes('\'') && !v.includes('\\')) { out.add(v); }
    }
  }
  return [...out].slice(0, 3);
}

describe.skipIf(!ENABLED || !process.env['SEARCH_BIE_MATRIX_OUT'])('matrix of the text of a BIE', () => {
  it('counts every case', () => {
    const result: Record<string, {group: string, expr: string, want: number}[]> = {};
    for (const owner of (process.env['SEARCH_BIE_MATRIX_OWNERS'] ?? '').split(',').map(Number)) {
      const cases: {group: string, expr: string, want: number}[] = [];
      const add = (group: string, condition: string, want: number) => cases.push({group, expr: '//*[' + condition + ']', want});
      for (const attribute of Object.keys(SOURCES)) {
        const values = [...samples(owner, attribute), 'no such text', ''];
        for (const value of values) {
          for (const v of new Set([value, value.toUpperCase(), value.toLowerCase()])) {
            add(attribute + ' =', `${attribute} = "${v}"`, count(owner, attribute, 'eq', v));
            add(attribute + ' !=', `${attribute} != "${v}"`, count(owner, attribute, 'ne', v));
            add(attribute + ' not =', `not(${attribute} = "${v}")`, -1);
            add(attribute + ' contains', `contains(${attribute}, "${v}")`, count(owner, attribute, 'contains', v));
            add(attribute + ' starts-with', `starts-with(${attribute}, "${v}")`, count(owner, attribute, 'starts', v));
            add(attribute + ' ends-with', `ends-with(${attribute}, "${v}")`, count(owner, attribute, 'ends', v));
          }
          for (const piece of new Set([value.substring(0, 1), value.substring(2, 5), value.slice(-3)])) {
            add(attribute + ' contains piece', `contains(${attribute}, "${piece}")`, count(owner, attribute, 'contains', piece));
            add(attribute + ' starts piece', `starts-with(${attribute}, "${piece}")`, count(owner, attribute, 'starts', piece));
            add(attribute + ' ends piece', `ends-with(${attribute}, "${piece}")`, count(owner, attribute, 'ends', piece));
          }
        }
        // restricted to a kind of node, and joined with another condition
        for (const kind of ['ASBIEP', 'BBIEP', 'BBIE_SC', 'ABIE']) {
          if (SOURCES[attribute].some(s => s.kind === kind)) {
            add(attribute + ' and type', `type = ${kind} and contains(${attribute}, "e")`, count(owner, attribute, 'contains', 'e', [kind]));
            add(attribute + ' or type', `type = ${kind} or contains(${attribute}, "zzqq")`, -2);
          }
        }
      }
      result[String(owner)] = cases.filter(c => c.want !== -1 && c.want !== -2);
      console.log(owner, 'cases', result[String(owner)].length);
    }
    writeFileSync(process.env['SEARCH_BIE_MATRIX_OUT'], JSON.stringify(result));
  }, 3_600_000);
});
