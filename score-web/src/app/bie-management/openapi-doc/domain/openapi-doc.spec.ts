import {BieForOasDoc, buildOperationId, operationIdVerbWord, recomputeOperationId} from './openapi-doc';

/**
 * Issue #1610 domain-level coverage of DELETE operation handling.
 *
 *  - BieForOasDoc change detection underpins the editor's generate() guard: switching an operation to
 *    DELETE and/or giving it a Request body must register as an unsaved change, otherwise the guard
 *    would let the user generate a stale (pre-edit) document.
 *  - operationId construction for the DELETE verb ('delete<Name>[List]').
 *
 * Pure domain logic — no Angular TestBed (see bie-flat-tree.spec.ts).
 */

describe('BieForOasDoc change detection for DELETE edits (#1610)', () => {
  function persisted(): BieForOasDoc {
    const bie = new BieForOasDoc();
    bie.verb = 'GET';
    bie.messageBody = 'Response';
    bie.reset(); // baseline: as loaded from the server
    return bie;
  }

  it('is unchanged immediately after a reset (load) baseline', () => {
    expect(persisted().isChanged).toBe(false);
  });

  it('detects switching the verb to DELETE as a change', () => {
    const bie = persisted();
    bie.verb = 'DELETE';
    expect(bie.isChanged).toBe(true);
  });

  it('detects giving a DELETE operation a Request body as a change', () => {
    const bie = persisted();
    bie.verb = 'DELETE';
    bie.messageBody = 'Request';
    expect(bie.isChanged).toBe(true);
  });

  it('returns to unchanged once the new state is reset (re-persisted)', () => {
    const bie = persisted();
    bie.verb = 'DELETE';
    bie.messageBody = 'Request';
    bie.reset();
    expect(bie.isChanged).toBe(false);
  });
});

describe('BieForOasDoc error-response body type (#1347)', () => {
  it('defaults to NONE and round-trips through the save payload', () => {
    const bie = new BieForOasDoc();
    expect(bie.errorResponseBodyType).toBe('NONE');
    expect(bie.json.errorResponseBodyType).toBe('NONE');
    expect(bie.json.confirmMessageTopLevelAsbiepId).toBeUndefined();
  });

  it('detects an error-response body type change as unsaved', () => {
    const bie = new BieForOasDoc();
    bie.verb = 'POST';
    bie.messageBody = 'Request';
    bie.reset();
    expect(bie.isChanged).toBe(false);
    bie.errorResponseBodyType = 'PROBLEM_DETAILS';
    expect(bie.isChanged).toBe(true);
  });

  it('detects a ConfirmMessage BIE change as unsaved and carries it in the payload', () => {
    const bie = new BieForOasDoc();
    bie.errorResponseBodyType = 'CONFIRM_MESSAGE';
    bie.reset();
    expect(bie.isChanged).toBe(false);
    bie.confirmMessageTopLevelAsbiepId = 42;
    expect(bie.isChanged).toBe(true);
    expect(bie.json.confirmMessageTopLevelAsbiepId).toBe(42);
  });
});

describe('operationId construction for DELETE (#1610 DELETE operations)', () => {
  it('maps the DELETE verb to the "delete" word', () => {
    expect(operationIdVerbWord('DELETE')).toBe('delete');
  });

  it('builds delete<Name> for a single resource', () => {
    expect(buildOperationId('DELETE', 'Purchase Order', false)).toBe('deletePurchaseOrder');
  });

  it('appends List for an array resource', () => {
    expect(buildOperationId('DELETE', 'Purchase Order', true)).toBe('deletePurchaseOrderList');
  });

  it('swaps an existing verb word to delete while preserving the BIE name', () => {
    expect(recomputeOperationId('DELETE', 'queryPurchaseOrder', false)).toBe('deletePurchaseOrder');
    expect(recomputeOperationId('DELETE', 'createPurchaseOrderList', true)).toBe('deletePurchaseOrderList');
  });
});
