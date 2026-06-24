import {OasDocCreateComponent} from './oas-doc-create.component';

/**
 * Issue #1610: a newly created OpenAPI document defaults to OpenAPI 3.1.1
 * (matching the BIE Expression default), where a request body on DELETE is honored.
 *
 * ngOnInit() touches no injected service, so it is invoked against a bare `this` context (the repo's
 * no-TestBed domain-test style — see bie-flat-tree.spec.ts).
 */

describe('OasDocCreateComponent', () => {
  it('should be defined', () => {
    expect(OasDocCreateComponent).toBeTruthy();
  });

  it('defaults a new document to OpenAPI 3.1.1 (#1610)', () => {
    const ctx: any = {};
    OasDocCreateComponent.prototype.ngOnInit.call(ctx);
    expect(ctx.disabled).toBe(false);
    expect(ctx.oasDoc).toBeTruthy();
    expect(ctx.oasDoc.openAPIVersion).toBe('3.1.1');
  });
});
