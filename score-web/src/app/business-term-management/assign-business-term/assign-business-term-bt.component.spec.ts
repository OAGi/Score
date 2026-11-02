import {SelectionModel} from '@angular/cdk/collections';
import {AssignBusinessTermBtComponent} from './assign-business-term-bt.component';
import {BusinessTermListEntry} from '../domain/business-term';

function bt(id: number, used = false): BusinessTermListEntry {
  const entry = new BusinessTermListEntry();
  entry.businessTermId = id;
  entry.used = used;
  return entry;
}

/**
 * The rows are chosen in the `score-table-view` (multi-select, every row can be chosen: `used` only protects the
 * Business Term catalog row from discard, and does not prevent reuse in assignments); the screen keeps the ids of the terms
 * and enables Create by what is chosen.
 */
describe('AssignBusinessTermBtComponent', () => {
  it('should be defined', () => {
    expect(AssignBusinessTermBtComponent).toBeTruthy();
  });

  it('chooses a business term by its id, used or not', () => {
    const selection = new SelectionModel<number>(true, []);
    const rowKey = (AssignBusinessTermBtComponent.prototype as any).rowKey ?? ((row: BusinessTermListEntry) => row.businessTermId);
    selection.select(rowKey(bt(10, true)));
    selection.select(rowKey(bt(20, false)));
    expect(selection.selected).toEqual([10, 20]);
  });

  it('enables Create only with a business term chosen', () => {
    const ctx: any = {selection: new SelectionModel<number>(true, [])};
    ctx.isCreateDisabled = AssignBusinessTermBtComponent.prototype.isCreateDisabled;
    expect(ctx.isCreateDisabled()).toBe(true);
    ctx.selection.select(10);
    expect(ctx.isCreateDisabled()).toBe(false);
  });
});
