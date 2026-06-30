import {BieOasDocAddDialogComponent} from './bie-oas-doc-add-dialog.component';

/**
 * Issue #1492 (parity with the OpenAPI Document editor's Add dialog): the BIE-root "Add to OpenAPI Document"
 * dialog pre-checks, client-side, that the chosen (Verb, Message Body) would not duplicate a body the BIE
 * already has on the SELECTED document, and disables Add when it would. The checks are pure functions of
 * `this`, so bind the prototype methods to a minimal data bag (no Angular wiring required).
 */
type Binding = {oasDocId: number; propertyTerm?: string; resourceName?: string; verb: string; messageBody: string};

function dialogCtx(opts: {
  propertyTerm: string;
  selectedDocId: number | null;
  verb: string;
  messageBody: string;
  existing: Binding[];
}): any {
  const ctx: any = {
    data: {propertyTerm: opts.propertyTerm, existingBindings: opts.existing},
    selectedOasDoc: opts.selectedDocId == null ? undefined : {oasDocId: opts.selectedDocId},
    verb: opts.verb,
    messageBody: opts.messageBody,
  };
  ctx.bodySlotKey = (BieOasDocAddDialogComponent.prototype as any).bodySlotKey;
  ctx.isDuplicateBodySlot = BieOasDocAddDialogComponent.prototype.isDuplicateBodySlot;
  ctx.isValid = Object.getOwnPropertyDescriptor(BieOasDocAddDialogComponent.prototype, 'isValid').get;
  return ctx;
}

describe('BieOasDocAddDialogComponent.isDuplicateBodySlot (#1492 add-time guard)', () => {
  const term = 'Purchase Order';

  it('flags a (Verb, Message Body) already bound to the selected document', () => {
    const ctx = dialogCtx({
      propertyTerm: term, selectedDocId: 7, verb: 'GET', messageBody: 'Response',
      existing: [{oasDocId: 7, propertyTerm: term, verb: 'GET', messageBody: 'Response'}],
    });
    expect(ctx.isDuplicateBodySlot()).toBe(true);
    expect(ctx.isValid.call(ctx)).toBe(false);
  });

  it('does NOT flag the same (Verb, Message Body) on a DIFFERENT document', () => {
    const ctx = dialogCtx({
      propertyTerm: term, selectedDocId: 7, verb: 'GET', messageBody: 'Response',
      existing: [{oasDocId: 99, propertyTerm: term, verb: 'GET', messageBody: 'Response'}],
    });
    expect(ctx.isDuplicateBodySlot()).toBe(false);
    expect(ctx.isValid.call(ctx)).toBe(true);
  });

  it('does NOT flag a different Message Body (Request vs Response) on the same (doc, verb)', () => {
    const ctx = dialogCtx({
      propertyTerm: term, selectedDocId: 7, verb: 'POST', messageBody: 'Request',
      existing: [{oasDocId: 7, propertyTerm: term, verb: 'POST', messageBody: 'Response'}],
    });
    expect(ctx.isDuplicateBodySlot()).toBe(false);
    expect(ctx.isValid.call(ctx)).toBe(true);
  });

  it('does NOT flag a different Verb on the same (doc, Message Body)', () => {
    const ctx = dialogCtx({
      propertyTerm: term, selectedDocId: 7, verb: 'POST', messageBody: 'Response',
      existing: [{oasDocId: 7, propertyTerm: term, verb: 'GET', messageBody: 'Response'}],
    });
    expect(ctx.isDuplicateBodySlot()).toBe(false);
  });

  it('is false (and Add stays gated on the other fields) when no document is selected yet', () => {
    const ctx = dialogCtx({
      propertyTerm: term, selectedDocId: null, verb: 'GET', messageBody: 'Response',
      existing: [{oasDocId: 7, propertyTerm: term, verb: 'GET', messageBody: 'Response'}],
    });
    expect(ctx.isDuplicateBodySlot()).toBe(false);
    expect(ctx.isValid.call(ctx)).toBe(false);
  });

  it('matches case-insensitively on the property term', () => {
    const ctx = dialogCtx({
      propertyTerm: 'purchase order', selectedDocId: 7, verb: 'GET', messageBody: 'Response',
      existing: [{oasDocId: 7, propertyTerm: 'Purchase Order', verb: 'GET', messageBody: 'Response'}],
    });
    expect(ctx.isDuplicateBodySlot()).toBe(true);
  });
});
