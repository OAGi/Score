import {DagSearchResult, DagSearchScope} from './dag-path-search';
import {PathPatternMode} from './path-pattern';
import {GraphSearchHost, GraphSearchMessage, GraphSearchReply} from './graph-search-host';
import {SearchDag} from './search-dag';

/** One screen's connection to the graph search: it owns a DAG on the other side and searches it. */
export interface GraphSearchSession {

  /** Hands over the DAG to search (replacing the previous one). */
  load(dag: SearchDag): void;

  search(expr: string, caseSensitive: boolean, mode: PathPatternMode, scope: DagSearchScope, onProgress?: (visited: number) => void): Promise<DagSearchResult>;

  /** The path (child indexes below the scope node) of the n-th match of the latest search. */
  locate(k: number): Promise<number[] | undefined>;

  close(): void;
}

/** Call ids are unique across sessions, since the worker's replies are offered to every session. */
let nextCallId = 1;

type Pending = {resolve: (reply: any) => void, reject: (error: Error) => void, onProgress?: (visited: number) => void};

/** Routes the host's replies back to the call that is waiting for them. */
class SessionCalls {

  private readonly pending = new Map<number, Pending>();

  constructor(readonly session: number, private readonly send: (message: GraphSearchMessage) => void) {
  }

  call<R extends GraphSearchReply>(make: (id: number) => GraphSearchMessage, onProgress?: (visited: number) => void): Promise<R> {
    const id = nextCallId++;
    return new Promise<R>((resolve, reject) => {
      this.pending.set(id, {resolve, reject, onProgress});
      this.send(make(id));
    });
  }

  receive(reply: GraphSearchReply) {
    const call = this.pending.get(reply.id);
    if (!call) {
      return;
    }
    if (reply.type === 'progress') {
      call.onProgress?.(reply.visited);
      return;
    }
    this.pending.delete(reply.id);
    if (reply.type === 'failed') {
      call.reject(new Error(reply.message));
    } else {
      call.resolve(reply);
    }
  }
}

function sessionOver(calls: SessionCalls, send: (message: GraphSearchMessage) => void): GraphSearchSession {
  const session = calls.session;
  return {
    load: dag => send({type: 'load', session, dag}),
    search: (expr, caseSensitive, mode, scope, onProgress) =>
      calls.call<Extract<GraphSearchReply, {type: 'result'}>>(
        id => ({type: 'search', session, id, expr, caseSensitive, mode, scope}), onProgress).then(r => r.result),
    locate: k => calls.call<Extract<GraphSearchReply, {type: 'located'}>>(
      id => ({type: 'locate', session, id, k})).then(r => r.path),
    close: () => send({type: 'close', session})
  };
}

let nextSession = 1;
let sharedWorker: Worker | undefined | null;
const workerCalls = new Map<number, SessionCalls>();

function worker(): Worker | undefined {
  if (sharedWorker === undefined) {
    try {
      sharedWorker = typeof Worker === 'undefined' ? null :
        new Worker(new URL('./tree-search.worker', import.meta.url), {type: 'module'});
      sharedWorker?.addEventListener('message', ({data}: MessageEvent<GraphSearchReply>) =>
        workerCalls.forEach(calls => calls.receive(data)));
    } catch {
      sharedWorker = null;
    }
  }
  return sharedWorker ?? undefined;
}

/**
 * Opens a graph search session. The searching runs in a Web Worker shared by all sessions, so that it never blocks
 * the page; where workers are not available it runs in the page's own thread (asynchronously, but not off-thread).
 */
export function openGraphSearchSession(): GraphSearchSession {
  const id = nextSession++;
  const shared = worker();
  if (shared) {
    const send = (message: GraphSearchMessage) => shared.postMessage(message);
    const calls = new SessionCalls(id, send);
    workerCalls.set(id, calls);
    const session = sessionOver(calls, send);
    return {...session, close: () => {
      workerCalls.delete(id);
      session.close();
    }};
  }
  const host = new GraphSearchHost();
  let calls: SessionCalls;
  const send = (message: GraphSearchMessage) => queueMicrotask(() => host.handle(message, reply => calls.receive(reply)));
  calls = new SessionCalls(id, send);
  return sessionOver(calls, send);
}
