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
