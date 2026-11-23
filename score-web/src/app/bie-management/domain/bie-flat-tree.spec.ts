import {
  AbieFlatNode,
  AsbiepFlatNode,
  BbiepFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource
} from './bie-flat-tree';
import {BIE_VALUE_CONSTRAINT_ROW_PREDICATES, BIE_VALUE_DOMAIN_ROW_PREDICATE, BieServerMarks} from './bie-server-search';
import {CcServerMarks} from '../../cc-management/domain/cc-server-search';
import {ServerPredicate} from '../../common/tree-search/server-predicate';
import {BieViewOrderEntry} from '../../cc-management/model-browser/domain/bie-view-order';
import {of} from 'rxjs';
import {DagPathSearch} from '../../common/tree-search/dag-path-search';
import {PathPattern} from '../../common/tree-search/path-pattern';

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

describe('PathPattern on a chain of BIE nodes', () => {
  const adapter = {
    namesOf: (n: any) => n.isGroup ? undefined : [n.name, n.displayName].filter(name => !!name),
    attributesOf: (n: any) => ({name: n.name, displayName: n.displayName || n.name, used: !!n.used}),
  };

  /** Whether the node is where the expression matches, judged by the chain of nodes down to it. */
  const evaluate = (expr: string, node: any): boolean => {
    const pattern = new PathPattern(expr);
    const chain: any[] = [];
    for (let n = node; n; n = n.parent) {
      if (adapter.namesOf(n)) {
        chain.unshift(n);
      }
    }
    const lookups = chain.map(n => (attribute: string) => (adapter.attributesOf(n) as Record<string, any>)[attribute]);
    return pattern.isMatch(pattern.stepAll(chain.map(n => adapter.namesOf(n)), lookups));
  };

  it('finds every node under the parent whose name contains the step, Manufacturing Party as well as Party', () => {
    const parent = {name: 'BOM Item Data', parent: undefined, isGroup: false} as any;
    const party = {name: 'Party', parent, isGroup: false} as any;
    const manufacturingParty = {name: 'Manufacturing Party', parent, isGroup: false} as any;
    const other = {name: 'Item', parent, isGroup: false} as any;

    expect(evaluate('/BOM Item Data/Party', party)).toBe(true);
    expect(evaluate('/BOM Item Data/Party', manufacturingParty)).toBe(true);
    expect(evaluate('/BOM Item Data/Party', other)).toBe(false);
  });

  it('judges a step by its predicates on the node', () => {
    const parent = {name: 'Sender', parent: undefined, isGroup: false} as any;
    const used = {name: 'Code', parent, isGroup: false, used: true} as any;
    const unused = {name: 'Code', parent, isGroup: false, used: false} as any;

    expect(evaluate('Sender/Code[used = true]', used)).toBe(true);
    expect(evaluate('Sender/Code[used = true]', unused)).toBe(false);
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

describe('the cycle rule of a BIE tree', () => {
  /** A chain of nodes, each the child of the one before, with the ASCCP and group-ness given. */
  function chain(...links: {asccp: number, group?: boolean}[]): AsbiepFlatNode[] {
    const nodes = links.map((link, i) => {
      const n = new AsbiepFlatNode();
      n.name = 'n' + i;
      n.asccpNode = {manifestId: link.asccp} as any;
      n.accNode = {manifestId: 1, componentType: link.group ? 'SemanticGroup' : 'Embedded'} as any;
      return n;
    });
    nodes.forEach((n, i) => n.parent = nodes[i - 1]);
    return nodes;
  }

  it('does not count a group as a place a component repeats: a group is not shown', () => {
    // Attachment > (References Group) > Metadata > (References Group) > Document Reference
    const [attachment, group, metadata, group2, reference] = chain({asccp: 1}, {asccp: 9, group: true}, {asccp: 2}, {asccp: 9, group: true}, {asccp: 3});
    const db = newDb();
    expect(db.detectCycle(group2)).toBe(false);
    expect(db.detectCycle(reference)).toBe(false);
    expect((db as any).isCycleNode(group2)).toBe(false);
    expect((db as any).isCycleNode(reference)).toBe(false);
    expect(attachment && group && metadata).toBeTruthy();
  });

  it('still stops at a component that repeats, and at a group that holds itself', () => {
    const [a, b, a2] = chain({asccp: 1}, {asccp: 2}, {asccp: 1});
    expect(newDb().detectCycle(a2)).toBe(true);
    expect((newDb() as any).isCycleNode(a2)).toBe(true);
    expect(b).toBeTruthy();
    const [g, g2] = chain({asccp: 9, group: true}, {asccp: 9, group: true});
    expect(newDb().detectCycle(g2)).toBe(true);
    expect(g).toBeTruthy();
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

describe('BieFlatNodeDatabase.searchGraph', () => {
  /** A database whose nodes are all given up front (nothing is loaded from a CC graph). */
  class FixedDatabase extends BieFlatNodeDatabase<any> {
    constructor() {
      super(null as any, null as any, 0, [], []);
    }

    /** A node can say which node holds the children it would load (a cycle loads them only when asked for them). */
    loadChildren(node: any) {
      if (node.loadsAs && node.getChildren().length === 0) {
        node._children = node.loadsAs.children;
      }
    }
  }

  /** The same component as another node: same ASCCP and ACC, so the same children. */
  function sameComponentAs(node: AsbiepFlatNode, name: string, asccManifestId: number): AsbiepFlatNode {
    const copy = asbiepChild(name, asccManifestId);
    copy.asccpNode = node.asccpNode;
    copy.accNode = node.accNode;
    return copy;
  }

  function search(db: FixedDatabase, root: any, expr: string, options?: {hideUnused?: boolean, marks?: BieServerMarks}) {
    // the tree answers for its own filters: the search does not pass its state in
    db.filters.setActive('hideUnused', !!options?.hideUnused);
    const graph = db.searchGraph(root, options?.marks);
    const pattern = new PathPattern(expr);
    const names = [root].map(n => [n.name, n.displayName].filter(x => !!x));
    const attributes = [(a: string) => ({name: root.name, displayName: root.displayName || root.name, used: !!root.used})[a]];
    const found = new DagPathSearch(graph.dag, pattern)
      .search({ancestors: [], record: graph.recordOf(root), mask: pattern.stepAll(names, attributes)});
    return {graph, found};
  }

  /** Root -> Item (a plain place of component I), Box -> Item' (another place of I, with its own display name). */
  function tree() {
    const item = asbiepChild('Item', 10);
    item.children = [bbiepChild('Code', 20)];
    const item2 = sameComponentAs(item, 'Item', 12);
    item2.children = [bbiepChild('Code', 20)];
    const box = asbiepChild('Box', 11);
    box.children = [item2];
    const root = abieParent(1, [item, box]);
    root.used = true;
    [item, box, item2].forEach(n => n.parent = n === item2 ? box : root);
    [item, item2].forEach(n => n.children.forEach(c => c.parent = n));
    return {root, item, box, item2};
  }

  it('knows the children of a component that is only met as a cycle where its places are first met', () => {
    // Root -> Left -> X (the place met first, its Item is a cycle, Left being the same ASCCP as Item) and
    // Root -> Right -> X' (another place of X, where Item is a real place with a Code below it)
    const x1 = asbiepChild('X', 30);
    const cycleItem = asbiepChild('Item', 31);
    cycleItem.isCycle = true;
    x1.children = [cycleItem];
    const left = asbiepChild('Left', 32);
    left.asccpNode = {manifestId: 70031} as any;
    left.children = [x1];
    const x2 = sameComponentAs(x1, 'X', 30);
    const item = asbiepChild('Item', 31);
    item.children = [bbiepChild('Code', 21)];
    x2.children = [item];
    const right = asbiepChild('Right', 33);
    right.children = [x2];
    const root = abieParent(1, [left, right]);
    root.used = true;
    left.parent = right.parent = root;
    x1.parent = left;
    x2.parent = right;
    cycleItem.parent = x1;
    item.parent = x2;
    item.children.forEach(c => c.parent = item);
    (cycleItem as any).loadsAs = item;

    expect(search(new FixedDatabase(), root, '//Right/X/Item/Code').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, '//Code').found.total).toBe(1);
  });

  it('counts what is below an unused node as unused, whatever its own flag says', () => {
    const {root, item, box, item2} = tree();
    item.used = true;
    item.children[0].used = true;
    item2.used = true;
    item2.children[0].used = true;
    box.used = true;
    // a used node below an unused one (the row says used, the parent's says unused)
    (box as any)._used = false;

    const count = (expr: string) => search(new FixedDatabase(), root, expr).found.total;

    // below Item (used) both are used; below Box (unused) the Item and its Code are not
    expect(count('//*[type = BBIEP and used]')).toBe(1);
    expect(count('Box/Item[used]')).toBe(0);
    expect(count('Item[used]')).toBe(1);
    // and it does not matter that loading the child checked the parent in the meantime
    (box as any)._used = true;
    expect(count('Box/Item[used]')).toBe(1);
  });

  it('shares one record between the places of a component', () => {
    const {root} = tree();
    const {graph} = search(new FixedDatabase(), root, 'Item');

    // Root, Item, Code, Box, and the second Item place, which is under another association (its Code is shared)
    expect(graph.dag.names.length).toBe(5);
    expect(search(new FixedDatabase(), root, 'Item').found.total).toBe(2);
    expect(search(new FixedDatabase(), root, 'Item/Code').found.total).toBe(2);
  });

  it('gives a place with its own display name a record of its own, and matches by either name', () => {
    const {root, item2} = tree();
    item2.used = true;
    item2.displayName = 'Special';

    const {graph, found} = search(new FixedDatabase(), root, 'Special');

    expect(found.total).toBe(1);
    expect(search(new FixedDatabase(), root, 'Box/Special/Code').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, 'Item/Code').found.total).toBe(2);
    // the clone shares the component's identity, so it is not expanded below the component's other places
    expect(graph.dag.identity[graph.recordOf(item2)]).toBe(graph.dag.identity[graph.recordOf(root.children[0] as any)]);
    expect(graph.recordOf(item2)).not.toBe(graph.recordOf(root.children[0] as any));
  });

  it('tells the places of a component apart by what the BIE says about them', () => {
    const {root, item2} = tree();
    item2.used = true;

    expect(search(new FixedDatabase(), root, 'Item').found.total).toBe(2);
    expect(search(new FixedDatabase(), root, 'Item[used = true]').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, 'Item[used = false]').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, 'Box/Item[used]/Code').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, 'Item[type = "ASBIEP" and used]').found.total).toBe(1);
  });

  it('tells the places of a component apart by what is set on the BIE: nillable and deprecated', () => {
    const {root, item, box, item2} = tree();
    box.used = true;
    item2.used = true;
    const attributeOf = (graph: any, node: any, name: string) => graph.dag.attributes[name]?.[graph.recordOf(node)];

    item2.bieNillable = true;
    let graph = search(new FixedDatabase(), root, 'Item').graph;
    expect(graph.recordOf(item2)).not.toBe(graph.recordOf(item));
    expect(graph.dag.attributes['nillable'][graph.recordOf(item2)]).toBe(true);
    expect(graph.dag.attributes['nillable'][graph.recordOf(item)]).toBe(false);

    item2.bieNillable = false;
    const code = item2.children[0] as any;
    code.bieNillable = true;
    graph = search(new FixedDatabase(), root, 'Item').graph;
    expect(graph.dag.attributes['nillable'][graph.recordOf(code)]).toBe(true);
    // only an ASBIEP and a BBIEP can be nillable
    expect(attributeOf(graph, root, 'nillable')).toBeUndefined();

    // deprecated on the BIE alone (the core component is not)
    code.bieNillable = false;
    code.deprecated = true;
    graph = search(new FixedDatabase(), root, 'Item').graph;
    expect(graph.dag.attributes['deprecated'][graph.recordOf(code)]).toBe(true);
    expect(attributeOf(graph, item.children[0], 'deprecated')).toBe(false);
  });

  it('searches the tree the user sees, which is only what the BIE uses, the same way', () => {
    const {root, item2} = tree();
    item2.used = true;

    const all = search(new FixedDatabase(), root, 'Item');
    const hidden = search(new FixedDatabase(), root, 'Item', {hideUnused: true});

    expect(all.found.total).toBe(2);
    expect(hidden.found.total).toBe(1);
    // Root, Box (leads to a used node), Item (used)
    expect(hidden.graph.dag.names.length).toBe(3);
    expect(search(new FixedDatabase(), root, 'Code', {hideUnused: true}).found.total).toBe(0);
    expect(search(new FixedDatabase(), root, 'Box/Item', {hideUnused: true}).found.total).toBe(1);
  });

  it('matches the names of a path from the root by what they contain, and starts at the root', () => {
    const {root} = tree();

    expect(search(new FixedDatabase(), root, '/Parent-1/Box/Item/Code').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, '/Parent-1/Bo').found.total).toBe(1);
    expect(search(new FixedDatabase(), root, '/Box/Item').found.total).toBe(0);
  });

  const serverPredicate = (operator: ServerPredicate['operator'], value: string, attribute = 'definition'): ServerPredicate =>
    ({id: '0', flag: 'srv_0', attribute, operator, value});

  it('flags the places the backend found text in, and tells them apart from the other places of the component', () => {
    const {root, item, item2} = tree();
    const marks = new BieServerMarks([serverPredicate('CONTAINS', 'second')], {
      matches: {'0': [{table: 'asbie', hashPath: item2.asbieHashPath, path: item2.asbiePath}]}
    }, false);

    const found = (expr: string) => search(new FixedDatabase(), root, expr, {marks}).found.total;
    expect(found('Item')).toBe(2);
    expect(found('Item[srv_0]')).toBe(1);
    expect(found('Item[not(srv_0)]')).toBe(1);
    expect(found('Box/Item[srv_0]/Code')).toBe(1);
    // the place without a match shares the record of its component, the other is a record of its own
    const {graph} = search(new FixedDatabase(), root, 'Item', {marks});
    expect(graph.recordOf(item2)).not.toBe(graph.recordOf(item));
    expect(graph.flagsOf(item2)).toEqual({srv_0: true});
    expect(graph.flagsOf(item)).toEqual({});
  });

  it('flags a place of the backend whether it is used or not', () => {
    const {root, item, box, item2} = tree();
    item.used = true;
    item.children[0].used = true;
    box.used = false;
    item2.used = false;
    item2.children[0].used = false;
    const code = item2.children[0];
    const marks = new BieServerMarks([serverPredicate('EQUALS', 'token', 'value_domain')], {
      matches: {'0': [{table: 'bbie', hashPath: (code as any).bbieHashPath, path: (code as any).bbiePath}]}
    }, false);

    expect(search(new FixedDatabase(), root, 'Box/Item/Code[srv_0]', {marks}).found.total).toBe(1);
    expect(search(new FixedDatabase(), root, 'Code[srv_0 and not(used)]', {marks}).found.total).toBe(1);
  });

  it('puts nothing of the BIE text into the graph when the backend has not been asked', () => {
    const {root, item2} = tree();
    item2._detail = {isChanged: false, asbie: {definition: 'The second item'}, asbiep: {}} as any;

    const {graph, found} = search(new FixedDatabase(), root, 'Item');

    expect(found.total).toBe(2);
    // the BIE text is not part of the graph, nor is any definition text
    expect(Object.keys(graph.dag.attributes ?? {}).filter(k => k.includes('definition'))).toEqual([]);
  });

  it('judges a place with unsaved text by that text instead of by what the backend has saved', () => {
    const {root, item2} = tree();
    const savedElsewhere = {table: 'asbie', hashPath: item2.asbieHashPath, path: item2.asbiePath};
    // the backend knows the old text; the editor has the new one
    item2._detail = {isChanged: true, asbie: {definition: 'New text'}, asbiep: {}} as any;

    const foundBy = (predicate: ServerPredicate, saved: boolean) => search(new FixedDatabase(), root, 'Item[srv_0]', {
      marks: new BieServerMarks([predicate], {matches: {'0': saved ? [savedElsewhere] : []}}, false)
    }).found.total;

    expect(foundBy(serverPredicate('CONTAINS', 'new'), false)).toBe(1);
    expect(foundBy(serverPredicate('CONTAINS', 'old'), true)).toBe(0);
  });

  it('flags the places by what the backend said about the definitions of the core components they profile', () => {
    const {root, item, item2} = tree();
    const none = {accs: [], asccs: [], asccps: [], bccs: [], bccps: [], dts: [], dtScs: []};
    const predicates = [
      {id: '0', flag: 'srv_0', attribute: 'association_definition', operator: 'CONTAINS', value: 'x'},
      {id: '1', flag: 'srv_1', attribute: 'component_definition', operator: 'CONTAINS', value: 'x'},
      {id: '2', flag: 'srv_2', attribute: 'type_definition', operator: 'CONTAINS', value: 'x'}
    ] as any;
    const ccMarks = new CcServerMarks(predicates, {matches: {
      '0': {...none, asccs: [10]},
      '1': {...none, asccps: [item.asccpNode.manifestId]},
      '2': {...none, accs: [item.accNode.manifestId]}
    }}, false);
    const found = (expr: string) => {
      const db = new FixedDatabase();
      db.filters.setActive('hideUnused', false);
      const graph = db.searchGraph(root, undefined, ccMarks);
      const pattern = new PathPattern(expr);
      return new DagPathSearch(graph.dag, pattern).search({
        ancestors: [], record: graph.recordOf(root), mask: pattern.stepAll([[root.name]])
      }).total;
    };

    // the association is the one of the first place (ASCC 10) only; the component and its type are those of both
    expect(found('Item[srv_0]')).toBe(1);
    expect(found('Item[srv_1]')).toBe(2);
    expect(found('Item[srv_2]')).toBe(2);
    // a BBIEP has an association and a component, but no type
    expect(found('Code[srv_2]')).toBe(0);
  });

  it('judges the value domain of a place by its row when it has one and by the default of its data type when it has none', () => {
    const {root, item, box, item2} = tree();
    const none = {accs: [], asccs: [], asccps: [], bccs: [], bccps: [], dts: [], dtScs: []};
    const code = item.children[0];
    // the two places of Code are two rows of the BIE
    const top = root.asbiepPath;
    (item as any)._asbiePath = top + '>ASCC-10';
    (code as any)._bbiePath = top + '>ASCC-10>BCC-20';
    (box as any)._asbiePath = top + '>ASCC-11';
    (item2 as any)._asbiePath = top + '>ASCC-11>ASCC-12';
    (item2.children[0] as any)._bbiePath = top + '>ASCC-11>ASCC-12>BCC-20';
    const predicates = [{id: '0', flag: 'srv_0', attribute: 'value_domain', operator: 'EQUALS', value: 'token'}] as any;
    // the data type of Code starts with token; the row of the first Code says something else
    const ccMarks = new CcServerMarks(predicates, {matches: {'0': {...none, bccps: [(code as any).bccpNode.manifestId]}}}, false);
    const row = {table: 'bbie', hashPath: (code as any).bbieHashPath, path: (code as any).bbiePath};
    const found = (expr: string, ownRow: boolean) => {
      const marks = new BieServerMarks([...predicates, BIE_VALUE_DOMAIN_ROW_PREDICATE],
        {matches: {'0': [], [BIE_VALUE_DOMAIN_ROW_PREDICATE.id]: ownRow ? [row] : []}}, false);
      const db = new FixedDatabase();
      db.filters.setActive('hideUnused', false);
      const graph = db.searchGraph(root, marks, ccMarks);
      const pattern = new PathPattern(expr);
      return new DagPathSearch(graph.dag, pattern).search({
        ancestors: [], record: graph.recordOf(root), mask: pattern.stepAll([[root.name]])
      }).total;
    };

    expect(found('Code[srv_0]', false)).toBe(2);
    expect(found('Code[srv_0]', true)).toBe(1);
    expect(found('Code[not(srv_0)]', true)).toBe(1);
  });

  it('judges the fixed value and the default value of a place by its row when the row stores one and by its core component when it does not', () => {
    const {root, item, box, item2} = tree();
    const none = {accs: [], asccs: [], asccps: [], bccs: [], bccps: [], dts: [], dtScs: []};
    const code = item.children[0];
    const top = root.asbiepPath;
    (item as any)._asbiePath = top + '>ASCC-10';
    (code as any)._bbiePath = top + '>ASCC-10>BCC-20';
    (box as any)._asbiePath = top + '>ASCC-11';
    (item2 as any)._asbiePath = top + '>ASCC-11>ASCC-12';
    (item2.children[0] as any)._bbiePath = top + '>ASCC-11>ASCC-12>BCC-20';
    const [fixedRow, defaultRow] = BIE_VALUE_CONSTRAINT_ROW_PREDICATES;
    const predicates = [{id: '0', flag: 'srv_0', attribute: 'fixed_value', operator: 'EQUALS', value: 'X'}] as any;
    // the BCCP of Code has the fixed value X, the BIE row of its first place has another fixed value (or a default value)
    const ccMarks = new CcServerMarks(predicates, {matches: {'0': {...none, bccps: [(code as any).bccpNode.manifestId]}}}, false);
    const row = {table: 'bbie', hashPath: (code as any).bbieHashPath, path: (code as any).bbiePath};
    const found = (expr: string, rows: {row: boolean, kind: 'fixed' | 'default'}) => {
      const matches: Record<string, any[]> = {'0': []};
      matches[fixedRow.id] = rows.row && rows.kind === 'fixed' ? [row] : [];
      matches[defaultRow.id] = rows.row && rows.kind === 'default' ? [row] : [];
      const marks = new BieServerMarks([...predicates, ...BIE_VALUE_CONSTRAINT_ROW_PREDICATES], {matches}, false);
      const db = new FixedDatabase();
      db.filters.setActive('hideUnused', false);
      const graph = db.searchGraph(root, marks, ccMarks);
      const pattern = new PathPattern(expr);
      return new DagPathSearch(graph.dag, pattern).search({
        ancestors: [], record: graph.recordOf(root), mask: pattern.stepAll([[root.name]])
      }).total;
    };

    // no row: both places say the value of the BCCP
    expect(found('Code[srv_0]', {row: false, kind: 'fixed'})).toBe(2);
    // a row with another fixed value, or with a default value: the first place is judged by its row, the second has none
    expect(found('Code[srv_0]', {row: true, kind: 'fixed'})).toBe(1);
    expect(found('Code[srv_0]', {row: true, kind: 'default'})).toBe(1);
    expect(found('Code[not(srv_0)]', {row: true, kind: 'default'})).toBe(1);
  });

  it('keeps a place that shows only some of its children apart from its component', () => {
    class FilteringDatabase extends FixedDatabase {
      constructor(private readonly filtered: Set<any>) {
        super();
      }

      protected override hasFilteredChildren(node: any): boolean {
        return this.filtered.has(node.self || node);
      }
    }
    const {root, item2} = tree();
    item2.used = true;
    item2.children = [];

    const db = new FilteringDatabase(new Set([item2]));
    const found = search(db, root, 'Code').found;

    // Item's own Code, and none below the filtered place
    expect(found.total).toBe(1);
  });
});
