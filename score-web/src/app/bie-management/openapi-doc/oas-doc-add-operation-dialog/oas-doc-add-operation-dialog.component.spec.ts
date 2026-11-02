import {OasDocAddOperationDialogComponent} from './oas-doc-add-operation-dialog.component';

/**
 * The Add Operation dialog must not enable Add for a Resource Name (Path) that would
 * produce an invalid OpenAPI path. The getters only read `operation`, so they are invoked against a
 * hand-built `this` (no Angular TestBed).
 */
function isValidFor(resourceName: string): boolean {
  const proto = OasDocAddOperationDialogComponent.prototype;
  const ctx: any = {operation: {verb: 'DELETE', resourceName, operationId: 'deleteX'}};
  Object.defineProperty(ctx, 'resourceNameError',
    {get: Object.getOwnPropertyDescriptor(proto, 'resourceNameError')!.get});
  return Object.getOwnPropertyDescriptor(proto, 'isValid')!.get!.call(ctx);
}

describe('OasDocAddOperationDialogComponent.isValid', () => {
  it('accepts a well-formed path', () => {
    expect(isValidFor('/orders/{id}')).toBe(true);
  });

  it('rejects a path without a leading slash, with spaces, or with a query/fragment', () => {
    expect(isValidFor('no leading slash  here')).toBe(false);
    expect(isValidFor('orders?x=1 #frag')).toBe(false);
    expect(isValidFor('/orders?x=1')).toBe(false);
  });
});
