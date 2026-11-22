import {backend, sql} from './integrity-env';

/** What the integrity checks of the component trees share: the graph the backend serves and the roots they sample. */

export interface GraphNode {
  type: string;
  manifestId: number;
  [key: string]: unknown;
}

export interface Graph {
  graph: {nodes: Record<string, GraphNode>, edges: Record<string, {targets: string[]}>};
}

export const RELEASES = [77, 10, 53];

export const COMPONENT_TYPES = ['Base', 'Semantics', 'Extension', 'SemanticGroup', 'UserExtensionGroup', 'Embedded', 'OAGIS10Nouns',
  'OAGIS10BODs', 'BOD', 'Verb', 'Noun', 'Choice', 'AttributeGroup'];

/** A reproducible sample of roots of every kind: the biggest, the smallest and some in between. */
export function sampleRoots(release: number): {type: 'ACC' | 'ASCCP' | 'BCCP' | 'DT', id: number}[] {
  const pick = (table: string, id: string, type: 'ACC' | 'ASCCP' | 'BCCP' | 'DT', limit: number) =>
    sql(`select ${id} from ${table} where release_id = ${release} order by md5(${id}) limit ${limit}`)
      .map(r => ({type, id: Number(r[0])}));
  return [...pick('asccp_manifest', 'asccp_manifest_id', 'ASCCP', 6), ...pick('acc_manifest', 'acc_manifest_id', 'ACC', 3),
    ...pick('bccp_manifest', 'bccp_manifest_id', 'BCCP', 3), ...pick('dt_manifest', 'dt_manifest_id', 'DT', 3)];
}

export function graphOf(type: string, id: number): Promise<Graph> {
  return backend<Graph>('/graphs/' + type.toLowerCase() + '/' + id);
}
