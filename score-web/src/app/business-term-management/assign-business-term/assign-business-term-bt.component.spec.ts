import {SelectionModel} from '@angular/cdk/collections';
import {AssignBusinessTermBtComponent} from './assign-business-term-bt.component';
import {BusinessTermListEntry} from '../domain/business-term';

function bt(id: number, used = false): BusinessTermListEntry {
  const entry = new BusinessTermListEntry();
  entry.businessTermId = id;
  entry.used = used;
  return entry;
}

function componentCtx(rows: BusinessTermListEntry[] = []): any {
  const ctx: any = {
    selection: new SelectionModel<number>(true, []),
    dataSource: {data: rows},
  };
  const proto = AssignBusinessTermBtComponent.prototype;
  ctx.isAllSelected = proto.isAllSelected;
  ctx.masterToggle = proto.masterToggle;
  ctx.select = proto.select;
  ctx.toggle = proto.toggle;
  ctx.isSelected = proto.isSelected;
  ctx.isCreateDisabled = proto.isCreateDisabled;
  return ctx;
}

describe('AssignBusinessTermBtComponent', () => {
  it('should be defined', () => {
    expect(AssignBusinessTermBtComponent).toBeTruthy();
  });

  it('allows assignment selection for a used business term', () => {
    const ctx = componentCtx();
    ctx.toggle(bt(10, true));
    expect(ctx.isSelected(bt(10, true))).toBe(true);
    expect(ctx.isCreateDisabled()).toBe(false);
  });

  it('master toggle includes used business terms', () => {
    const ctx = componentCtx([bt(10, true), bt(20, false)]);
    ctx.masterToggle();
    expect(ctx.selection.selected).toEqual([10, 20]);
    expect(ctx.isAllSelected()).toBe(true);
  });
});
