import {describe, expect, it} from 'vitest';
import {BACKEND, ENABLED} from './integrity-env';
import {CcRelease} from './cc-sql-oracle';
import {CcTreeOracle, ONode, Rows, viewOf} from './cc-tree-oracle';
import {RELEASES, graphOf, sampleRoots} from './cc-graph-support';
import {CcFlatNode, CcFlatNodeDatabase} from '../../../cc-management/domain/cc-flat-tree';
import {CcGraph} from '../../../cc-management/domain/core-component-node';
import {DagPathSearch} from '../dag-path-search';
import {PathPattern} from '../path-pattern';

/**
 * The in-browser attributes of the component trees against the database: for roots of every type, what the search
 * engine counts over the tree built from the backend's graph (as a user's search does) is what an independent unfolding
 * of the tables counts, for expressions over every graph attribute and their combinations.
 */

type Test = (r: Rows) => boolean;

const text = (v: Rows[string]): string | undefined => v === undefined || Array.isArray(v) ? undefined : String(v);
const fold = (s: string, cs: boolean) => cs ? s : s.toLowerCase();

/** The expressions and what they mean on the rows. A missing value fails every comparison, `!=` included. */
function cases(cs: boolean): {expr: string, test: Test}[] {
  const eq = (a: string, v: string): Test => r => { const t = text(r[a]); return t !== undefined && fold(t, cs) === fold(v, cs); };
  const ne = (a: string, v: string): Test => r => { const t = text(r[a]); return t !== undefined && fold(t, cs) !== fold(v, cs); };
  const has = (a: string, v: string): Test => r => { const t = text(r[a]); return t !== undefined && fold(t, cs).includes(fold(v, cs)); };
  const starts = (a: string, v: string): Test => r => { const t = text(r[a]); return t !== undefined && fold(t, cs).startsWith(fold(v, cs)); };
  const ends = (a: string, v: string): Test => r => { const t = text(r[a]); return t !== undefined && fold(t, cs).endsWith(fold(v, cs)); };
  const flag = (a: string, v: boolean): Test => r => r[a] === v;
  const num = (a: string, op: string, n: number): Test => r => {
    const x = r[a];
    if (typeof x !== 'number') { return false; }
    return op === '=' ? x === n : op === '<' ? x < n : op === '<=' ? x <= n : op === '>' ? x > n : op === '>=' ? x >= n : x !== n;
  };
  const tag = (v: string): Test => r => Array.isArray(r['tag']) && (r['tag'] as string[]).some(t => fold(t, cs) === fold(v, cs));
  const and = (...t: Test[]): Test => r => t.every(x => x(r));
  const or = (...t: Test[]): Test => r => t.some(x => x(r));
  const not = (t: Test): Test => r => !t(r);
  const list: {expr: string, test: Test}[] = [
    {expr: '//*[deprecated = true]', test: flag('deprecated', true)},
    {expr: '//*[deprecated = false]', test: flag('deprecated', false)},
    {expr: '//*[deprecated]', test: flag('deprecated', true)},
    {expr: '//*[not(deprecated)]', test: not(flag('deprecated', true))},
    {expr: '//*[abstract = true]', test: flag('abstract', true)},
    {expr: '//*[abstract = false]', test: flag('abstract', false)},
    {expr: '//*[reusable = false]', test: flag('reusable', false)},
    {expr: '//*[reusable = true]', test: flag('reusable', true)},
    {expr: '//*[commonly_used = true]', test: flag('commonly_used', true)},
    {expr: '//*[nillable = true]', test: flag('nillable', true)},
    {expr: '//*[nillable = false]', test: flag('nillable', false)},
    {expr: '//*[not(nillable)]', test: not(flag('nillable', true))},
    {expr: '//*[type = ACC]', test: eq('type', 'ACC')},
    {expr: '//*[type = ASCCP]', test: eq('type', 'ASCCP')},
    {expr: '//*[type = BCCP]', test: eq('type', 'BCCP')},
    {expr: '//*[type = DT_SC]', test: eq('type', 'DT_SC')},
    {expr: '//*[type = DT]', test: eq('type', 'DT')},
    {expr: '//*[type != ACC]', test: ne('type', 'ACC')},
    {expr: '//*[state = "Published"]', test: eq('state', 'Published')},
    {expr: '//*[state = "published"]', test: eq('state', 'published')},
    {expr: '//*[state != "Published"]', test: ne('state', 'Published')},
    {expr: '//*[state = "WIP"]', test: eq('state', 'WIP')},
    {expr: '//*[contains(den, "Code")]', test: has('den', 'Code')},
    {expr: '//*[contains(den, "code")]', test: has('den', 'code')},
    {expr: '//*[starts-with(den, "A")]', test: starts('den', 'A')},
    {expr: '//*[ends-with(den, "Details")]', test: ends('den', 'Details')},
    {expr: '//*[den = "Identifier. Type"]', test: eq('den', 'Identifier. Type')},
    {expr: '//*[den != "Identifier. Type"]', test: ne('den', 'Identifier. Type')},
    {expr: '//*[contains(property_term, "Identifier")]', test: has('property_term', 'Identifier')},
    {expr: '//*[property_term = "Identifier"]', test: eq('property_term', 'Identifier')},
    {expr: '//*[property_term != "Identifier"]', test: ne('property_term', 'Identifier')},
    {expr: '//*[contains(object_class_term, "e")]', test: has('object_class_term', 'e')},
    {expr: '//*[object_class_term = "Code"]', test: eq('object_class_term', 'Code')},
    {expr: '//*[data_type_term = "Code"]', test: eq('data_type_term', 'Code')},
    {expr: '//*[data_type_term != "Code"]', test: ne('data_type_term', 'Code')},
    {expr: '//*[representation_term = "Identifier"]', test: eq('representation_term', 'Identifier')},
    {expr: '//*[qualifier = ""]', test: eq('qualifier', '')},
    {expr: '//*[qualifier != "x"]', test: ne('qualifier', 'x')},
    {expr: '//*[contains(qualifier, "a")]', test: has('qualifier', 'a')},
    {expr: '//*[qualifier]', test: r => { const t = text(r['qualifier']); return t !== undefined && t !== ''; }},
    {expr: '//*[object_class_qualifier]', test: r => { const t = text(r['object_class_qualifier']); return t !== undefined && t !== ''; }},
    {expr: '//*[contains(object_class_qualifier, "e")]', test: has('object_class_qualifier', 'e')},
    {expr: '//*[object_class_qualifier != "x"]', test: ne('object_class_qualifier', 'x')},
    {expr: '//*[component_type = "Extension"]', test: eq('component_type', 'Extension')},
    {expr: '//*[component_type != "Semantics"]', test: ne('component_type', 'Semantics')},
    {expr: '//*[entity_type = "Attribute"]', test: eq('entity_type', 'Attribute')},
    {expr: '//*[entity_type != "Attribute"]', test: ne('entity_type', 'Attribute')},
    {expr: '//*[entity_type = "Element"]', test: eq('entity_type', 'Element')},
    {expr: '//*[contains(guid, "a")]', test: has('guid', 'a')},
    {expr: '//*[tag = "Core"]', test: tag('Core')},
    {expr: '//*[tag = "core"]', test: tag('core')},
    {expr: '//*[tag]', test: r => Array.isArray(r['tag']) && r['tag'].length > 0},
    {expr: '//*[cardinality_min = 1]', test: num('cardinality_min', '=', 1)},
    {expr: '//*[cardinality_min = 0]', test: num('cardinality_min', '=', 0)},
    {expr: '//*[cardinality_min >= 1]', test: num('cardinality_min', '>=', 1)},
    {expr: '//*[cardinality_max < 10]', test: num('cardinality_max', '<', 10)},
    {expr: '//*[cardinality_max = 1]', test: num('cardinality_max', '=', 1)},
    {expr: '//*[cardinality_max > 1]', test: num('cardinality_max', '>', 1)},
    {expr: '//*[cardinality_max != 1]', test: num('cardinality_max', '!=', 1)},
    {expr: '//*[cardinality = 0..1]', test: and(num('cardinality_min', '=', 0), num('cardinality_max', '=', 1))},
    {expr: '//*[cardinality = 1..*]', test: and(num('cardinality_min', '=', 1), num('cardinality_max', '=', Infinity))},
    {expr: '//*[cardinality != Optional]', test: r => typeof r['cardinality_min'] === 'number' && typeof r['cardinality_max'] === 'number'
      && !(r['cardinality_min'] === 0 && r['cardinality_max'] === 1)},
    // combinations
    {expr: '//*[type = BCCP and deprecated]', test: and(eq('type', 'BCCP'), flag('deprecated', true))},
    {expr: '//*[type = ASCCP or type = BCCP]', test: or(eq('type', 'ASCCP'), eq('type', 'BCCP'))},
    {expr: '//*[(deprecated or nillable) and not(type = DT_SC)]', test: and(or(flag('deprecated', true), flag('nillable', true)), not(eq('type', 'DT_SC')))},
    {expr: '//*[not(type = ACC) and not(type = DT_SC)]', test: and(not(eq('type', 'ACC')), not(eq('type', 'DT_SC')))},
    {expr: '//*[state != "Published" and contains(den, "Code")]', test: and(ne('state', 'Published'), has('den', 'Code'))},
    {expr: '//*[(entity_type = "Attribute" or cardinality_max = 1) and not(deprecated)]', test: and(or(eq('entity_type', 'Attribute'), num('cardinality_max', '=', 1)), not(flag('deprecated', true)))},
    {expr: '//*[type = ASCCP and not(reusable) and cardinality_min = 0]', test: and(eq('type', 'ASCCP'), not(flag('reusable', true)), num('cardinality_min', '=', 0))},
    {expr: '//*[qualifier != "x" or deprecated]', test: or(ne('qualifier', 'x'), flag('deprecated', true))},
    {expr: '//*[not(qualifier != "x")]', test: not(ne('qualifier', 'x'))},
    {expr: '//*[not(contains(den, "Code")) and type = BCCP]', test: and(not(has('den', 'Code')), eq('type', 'BCCP'))},
    {expr: '//*[contains(den, "Code") or contains(property_term, "Code")]', test: or(has('den', 'Code'), has('property_term', 'Code'))},
    {expr: '//*[tag = "Core" or deprecated]', test: or(tag('Core'), flag('deprecated', true))}
  ];
  return list;
}

function pathOf(root: CcFlatNode): CcFlatNode[] {
  const chain: CcFlatNode[] = [];
  for (let n = root; n; n = n.parent as CcFlatNode) {
    chain.unshift(n);
  }
  return chain;
}

describe.skipIf(!ENABLED)('CC search engine against an SQL unfolding (' + BACKEND + ')', () => {
  for (const release of RELEASES) {
    it(`release ${release}: the tree has the size the tables say`, async () => {
      const data = new CcRelease(release);
      const oracle = new CcTreeOracle(data, process.env['SEARCH_INTEGRITY_BLOCK_ACC'] === '1');
      const problems: string[] = [];
      for (const root of sampleRoots(release)) {
        const graph = await graphOf(root.type, root.id) as unknown as CcGraph;
        const database = new CcFlatNodeDatabase<CcFlatNode>(graph, root.type, root.id);
        const rootNode = database.rootNode;
        const searchGraph = database.searchGraph(rootNode);
        const pattern = new PathPattern('//*', false);
        const result = new DagPathSearch(searchGraph.dag, pattern).search({
          ancestors: [], record: searchGraph.recordOf(rootNode), mask: pattern.stepAll([[rootNode.name]])
        });
        const want = oracle.count(oracle.root(root.type, root.id), () => true);
        if (result.nodes !== want) {
          problems.push(`${root.type} ${root.id}: engine ${result.nodes} SQL ${want}`);
        }
      }
      expect(problems).toEqual([]);
    }, 900_000);
  }

  for (const release of RELEASES) {
    for (const caseSensitive of [false, true]) {
      it(`release ${release}${caseSensitive ? ' (case sensitive)' : ''}: every expression counts what the tables say`, async () => {
        const data = new CcRelease(release);
        const oracle = new CcTreeOracle(data, process.env['SEARCH_INTEGRITY_BLOCK_ACC'] === '1');
        const problems: string[] = [];
        let compared = 0;
        let nonZero = 0;
        let skipped = 0;
        for (const root of sampleRoots(release)) {
          const graph = await graphOf(root.type, root.id) as unknown as CcGraph;
          const database = new CcFlatNodeDatabase<CcFlatNode>(graph, root.type, root.id);
          const rootNode = database.rootNode;
          const searchGraph = database.searchGraph(rootNode);
          if (!searchGraph) {
            problems.push(`${root.type} ${root.id}: no search graph`);
            continue;
          }
          const rootNodeO: ONode = oracle.root(root.type, root.id);
          for (const {expr, test} of cases(caseSensitive)) {
            let want: number;
            try {
              want = oracle.count(rootNodeO, node => test(viewOf(node)));
            } catch (e) {
              skipped++;
              continue;
            }
            const pattern = new PathPattern(expr, caseSensitive);
            const chain = pathOf(rootNode);
            const result = new DagPathSearch(searchGraph.dag, pattern).search({
              ancestors: chain.slice(0, -1).map(n => searchGraph.recordOf(n)),
              record: searchGraph.recordOf(rootNode),
              mask: pattern.stepAll(chain.map(n => [n.name]), chain.map(n => (attribute: string) => searchGraph.dag.attributes?.[attribute]?.[searchGraph.recordOf(n)]))
            });
            compared++;
            nonZero += want > 0 ? 1 : 0;
            if (!result.exact || result.total !== want) {
              problems.push(`${root.type} ${root.id} ${expr}: engine ${result.total}${result.exact ? '' : ' (budget)'} SQL ${want}`);
            }
          }
        }
        console.log(`release ${release} cs=${caseSensitive}: compared ${compared}, non-zero ${nonZero}, skipped ${skipped}, problems ${problems.length}`);
        expect(compared).toBeGreaterThan(100);
        expect(nonZero).toBeGreaterThan(20);
        expect(problems.slice(0, 60)).toEqual([]);
      }, 1_800_000);
    }
  }
});
