import {describe, expect, it} from 'vitest';
import {BACKEND, ENABLED, sql} from './integrity-env';
import {graphOf, sampleRoots} from './cc-graph-support';
import {ModelBrowserAccNode, ModelBrowserAsccpNode, ModelBrowserNode, ModelBrowserNodeDatabase, ModelBrowserNodeDataSource} from '../../../cc-management/model-browser/domain/model-browser-node';
import {CcGraph} from '../../../cc-management/domain/core-component-node';
import {DagPathSearch} from '../dag-path-search';
import {PathPattern} from '../path-pattern';

/**
 * The model browser shows the component trees the way a BIE is shown: its search runs on a graph of the same
 * structure as the screen unfolds, so the search covers exactly the nodes the screen shows, and what it says about a node
 * (the CC attributes the BIE trees share) is what the walk of the tree says.
 */
describe.skipIf(!ENABLED)('Model browser search against the tree (' + BACKEND + ')', () => {
  for (const release of [77, 10]) {
    it(`release ${release}: the search covers what the tree shows`, async () => {
      const problems: string[] = [];
      let compared = 0;
      for (const root of sampleRoots(release).filter(r => r.type === 'ASCCP')) {
        const graph = await graphOf(root.type, root.id) as unknown as CcGraph;
        const database = new ModelBrowserNodeDatabase<ModelBrowserNode>(graph, root.type, root.id);
        const dataSource = new ModelBrowserNodeDataSource<ModelBrowserNode>(database, null, []);
        database.dataSource = dataSource;
        const rootNode = database.rootNode;
        let walked = 0;
        let walkedDeprecated = 0;
        let budget = 150_000;
        const walk = (node: ModelBrowserNode): void => {
          if (--budget < 0) {
            throw new Error('too big to walk');
          }
          walked++;
          walkedDeprecated += node.deprecated ? 1 : 0;
          if (!((node instanceof ModelBrowserAsccpNode || node instanceof ModelBrowserAccNode) && node.isCycle)) {
            if (node.children.length === 0) {
              database.loadChildren(node);
            }
            database.children(node).forEach(walk);
          }
        };
        try {
          walk(rootNode);
        } catch (e) {
          if ((e as Error).message !== 'too big to walk') {
            throw e;
          }
          continue;
        }
        const searchGraph = database.searchGraph(rootNode);
        const run = (expr: string) => {
          const pattern = new PathPattern(expr);
          return new DagPathSearch(searchGraph.dag, pattern).search({
            ancestors: [], record: searchGraph.recordOf(rootNode),
            mask: pattern.stepAll([[rootNode.name]], [(a: string) => searchGraph.dag.attributes?.[a]?.[searchGraph.recordOf(rootNode)]])
          });
        };
        compared++;
        const size = run('//*').nodes;
        if (size !== walked) {
          problems.push(`${root.type} ${root.id}: engine ${size} tree ${walked}`);
        }
        const deprecated = run('//*[deprecated]').total;
        if (deprecated !== walkedDeprecated) {
          problems.push(`${root.type} ${root.id} deprecated: engine ${deprecated} tree ${walkedDeprecated}`);
        }
      }
      expect(compared).toBeGreaterThan(0);
      expect(problems).toEqual([]);
    }, 900_000);
  }
});
