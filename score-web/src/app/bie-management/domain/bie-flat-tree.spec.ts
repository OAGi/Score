import {
  AbieFlatNode,
  AsbiepFlatNode,
  BbiepFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource,
  BiePathLikeExpressionEvaluator
} from './bie-flat-tree';
import {BieViewOrderEntry} from '../../cc-management/model-browser/domain/bie-view-order';
import {of} from 'rxjs';

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

describe('BieFlatNode inherited identity', () => {
  it('rejects inherited nodes without a based top-level BIE id', () => {
    const node = new AsbiepFlatNode();

    expect(() => node.inherited = true).toThrow(/basedTopLevelAsbiepId/);
  });

  it('accepts an inherited node with a positive based top-level BIE id', () => {
    const node = new AsbiepFlatNode();
    node.basedTopLevelAsbiepId = 27;

    node.inherited = true;

    expect(node.inherited).toBe(true);
  });

  it('rejects clearing the base id while the node is inherited', () => {
    const node = new AsbiepFlatNode();
    node.basedTopLevelAsbiepId = 27;
    node.inherited = true;

    expect(() => node.basedTopLevelAsbiepId = undefined).toThrow(/basedTopLevelAsbiepId/);
    expect(node.inherited).toBe(true);
    expect(node.basedTopLevelAsbiepId).toBe(27);
  });

  it('allows clearing the base id after inheritance is disabled', () => {
    const node = new AsbiepFlatNode();
    node.basedTopLevelAsbiepId = 27;
    node.inherited = true;

    node.inherited = false;
    node.basedTopLevelAsbiepId = undefined;

    expect(node.inherited).toBe(false);
    expect(node.basedTopLevelAsbiepId).toBeUndefined();
  });
});

describe('BieFlatNodeDataSource inherited reused details', () => {
  it.each([undefined, 40])('loads the incoming association in its owner family (owner base %s)', ownerBaseId => {
    const parent = abieParent(43);
    parent.topLevelAsbiepId = 43;
    parent.basedTopLevelAsbiepId = ownerBaseId;
    const node = asbiepChild('Reused', 11);
    node.parent = parent;
    node.reused = true;
    node.topLevelAsbiepId = 29;
    node.basedTopLevelAsbiepId = 17;
    node.inherited = true;

    const asbieDetails = {
      asbieId: 1,
      toAsbiepId: 29,
      basedAscc: {},
      cardinality: {min: 0, max: 1},
      ownerTopLevelAsbiep: {topLevelAsbiepId: 43, version: '1.0', status: 'Published'}
    };
    const basedAsbieDetails = {
      ...asbieDetails,
      asbieId: 4,
      toAsbiepId: 18,
      ownerTopLevelAsbiep: {topLevelAsbiepId: 40, version: '0.9', status: 'Published'}
    };
    const asbiepDetails = {
      asbiepId: 2,
      basedAsccp: {},
      roleOfAbieId: 3,
      ownerTopLevelAsbiep: {topLevelAsbiepId: 29, version: '1.0', status: 'Published'}
    };
    const basedAsbiepDetails = {
      ...asbiepDetails,
      asbiepId: 18,
      roleOfAbieId: 19,
      ownerTopLevelAsbiep: {topLevelAsbiepId: 17, version: '0.9', status: 'Published'}
    };
    const abieDetails = {
      abieId: 3,
      basedAcc: {},
      ownerTopLevelAsbiep: {topLevelAsbiepId: 29, version: '1.0', status: 'Published'}
    };
    const basedAbieDetails = {
      ...abieDetails,
      abieId: 19,
      ownerTopLevelAsbiep: {topLevelAsbiepId: 17, version: '0.9', status: 'Published'}
    };
    const service = {
      getAsbieDetailsByPath: vi.fn((ownerId: number, manifestId: number, path: string) => {
        // BOM's incoming ASCC does not exist inside the referenced child BIE.
        expect([43, 40]).toContain(ownerId);
        expect(path).toBe('ASCCP-90043>ACC-43>ASCC-11');
        return of(ownerId === 40 ? basedAsbieDetails : asbieDetails);
      }),
      getAsbiepDetailsByPath: vi.fn(() => of(asbiepDetails)),
      getAbieDetailsByPath: vi.fn(() => of(abieDetails)),
      getAsbiepDetails: vi.fn(() => of(basedAsbiepDetails)),
      getAbieDetails: vi.fn(() => of(basedAbieDetails))
    };
    const dataSource = new BieFlatNodeDataSource(newDb(), service as any, null as any);
    const callback = vi.fn();

    dataSource.loadDetails(node, callback);

    expect(service.getAsbieDetailsByPath.mock.calls.map(([ownerId]) => ownerId)).toEqual([43, ownerBaseId ?? 43]);
    if (ownerBaseId) {
      expect(service.getAsbiepDetails).toHaveBeenCalledWith(18);
      expect(service.getAbieDetails).toHaveBeenCalledWith(19);
    } else {
      expect(service.getAsbiepDetails).not.toHaveBeenCalled();
      expect(service.getAbieDetails).not.toHaveBeenCalled();
    }
    expect(callback).toHaveBeenCalledWith(node);
    expect(node.detail.isLoaded).toBe(true);
    expect((node.detail as any).base).toBeDefined();
  });
});

describe('BiePathLikeExpressionEvaluator', () => {
  it('matches path segments exactly so Party does not resolve to Manufacturing Party', () => {
    const parent = {name: 'BOM Item Data', parent: undefined, isGroup: false} as any;
    const party = {name: 'Party', parent, isGroup: false} as any;
    const manufacturingParty = {name: 'Manufacturing Party', parent, isGroup: false} as any;
    const evaluator = new BiePathLikeExpressionEvaluator('/BOM Item Data/Party');

    expect(evaluator.eval(party)).toBe(true);
    expect(evaluator.eval(manufacturingParty)).toBe(false);
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

describe('Association expansion after loading', () => {
  it.each([AbieFlatNode, AsbiepFlatNode])('caches an empty association after loading', NodeType => {
    const db = newDb();
    const node = new NodeType();
    node.dataSource = {database: db, hideUnused: false} as any;
    const associations = vi.spyOn(db, 'getAssociations').mockReturnValue([]);
    expect(node.expandable).toBe(false);
    expect(node.expandable).toBe(false);
    expect(associations).toHaveBeenCalledTimes(1);
  });

  it('loads an initially empty root during initialization and preserves filter changes', () => {
    const db = newDb();
    const node = new AbieFlatNode();
    node.required = true;
    const child = {used: false, required: false, children: []} as any;
    vi.spyOn(db, 'rootNode', 'get').mockReturnValue(node);
    const load = vi.spyOn(db, 'loadChildren').mockImplementation(parent => { parent.children = [child]; });
    const ds = new BieFlatNodeDataSource<any>(db, null as any, null as any);
    node.dataSource = ds;
    ds.init();
    expect(node.expandable).toBe(true);
    ds.hideUnused = true;
    expect(node.expandable).toBe(false);
    child.used = true;
    expect(node.expandable).toBe(true);
    expect(load).toHaveBeenCalledTimes(1);
  });

  it('does not traverse a circular association when checking expansion', () => {
    const db = newDb();
    const node = new AsbiepFlatNode();
    node.isCycle = true;
    node.dataSource = {database: db} as any;
    const associations = vi.spyOn(db, 'getAssociations');
    expect(node.expandable).toBe(false);
    expect(associations).not.toHaveBeenCalled();
  });
});

describe('BBIE supplementary-component expansion', () => {
  it.each(['Attribute', 'Element'])('uses positive SC cardinality for %s BBIEs', entityType => {
    const node = bbiepChild('Field', 1);
    node.bccNode.entityType = entityType;
    expect(node.expandable).toBe(false);
    node.children = [{cardinalityMax: 0} as any];
    expect(node.expandable).toBe(false);
    node.children = [{cardinalityMax: 0} as any, {cardinalityMax: 1} as any];
    expect(node.expandable).toBe(true);
    node.expandable = undefined;
    expect(node.expandable).toBe(true);
    node.children[1].cardinalityMax = 0;
    expect(node.expandable).toBe(false);
  });

  it('loads the immediate SC list before deciding whether a lazy BBIE expands', () => {
    const node = bbiepChild('Identifier', 2);
    const loadChildren = vi.fn((parent: BbiepFlatNode) => {
      parent.children = [{cardinalityMax: 1} as any];
    });
    node.dataSource = {database: {loadChildren}, hideUnused: false} as any;
    expect(node.expandable).toBe(true);
    expect(node.expandable).toBe(true);
    expect(loadChildren).toHaveBeenCalledTimes(1);
    expect(loadChildren).toHaveBeenCalledWith(node);
  });
});


describe('Hide unused bounded traversal', () => {
  it('filters unused branches without materializing their CC descendants', () => {
    const db = newDb();
    const used = asbiepChild('Used', 1);
    const unused = asbiepChild('Unused', 2);
    const parent = abieParent(10, [used, unused]);
    used.used = true;
    unused.used = false;
    parent.used = true;
    const ds = new BieFlatNodeDataSource<any>(db, null as any, null as any);
    ds.data = [parent, used, unused];
    const load = vi.spyOn(db, 'loadChildren').mockImplementation(() => {});

    ds.hideUnused = true;

    expect(ds.data).toEqual([parent, used]);
    expect(load).not.toHaveBeenCalled();
    expect(unused.getChildren()).toEqual([]);
  });

  it('retains loaded inherited descendants and reflects later usage changes', () => {
    const db = newDb();
    const child = asbiepChild('Inherited', 1);
    child.basedTopLevelAsbiepId = 27;
    child.inherited = true;
    const parent = abieParent(10, [child]);
    parent.used = false;
    expect(db.hasUsedOrInheritedDescendant(parent)).toBe(true);
    child.inherited = false;
    child.used = false;
    expect(db.hasUsedOrInheritedDescendant(parent)).toBe(false);
    child.used = true;
    expect(db.hasUsedOrInheritedDescendant(parent)).toBe(true);
  });

  it('keeps repeated occurrences independent even when their persisted paths match', () => {
    const db = newDb();
    const first = abieParent(10);
    const second = abieParent(10);
    first.used = false;
    second.used = true;
    expect(first.path).toBe(second.path);
    expect(db.hasUsedOrInheritedDescendant(first)).toBe(false);
    expect(db.hasUsedOrInheritedDescendant(second)).toBe(true);
  });

  it('terminates for cycles in loaded children without dropping a used sibling', () => {
    const db = newDb();
    const parent = abieParent(10);
    parent.used = false;
    const used = asbiepChild('Used', 1);
    used.parent = parent;
    // Model loaded flags without the editor setter propagating usage to ancestors.
    Object.defineProperty(used, 'used', {value: true, writable: true});
    parent.children = [parent as any, used];
    expect(db.hasUsedOrInheritedDescendant(parent)).toBe(true);
    used.used = false;
    expect(db.hasUsedOrInheritedDescendant(parent)).toBe(false);
  });
});
