import {
  AbieFlatNode,
  AsbiepFlatNode,
  BbiepFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource
} from './bie-flat-tree';
import {BieViewOrderEntry} from '../../cc-management/model-browser/domain/bie-view-order';

/**
 * #1638 BIE-editor wiring tests. The editor reads the SAME instance-level sibling order as the model
 * browser and applies it on the client (lazy per-view-parent fetch + resort). These exercise the real
 * BieFlatNode database/datasource — no graph, no HTTP, no Angular TestBed. The BIE editor is view-only
 * for #1638, so only the read/resort/lazy-fetch path is covered here (no set/drag controls).
 */

/** A bare ABIE view parent: expandable short-circuited, just enough to derive a query path. */
function abieParent(accManifestId: number, children: AsbiepFlatNode[] = []): AbieFlatNode {
  const n = new AbieFlatNode();
  n.name = 'Parent-' + accManifestId;
  n.level = 0;
  n.accNode = {manifestId: accManifestId, componentType: 'Embedded', deprecated: false} as any;
  n.asccpNode = {manifestId: 90000 + accManifestId, deprecated: false} as any;
  n.expandable = true; // skip the graph-loading expandable getter
  n.children = children;
  children.forEach(c => c.parent = n);
  return n;
}

/** A flattened ASBIEP sibling under a view parent, keyed by its asccManifestId. */
function asbiepChild(name: string, asccManifestId: number): AsbiepFlatNode {
  const n = new AsbiepFlatNode();
  n.name = name;
  n.level = 1;
  n.accNode = {manifestId: 50000 + asccManifestId, componentType: 'Embedded', deprecated: false} as any;
  n.asccNode = {manifestId: asccManifestId, deprecated: false} as any;
  n.asccpNode = {manifestId: 70000 + asccManifestId, deprecated: false} as any;
  return n;
}

/** A flattened element BBIEP sibling under a view parent, keyed by its bccManifestId. */
function bbiepChild(name: string, bccManifestId: number): BbiepFlatNode {
  const n = new BbiepFlatNode();
  n.name = name;
  n.level = 1;
  n.bccNode = {manifestId: bccManifestId, entityType: 'Element', deprecated: false} as any;
  n.bccpNode = {manifestId: 80000 + bccManifestId, deprecated: false} as any;
  n.bdtNode = {manifestId: 60000 + bccManifestId, deprecated: false} as any;
  return n;
}

/** A flattened ATTRIBUTE BBIEP sibling (entityType=Attribute) — sorts into the attributes-first partition. */
function attrBbiepChild(name: string, bccManifestId: number): BbiepFlatNode {
  const n = bbiepChild(name, bccManifestId);
  (n.bccNode as any).entityType = 'Attribute';
  return n;
}

function newDb(): BieFlatNodeDatabase<any> {
  return new BieFlatNodeDatabase<any>(null as any, null as any, 0, [], []);
}

describe('BieFlatNodeDatabase view-order store (#1638)', () => {
  it('setViewOrderForParent then getViewOrderWeight reads back ASCC and BCC weights', () => {
    const db = newDb();
    const asbiep = asbiepChild('Beta', 2);
    const bbiep = bbiepChild('Gamma', 3);
    const entries: BieViewOrderEntry[] = [
      {fromAccManifestId: 1, asccManifestId: 2, weight: 100},
      {fromAccManifestId: 1, bccManifestId: 3, weight: 200},
    ];

    db.setViewOrderForParent(1, entries);

    expect(db.getViewOrderWeight(1, asbiep)).toBe(100);
    expect(db.getViewOrderWeight(1, bbiep)).toBe(200);
    expect(db.getViewOrderWeight(1, asbiepChild('Other', 9))).toBeUndefined();
  });

  it('re-setting a parent CLEARS its previous keys (a removed row drops back to undefined)', () => {
    const db = newDb();
    const asbiep = asbiepChild('Beta', 2);
    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 2, weight: 100}]);
    expect(db.getViewOrderWeight(1, asbiep)).toBe(100);

    db.setViewOrderForParent(1, []);
    expect(db.getViewOrderWeight(1, asbiep)).toBeUndefined();
  });

  it('prefix clearing is delimiter-safe (738304 does not clobber 7383040)', () => {
    const db = newDb();
    const asbiep = asbiepChild('Beta', 5);
    db.setViewOrderForParent(738304, [{fromAccManifestId: 738304, asccManifestId: 5, weight: 1}]);
    db.setViewOrderForParent(7383040, [{fromAccManifestId: 7383040, asccManifestId: 5, weight: 2}]);

    db.setViewOrderForParent(738304, []);

    expect(db.getViewOrderWeight(738304, asbiep)).toBeUndefined();
    expect(db.getViewOrderWeight(7383040, asbiep)).toBe(2);
  });
});

describe('BieFlatNodeDatabase.children resort (#1638)', () => {
  it('is a byte-identical no-op while nothing is weighted (seq_key order preserved)', () => {
    const db = newDb();
    const c1 = asbiepChild('Charlie', 1);
    const c2 = asbiepChild('Bravo', 2);
    const c3 = asbiepChild('Alpha', 3);
    const parent = abieParent(1, [c1, c2, c3]);

    expect(db.children(parent)).toEqual([c1, c2, c3]);
  });

  it('a positive weight lifts that sibling above the unset ones, and clearing restores seq_key', () => {
    const db = newDb();
    const c1 = asbiepChild('Charlie', 1);
    const c2 = asbiepChild('Bravo', 2);
    const c3 = asbiepChild('Alpha', 3);
    const parent = abieParent(1, [c1, c2, c3]);

    db.setViewOrderForParent(1, [{fromAccManifestId: 1, asccManifestId: 2, weight: 100}]);
    expect(db.children(parent)).toEqual([c2, c1, c3]);

    db.setViewOrderForParent(1, []);
    expect(db.children(parent)).toEqual([c1, c2, c3]);
  });
});

describe('BieFlatNodeDatabase — view-order key & attribute/element partition (#1638, view-only)', () => {
  it('viewParentAccManifestId() is the node\'s accNode.manifestId (the view-order key the BIE editor reads)', () => {
    // This is the invariant the release-scope e2e cites: a BIE keys its view order by the ACC manifest
    // id, the SAME key the model browser writes — so a reorder shows in the BIE for that release.
    const db = newDb();
    const parent = abieParent(4242);
    expect(db.viewParentAccManifestId(parent)).toBe(4242);
  });

  it('reflects attributes-first, then a per-partition order, exactly like the model browser', () => {
    const db = newDb();
    const elemA = asbiepChild('ElemA', 10);
    const attr1 = attrBbiepChild('Attr1', 20);
    const elemB = asbiepChild('ElemB', 11);
    const attr2 = attrBbiepChild('Attr2', 21);
    const parent = abieParent(1, [elemA, attr1, elemB, attr2] as any);

    expect(db.children(parent)).toEqual([attr1, attr2, elemA, elemB]);

    // weight set in the model browser (BCC key) is reflected here read-only: Attr2 floats up
    db.setViewOrderForParent(1, [{fromAccManifestId: 1, bccManifestId: 21, weight: 100}]);
    expect(db.children(parent)).toEqual([attr2, attr1, elemA, elemB]);
  });
});

describe('BieFlatNodeDataSource.nodeExpanded$ (#1638 lazy-fetch trigger)', () => {
  function wired(): {ds: BieFlatNodeDataSource<any>, parent: AbieFlatNode} {
    const db = newDb();
    const ds = new BieFlatNodeDataSource<any>(db, null as any, null as any);
    const child = asbiepChild('Child', 2);
    const parent = abieParent(1, [child]);
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

  it('addresses the expanded row by queryPath, not hashPath (reused-subtree collision fix)', () => {
    const db = newDb();
    const ds = new BieFlatNodeDataSource<any>(db, null as any, null as any);

    // Two distinct rows whose hashPath COLLIDES by design: a reused TopLevelAsbiep makes the
    // asbiepPath (hence path/hashPath) identical, so an indexOf(hashPath) would find the wrong row.
    // Their queryPath (parent-chain, name-based) differs, so toggleNode must use it.
    const childA = asbiepChild('ChildA', 11);
    const childB = asbiepChild('ChildB', 22);
    const parentA = abieParent(1, [childA]);
    const parentB = abieParent(1, [childB]);
    parentA.name = 'Alpha';
    parentB.name = 'Beta';
    parentA.asccpNode = {manifestId: 999, deprecated: false} as any; // same asccpNode + accNode =>
    parentB.asccpNode = {manifestId: 999, deprecated: false} as any; // identical path => same hashPath

    expect(parentA.hashPath).toBe(parentB.hashPath); // the collision is real
    expect(parentA.queryPath).not.toBe(parentB.queryPath); // but the query paths differ

    ds.data = [parentA, parentB];
    ds.toggleNode(parentB, true);

    // childB must be inserted AFTER parentB (index 2), not after the colliding parentA (index 1).
    expect(ds.data).toEqual([parentA, parentB, childB]);
  });
});
