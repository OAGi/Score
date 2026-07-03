import {vi} from 'vitest';
import {of} from 'rxjs';
import {OasDocDetailComponent} from './oas-doc-detail.component';
import {OasOperationValidator} from '../domain/oas-operation-validation';
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

describe('OasDocDetailComponent.openConfirmMessageDialog release resolution (#1347)', () => {
  // The ConfirmMessage picker locks a BIE-backed operation's Branch to its own release; a BODYLESS
  // operation (no release of its own) in a document spanning MULTIPLE releases instead gets an enabled
  // Branch selector over all of the document's releases (else it locks to the single/none release).
  // openConfirmMessageDialog delegates the release list to fetchAllOasDocReleasesThen and the dialog open
  // to openConfirmMessageDialogWith, so both are stubbed to capture how the picker is configured.
  function ctxWith(fetchedReleases: any[]): { ctx: any; captured: any } {
    const captured: any = {};
    const ctx: any = {
      openConfirmMessageDialog: OasDocDetailComponent.prototype['openConfirmMessageDialog'],
      openConfirmMessageDialogWith: (_row: any, _handler: any, selectableReleases: any, defaultRelease: any) => {
        captured.selectableReleases = selectableReleases;
        captured.defaultRelease = defaultRelease;
      },
      fetchAllOasDocReleasesThen: (done: (r: any[]) => void) => done(fetchedReleases),
    };
    return {ctx, captured};
  }

  it('locks a BIE-backed operation to its own release (no Branch selector)', () => {
    const {ctx, captured} = ctxWith([]);
    ctx.openConfirmMessageDialog({releaseId: 9, releaseNum: '10.9'}, () => {});
    expect(captured.selectableReleases).toBeUndefined();
    expect(captured.defaultRelease).toEqual({releaseId: 9, releaseNum: '10.9'});
  });

  it('offers an enabled Branch selector for a bodyless op in a multi-release document', () => {
    const releases = [{releaseId: 5, releaseNum: '10.11'}, {releaseId: 9, releaseNum: '10.12'}];
    const {ctx, captured} = ctxWith(releases);
    ctx.openConfirmMessageDialog({releaseId: 0, releaseNum: ''}, () => {});
    expect(captured.selectableReleases).toBe(releases);
    expect(captured.defaultRelease).toEqual(releases[0]);
  });

  it('locks a bodyless op to the single release when the document has exactly one', () => {
    const {ctx, captured} = ctxWith([{releaseId: 7, releaseNum: '10.7'}]);
    ctx.openConfirmMessageDialog({releaseId: 0, releaseNum: ''}, () => {});
    expect(captured.selectableReleases).toBeUndefined();
    expect(captured.defaultRelease).toEqual({releaseId: 7, releaseNum: '10.7'});
  });

  it('falls back to no release for a bodyless-only document (dialog then uses the latest release)', () => {
    const {ctx, captured} = ctxWith([]);
    ctx.openConfirmMessageDialog({releaseId: 0, releaseNum: ''}, () => {});
    expect(captured.selectableReleases).toBeUndefined();
    expect(captured.defaultRelease).toEqual({releaseId: undefined, releaseNum: undefined});
  });
});

describe('OasDocDetailComponent document-level "apply to all" error response (#1347)', () => {
  type BulkRow = {
    oasOperationId: number;
    releaseId: number;
    releaseNum?: string;
    errorResponseBodyType: string;
    confirmMessageTopLevelAsbiepId?: number;
    confirmMessageDen?: string;
  };

  const OPTIONS = [
    {value: 'NONE', label: 'No Response Body'},
    {value: 'PROBLEM_DETAILS', label: 'IETF Problem Details'},
    {value: 'CONFIRM_MESSAGE', label: 'OAGi Confirm Message'}
  ];

  // A `this` context exposing the SERVER-DRIVEN bulk-apply wiring. openAPIService + the ConfirmMessage
  // dialog are stubbed with synchronous observables so the persist -> apply -> reload chain runs inline.
  // The release-scoping/inheritance decision logic now lives (and is unit-tested) on the backend; these
  // tests assert the client wiring (correct payload, persist-first, reload).
  function bulkCtx(bodyType: string, rows: BulkRow[],
                   opts: {dialogResult?: any; changed?: boolean; changedRows?: any[]; releases?: any[]} = {}): any {
    const svc: any = {
      applyErrorResponseBodyTypeToAll: vi.fn(() => of(null)),
      updateDetails: vi.fn(() => of(null)),
      updateOasDoc: vi.fn(() => of(null)),
      getOasDoc: vi.fn(() => of(null)),
    };
    const oasDoc = {oasDocId: 7};
    const ctx: any = {
      table: {dataSource: {data: rows}},
      paginator: {length: rows.length},
      bulkErrorResponseBodyType: bodyType,
      errorResponseBodyTypeOptions: OPTIONS,
      oasDoc,
      loading: false,
      snackBar: {open: vi.fn()},
      openAPIService: svc,
      dialog: {open: vi.fn(() => ({afterClosed: () => of(opts.dialogResult)}))},
      isChanged: () => !!opts.changed,
      getChanged: () => (opts.changed ? (opts.changedRows || []) : []),
      recomputeOasValidation: vi.fn(),
      oasValidator: {duplicateBodySlots: new Set()},
      // no doc-level change -> persist takes the details-only branch
      hashCodeForOasDoc: hashCode(oasDoc),
      loadBieListForOasDoc: vi.fn(),
      fetchAllOasDocReleasesThen: vi.fn((done: (releases: any[]) => void) => done(opts.releases || [])),
      bulkBodyTypeLabel: OasDocDetailComponent.prototype['bulkBodyTypeLabel'],
      persistChangesThen: OasDocDetailComponent.prototype['persistChangesThen'],
      sendBulkErrorResponse: OasDocDetailComponent.prototype['sendBulkErrorResponse'],
      applyErrorResponseBodyTypeToAll: OasDocDetailComponent.prototype.applyErrorResponseBodyTypeToAll,
    };
    return ctx;
  }

  it('fetchAllOasDocReleasesThen fetches the document releases from the backend in ONE call (no BIE-list scan)', () => {
    // The distinct-release derivation moved server-side (a single SELECT DISTINCT query). The client just
    // requests /bie_list/releases and passes the result through — it no longer fetches the paginated BIE
    // list and combines releases itself, so the visible grid's pagination is left untouched.
    const releases = [{releaseId: 5, releaseNum: '10.8'}, {releaseId: 9, releaseNum: '10.9'}];
    const svc: any = {
      getReleasesForOasDoc: vi.fn(() => of(releases)),
      getBieForOasDocListWithRequest: vi.fn(),
    };
    const ctx: any = {
      oasDoc: {oasDocId: 7},
      loading: false,
      openAPIService: svc,
      fetchAllOasDocReleasesThen: OasDocDetailComponent.prototype['fetchAllOasDocReleasesThen'],
    };
    let received: any;
    ctx.fetchAllOasDocReleasesThen((r: any) => received = r);
    expect(svc.getReleasesForOasDoc).toHaveBeenCalledWith(ctx.oasDoc);
    expect(svc.getBieForOasDocListWithRequest).not.toHaveBeenCalled(); // no full BIE-list scan
    expect(received).toBe(releases);
  });

  it('NONE / IETF Problem Details POST to the server for all operations (no dialog), then reload', () => {
    const ctx = bulkCtx('PROBLEM_DETAILS', [{oasOperationId: 1, releaseId: 5, errorResponseBodyType: 'NONE'}]);
    ctx.applyErrorResponseBodyTypeToAll();
    expect(ctx.dialog.open).not.toHaveBeenCalled();
    expect(ctx.openAPIService.applyErrorResponseBodyTypeToAll)
      .toHaveBeenCalledWith(7, {errorResponseBodyType: 'PROBLEM_DETAILS'});
    expect(ctx.loadBieListForOasDoc).toHaveBeenCalled();
  });

  it('Confirm Message loads all operations, picks release+BIE, then POSTs that release + BIE', () => {
    const ctx = bulkCtx('CONFIRM_MESSAGE', [{oasOperationId: 1, releaseId: 7, errorResponseBodyType: 'NONE'}],
      {dialogResult: {topLevelAsbiepId: 42, den: 'Confirm Message. Confirm Message', releaseId: 7},
       releases: [{releaseId: 7, releaseNum: '10.7'}]});
    ctx.applyErrorResponseBodyTypeToAll();
    expect(ctx.fetchAllOasDocReleasesThen).toHaveBeenCalledTimes(1); // full-doc release list
    expect(ctx.dialog.open).toHaveBeenCalledTimes(1);
    expect(ctx.openAPIService.applyErrorResponseBodyTypeToAll).toHaveBeenCalledWith(7, {
      errorResponseBodyType: 'CONFIRM_MESSAGE', confirmMessageTopLevelAsbiepId: 42, releaseId: 7,
    });
    expect(ctx.loadBieListForOasDoc).toHaveBeenCalled();
  });

  it('Confirm Message cancelled does NOT POST, but reloads to restore pagination', () => {
    const ctx = bulkCtx('CONFIRM_MESSAGE', [{oasOperationId: 1, releaseId: 7, errorResponseBodyType: 'NONE'}],
      {dialogResult: undefined, releases: [{releaseId: 7, releaseNum: '10.7'}]});
    ctx.applyErrorResponseBodyTypeToAll();
    expect(ctx.openAPIService.applyErrorResponseBodyTypeToAll).not.toHaveBeenCalled();
    expect(ctx.loadBieListForOasDoc).toHaveBeenCalled();
  });

  it('persists unsaved inline edits BEFORE the bulk apply', () => {
    const ctx = bulkCtx('PROBLEM_DETAILS', [{oasOperationId: 1, releaseId: 5, errorResponseBodyType: 'NONE'}],
      {changed: true, changedRows: [{operationId: 'queryThing'}]});
    ctx.applyErrorResponseBodyTypeToAll();
    // details saved first, then the bulk apply, then reload
    expect(ctx.openAPIService.updateDetails).toHaveBeenCalledTimes(1);
    expect(ctx.openAPIService.applyErrorResponseBodyTypeToAll).toHaveBeenCalledTimes(1);
    const saveOrder = ctx.openAPIService.updateDetails.mock.invocationCallOrder[0];
    const bulkOrder = ctx.openAPIService.applyErrorResponseBodyTypeToAll.mock.invocationCallOrder[0];
    expect(saveOrder).toBeLessThan(bulkOrder);
  });
});

describe('OasDocDetailComponent.doUpdate gates duplicate body slots (#1492 Option 2)', () => {
  function updateCtx(rows: any[], changed: any[]): any {
    const oasDoc = {oasDocId: 1};
    const ctx: any = {
      table: {dataSource: {data: rows}},
      // The duplicate-body gate now lives in the shared validator (delegated to by recomputeOasValidation).
      oasValidator: new OasOperationValidator(),
      // Match the baseline so docChanged is false; with no changed rows, doUpdate returns after the gate.
      hashCodeForOasDoc: hashCode(oasDoc),
      oasDoc,
      snackBar: {open: vi.fn()},
      getChanged: () => changed,
      updateDetails: vi.fn(),
    };
    ctx.recomputeOasValidation = OasDocDetailComponent.prototype.recomputeOasValidation;
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
