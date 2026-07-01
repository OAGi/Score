import {vi} from 'vitest';
import {of} from 'rxjs';
import {BusinessTermDetailComponent} from './business-term-detail.component';

/**
 * #1754: the edit form is the client half of the catalog-uniqueness contract. A business term is
 * uniquely identified by the (name + External Reference URI) pair, so update HARD-BLOCKS only a
 * duplicate of that pair; a same-name term with a different URI is a distinct term and is allowed
 * with no override. Discard requires an explicit confirmation. Following this repo's convention the
 * methods are invoked against a hand-built `this` — no Angular TestBed.
 */
describe('BusinessTermDetailComponent (#1754 uniqueness gating + discard)', () => {

  function detailCtx(): any {
    const ctx: any = {
      businessTerm: {businessTermId: 1, businessTerm: 'Ship To', externalReferenceUri: 'http://ref/1'},
      hashCode: 0,
      service: {
        // No checkNameUniqueness — update() must not call it (name alone is not a uniqueness key).
        checkUniqueness: vi.fn().mockReturnValue(of(true)),
        update: vi.fn().mockReturnValue(of({})),
        delete: vi.fn().mockReturnValue(of({})),
      },
      snackBar: {open: vi.fn()},
      router: {navigateByUrl: vi.fn()},
      titleService: {setTitle: vi.fn(), getTitle: vi.fn()},
      confirmDialogService: {
        newConfig: vi.fn().mockImplementation(() => ({data: {}})),
        open: vi.fn().mockReturnValue({afterClosed: () => of(undefined)}),
      },
    };
    const proto = BusinessTermDetailComponent.prototype as any;
    ctx.update = proto.update;
    ctx.checkUniqueness = proto.checkUniqueness;
    ctx.doUpdate = proto.doUpdate;
    ctx.openDialogBusinessTermUpdate = proto.openDialogBusinessTermUpdate;
    ctx.discard = proto.discard;
    return ctx;
  }

  it('should be defined', () => {
    expect(BusinessTermDetailComponent).toBeTruthy();
  });

  it('hard-blocks a duplicate (name + External Reference URI) pair without updating', () => {
    const ctx = detailCtx();
    ctx.service.checkUniqueness.mockReturnValue(of(false));
    ctx.update();
    const cfg = ctx.confirmDialogService.open.mock.calls.at(-1)[0];
    expect(cfg.data.content[0]).toContain('external reference URI');
    expect(ctx.service.update).not.toHaveBeenCalled();
  });

  it('does not treat a name-only match as a duplicate — updates with no override', () => {
    // The service mock deliberately has no checkNameUniqueness; if update() still called a name-only
    // check this would throw. A same-name/different-URI term therefore updates freely.
    const ctx = detailCtx();
    ctx.update();
    expect(ctx.service.checkUniqueness).toHaveBeenCalledTimes(1);
    expect(ctx.service.update).toHaveBeenCalled();
  });

  it('discard removes the term only after the confirmation is accepted; cancel is a no-op', () => {
    const ctx = detailCtx();

    ctx.confirmDialogService.open.mockReturnValue({afterClosed: () => of(false)});
    ctx.discard();
    expect(ctx.service.delete).not.toHaveBeenCalled();

    ctx.confirmDialogService.open.mockReturnValue({afterClosed: () => of(true)});
    ctx.discard();
    expect(ctx.service.delete).toHaveBeenCalledWith(1);
  });
});
