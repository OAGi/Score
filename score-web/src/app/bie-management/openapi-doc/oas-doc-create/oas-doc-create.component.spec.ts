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
