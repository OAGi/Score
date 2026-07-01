import {ContextSchemeCreateComponent} from './context-scheme-create.component';
import {ContextSchemeValue} from '../domain/context-scheme';

describe('ContextSchemeCreateComponent', () => {
  it('should be defined', () => {
    expect(ContextSchemeCreateComponent).toBeTruthy();
  });
});

function value(guid: string | undefined, val: string, meaning = ''): ContextSchemeValue {
  return {guid, value: val, meaning} as ContextSchemeValue;
}

/**
 * Issue #1744 class regression: the create page's context-scheme-value de-duplication must exclude the
 * row being edited (by guid). Before the fix, editing an existing value in place (e.g. changing only its
 * Meaning) matched the value against itself and was wrongly rejected as a duplicate. The check is pure,
 * so it is bound to a minimal `this` with just the MatTableDataSource (no Angular wiring required).
 */
describe('ContextSchemeCreateComponent.isDuplicateContextSchemeValue (#1744 class)', () => {
  function ctxWith(data: ContextSchemeValue[]): any {
    const ctx: any = {dataSource: {data}};
    ctx.isDuplicateContextSchemeValue = ContextSchemeCreateComponent.prototype.isDuplicateContextSchemeValue;
    return ctx;
  }

  it('does NOT flag an existing value edited in place (same guid, meaning changed)', () => {
    const ctx = ctxWith([value('g1', 'US', 'United States')]);
    expect(ctx.isDuplicateContextSchemeValue(value('g1', 'US', 'USA'))).toBe(false);
  });

  it('flags editing one row to a value already held by a DIFFERENT row', () => {
    const ctx = ctxWith([value('g1', 'US', 'United States'), value('g2', 'CA', 'Canada')]);
    expect(ctx.isDuplicateContextSchemeValue(value('g2', 'US', 'Canada'))).toBe(true);
  });

  it('flags a newly added value (no guid yet) that duplicates an existing row', () => {
    const ctx = ctxWith([value('g1', 'US', 'United States')]);
    expect(ctx.isDuplicateContextSchemeValue(value(undefined, 'US'))).toBe(true);
  });

  it('allows a distinct newly added value', () => {
    const ctx = ctxWith([value('g1', 'US', 'United States')]);
    expect(ctx.isDuplicateContextSchemeValue(value(undefined, 'CA'))).toBe(false);
  });
});
