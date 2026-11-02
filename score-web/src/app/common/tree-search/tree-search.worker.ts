/// <reference lib="webworker" />
import {GraphSearchHost, GraphSearchMessage} from './graph-search-host';

const host = new GraphSearchHost();

addEventListener('message', ({data}: MessageEvent<GraphSearchMessage>) => host.handle(data, reply => postMessage(reply)));
