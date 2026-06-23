import {APIRequestContext, expect, request as apiRequest, test} from '@playwright/test';

/**
 * #1638 sibling view order — end-to-end REST round-trip.
 *
 * Exercises the committed backend endpoints the model browser / BIE editor use:
 *   GET  /api/bie-view-order/acc/{accManifestId}   (read, any viewer)
 *   PUT  /api/bie-view-order/acc/{accManifestId}   (upsert/delete, DEVELOPER-only)
 *
 * It is intentionally driven at the HTTP layer (not via UI choreography) so it is deterministic and
 * SELF-CLEANING: it snapshots the target child's current weight up front and restores it in afterAll
 * (re-setting the original weight, or deleting the row when it was unset). Nothing is left behind, so
 * it is safe to run against a shared/dev instance.
 *
 * It runs ONLY when a live stack + credentials are supplied via env (otherwise it reports skipped, so
 * `npm run e2e` stays green without a backend):
 *   E2E_USERNAME / E2E_PASSWORD   developer login (the write path is developer-only)
 *   E2E_VIEW_ORDER_ACC            a view-parent acc_manifest_id (the expanded ACC node)
 *   E2E_VIEW_ORDER_ASCC           one child ascc_manifest_id flattened under that parent
 *   E2E_BASE_URL                  optional; defaults to http://localhost:4200 (the dev server, which
 *                                 proxies /api to the backend). "localhost" resolves to both IPv4 and
 *                                 IPv6, so it works whether `ng serve` bound 127.0.0.1 or [::1] (it
 *                                 binds ::1 by default, where a literal 127.0.0.1 would be refused).
 *                                 Point it at a remote instance to test there.
 */

const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:4200';
const USERNAME = process.env.E2E_USERNAME;
const PASSWORD = process.env.E2E_PASSWORD;
const ACC = process.env.E2E_VIEW_ORDER_ACC;
const ASCC = process.env.E2E_VIEW_ORDER_ASCC;

const haveLiveEnv = !!(USERNAME && PASSWORD && ACC && ASCC);

// A weight high enough to be recognizably ours (and to win the sort), unlikely to match a real one.
const TEST_WEIGHT = 987654;

if (!haveLiveEnv) {
  test('#1638 view-order REST round-trip (skipped — set E2E_* to run live)', () => {
    test.skip(true,
      'Provide E2E_USERNAME, E2E_PASSWORD, E2E_VIEW_ORDER_ACC and E2E_VIEW_ORDER_ASCC ' +
      '(developer credentials + a view-parent ACC and one child ASCC) to run this against a live stack.');
  });
} else {
  test.describe('#1638 sibling view order — REST round-trip (developer-only, self-cleaning)', () => {
    // Serial: the two tests share one APIRequestContext + one DB row + one beforeAll/afterAll. Under
    // fullyParallel they would split across workers and clobber each other's row via afterAll.
    test.describe.configure({mode: 'serial'});

    let api: APIRequestContext;
    let originalWeight: number | null = null;

    const orderPath = `/api/bie-view-order/acc/${ACC}`;

    async function getEntries(): Promise<Array<{asccManifestId: number; bccManifestId: number; weight: number}>> {
      const res = await api.get(orderPath);
      expect(res.ok(), `GET ${orderPath} -> ${res.status()}`).toBeTruthy();
      return await res.json();
    }

    /** The current weight of the target child under the view parent, or null when it is unset. */
    function weightOfTargetAscc(entries: Array<{asccManifestId: number; weight: number}>): number | null {
      const hit = entries.find(e => String(e.asccManifestId) === String(ASCC));
      return hit ? hit.weight : null;
    }

    /** Upsert (number) or reset/delete (null) the target child's weight under the view parent. */
    async function putWeight(weight: number | null) {
      const res = await api.put(orderPath, {data: {entries: [{asccManifestId: Number(ASCC), weight}]}});
      expect(res.ok(), `PUT ${orderPath} weight=${weight} -> ${res.status()}`).toBeTruthy();
    }

    test.beforeAll(async () => {
      api = await apiRequest.newContext({baseURL: BASE_URL});
      // Spring Security form login; the session cookie persists in this context for later calls.
      const login = await api.post('/api/login', {form: {username: USERNAME!, password: PASSWORD!}});
      expect(login.ok(), `login -> ${login.status()} (need valid developer credentials)`).toBeTruthy();
      // Snapshot the pre-test weight so afterAll can restore it exactly — no DB pollution.
      originalWeight = weightOfTargetAscc(await getEntries());
    });

    test.afterAll(async () => {
      if (!api) {
        return;
      }
      try {
        await putWeight(originalWeight); // number -> restore; null -> delete the row (back to unset)
      } finally {
        await api.dispose();
      }
    });

    test('GET returns an array for any viewer (read path)', async () => {
      expect(Array.isArray(await getEntries())).toBeTruthy();
    });

    test('PUT upserts a child weight, GET reads it back, and null resets it (write path)', async () => {
      await putWeight(TEST_WEIGHT);
      expect(weightOfTargetAscc(await getEntries())).toBe(TEST_WEIGHT);

      // A null weight removes the row, so the child drops back to its seq_key position.
      await putWeight(null);
      expect(weightOfTargetAscc(await getEntries())).toBeNull();
    });
  });
}
