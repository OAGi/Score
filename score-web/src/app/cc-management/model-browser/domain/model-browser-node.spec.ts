import {
  ModelBrowserAccNode,
  ModelBrowserAsccpNode,
  ModelBrowserBccpNode,
  ModelBrowserNodeDatabase,
  ModelBrowserNodeDataSource
} from './model-browser-node';
import {BieViewOrderEntry} from './bie-view-order';

/**
 * #1638 model-browser wiring tests. These exercise the REAL classes the Model Browser uses to apply
 * the instance-level sibling order on the client (lazy per-view-parent fetch + resort), with no graph,
 * no HTTP and no Angular TestBed — the order weights are the only behavior under test here.
 */

/** A bare ACC view parent: expandable short-circuited, just enough of a graph node to derive a path. */
function accParent(accManifestId: number, children: ModelBrowserAccNode[] = []): ModelBrowserAccNode {
  const n = new ModelBrowserAccNode();
  n.name = 'Parent-' + accManifestId;
  n.level = 0;
  n.accNode = {manifestId: accManifestId, componentType: 'Embedded', deprecated: false} as any;
  n.asccpNode = {manifestId: 90000 + accManifestId, deprecated: false} as any;
  n.expandable = true; // skip the graph-loading expandable getter
  n.children = children;
  children.forEach(c => c.parent = n); // so a child's path/hashPath can resolve
  return n;
}

/** A flattened ASCCP sibling under a view parent, keyed by its asccManifestId. */
function asccChild(name: string, asccManifestId: number): ModelBrowserAsccpNode {
  const n = new ModelBrowserAsccpNode();
  n.name = name;
  n.level = 1;
  n.accNode = {manifestId: 50000 + asccManifestId, componentType: 'Embedded', deprecated: false} as any;
  n.asccNode = {manifestId: asccManifestId, deprecated: false} as any;
  n.asccpNode = {manifestId: 70000 + asccManifestId, deprecated: false} as any;
  return n;
}

/** A flattened element BCC sibling under a view parent, keyed by its bccManifestId. */
function bccChild(name: string, bccManifestId: number): ModelBrowserBccpNode {
  const n = new ModelBrowserBccpNode();
  n.name = name;
  n.level = 1;
  n.bccNode = {manifestId: bccManifestId, entityType: 'Element', deprecated: false} as any;
  n.bccpNode = {manifestId: 80000 + bccManifestId, deprecated: false} as any;
  n.bdtNode = {manifestId: 60000 + bccManifestId, deprecated: false} as any;
  return n;
}

/** A flattened ATTRIBUTE BCC sibling (entityType=Attribute) — sorts into the attributes-first partition. */
function attrBccChild(name: string, bccManifestId: number): ModelBrowserBccpNode {
  const n = bccChild(name, bccManifestId);
  (n.bccNode as any).entityType = 'Attribute';
  return n;
}

function newDb(): ModelBrowserNodeDatabase<any> {
  return new ModelBrowserNodeDatabase<any>(null as any, 'ACC', 1);
}

describe('ModelBrowserNodeDatabase view-order store (#1638)', () => {
  it('setViewOrderForParent then getViewOrderWeight reads back ASCC and BCC weights', () => {
    const db = newDb();
    const ascc = asccChild('Beta', 2);
    const bcc = bccChild('Gamma', 3);
    const entries: BieViewOrderEntry[] = [
      {fromAccManifestId: 1, asccManifestId: 2, weight: 100},
      {fromAccManifestId: 1, bccManifestId: 3, weight: 200},
    ];

    db.setViewOrderForParent(1, entries);

    expect(db.getViewOrderWeight(1, ascc)).toBe(100);
    expect(db.getViewOrderWeight(1, bcc)).toBe(200);
    // A child not weighted under this parent reads back undefined (=> default / seq_key order).
    expect(db.getViewOrderWeight(1, asccChild('Other', 9))).toBeUndefined();
  });

  it('re-setting a parent CLEARS its previous keys (a removed row drops back to undefined)', () => {
    const db = newDb();
    const ascc = asccChild('Beta', 2);
    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 2, weight: 100}]);
    expect(db.getViewOrderWeight(1, ascc)).toBe(100);

    // Server now returns nothing for parent 1 (the weight was reset) -> the old key must be gone.
    db.setViewOrderForParent(1, []);
    expect(db.getViewOrderWeight(1, ascc)).toBeUndefined();
  });

  it('re-setting one parent does NOT disturb another parent', () => {
    const db = newDb();
    const ascc = asccChild('Beta', 2);
    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 2, weight: 11}]);
    db.setViewOrderForParent(5, [{fromAccManifestId: 5, asccManifestId: 2, weight: 22}]);

    db.setViewOrderForParent(1, []); // clear parent 1 only

    expect(db.getViewOrderWeight(1, ascc)).toBeUndefined();
    expect(db.getViewOrderWeight(5, ascc)).toBe(22);
  });

  it('prefix clearing is delimiter-safe (738304 does not clobber 7383040)', () => {
    const db = newDb();
    const ascc = asccChild('Beta', 5);
    db.setViewOrderForParent(738304, [{fromAccManifestId: 738304, asccManifestId: 5, weight: 1}]);
    db.setViewOrderForParent(7383040, [{fromAccManifestId: 7383040, asccManifestId: 5, weight: 2}]);

    db.setViewOrderForParent(738304, []); // must clear ONLY 738304, not the longer-id sibling

    expect(db.getViewOrderWeight(738304, ascc)).toBeUndefined();
    expect(db.getViewOrderWeight(7383040, ascc)).toBe(2);
  });
});

describe('ModelBrowserNodeDatabase.viewParentAccManifestId (#1638 — null-safe view-parent resolution)', () => {
  // The order-weight badge calls component.currentWeightOf() for EVERY rendered row, which resolves
  // the view parent's ACC via this method. It MUST return undefined (not throw) when the node's
  // display parent is not an ACC — e.g. a DT_SC node whose display parent is a BCCP with no accNode.
  it('returns the ACC manifest id for an ACC view parent', () => {
    expect(newDb().viewParentAccManifestId(accParent(7))).toBe(7);
  });

  it('returns the target ACC manifest id for an ASCCP view parent (an ASCCP carries an accNode)', () => {
    expect(newDb().viewParentAccManifestId(asccChild('Beta', 2))).toBe(50002);
  });

  it('returns undefined for a BCCP node (no accNode) — the DT_SC display-parent case that must not throw', () => {
    expect(newDb().viewParentAccManifestId(bccChild('Gamma', 3))).toBeUndefined();
    expect(newDb().viewParentAccManifestId(attrBccChild('Attr', 4))).toBeUndefined();
  });
});

describe('ModelBrowserNodeDatabase.children resort (#1638)', () => {
  it('is a byte-identical no-op while nothing is weighted (seq_key order preserved)', () => {
    const db = newDb();
    const c1 = asccChild('Charlie', 1);
    const c2 = asccChild('Bravo', 2);
    const c3 = asccChild('Alpha', 3);
    const parent = accParent(1, [c1, c2, c3]);

    expect(db.children(parent)).toEqual([c1, c2, c3]);
  });

  it('a positive weight lifts that sibling above the unset ones', () => {
    const db = newDb();
    const c1 = asccChild('Charlie', 1);
    const c2 = asccChild('Bravo', 2);
    const c3 = asccChild('Alpha', 3);
    const parent = accParent(1, [c1, c2, c3]);

    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 2, weight: 100}]);

    expect(db.children(parent)).toEqual([c2, c1, c3]);
  });

  it('clearing the parent restores the seq_key order', () => {
    const db = newDb();
    const c1 = asccChild('Charlie', 1);
    const c2 = asccChild('Bravo', 2);
    const c3 = asccChild('Alpha', 3);
    const parent = accParent(1, [c1, c2, c3]);

    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 2, weight: 100}]);
    expect(db.children(parent)).toEqual([c2, c1, c3]);

    db.setViewOrderForParent(1, []);
    expect(db.children(parent)).toEqual([c1, c2, c3]);
  });
});

describe('ModelBrowserNodeDatabase.children — attribute/element partition (#1638)', () => {
  // seq_key order interleaves attributes and elements; flatten() must put attributes FIRST, and a
  // weight in one partition must reorder ONLY that partition (the attribute/element boundary is hard).
  function mixedParent() {
    const elemA = asccChild('ElemA', 10);
    const attr1 = attrBccChild('Attr1', 20);
    const elemB = asccChild('ElemB', 11);
    const attr2 = attrBccChild('Attr2', 21);
    // declared seq order: elemA, attr1, elemB, attr2
    return {parent: accParent(1, [elemA, attr1, elemB, attr2]), elemA, attr1, elemB, attr2};
  }

  it('flattens attributes first, then elements, preserving each partition\'s seq order', () => {
    const db = newDb();
    const {parent, elemA, attr1, elemB, attr2} = mixedParent();
    expect(db.children(parent)).toEqual([attr1, attr2, elemA, elemB]);
  });

  it('weighting an attribute (BCC key) reorders ONLY the attribute partition', () => {
    const db = newDb();
    const {parent, elemA, attr1, elemB, attr2} = mixedParent();

    db.setViewOrderForParent(1, [{fromAccManifestId: 1, bccManifestId: 21, weight: 100}]); // lift Attr2
    expect(db.getViewOrderWeight(1, attr2)).toBe(100);

    // attributes resort (Attr2 above Attr1); elements untouched
    expect(db.children(parent)).toEqual([attr2, attr1, elemA, elemB]);
  });

  it('weighting an element (ASCC key) reorders ONLY the element partition', () => {
    const db = newDb();
    const {parent, elemA, attr1, elemB, attr2} = mixedParent();

    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 11, weight: 100}]); // lift ElemB
    expect(db.children(parent)).toEqual([attr1, attr2, elemB, elemA]);
  });

  it('attribute and element weights coexist and resort their partitions independently', () => {
    const db = newDb();
    const {parent, elemA, attr1, elemB, attr2} = mixedParent();

    db.setViewOrderForParent(1, [
      {fromAccManifestId: 1, bccManifestId: 21, weight: 100},  // Attr2 first among attributes
      {fromAccManifestId: 1, asccManifestId: 11, weight: 100}  // ElemB first among elements
    ]);
    expect(db.children(parent)).toEqual([attr2, attr1, elemB, elemA]);
  });
});

describe('ModelBrowserNodeDataSource.nodeExpanded$ (#1638 lazy-fetch trigger)', () => {
  function wired(): {ds: ModelBrowserNodeDataSource<any>, parent: ModelBrowserAccNode} {
    const db = newDb();
    const ds = new ModelBrowserNodeDataSource<any>(db, null as any);
    const child = asccChild('Child', 2);
    const parent = accParent(1, [child]);
    ds.data = [parent];
    return {ds, parent};
  }

  it('emits the expanded node on expand', () => {
    const {ds, parent} = wired();
    const seen: any[] = [];
    ds.nodeExpanded$.subscribe(n => seen.push(n));

    ds.toggleNode(parent, true);

    expect(seen).toEqual([parent]);
    expect(parent.expanded).toBe(true);
  });

  it('does NOT emit on collapse', () => {
    const {ds, parent} = wired();
    ds.toggleNode(parent, true);

    const seen: any[] = [];
    ds.nodeExpanded$.subscribe(n => seen.push(n));
    ds.toggleNode(parent, false);

    expect(seen).toEqual([]);
    expect(parent.expanded).toBe(false);
  });
});
