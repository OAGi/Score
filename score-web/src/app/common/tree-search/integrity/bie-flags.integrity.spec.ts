import {describe, expect, it} from 'vitest';
import {BACKEND, ENABLED, backend, sql, sqlNumber} from './integrity-env';
import {BieFlatNode, BieFlatNodeDataSource, BieFlatNodeDatabase} from '../../../bie-management/domain/bie-flat-tree';
import {CcGraph} from '../../../cc-management/domain/core-component-node';
import {DagPathSearch} from '../dag-path-search';
import {PathPattern} from '../path-pattern';

/**
 * The attributes of the BIE trees that come from what is set on the BIE (used, nillable, deprecated, display
 * name) against SQL over the BIE's own rows: the edit tree is loaded as the screen loads it, and what a search counts
 * has to be what the rows say.
 */

function sqlOne(q: string): string { return sql(q)[0]?.[0]; }

const graphs = new WeakMap<object, CcGraph>();

async function database(owner: number): Promise<BieFlatNodeDatabase<BieFlatNode>> {
  const [graph, used, refs, root] = await Promise.all([
    backend<CcGraph>('/graphs/top_level_asbiep/' + owner),
    backend<any[]>('/profile_bie/' + owner + '/used_list'),
    backend<any[]>('/profile_bie/' + owner + '/ref_list'),
    backend<any>('/profile_bie/node/root/' + owner)
  ]);
  const db = new BieFlatNodeDatabase<BieFlatNode>(graph, root, owner, used, refs);
  graphs.set(db, graph);
  return db;
}

function total(db: BieFlatNodeDatabase<BieFlatNode>, expr: string): number {
  return totalFrom(db, db.rootNode, expr);
}

function totalFrom(db: BieFlatNodeDatabase<BieFlatNode>, root: BieFlatNode, expr: string): number {
  const graph = db.searchGraph(root);
  const pattern = new PathPattern(expr);
  const lookup = (attribute: string) => graph.dag.attributes?.[attribute]?.[graph.recordOf(root)];
  return new DagPathSearch(graph.dag, pattern).search({
    ancestors: [], record: graph.recordOf(root), mask: pattern.stepAll([[root.name]], [lookup])
  }).total;
}

describe.skipIf(!ENABLED)('BIE flags against SQL (' + BACKEND + ')', () => {
  /**
   * The used rows of a BIE that the edit screen can show, from the rows alone: a row is shown when its path is one of the CC graph (a row saved under an older structure is not), every ASBIE above it is used, no User Extension Group
   * is above it (the edit tree does not show them) and no ASCCP comes twice above it (the second one is shown, but not
   * expanded). A used row below an unused ASBIE is shown as unused, whatever its row says.
   */
  function shownRows(owner: number, userExtensionGroups: Set<string>, graph?: CcGraph): {bbie: string[], asbie: string[], sc: string[]} {
    const asbies = sql(`select path, is_used, asbie_id from asbie where owner_top_level_asbiep_id = ${owner}`);
    const used = new Map(asbies.map(([path, flag]) => [path, flag === '1']));
    const edges = graph?.graph.edges ?? {};
    /** Whether every step of the path is an edge of the CC graph (a row saved under an older structure can have a path that is none). */
    const walkable = (path: string): boolean => {
      const steps = path.replace(/>BCCP-.*$/, '').split('>');
      return !graph || steps.every((step, i) => i === 0 || edges[steps[i - 1]]?.targets.includes(step));
    };
    const shown = (path: string): boolean => {
      if (!walkable(path)) {
        return false;
      }
      const ascc = [...path.matchAll(/ASCCP-(\d+)/g)].map(m => m[1]);
      if (ascc.length !== new Set(ascc).size || ascc.some(id => userExtensionGroups.has(id))) {
        return false;
      }
      // every ASBIE above the row: its path is a proper prefix of the row's path; what is below an unused node is unused
      return asbies.every(([above]) => !path.startsWith(above + '>') || used.get(above));
    };
    const ids = (rows: string[][]) => rows.filter(([path, flag]) => flag === '1' && shown(path)).map(r => r[2]);
    return {
      bbie: ids(sql(`select path, is_used, bbie_id from bbie where owner_top_level_asbiep_id = ${owner}`)),
      asbie: ids(asbies.map(([path, flag, id]) => [path.substring(0, path.lastIndexOf('>')), flag, id])),
      sc: ids(sql(`select s.path, b.is_used, s.bbie_sc_id from bbie_sc s join bbie b on b.bbie_id = s.bbie_id where s.owner_top_level_asbiep_id = ${owner} and s.is_used = 1`)
        .map(([path, flag, id]) => [path.replace(/>BCCP-.*$/, ''), flag, id]))
    };
  }

  /** What the screen can show of a BIE: its own used rows, and those of every BIE it reuses, at each place it reuses it. */
  function shownCounts(owner: number, userExtensionGroups: Set<string>, graphOf: (owner: number) => CcGraph | undefined, visiting: number[] = []): number[] {
    const rows = shownRows(owner, userExtensionGroups, graphOf(owner));
    const total = [rows.bbie.length, rows.asbie.length, rows.sc.length];
    if (visiting.includes(owner)) {
      return total;
    }
    const shownAsbies = new Set(rows.asbie);
    for (const [id, reused] of sql(`select a.asbie_id, p.owner_top_level_asbiep_id from asbie a join asbiep p on p.asbiep_id = a.to_asbiep_id ` +
      `where a.owner_top_level_asbiep_id = ${owner} and a.is_used = 1 and p.owner_top_level_asbiep_id <> ${owner}`)) {
      if (shownAsbies.has(id)) {
        shownCounts(Number(reused), userExtensionGroups, graphOf, [...visiting, owner]).forEach((n, i) => total[i] += n);
      }
    }
    return total;
  }

  it('finds every used row the edit screen can show, in every BIE of the data', async () => {
    const owners = sql('select top_level_asbiep_id from top_level_asbiep order by 1').map(r => Number(r[0]));
    const userExtensionGroups = new Set(sql(`select m.asccp_manifest_id from asccp_manifest m join asccp a on a.asccp_id = m.asccp_id where a.property_term like '%User Extension Group'`).map(r => r[0]));
    const graphsByOwner = new Map<number, CcGraph>();
    const classes: Record<string, string[]> = {equal: [], 'engine has more': [], 'engine has fewer': [], error: []};
    for (const owner of owners) {
      let db: BieFlatNodeDatabase<BieFlatNode>;
      try { db = await database(owner); } catch (e) { continue; }
      const reusedOwners = async (o: number, seen: number[] = []): Promise<void> => {
        if (seen.includes(o) || graphsByOwner.has(o)) { return; }
        try { graphsByOwner.set(o, await backend<CcGraph>('/graphs/top_level_asbiep/' + o)); } catch (e) { return; }
        for (const [r] of sql(`select distinct p.owner_top_level_asbiep_id from asbie a join asbiep p on p.asbiep_id = a.to_asbiep_id where a.owner_top_level_asbiep_id = ${o} and p.owner_top_level_asbiep_id <> ${o}`)) {
          await reusedOwners(Number(r), [...seen, o]);
        }
      };
      await reusedOwners(owner);
      const want = shownCounts(owner, userExtensionGroups, o => graphsByOwner.get(o));
      let got: number[];
      try {
        const root = db.rootNode;
        const source = new BieFlatNodeDataSource<BieFlatNode>(db, null, null, []);
        source.data = [root];
        if (process.env['SEARCH_INTEGRITY_INIT'] === '1') { source.init(); }
        got = ['BBIEP', 'ASBIEP', 'BBIE_SC'].map(t => totalFrom(db, source.data[0], `//*[type = ${t} and used]`));
      } catch (e) { classes['error'].push(`BIE ${owner}: ${(e as Error).message}`); continue; }
      const cls = got.every((g, i) => g === want[i]) ? 'equal' : got.some((g, i) => g < want[i]) ? 'engine has fewer' : 'engine has more';
      classes[cls].push(`BIE ${owner}: reachable ${want} engine ${got}`);
    }
    const states = (list: string[]) => {
      const ids = list.map(l => Number(/BIE (\d+)/.exec(l)[1]));
      return ids.length === 0 ? '' : JSON.stringify(sql(`select state, count(*) from top_level_asbiep where top_level_asbiep_id in (${ids.join(',')}) group by 1`));
    };
    for (const [name, list] of Object.entries(classes)) { console.log('STATES', name, states(list)); }
    for (const [name, list] of Object.entries(classes)) { console.log('CLASS', name, list.length, '\n' + list.slice(0, 12).join('\n')); }
    // Every BIE: what the rows say is used, and shown, is what a search finds, and nothing else (below an unused node a row is unused).
    expect(classes['engine has fewer']).toEqual([]);
    expect(classes['engine has more']).toEqual([]);
    expect(classes['error']).toEqual([]);
    expect(classes['equal'].length).toBeGreaterThan(100);
  }, 1800000);

  it('finds what is set on a BIE (nillable, deprecated, display name), with the data set up for it', async () => {
    const owner = 710;
    const userExtensionGroups = new Set(sql(`select m.asccp_manifest_id from asccp_manifest m join asccp a on a.asccp_id = m.asccp_id where a.property_term like '%User Extension Group'`).map(r => r[0]));
    const rows = shownRows(owner, userExtensionGroups);
    const plainBbie = (condition: string) => sql(`select b.bbie_id from bbie b join bcc_manifest bm on bm.bcc_manifest_id = b.based_bcc_manifest_id ` +
      `join bcc c on c.bcc_id = bm.bcc_id join bccp_manifest pm on pm.bccp_manifest_id = bm.to_bccp_manifest_id join bccp p on p.bccp_id = pm.bccp_id ` +
      `where b.owner_top_level_asbiep_id = ${owner} and b.bbie_id in (${rows.bbie.join(',')}) and ${condition} limit 1`)[0][0];
    const forNillable = plainBbie('b.is_nillable = 0 and c.is_nillable = 0 and p.is_nillable = 0');
    const forDeprecated = plainBbie('b.is_deprecated = 0 and c.is_deprecated = 0 and p.is_deprecated = 0');
    const asbiep = sql(`select a.to_asbiep_id from asbie a where a.asbie_id in (${rows.asbie.join(',')}) limit 1`)[0][0];
    const bbiep = sql(`select b.to_bbiep_id from bbie b where b.bbie_id in (${rows.bbie.join(',')}) limit 1`)[0][0];
    const sc = rows.sc[0];

    const counts = async () => {
      const db = await database(owner);
      return {
        nillable: total(db, '//*[used and nillable]'),
        deprecated: total(db, '//*[used and deprecated]'),
        named: total(db, '//*[display_name = "Zq Fixture"]'),
        namedContains: total(db, '//*[contains(display_name, "zQ fix")]'),
        namedCs: (() => { const root = db.rootNode; const g = db.searchGraph(root); const pattern = new PathPattern('//*[display_name = "zq fixture"]', true);
          return new DagPathSearch(g.dag, pattern).search({ancestors: [], record: g.recordOf(root), mask: pattern.stepAll([[root.name]], [a => g.dag.attributes?.[a]?.[g.recordOf(root)]])}).total; })()
      };
    };
    const before = await counts();
    try {
      sql(`update bbie set is_nillable = 1 where bbie_id = ${forNillable}; update bbie set is_deprecated = 1 where bbie_id = ${forDeprecated}; ` +
        `update asbiep set display_name = 'Zq Fixture' where asbiep_id = ${asbiep}; update bbiep set display_name = 'Zq Fixture' where bbiep_id = ${bbiep}; ` +
        `update bbie_sc set display_name = 'Zq Fixture' where bbie_sc_id = ${sc}`);
      const after = await counts();
      expect(after.nillable - before.nillable).toBe(1);
      expect(after.deprecated - before.deprecated).toBe(1);
      expect(after.named).toBe(3);
      expect(after.namedContains).toBe(3);
      expect(after.namedCs).toBe(0);
    } finally {
      sql(`update bbie set is_nillable = 0 where bbie_id = ${forNillable}; update bbie set is_deprecated = 0 where bbie_id = ${forDeprecated}; ` +
        `update asbiep set display_name = null where asbiep_id = ${asbiep}; update bbiep set display_name = null where bbiep_id = ${bbiep}; update bbie_sc set display_name = null where bbie_sc_id = ${sc}`);
    }
    expect(await counts()).toEqual(before);
  }, 600000);
});
