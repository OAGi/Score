import {vi} from 'vitest';
import {of} from 'rxjs';
import {OasDocCreateComponent} from './oas-doc-create.component';

/**
 * Issue #1610 / #1760: a newly created OpenAPI document defaults to the OpenAPI 3.1 family
 * (matching the BIE Expression default), where a request body on DELETE is honored. The UI stores
 * only the minor version; the backend pins the canonical patch (3.1.2) on generate.
 *
 * ngOnInit() touches no injected service, so it is invoked against a bare `this` context (the repo's
 * no-TestBed domain-test style — see bie-flat-tree.spec.ts).
 */

describe('OasDocCreateComponent', () => {
  it('should be defined', () => {
    expect(OasDocCreateComponent).toBeTruthy();
  });

  it('defaults a new document to the OpenAPI 3.1 family (#1610 / #1760)', () => {
    const ctx: any = {};
    OasDocCreateComponent.prototype.ngOnInit.call(ctx);
    expect(ctx.disabled).toBe(false);
    expect(ctx.oasDoc).toBeTruthy();
    expect(ctx.oasDoc.openAPIVersion).toBe('3.1');
  });
});

/**
 * Create and Update run the same uniqueness check (POST check_uniqueness on Title + OpenAPI Version +
 * Doc Version; the License Name is not part of the key): a duplicate opens the "Invalid parameters"
 * dialog and saves nothing, a unique document proceeds to the save.
 */
describe('OasDocCreateComponent.create duplicate check', () => {
  function ctx(unique: boolean): any {
    const c: any = {
      oasDoc: {title: 'dup title', openAPIVersion: '3.1', version: '1.0', licenseName: 'MIT'},
      openAPIService: {checkUniqueness: vi.fn(() => of(unique))},
      doCreate: vi.fn(),
      openDialogOasDocCreate: vi.fn(),
    };
    c.checkUniqueness = OasDocCreateComponent.prototype.checkUniqueness;
    return c;
  }

  it('opens the duplicate dialog and does not create when the check fails', () => {
    const c = ctx(false);
    OasDocCreateComponent.prototype.create.call(c);
    expect(c.openAPIService.checkUniqueness).toHaveBeenCalledWith(c.oasDoc);
    expect(c.openDialogOasDocCreate).toHaveBeenCalledTimes(1);
    expect(c.doCreate).not.toHaveBeenCalled();
  });

  it('creates when the check passes', () => {
    const c = ctx(true);
    OasDocCreateComponent.prototype.create.call(c);
    expect(c.doCreate).toHaveBeenCalledTimes(1);
    expect(c.openDialogOasDocCreate).not.toHaveBeenCalled();
  });

  it('opens a dialog that does not mention the License Name', () => {
    const dialogConfig: any = {data: {}};
    const open = vi.fn(() => ({afterClosed: () => of(undefined)}));
    const c: any = {confirmDialogService: {newConfig: () => dialogConfig, open}};
    OasDocCreateComponent.prototype.openDialogOasDocCreate.call(c);
    expect(open).toHaveBeenCalledWith(dialogConfig);
    expect(dialogConfig.data.content[0]).not.toMatch(/license/i);
  });
});
