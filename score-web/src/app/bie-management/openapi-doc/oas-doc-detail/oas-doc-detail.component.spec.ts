import {vi} from 'vitest';
import {of, throwError} from 'rxjs';
import {OasDocDetailComponent} from './oas-doc-detail.component';
import {OasOperationValidator} from '../domain/oas-operation-validation';
import {hashCode} from '../../../common/utility';
import {EditedRows} from '../../../common/score-table';
import {OasDocHeaderEdits} from '../domain/oas-doc-change';
import {OasDocChangeChecker} from '../domain/oas-doc-change-checker';
import {FormControl} from '@angular/forms';
import {httpUrlValidator, optionalEmailValidator} from '../oas-doc-url.validator';
import {linkHeadersToRows} from '../domain/oas-doc-http-header-preset';

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

/** A `this` context exposing the version-detection + banner methods bound to a shared data bag. */
function detailCtx(openAPIVersion: any, rows: Row[] = []): any {
  const ctx: any = {
    oasDoc: openAPIVersion === null ? null : {openAPIVersion},
    rows,
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
    const ctx: any = {oasDoc: {openAPIVersion: '3.0.3'}, rows: []};
    ctx.isOpenApi31 = OasDocDetailComponent.prototype.isOpenApi31;
    ctx.hasIgnoredDeleteRequestBody = OasDocDetailComponent.prototype.hasIgnoredDeleteRequestBody;
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(false);
  });
});

describe('OasDocDetailComponent.onChange verb handling (#1610)', () => {
  function verbCtx(): any {
    const ctx: any = {};
    ctx.onChange = OasDocDetailComponent.prototype.onChange;
    return ctx;
  }

  const rowOf = (verb: string, messageBody: string): any => {
    const row: any = {verb, messageBody, touched: [] as string[]};
    row.touch = (field: string) => row.touched.push(field);
    return row;
  };

  // The server makes what follows from the verb (the Operation ID, and that a GET has no Request body): the page says that
  // the verb was changed, and does not change the row itself.
  it('says that the verb was changed, and leaves the rest to the server', () => {
    for (const [verb, messageBody] of [['GET', 'Request'], ['DELETE', 'Request'], ['POST', 'Request']]) {
      const source = rowOf(verb, messageBody);
      verbCtx().onChange('verb', source);
      expect(source.touched).toEqual(['verb']);
      expect(source.messageBody).toBe(messageBody);
    }
  });

  it('says nothing for another property', () => {
    const source = rowOf('GET', 'Response');
    verbCtx().onChange('tagName', source);
    expect(source.touched).toEqual([]);
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
      rows,
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
      saveChanges: vi.fn(() => of({valid: true, violations: [], httpHeaders: []})),
      getOasDoc: vi.fn(() => of(null)),
    };
    const oasDoc = {oasDocId: 7};
    const ctx: any = {
      rows,
      totalRows: rows.length,
      bulkErrorResponseBodyType: bodyType,
      errorResponseBodyTypeOptions: OPTIONS,
      oasDoc,
      loading: false,
      snackBar: {open: vi.fn()},
      openAPIService: svc,
      dialog: {open: vi.fn(() => ({afterClosed: () => of(opts.dialogResult)}))},
      isChanged: () => !!opts.changed,
      getChanged: () => (opts.changed ? (opts.changedRows || []) : []),
      // the server has said that what is changed can be saved
      updateDisabled: false,
      changeChecker: {status: () => 'valid', violations: () => []},
      buildChangeSet: () => ({operations: (opts.changedRows || []).map(row => ({reset: () => {}, ...row})), headers: {upserts: [], removed: []}}),
      saveChanges: OasDocDetailComponent.prototype['saveChanges'],
      reloadAfterSave: (then: () => void) => then(),
      cannotSaveMessage: OasDocDetailComponent.prototype['cannotSaveMessage'],
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

  it('saves the change set BEFORE the bulk apply', () => {
    const ctx = bulkCtx('PROBLEM_DETAILS', [{oasOperationId: 1, releaseId: 5, errorResponseBodyType: 'NONE'}],
      {changed: true, changedRows: [{operationId: 'queryThing'}]});
    ctx.applyErrorResponseBodyTypeToAll();
    expect(ctx.openAPIService.saveChanges).toHaveBeenCalledTimes(1);
    expect(ctx.openAPIService.applyErrorResponseBodyTypeToAll).toHaveBeenCalledTimes(1);
    const saveOrder = ctx.openAPIService.saveChanges.mock.invocationCallOrder[0];
    const bulkOrder = ctx.openAPIService.applyErrorResponseBodyTypeToAll.mock.invocationCallOrder[0];
    expect(saveOrder).toBeLessThan(bulkOrder);
  });

  it('does not apply while the server has not said that what is changed can be saved', () => {
    const ctx = bulkCtx('PROBLEM_DETAILS', [{oasOperationId: 1, releaseId: 5, errorResponseBodyType: 'NONE'}],
      {changed: true, changedRows: [{operationId: 'queryThing'}]});
    ctx.updateDisabled = true;
    ctx.changeChecker = {status: () => 'invalid', violations: () => [{message: 'The Operation ID is used.'}]};

    ctx.applyErrorResponseBodyTypeToAll();

    expect(ctx.openAPIService.saveChanges).not.toHaveBeenCalled();
    expect(ctx.openAPIService.applyErrorResponseBodyTypeToAll).not.toHaveBeenCalled();
    expect(ctx.snackBar.open).toHaveBeenCalledWith('The Operation ID is used.', '', expect.anything());
  });
});

describe('OasDocDetailComponent asks the server what the user has changed, and saves it once the server has said that it can be saved', () => {
  const proto: any = OasDocDetailComponent.prototype;
  const header = (oasOperationId: number, name: string, extra: any = {}): any =>
    ({oasOperationId, name, in: 'query', direction: 'REQUEST', required: false, schema: {type: 'string'}, ...extra});

  /** The component without its constructor: the real methods, and only the state they work on. */
  function pageOf(status: 'idle' | 'checking' | 'valid' | 'invalid' | 'failed' = 'valid', options: {
    rows?: any[]; saveChanges?: () => any; getOasDoc?: () => any; httpHeaders?: any[]
  } = {}): any {
    const page: any = Object.create(proto);
    page.oasDoc = {oasDocId: 5, title: 't', version: '1', openAPIVersion: '3.1', httpHeaders: options.httpHeaders || [],
      securitySchemes: [], securityRequirements: []};
    page.rows = options.rows || [];
    page.edits = new EditedRows<any>(row => row.oasOperationId + '|' + row.messageBody, row => row.isChanged);
    page.headerEdits = new OasDocHeaderEdits();
    page.openAPIService = {
      getBieForOasDocPage: vi.fn(() => of({list: [], length: 0})),
      checkChanges: vi.fn(() => of({valid: true, violations: [], httpHeaders: []})),
      saveChanges: vi.fn(options.saveChanges || (() => of({valid: true, violations: [], httpHeaders: []}))),
      getOasDoc: vi.fn(options.getOasDoc || (() => of({securitySchemes: [], securityRequirements: [], httpHeaders: []}))),
      checkBIEReusedAcrossOperationsAfterUpdate: vi.fn(() => of({errorMessages: []})),
    };
    page.snackBar = {open: vi.fn()};
    page.loading = false;
    page.isUpdating = false;
    page.titleService = {setTitle: vi.fn()};
    page.contactUrl = new FormControl('');
    page.contactEmail = new FormControl('');
    page.licenseUrl = new FormControl('');
    page.termServiceUrl = new FormControl('');
    page.loadBieListForOasDoc = vi.fn();
    page.changeChecker = new OasDocChangeChecker({
      build: () => page.buildChangeSet(), check: cs => page.openAPIService.checkChanges(5, cs),
      onResult: (result, changeSet) => page.takeCheckResult(result, changeSet), debounceMs: 10
    });
    page.init(page.oasDoc);
    // what init() does not know: the checker has the answer of the server
    page.changeChecker.status.set(status);
    return page;
  }

  const editedRow = (oasOperationId: number, messageBody = 'Request'): any => {
    const row: any = {oasOperationId, messageBody, operationId: 'op' + oasOperationId, isChanged: true, hashCode: 1,
      json: {oasOperationId, messageBody}};
    row.reset = () => row.isChanged = false;
    return row;
  };

  describe('Update', () => {
    it('is there only when something is changed and the server has said that it can be saved', () => {
      const page = pageOf('valid');
      expect(page.updateDisabled).toBe(true); // nothing is changed

      page.oasDoc.title = 'another';
      expect(page.updateDisabled).toBe(false);
      for (const status of ['checking', 'invalid', 'failed', 'idle'] as const) {
        page.changeChecker.status.set(status);
        expect(page.updateDisabled).toBe(true);
      }
    });

    it('does not save while it is not there', () => {
      const page = pageOf('checking');
      page.oasDoc.title = 'another';

      page.doUpdate();

      expect(page.openAPIService.saveChanges).not.toHaveBeenCalled();
    });
  });

  describe('the change set', () => {
    it('has the document only when its fields are changed, and the entries as changes', () => {
      const page = pageOf();
      page.oasDoc.httpHeaders = [header(1, 'a')];
      page.headerEdits.reset([]);
      page.oasDoc.httpHeaders.push(header(1, 'b'));

      const changeSet = page.buildChangeSet();

      expect(changeSet.document).toBeUndefined();
      expect(changeSet.headers.upserts.map((h: any) => h.name)).toEqual(['a', 'b']);
    });

    it('has the document when a field of it is changed', () => {
      const page = pageOf();
      page.oasDoc.title = 'another';

      expect(page.buildChangeSet().document).toBe(page.oasDoc);
    });

    it('has the edited operations of every page, not only of the page that is shown', () => {
      const page = pageOf('valid', {rows: [editedRow(1), editedRow(2)]});
      page.rows = page.edits.carry(page.rows, [editedRow(3)]); // the next page

      expect(page.buildChangeSet().operations.map((r: any) => r.oasOperationId)).toEqual([1, 2, 3]);
    });

    it('is empty when nothing is changed: the server judges the document as it is', () => {
      expect(pageOf().buildChangeSet()).toEqual({document: undefined, operations: [], headers: {upserts: [], removed: []}});
    });

    it('says that the user deleted an entry that the server registered, so that it is not registered again', () => {
      const page = pageOf('valid', {rows: [editedRow(1)]}); // an operation was changed, which is what registers the entry
      const registered = header(1, 'sinceLastDateTime');
      page.oasDoc.httpHeaders = [registered];
      page.headerEdits.adopt([registered]); // the server's answer to a check

      page.oasDoc.httpHeaders = []; // the user deleted it
      const changeSet = page.buildChangeSet();

      expect(changeSet.headers.removed.map((k: any) => k.name)).toEqual(['sinceLastDateTime']);
    });
  });

  describe('when the user changes something', () => {
    it('asks the server once for a change, and not again for the answer', () => {
      vi.useFakeTimers();
      try {
        const page = pageOf('idle');
        page.oasDoc.title = 'another';
        page.ngDoCheck();
        page.ngDoCheck(); // the same change
        vi.advanceTimersByTime(10);

        expect(page.openAPIService.checkChanges).toHaveBeenCalledTimes(1);
        page.ngDoCheck();
        vi.advanceTimersByTime(10);
        expect(page.openAPIService.checkChanges).toHaveBeenCalledTimes(1);
      } finally {
        vi.useRealTimers();
      }
    });

    it('takes the entries that the server registered in as what the page shows, and not as the user\'s change', () => {
      vi.useFakeTimers();
      try {
        const registered = header(1, 'id', {in: 'path', required: true});
        const page = pageOf('idle', {rows: [{oasOperationId: 1, httpHeaders: []}]});
        page.openAPIService.checkChanges = vi.fn(() => of({valid: true, violations: [], httpHeaders: [registered]}));
        page.oasDoc.title = 'another';

        page.ngDoCheck();
        vi.advanceTimersByTime(10);

        expect(page.oasDoc.httpHeaders).toEqual([registered]);
        expect(page.rows[0].httpHeaders).toEqual([registered]);
        expect(page.headerEdits.changes()).toEqual({upserts: [], removed: []});
        page.ngDoCheck();
        vi.advanceTimersByTime(10);
        expect(page.openAPIService.checkChanges).toHaveBeenCalledTimes(1); // the answer is not a change
      } finally {
        vi.useRealTimers();
      }
    });

    it('does not take an answer to what the page was before the user changed it again', () => {
      const page = pageOf();
      page.oasDoc.title = 'one';
      page.buildChangeSet();
      page.oasDoc.title = 'two';

      expect(page.takeCheckResult({valid: true, violations: [], httpHeaders: [header(1, 'late')]}, {operations: [], headers: {upserts: [], removed: []}})).toBe(false);
      expect(page.oasDoc.httpHeaders).toEqual([]);
    });
  });

  describe('what the server makes of the verb or the array indicator that the user changed', () => {
    it('is the Operation ID, the Resource Name and the Message Body of the row, and the row is then not touched', () => {
      const row = editedRow(7, 'Request');
      row.touched = ['verb'];
      const page = pageOf('valid', {rows: [row]});
      const changeSet = {operations: [row], headers: {upserts: [], removed: []}};
      page.checkedFingerprint = page.changeFingerprint();

      page.takeCheckResult({valid: true, violations: [], httpHeaders: [], operations: [
        {oasOperationId: 7, sentMessageBody: 'Request', operationId: 'queryOrder', resourceName: '/order', messageBody: 'Response'}]}, changeSet);

      expect(row.operationId).toBe('queryOrder');
      expect(row.resourceName).toBe('/order');
      expect(row.messageBody).toBe('Response');
      expect(row.touched).toEqual([]);
    });

    it('is shown when the change cannot be saved too, next to what is wrong, and the entries are left as they are', () => {
      const row = editedRow(7, 'Response');
      row.touched = ['arrayIndicator'];
      const page = pageOf('valid', {rows: [row], httpHeaders: [header(7, 'kept')]});
      page.checkedFingerprint = page.changeFingerprint();

      page.takeCheckResult({valid: false, httpHeaders: [], violations: [{scope: 'OPERATION', message: 'used'}], operations: [
        {oasOperationId: 7, sentMessageBody: 'Response', operationId: 'queryOrderList', resourceName: '/order-list', messageBody: 'Response'}]},
        {operations: [row], headers: {upserts: [], removed: []}});

      expect(row.operationId).toBe('queryOrderList');
      expect(row.touched).toEqual([]);
      expect(page.oasDoc.httpHeaders.map((h: any) => h.name)).toEqual(['kept']);
    });

    it('is asked for again when the user touches the array indicator, and not for the answer', () => {
      vi.useFakeTimers();
      try {
        const row: any = editedRow(7, 'Response');
        row.touch = (field: string) => row.touched.push(field);
        row.touched = [];
        const page = pageOf('idle', {rows: [row]});
        page.touchArrayIndicator(row);

        expect(row.touched).toEqual(['arrayIndicator']);
      } finally {
        vi.useRealTimers();
      }
    });
  });

  describe('what the server says is wrong', () => {
    const violation = (extra: any): any => ({scope: 'OPERATION', message: 'wrong', ...extra});

    it('is shown at the cell of the operation, and of its Request or Response row when it says which', () => {
      const page = pageOf('invalid');
      page.changeChecker.violations.set([
        violation({oasOperationId: 7, field: 'operationId'}),
        violation({oasOperationId: 8, messageBody: 'Request', field: 'messageBody'})]);

      expect(page.violationOf({oasOperationId: 7, messageBody: 'Response'}, 'operationId')).toBeTruthy();
      expect(page.violationOf({oasOperationId: 7, messageBody: 'Response'}, 'messageBody')).toBeUndefined();
      expect(page.isDuplicateBodySlot({oasOperationId: 8, messageBody: 'Request'})).toBe(true);
      expect(page.isDuplicateBodySlot({oasOperationId: 8, messageBody: 'Response'})).toBe(false);
      expect(page.isDuplicateBodySlot({oasOperationId: 9, messageBody: 'Request'})).toBe(false);
    });

    it('marks an Operation ID that is blank, or that the server refused, as an error', () => {
      const page = pageOf('invalid');
      page.changeChecker.violations.set([violation({oasOperationId: 7, field: 'operationId'})]);
      const isError = (row: any) => page.operationIdErrorStateMatcher(row).isErrorState();

      expect(isError({oasOperationId: 7, operationId: 'a'})).toBe(true);
      expect(isError({oasOperationId: 8, operationId: ' '})).toBe(true);
      expect(isError({oasOperationId: 8, operationId: 'a'})).toBe(false);
    });
  });

  describe('saving', () => {
    it('saves the change set, then loads the document as saved, and says so', () => {
      const registered = header(1, 'id', {in: 'path'});
      const page = pageOf('valid', {getOasDoc: () => of({securitySchemes: [{oasSecuritySchemeId: 3}], securityRequirements: [], httpHeaders: [registered]})});
      page.oasDoc.title = 'another';

      page.doUpdate();

      expect(page.openAPIService.saveChanges).toHaveBeenCalledWith(5, expect.objectContaining({document: page.oasDoc}));
      expect(page.oasDoc.securitySchemes).toEqual([{oasSecuritySchemeId: 3}]);
      expect(page.oasDoc.httpHeaders).toEqual([registered]);
      expect(page.isChanged()).toBe(false); // as saved
      expect(page.changeChecker.status()).toBe('checking'); // the document as saved is judged again
      expect(page.loadBieListForOasDoc).toHaveBeenCalled();
      expect(page.snackBar.open).toHaveBeenCalledWith('Updated', '', expect.anything());
    });

    it('does not take the rows that were saved for unsaved, whatever the page reads next', () => {
      const row = editedRow(1);
      const page = pageOf('valid', {rows: [row]});
      // the rows are read again after the save; until they are, they are the rows that were edited
      page.loadBieListForOasDoc = vi.fn();

      page.doUpdate();

      expect(row.isChanged).toBe(false);
      expect(page.isChanged()).toBe(false);
    });

    it('asks whether a BIE is reused across operations only when operations were saved', () => {
      const withOperations = pageOf('valid', {rows: [editedRow(1)]});
      withOperations.doUpdate();
      expect(withOperations.openAPIService.checkBIEReusedAcrossOperationsAfterUpdate).toHaveBeenCalledTimes(1);

      const documentOnly = pageOf('valid');
      documentOnly.oasDoc.title = 'another';
      documentOnly.doUpdate();
      expect(documentOnly.openAPIService.checkBIEReusedAcrossOperationsAfterUpdate).not.toHaveBeenCalled();
    });

    it('shows where it is wrong when the server refuses, and keeps what the user changed', () => {
      const violations = [{scope: 'OPERATION', oasOperationId: 7, field: 'operationId', message: 'The operation ID is used.'}];
      const page = pageOf('valid', {saveChanges: () => throwError(() => ({status: 400, error: {valid: false, violations}}))});
      page.oasDoc.title = 'another';

      page.doUpdate();

      expect(page.changeChecker.status()).toBe('invalid');
      expect(page.changeChecker.violations()).toEqual(violations);
      expect(page.snackBar.open).toHaveBeenCalledWith('The operation ID is used.', '', expect.anything());
      expect(page.openAPIService.getOasDoc).not.toHaveBeenCalled();
      expect(page.oasDoc.title).toBe('another');
      expect(page.loading).toBe(false);
    });
  });
});

describe('OasDocDetailComponent.generate guards unsaved changes (#1610)', () => {
  const sortPage = {sortActives: ['tagName'], sortDirections: ['asc']};

  function generateCtx(changed: boolean): any {
    const ctx: any = {
      isChanged: () => changed,
      snackBar: {open: vi.fn()},
      openAPIService: {generateOpenAPI: vi.fn().mockReturnValue({subscribe: vi.fn()})},
      oasDoc: {oasDocId: 123},
      sortRequest: () => sortPage,
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
    expect(ctx.openAPIService.generateOpenAPI).toHaveBeenCalledWith(123, sortPage);
    expect(ctx.loading).toBe(true);
  });
});

describe('OasDocDetailComponent.update duplicate check (same check as Create)', () => {
  function ctx(unique: boolean): any {
    const c: any = {
      oasDoc: {oasDocId: 7, title: 'dup title', openAPIVersion: '3.1', version: '1.0', licenseName: 'MIT'},
      openAPIService: {checkUniqueness: vi.fn(() => of(unique))},
      doUpdate: vi.fn(),
      openDialogOasDocUpdate: vi.fn(),
    };
    c.checkUniqueness = OasDocDetailComponent.prototype.checkUniqueness;
    return c;
  }

  it('opens the duplicate dialog and does not update when the check fails', () => {
    const c = ctx(false);
    OasDocDetailComponent.prototype.update.call(c);
    expect(c.openAPIService.checkUniqueness).toHaveBeenCalledWith(c.oasDoc);
    expect(c.openDialogOasDocUpdate).toHaveBeenCalledTimes(1);
    expect(c.doUpdate).not.toHaveBeenCalled();
  });

  it('updates when the check passes', () => {
    const c = ctx(true);
    OasDocDetailComponent.prototype.update.call(c);
    expect(c.doUpdate).toHaveBeenCalledTimes(1);
    expect(c.openDialogOasDocUpdate).not.toHaveBeenCalled();
  });

  it('opens a dialog that does not mention the License Name', () => {
    const dialogConfig: any = {data: {}};
    const open = vi.fn(() => ({afterClosed: () => of(undefined)}));
    const c: any = {confirmDialogService: {newConfig: () => dialogConfig, open}};
    OasDocDetailComponent.prototype.openDialogOasDocUpdate.call(c);
    expect(dialogConfig.data.content[0]).not.toMatch(/license/i);
  });
});

describe('OasDocDetailComponent.handleKeyboardEvent Ctrl/Cmd+S', () => {
  function saveCtx(changed: boolean, disabled: boolean): any {
    const ctx: any = {
      oasDoc: {},
      isChanged: () => changed,
      isDisabled: () => disabled,
      update: vi.fn(),
    };
    ctx.handleKeyboardEvent = OasDocDetailComponent.prototype.handleKeyboardEvent;
    return ctx;
  }

  function press(ctx: any, init: Partial<KeyboardEvent> = {key: 's', ctrlKey: true}) {
    const event: any = {preventDefault: vi.fn(), stopPropagation: vi.fn(), ...init};
    ctx.handleKeyboardEvent(event);
    return event;
  }

  it('saves when the document is changed and the Update button would be enabled', () => {
    const ctx = saveCtx(true, false);
    press(ctx);
    expect(ctx.update).toHaveBeenCalledTimes(1);
  });

  it('does not save while the Update button is disabled (blank Title or Version, invalid security)', () => {
    const ctx = saveCtx(true, true);
    const event = press(ctx);
    expect(ctx.update).not.toHaveBeenCalled();
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('does not save when nothing has changed', () => {
    const ctx = saveCtx(false, false);
    press(ctx);
    expect(ctx.update).not.toHaveBeenCalled();
  });

  it('ignores other keys', () => {
    const ctx = saveCtx(true, false);
    press(ctx, {key: 'a', ctrlKey: true});
    expect(ctx.update).not.toHaveBeenCalled();
  });

  describe('Terms of Service, Contact URL, Contact Email and License URL are validated', () => {
    const doc = {oasDocId: 1, title: 'T', openAPIVersion: '3.1', version: '1'};

    function isDisabledCtx(values: {tos?: string; contactUrl?: string; email?: string; licenseUrl?: string}): boolean {
      const ctx: any = {
        disabled: false,
        termServiceUrl: new FormControl(values.tos ?? '', [httpUrlValidator]),
        contactUrl: new FormControl(values.contactUrl ?? '', [httpUrlValidator]),
        contactEmail: new FormControl(values.email ?? '', [optionalEmailValidator]),
        licenseUrl: new FormControl(values.licenseUrl ?? '', [httpUrlValidator]),
        areSecuritySchemesValid: () => true,
        areSecurityRequirementsValid: () => true,
      };
      return OasDocDetailComponent.prototype.isDisabled.call(ctx, doc as any);
    }

    it('enables Update for empty or valid values', () => {
      expect(isDisabledCtx({})).toBe(false);
      expect(isDisabledCtx({
        tos: 'https://example.com/tos', contactUrl: 'http://example.com', email: 'a@b.com',
        licenseUrl: 'https://www.apache.org/licenses/LICENSE-2.0'
      })).toBe(false);
    });

    it('disables Update for an invalid value of any of the four fields', () => {
      expect(isDisabledCtx({tos: 'not a url'})).toBe(true);
      expect(isDisabledCtx({contactUrl: 'javascript:alert(1)'})).toBe(true);
      expect(isDisabledCtx({email: 'not-an-email'})).toBe(true);
      expect(isDisabledCtx({licenseUrl: 'ftp//bad url'})).toBe(true);
    });
  });
});

describe('OasDocDetailComponent Enable / Disable traceparent (#1791)', () => {
  const proto: any = OasDocDetailComponent.prototype;

  /** A `this` with only the state init(), isChanged() and the Enable / Disable actions touch. */
  function ctx(operationIdsOfTheDocument: number[], visibleRows: number[] = operationIdsOfTheDocument): any {
    const requests: any[] = [];
    const c: any = {
      titleService: {setTitle: vi.fn()},
      snackBar: {open: vi.fn()},
      isUpdating: true,
      hashCodeForOasDoc: 0,
      operationIds: [],
      rows: visibleRows.map(oasOperationId => ({oasOperationId, httpHeaders: []})),
      requests,
      openAPIService: {
        getBieForOasDocPage: vi.fn((oasDocId: number, params: any) => {
          requests.push({oasDocId, params});
          // the two rows of one operation (a request and a response) are two rows with one operation id
          return of({list: operationIdsOfTheDocument.flatMap(oasOperationId => [{oasOperationId}, {oasOperationId}])});
        })
      },
    };
    for (const method of ['getChanged', 'isChanged', 'init', 'showValidationErrors', 'refreshOperationIds',
      'fetchOperationIds', 'enableTraceparent', 'disableTraceparent']) {
      c[method] = proto[method];
    }
    for (const accessor of ['canEnableTraceparent', 'canDisableTraceparent']) {
      Object.defineProperty(c, accessor, Object.getOwnPropertyDescriptor(proto, accessor) as PropertyDescriptor);
    }
    for (const method of ['fieldsHash', 'changeFingerprint']) {
      c[method] = proto[method];
    }
    c.edits = new EditedRows<any>(row => row.oasOperationId + '|' + row.messageBody, row => row.isChanged);
    c.headerEdits = new OasDocHeaderEdits();
    c.changeChecker = {reset: vi.fn(), request: vi.fn(), status: () => 'idle'};
    c.contactUrl = new FormControl('', [httpUrlValidator]);
    c.contactEmail = new FormControl('', [optionalEmailValidator]);
    c.licenseUrl = new FormControl('', [httpUrlValidator]);
    c.termServiceUrl = new FormControl('', [httpUrlValidator]);
    return c;
  }

  function loaded(c: any, httpHeaders?: any[]): any {
    c.init({oasDocId: 5, title: 't', version: '1', openAPIVersion: '3.1', httpHeaders});
    return c;
  }

  const names = (c: any) => c.oasDoc.httpHeaders.map((h: any) => h.name + '@' + h.oasOperationId + (h.direction === 'REQUEST' ? ':req' : ':res'));
  const saved = (c: any) => JSON.parse(JSON.stringify(c.oasDoc.httpHeaders));

  describe('what is offered', () => {
    it('learns the operations of the document, of every page, when the document is loaded', () => {
      const c = loaded(ctx([1, 2, 3, 4], [1, 2]));

      expect(c.operationIds).toEqual([1, 2, 3, 4]);
      expect(c.requests[0].oasDocId).toBe(5);
      expect(c.requests[0].params.get('pageIndex')).toBe('-1');
      expect(c.requests[0].params.get('pageSize')).toBe('-1');
    });

    it('offers Enable, not Disable, while no operation has the header', () => {
      const c = loaded(ctx([1, 2]));
      expect(c.canEnableTraceparent).toBe(true);
      expect(c.canDisableTraceparent).toBe(false);
    });

    it('offers Enable and Disable while only some operations have it', () => {
      const first = loaded(ctx([1, 2, 3]));
      first.enableTraceparent();
      const c = loaded(ctx([1, 2, 3, 4]), saved(first)); // a BIE was added since

      expect(c.canEnableTraceparent).toBe(true);
      expect(c.canDisableTraceparent).toBe(true);
    });

    it('offers Disable, not Enable, once every operation has it', () => {
      const first = loaded(ctx([1, 2]));
      first.enableTraceparent();
      const c = loaded(ctx([1, 2]), saved(first));

      expect(c.canEnableTraceparent).toBe(false);
      expect(c.canDisableTraceparent).toBe(true);
    });

    it('offers neither Enable nor Disable for a document without operations and headers', () => {
      const c = loaded(ctx([]));
      expect(c.canEnableTraceparent).toBe(false);
      expect(c.canDisableTraceparent).toBe(false);
    });

    it('treats a header of the whole document as every operation having it', () => {
      const wide = [{name: 'traceparent', schema: {type: 'string'}, direction: 'REQUEST', required: false}, {name: 'traceparent', schema: {type: 'string'}, direction: 'RESPONSE', required: true}];
      const c = loaded(ctx([1, 2]), wide);

      expect(c.canEnableTraceparent).toBe(false);
      expect(c.canDisableTraceparent).toBe(true);
    });
  });

  describe('Enable', () => {
    it('fills the headers of every operation, which is a change of the document that Update saves', () => {
      const c = loaded(ctx([1, 2, 3]));
      expect(c.isChanged()).toBe(false);

      c.enableTraceparent();

      expect(names(c)).toEqual(['traceparent@1:req', 'traceparent@1:res', 'traceparent@2:req', 'traceparent@2:res', 'traceparent@3:req', 'traceparent@3:res']);
      expect(c.isChanged()).toBe(true);
      expect(c.snackBar.open.mock.calls[0][0]).toContain('added (3 request parameters and 3 response headers)');
    });

    it('reaches the operations of every page, not only those the table shows', () => {
      const c = loaded(ctx([1, 2, 3, 4], [1, 2]));
      c.enableTraceparent();

      expect(names(c)).toEqual(['traceparent@1:req', 'traceparent@1:res', 'traceparent@2:req', 'traceparent@2:res', 'traceparent@3:req', 'traceparent@3:res', 'traceparent@4:req', 'traceparent@4:res']);
    });

    it('shows the headers in the rows on the page, as the objects of the document', () => {
      const c = loaded(ctx([1, 2], [1]));
      c.enableTraceparent();

      expect(c.rows[0].httpHeaders.length).toBe(2);
      expect(c.rows[0].httpHeaders[0]).toBe(c.oasDoc.httpHeaders[0]);
    });

    it('adds only to the operations that lack it, and then no longer offers Enable', () => {
      const first = loaded(ctx([1, 2]));
      first.enableTraceparent();
      const c = loaded(ctx([1, 2, 3]), saved(first));

      c.enableTraceparent();

      expect(names(c)).toEqual(['traceparent@1:req', 'traceparent@1:res', 'traceparent@2:req', 'traceparent@2:res', 'traceparent@3:req', 'traceparent@3:res']);
      expect(c.isChanged()).toBe(true);
      expect(c.snackBar.open.mock.calls[0][0]).toContain('added (1 request parameter and 1 response header)');
      expect(c.canEnableTraceparent).toBe(false);
    });

    it('leaves the other headers of the document alone', () => {
      const other = {name: 'x-request-id', oasOperationId: 1, schema: {type: 'string'}, direction: 'REQUEST', required: false};
      const c = loaded(ctx([1, 2]), [other]);
      c.enableTraceparent();

      expect(names(c)).toEqual(['traceparent@1:req', 'traceparent@1:res', 'x-request-id@1:req', 'traceparent@2:req', 'traceparent@2:res']);
    });

    it('sends the headers of the document to the server as the request body', () => {
      const c = loaded(ctx([1]));
      c.enableTraceparent();
      const sent = JSON.parse(JSON.stringify(c.oasDoc));

      expect(sent.httpHeaders.length).toBe(2);
      expect(sent.httpHeaders[0].oasOperationId).toBe(1);
      expect(sent.httpHeaders.map((h: any) => [h.direction, h.required])).toEqual([['REQUEST', false], ['RESPONSE', true]]);
      expect(typeof sent.httpHeaders[0].schema).toBe('object');
    });
  });

  describe('Disable', () => {
    function enabled(operationIds: number[], extra: any[] = []): any {
      const first = loaded(ctx(operationIds), extra);
      first.enableTraceparent();
      return loaded(ctx(operationIds), saved(first));
    }

    it('takes the header away from every operation, which is a change of the document that Update saves', () => {
      const c = enabled([1, 2, 3]);
      expect(c.isChanged()).toBe(false);

      c.disableTraceparent();

      expect(c.oasDoc.httpHeaders).toEqual([]);
      expect(c.isChanged()).toBe(true);
      expect(c.snackBar.open.mock.calls[0][0]).toContain('removed (3 request parameters and 3 response headers)');
    });

    it('takes a header of the whole document away too, and leaves the other headers alone', () => {
      const other = {name: 'x-request-id', oasOperationId: 1, schema: {type: 'string'}, direction: 'REQUEST', required: false};
      const wide = [{name: 'traceparent', schema: {type: 'string'}, direction: 'REQUEST', required: false}, {name: 'traceparent', schema: {type: 'string'}, direction: 'RESPONSE', required: true}];
      const c = loaded(ctx([1, 2]), [other, ...wide]);

      c.disableTraceparent();

      expect(names(c)).toEqual(['x-request-id@1:req']);
    });

    it('empties the headers of the rows, and then offers Enable and no longer Disable', () => {
      const c = enabled([1, 2]);
      expect(c.canDisableTraceparent).toBe(true);
      c.disableTraceparent();

      expect(c.rows.every((row: any) => row.httpHeaders.length === 0)).toBe(true);
      expect(c.canEnableTraceparent).toBe(true);
      expect(c.canDisableTraceparent).toBe(false);
    });

    it('does not touch the server: nothing is saved until Update', () => {
      const c = enabled([1, 2]);
      const calls = c.openAPIService.getBieForOasDocPage.mock.calls.length;
      c.disableTraceparent();

      // only the list of the operations is read, and that at load: Disable reads nothing, and writes nothing
      expect(c.openAPIService.getBieForOasDocPage.mock.calls.length).toBe(calls);
      expect(Object.keys(c.openAPIService)).toEqual(['getBieForOasDocPage']);
    });
  });

  describe('Enable and Disable undo each other', () => {
    it('Enable and then Disable is no change of a document without headers', () => {
      const c = loaded(ctx([1, 2, 3]));
      c.enableTraceparent();
      expect(c.isChanged()).toBe(true);

      c.disableTraceparent();
      expect(c.isChanged()).toBe(false);
    });

    it('Disable and then Enable is no change of a document that has the headers', () => {
      const first = loaded(ctx([3, 1, 2]));
      first.enableTraceparent();
      const c = loaded(ctx([3, 1, 2]), saved(first));

      c.disableTraceparent();
      expect(c.isChanged()).toBe(true);
      c.enableTraceparent();
      expect(c.isChanged()).toBe(false);
    });

    it('Disable and then Enable keeps the other headers where they were', () => {
      const other = {name: 'x-request-id', oasOperationId: 2, schema: {type: 'string'}, direction: 'REQUEST', required: false};
      const first = loaded(ctx([1, 2, 3]), [other]);
      first.enableTraceparent();
      const c = loaded(ctx([1, 2, 3]), saved(first));

      c.disableTraceparent();
      c.enableTraceparent();

      expect(c.isChanged()).toBe(false);
      expect(names(c)).toEqual(['traceparent@1:req', 'traceparent@1:res', 'traceparent@2:req', 'traceparent@2:res', 'x-request-id@2:req', 'traceparent@3:req', 'traceparent@3:res']);
    });
  });
});

describe('OasDocDetailComponent.init shows what keeps Update disabled', () => {
  const proto: any = OasDocDetailComponent.prototype;

  function ctx(): any {
    const c: any = {
      titleService: {setTitle: vi.fn()}, isUpdating: true, operationIds: [], rows: [],
      openAPIService: {getBieForOasDocPage: vi.fn(() => of({list: []}))},
    };
    c.init = proto.init;
    c.refreshOperationIds = proto.refreshOperationIds;
    c.fetchOperationIds = proto.fetchOperationIds;
    c.showValidationErrors = proto.showValidationErrors;
    c.fieldsHash = proto.fieldsHash;
    c.changeFingerprint = proto.changeFingerprint;
    c.edits = new EditedRows<any>(row => row.oasOperationId + '|' + row.messageBody, row => row.isChanged);
    c.headerEdits = new OasDocHeaderEdits();
    c.changeChecker = {reset: vi.fn(), request: vi.fn()};
    c.contactUrl = new FormControl('', [httpUrlValidator]);
    c.contactEmail = new FormControl('', [optionalEmailValidator]);
    c.licenseUrl = new FormControl('', [httpUrlValidator]);
    c.termServiceUrl = new FormControl('', [httpUrlValidator]);
    return c;
  }

  it('touches the validated fields, so that an invalid value in the loaded document shows its error', () => {
    const c = ctx();
    c.termServiceUrl.setValue('Terms'); // a Terms of Service that is not a URL, as in a document of production data
    expect(c.termServiceUrl.touched).toBe(false);

    c.init({oasDocId: 5, title: 't', version: '1', openAPIVersion: '3.1', termsOfService: 'Terms'});

    for (const control of [c.contactUrl, c.contactEmail, c.licenseUrl, c.termServiceUrl]) {
      expect(control.touched).toBe(true);
    }
    expect(c.termServiceUrl.invalid && c.termServiceUrl.touched).toBe(true);
    expect(c.termServiceUrl.hasError('url')).toBe(true);
  });

  it('does not change a value, nor the baseline: loading is not a change', () => {
    const c = ctx();
    c.termServiceUrl.setValue('Terms');
    c.init({oasDocId: 5, title: 't', version: '1', openAPIVersion: '3.1', termsOfService: 'Terms'});

    expect(c.termServiceUrl.value).toBe('Terms');
    expect(c.oasDoc.termsOfService).toBe('Terms');
    expect(c.hashCodeForOasDoc).toBe(hashCode(c.oasDoc));
  });
});

describe('OasDocDetailComponent.locationLabel (#1791)', () => {
  const label = (OasDocDetailComponent.prototype as any).locationLabel;
  const base = {name: 'x', schema: {}, required: false};

  it('names the location of a request parameter unless it is in a header', () => {
    expect(label({...base, direction: 'REQUEST', in: 'query'})).toBe(' (query)');
    expect(label({...base, direction: 'REQUEST', in: 'path'})).toBe(' (path)');
    expect(label({...base, direction: 'REQUEST', in: 'cookie'})).toBe(' (cookie)');
    expect(label({...base, direction: 'REQUEST', in: 'header'})).toBe('');
    expect(label({...base, direction: 'REQUEST'})).toBe('');
    expect(label({...base, direction: 'RESPONSE'})).toBe('');
  });
});

describe('OasDocDetailComponent HTTP header dialog (#1791)', () => {
  const proto: any = OasDocDetailComponent.prototype;

  /** A `this` with the document, its rows, and a dialog that answers what it is told to. */
  function ctx(answer: any, httpHeaders: any[] = []): any {
    const opened: any[] = [];
    const c: any = {
      oasDoc: {oasDocId: 5, title: 't', httpHeaders},
      rows: [{oasOperationId: 1, operationId: 'getA', httpHeaders: []}, {oasOperationId: 2, operationId: 'getB', httpHeaders: []}],
      opened,
      dialog: {
        open: vi.fn((component: any, config: any) => {
          opened.push(config.data);
          return {afterClosed: () => of(answer)};
        })
      },
    };
    c.addHttpHeader = proto.addHttpHeader;
    c.editHttpHeader = proto.editHttpHeader;
    c.openHttpHeaderDialog = proto.openHttpHeaderDialog;
    linkHeadersToRows(c.oasDoc, c.rows);
    return c;
  }

  const header = (name: string, oasOperationId?: number, over: any = {}) => ({
    name, oasOperationId, schema: {type: 'string'}, direction: 'REQUEST', required: false, ...over
  });
  const names = (c: any) => c.oasDoc.httpHeaders.map((h: any) => h.name + '@' + h.oasOperationId + (h.direction === 'REQUEST' ? ':req' : ':res'));

  describe('adding with the + of a row', () => {
    it('opens the dialog with a new header for the operation of the row', () => {
      const c = ctx(undefined);
      c.addHttpHeader(c.rows[1], 'REQUEST');

      expect(c.opened[0].isNew).toBe(true);
      expect(c.opened[0].header.oasOperationId).toBe(2);
      expect(c.opened[0].header.name).toBe('');
      expect(c.opened[0].header.direction).toBe('REQUEST');
      expect(c.opened[0].scopeLabel).toContain('getB');
    });

    it('opens it for a parameter that is in the query at first', () => {
      const c = ctx(undefined);
      c.addHttpHeader(c.rows[0], 'REQUEST');
      expect(c.opened[0].header.in).toBe('query');
    });

    it('opens it for a response header when the + of the Response Headers column is clicked', () => {
      const c = ctx(undefined);
      c.addHttpHeader(c.rows[0], 'RESPONSE');
      expect(c.opened[0].header.direction).toBe('RESPONSE');
    });

    it('lets an operation that has no Request row, as a GET, get a request parameter', () => {
      const c = ctx({action: 'save', header: header('traceparent', 1)});
      c.addHttpHeader(c.rows[0], 'REQUEST');
      expect(names(c)).toEqual(['traceparent@1:req']);
    });

    it('adds what the dialog returns to the document and to the rows of the operation', () => {
      const c = ctx({action: 'save', header: header('x-request-id', 2)});
      c.addHttpHeader(c.rows[1], 'REQUEST');

      expect(names(c)).toEqual(['x-request-id@2:req']);
      expect(c.rows[1].httpHeaders[0]).toBe(c.oasDoc.httpHeaders[0]);
      expect(c.rows[0].httpHeaders).toEqual([]);
    });

    it('adds nothing when the dialog is cancelled', () => {
      const c = ctx(undefined);
      c.addHttpHeader(c.rows[0], 'REQUEST');
      expect(c.oasDoc.httpHeaders).toEqual([]);
    });

    it('keeps the headers in one order', () => {
      const c = ctx({action: 'save', header: header('a', 1)}, [header('b', 2), header('z', 1)]);
      c.addHttpHeader(c.rows[0], 'REQUEST');
      expect(names(c)).toEqual(['a@1:req', 'z@1:req', 'b@2:req']);
    });
  });

  describe('editing with a click on a header', () => {
    it('opens the dialog with a copy of the header, and the other headers to tell a clash', () => {
      const traceparent = header('traceparent', 1);
      const other = header('x-other', 1);
      const c = ctx(undefined, [traceparent, other]);
      c.editHttpHeader(c.rows[0], traceparent);

      expect(c.opened[0].isNew).toBe(false);
      expect(c.opened[0].header).toEqual(traceparent);
      expect(c.opened[0].header).not.toBe(traceparent);
      expect(c.opened[0].others).toEqual([other]);
    });

    it('changes the header in the document, which stays the same object, so the rows still show it', () => {
      const traceparent = header('traceparent', 1);
      const c = ctx({action: 'save', header: header('traceparent', 1, {required: true, description: 'changed'})}, [traceparent]);
      c.editHttpHeader(c.rows[0], traceparent);

      expect(c.oasDoc.httpHeaders[0]).toBe(traceparent);
      expect(traceparent.required).toBe(true);
      expect((traceparent as any).description).toBe('changed');
      expect(c.rows[0].httpHeaders[0]).toBe(traceparent);
    });

    it('leaves the header as it is when the dialog is cancelled', () => {
      const traceparent = header('traceparent', 1);
      const c = ctx(undefined, [traceparent]);
      c.editHttpHeader(c.rows[0], traceparent);
      expect(traceparent).toEqual(header('traceparent', 1));
    });

    it('says so when the header is one of the whole document', () => {
      const wide = header('traceparent');
      const c = ctx(undefined, [wide]);
      c.editHttpHeader(c.rows[0], wide);
      expect(c.opened[0].scopeLabel).toContain('whole document');
    });
  });

  describe('deleting from the dialog', () => {
    it('takes the header out of the document and of the rows, and leaves the others', () => {
      const traceparent = header('traceparent', 1);
      const other = header('x-other', 1);
      const c = ctx({action: 'delete'}, [traceparent, other]);
      c.editHttpHeader(c.rows[0], traceparent);

      expect(c.oasDoc.httpHeaders).toEqual([other]);
      expect(c.rows[0].httpHeaders).toEqual([other]);
    });
  });
});
