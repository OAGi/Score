import {vi} from 'vitest';
import {of} from 'rxjs';
import {ContextSchemeDetailComponent} from './context-scheme-detail.component';

describe('ContextSchemeDetailComponent', () => {
  it('should be defined', () => {
    expect(ContextSchemeDetailComponent).toBeTruthy();
  });

  it('passes the current context scheme id to the uniqueness check when updating', () => {
    const callback = vi.fn();
    const service = {
      checkUniqueness: vi.fn().mockReturnValue(of(false))
    };
    const ctx: any = {
      service,
      contextSchemeUpdateRequest: {
        contextSchemeId: 42,
        schemeId: 'scheme-id',
        schemeAgencyId: 'agency-id',
        schemeVersionId: '1.0'
      }
    };
    ctx.checkUniqueness = ContextSchemeDetailComponent.prototype.checkUniqueness;

    ctx.checkUniqueness(callback);

    expect(service.checkUniqueness).toHaveBeenCalledWith('scheme-id', 'agency-id', '1.0', 42);
    expect(callback).toHaveBeenCalled();
  });
});

/**
 * Whitespace-only Scheme ID, Agency ID and Version must keep the Update button disabled.
 */
describe('ContextSchemeDetailComponent.isDisabled', () => {
  function disabledFor(overrides: any): boolean {
    const ctx: any = {disabled: false, isUpdating: false};
    const scheme: any = {
      contextCategoryId: 1, schemeName: 'Name', schemeId: 'GEO', schemeAgencyId: 'OAGi', schemeVersionId: '1.0',
      ...overrides
    };
    return ContextSchemeDetailComponent.prototype.isDisabled.call(ctx, scheme);
  }

  it('is enabled when every required field has text', () => {
    expect(disabledFor({})).toBe(false);
  });

  it('is disabled when Scheme ID, Agency ID or Version is whitespace-only', () => {
    expect(disabledFor({schemeId: '   '})).toBe(true);
    expect(disabledFor({schemeAgencyId: '   '})).toBe(true);
    expect(disabledFor({schemeVersionId: '   '})).toBe(true);
  });
});
