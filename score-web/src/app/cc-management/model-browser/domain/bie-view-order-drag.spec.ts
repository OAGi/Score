import {
  applyViewOrder,
  computeMinimalReweights,
  REORDER_REJECT_ATTRIBUTE_INTO_ELEMENT,
  REORDER_REJECT_ELEMENT_INTO_ATTRIBUTE,
  REORDER_REJECT_NOT_SIBLINGS,
  reorderRejectReason,
  ReorderSiblingRef
} from './bie-view-order';

/**
 * #1638 EXHAUSTIVE reorder tests. The model browser's drop() computes the weights to PUT via
 * computeMinimalReweights() and the view re-sorts via applyViewOrder(); the partition/level guard is
 * reorderRejectReason(). These are the pure cores, so we can drive EVERY permutation here.
 *
 * Covered:
 *  - drag i->j round-trip for every (from,to) at sizes 2..8 (all-unset start) — the dropped order is
 *    reproduced exactly, with a bounded, non-empty set of weight writes;
 *  - drag round-trips that START from already-weighted partitions (hand-verified scenarios);
 *  - the full attribute/element x level x view-parent reject matrix (what may NOT be dragged where);
 *  - manual single-weight moves (float a sibling to the front / to the back / to an exact slot).
 */

interface Sib {
  id: number;
  name: string;
  weight?: number;
}

/** Array reorder, exactly like a drag&drop move of one row. */
function move<T>(arr: T[], from: number, to: number): T[] {
  const copy = arr.slice();
  const [it] = copy.splice(from, 1);
  copy.splice(to, 0, it);
  return copy;
}

function displayOrder(seq: Sib[]): Sib[] {
  return applyViewOrder(seq, s => s.weight, s => s.name);
}

function ids(list: Sib[]): number[] {
  return list.map(s => s.id);
}

/**
 * Simulate the model browser drop() for a drag from display index `from` to display index `to`:
 * compute the minimal reweights, apply them, and return BOTH the order the drag intends (newDisplay)
 * and the order the app actually renders afterwards (re-sorting the seq_key list with the new weights).
 *
 * INVARIANT: `seq` MUST be given in seq_key (flatten / database) order — both the seq_key rank and
 * applyViewOrder's stable fallback key off its index, exactly as production seqKeyPartitions(parent)
 * and sortByViewOrder do. A fixture built in DISPLAY order would silently give a wrong oracle.
 */
function simulateDrag(seq: Sib[], from: number, to: number): {expected: number[]; actual: number[]; writes: number} {
  const current = displayOrder(seq);
  const newDisplay = move(current, from, to);
  const seqIndexById = new Map(seq.map((s, i) => [s.id, i]));
  const currentWeights = newDisplay.map(s => s.weight);
  const seqKeyRank = newDisplay.map(s => seqIndexById.get(s.id) as number);

  const reweights = computeMinimalReweights(currentWeights, seqKeyRank);

  const weightById = new Map<number, number | undefined>(seq.map(s => [s.id, s.weight]));
  reweights.forEach((w, idx) => weightById.set(newDisplay[idx].id, w));
  const applied = seq.map(s => ({...s, weight: weightById.get(s.id)}));

  return {expected: ids(newDisplay), actual: ids(displayOrder(applied)), writes: reweights.size};
}

function unsetSeq(n: number): Sib[] {
  // names ascending so the comparator's name tie-break (only hit between two weighted ties) agrees
  // with seq_key order — a tie never silently contradicts the dragged order in the all-unset sweep.
  return Array.from({length: n}, (_, i) => ({id: i, name: String.fromCharCode(97 + i)}));
}

describe('#1638 drag round-trip — every i->j permutation reproduces the dropped order (all-unset start)', () => {
  for (let n = 2; n <= 8; n++) {
    const seq = unsetSeq(n);
    for (let from = 0; from < n; from++) {
      for (let to = 0; to < n; to++) {
        if (from === to) {
          continue;
        }
        it(`N=${n}: drag display ${from} -> ${to}`, () => {
          const {expected, actual, writes} = simulateDrag(seq, from, to);
          // the rendered order after the PUT equals exactly the order the user dragged to
          expect(actual).toEqual(expected);
          // a real move always writes at least one weight, and never more than (N-1)
          expect(writes).toBeGreaterThanOrEqual(1);
          expect(writes).toBeLessThanOrEqual(n - 1);
        });
      }
    }
  }
});

describe('#1638 drag — named boundary moves over a 5-element partition (exact writes)', () => {
  // a,b,c,d,e at display 0..4, all unset. Each case names a position the user enumerated and pins the
  // exact number of weight writes, so a regression that over-writes is caught beyond the order check.
  const seq = unsetSeq(5);

  it('1st -> 2nd: order [b,a,c,d,e], exactly 1 write', () => {
    const r = simulateDrag(seq, 0, 1);
    expect(r.actual).toEqual([1, 0, 2, 3, 4]);
    expect(r.actual).toEqual(r.expected);
    expect(r.writes).toBe(1);
  });

  it('1st -> 3rd: order [b,c,a,d,e], exactly 2 writes', () => {
    const r = simulateDrag(seq, 0, 2);
    expect(r.actual).toEqual([1, 2, 0, 3, 4]);
    expect(r.writes).toBe(2);
  });

  it('1st -> 2nd-to-last: order [b,c,d,a,e], exactly 3 writes', () => {
    const r = simulateDrag(seq, 0, 3);
    expect(r.actual).toEqual([1, 2, 3, 0, 4]);
    expect(r.writes).toBe(3);
  });

  it('1st -> last: order [b,c,d,e,a], exactly 4 writes (n-1)', () => {
    const r = simulateDrag(seq, 0, 4);
    expect(r.actual).toEqual([1, 2, 3, 4, 0]);
    expect(r.writes).toBe(4);
  });

  it('3rd -> 1st: order [c,a,b,d,e], exactly 1 write', () => {
    const r = simulateDrag(seq, 2, 0);
    expect(r.actual).toEqual([2, 0, 1, 3, 4]);
    expect(r.writes).toBe(1);
  });

  it('2nd -> last and 2nd -> 1st both round-trip', () => {
    expect(simulateDrag(seq, 1, 4).actual).toEqual([0, 2, 3, 4, 1]);
    expect(simulateDrag(seq, 1, 0).actual).toEqual([1, 0, 2, 3, 4]);
  });
});

describe('#1638 drag round-trip — the issue-thread "fewest touches" cases', () => {
  it('all unset, drag the 1st just after the 2nd: exactly ONE write', () => {
    // [Identifier, UUID, Source] -> drag Identifier after UUID -> [UUID, Identifier, Source]
    const seq = [{id: 0, name: 'Identifier'}, {id: 1, name: 'UUID'}, {id: 2, name: 'Source'}];
    const {expected, actual, writes} = simulateDrag(seq, 0, 1);
    expect(actual).toEqual(expected);
    expect(actual).toEqual([1, 0, 2]);
    expect(writes).toBe(1);
  });

  it('21 siblings, drag the 1st down one slot: touches exactly ONE', () => {
    const seq = unsetSeq(21);
    const {expected, actual, writes} = simulateDrag(seq, 0, 1);
    expect(actual).toEqual(expected);
    expect(writes).toBe(1);
  });
});

describe('#1638 drag round-trip — starting from an already-weighted partition', () => {
  // display order here is [B(30), C(20), A(10), D(unset)] because weight sorts first.
  const weighted: Sib[] = [
    {id: 0, name: 'A', weight: 10},
    {id: 1, name: 'B', weight: 30},
    {id: 2, name: 'C', weight: 20},
    {id: 3, name: 'D'}
  ];

  it('sanity: the starting display order is weight-desc then unset', () => {
    expect(ids(displayOrder(weighted))).toEqual([1, 2, 0, 3]);
  });

  it('drag the unset D up to the very top', () => {
    const {expected, actual} = simulateDrag(weighted, 3, 0);
    expect(actual).toEqual(expected);
    expect(actual[0]).toBe(3);
  });

  it('drag the top B down to the bottom', () => {
    const {expected, actual} = simulateDrag(weighted, 0, 3);
    expect(actual).toEqual(expected);
    expect(actual[actual.length - 1]).toBe(1);
  });

  it('every adjacent swap in a weighted partition round-trips', () => {
    const current = displayOrder(weighted);
    for (let i = 0; i < current.length - 1; i++) {
      const {expected, actual} = simulateDrag(weighted, i, i + 1);
      expect(actual).toEqual(expected);
    }
  });
});

// ----------------------------------------------------------------------------------------------------
// Partition / level guard — what may NOT be dragged where (#1638).
// ----------------------------------------------------------------------------------------------------

function ref(over: Partial<ReorderSiblingRef>): ReorderSiblingRef {
  return {viewParentKey: 'P1', level: 2, isAttribute: false, key: 'k', ...over};
}

describe('#1638 reorderRejectReason — partition/level matrix', () => {
  it('valid: two elements under the same parent at the same level', () => {
    expect(reorderRejectReason(ref({key: 'a'}), ref({key: 'b'}))).toBeNull();
  });

  it('valid: two attributes under the same parent at the same level', () => {
    expect(reorderRejectReason(
      ref({key: 'a', isAttribute: true}), ref({key: 'b', isAttribute: true}))).toBeNull();
  });

  it('no-op: dropped onto itself', () => {
    expect(reorderRejectReason(ref({key: 'a'}), ref({key: 'a'}))).toBeNull();
  });

  it('rejected: an attribute dragged into the element area', () => {
    expect(reorderRejectReason(ref({key: 'a', isAttribute: true}), ref({key: 'b', isAttribute: false})))
      .toBe(REORDER_REJECT_ATTRIBUTE_INTO_ELEMENT);
  });

  it('rejected: an element dragged into the attribute area', () => {
    expect(reorderRejectReason(ref({key: 'a', isAttribute: false}), ref({key: 'b', isAttribute: true})))
      .toBe(REORDER_REJECT_ELEMENT_INTO_ATTRIBUTE);
  });

  it('rejected: a different view parent', () => {
    expect(reorderRejectReason(ref({key: 'a', viewParentKey: 'P1'}), ref({key: 'b', viewParentKey: 'P2'})))
      .toBe(REORDER_REJECT_NOT_SIBLINGS);
  });

  it('rejected: a different level (different depth in the tree)', () => {
    expect(reorderRejectReason(ref({key: 'a', level: 2}), ref({key: 'b', level: 3})))
      .toBe(REORDER_REJECT_NOT_SIBLINGS);
  });

  it('rejected: the dragged node has no reorderable view parent', () => {
    expect(reorderRejectReason(ref({key: 'a', viewParentKey: undefined}), ref({key: 'b'})))
      .toBe(REORDER_REJECT_NOT_SIBLINGS);
  });

  it('exhaustive: every (parent, level, partition) combination yields the documented outcome', () => {
    const parents = ['P1', 'P2'];
    const levels = [2, 3];
    const attrs = [false, true];
    let checked = 0;
    for (const dp of parents) {
      for (const dl of levels) {
        for (const da of attrs) {
          for (const tp of parents) {
            for (const tl of levels) {
              for (const ta of attrs) {
                const dragged = ref({key: 'D', viewParentKey: dp, level: dl, isAttribute: da});
                const target = ref({key: 'T', viewParentKey: tp, level: tl, isAttribute: ta});
                const reason = reorderRejectReason(dragged, target);
                if (dp !== tp || dl !== tl) {
                  expect(reason).toBe(REORDER_REJECT_NOT_SIBLINGS);
                } else if (da !== ta) {
                  expect(reason).toBe(da ? REORDER_REJECT_ATTRIBUTE_INTO_ELEMENT : REORDER_REJECT_ELEMENT_INTO_ATTRIBUTE);
                } else {
                  expect(reason).toBeNull(); // same parent + level + partition => valid
                }
                checked++;
              }
            }
          }
        }
      }
    }
    expect(checked).toBe(parents.length * levels.length * attrs.length * parents.length * levels.length * attrs.length);
  });
});

// ----------------------------------------------------------------------------------------------------
// Manual weight setting (the "Set order weight…" dialog) — float a sibling to a position (#1638).
// ----------------------------------------------------------------------------------------------------

describe('#1638 manual weight — single-weight moves over a 5-sibling partition', () => {
  const base = unsetSeq(5); // a,b,c,d,e at display 0..4 (all unset)

  function withWeight(id: number, weight: number | undefined): Sib[] {
    return base.map(s => (s.id === id ? {...s, weight} : {...s}));
  }

  it('a positive weight floats any sibling to the very front (others keep seq order)', () => {
    for (let id = 0; id < 5; id++) {
      const order = ids(displayOrder(withWeight(id, 100)));
      expect(order[0]).toBe(id);
      expect(order.slice(1)).toEqual(base.filter(s => s.id !== id).map(s => s.id));
    }
  });

  it('a negative weight sinks any sibling to the very back (others keep seq order)', () => {
    for (let id = 0; id < 5; id++) {
      const order = ids(displayOrder(withWeight(id, -100)));
      expect(order[order.length - 1]).toBe(id);
      expect(order.slice(0, -1)).toEqual(base.filter(s => s.id !== id).map(s => s.id));
    }
  });

  it('resetting a weight (undefined) returns the sibling to its seq_key slot', () => {
    const moved = withWeight(2, 100); // c to front
    expect(ids(displayOrder(moved))[0]).toBe(2);
    const reset = moved.map(s => (s.id === 2 ? {...s, weight: undefined} : s));
    expect(ids(displayOrder(reset))).toEqual([0, 1, 2, 3, 4]);
  });

  it('two manual weights land a sibling at an exact middle slot (b above c, c above the rest)', () => {
    // want display [c, b, a, d, e]: give c the highest, b the next; a/d/e stay unset
    const weighted = base.map(s => {
      if (s.id === 2) { return {...s, weight: 200}; } // c
      if (s.id === 1) { return {...s, weight: 100}; } // b
      return {...s};
    });
    expect(ids(displayOrder(weighted))).toEqual([2, 1, 0, 3, 4]);
  });

  it('equal positive weights fall back to property-term (name) ascending', () => {
    const weighted = base.map(s => (s.id === 0 || s.id === 3 ? {...s, weight: 50} : {...s}));
    // a(id0,'a',50) and d(id3,'d',50) both weighted -> name asc a<d, both above the unset b,c,e
    const order = ids(displayOrder(weighted));
    expect(order.slice(0, 2)).toEqual([0, 3]);
    expect(order.slice(2)).toEqual([1, 2, 4]);
  });
});
