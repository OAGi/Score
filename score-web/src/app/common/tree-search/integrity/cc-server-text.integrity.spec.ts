import {describe, expect, it} from 'vitest';
import {BACKEND, ENABLED, backend, lit, sql} from './integrity-env';

/**
 * The text of the core components that the backend searches (`CcSearchAttribute`) against an independent SQL count:
 * for every attribute, operator and type of value (words, case, quotes, wildcards, accents, the empty text, NULL
 * columns) the manifests the backend answers are exactly the manifests of the release whose column satisfies the
 * condition. Run it as described in `integrity-env.ts`.
 */

type Type = 'accs' | 'asccs' | 'asccps' | 'bccs' | 'bccps' | 'dts' | 'dtScs';
type Operator = 'EQUALS' | 'NOT_EQUALS' | 'CONTAINS' | 'STARTS_WITH' | 'ENDS_WITH';

interface Table {
  type: Type;
  manifest: string;
  id: string;
  table: string;
}

const TABLES: Record<Type, Table> = {
  accs: {type: 'accs', manifest: 'acc_manifest', id: 'acc_manifest_id', table: 'acc'},
  asccs: {type: 'asccs', manifest: 'ascc_manifest', id: 'ascc_manifest_id', table: 'ascc'},
  asccps: {type: 'asccps', manifest: 'asccp_manifest', id: 'asccp_manifest_id', table: 'asccp'},
  bccs: {type: 'bccs', manifest: 'bcc_manifest', id: 'bcc_manifest_id', table: 'bcc'},
  bccps: {type: 'bccps', manifest: 'bccp_manifest', id: 'bccp_manifest_id', table: 'bccp'},
  dts: {type: 'dts', manifest: 'dt_manifest', id: 'dt_manifest_id', table: 'dt'},
  dtScs: {type: 'dtScs', manifest: 'dt_sc_manifest', id: 'dt_sc_manifest_id', table: 'dt_sc'}
};

/** What each attribute reads, per type of component: written down independently of the repository, from the data model. */
const COLUMNS: Record<string, Partial<Record<Type, string>>> = {
  definition: {accs: 'definition', asccs: 'definition', asccps: 'definition', bccs: 'definition', bccps: 'definition', dts: 'definition', dtScs: 'definition'},
  definition_source: {accs: 'definition_source', asccs: 'definition_source', asccps: 'definition_source', bccs: 'definition_source', bccps: 'definition_source', dts: 'definition_source', dtScs: 'definition_source'},
  association_definition: {asccs: 'definition', bccs: 'definition'},
  component_definition: {asccps: 'definition', bccps: 'definition', dtScs: 'definition'},
  type_definition: {accs: 'definition', dts: 'definition'},
  default_value: {bccs: 'default_value', bccps: 'default_value', dtScs: 'default_value'},
  fixed_value: {bccs: 'fixed_value', bccps: 'fixed_value', dtScs: 'fixed_value'},
  content_component_definition: {dts: 'content_component_definition'}
};

/** The condition on a column, spelled without LIKE so that the backend's pattern building is not what is trusted. */
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

function expected(release: number, attribute: string, operator: Operator, value: string, caseSensitive: boolean): Record<Type, number[]> {
  const result = {} as Record<Type, number[]>;
  for (const type of Object.keys(TABLES) as Type[]) {
    const column = COLUMNS[attribute][type];
    if (!column) {
      result[type] = [];
      continue;
    }
    const t = TABLES[type];
    const unstorable = [...value].some(c => c.codePointAt(0) > 0xFFFF);
    const key = t.id.replace('_manifest_id', '_id');
    result[type] = sql(`select m.${t.id} from ${t.manifest} m join ${t.table} c on c.${key} = m.${key} ` +
      `where m.release_id = ${release} and (${unstorable ? (operator === 'NOT_EQUALS' ? `c.${column} is not null` : 'false')
        : condition('c.' + column, operator, value, caseSensitive)}) order by 1`)
      .map(row => Number(row[0]));
  }
  return result;
}

interface Answer {
  matches: Record<string, Record<Type, number[]>>;
}

async function asked(root: number, attribute: string, operator: Operator, value: string, caseSensitive: boolean): Promise<Record<Type, number[]>> {
  const answer = await backend<Answer>('/core-components/search-nodes', {
    asccpManifestId: root,
    predicates: [{id: 'p', attribute, operator, value, caseSensitive}]
  });
  const sorted = {} as Record<Type, number[]>;
  for (const type of Object.keys(TABLES) as Type[]) {
    sorted[type] = [...(answer.matches['p']?.[type] ?? [])].sort((a, b) => a - b);
  }
  return sorted;
}

/** A value found in the data: a text from a column that has one, cut to a part. */
function sample(column: string, table: string, where: string): string | undefined {
  const rows = sql(`select ${column} from ${table} where ${where} limit 1`);
  return rows[0]?.[0];
}

describe.skipIf(!ENABLED)('CC server text search against SQL (' + BACKEND + ')', () => {

  const releases = [77, 10, 63];
  /** A root of the release that the user can open (the backend answers for the release of the root). */
  const root = async (release: number): Promise<number> => {
    for (const [id] of sql(`select asccp_manifest_id from asccp_manifest where release_id = ${release} order by 1 limit 20`)) {
      try {
        await asked(Number(id), 'definition', 'CONTAINS', 'x', false);
        return Number(id);
      } catch (e) {
        // not found for this user
      }
    }
    throw new Error('No root of release ' + release + ' can be opened');
  };

  /** The values to search for, picked from the data so that they have matches, near-matches and none. */
  function values(): string[] {
    const plain = sample('substring_index(trim(definition), " ", 1)', 'acc', 'definition is not null and char_length(definition) > 40') ?? 'The';
    const withUnderscore = sample('left(replace(replace(replace(definition, char(10), " "), char(13), " "), char(9), " "), 30)', 'asccp', 'definition like "%!_%" escape "!"');
    const found = new Set<string>([
      plain, plain.toUpperCase(), plain.toLowerCase(), 'identifier', 'Identifier', 'IDENTIFIER', 'a', '',
      '%', '_', '!', '!%', '\'', '"', 'e', 'no such text anywhere ☃x', '😀',
      'The ', ' ', 'true', '0', '1', 'http://', 'http'
    ]);
    if (withUnderscore) {
      found.add(withUnderscore.substring(0, 30));
    }
    return [...found];
  }

  const attributes = Object.keys(COLUMNS);
  const operators: Operator[] = ['CONTAINS', 'EQUALS', 'NOT_EQUALS', 'STARTS_WITH', 'ENDS_WITH'];

  it('compares in the collation of the columns (case and accent insensitive), and binary when case sensitive', async () => {
    const release = releases[0];
    const rootManifest = await root(release);
    const plain = await asked(rootManifest, 'definition', 'CONTAINS', 'e', false);
    expect(await asked(rootManifest, 'definition', 'CONTAINS', 'é', false)).toEqual(plain);
    expect(await asked(rootManifest, 'definition', 'CONTAINS', 'É', false)).toEqual(plain);
    const exact = await asked(rootManifest, 'definition', 'CONTAINS', 'é', true);
    const sqlExact = sql(`select count(*) from asccp_manifest m join asccp c on c.asccp_id = m.asccp_id where m.release_id = ${release} ` +
      'and instr(c.definition collate utf8mb3_bin, \'é\' collate utf8mb3_bin) > 0')[0][0];
    expect(exact.asccps.length).toBe(Number(sqlExact));
    expect(exact.asccps.length).toBeLessThan(plain.asccps.length);
  }, 120_000);

  for (const release of releases) {
    for (const attribute of attributes) {
      it(`release ${release}: ${attribute}`, async () => {
        const rootManifest = await root(release);
        const mismatches: string[] = [];
        let compared = 0;
        let nonEmpty = 0;
        for (const operator of operators) {
          for (const value of values()) {
            for (const caseSensitive of [false, true]) {
              const want = expected(release, attribute, operator, value, caseSensitive);
              const got = await asked(rootManifest, attribute, operator, value, caseSensitive);
              compared++;
              for (const type of Object.keys(TABLES) as Type[]) {
                nonEmpty += want[type].length > 0 ? 1 : 0;
                if (JSON.stringify(want[type]) !== JSON.stringify(got[type])) {
                  mismatches.push(`${operator} ${JSON.stringify(value)} cs=${caseSensitive} ${type}: SQL ${want[type].length} backend ${got[type].length}`);
                }
              }
            }
          }
        }
        expect(compared).toBeGreaterThan(100);
        // an attribute that no component of the release has (the data has no fixed values) is compared on its absence only
        const hasData = Object.entries(COLUMNS[attribute]).some(([type, column]) => {
          const t = TABLES[type as Type];
          const key = t.id.replace('_manifest_id', '_id');
          return sql(`select count(*) from ${t.manifest} m join ${t.table} c on c.${key} = m.${key} where m.release_id = ${release} and c.${column} is not null`)[0][0] !== '0';
        });
        if (hasData) {
          expect(nonEmpty, 'the check found nothing to compare').toBeGreaterThan(0);
        }
        expect(mismatches).toEqual([]);
      }, 600_000);
    }
  }
});
