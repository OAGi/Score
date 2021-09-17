import {CcGraph, CcGraphEdge, CcGraphNode} from '../../domain/core-component-node';

export class FindUsagesResponse {
  rootNodeKey: string;
  graph: {
    nodes: { [key: string]: CcGraphNode; };
    edges: { [key: string]: CcGraphEdge; };
  };
}
