import {vi} from 'vitest';
import {of} from 'rxjs';
import {BiePackageDetailComponent} from './bie-package-detail.component';
import {hashCode} from '../../../common/utility';

describe('BiePackageDetailComponent', () => {
  it('should be defined', () => {
    expect(BiePackageDetailComponent).toBeTruthy();
  });
});

/**
 * Ctrl/Cmd+S must not bypass the Update button's guard. The button is
 * `[disabled]="updateDisabled"`; handleKeyboardEvent() -> update() has to honor the same guard,
 * not just compare the hash, or a package with an emptied required field (e.g. Name) still gets saved.
 *
 * Follows the repo's domain-test style (see oas-doc-detail.component.spec.ts): no Angular TestBed.
 * The component uses inject() in its field initializers, so it cannot be `new`-ed outside an
 * injection context; instead update()/updateDisabled are invoked against a hand-built `this`.
 */
describe('BiePackageDetailComponent.update guards empty required fields (#20)', () => {
  function updateCtx(biePackage: any, hashedBiePackage: any = biePackage): any {
    const svc: any = {update: vi.fn(() => of(biePackage))};
    const ctx: any = {
      biePackage,
      hashCode: hashCode(hashedBiePackage),
      biePackageService: svc,
      snackBar: {open: vi.fn()},
      init: vi.fn(),
    };
    ctx.isChanged = BiePackageDetailComponent.prototype.isChanged;
    ctx.isDisabled = BiePackageDetailComponent.prototype.isDisabled;
    Object.defineProperty(ctx, 'updateDisabled', Object.getOwnPropertyDescriptor(
      BiePackageDetailComponent.prototype, 'updateDisabled')!);
    ctx.update = BiePackageDetailComponent.prototype.update;
    return ctx;
  }

  it('does NOT save when the Name field has been emptied (button would be disabled)', () => {
    const changed = {name: '', versionId: 'v1.0', versionName: 'v1', description: 'desc'};
    const ctx = updateCtx(changed, {name: 'Original', versionId: 'v1.0', versionName: 'v1', description: 'desc'});
    ctx.update();
    expect(ctx.biePackageService.update).not.toHaveBeenCalled();
    expect(ctx.snackBar.open).not.toHaveBeenCalled();
  });

  it('does NOT save when Version ID, Version Name, or Description is emptied', () => {
    const base = {name: 'Name', versionId: '', versionName: 'v1', description: 'desc'};
    const original = {name: 'Name', versionId: 'v1.0', versionName: 'v1', description: 'desc'};
    const ctx = updateCtx(base, original);
    ctx.update();
    expect(ctx.biePackageService.update).not.toHaveBeenCalled();
  });

  it('saves when changed and every required field is non-empty', () => {
    const changed = {name: 'New Name', versionId: 'v1.0', versionName: 'v1', description: 'desc'};
    const ctx = updateCtx(changed, {name: 'Name', versionId: 'v1.0', versionName: 'v1', description: 'desc'});
    ctx.update();
    expect(ctx.biePackageService.update).toHaveBeenCalledWith(changed);
  });

  it('does NOT save when nothing changed, even if required fields happen to be empty', () => {
    const unchanged = {name: '', versionId: '', versionName: '', description: ''};
    const ctx = updateCtx(unchanged);
    ctx.update();
    expect(ctx.biePackageService.update).not.toHaveBeenCalled();
  });
});
