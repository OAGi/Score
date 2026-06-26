import {vi} from 'vitest';
import {of, throwError} from 'rxjs';
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
    // doAddBieForOasDoc now delegates payload construction to buildAssignPayload (#1492 refactor).
    ctx.buildAssignPayload = (OasDocAssignDialogComponent.prototype as any).buildAssignPayload;
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

describe('OasDocAssignDialogComponent body-slot pre-check (#1492 Option 2)', () => {
  function bie(key: number, propertyTerm: string, arrayIndicator = false): any {
    return {topLevelAsbiepId: key, propertyTerm, arrayIndicator, suppressRootIndicator: false};
  }

  // A `this` context exposing the pre-check helpers over a fixed set of existing rows + selections.
  function checkCtx(existingRows: any[], selected: any[], verbs: any, bodies: any): any {
    const ctx: any = {
      existingRows,
      verbSelection: verbs,
      messageBodySelection: bodies,
      selection: {selected},
    };
    ctx.bodySlotKeyForAdd = (OasDocAssignDialogComponent.prototype as any).bodySlotKeyForAdd;
    ctx.isDuplicateBodySlotForAdd = OasDocAssignDialogComponent.prototype.isDuplicateBodySlotForAdd;
    ctx.hasDuplicateBodySlotSelected = OasDocAssignDialogComponent.prototype.hasDuplicateBodySlotSelected;
    return ctx;
  }

  it('flags a selection whose (propertyTerm, verb, messageBody) already exists on the document', () => {
    const existing = [{propertyTerm: 'Purchase Order', resourceName: 'purchase-order', verb: 'POST', messageBody: 'Request'}];
    const sel = bie(1, 'Purchase Order');
    const ctx = checkCtx(existing, [sel], {1: 'POST'}, {1: 'Request'});
    expect(ctx.isDuplicateBodySlotForAdd(sel)).toBe(true);
    expect(ctx.hasDuplicateBodySlotSelected()).toBe(true);
  });

  it('does NOT flag a Response when only the Request exists (one op, two bodies is legal)', () => {
    const existing = [{propertyTerm: 'Purchase Order', resourceName: 'purchase-order', verb: 'POST', messageBody: 'Request'}];
    const sel = bie(1, 'Purchase Order');
    const ctx = checkCtx(existing, [sel], {1: 'POST'}, {1: 'Response'});
    expect(ctx.isDuplicateBodySlotForAdd(sel)).toBe(false);
    expect(ctx.hasDuplicateBodySlotSelected()).toBe(false);
  });

  it('does not flag a different verb on the same property term', () => {
    const existing = [{propertyTerm: 'Purchase Order', resourceName: 'purchase-order', verb: 'POST', messageBody: 'Request'}];
    const sel = bie(1, 'Purchase Order');
    const ctx = checkCtx(existing, [sel], {1: 'PUT'}, {1: 'Request'});
    expect(ctx.isDuplicateBodySlotForAdd(sel)).toBe(false);
  });
});

describe('OasDocAssignDialogComponent.addBieForOasDoc serialized adds (#1492 Option 2)', () => {
  function bie(key: number, propertyTerm: string): any {
    return {topLevelAsbiepId: key, propertyTerm, arrayIndicator: false, suppressRootIndicator: false};
  }

  // Builds a context whose openAPIService records the ORDER of assign POSTs, so we can assert the adds
  // were serialized (one completes before the next starts) and the dialog closed exactly once at the end.
  function batchCtx(selected: any[], verbs: any, bodies: any, existingRows: any[] = []): any {
    const order: string[] = [];
    const ctx: any = {
      existingRows,
      verbSelection: verbs,
      messageBodySelection: bodies,
      selection: {selected},
      oasDoc: {oasDocId: 7},
      assignBieForOasDoc: {},
      snackBar: {open: vi.fn()},
      dialogRef: {close: vi.fn()},
      loading: false,
      _order: order,
      openAPIService: {
        checkBIEReusedAcrossMultipleOperations: vi.fn().mockReturnValue(of({errorMessages: []})),
        assignBieForOasDoc: vi.fn().mockImplementation((payload: any) => {
          order.push('assign:' + payload.propertyTerm);
          return of({});
        }),
      },
    };
    ctx.bodySlotKeyForAdd = (OasDocAssignDialogComponent.prototype as any).bodySlotKeyForAdd;
    ctx.buildAssignPayload = (OasDocAssignDialogComponent.prototype as any).buildAssignPayload;
    ctx.addBieForOasDoc = OasDocAssignDialogComponent.prototype.addBieForOasDoc;
    return ctx;
  }

  it('POSTs each selected BIE and closes the dialog once after ALL complete', () => {
    const a = bie(1, 'Alpha');
    const b = bie(2, 'Beta');
    const ctx = batchCtx([a, b], {1: 'POST', 2: 'PUT'}, {1: 'Request', 2: 'Request'});
    ctx.addBieForOasDoc();

    // With synchronous `of(...)` sources, concatMap runs them in order to completion before finalize.
    expect(ctx.openAPIService.assignBieForOasDoc).toHaveBeenCalledTimes(2);
    expect(ctx._order).toEqual(['assign:Alpha', 'assign:Beta']);
    expect(ctx.dialogRef.close).toHaveBeenCalledTimes(1);
    expect(ctx.snackBar.open).toHaveBeenCalledWith('Added', '', {duration: 3000});
    expect(ctx.loading).toBe(false);
  });

  it('blocks the whole batch (no POST) when a selection duplicates an existing body slot', () => {
    const existing = [{propertyTerm: 'Alpha', resourceName: 'alpha', verb: 'POST', messageBody: 'Request'}];
    const a = bie(1, 'Alpha');
    const ctx = batchCtx([a], {1: 'POST'}, {1: 'Request'}, existing);
    ctx.addBieForOasDoc();

    expect(ctx.openAPIService.assignBieForOasDoc).not.toHaveBeenCalled();
    expect(ctx.dialogRef.close).not.toHaveBeenCalled();
    expect(ctx.snackBar.open).toHaveBeenCalledWith('This endpoint already has a Request body.', '', {duration: 5000});
  });

  it('blocks the batch when two selections collide on the same (propertyTerm, verb, messageBody)', () => {
    const a = bie(1, 'Alpha');
    const a2 = bie(2, 'Alpha');
    const ctx = batchCtx([a, a2], {1: 'POST', 2: 'POST'}, {1: 'Request', 2: 'Request'});
    ctx.addBieForOasDoc();

    expect(ctx.openAPIService.assignBieForOasDoc).not.toHaveBeenCalled();
    expect(ctx.snackBar.open).toHaveBeenCalledWith('This endpoint already has a Request body.', '', {duration: 5000});
  });

  it('skips a BIE flagged by checkBIEReusedAcrossMultipleOperations but still adds the others', () => {
    const a = bie(1, 'Alpha');
    const b = bie(2, 'Beta');
    const ctx = batchCtx([a, b], {1: 'POST', 2: 'PUT'}, {1: 'Request', 2: 'Request'});
    ctx.openAPIService.checkBIEReusedAcrossMultipleOperations = vi.fn().mockImplementation((bieArg: any) =>
      bieArg.propertyTerm === 'Alpha' ? of({errorMessages: ['reused!']}) : of({errorMessages: []}));
    ctx.addBieForOasDoc();

    expect(ctx.openAPIService.assignBieForOasDoc).toHaveBeenCalledTimes(1);
    expect(ctx._order).toEqual(['assign:Beta']);
    expect(ctx.snackBar.open).toHaveBeenCalledWith('reused!', '', {duration: 5000});
    expect(ctx.dialogRef.close).toHaveBeenCalledTimes(1);
  });

  it('continues past a backend assign error (e.g. 400 dup) and still closes the dialog', () => {
    const a = bie(1, 'Alpha');
    const b = bie(2, 'Beta');
    const ctx = batchCtx([a, b], {1: 'POST', 2: 'PUT'}, {1: 'Request', 2: 'Request'});
    ctx.openAPIService.assignBieForOasDoc = vi.fn().mockImplementation((payload: any) => {
      ctx._order.push('assign:' + payload.propertyTerm);
      return payload.propertyTerm === 'Alpha' ? throwError(() => ({status: 400})) : of({});
    });
    ctx.addBieForOasDoc();

    // Alpha errors (swallowed by catchError), Beta still runs; dialog closes once.
    expect(ctx._order).toEqual(['assign:Alpha', 'assign:Beta']);
    expect(ctx.dialogRef.close).toHaveBeenCalledTimes(1);
    // Only Beta succeeded, so "Added" is shown (addedCount > 0).
    expect(ctx.snackBar.open).toHaveBeenCalledWith('Added', '', {duration: 3000});
  });
});
