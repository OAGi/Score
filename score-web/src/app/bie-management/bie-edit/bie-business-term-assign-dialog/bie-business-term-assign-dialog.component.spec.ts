import {SelectionModel} from '@angular/cdk/collections';
import {BieBusinessTermAssignDialogComponent} from './bie-business-term-assign-dialog.component';
import {BusinessTermListEntry} from '../../../business-term-management/domain/business-term';

/**
 * In-place Business Term assign dialog. The row-selection and Create-enablement logic is a pure function of
 * `this` (a multi-select SelectionModel keyed by businessTermId), so bind the prototype methods to a minimal
 * data bag — no Angular wiring required (mirrors the bie-oas-doc-add-dialog spec).
 */
function bt(id: number, used = false): BusinessTermListEntry {
  const entry = new BusinessTermListEntry();
  entry.businessTermId = id;
  entry.used = used;
  return entry;
}

function dialogCtx(rows: BusinessTermListEntry[] = []): any {
  const ctx: any = {
    selection: new SelectionModel<number>(true, []),
    loading: false,
    table: {
      dataSource: {
        data: rows
      }
    }
  };
  const proto = BieBusinessTermAssignDialogComponent.prototype;
  ctx.select = proto.select;
  ctx.toggle = proto.toggle;
  ctx.isSelected = proto.isSelected;
  ctx.visibleRows = proto['visibleRows'];
  ctx.isAllSelected = proto.isAllSelected;
  ctx.isPartiallySelected = proto.isPartiallySelected;
  ctx.masterToggle = proto.masterToggle;
  ctx.isCreateDisabled = proto.isCreateDisabled;
  return ctx;
}

describe('BieBusinessTermAssignDialogComponent selection', () => {
  it('selects an assignable row', () => {
    const ctx = dialogCtx();
    ctx.toggle(bt(10));
    expect(ctx.isSelected(bt(10))).toBe(true);
    expect(ctx.isCreateDisabled()).toBe(false);
  });

  it('allows a business term already referenced by another component', () => {
    const ctx = dialogCtx();
    ctx.toggle(bt(10, true));
    expect(ctx.isSelected(bt(10, true))).toBe(true);
    expect(ctx.isCreateDisabled()).toBe(false);
  });

  it('is multi-select: choosing another row preserves the previous choice', () => {
    const ctx = dialogCtx();
    ctx.toggle(bt(10));
    ctx.toggle(bt(20));
    expect(ctx.isSelected(bt(10))).toBe(true);
    expect(ctx.isSelected(bt(20))).toBe(true);
    expect(ctx.selection.selected.length).toBe(2);
  });

  it('toggling the only selected row off disables Create', () => {
    const ctx = dialogCtx();
    ctx.toggle(bt(10));
    ctx.toggle(bt(10));
    expect(ctx.isSelected(bt(10))).toBe(false);
    expect(ctx.isCreateDisabled()).toBe(true);
  });

  it('master toggle selects and clears visible rows', () => {
    const ctx = dialogCtx([bt(10), bt(20)]);
    ctx.masterToggle();
    expect(ctx.selection.selected).toEqual([10, 20]);
    expect(ctx.isAllSelected()).toBe(true);
    expect(ctx.isPartiallySelected()).toBe(false);

    ctx.masterToggle();
    expect(ctx.selection.selected).toEqual([]);
    expect(ctx.isAllSelected()).toBe(false);
  });

  it('tracks partial selection for visible rows', () => {
    const ctx = dialogCtx([bt(10), bt(20)]);
    ctx.toggle(bt(10));
    expect(ctx.isAllSelected()).toBe(false);
    expect(ctx.isPartiallySelected()).toBe(true);
  });

  it('disables Create while loading even with a selection', () => {
    const ctx = dialogCtx();
    ctx.toggle(bt(10));
    ctx.loading = true;
    expect(ctx.isCreateDisabled()).toBe(true);
  });
});
