import {ExpressionEvaluator, FlatNode} from '../flat-tree';
import {TreeGraphAdapter} from './graph-tree-search';
import {PathPattern} from './path-pattern';

/**
 * Judges one node of a tree against a search expression by reading the chain of nodes down to it, with the same
 * `PathPattern` (and so the same predicates, names and anchoring) the graph search uses. It is the way to search a
 * tree that has to be walked.
 */
export class PatternExpressionEvaluator<N extends FlatNode> implements ExpressionEvaluator<N> {

  private readonly pattern: PathPattern;

  constructor(expr: string,
              private readonly adapter: Pick<TreeGraphAdapter<N>, 'namesOf' | 'attributesOf' | 'strictRootedPaths'>,
              caseSensitive = false) {
    const mode = adapter.strictRootedPaths && expr.startsWith('/') ? {exact: true, anchored: true} : {};
    this.pattern = new PathPattern(expr, caseSensitive, mode);
  }

  range(data: N[], current: N): N[] {
    return [];
  }

  eval(node: N): boolean {
    const chain: N[] = [];
    for (let n = node; n; n = n.parent as N) {
      if (this.adapter.namesOf(n)) {
        chain.push(n);
      }
    }
    chain.reverse();
    const lookups = this.pattern.hasPredicates ?
      chain.map(n => this.adapter.attributesOf?.(n)).map(values => values && ((attribute: string) => values[attribute])) : undefined;
    return this.pattern.isMatch(this.pattern.stepAll(chain.map(n => this.adapter.namesOf(n)), lookups));
  }

  keywordForHighlight(): string {
    return this.pattern.highlight;
  }
}
