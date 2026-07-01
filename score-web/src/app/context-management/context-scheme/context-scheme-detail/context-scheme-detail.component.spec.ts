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
