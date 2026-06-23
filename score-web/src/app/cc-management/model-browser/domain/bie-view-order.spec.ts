import {applyViewOrder, asccViewOrderKey, bccViewOrderKey, buildViewOrderMap, computeMinimalReweights, BieViewOrderEntry} from './bie-view-order';

interface Item {
  id: number;
  name: string;
  weight?: number;
}

function order(items: Item[]): number[] {
  return applyViewOrder(items, (i: Item) => i.weight, (i: Item) => i.name).map(i => i.id);
}

describe('applyViewOrder (#1638 sibling sort)', () => {
  it('is a stable no-op when nothing is weighted (keeps seq_key order)', () => {
    expect(order([{id: 1, name: 'C'}, {id: 2, name: 'A'}, {id: 3, name: 'B'}])).toEqual([1, 2, 3]);
  });

  it('orders weighted siblings by weight descending', () => {
    expect(order([{id: 1, name: 'A', weight: 10}, {id: 2, name: 'B', weight: 30}, {id: 3, name: 'C', weight: 20}]))
      .toEqual([2, 3, 1]);
  });

  it('treats unset siblings as the default weight 0: a positive weight sorts above them', () => {
    // [A, B, C] all unset; B := 100  ->  [B, A, C]
    expect(order([{id: 1, name: 'A'}, {id: 2, name: 'B', weight: 100}, {id: 3, name: 'C'}]))
      .toEqual([2, 1, 3]);
  });

  it('treats unset siblings as the default weight 0: a negative weight sorts below them', () => {
    // [A, B, C] all unset; B := -100  ->  [A, C, B]
    expect(order([{id: 1, name: 'A'}, {id: 2, name: 'B', weight: -100}, {id: 3, name: 'C'}]))
      .toEqual([1, 3, 2]);
  });

  it('orders positive > unset(0) > negative', () => {
    // A := -5, B unset (0), C := 5  ->  [C, B, A]
    expect(order([{id: 1, name: 'A', weight: -5}, {id: 2, name: 'B'}, {id: 3, name: 'C', weight: 5}]))
      .toEqual([3, 2, 1]);
  });

  it('keeps unset siblings in their incoming (seq_key) order, after any positively-weighted one', () => {
    expect(order([{id: 1, name: 'Z'}, {id: 2, name: 'Y', weight: 1}, {id: 3, name: 'X'}]))
      .toEqual([2, 1, 3]);
  });

  it('breaks equal weights by property term ascending', () => {
    expect(order([{id: 1, name: 'Banana', weight: 100}, {id: 2, name: 'Apple', weight: 100}]))
      .toEqual([2, 1]);
  });

  it('falls back to stable order when both weight and name tie', () => {
    expect(order([{id: 1, name: 'Same', weight: 7}, {id: 2, name: 'Same', weight: 7}]))
      .toEqual([1, 2]);
  });

  it('supports duplicate, non-sequential weights', () => {
    expect(order([{id: 1, name: 'A', weight: 5}, {id: 2, name: 'B', weight: 5}, {id: 3, name: 'C', weight: 9}]))
      .toEqual([3, 1, 2]);
  });

  it('does not mutate the input array', () => {
    const input: Item[] = [{id: 1, name: 'A', weight: 1}, {id: 2, name: 'B', weight: 2}];
    applyViewOrder(input, (i: Item) => i.weight, (i: Item) => i.name);
    expect(input.map(i => i.id)).toEqual([1, 2]);
  });
});

describe('computeMinimalReweights (#1638 drag — minimal touches)', () => {
  // Issue-thread scenario: seq_key order [Identifier, UUID, Source], all unset. Drag Identifier to
  // AFTER UUID -> new order [UUID, Identifier, Source]. Only UUID needs a weight; everything else
  // stays unset (already in correct seq_key order). seqKeyRank in new order: UUID=1, Identifier=0, Source=2.
  it('all unset, swap two: only the one node that must rise gets a weight (spaced by the step)', () => {
    const r = computeMinimalReweights([undefined, undefined, undefined], [1, 0, 2]);
    expect([...r.entries()]).toEqual([[0, 10]]); // UUID := 10 (ORDER_WEIGHT_STEP); Identifier & Source stay unset
  });

  it('does NOT re-weight far/unaffected siblings (21-sibling case touches 1)', () => {
    // new order [X, s0..s19] where X rose above s0; all unset; seqKeyRank: X=1, s0=0, s1=2, s2=3, ...
    const n = 21;
    const weights = new Array(n).fill(undefined);
    const ranks = [1, 0]; for (let i = 2; i < n; i++) { ranks.push(i); }
    const r = computeMinimalReweights(weights, ranks);
    expect(r.size).toBe(1);
    expect(r.get(0)).toBe(10);
  });

  it('drag A in front of B(100): only A := 110 (B kept, C stays unset)', () => {
    // new order [A, B, C], weights [unset, 100, unset], seqKeyRank [0,1,2]
    const r = computeMinimalReweights([undefined, 100, undefined], [0, 1, 2]);
    expect([...r.entries()]).toEqual([[0, 110]]);
  });

  it('drag B below C: B kept at 100, C := 110, A := 120 (each a step above the one below)', () => {
    // prior A=110,B=100,C unset; new order [A, C, B]; weights [110, unset, 100]; seqKeyRank [0,2,1]
    const r = computeMinimalReweights([110, undefined, 100], [0, 2, 1]);
    expect(r.get(2)).toBeUndefined(); // B kept at 100
    expect(r.get(1)).toBe(110);       // C := 100 + step
    expect(r.get(0)).toBe(120);       // A := 110 + step
    expect(r.size).toBe(2);
  });

  it('keeps a hand-inserted in-between weight (a non-step multiple) untouched when it still sorts right', () => {
    // display [X(10), Y(5), Z(unset)] is already the wanted order -> nothing is rewritten, so the
    // hand-set 5 (deliberately NOT a multiple of the step) survives a re-drag that needs no change.
    expect(computeMinimalReweights([10, 5, undefined], [0, 1, 2]).size).toBe(0);
    // a re-drag that lifts the unset Z above the hand-set Y(5): Y stays as the anchor, Z gets one
    // step above it (5 -> 15). The in-between 5 is preserved, only the moved sibling is written.
    const r = computeMinimalReweights([undefined, 5], [1, 0]); // new order top->bottom [Z(unset), Y(5)]
    expect(r.get(1)).toBeUndefined(); // Y(5) kept (bottom anchor)
    expect(r.get(0)).toBe(15);        // Z := 5 + step
    expect(r.size).toBe(1);
  });

  it('no-op when the new order already matches seq_key / current weights', () => {
    expect(computeMinimalReweights([undefined, undefined, undefined], [0, 1, 2]).size).toBe(0);
  });
});

describe('buildViewOrderMap + key helpers', () => {
  it('keys ASCC and BCC entries distinctly and ignores absent children', () => {
    const entries: BieViewOrderEntry[] = [
      {fromAccManifestId: 1, asccManifestId: 2, weight: 100},
      {fromAccManifestId: 1, bccManifestId: 3, weight: 200},
    ];
    const map = buildViewOrderMap(entries);
    expect(map.get(asccViewOrderKey(1, 2))).toBe(100);
    expect(map.get(bccViewOrderKey(1, 3))).toBe(200);
    expect(map.size).toBe(2);
  });

  it('distinguishes the same child id under different view parents', () => {
    expect(asccViewOrderKey(1, 5)).not.toEqual(asccViewOrderKey(2, 5));
    expect(asccViewOrderKey(1, 5)).not.toEqual(bccViewOrderKey(1, 5));
  });

  it('handles empty / undefined input', () => {
    expect(buildViewOrderMap([]).size).toBe(0);
    expect(buildViewOrderMap(undefined as unknown as BieViewOrderEntry[]).size).toBe(0);
  });
});
