import {vi} from 'vitest';
import {of, throwError} from 'rxjs';
import {OasDocAssignDialogComponent} from './oas-doc-assign-dialog.component';

/**
 * The "Add BIE" dialog holds no rule about the operations it adds: the server says, for each selected BIE, what the Resource Name
 * and the Operation ID are and whether the endpoint can take it, and Add waits for those answers. The helpers only read the
 * answers (`proposals`) and the selection, so they are invoked against a hand-built `this` (no Angular TestBed).
 */

describe('OasDocAssignDialogComponent', () => {
  it('should be defined', () => {
    expect(OasDocAssignDialogComponent).toBeTruthy();
  });
});

describe('OasDocAssignDialogComponent.onChange', () => {
  it('asks the server again when the verb of a row is chosen, and leaves the rows as they are', () => {
    const ctx: any = {propose: vi.fn(), verbSelection: {1: 'GET'}, messageBodySelection: {1: 'Request'}};
    ctx.onChange = OasDocAssignDialogComponent.prototype.onChange;

    ctx.onChange('verbSelection', 1);

    expect(ctx.propose).toHaveBeenCalledTimes(1);
    expect(ctx.messageBodySelection[1]).toBe('Request'); // a GET with a request body is the server's to refuse
  });

  it('does not ask for another property', () => {
    const ctx: any = {propose: vi.fn()};
    ctx.onChange = OasDocAssignDialogComponent.prototype.onChange;

    ctx.onChange('businessContext', 1);

    expect(ctx.propose).not.toHaveBeenCalled();
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

  it('maps a DELETE + Request selection to oasRequest=true, and leaves the Operation ID to the server', () => {
    const ctx = addCtx('DELETE', 'Request');
    ctx.doAddBieForOasDoc(bie(ctx.key));

    expect(ctx.assignBieForOasDoc.verb).toBe('DELETE');
    expect(ctx.assignBieForOasDoc.messageBody).toBe('Request');
    expect(ctx.assignBieForOasDoc.oasRequest).toBe(true);
    expect(ctx.assignBieForOasDoc.operationId).toBeFalsy();
    expect(ctx.assignBieForOasDoc.oasDocId).toBe(77);
    expect(ctx.openAPIService.assignBieForOasDoc).toHaveBeenCalledWith(ctx.assignBieForOasDoc);
  });

  it('maps a DELETE + Response selection to oasRequest=false', () => {
    const ctx = addCtx('DELETE', 'Response');
    ctx.doAddBieForOasDoc(bie(ctx.key));

    expect(ctx.assignBieForOasDoc.oasRequest).toBe(false);
  });
});

describe('OasDocAssignDialogComponent shows what the server says of the selection', () => {
  const bie = (key: number): any => ({topLevelAsbiepId: key});
  const ok = {resourceName: '/a', operationId: 'queryA', violations: []};
  const refused = (message: string) => ({resourceName: '/a', operationId: 'queryA', violations: [{scope: 'OPERATION', message}]});

  function ctxOf(selected: any[], proposals: any): any {
    const ctx: any = {proposals, selection: {selected}};
    ctx.problemsOf = OasDocAssignDialogComponent.prototype.problemsOf;
    ctx.isAddRefused = OasDocAssignDialogComponent.prototype.isAddRefused;
    return ctx;
  }

  it('says why a BIE cannot be added', () => {
    const ctx = ctxOf([bie(1)], {1: refused('This operation already has a Request body.')});

    expect(ctx.problemsOf(bie(1))).toEqual(['This operation already has a Request body.']);
  });

  it('says nothing of a BIE the server has not answered about yet', () => {
    expect(ctxOf([bie(1)], {}).problemsOf(bie(1))).toEqual([]);
  });

  it('refuses Add while a selected BIE is refused or not answered about yet', () => {
    expect(ctxOf([bie(1), bie(2)], {1: ok, 2: refused('no')}).isAddRefused()).toBe(true);
    expect(ctxOf([bie(1), bie(2)], {1: ok}).isAddRefused()).toBe(true);
  });

  it('does not refuse Add when the server found nothing wrong with any selected BIE', () => {
    expect(ctxOf([bie(1), bie(2)], {1: ok, 2: ok}).isAddRefused()).toBe(false);
    expect(ctxOf([], {}).isAddRefused()).toBe(false);
  });
});

describe('OasDocAssignDialogComponent.addBieForOasDoc serialized adds (#1492 Option 2)', () => {
  function bie(key: number, propertyTerm: string): any {
    return {topLevelAsbiepId: key, propertyTerm, arrayIndicator: false, suppressRootIndicator: false};
  }

  // Builds a context whose openAPIService records the ORDER of assign POSTs, so we can assert the adds
  // were serialized (one completes before the next starts) and the dialog closed exactly once at the end.
  function batchCtx(selected: any[], verbs: any, bodies: any): any {
    const order: string[] = [];
    const ctx: any = {
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

  it('ADDS both selections that share a property term but are distinct BIEs (different Business Contexts)', () => {
    // Issue #1492 multi-add fix: two profiles of one ASCCP under different Business Contexts share a DEN /
    // property term but resolve to different resource paths (the backend derives the path from the Business
    // Context), so BOTH must be added. (Previously the batch was de-duplicated on the shared property term
    // and silently added nothing.)
    const a = bie(1, 'Alpha');
    const a2 = bie(2, 'Alpha');
    const ctx = batchCtx([a, a2], {1: 'POST', 2: 'POST'}, {1: 'Request', 2: 'Request'});
    ctx.addBieForOasDoc();

    expect(ctx.openAPIService.assignBieForOasDoc).toHaveBeenCalledTimes(2);
    expect(ctx._order).toEqual(['assign:Alpha', 'assign:Alpha']);
    expect(ctx.dialogRef.close).toHaveBeenCalledTimes(1);
    expect(ctx.snackBar.open).toHaveBeenCalledWith('Added', '', {duration: 3000});
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
