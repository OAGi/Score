import {vi} from 'vitest';
import {OasDocAssignDialogComponent} from './oas-doc-assign-dialog.component';

/**
 * Issue #1610: in the "Add BIE" dialog, switching an operation's Verb reverts a
 * Request body to Response ONLY for GET. A DELETE keeps its Request body (honored in OpenAPI 3.1,
 * dropped — with a banner — in 3.0.3). Previously both GET and DELETE reverted.
 *
 * onChange('verbSelection', key) only reads/writes the verbSelection / messageBodySelection maps, so
 * it is invoked against a hand-built `this` (no Angular TestBed — see bie-flat-tree.spec.ts).
 */

function assignCtx(verb: string, messageBody: string): any {
  const key = 1001; // a topLevelAsbiepId acts as the selection map key
  const ctx: any = {
    key,
    verbSelection: {[key]: verb},
    messageBodySelection: {[key]: messageBody},
  };
  ctx.onChange = OasDocAssignDialogComponent.prototype.onChange;
  return ctx;
}

describe('OasDocAssignDialogComponent', () => {
  it('should be defined', () => {
    expect(OasDocAssignDialogComponent).toBeTruthy();
  });
});

describe('OasDocAssignDialogComponent.onChange verbSelection (#1610)', () => {
  it('reverts a GET request body to Response', () => {
    const ctx = assignCtx('GET', 'Request');
    ctx.onChange('verbSelection', ctx.key);
    expect(ctx.messageBodySelection[ctx.key]).toBe('Response');
  });

  it('KEEPS a Request body for DELETE', () => {
    const ctx = assignCtx('DELETE', 'Request');
    ctx.onChange('verbSelection', ctx.key);
    expect(ctx.messageBodySelection[ctx.key]).toBe('Request');
  });

  it('leaves a POST request body untouched', () => {
    const ctx = assignCtx('POST', 'Request');
    ctx.onChange('verbSelection', ctx.key);
    expect(ctx.messageBodySelection[ctx.key]).toBe('Request');
  });

  it('does not force a Response body onto a GET (nothing to revert)', () => {
    const ctx = assignCtx('GET', 'Response');
    ctx.onChange('verbSelection', ctx.key);
    expect(ctx.messageBodySelection[ctx.key]).toBe('Response');
  });
});

describe('OasDocAssignDialogComponent.doAddBieForOasDoc payload mapping (#1610)', () => {
  function addCtx(verb: string, messageBody: string): any {
    const key = 2002; // topLevelAsbiepId
    const ctx: any = {
      key,
      assignBieForOasDoc: {},
      verbSelection: {[key]: verb},
      messageBodySelection: {[key]: messageBody},
      oasDoc: {oasDocId: 77},
      openAPIService: {assignBieForOasDoc: vi.fn().mockReturnValue({subscribe: vi.fn()})},
      snackBar: {open: vi.fn()},
      dialogRef: {close: vi.fn()},
    };
    ctx.doAddBieForOasDoc = OasDocAssignDialogComponent.prototype.doAddBieForOasDoc;
    return ctx;
  }

  function bie(key: number) {
    return {topLevelAsbiepId: key, propertyTerm: 'Purchase Order', arrayIndicator: false, suppressRootIndicator: false};
  }

  it('maps a DELETE + Request selection to oasRequest=true and a delete operationId', () => {
    const ctx = addCtx('DELETE', 'Request');
    ctx.doAddBieForOasDoc(bie(ctx.key));

    expect(ctx.assignBieForOasDoc.verb).toBe('DELETE');
    expect(ctx.assignBieForOasDoc.messageBody).toBe('Request');
    expect(ctx.assignBieForOasDoc.oasRequest).toBe(true);
    expect(ctx.assignBieForOasDoc.operationId).toBe('deletePurchaseOrder');
    expect(ctx.assignBieForOasDoc.oasDocId).toBe(77);
    expect(ctx.openAPIService.assignBieForOasDoc).toHaveBeenCalledWith(ctx.assignBieForOasDoc);
  });

  it('maps a DELETE + Response selection to oasRequest=false', () => {
    const ctx = addCtx('DELETE', 'Response');
    ctx.doAddBieForOasDoc(bie(ctx.key));

    expect(ctx.assignBieForOasDoc.oasRequest).toBe(false);
  });
});
