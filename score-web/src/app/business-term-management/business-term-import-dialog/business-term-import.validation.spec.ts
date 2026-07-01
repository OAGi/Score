import {isValidUri, validateImportRow} from './business-term-import.validation';
import {
  BUSINESS_TERM_NAME_MAX_LENGTH,
  EXTERNAL_REFERENCE_ID_MAX_LENGTH,
  ImportRow,
} from './business-term-import.model';

/**
 * #1754: unit coverage for the per-row preview validation the import dialog runs client-side to
 * pre-check / flag rows. It mirrors the backend BusinessTermInputValidator (same rules and exact
 * messages), so these tests also pin the strings the review step's tooltips and the "need review"
 * chip depend on.
 */
function row(over: Partial<ImportRow>): ImportRow {
  return {
    id: 0, rowIndex: 1, businessTerm: 'Term', externalReferenceUri: 'https://x.example.com/1',
    externalReferenceId: '', definition: '', comment: '', errors: [], userDeselected: false, ...over,
  };
}

describe('isValidUri — mirrors the permissive java.net.URI check', () => {

  it('accepts ordinary absolute, relative, and urn references', () => {
    expect(isValidUri('https://glossary.example.com/term/1')).toBe(true);
    expect(isValidUri('term/1')).toBe(true);            // scheme-less / relative is accepted
    expect(isValidUri('urn:example:1')).toBe(true);
  });

  it('accepts a correctly percent-encoded value', () => {
    expect(isValidUri('https://x.example.com/a%20b')).toBe(true);
  });

  it('rejects whitespace and the characters java.net.URI rejects outright', () => {
    for (const bad of ['has space', 'a<b', 'a>b', 'a"b', 'a{b', 'a}b', 'a|b', 'a\\b', 'a^b', 'a`b']) {
      expect(isValidUri(bad), bad).toBe(false);
    }
  });

  it('rejects a malformed percent-escape', () => {
    expect(isValidUri('https://x.example.com/a%2')).toBe(false);
    expect(isValidUri('https://x.example.com/a%zz')).toBe(false);
  });

  it('rejects null', () => {
    expect(isValidUri(null as unknown as string)).toBe(false);
  });
});

describe('validateImportRow — server-parity row validation', () => {

  it('a well-formed row has no errors', () => {
    expect(validateImportRow(row({}))).toEqual([]);
  });

  it('requires the business term', () => {
    expect(validateImportRow(row({businessTerm: ''}))).toContain('The business term is required.');
  });

  it('flags a business term longer than the limit', () => {
    const long = 'a'.repeat(BUSINESS_TERM_NAME_MAX_LENGTH + 1);
    expect(validateImportRow(row({businessTerm: long})))
      .toContain(long + ' is longer than ' + BUSINESS_TERM_NAME_MAX_LENGTH + ' characters limit.');
  });

  it('requires the external reference URI', () => {
    expect(validateImportRow(row({externalReferenceUri: ''})))
      .toContain('The external reference URI is required.');
  });

  it('flags an invalid external reference URI', () => {
    expect(validateImportRow(row({externalReferenceUri: 'has space'})))
      .toContain('has space is not a valid URI.');
  });

  it('flags an external reference id longer than the limit; the id itself is optional', () => {
    const longId = 'x'.repeat(EXTERNAL_REFERENCE_ID_MAX_LENGTH + 1);
    expect(validateImportRow(row({externalReferenceId: longId})))
      .toContain(longId + ' is longer than ' + EXTERNAL_REFERENCE_ID_MAX_LENGTH + ' characters limit.');
    // an id at exactly the limit is fine, and an absent id never errors
    expect(validateImportRow(row({externalReferenceId: 'x'.repeat(EXTERNAL_REFERENCE_ID_MAX_LENGTH)}))).toEqual([]);
    expect(validateImportRow(row({externalReferenceId: ''}))).toEqual([]);
  });

  it('does not trim values (mirrors the server hasLength semantics): a single space is a non-empty term', () => {
    expect(validateImportRow(row({businessTerm: ' '}))).toEqual([]);
  });

  it('accumulates multiple errors for a row that violates several rules', () => {
    const errs = validateImportRow(row({businessTerm: '', externalReferenceUri: 'has space'}));
    expect(errs).toContain('The business term is required.');
    expect(errs).toContain('has space is not a valid URI.');
  });
});
