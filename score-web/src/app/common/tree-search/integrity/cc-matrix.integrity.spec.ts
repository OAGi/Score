import {writeFileSync} from 'node:fs';
import {describe, it} from 'vitest';
import {ENABLED, sql} from './integrity-env';
import {CcRelease} from './cc-sql-oracle';
import {BieRow, CcTreeOracle, ONode, Rows, RowTrail, viewOf as viewOfCc} from './cc-tree-oracle';
import {Case, attributeCases, booleanCases} from './search-matrix';

/**
 * Writes the matrix of every attribute, operator, function and boolean shape for a root of the component trees together
 * with what the tables count for each (`SEARCH_MATRIX_OUT`), for the browser run that types them into the search bars.
 * `SEARCH_MATRIX_ROOTS` is `type:release:id,...`; a root of a BIE edit screen has the id of the BIE as a fourth part
 * (`ASCCP:77:742898:371`): its rows say the cardinality, fixed value and default value of the places they are the rows of.
 */
const ATTRIBUTES = {
  text: ['type', 'object_class_term', 'property_term', 'den', 'data_type_term', 'representation_term', 'qualifier', 'component_type',
    'object_class_qualifier', 'entity_type', 'state', 'guid'],
  flag: ['deprecated', 'abstract', 'reusable', 'commonly_used', 'nillable'],
  number: ['cardinality_min', 'cardinality_max'],
  tag: 'tag',
  cardinality: true
};

/** The text of a BIE tree that the backend answers (and the places that have none fall back to their core component). */
const SERVER_TEXT = ['fixed_value', 'default_value'];

/** The type of node a BIE-like tree (model browser, BIE edit) calls each type of component. */
const BIE_TYPES: Record<string, string> = {ACC: 'ABIE', ASCCP: 'ASBIEP', BCCP: 'BBIEP', DT_SC: 'BBIE_SC'};
const asBie = process.env['SEARCH_MATRIX_BIE_TYPES'] === '1';
/** The screen of a data type shows the data type alone in its tree. */
const rootOnly = process.env['SEARCH_MATRIX_ROOT_ONLY'] === '1';

/**
 * What a node says when asked. A BIE tree has no node of its own for an ACC (the ASCCP node stands for the ASCCP and
 * the ACC it is the role of, and the base ACC is inlined), so such a node says nothing; the root is the ABIE.
 */
function viewOf(node: ONode, root?: ONode, oracle?: CcTreeOracle, data?: CcRelease) {
  if (!asBie) {
    return viewOfCc(node);
  }
  if (node.type === 'ACC') {
    return undefined;
  }
  // a semantic group is not shown: what it holds is shown where it stands
  if (node.type === 'ASCCP' && node !== root && data && COMPONENT_TYPES[data.accs.get(node.asccp.roleOfAcc)?.componentType]?.endsWith('Group')) {
    return undefined;
  }
  if (node === root && oracle && node.type === 'ASCCP') {
    // the root is the ABIE of the ACC the ASCCP is the role of
    const acc = oracle.children(node)[0];
    return {...viewOfCc(acc), deprecated: !!(viewOfCc(acc)['deprecated'] || node.asccp.deprecated), type: 'ABIE'};
  }
  const rows = viewOfCc(node);
  // the node of an ASCCP is also its ACC, which can be deprecated too
  const accDeprecated = node.type === 'ASCCP' && oracle && !!oracle.children(node)[0]?.acc?.deprecated;
  return {...rows, ...valueConstraintOf(node), deprecated: !!rows['deprecated'] || accDeprecated, type: BIE_TYPES[node.type] ?? node.type};
}

/** The fixed value and the default value of a BBIEP or a BBIE_SC: the row of its BIE when it keeps one, else those of its BCCP or DT_SC. */
function valueConstraintOf(node: ONode): Rows {
  if (!withRows || (node.type !== 'BCCP' && node.type !== 'DT_SC')) {
    return {};
  }
  const component = node.type === 'BCCP' ? node.bccp : node.sc;
  const own = !!node.row && !!(node.row.fixed || node.row.defaultValue);
  return own ? {fixed_value: node.row.fixed, default_value: node.row.defaultValue}
    : {fixed_value: component.fixedValue, default_value: component.defaultValue};
}

/** The rows of a BIE that its edit screen is searched in (the matrix then also asks for its fixed and default values). */
let withRows = false;

function rowsOf(owner: number): {path: string, row: BieRow}[] {
  const value = (v: string) => v === 'NULL' || v === '' ? undefined : v;
  const rows: {path: string, row: BieRow}[] = [];
  for (const [path, used, min, max] of sql(`select path, is_used, cardinality_min, cardinality_max from asbie where owner_top_level_asbiep_id = ${owner}`)) {
    rows.push({path, row: {used: used === '1', min: Number(min), max: Number(max)}});
  }
  for (const table of ['bbie', 'bbie_sc']) {
    for (const [path, used, min, max, fixed, def] of sql(`select path, is_used, cardinality_min, cardinality_max, fixed_value, default_value from ${table} where owner_top_level_asbiep_id = ${owner}`)) {
      rows.push({path, row: {used: used === '1', min: Number(min), max: Number(max), fixed: value(fixed), defaultValue: value(def)}});
    }
  }
  return rows;
}

const COMPONENT_TYPES = ['Base', 'Semantics', 'Extension', 'SemanticGroup', 'UserExtensionGroup', 'Embedded', 'OAGIS10Nouns',
  'OAGIS10BODs', 'BOD', 'Verb', 'Noun', 'Choice', 'AttributeGroup'];

describe.skipIf(!ENABLED || !process.env['SEARCH_MATRIX_OUT'])('matrix of the component trees', () => {
  it('counts every case', () => {
    const out: Record<string, {group: string, expr: string, want: number}[]> = {};
    for (const spec of (process.env['SEARCH_MATRIX_ROOTS'] ?? '').split(',')) {
      const [type, release, id, owner] = spec.split(':');
      const data = new CcRelease(Number(release));
      const oracle = new CcTreeOracle(data, process.env['SEARCH_MATRIX_BLOCK_ACC'] === '1', 30_000_000, asBie);
      withRows = asBie && !!owner;
      const plainRoot = oracle.root(type as 'ACC' | 'ASCCP' | 'BCCP' | 'DT', Number(id));
      const root = withRows ? oracle.rootWith(plainRoot, RowTrail.of(rowsOf(Number(owner)))) : plainRoot;
      const harvest: Record<string, Set<string | number>> = {};
      oracle.count(root, node => {
        for (const [k, v] of Object.entries(viewOf(node, root, oracle, data) ?? {})) {
          if (v === undefined || typeof v === 'boolean' || k.startsWith('$')) { continue; }
          for (const x of Array.isArray(v) ? v : [v]) { (harvest[k] ??= new Set()).add(x as string | number); }
        }
        return false;
      });
      const values: Record<string, (string | number)[]> = {};
      for (const [k, set] of Object.entries(harvest)) {
        const sorted = [...set].filter(x => typeof x === 'string').map(String).sort((a, b) => a.length - b.length);
        // a short, a long and some in between, so that a value is not always the same type
        values[k] = k === 'guid' ? sorted.slice(0, 1) : [sorted[0], sorted[Math.floor(sorted.length / 3)], sorted[Math.floor(sorted.length * 2 / 3)], sorted[sorted.length - 1]]
          .filter((x, i, a) => x !== undefined && a.indexOf(x) === i);
      }
      if (asBie) {
        values['type'] = ['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC'];
      }
      const atoms = attributeCases(withRows ? {...ATTRIBUTES, text: [...ATTRIBUTES.text, ...SERVER_TEXT], serverText: SERVER_TEXT} : ATTRIBUTES, values);
      const cases: Case[] = [...atoms, ...booleanCases(atoms, 400)];
      out[spec] = cases.map(c => {
        const condition = '//*[' + c.condition + ']';
        return {group: c.group, expr: condition, want: oracle.count(root, node => { const rows = viewOf(node, root, oracle, data); return !!rows && (!rootOnly || node === root) && c.test(rows); })};
      });

      // what a node has below it: the nodes the tree shows as its children (a BIE tree shows an ACC and a group through)
      const shown = (node: ONode): ONode[] => oracle.kids(node, []).flatMap(child =>
        asBie && (child.type === 'ACC' || child.type === 'ASCCP' && viewOf(child, root, oracle, data) === undefined) ? shown(child) : [child]);
      const kidsOf = (node: ONode, blocked: boolean) => blocked || rootOnly ? [] : shown(node).filter(k => viewOf(k, root, oracle, data) !== undefined);
      const conditions = ['type = ' + (asBie ? 'BBIEP' : 'BCCP'), 'deprecated', 'cardinality_min = 0', 'entity_type = "Attribute"', 'nillable',
        'contains(den, "Code")', 'type = ' + (asBie ? 'BBIE_SC' : 'DT_SC'), 'not(deprecated)', 'cardinality_max > 1'];
      const byCondition = new Map(atoms.map(a => [a.condition, a]));
      const structure: {group: string, expr: string, test: (node: ONode, blocked: boolean) => boolean}[] = [];
      const compare = (op: string, a: number, b: number) => op === '=' ? a === b : op === '!=' ? a !== b : op === '<' ? a < b : op === '<=' ? a <= b : op === '>' ? a > b : a >= b;
      for (const op of ['=', '!=', '<', '<=', '>', '>=']) {
        for (const n of [0, 1, 2, 5, 20]) {
          structure.push({group: 'count(*)', expr: `//*[count(*) ${op} ${n}]`, test: (node, blocked) => compare(op, kidsOf(node, blocked).length, n)});
        }
      }
      structure.push({group: 'child exists', expr: '//*[*]', test: (node, blocked) => kidsOf(node, blocked).length > 0});
      structure.push({group: 'child absent', expr: '//*[not(*)]', test: (node, blocked) => kidsOf(node, blocked).length === 0});
      for (const condition of conditions) {
        const atom = byCondition.get(condition);
        if (!atom) { continue; }
        const test = (k: ONode) => atom.test(viewOf(k, root, oracle, data) ?? {});
        structure.push({group: 'child where', expr: `//*[*[${condition}]]`, test: (node, blocked) => kidsOf(node, blocked).some(test)});
        structure.push({group: 'no child where', expr: `//*[not(*[${condition}])]`, test: (node, blocked) => !kidsOf(node, blocked).some(test)});
        structure.push({group: 'every child', expr: `//*[count(*[${condition}]) = count(*)]`, test: (node, blocked) => kidsOf(node, blocked).every(test)});
        for (const op of ['=', '>', '>=', '<']) {
          for (const n of [0, 1, 3]) {
            structure.push({group: 'count(*[..])', expr: `//*[count(*[${condition}]) ${op} ${n}]`, test: (node, blocked) => compare(op, kidsOf(node, blocked).filter(test).length, n)});
          }
        }
        structure.push({group: 'own and child', expr: `//*[${condition} and *[${condition}]]`, test: (node, blocked) => !!viewOf(node, root, oracle, data) && test(node) && kidsOf(node, blocked).some(test)});
      }
      out[spec].push(...structure.map(c => ({group: c.group, expr: c.expr, want: oracle.count(root, (node, blocked) => !!viewOf(node, root, oracle, data) && (!rootOnly || node === root) && c.test(node, blocked))})));
      console.log(spec, 'cases', cases.length);
    }
    writeFileSync(process.env['SEARCH_MATRIX_OUT'], JSON.stringify(out));
  }, 3_600_000);
});
