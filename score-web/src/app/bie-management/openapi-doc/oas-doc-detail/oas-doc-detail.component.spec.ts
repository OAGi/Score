import {vi} from 'vitest';
import {OasDocDetailComponent} from './oas-doc-detail.component';
import {hashCode} from 'src/app/common/utility';

/**
 * Issue #1610 unit tests for the OpenAPI-document editor.
 *
 * Focus areas:
 *   1) OpenAPI version detection — isOpenApi31() decides whether a DELETE request body is honored,
 *      and gates the 3.0.3-only "ignored DELETE body" banner.
 *   2) DELETE operation handling at the UI level — a DELETE may now carry a Request body in any
 *      version (only GET is reverted to Response), and 3.0.3 documents surface a warning banner.
 *
 * These follow the repo's domain-test style (see bie-flat-tree.spec.ts): no Angular TestBed. The
 * component uses inject() in its field initializers, so it cannot be `new`-ed outside an injection
 * context; instead each method under test is invoked against a hand-built `this` carrying only the
 * state that method touches. This keeps the test pinned to the commit's logic, not Angular wiring.
 */

type Row = { verb: string; messageBody: string };

function tableWith(rows: Row[]): any {
  return {dataSource: {data: rows}};
}

/** A `this` context exposing the version-detection + banner methods bound to a shared data bag. */
function detailCtx(openAPIVersion: any, rows: Row[] = []): any {
  const ctx: any = {
    oasDoc: openAPIVersion === null ? null : {openAPIVersion},
    table: tableWith(rows),
  };
  ctx.isOpenApi31 = OasDocDetailComponent.prototype.isOpenApi31;
  ctx.hasIgnoredDeleteRequestBody = OasDocDetailComponent.prototype.hasIgnoredDeleteRequestBody;
  return ctx;
}

describe('OasDocDetailComponent', () => {
  it('should be defined', () => {
    expect(OasDocDetailComponent).toBeTruthy();
  });
});

describe('OasDocDetailComponent.isOpenApi31 (#1610)', () => {
  it('is true for 3.1.x versions', () => {
    expect(detailCtx('3.1.1').isOpenApi31()).toBe(true);
    expect(detailCtx('3.1.0').isOpenApi31()).toBe(true);
    expect(detailCtx('3.1').isOpenApi31()).toBe(true);
  });

  it('trims surrounding whitespace before matching', () => {
    expect(detailCtx('  3.1.1 ').isOpenApi31()).toBe(true);
  });

  it('is false for 3.0.3 and other non-3.1 versions', () => {
    expect(detailCtx('3.0.3').isOpenApi31()).toBe(false);
    expect(detailCtx('3.0.0').isOpenApi31()).toBe(false);
    // a "3.1" substring that is not the version prefix must not match
    expect(detailCtx('2.3.1').isOpenApi31()).toBe(false);
  });

  it('is false when the version or the document is missing', () => {
    expect(detailCtx('').isOpenApi31()).toBe(false);
    expect(detailCtx(undefined).isOpenApi31()).toBe(false);
    expect(detailCtx(null).isOpenApi31()).toBe(false);
  });
});

describe('OasDocDetailComponent.hasIgnoredDeleteRequestBody (#1610 banner)', () => {
  it('is true for a 3.0.3 document that has a DELETE + Request operation', () => {
    const ctx = detailCtx('3.0.3', [{verb: 'DELETE', messageBody: 'Request'}]);
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(true);
  });

  it('is false for the SAME data when the document targets 3.1.1 (the body is honored)', () => {
    const ctx = detailCtx('3.1.1', [{verb: 'DELETE', messageBody: 'Request'}]);
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(false);
  });

  it('is false on 3.0.3 when no DELETE carries a Request body', () => {
    expect(detailCtx('3.0.3', [{verb: 'DELETE', messageBody: 'Response'}]).hasIgnoredDeleteRequestBody()).toBe(false);
    expect(detailCtx('3.0.3', [{verb: 'POST', messageBody: 'Request'}]).hasIgnoredDeleteRequestBody()).toBe(false);
  });

  it('is false on 3.0.3 when the operation list is empty', () => {
    expect(detailCtx('3.0.3', []).hasIgnoredDeleteRequestBody()).toBe(false);
  });

  it('does not throw when the table/dataSource is not yet loaded', () => {
    const ctx: any = {oasDoc: {openAPIVersion: '3.0.3'}, table: undefined};
    ctx.isOpenApi31 = OasDocDetailComponent.prototype.isOpenApi31;
    ctx.hasIgnoredDeleteRequestBody = OasDocDetailComponent.prototype.hasIgnoredDeleteRequestBody;
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(false);
  });
});

describe('OasDocDetailComponent.onChange verb handling (#1610)', () => {
  function verbCtx(): any {
    const ctx: any = {updateOperationIdForVerb: vi.fn()};
    ctx.onChange = OasDocDetailComponent.prototype.onChange;
    return ctx;
  }

  it('reverts a GET operation that still has a Request body back to Response', () => {
    const ctx = verbCtx();
    const source = {verb: 'GET', messageBody: 'Request'};
    ctx.onChange('verb', source);
    expect(source.messageBody).toBe('Response');
    expect(ctx.updateOperationIdForVerb).toHaveBeenCalledWith(source);
  });

  it('KEEPS a Request body on a DELETE operation (honored in 3.1, dropped-with-banner in 3.0.3)', () => {
    const ctx = verbCtx();
    const source = {verb: 'DELETE', messageBody: 'Request'};
    ctx.onChange('verb', source);
    expect(source.messageBody).toBe('Request');
    expect(ctx.updateOperationIdForVerb).toHaveBeenCalledWith(source);
  });

  it('leaves a non-GET Request body untouched (e.g. POST) and still resyncs the operationId', () => {
    const ctx = verbCtx();
    const source = {verb: 'POST', messageBody: 'Request'};
    ctx.onChange('verb', source);
    expect(source.messageBody).toBe('Request');
    expect(ctx.updateOperationIdForVerb).toHaveBeenCalledWith(source);
  });
});

describe('OasDocDetailComponent error-response selector (#1347)', () => {
  type ErrRow = {
    oasOperationId: number;
    errorResponseBodyType: string;
    confirmMessageTopLevelAsbiepId?: number;
    confirmMessageDen?: string;
  };

  // A `this` context exposing the inline error-response selector methods over a shared row list. The
  // ConfirmMessage dialog is stubbed to invoke its handler synchronously with `dialogResult`, so the
  // commit / revert / keep branches can be asserted without Angular's MatDialog.
  function errCtx(rows: ErrRow[], dialogResult?: any): any {
    const ctx: any = {
      table: {dataSource: {data: rows}},
      committedErrorResponseBodyType: new WeakMap<any, string>(),
      applyErrorResponseBodyType: OasDocDetailComponent.prototype['applyErrorResponseBodyType'],
      onErrorResponseBodyTypeChange: OasDocDetailComponent.prototype.onErrorResponseBodyTypeChange,
      openConfirmMessageBiePicker: OasDocDetailComponent.prototype.openConfirmMessageBiePicker,
      confirmMessageChipLabel: OasDocDetailComponent.prototype.confirmMessageChipLabel,
      // Stub the dialog opener: fire the handler with the configured result (object = picked, undefined = cancel).
      openConfirmMessageDialog: function (_row: any, handler: (r: any) => void) {
        handler(dialogResult);
      },
    };
    // Seed the per-row baseline body type (mirrors loadBieListForOasDoc).
    rows.forEach(r => ctx.committedErrorResponseBodyType.set(r, r.errorResponseBodyType || 'NONE'));
    return ctx;
  }

  it('chip label shows the DEN, or a prompt when none is picked', () => {
    const ctx = errCtx([]);
    expect(ctx.confirmMessageChipLabel({confirmMessageDen: 'Confirm Message. Confirm Message'}))
      .toBe('Confirm Message. Confirm Message');
    expect(ctx.confirmMessageChipLabel({confirmMessageDen: ''})).toBe('Pick a ConfirmMessage BIE…');
    expect(ctx.confirmMessageChipLabel({})).toBe('Pick a ConfirmMessage BIE…');
  });

  it('NONE / PROBLEM_DETAILS commit immediately and never open the dialog', () => {
    const row: ErrRow = {oasOperationId: 7, errorResponseBodyType: 'PROBLEM_DETAILS'};
    const ctx = errCtx([row]);
    let dialogOpened = false;
    ctx.openConfirmMessageDialog = () => { dialogOpened = true; };
    ctx.onErrorResponseBodyTypeChange(row);
    expect(dialogOpened).toBe(false);
    expect(row.errorResponseBodyType).toBe('PROBLEM_DETAILS');
  });

  it('applies the body type to every sibling row sharing the oasOperationId', () => {
    const reqRow: ErrRow = {oasOperationId: 9, errorResponseBodyType: 'NONE'};
    const respRow: ErrRow = {oasOperationId: 9, errorResponseBodyType: 'NONE'};
    const otherRow: ErrRow = {oasOperationId: 10, errorResponseBodyType: 'NONE'};
    const ctx = errCtx([reqRow, respRow, otherRow]);
    reqRow.errorResponseBodyType = 'PROBLEM_DETAILS';
    ctx.onErrorResponseBodyTypeChange(reqRow);
    expect(reqRow.errorResponseBodyType).toBe('PROBLEM_DETAILS');
    expect(respRow.errorResponseBodyType).toBe('PROBLEM_DETAILS'); // the sibling followed
    expect(otherRow.errorResponseBodyType).toBe('NONE');           // a different operation is untouched
  });

  it('CONFIRM_MESSAGE with a picked BIE stores the id + DEN on all siblings', () => {
    const reqRow: ErrRow = {oasOperationId: 4, errorResponseBodyType: 'NONE'};
    const respRow: ErrRow = {oasOperationId: 4, errorResponseBodyType: 'NONE'};
    const ctx = errCtx([reqRow, respRow], {topLevelAsbiepId: 42, den: 'Confirm Message. Confirm Message'});
    reqRow.errorResponseBodyType = 'CONFIRM_MESSAGE';
    ctx.onErrorResponseBodyTypeChange(reqRow);
    for (const r of [reqRow, respRow]) {
      expect(r.errorResponseBodyType).toBe('CONFIRM_MESSAGE');
      expect(r.confirmMessageTopLevelAsbiepId).toBe(42);
      expect(r.confirmMessageDen).toBe('Confirm Message. Confirm Message');
    }
  });

  it('CONFIRM_MESSAGE cancelled with no prior BIE reverts to the previously committed type', () => {
    const row: ErrRow = {oasOperationId: 3, errorResponseBodyType: 'PROBLEM_DETAILS'};
    const ctx = errCtx([row], undefined); // dialog cancelled
    row.errorResponseBodyType = 'CONFIRM_MESSAGE'; // ngModel updated the row before the handler runs
    ctx.onErrorResponseBodyTypeChange(row);
    expect(row.errorResponseBodyType).toBe('PROBLEM_DETAILS'); // reverted
    expect(row.confirmMessageTopLevelAsbiepId).toBeUndefined();
  });

  it('CONFIRM_MESSAGE cancelled but with an existing BIE keeps CONFIRM_MESSAGE and the BIE', () => {
    const row: ErrRow = {
      oasOperationId: 3, errorResponseBodyType: 'CONFIRM_MESSAGE',
      confirmMessageTopLevelAsbiepId: 8, confirmMessageDen: 'Confirm Message. Confirm Message'
    };
    const ctx = errCtx([row], undefined); // re-opened the picker, then cancelled
    ctx.onErrorResponseBodyTypeChange(row);
    expect(row.errorResponseBodyType).toBe('CONFIRM_MESSAGE');
    expect(row.confirmMessageTopLevelAsbiepId).toBe(8);
    expect(row.confirmMessageDen).toBe('Confirm Message. Confirm Message');
  });

  it('switching from CONFIRM_MESSAGE to NONE clears the ConfirmMessage BIE', () => {
    const row: ErrRow = {
      oasOperationId: 5, errorResponseBodyType: 'CONFIRM_MESSAGE',
      confirmMessageTopLevelAsbiepId: 8, confirmMessageDen: 'Confirm Message. Confirm Message'
    };
    const ctx = errCtx([row]);
    ctx.committedErrorResponseBodyType.set(row, 'CONFIRM_MESSAGE');
    row.errorResponseBodyType = 'NONE';
    ctx.onErrorResponseBodyTypeChange(row);
    expect(row.errorResponseBodyType).toBe('NONE');
    expect(row.confirmMessageTopLevelAsbiepId).toBeUndefined();
    expect(row.confirmMessageDen).toBe('');
  });

  it('the chip re-picker updates the BIE on all siblings without changing the body type', () => {
    const reqRow: ErrRow = {
      oasOperationId: 6, errorResponseBodyType: 'CONFIRM_MESSAGE',
      confirmMessageTopLevelAsbiepId: 1, confirmMessageDen: 'Old. Old'
    };
    const respRow: ErrRow = {
      oasOperationId: 6, errorResponseBodyType: 'CONFIRM_MESSAGE',
      confirmMessageTopLevelAsbiepId: 1, confirmMessageDen: 'Old. Old'
    };
    const ctx = errCtx([reqRow, respRow], {topLevelAsbiepId: 99, den: 'New. New'});
    ctx.openConfirmMessageBiePicker(reqRow, {stopPropagation: () => {}, preventDefault: () => {}});
    for (const r of [reqRow, respRow]) {
      expect(r.errorResponseBodyType).toBe('CONFIRM_MESSAGE');
      expect(r.confirmMessageTopLevelAsbiepId).toBe(99);
      expect(r.confirmMessageDen).toBe('New. New');
    }
  });

  it('a not-yet-persisted operation (oasOperationId 0) is never treated as a sibling', () => {
    const newA: ErrRow = {oasOperationId: 0, errorResponseBodyType: 'NONE'};
    const newB: ErrRow = {oasOperationId: 0, errorResponseBodyType: 'NONE'};
    const ctx = errCtx([newA, newB]);
    newA.errorResponseBodyType = 'PROBLEM_DETAILS';
    ctx.onErrorResponseBodyTypeChange(newA);
    expect(newA.errorResponseBodyType).toBe('PROBLEM_DETAILS');
    expect(newB.errorResponseBodyType).toBe('NONE'); // not cross-contaminated
  });
});

describe('OasDocDetailComponent.resolveOasDocRelease locks to the connected BIE release (#1347)', () => {
  // The ConfirmMessage picker is locked to the release of the document's connected BIE. The document has
  // no release of its own, so resolveOasDocRelease derives it from the table rows.
  function ctxWith(rows: any[]): any {
    return {
      table: {dataSource: {data: rows}},
      resolveOasDocRelease: OasDocDetailComponent.prototype['resolveOasDocRelease'],
    };
  }

  it('uses the acted-on row release when present', () => {
    const ctx = ctxWith([{releaseId: 5, releaseNum: '10.8'}, {releaseId: 9, releaseNum: '10.9'}]);
    expect(ctx.resolveOasDocRelease({releaseId: 9, releaseNum: '10.9'}))
      .toEqual({releaseId: 9, releaseNum: '10.9'});
  });

  it('falls back to any BIE-backed row when the acted-on row is bodyless (no release)', () => {
    const ctx = ctxWith([{releaseId: 0, releaseNum: ''}, {releaseId: 7, releaseNum: '10.7'}]);
    expect(ctx.resolveOasDocRelease({releaseId: 0, releaseNum: ''}))
      .toEqual({releaseId: 7, releaseNum: '10.7'});
  });

  it('returns an undefined release for a document with no connected BIE', () => {
    const ctx = ctxWith([{releaseId: 0, releaseNum: ''}]);
    expect(ctx.resolveOasDocRelease({releaseId: 0, releaseNum: ''}))
      .toEqual({releaseId: undefined, releaseNum: undefined});
  });
});

describe('OasDocDetailComponent body-slot uniqueness (#1492 Option 2)', () => {
  type SlotRow = {resourceName: string; verb: string; messageBody: string};

  // A `this` context exposing the body-slot guard methods over a shared row list.
  function slotCtx(rows: SlotRow[] = []): any {
    const ctx: any = {
      table: {dataSource: {data: rows}},
      duplicateBodySlots: new Set<string>(),
    };
    ctx.bodySlotKey = OasDocDetailComponent.prototype.bodySlotKey;
    ctx.isDuplicateBodySlot = OasDocDetailComponent.prototype.isDuplicateBodySlot;
    ctx.bodySlotErrorStateMatcher = OasDocDetailComponent.prototype.bodySlotErrorStateMatcher;
    ctx.recomputeDuplicateBodySlots = OasDocDetailComponent.prototype.recomputeDuplicateBodySlots;
    return ctx;
  }

  it('bodySlotKey is the resourceName|verb|messageBody triple (trimmed)', () => {
    const ctx = slotCtx();
    expect(ctx.bodySlotKey({resourceName: '  /orders ', verb: 'POST', messageBody: 'Request'}))
      .toBe('/orders|POST|Request');
  });

  it('does NOT flag a legitimate Request + Response on one endpoint (different message bodies)', () => {
    const ctx = slotCtx([
      {resourceName: '/orders', verb: 'POST', messageBody: 'Request'},
      {resourceName: '/orders', verb: 'POST', messageBody: 'Response'},
    ]);
    ctx.recomputeDuplicateBodySlots();
    expect(ctx.duplicateBodySlots.size).toBe(0);
    expect(ctx.isDuplicateBodySlot(ctx.table.dataSource.data[0])).toBe(false);
    expect(ctx.isDuplicateBodySlot(ctx.table.dataSource.data[1])).toBe(false);
  });

  it('flags a true duplicate (a 2nd Request on the same (path, verb))', () => {
    const dupA = {resourceName: '/orders', verb: 'POST', messageBody: 'Request'};
    const dupB = {resourceName: '/orders', verb: 'POST', messageBody: 'Request'};
    const other = {resourceName: '/orders', verb: 'POST', messageBody: 'Response'};
    const ctx = slotCtx([dupA, dupB, other]);
    ctx.recomputeDuplicateBodySlots();
    expect(ctx.duplicateBodySlots.has('/orders|POST|Request')).toBe(true);
    expect(ctx.isDuplicateBodySlot(dupA)).toBe(true);
    expect(ctx.isDuplicateBodySlot(dupB)).toBe(true);
    expect(ctx.isDuplicateBodySlot(other)).toBe(false); // the Response sibling is fine
  });

  it('does not collide across DIFFERENT verbs on the same resource', () => {
    const ctx = slotCtx([
      {resourceName: '/orders', verb: 'POST', messageBody: 'Request'},
      {resourceName: '/orders', verb: 'PUT', messageBody: 'Request'},
    ]);
    ctx.recomputeDuplicateBodySlots();
    expect(ctx.duplicateBodySlots.size).toBe(0);
  });

  it('ignores rows with no verb or no message body when counting', () => {
    const ctx = slotCtx([
      {resourceName: '/orders', verb: '', messageBody: ''},
      {resourceName: '/orders', verb: '', messageBody: ''},
    ]);
    ctx.recomputeDuplicateBodySlots();
    expect(ctx.duplicateBodySlots.size).toBe(0);
  });

  it('bodySlotErrorStateMatcher returns a per-row matcher reflecting the duplicate set', () => {
    const dup = {resourceName: '/orders', verb: 'POST', messageBody: 'Request'};
    const ok = {resourceName: '/orders', verb: 'POST', messageBody: 'Response'};
    const ctx = slotCtx([dup, dup, ok]);
    ctx.recomputeDuplicateBodySlots();
    expect(ctx.bodySlotErrorStateMatcher(dup).isErrorState()).toBe(true);
    expect(ctx.bodySlotErrorStateMatcher(ok).isErrorState()).toBe(false);
  });
});

describe('OasDocDetailComponent operationId uniqueness (#1732 / #1492)', () => {
  type OpRow = {resourceName: string; verb: string; messageBody: string; operationId: string};

  // A `this` context exposing recomputeDuplicateOperationIds() over a shared row list. The check keys
  // on the OPERATION (resourceName|verb) — NOT on operationId, and NOT on messageBody — so a single
  // operation's Request + Response rows (which legitimately share one operationId) collapse to one
  // operation and are not a duplicate.
  function opIdCtx(rows: OpRow[] = []): any {
    const ctx: any = {
      table: {dataSource: {data: rows}},
      duplicateOperationIds: new Set<string>(),
    };
    ctx.recomputeDuplicateOperationIds = OasDocDetailComponent.prototype.recomputeDuplicateOperationIds;
    return ctx;
  }

  // S1: the exact reported case — one DELETE /test/1/expense-report operation with a Request BIE and a
  // Response BIE shows as two rows sharing operationId 'deleteExpenseReport'. Must NOT be flagged.
  it('does NOT flag a Request + Response of one operation sharing one operationId', () => {
    const ctx = opIdCtx([
      {resourceName: '/test/1/expense-report', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteExpenseReport'},
      {resourceName: '/test/1/expense-report', verb: 'DELETE', messageBody: 'Response', operationId: 'deleteExpenseReport'},
    ]);
    ctx.recomputeDuplicateOperationIds();
    expect(ctx.duplicateOperationIds.size).toBe(0);
  });

  // S2: the same operationId on two DIFFERENT endpoints (different path) is a real OpenAPI violation.
  it('flags one operationId reused on two different endpoints (different resourceName)', () => {
    const ctx = opIdCtx([
      {resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteThing'},
      {resourceName: '/b', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteThing'},
    ]);
    ctx.recomputeDuplicateOperationIds();
    expect(ctx.duplicateOperationIds.has('deleteThing')).toBe(true);
  });

  // S3: still a real duplicate when the two colliding rows differ in messageBody but belong to two
  // distinct operations — proves messageBody must be EXCLUDED from the operation key.
  it('flags one operationId across two different operations even when message bodies differ', () => {
    const ctx = opIdCtx([
      {resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteThing'},
      {resourceName: '/b', verb: 'DELETE', messageBody: 'Response', operationId: 'deleteThing'},
    ]);
    ctx.recomputeDuplicateOperationIds();
    expect(ctx.duplicateOperationIds.has('deleteThing')).toBe(true);
  });

  it('does not flag distinct operationIds on different endpoints', () => {
    const ctx = opIdCtx([
      {resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteA'},
      {resourceName: '/b', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteB'},
    ]);
    ctx.recomputeDuplicateOperationIds();
    expect(ctx.duplicateOperationIds.size).toBe(0);
  });

  it('ignores rows with a blank operationId (the required check owns empties, not uniqueness)', () => {
    const ctx = opIdCtx([
      {resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: ''},
      {resourceName: '/b', verb: 'DELETE', messageBody: 'Request', operationId: '   '},
    ]);
    ctx.recomputeDuplicateOperationIds();
    expect(ctx.duplicateOperationIds.size).toBe(0);
  });

  it('uses a trimmed (resourceName, verb) operation key so one endpoint is not split by whitespace', () => {
    const ctx = opIdCtx([
      {resourceName: ' /orders ', verb: 'POST', messageBody: 'Request', operationId: 'createOrder'},
      {resourceName: '/orders', verb: 'POST', messageBody: 'Response', operationId: 'createOrder'},
    ]);
    ctx.recomputeDuplicateOperationIds();
    expect(ctx.duplicateOperationIds.size).toBe(0);
  });
});

describe('OasDocDetailComponent.doUpdate gates duplicate body slots (#1492 Option 2)', () => {
  function updateCtx(rows: any[], changed: any[]): any {
    const oasDoc = {oasDocId: 1};
    const ctx: any = {
      table: {dataSource: {data: rows}},
      duplicateBodySlots: new Set<string>(),
      // Match the baseline so docChanged is false; with no changed rows, doUpdate returns after the gate.
      hashCodeForOasDoc: hashCode(oasDoc),
      oasDoc,
      snackBar: {open: vi.fn()},
      getChanged: () => changed,
      updateDetails: vi.fn(),
    };
    ctx.bodySlotKey = OasDocDetailComponent.prototype.bodySlotKey;
    ctx.recomputeDuplicateBodySlots = OasDocDetailComponent.prototype.recomputeDuplicateBodySlots;
    ctx.doUpdate = OasDocDetailComponent.prototype.doUpdate;
    return ctx;
  }

  it('blocks the update with a snackBar when a duplicate body slot exists', () => {
    const dup = {resourceName: '/orders', verb: 'POST', messageBody: 'Request', operationId: 'createOrder'};
    const dup2 = {resourceName: '/orders', verb: 'POST', messageBody: 'Request', operationId: 'createOrder2'};
    const ctx = updateCtx([dup, dup2], [dup, dup2]);
    ctx.doUpdate();
    expect(ctx.snackBar.open).toHaveBeenCalledTimes(1);
    expect(ctx.snackBar.open.mock.calls[0][0]).toContain('only one Request and one Response body');
    expect(ctx.updateDetails).not.toHaveBeenCalled();
  });

  it('does not block when there are no duplicate body slots', () => {
    const req = {resourceName: '/orders', verb: 'POST', messageBody: 'Request', operationId: 'createOrder'};
    const resp = {resourceName: '/orders', verb: 'POST', messageBody: 'Response', operationId: 'queryOrder'};
    // No doc change and no detail change => doUpdate does nothing, but it must NOT hit the dup snackBar.
    const ctx = updateCtx([req, resp], []);
    ctx.doUpdate();
    const dupCalls = ctx.snackBar.open.mock.calls.filter(
      (c: any[]) => typeof c[0] === 'string' && c[0].includes('only one Request and one Response body'));
    expect(dupCalls.length).toBe(0);
  });
});

describe('OasDocDetailComponent.generate guards unsaved changes (#1610)', () => {
  function generateCtx(changed: boolean): any {
    const ctx: any = {
      isChanged: () => changed,
      snackBar: {open: vi.fn()},
      openAPIService: {generateOpenAPI: vi.fn().mockReturnValue({subscribe: vi.fn()})},
      oasDoc: {oasDocId: 123},
      request: {page: {pageIndex: 0}},
      loading: false,
    };
    ctx.generate = OasDocDetailComponent.prototype.generate;
    return ctx;
  }

  it('blocks generation and prompts to Update when there are unsaved changes', () => {
    const ctx = generateCtx(true);
    ctx.generate();
    expect(ctx.openAPIService.generateOpenAPI).not.toHaveBeenCalled();
    expect(ctx.loading).toBe(false);
    expect(ctx.snackBar.open).toHaveBeenCalledTimes(1);
    const message = ctx.snackBar.open.mock.calls[0][0] as string;
    expect(message).toContain('unsaved changes');
    expect(message).toContain('Update');
  });

  it('generates against the persisted document when there are no unsaved changes', () => {
    const ctx = generateCtx(false);
    ctx.generate();
    expect(ctx.snackBar.open).not.toHaveBeenCalled();
    expect(ctx.openAPIService.generateOpenAPI).toHaveBeenCalledWith(123, ctx.request.page);
    expect(ctx.loading).toBe(true);
  });
});
