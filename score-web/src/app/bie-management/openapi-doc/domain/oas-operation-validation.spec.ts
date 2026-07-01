import {
  applyOasSuffix,
  oasBodySlotKey,
  oasOperationIdKey,
  oasOperationKey,
  OasOperationRow,
  OasOperationValidator,
} from './oas-operation-validation';

/**
 * Shared OpenAPI operation-uniqueness validator (issues #1757 / #1492 / #1732 / #1519).
 *
 * The single source of truth used by BOTH the OpenAPI Document editor (oas-doc-detail) and the
 * BIE-root OpenAPI bindings panel (bie-edit). These tests pin the "operationId is a duplicate only
 * when it spans 2+ DISTINCT (Resource Name, Verb) operations" rule and, crucially, the #1757 fix:
 * the distinct-operation identity is (Resource Name, Verb) — never the oas_operation_id row PK — so a
 * Request+Response pair collapses to one operation even when it is backed by two operation rows.
 */

function row(o: Partial<OasOperationRow>): OasOperationRow {
  return {
    oasDocId: o.oasDocId ?? 1,
    resourceName: o.resourceName ?? '',
    verb: o.verb ?? '',
    messageBody: o.messageBody ?? '',
    operationId: o.operationId ?? '',
  };
}

describe('oas-operation-validation key builders', () => {
  it('oasOperationIdKey is (document | trimmed operationId)', () => {
    expect(oasOperationIdKey(row({oasDocId: 3, operationId: '  createOrder '}))).toBe('3|createOrder');
  });

  it('oasOperationKey is the (trimmed Resource Name | Verb) operation identity — message body excluded', () => {
    expect(oasOperationKey(row({resourceName: ' /orders ', verb: 'POST', messageBody: 'Request'})))
      .toBe('/orders|POST');
  });

  it('oasBodySlotKey is (document | trimmed Resource Name | Verb | Message Body)', () => {
    expect(oasBodySlotKey(row({oasDocId: 2, resourceName: ' /orders ', verb: 'POST', messageBody: 'Request'})))
      .toBe('2|/orders|POST|Request');
  });
});

describe('applyOasSuffix', () => {
  it('adds the suffix when present and not already there', () => {
    expect(applyOasSuffix('createOrder', 'List', true)).toBe('createOrderList');
    expect(applyOasSuffix('createOrderList', 'List', true)).toBe('createOrderList'); // idempotent
  });

  it('removes the suffix when absent', () => {
    expect(applyOasSuffix('createOrderList', 'List', false)).toBe('createOrder');
    expect(applyOasSuffix('createOrder', 'List', false)).toBe('createOrder'); // idempotent
  });

  it('tolerates a null/undefined value', () => {
    expect(applyOasSuffix(undefined as any, 'List', true)).toBe('List');
    expect(applyOasSuffix(null as any, 'List', false)).toBe('');
  });
});

describe('OasOperationValidator operationId uniqueness', () => {
  // S1 — the exact reported #1757 case: one endpoint with a Request BIE and a Response BIE surfaces as
  // two rows sharing operationId 'deleteExpenseReport'. Must NOT be flagged.
  it('does NOT flag a Request + Response of one operation sharing one operationId', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/test/1/expense-report', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteExpenseReport'}),
      row({resourceName: '/test/1/expense-report', verb: 'DELETE', messageBody: 'Response', operationId: 'deleteExpenseReport'}),
    ]);
    expect(v.duplicateOperationIds.size).toBe(0);
  });

  // S1b — the #1757 root cause: the SAME logical operation backed by TWO different oas_operation rows
  // (legacy pre-#1492 data). Because the validator keys on (Resource Name, Verb) and NEVER on the row
  // PK, this legit pair still collapses to one operation. (The OasOperationRow shape has no
  // oas_operation_id field, which structurally guarantees the PK cannot be consulted.)
  it('does NOT flag a Request + Response even when they would map to two separate operation rows', () => {
    const v = new OasOperationValidator();
    const rows = [
      row({resourceName: '/wip-status', verb: 'GET', messageBody: 'Request', operationId: 'getWipStatus'}),
      row({resourceName: '/wip-status', verb: 'GET', messageBody: 'Response', operationId: 'getWipStatus'}),
    ];
    v.recompute(rows);
    expect(v.duplicateOperationIds.size).toBe(0);
    expect(v.isDuplicateOperationId(rows[0])).toBe(false);
    expect(v.isDuplicateOperationId(rows[1])).toBe(false);
  });

  it('flags one operationId reused on two DIFFERENT endpoints (different resourceName)', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteThing'}),
      row({resourceName: '/b', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteThing'}),
    ]);
    expect(v.duplicateOperationIds.has('1|deleteThing')).toBe(true);
  });

  it('flags one operationId across two operations that differ only by verb', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/orders', verb: 'POST', messageBody: 'Request', operationId: 'orderThing'}),
      row({resourceName: '/orders', verb: 'PUT', messageBody: 'Request', operationId: 'orderThing'}),
    ]);
    expect(v.duplicateOperationIds.has('1|orderThing')).toBe(true);
  });

  it('flags one operationId across two operations even when message bodies differ (body excluded from op key)', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteThing'}),
      row({resourceName: '/b', verb: 'DELETE', messageBody: 'Response', operationId: 'deleteThing'}),
    ]);
    expect(v.duplicateOperationIds.has('1|deleteThing')).toBe(true);
  });

  it('does not flag distinct operationIds on different endpoints', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteA'}),
      row({resourceName: '/b', verb: 'DELETE', messageBody: 'Request', operationId: 'deleteB'}),
    ]);
    expect(v.duplicateOperationIds.size).toBe(0);
  });

  it('ignores rows with a blank operationId (the required check owns empties, not uniqueness)', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/a', verb: 'DELETE', messageBody: 'Request', operationId: ''}),
      row({resourceName: '/b', verb: 'DELETE', messageBody: 'Request', operationId: '   '}),
    ]);
    expect(v.duplicateOperationIds.size).toBe(0);
  });

  it('uses a trimmed (Resource Name, Verb) key so one endpoint is not split by whitespace', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: ' /orders ', verb: 'POST', messageBody: 'Request', operationId: 'createOrder'}),
      row({resourceName: '/orders', verb: 'POST', messageBody: 'Response', operationId: 'createOrder'}),
    ]);
    expect(v.duplicateOperationIds.size).toBe(0);
  });

  // #1519: the BIE-root panel spans multiple documents. The same operationId in two DIFFERENT
  // documents is NOT a collision (uniqueness is per document).
  it('does NOT flag the same operationId reused across two different documents', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({oasDocId: 1, resourceName: '/a', verb: 'GET', messageBody: 'Response', operationId: 'getThing'}),
      row({oasDocId: 2, resourceName: '/a', verb: 'GET', messageBody: 'Response', operationId: 'getThing'}),
    ]);
    expect(v.duplicateOperationIds.size).toBe(0);
  });

  it('still flags a real collision WITHIN one document while the panel spans several', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({oasDocId: 1, resourceName: '/a', verb: 'GET', messageBody: 'Response', operationId: 'getThing'}),
      row({oasDocId: 1, resourceName: '/b', verb: 'GET', messageBody: 'Response', operationId: 'getThing'}),
      row({oasDocId: 2, resourceName: '/a', verb: 'GET', messageBody: 'Response', operationId: 'getThing'}),
    ]);
    expect(v.duplicateOperationIds.has('1|getThing')).toBe(true);
    expect(v.duplicateOperationIds.has('2|getThing')).toBe(false);
  });

  it('operationIdErrorStateMatcher is in error for an empty value and for a duplicate, not for a clean id', () => {
    const v = new OasOperationValidator();
    const dupA = row({resourceName: '/a', verb: 'GET', messageBody: 'Response', operationId: 'getThing'});
    const dupB = row({resourceName: '/b', verb: 'GET', messageBody: 'Response', operationId: 'getThing'});
    const empty = row({resourceName: '/c', verb: 'GET', messageBody: 'Response', operationId: ''});
    const ok = row({resourceName: '/d', verb: 'GET', messageBody: 'Response', operationId: 'getOther'});
    v.recompute([dupA, dupB, empty, ok]);
    expect(v.operationIdErrorStateMatcher(dupA).isErrorState(null as any, null as any)).toBe(true);
    expect(v.operationIdErrorStateMatcher(empty).isErrorState(null as any, null as any)).toBe(true);
    expect(v.operationIdErrorStateMatcher(ok).isErrorState(null as any, null as any)).toBe(false);
  });
});

describe('OasOperationValidator body-slot uniqueness (#1492 Option 2)', () => {
  it('does NOT flag a legitimate Request + Response on one endpoint (different message bodies)', () => {
    const v = new OasOperationValidator();
    const rows = [
      row({resourceName: '/orders', verb: 'POST', messageBody: 'Request'}),
      row({resourceName: '/orders', verb: 'POST', messageBody: 'Response'}),
    ];
    v.recompute(rows);
    expect(v.duplicateBodySlots.size).toBe(0);
    expect(v.isDuplicateBodySlot(rows[0])).toBe(false);
    expect(v.isDuplicateBodySlot(rows[1])).toBe(false);
  });

  it('flags a true duplicate (a 2nd Request on the same (Resource Name, Verb))', () => {
    const v = new OasOperationValidator();
    const dupA = row({resourceName: '/orders', verb: 'POST', messageBody: 'Request'});
    const dupB = row({resourceName: '/orders', verb: 'POST', messageBody: 'Request'});
    const other = row({resourceName: '/orders', verb: 'POST', messageBody: 'Response'});
    v.recompute([dupA, dupB, other]);
    expect(v.duplicateBodySlots.has('1|/orders|POST|Request')).toBe(true);
    expect(v.isDuplicateBodySlot(dupA)).toBe(true);
    expect(v.isDuplicateBodySlot(dupB)).toBe(true);
    expect(v.isDuplicateBodySlot(other)).toBe(false);
  });

  it('does not collide across DIFFERENT verbs on the same resource', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/orders', verb: 'POST', messageBody: 'Request'}),
      row({resourceName: '/orders', verb: 'PUT', messageBody: 'Request'}),
    ]);
    expect(v.duplicateBodySlots.size).toBe(0);
  });

  it('does not collide across different documents (same path/verb/body in two docs)', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({oasDocId: 1, resourceName: '/orders', verb: 'POST', messageBody: 'Request'}),
      row({oasDocId: 2, resourceName: '/orders', verb: 'POST', messageBody: 'Request'}),
    ]);
    expect(v.duplicateBodySlots.size).toBe(0);
  });

  it('ignores rows with no verb or no message body when counting', () => {
    const v = new OasOperationValidator();
    v.recompute([
      row({resourceName: '/orders', verb: '', messageBody: ''}),
      row({resourceName: '/orders', verb: '', messageBody: ''}),
    ]);
    expect(v.duplicateBodySlots.size).toBe(0);
  });

  it('bodySlotErrorStateMatcher reflects the duplicate set per row', () => {
    const v = new OasOperationValidator();
    const dup = row({resourceName: '/orders', verb: 'POST', messageBody: 'Request'});
    const ok = row({resourceName: '/orders', verb: 'POST', messageBody: 'Response'});
    v.recompute([dup, row({resourceName: '/orders', verb: 'POST', messageBody: 'Request'}), ok]);
    expect(v.bodySlotErrorStateMatcher(dup).isErrorState(null as any, null as any)).toBe(true);
    expect(v.bodySlotErrorStateMatcher(ok).isErrorState(null as any, null as any)).toBe(false);
  });
});
