/**
 * Domain model + pure logic for the instance-level sibling view order (Issue #1638). Kept out of the
 * Angular service ({@link ./bie-view-order.service}) so the data shapes and the (heavily unit-tested)
 * comparator / reweight / reorder-guard functions can be imported without the HttpClient dependency,
 * matching the repo convention (model in {@code *.ts}, service in {@code *.service.ts}).
 */

/**
 * One sibling view-order weight (Issue #1638), as returned by
 * {@code GET /bie-view-order/acc/{accManifestId}}.
 *
 * Keyed by the VIEW parent ({@code fromAccManifestId}) and the child association
 * ({@code asccManifestId} XOR {@code bccManifestId}). Higher weight sorts first.
 */
export interface BieViewOrderEntry {
  fromAccManifestId: number;
  asccManifestId?: number;
  bccManifestId?: number;
  weight: number;
}

/** A single child weight to upsert (or delete, when {@code weight} is null) under a view parent. */
export interface BieViewOrderUpdateEntry {
  asccManifestId?: number;
  bccManifestId?: number;
  weight: number | null;
}

export function asccViewOrderKey(fromAccManifestId: number, asccManifestId: number): string {
  return fromAccManifestId + ':ASCC:' + asccManifestId;
}

export function bccViewOrderKey(fromAccManifestId: number, bccManifestId: number): string {
  return fromAccManifestId + ':BCC:' + bccManifestId;
}

/** Build the client-side lookup ({@code "<fromAcc>:ASCC:<ascc>"} / {@code ":BCC:<bcc>"} -> weight). */
export function buildViewOrderMap(entries: BieViewOrderEntry[]): Map<string, number> {
  const map = new Map<string, number>();
  (entries || []).forEach(e => {
    if (e.asccManifestId !== undefined && e.asccManifestId !== null) {
      map.set(asccViewOrderKey(e.fromAccManifestId, e.asccManifestId), e.weight);
    } else if (e.bccManifestId !== undefined && e.bccManifestId !== null) {
      map.set(bccViewOrderKey(e.fromAccManifestId, e.bccManifestId), e.weight);
    }
  });
  return map;
}

/**
 * The default order weight for a sibling that has not been explicitly weighted (Issue #1638).
 * A positive weight therefore sorts ABOVE unset siblings and a negative weight BELOW them.
 */
export const DEFAULT_ORDER_WEIGHT = 0;

/**
 * The gap between consecutive weights assigned by a drag&drop reorder (Issue #1638).
 *
 * A step > 1 deliberately leaves room BETWEEN two reordered siblings so a developer can later slot a
 * third sibling between them with the "Set order weight…" dialog (e.g. a weight of 5 lands between a
 * dragged 10 and an unset 0) WITHOUT having to re-space the whole partition. The minimal-reweight pass
 * ({@link computeMinimalReweights}) still only (re)writes the siblings whose order is actually
 * violated, so such an in-between weight — even one that is not a multiple of the step — is left
 * untouched on a later drag as long as it still sorts correctly relative to the sibling below it.
 */
export const ORDER_WEIGHT_STEP = 10;

/**
 * The authoritative sibling-order comparator (Issue #1638), shared by the model browser and the
 * BIE editor so both order identically.
 *
 * Sort key: effective weight DESCENDING, where an UNSET sibling uses {@link DEFAULT_ORDER_WEIGHT}
 * (0) — so a weight > 0 moves above unset siblings, a weight < 0 below them. Ties are broken by
 * property term ASC, but ONLY between two EXPLICITLY-weighted siblings; whenever at least one side
 * is unset, ties keep the incoming (seq_key) order. That keeps the default view (nothing weighted,
 * everything at the default 0) byte-identical to the seq_key order.
 */
export function applyViewOrder<T>(list: T[],
                                  weightOf: (n: T) => number | undefined,
                                  nameOf: (n: T) => string): T[] {
  return list
    .map((n, i) => ({n, i}))
    .sort((a, b) => {
      const wa = weightOf(a.n);
      const wb = weightOf(b.n);
      const ea = (wa === undefined || wa === null) ? DEFAULT_ORDER_WEIGHT : wa;
      const eb = (wb === undefined || wb === null) ? DEFAULT_ORDER_WEIGHT : wb;
      if (ea !== eb) {
        return eb - ea; // effective weight DESC (higher first)
      }
      // Equal effective weight: property term ASC only when BOTH are explicitly weighted (rule #9);
      // otherwise keep the incoming (seq_key) order so the default view stays byte-identical.
      if (wa !== undefined && wa !== null && wb !== undefined && wb !== null) {
        const byName = (nameOf(a.n) || '').localeCompare(nameOf(b.n) || '');
        if (byName !== 0) {
          return byName;
        }
      }
      return a.i - b.i; // stable fallback (seq_key order)
    })
    .map(x => x.n);
}

/**
 * Compute the MINIMAL re-weighting for a drag&drop reorder (Issue #1638).
 *
 * Inputs (both in the NEW display order, top -> bottom, for the dragged sibling's partition):
 *  - `currentWeights[i]` — the sibling's current explicit weight, or undefined/null when unset;
 *  - `seqKeyRank[i]`      — the sibling's position in the DEFAULT (seq_key) order (lower = earlier).
 *
 * It walks bottom -> top and only assigns a weight where the order is actually violated, so it touches
 * as FEW siblings as possible:
 *  - a sibling stays UNSET if it is unset and already in seq_key order relative to the unset run below
 *    it (the comparator's default-0 / seq_key fallback already places it correctly);
 *  - an already-weighted sibling KEEPS its weight if that already sorts it above the sibling below
 *    (regardless of whether it is a multiple of {@link ORDER_WEIGHT_STEP} — a hand-inserted in-between
 *    weight is therefore preserved);
 *  - otherwise it gets the next weight one {@link ORDER_WEIGHT_STEP} above the sibling below it,
 *    leaving a gap so a sibling can later be slotted between the two.
 *
 * Returns a Map of {@code newOrderIndex -> new weight} for ONLY the siblings that must be (re)weighted;
 * an empty map means no write is needed.
 *
 * Example (the #1638 thread): siblings all unset, drag "Identifier" to just after "UUID" -> only UUID
 * gets weight 10; every other sibling stays unset (they remain in their correct seq_key order).
 */
export function computeMinimalReweights(currentWeights: (number | undefined | null)[],
                                        seqKeyRank: number[]): Map<number, number> {
  const result = new Map<number, number>();
  const n = currentWeights.length;
  if (n === 0) {
    return result;
  }
  const weightOf = (i: number) => {
    const w = currentWeights[i];
    return (w === undefined || w === null) ? null : w;
  };

  // The bottom sibling is never constrained from below, so it keeps whatever it has.
  let belowEff = (weightOf(n - 1) === null) ? DEFAULT_ORDER_WEIGHT : (weightOf(n - 1) as number);
  let belowUnset = weightOf(n - 1) === null;
  let belowRank = seqKeyRank[n - 1];

  for (let i = n - 2; i >= 0; i--) {
    const w = weightOf(i);
    if (w === null && belowUnset && seqKeyRank[i] < belowRank) {
      // Unset and already in seq_key order above the unset run below -> leave it unset.
      belowEff = DEFAULT_ORDER_WEIGHT;
      belowUnset = true;
    } else if (w !== null && w > belowEff) {
      // Already weighted and already sorts above the sibling below -> keep it.
      belowEff = w;
      belowUnset = false;
    } else {
      // Violation: assign the next step above the sibling below, leaving a gap for a later manual
      // insertion between the two (Issue #1638).
      const newWeight = belowEff + ORDER_WEIGHT_STEP;
      result.set(i, newWeight);
      belowEff = newWeight;
      belowUnset = false;
    }
    belowRank = seqKeyRank[i];
  }
  return result;
}

/**
 * One side of a drag-reorder validity check (Issue #1638): the minimal facts about a flattened sibling
 * the partition/level rules need. Mapped from a tree node by the model browser.
 */
export interface ReorderSiblingRef {
  /**
   * Identity of the node's DISPLAY (view) parent — two siblings match iff equal; undefined when the
   * node has no reorderable view parent. MUST be a POSITION-dependent key (e.g. the model browser's
   * positional hashPath): a key that collapses distinct positions to the same value (such as a
   * reused-subtree bare path) would wrongly treat unrelated nodes as siblings.
   */
  viewParentKey: string | undefined;
  /** Tree depth; reordering is only among same-level siblings. */
  level: number;
  /** Attribute (BCC entityType=Attribute) vs element (ASCCP / non-attribute BCC) — a HARD partition. */
  isAttribute: boolean;
  /** Stable identity of the node itself (e.g. its hashPath); a node dropped onto itself is a no-op. */
  key: string;
}

export const REORDER_REJECT_NOT_SIBLINGS = 'Components can only be reordered among siblings at the same level.';
export const REORDER_REJECT_ATTRIBUTE_INTO_ELEMENT = 'An attribute can\'t be reordered into the element area.';
export const REORDER_REJECT_ELEMENT_INTO_ATTRIBUTE = 'An element can\'t be reordered into the attribute area.';

/**
 * Why a drag-reorder of {@code dragged} onto {@code target} is rejected, or {@code null} when it is
 * valid — or a no-op (Issue #1638). Reordering is allowed ONLY among real siblings: same view parent,
 * same level, and the SAME attribute/element partition (attributes and elements are a hard partition,
 * mirroring the flattened layout and the generated schema). Pure so it can be exhaustively unit-tested
 * and shared by the (developer-only) drag handler and the drop guard.
 */
export function reorderRejectReason(dragged: ReorderSiblingRef, target: ReorderSiblingRef): string | null {
  if (!target || target.key === dragged.key) {
    return null; // dropped onto itself / nothing -> no-op
  }
  if (dragged.viewParentKey === undefined ||
      target.viewParentKey !== dragged.viewParentKey ||
      target.level !== dragged.level) {
    return REORDER_REJECT_NOT_SIBLINGS;
  }
  if (target.isAttribute !== dragged.isAttribute) {
    return dragged.isAttribute ? REORDER_REJECT_ATTRIBUTE_INTO_ELEMENT : REORDER_REJECT_ELEMENT_INTO_ATTRIBUTE;
  }
  return null;
}
