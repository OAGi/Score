import {APIRequestContext, expect, request as apiRequest, test} from '@playwright/test';

/**
 * #1638 sibling view order — release scoping & cross-reference, end-to-end.
 *
 * Proves the instance-level / manifest-keyed semantics against a live OAGIS library:
 *   1. A reorder set in release A (e.g. 10.6) is stored under that release's ACC manifest and is
 *      read back there (the change is applied in 10.6).
 *   2. The SAME logical ACC in release B (e.g. 10.8) is a DIFFERENT manifest, so the reorder does NOT
 *      appear there (10.8 is unchanged).
 *   3. The reordered ACC manifest is the SAME one reachable under "Get Purchase Order" → Data Area, so
 *      the reorder shows everywhere that ACC appears in release A (Get/Process Purchase Order, …).
 *   4. The BIE editor keys its view order by that same ACC manifest id — see the bie-flat-tree.spec
 *      unit test "viewParentAccManifestId() is the node's accNode.manifestId (the view-order key)" —
 *      so a BIE rooted at Purchase Order / Get Purchase Order in release A shows the reorder, and in
 *      release B (a different manifest) does not. (A full BIE-creation e2e is deliberately omitted: it
 *      adds business-context/state setup and BIE cleanup with no extra coverage of the keying.)
 *
 * Driven at the HTTP layer (no UI), SELF-DISCOVERING (library → releases → ACC → graph) and
 * SELF-CLEANING (snapshots and restores the touched weight). Runs ONLY with developer credentials and
 * when the expected OAGIS data is present; otherwise it reports skipped, so `npm run e2e` stays green.
 *
 *   E2E_USERNAME / E2E_PASSWORD   developer login (the write path is developer-only)            [required]
 *   E2E_BASE_URL                  default http://localhost:4200 (dev server proxies /api;          [optional]
 *                                 "localhost" works for both 127.0.0.1 and the IPv6 [::1] that
 *                                 `ng serve` binds by default — a literal 127.0.0.1 may be refused)
 *   E2E_LIBRARY                   library name; default = the library marked default / the first [optional]
 *   E2E_RELEASE_A / E2E_RELEASE_B release numbers; default "10.6" / "10.8"                       [optional]
 *   E2E_ACC_OBJECT_CLASS_TERM        ACC object class term to reorder; default "Purchase Order"      [optional]
 *   E2E_CROSS_REF_ASCCP_PROPERTY_TERM cross-ref root ASCCP property term; default "Get Purchase Order" [optional]
 */

const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:4200';
const USERNAME = process.env.E2E_USERNAME;
const PASSWORD = process.env.E2E_PASSWORD;
const LIBRARY_NAME = process.env.E2E_LIBRARY;
const RELEASE_A = process.env.E2E_RELEASE_A || '10.6';
const RELEASE_B = process.env.E2E_RELEASE_B || '10.8';
const ACC_OBJECT_CLASS_TERM = process.env.E2E_ACC_OBJECT_CLASS_TERM || 'Purchase Order';
const CROSS_REF_ASCCP_PROPERTY_TERM = process.env.E2E_CROSS_REF_ASCCP_PROPERTY_TERM || 'Get Purchase Order';

const TEST_WEIGHT = 987654;

const norm = (s: string) => (s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
/** Object-class / property term of a CC entry = the part before the first '.' of its DEN. */
const headTerm = (den: string) => (den || '').split('.')[0].trim();

if (!(USERNAME && PASSWORD)) {
  test('#1638 release-scope e2e (skipped — set E2E_USERNAME/E2E_PASSWORD to run live)', () => {
    test.skip(true, 'Provide developer E2E_USERNAME and E2E_PASSWORD (and a live OAGIS library) to run.');
  });
} else {
  test.describe('#1638 sibling view order — release scoping & cross-reference (self-cleaning)', () => {
    // Serial: these tests share one APIRequestContext + one DB row (the weight under accA) and one
    // beforeAll/afterAll lifecycle. Under fullyParallel they would otherwise split across workers, each
    // running afterAll's delete — clobbering the other worker's row mid-test.
    test.describe.configure({mode: 'serial'});

    let api: APIRequestContext;
    let skipReason: string | null = null;

    // Discovered context.
    let accA: number | undefined;            // Purchase Order ACC manifest in release A
    let accB: number | undefined;            // Purchase Order ACC manifest in release B
    let childAsccA: number | undefined;      // a child ASCC of accA to reorder
    let originalWeightA: number | null = null;
    let crossRefAccIds: number[] = [];       // ACC manifests reachable from "Get Purchase Order" in A

    async function getJson(url: string): Promise<any> {
      const res = await api.get(url);
      if (!res.ok()) {
        throw new Error(`GET ${url} -> ${res.status()}`);
      }
      return res.json();
    }

    async function findLibraryId(): Promise<number> {
      const libs: any[] = await getJson('/api/libraries/summaries');
      const lib = LIBRARY_NAME
        ? libs.find(l => norm(l.name) === norm(LIBRARY_NAME))
        : (libs.find(l => l.isDefault) || libs[0]);
      if (!lib) {
        throw new Error(`No library found${LIBRARY_NAME ? ` named "${LIBRARY_NAME}"` : ''}.`);
      }
      return lib.libraryId;
    }

    async function findReleaseId(libraryId: number, releaseNum: string): Promise<number | undefined> {
      const releases: any[] = await getJson(`/api/releases/summaries?libraryId=${libraryId}`);
      return releases.find(r => norm(r.releaseNum) === norm(releaseNum))?.releaseId;
    }

    /** First CC entry of `type` in the release whose DEN head-term equals `name`. */
    async function findCc(libraryId: number, releaseId: number, type: 'acc' | 'asccp', name: string): Promise<any | undefined> {
      const url = `/api/core-components?libraryId=${libraryId}&releaseId=${releaseId}`
        + `&types=${type}&den=${encodeURIComponent(name)}&pageIndex=0&pageSize=50`;
      const page = await getJson(url);
      const list: any[] = page.list || [];
      return list.find(e => norm(e.type) === norm(type) && norm(headTerm(e.den)) === norm(name));
    }

    /**
     * The manifest id of the first ASCC child of an ACC, via the ACC-rooted graph. Follows a leading
     * based-ACC target (an ACC whose first edge is another ACC inherits that ACC's children), so it
     * still finds a child for ACCs that declare their associations on a base.
     */
    async function firstChildAsccManifest(accManifestId: number): Promise<number | undefined> {
      const graph = await getJson(`/api/graphs/acc/${accManifestId}`);
      const nodes = graph.graph.nodes;
      const edges = graph.graph.edges;
      const queue: string[] = ['ACC-' + accManifestId];
      const seen = new Set<string>();
      while (queue.length) {
        const key = queue.shift() as string;
        if (seen.has(key)) {
          continue;
        }
        seen.add(key);
        const targets: string[] = edges[key]?.targets || [];
        for (const t of targets) {
          if (t.startsWith('ASCC-')) {
            const node = nodes[t];
            if (node && node.manifestId) {
              return node.manifestId;
            }
          } else if (t.startsWith('ACC-')) {
            queue.push(t); // based ACC: inherited associations live under it
          }
        }
      }
      return undefined;
    }

    /** All ACC manifest ids reachable in an ASCCP-rooted graph (for the cross-reference proof). */
    async function reachableAccIds(asccpManifestId: number): Promise<number[]> {
      const graph = await getJson(`/api/graphs/asccp/${asccpManifestId}`);
      const nodes = graph.graph.nodes;
      return Object.keys(nodes)
        .filter(k => k.startsWith('ACC-'))
        .map(k => nodes[k].manifestId)
        .filter((v: any) => !!v);
    }

    async function viewOrderEntries(accManifestId: number): Promise<any[]> {
      return getJson(`/api/bie-view-order/acc/${accManifestId}`);
    }

    async function putWeight(accManifestId: number, asccManifestId: number, weight: number | null) {
      const res = await api.put(`/api/bie-view-order/acc/${accManifestId}`,
        {data: {entries: [{asccManifestId, weight}]}});
      expect(res.ok(), `PUT /bie-view-order/acc/${accManifestId} -> ${res.status()}`).toBeTruthy();
    }

    test.beforeAll(async () => {
      api = await apiRequest.newContext({baseURL: BASE_URL});
      const login = await api.post('/api/login', {form: {username: USERNAME!, password: PASSWORD!}});
      if (!login.ok()) {
        skipReason = `login failed (${login.status()}) — need valid developer credentials`;
        return;
      }
      try {
        const libraryId = await findLibraryId();
        const releaseAId = await findReleaseId(libraryId, RELEASE_A);
        const releaseBId = await findReleaseId(libraryId, RELEASE_B);
        if (!releaseAId || !releaseBId) {
          skipReason = `releases "${RELEASE_A}" and/or "${RELEASE_B}" not found in this library`;
          return;
        }

        const ccA = await findCc(libraryId, releaseAId, 'acc', ACC_OBJECT_CLASS_TERM);
        const ccB = await findCc(libraryId, releaseBId, 'acc', ACC_OBJECT_CLASS_TERM);
        if (!ccA || !ccB) {
          skipReason = `ACC "${ACC_OBJECT_CLASS_TERM}" not found in both releases`;
          return;
        }
        accA = ccA.manifestId;
        accB = ccB.manifestId;

        childAsccA = await firstChildAsccManifest(accA!);
        if (!childAsccA) {
          skipReason = `ACC "${ACC_OBJECT_CLASS_TERM}" in ${RELEASE_A} has no ASCC child to reorder`;
          return;
        }

        // Snapshot the child's current weight (restored in afterAll).
        const before = await viewOrderEntries(accA!);
        const hit = before.find(e => String(e.asccManifestId) === String(childAsccA));
        originalWeightA = hit ? hit.weight : null;

        // Cross-reference root: "Get Purchase Order" ASCCP in release A.
        const parent = await findCc(libraryId, releaseAId, 'asccp', CROSS_REF_ASCCP_PROPERTY_TERM);
        if (parent) {
          crossRefAccIds = await reachableAccIds(parent.manifestId);
        }
      } catch (e: any) {
        skipReason = `discovery failed: ${e?.message || e}`;
      }
    });

    test.afterAll(async () => {
      if (api) {
        try {
          if (accA && childAsccA) {
            await putWeight(accA, childAsccA, originalWeightA); // restore (number) or delete (null)
          }
        } finally {
          await api.dispose();
        }
      }
    });

    test('a reorder set in release A is read back there; release B (different manifest) is unaffected', async () => {
      test.skip(!!skipReason, skipReason || '');

      expect(accA, 'PO ACC manifest in A').toBeTruthy();
      expect(accB, 'PO ACC manifest in B').toBeTruthy();
      // Release-scoped keying: the same object class is a different manifest per release.
      expect(accA).not.toBe(accB);

      await putWeight(accA!, childAsccA!, TEST_WEIGHT);

      const inA = await viewOrderEntries(accA!);
      const hitA = inA.find(e => String(e.asccManifestId) === String(childAsccA));
      expect(hitA, `child ${childAsccA} weighted under A`).toBeTruthy();
      expect(hitA.weight).toBe(TEST_WEIGHT);

      // Release B must not carry our change (its manifests are entirely separate).
      const inB = await viewOrderEntries(accB!);
      expect(inB.some((e: any) => e.weight === TEST_WEIGHT)).toBe(false);
    });

    test('the reordered ACC is the same manifest reachable under "Get Purchase Order" (cross-reference)', async () => {
      test.skip(!!skipReason, skipReason || '');
      test.skip(crossRefAccIds.length === 0, `"${CROSS_REF_ASCCP_PROPERTY_TERM}" not found in ${RELEASE_A}`);

      // The Purchase Order ACC reached under Get Purchase Order → Data Area is the SAME manifest we
      // reordered, so the weight set on it shows there too (and under Process Purchase Order, etc.).
      expect(crossRefAccIds).toContain(accA);
    });
  });
}
