import {BieForOasDoc, toMinorOpenApiVersion} from './openapi-doc';

/**
 * Issue #1610 domain-level coverage of DELETE operation handling.
 *
 *  - BieForOasDoc change detection underpins the editor's generate() guard: switching an operation to
 *    DELETE and/or giving it a Request body must register as an unsaved change, otherwise the guard
 *    would let the user generate a stale (pre-edit) document.
 *
 * operationId / resource-path construction now lives in oas-operation-validation.spec.ts alongside the
 * rule itself. Pure domain logic — no Angular TestBed (see bie-flat-tree.spec.ts).
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

describe('toMinorOpenApiVersion (#1760)', () => {
  it('folds a legacy patch version to its minor family', () => {
    expect(toMinorOpenApiVersion('3.0.3')).toBe('3.0');
    expect(toMinorOpenApiVersion('3.1.1')).toBe('3.1');
    expect(toMinorOpenApiVersion('3.1.2')).toBe('3.1');
  });

  it('passes an already-minor version through unchanged', () => {
    expect(toMinorOpenApiVersion('3.0')).toBe('3.0');
    expect(toMinorOpenApiVersion('3.1')).toBe('3.1');
  });

  it('trims surrounding whitespace', () => {
    expect(toMinorOpenApiVersion('  3.1.1 ')).toBe('3.1');
  });

  it('leaves empty / undefined values untouched', () => {
    expect(toMinorOpenApiVersion('')).toBe('');
    expect(toMinorOpenApiVersion(undefined as any)).toBeUndefined();
    expect(toMinorOpenApiVersion(null as any)).toBeNull();
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
