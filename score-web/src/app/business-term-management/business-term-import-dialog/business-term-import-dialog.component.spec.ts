import {HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {BusinessTermImportDialogComponent} from './business-term-import-dialog.component';

/**
 * #1754: the import dialog surfaces server errors for the parse + import steps via a single
 * errorMessage() helper whose precedence is: the X-Error-Message response header, then a plain
 * string err.error body, then a caller-supplied fallback. Borrowed from the prototype (the helper
 * reads only its arguments) so no Angular TestBed / component construction is needed.
 */
describe('BusinessTermImportDialogComponent.errorMessage precedence (#1754)', () => {
  const errorMessage: (err: unknown, fallback: string) => string =
    (BusinessTermImportDialogComponent.prototype as any).errorMessage;

  it('prefers the X-Error-Message response header', () => {
    const err = new HttpErrorResponse({
      error: 'body message',
      headers: new HttpHeaders({'X-Error-Message': 'header message'}),
    });
    expect(errorMessage(err, 'fallback')).toBe('header message');
  });

  it('falls back to a plain string err.error body when there is no header', () => {
    const err = new HttpErrorResponse({error: 'body message'});
    expect(errorMessage(err, 'fallback')).toBe('body message');
  });

  it('uses the fallback when neither a header nor a string body is present', () => {
    const err = new HttpErrorResponse({error: {some: 'object'}});
    expect(errorMessage(err, 'fallback')).toBe('fallback');
  });

  it('uses the fallback for a non-HTTP error', () => {
    expect(errorMessage(new Error('boom'), 'fallback')).toBe('fallback');
  });
});

/**
 * A business term is identified by its (name + External Reference URI) pair, the same rule the server,
 * Create and Edit apply. The preview flags only a repeated pair; terms that share just a URI are
 * distinct. Borrowed from the prototype with a minimal stand-in `this` (no TestBed).
 */
describe('BusinessTermImportDialogComponent duplicate (name + URI) pair', () => {
  const revalidateAll: () => void = (BusinessTermImportDialogComponent.prototype as any).revalidateAll;

  function row(id: number, businessTerm: string, externalReferenceUri: string): any {
    return {
      id, rowIndex: id, businessTerm, externalReferenceUri,
      externalReferenceId: '', definition: '', comment: '', errors: [], userDeselected: false,
    };
  }

  function validate(rows: any[]): any[] {
    const self: any = {rows, selection: {select: () => {}, deselect: () => {}}, validCount: 0, reviewCount: 0};
    revalidateAll.call(self);
    return rows;
  }

  it('flags a repeated (name + URI) pair', () => {
    const rows = validate([row(1, 'Customer', 'http://x/1'), row(2, 'Customer', 'http://x/1')]);
    expect(rows[0].errors).toEqual([]);
    expect(rows[1].errors).toEqual(['Duplicate business term and external reference URI in this import.']);
  });

  it('allows different names under the same URI', () => {
    const rows = validate([row(1, 'Customer', 'http://x/1'), row(2, 'Client', 'http://x/1')]);
    expect(rows[0].errors).toEqual([]);
    expect(rows[1].errors).toEqual([]);
  });

  it('allows the same name under different URIs', () => {
    const rows = validate([row(1, 'Customer', 'http://x/1'), row(2, 'Customer', 'http://x/2')]);
    expect(rows[1].errors).toEqual([]);
  });
});
