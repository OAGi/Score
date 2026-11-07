import {OasDocAddOperationDialogComponent} from './oas-doc-add-operation-dialog.component';

/**
 * The Add Operation dialog holds no rule about the path or the Operation ID: Add is there when the server says that the
 * operation can be added, and what it says is wrong is shown. The getters only read `operation` and `proposal`, so they are
 * invoked against a hand-built `this` (no Angular TestBed).
 */
function ctxOf(proposal: any, operation: any = {verb: 'DELETE', resourceName: '/orders/{id}', operationId: 'deleteOrder'}): any {
  const proto = OasDocAddOperationDialogComponent.prototype;
  const ctx: any = {operation, proposal};
  for (const accessor of ['resourceNameError', 'problems', 'isValid']) {
    Object.defineProperty(ctx, accessor, Object.getOwnPropertyDescriptor(proto, accessor)!);
  }
  return ctx;
}

const violation = (field: string, message: string) => ({scope: 'OPERATION', field, message});

describe('OasDocAddOperationDialogComponent.isValid', () => {
  it('is there when the server found nothing wrong', () => {
    expect(ctxOf({resourceName: '/orders/{id}', operationId: 'deleteOrder', violations: []}).isValid).toBe(true);
  });

  it('is not there until the server has answered', () => {
    expect(ctxOf(undefined).isValid).toBe(false);
  });

  it('is not there for a Resource Name that the server says is not a path, and the dialog says why', () => {
    const ctx = ctxOf({violations: [violation('resourceName', 'Path must start with "/".')]}, {verb: 'DELETE', resourceName: 'orders', operationId: 'deleteOrders'});

    expect(ctx.isValid).toBe(false);
    expect(ctx.resourceNameError).toBe('Path must start with "/".');
    expect(ctx.problems).toEqual([]);
  });

  it('shows what else the server says is wrong, apart from the Resource Name', () => {
    const ctx = ctxOf({violations: [violation('operationId', 'The operation ID is used.')]});

    expect(ctx.isValid).toBe(false);
    expect(ctx.problems).toEqual(['The operation ID is used.']);
    expect(ctx.resourceNameError).toBe('');
  });

  it('is not there without a Resource Name or an Operation ID', () => {
    const proposal = {violations: []};
    expect(ctxOf(proposal, {verb: 'DELETE', resourceName: '', operationId: 'x'}).isValid).toBe(false);
    expect(ctxOf(proposal, {verb: 'DELETE', resourceName: '/x', operationId: ''}).isValid).toBe(false);
  });
});

describe('OasDocAddOperationDialogComponent.propose', () => {
  function ctxOf(operationIdTouched: boolean): any {
    const ctx: any = {
      operation: {verb: 'DELETE', resourceName: '/item/{id}', operationId: 'deleteProductionOrder'},
      operationIdTouched, proposal: {violations: []}, proposalRequests: {next: () => undefined}
    };
    ctx.propose = OasDocAddOperationDialogComponent.prototype.propose;
    return ctx;
  }

  it('does not keep the Operation ID that the server made for what the user had before, until it answers', () => {
    const ctx = ctxOf(false);

    ctx.propose();

    expect(ctx.operation.operationId).toBe('');
    expect(ctx.proposal).toBeUndefined();
  });

  it('keeps the Operation ID that the user gave', () => {
    const ctx = ctxOf(true);

    ctx.propose();

    expect(ctx.operation.operationId).toBe('deleteProductionOrder');
  });
});
