import {SelectionModel} from '@angular/cdk/collections';
import {BieBusinessTermAssignDialogComponent} from './bie-business-term-assign-dialog.component';

/**
 * In-place Business Term assign dialog. The rows are chosen in the `score-table-view` (multi-select, keyed by
 * businessTermId, every row can be chosen); the Create-enablement logic is a pure function of `this`, so bind the prototype
 * method to a minimal data bag — no Angular wiring required (mirrors the bie-oas-doc-add-dialog spec).
 */
function dialogCtx(): any {
  const ctx: any = {
    selection: new SelectionModel<number>(true, []),
    busy: () => false,
    view: () => ({loading: () => false})
  };
  ctx.isCreateDisabled = BieBusinessTermAssignDialogComponent.prototype.isCreateDisabled;
  return ctx;
}

describe('BieBusinessTermAssignDialogComponent selection', () => {
  it('disables Create until a business term is chosen', () => {
    const ctx = dialogCtx();
    expect(ctx.isCreateDisabled()).toBe(true);
    ctx.selection.select(10);
    expect(ctx.isCreateDisabled()).toBe(false);
  });

  it('is multi-select: choosing another row preserves the previous choice', () => {
    const ctx = dialogCtx();
    ctx.selection.select(10);
    ctx.selection.select(20);
    expect(ctx.selection.selected).toEqual([10, 20]);
    expect(ctx.isCreateDisabled()).toBe(false);
  });

  it('toggling the only selected row off disables Create', () => {
    const ctx = dialogCtx();
    ctx.selection.select(10);
    ctx.selection.deselect(10);
    expect(ctx.isCreateDisabled()).toBe(true);
  });

  it('disables Create while the list or the assignment is loading, even with a selection', () => {
    const ctx = dialogCtx();
    ctx.selection.select(10);
    ctx.busy = () => true;
    expect(ctx.isCreateDisabled()).toBe(true);
    ctx.busy = () => false;
    ctx.view = () => ({loading: () => true});
    expect(ctx.isCreateDisabled()).toBe(true);
  });
});
