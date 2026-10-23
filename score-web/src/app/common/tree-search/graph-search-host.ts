import {DagPathSearch, DagSearchResult, DagSearchScope} from './dag-path-search';
import {PathPattern, PathPatternMode} from './path-pattern';
import {SearchDag} from './search-dag';

/** What a client sends to the search host (the worker, or the in-thread stand-in). `session` tells whose DAG it is. */
export type GraphSearchMessage =
  | {type: 'load', session: number, dag: SearchDag}
  | {type: 'search', session: number, id: number, expr: string, caseSensitive: boolean, mode: PathPatternMode, scope: DagSearchScope}
  | {type: 'locate', session: number, id: number, k: number}
  | {type: 'close', session: number};

export type GraphSearchReply =
  | {type: 'progress', id: number, visited: number}
  | {type: 'result', id: number, result: DagSearchResult}
  | {type: 'located', id: number, path: number[] | undefined}
  | {type: 'failed', id: number, message: string};

/**
 * The server side of a graph search: keeps each session's DAG and its latest search, and answers the messages.
 * It has no knowledge of workers, so the same code runs in a Web Worker and, in tests or where workers are not
 * available, in the page's own thread.
 */
export class GraphSearchHost {

  private readonly sessions = new Map<number, {dag?: SearchDag, search?: DagPathSearch}>();

  handle(message: GraphSearchMessage, reply: (reply: GraphSearchReply) => void) {
    switch (message.type) {
      case 'load':
        this.sessions.set(message.session, {dag: message.dag});
        return;
      case 'close':
        this.sessions.delete(message.session);
        return;
      case 'search':
        return this.search(message, reply);
      case 'locate':
        return this.locate(message, reply);
    }
  }

  private search(message: Extract<GraphSearchMessage, {type: 'search'}>, reply: (reply: GraphSearchReply) => void) {
    const session = this.sessions.get(message.session);
    if (!session?.dag) {
      reply({type: 'failed', id: message.id, message: 'No DAG loaded for the session.'});
      return;
    }
    const search = new DagPathSearch(session.dag, new PathPattern(message.expr, message.caseSensitive, message.mode), undefined,
      visited => reply({type: 'progress', id: message.id, visited}));
    session.search = search;
    reply({type: 'result', id: message.id, result: search.search(message.scope)});
  }

  private locate(message: Extract<GraphSearchMessage, {type: 'locate'}>, reply: (reply: GraphSearchReply) => void) {
    const search = this.sessions.get(message.session)?.search;
    if (!search) {
      reply({type: 'failed', id: message.id, message: 'No search to locate a match in.'});
      return;
    }
    reply({type: 'located', id: message.id, path: search.locate(message.k)});
  }
}
