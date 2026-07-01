import {vi} from 'vitest';
import {Subject} from 'rxjs';
import {BieEditComponent} from './bie-edit.component';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';

describe('BieEditComponent', () => {
  it('should be defined', () => {
    expect(BieEditComponent).toBeTruthy();
  });
});

/**
 * Issue #1610 (parity with the OpenAPI Document editor's #1610 banner): the BIE-root "OpenAPI Document
 * Information" panel warns, per binding card, that a DELETE Request body is ignored when that binding's
 * owning OpenAPI Document targets OpenAPI 3.0.x. The check is pure, so bind the prototype method to a
 * minimal `this` and a plain binding bag (no Angular wiring required). The warning is read-only on the
 * BIE screen — the OpenAPI Version itself is changed on the OpenAPI Document screen.
 */
function deleteBodyIgnored(verb: string, messageBody: string, openAPIVersion: any): boolean {
  const ctx: any = {isOasBindingDeleteBodyIgnored: BieEditComponent.prototype.isOasBindingDeleteBodyIgnored};
  return ctx.isOasBindingDeleteBodyIgnored({verb, messageBody, openAPIVersion});
}

describe('BieEditComponent.isOasBindingDeleteBodyIgnored (#1610 per-card warning)', () => {
  it('is true for a DELETE + Request binding whose document targets 3.0.3', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.0.3')).toBe(true);
  });

  it('is false for the SAME binding when the document targets 3.1.1 (the body is honored)', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.1.1')).toBe(false);
  });

  it('treats any 3.1.x version (e.g. 3.1.0, padded) as honored', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.1.0')).toBe(false);
    expect(deleteBodyIgnored('DELETE', 'Request', '  3.1.1 ')).toBe(false);
  });

  it('is false for a DELETE + Response binding on 3.0.3 (only a Request body is dropped)', () => {
    expect(deleteBodyIgnored('DELETE', 'Response', '3.0.3')).toBe(false);
  });

  it('is false for a non-DELETE Request binding on 3.0.3 (e.g. POST)', () => {
    expect(deleteBodyIgnored('POST', 'Request', '3.0.3')).toBe(false);
  });

  it('warns when the version is missing/blank (not a 3.1 prefix)', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '')).toBe(true);
    expect(deleteBodyIgnored('DELETE', 'Request', undefined)).toBe(true);
  });
});

function businessContext(businessContextId: number, name: string): BusinessContext {
  return {businessContextId, name, guid: 'bc-' + businessContextId} as BusinessContext;
}

describe('BieEditComponent business context assignment', () => {
  function businessContextCtx(overrides: any = {}): any {
    const ctx: any = {
      topLevelAsbiepId: 1001,
      businessContexts: [businessContext(1, 'Shared Context')],
      allBusinessContexts: [
        businessContext(1, 'Shared Context'),
        businessContext(2, 'Shared Context'),
        businessContext(3, 'Trading Partner')
      ],
      businessContextUpdating: false,
      businessContextCtrl: {setValue: vi.fn()},
      businessContextInput: {nativeElement: {value: 'Shared Context'}},
      bizCtxService: {
        assign: vi.fn(),
        unassign: vi.fn()
      },
      snackBar: {open: vi.fn()},
      ...overrides
    };
    ctx._filter = BieEditComponent.prototype._filter;
    ctx.addBusinessContext = BieEditComponent.prototype.addBusinessContext;
    ctx.removeBusinessContext = BieEditComponent.prototype.removeBusinessContext;
    return ctx;
  }

  it('filters already assigned business contexts by id, not by name', () => {
    const ctx = businessContextCtx();

    expect(ctx._filter().map((e: BusinessContext) => e.businessContextId)).toEqual([2, 3]);
    expect(ctx._filter('Shared').map((e: BusinessContext) => e.businessContextId)).toEqual([2]);
  });

  it('sets the updating flag while an assignment request is in flight', () => {
    const selected = businessContext(2, 'Shared Context');
    const assignment$ = new Subject<void>();
    const ctx = businessContextCtx();
    ctx.bizCtxService.assign.mockReturnValue(assignment$);

    ctx.addBusinessContext({option: {value: selected}});

    expect(ctx.bizCtxService.assign).toHaveBeenCalledWith(1001, selected);
    expect(ctx.businessContextUpdating).toBe(true);
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);

    assignment$.next();

    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1, 2]);
    expect(ctx.businessContextUpdating).toBe(false);
    expect(ctx.businessContextInput.nativeElement.value).toBe('');
    expect(ctx.businessContextCtrl.setValue).toHaveBeenCalledWith(null);
    expect(ctx.snackBar.open).toHaveBeenCalledWith('Updated', '', {duration: 3000});
  });

  it('ignores duplicate assignment requests for the same business context id', () => {
    const ctx = businessContextCtx();

    ctx.addBusinessContext({option: {value: businessContext(1, 'Shared Context')}});

    expect(ctx.bizCtxService.assign).not.toHaveBeenCalled();
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);
  });

  it('does not unassign the last remaining business context', () => {
    const ctx = businessContextCtx();

    ctx.removeBusinessContext(ctx.businessContexts[0]);

    expect(ctx.bizCtxService.unassign).not.toHaveBeenCalled();
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);
    expect(ctx.businessContextUpdating).toBe(false);
  });
});
