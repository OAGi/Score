import {vi} from 'vitest';
import {of} from 'rxjs';
import {BusinessTermCreateComponent} from './business-term-create.component';

/**
 * #1754: the create form is the client half of the catalog-uniqueness contract (the server now
 * enforces the same rule). A business term is uniquely identified by the (name + External Reference
 * URI) pair, so create HARD-BLOCKS only a duplicate of that pair; a same-name term with a different
 * URI is a distinct term and is allowed. Following this repo's convention (see
 * oas-doc-assign-dialog.spec.ts) the methods are invoked against a hand-built `this` — no Angular
 * TestBed — so the inject()-based component never has to be constructed.
 */
describe('BusinessTermCreateComponent (#1754 uniqueness gating)', () => {

  function createCtx(): any {
    const ctx: any = {
      businessTerm: {businessTerm: 'Ship To', externalReferenceUri: 'http://ref/1'},
      service: {
        // No checkNameUniqueness — create() must not call it (name alone is not a uniqueness key).
        checkUniqueness: vi.fn().mockReturnValue(of(true)),
        create: vi.fn().mockReturnValue(of({})),
      },
      snackBar: {open: vi.fn()},
      router: {navigateByUrl: vi.fn()},
      confirmDialogService: {
        newConfig: vi.fn().mockImplementation(() => ({data: {}})),
        open: vi.fn().mockReturnValue({afterClosed: () => of(undefined)}),
      },
    };
    const proto = BusinessTermCreateComponent.prototype as any;
    ctx.create = proto.create;
    ctx.checkUniqueness = proto.checkUniqueness;
    ctx.doCreate = proto.doCreate;
    ctx.openDialogbusinessTermCreate = proto.openDialogbusinessTermCreate;
    return ctx;
  }

  it('should be defined', () => {
    expect(BusinessTermCreateComponent).toBeTruthy();
  });

  it('creates when the (name + External Reference URI) pair is unique', () => {
    const ctx = createCtx();
    ctx.create();
    expect(ctx.service.create).toHaveBeenCalled();
    expect(ctx.router.navigateByUrl).toHaveBeenCalledWith('/business_term_management/business_term');
  });

  it('hard-blocks a duplicate (name + External Reference URI) pair without creating', () => {
    const ctx = createCtx();
    ctx.service.checkUniqueness.mockReturnValue(of(false));
    ctx.create();
    const cfg = ctx.confirmDialogService.open.mock.calls.at(-1)[0];
    expect(cfg.data.content[0]).toContain('external reference URI');
    expect(ctx.service.create).not.toHaveBeenCalled();
  });

  it('does not treat a name-only match as a duplicate — the composite pair is the only gate', () => {
    // The service mock deliberately has no checkNameUniqueness; if create() still called a name-only
    // check this would throw. A same-name/different-URI term therefore creates freely.
    const ctx = createCtx();
    ctx.create();
    expect(ctx.service.checkUniqueness).toHaveBeenCalledTimes(1);
    expect(ctx.service.create).toHaveBeenCalled();
  });
});
