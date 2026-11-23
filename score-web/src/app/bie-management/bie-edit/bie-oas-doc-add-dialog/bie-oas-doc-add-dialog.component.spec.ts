import {BieOasDocAddDialogComponent} from './bie-oas-doc-add-dialog.component';

/**
 * The BIE-root "Add to OpenAPI Document" dialog holds no rule about the operation it adds: what the Resource Name and the
 * Operation ID are, and whether the endpoint can take it, are the server's, and Add is there when the server says it can.
 * The getters only read `proposal`, so they are invoked against a hand-built `this` (no Angular TestBed).
 */
function dialogCtx(proposal: any, selected = true): any {
  const ctx: any = {proposal, selectedOasDoc: selected ? {oasDocId: 5} : undefined, verb: 'GET', messageBody: 'Response'};
  for (const accessor of ['operationId', 'resourcePath', 'problems', 'isValid']) {
    Object.defineProperty(ctx, accessor, Object.getOwnPropertyDescriptor(BieOasDocAddDialogComponent.prototype, accessor)!);
  }
  return ctx;
}

const violation = (message: string) => ({scope: 'OPERATION', field: 'messageBody', message});

describe('BieOasDocAddDialogComponent shows what the server makes of the operation', () => {
  it('shows the Resource Name and the Operation ID that the server answered', () => {
    const ctx = dialogCtx({resourceName: '/bc/1.0/order', operationId: 'queryOrder', violations: []});

    expect(ctx.resourcePath).toBe('/bc/1.0/order');
    expect(ctx.operationId).toBe('queryOrder');
  });

  it('shows nothing until the server has answered, and Add is not there', () => {
    const ctx = dialogCtx(undefined);

    expect(ctx.resourcePath).toBe('');
    expect(ctx.operationId).toBe('');
    expect(ctx.isValid).toBe(false);
  });

  it('is valid when the server found nothing wrong', () => {
    expect(dialogCtx({resourceName: '/a', operationId: 'queryA', violations: []}).isValid).toBe(true);
  });

  it('is not valid, and says why, when the server found something wrong', () => {
    const ctx = dialogCtx({resourceName: '/a', operationId: 'queryA', violations: [violation('This operation already has a Response body.')]});

    expect(ctx.isValid).toBe(false);
    expect(ctx.problems).toEqual(['This operation already has a Response body.']);
  });

  it('is not valid without a document', () => {
    expect(dialogCtx({resourceName: '/a', operationId: 'queryA', violations: []}, false).isValid).toBe(false);
  });
});

describe('BieOasDocAddDialogComponent.candidate', () => {
  it('says the BIE, the verb, the message body and the array indicator that are chosen, and nothing else', () => {
    const component: any = Object.create(BieOasDocAddDialogComponent.prototype);
    component.data = {topLevelAsbiepId: 9, propertyTerm: 'Order'};
    component.verb = 'POST';
    component.messageBody = 'Request';
    component.arrayIndicator = true;

    expect(component.candidate()).toEqual({
      topLevelAsbiepId: 9, propertyTerm: 'Order', verb: 'POST', messageBody: 'Request', arrayIndicator: true
    });
  });
});
