import {describe, expect, it} from 'vitest';
import {BACKEND, ENABLED} from './integrity-env';
import {COMPONENT_TYPES, GraphNode, RELEASES, graphOf, sampleRoots} from './cc-graph-support';
import {CcRelease} from './cc-sql-oracle';

/**
 * What the in-browser attributes of the component trees are made of: the CC graph the backend serves. For roots of
 * every type, in several releases, the graph has to hold exactly the components the database says are reachable from
 * the root, and every node has to say about itself what its row says (the attributes the searches judge).
 */

describe.skipIf(!ENABLED)('CC graph against SQL (' + BACKEND + ')', () => {
  for (const release of RELEASES) {
    it(`release ${release}: the graph of every root is what the tables say`, async () => {
      const data = new CcRelease(release);
      const problems: string[] = [];
      const check = (where: string, got: unknown, want: unknown) => {
        compared++;
        if (JSON.stringify(got ?? null) !== JSON.stringify(want ?? null)) {
          problems.push(`${where}: graph ${JSON.stringify(got)} SQL ${JSON.stringify(want)}`);
        }
      };
      const roots = sampleRoots(release);
      expect(roots.length).toBeGreaterThan(2);
      let compared = 0;
      for (const root of roots) {
        const {graph} = await graphOf(root.type, root.id);
        const want = data.reachable(root.type, root.id);
        const types: Record<string, Set<number>> = {ACC: want.accs, ASCC: want.asccs, ASCCP: want.asccps, BCC: want.bccs,
          BCCP: want.bccps, DT: want.dts, DT_SC: want.dtScs};
        for (const [type, ids] of Object.entries(types)) {
          const got = Object.values(graph.nodes).filter(n => n.type === type).map(n => n.manifestId).sort((a, b) => a - b);
          check(`${root.type} ${root.id} ${type} set`, got, [...ids].sort((a, b) => a - b));
        }
        for (const node of Object.values(graph.nodes)) {
          const where = `${node.type} ${node.manifestId}`;
          const tags = (n: GraphNode) => ((n['tagList'] as {name: string}[]) ?? []).map(t => t.name).sort();
          switch (node.type) {
            case 'ACC': {
              const a = data.accs.get(node.manifestId);
              if (!a) { break; }
              check(where + ' state', node['state'], a.state);
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' guid', node['guid'], a.guid);
              check(where + ' objectClassTerm', node['objectClassTerm'], a.objectClassTerm);
              check(where + ' den', node['den'], a.den);
              check(where + ' componentType', node['componentType'], COMPONENT_TYPES[a.componentType]);
              check(where + ' abstract', node['abstract'], a.abstract);
              check(where + ' objectClassQualifier', node['objectClassQualifier'], a.objectClassQualifier ? a.objectClassQualifier : undefined);
              check(where + ' tags', tags(node), a.tags);
              break;
            }
            case 'ASCC': {
              const a = data.asccs.get(node.manifestId);
              if (!a) { break; }
              check(where + ' state', node['state'], a.state);
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' min', node['cardinalityMin'], a.min);
              check(where + ' max', node['cardinalityMax'], a.max);
              break;
            }
            case 'ASCCP': {
              const a = data.asccps.get(node.manifestId);
              if (!a) { break; }
              check(where + ' state', node['state'], a.state);
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' guid', node['guid'], a.guid);
              check(where + ' propertyTerm', node['propertyTerm'], a.propertyTerm);
              check(where + ' den', node['den'], a.den);
              check(where + ' reusable', node['reusable'], a.reusable);
              check(where + ' nillable', node['nillable'], a.nillable);
              check(where + ' tags', tags(node), a.tags);
              break;
            }
            case 'BCC': {
              const a = data.bccs.get(node.manifestId);
              if (!a) { break; }
              check(where + ' state', node['state'], a.state);
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' min', node['cardinalityMin'], a.min);
              check(where + ' max', node['cardinalityMax'], a.max);
              check(where + ' entityType', node['entityType'], a.entityType === 0 ? 'Attribute' : 'Element');
              check(where + ' nillable', node['nillable'], a.nillable);
              break;
            }
            case 'BCCP': {
              const a = data.bccps.get(node.manifestId);
              if (!a) { break; }
              check(where + ' state', node['state'], a.state);
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' guid', node['guid'], a.guid);
              check(where + ' propertyTerm', node['propertyTerm'], a.propertyTerm);
              check(where + ' den', node['den'], a.den);
              check(where + ' nillable', node['nillable'], a.nillable);
              check(where + ' tags', tags(node), a.tags);
              break;
            }
            case 'DT': {
              const a = data.dts.get(node.manifestId);
              if (!a) { break; }
              check(where + ' state', node['state'], a.state);
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' guid', node['guid'], a.guid);
              check(where + ' dataTypeTerm', node['dataTypeTerm'], a.dataTypeTerm);
              check(where + ' den', node['den'], a.den);
              check(where + ' qualifier', node['qualifier'], a.qualifier);
              check(where + ' commonlyUsed', node['commonlyUsed'], a.commonlyUsed);
              check(where + ' tags', tags(node), a.tags);
              break;
            }
            case 'DT_SC': {
              const a = data.dtScs.get(node.manifestId);
              if (!a) { break; }
              check(where + ' deprecated', node['deprecated'], a.deprecated);
              check(where + ' guid', node['guid'], a.guid);
              check(where + ' objectClassTerm', node['objectClassTerm'], a.objectClassTerm);
              check(where + ' propertyTerm', node['propertyTerm'], a.propertyTerm);
              check(where + ' representationTerm', node['representationTerm'], a.representationTerm);
              check(where + ' min', node['cardinalityMin'], a.min);
              check(where + ' max', node['cardinalityMax'], a.max);
              break;
            }
          }
        }
      }
      expect(compared, 'nothing was compared').toBeGreaterThan(50);
      expect(problems.slice(0, 40)).toEqual([]);
    }, 900_000);
  }
});
