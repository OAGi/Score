import {BieEditComponent} from './bie-edit.component';

describe('BieEditComponent', () => {
  it('should be defined', () => {
    expect(BieEditComponent).toBeTruthy();
  });
});

/**
 * Issue #1610 (parity with the OpenAPI Document editor's #1610 banner): the BIE-root "OpenAPI Document
 * Information" panel warns, per binding card, that a DELETE Request body is ignored when that binding's
 * owning OpenAPI Document targets OpenAPI 3.0.x. The check is pure, so bind the prototype method to a
 * minimal `this` and a plain binding bag (no Angular wiring required). The warning is read-only on the
 * BIE screen — the OpenAPI Version itself is changed on the OpenAPI Document screen.
 */
function deleteBodyIgnored(verb: string, messageBody: string, openAPIVersion: any): boolean {
  const ctx: any = {isOasBindingDeleteBodyIgnored: BieEditComponent.prototype.isOasBindingDeleteBodyIgnored};
  return ctx.isOasBindingDeleteBodyIgnored({verb, messageBody, openAPIVersion});
}

describe('BieEditComponent.isOasBindingDeleteBodyIgnored (#1610 per-card warning)', () => {
  it('is true for a DELETE + Request binding whose document targets 3.0.3', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.0.3')).toBe(true);
  });

  it('is false for the SAME binding when the document targets 3.1.1 (the body is honored)', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.1.1')).toBe(false);
  });

  it('treats any 3.1.x version (e.g. 3.1.0, padded) as honored', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.1.0')).toBe(false);
    expect(deleteBodyIgnored('DELETE', 'Request', '  3.1.1 ')).toBe(false);
  });

  it('is false for a DELETE + Response binding on 3.0.3 (only a Request body is dropped)', () => {
    expect(deleteBodyIgnored('DELETE', 'Response', '3.0.3')).toBe(false);
  });

  it('is false for a non-DELETE Request binding on 3.0.3 (e.g. POST)', () => {
    expect(deleteBodyIgnored('POST', 'Request', '3.0.3')).toBe(false);
  });

  it('warns when the version is missing/blank (not a 3.1 prefix)', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '')).toBe(true);
    expect(deleteBodyIgnored('DELETE', 'Request', undefined)).toBe(true);
  });
});
