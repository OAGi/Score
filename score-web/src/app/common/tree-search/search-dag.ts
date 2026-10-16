import {AttrValue} from './search-expression';

/**
 * A tree whose repeated subtrees are stored once: record `i` stands for every tree node that shows the same component,
 * and its children are records too. The tree itself is this DAG unfolded, which can be millions of nodes where the
 * DAG has thousands of records, so searching the DAG (see `DagPathSearch`) never has to build the tree.
 *
 * The arrays are flat (and the whole object structured-cloneable) so that it can be posted to a worker.
 */
export interface SearchDag {
  /** Per record, the names a search can match (the display name, and e.g. an alternative one). */
  names: string[][];
  /** Record `i`'s children are `childList[childStart[i] .. childStart[i + 1])`, in the order the tree shows them. */
  childStart: Int32Array;
  childList: Int32Array;
  /**
   * Per record, the component it shows. A component is normally one record, but a place that differs from the others
   * (e.g. a node with its own display name) has a record of its own; such records share the identity of their
   * component, which is what the rule "a component is not expanded below itself" goes by. Absent: every record is its
   * own component.
   */
  identity?: Int32Array;
  /**
   * Per attribute a predicate may ask about, its value on every record (`undefined`: the record has none). Absent: the
   * records have no attributes beyond their names.
   */
  attributes?: Record<string, AttrValue[]>;
}

/** Builds a `SearchDag` record by record; a record is identified by a key so that equal components are shared. */
export class SearchDagBuilder {

  private readonly indexByKey = new Map<string, number>();
  private readonly names: string[][] = [];
  private readonly children: number[][] = [];
  private readonly identities: number[] = [];
  private readonly identityByKey = new Map<string, number>();
  private readonly attributes: Record<string, AttrValue>[] = [];

  /**
   * The record of the key, creating it (with the given names) when it is new; `created` tells which. The record shows
   * the component `identityKey` (by default, its own key): records with the same one are the same component.
   */
  record(key: string, names: string[], identityKey = key, attributes?: Record<string, AttrValue>): {index: number, created: boolean} {
    const existing = this.indexByKey.get(key);
    if (existing !== undefined) {
      return {index: existing, created: false};
    }
    const index = this.names.length;
    this.indexByKey.set(key, index);
    this.names.push(names);
    this.children.push([]);
    this.attributes.push(attributes);
    if (!this.identityByKey.has(identityKey)) {
      this.identityByKey.set(identityKey, this.identityByKey.size);
    }
    this.identities.push(this.identityByKey.get(identityKey));
    return {index, created: true};
  }

  indexOf(key: string): number | undefined {
    return this.indexByKey.get(key);
  }

  childrenOf(index: number): number[] {
    return this.children[index];
  }

  setChildren(index: number, children: number[]) {
    this.children[index] = children;
  }

  build(): SearchDag {
    const childStart = new Int32Array(this.names.length + 1);
    const childList = new Int32Array(this.children.reduce((sum, c) => sum + c.length, 0));
    let offset = 0;
    this.children.forEach((c, i) => {
      childStart[i] = offset;
      childList.set(c, offset);
      offset += c.length;
    });
    childStart[this.names.length] = offset;
    const attributes: Record<string, AttrValue[]> = {};
    this.attributes.forEach((record, i) => {
      for (const attribute of Object.keys(record || {})) {
        (attributes[attribute] ??= new Array(this.names.length).fill(undefined))[i] = record[attribute];
      }
    });
    return {names: this.names, childStart, childList, identity: Int32Array.from(this.identities), attributes};
  }
}
