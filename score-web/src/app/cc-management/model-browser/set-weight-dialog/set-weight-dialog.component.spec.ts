import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SetWeightDialogComponent, SetWeightDialogData} from './set-weight-dialog.component';

/**
 * #1638 "Set order weight…" dialog — result contract (MatDialogRef.close):
 *   a number  => set this weight (integer-truncated)
 *   null      => reset to the default (seq_key) position
 *   undefined => cancel (no change)
 */
describe('SetWeightDialogComponent (#1638)', () => {
  const SENTINEL = Symbol('not-closed');
  let closed: unknown;

  function create(data: SetWeightDialogData): SetWeightDialogComponent {
    closed = SENTINEL;
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [SetWeightDialogComponent],
      providers: [
        {provide: MatDialogRef, useValue: {close: (v: unknown) => (closed = v)}},
        {provide: MAT_DIALOG_DATA, useValue: data}
      ]
    });
    return TestBed.createComponent(SetWeightDialogComponent).componentInstance;
  }

  it('prefills from currentWeight and reports hasCurrent', () => {
    const c = create({name: 'Identifier', currentWeight: 7});
    expect(c.weight).toBe(7);
    expect(c.hasCurrent).toBe(true);
    expect(c.isValid()).toBe(true);
  });

  it('starts empty (null) and invalid when there is no current weight', () => {
    const c = create({name: 'Identifier'});
    expect(c.weight).toBeNull();
    expect(c.hasCurrent).toBe(false);
    expect(c.isValid()).toBe(false);
  });

  it('save() closes with the integer weight, truncating decimals', () => {
    const c = create({name: 'X'});
    c.weight = 5.9 as unknown as number;
    c.save();
    expect(closed).toBe(5);

    const d = create({name: 'X'});
    d.weight = -3.7 as unknown as number;
    d.save();
    expect(closed).toBe(-3);
  });

  it('save() is a no-op while invalid (null / NaN)', () => {
    const c = create({name: 'X'});
    c.weight = null;
    c.save();
    expect(closed).toBe(SENTINEL);

    c.weight = NaN as unknown as number;
    expect(c.isValid()).toBe(false);
    c.save();
    expect(closed).toBe(SENTINEL);
  });

  it('save() accepts an explicit zero weight', () => {
    const c = create({name: 'X'});
    c.weight = 0;
    expect(c.isValid()).toBe(true);
    c.save();
    expect(closed).toBe(0);
  });

  it('reset() closes with null (reset to default)', () => {
    const c = create({name: 'X', currentWeight: 9});
    c.reset();
    expect(closed).toBeNull();
  });

  it('cancel() closes with undefined (no change)', () => {
    const c = create({name: 'X', currentWeight: 9});
    c.cancel();
    expect(closed).toBeUndefined();
  });
});
