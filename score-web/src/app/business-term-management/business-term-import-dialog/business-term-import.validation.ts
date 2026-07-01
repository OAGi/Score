import {
  BUSINESS_TERM_NAME_MAX_LENGTH,
  EXTERNAL_REFERENCE_ID_MAX_LENGTH,
  EXTERNAL_REFERENCE_URI_MAX_LENGTH,
  ImportRow,
} from './business-term-import.model';

/**
 * Approximate mirror of the backend {@code Utility.isValidURI}, which only rejects values that make
 * {@code new java.net.URI(value)} throw a syntax error — i.e. it is very permissive (relative and
 * scheme-less values are accepted). The server stays authoritative; any divergence simply surfaces
 * as a per-row FAILED result carrying the server's message.
 */
export function isValidUri(value: string): boolean {
  if (value == null) {
    return false;
  }
  // Characters that java.net.URI rejects outright (incl. whitespace).
  if (/[\s<>"{}|\\^`]/.test(value)) {
    return false;
  }
  // A '%' not followed by two hex digits is a malformed percent-escape.
  if (/%(?![0-9A-Fa-f]{2})/.test(value)) {
    return false;
  }
  return true;
}

/**
 * Validate one preview row against the same rules (and exact messages) the server enforces in
 * BusinessTermInputValidator. Values are NOT trimmed, mirroring the server's {@code hasLength}
 * semantics. Returns the list of error messages; an empty list means the row is importable.
 */
export function validateImportRow(row: ImportRow): string[] {
  const errors: string[] = [];
  const businessTerm = row.businessTerm ?? '';
  const uri = row.externalReferenceUri ?? '';
  const id = row.externalReferenceId ?? '';

  if (businessTerm.length === 0) {
    errors.push('The business term is required.');
  } else if (businessTerm.length > BUSINESS_TERM_NAME_MAX_LENGTH) {
    errors.push(businessTerm + ' is longer than ' + BUSINESS_TERM_NAME_MAX_LENGTH + ' characters limit.');
  }

  if (uri.length === 0) {
    errors.push('The external reference URI is required.');
  } else if (uri.length > EXTERNAL_REFERENCE_URI_MAX_LENGTH) {
    errors.push('The external reference URI is longer than ' + EXTERNAL_REFERENCE_URI_MAX_LENGTH + ' characters limit.');
  } else if (!isValidUri(uri)) {
    errors.push(uri + ' is not a valid URI.');
  }

  if (id.length > 0 && id.length > EXTERNAL_REFERENCE_ID_MAX_LENGTH) {
    errors.push(id + ' is longer than ' + EXTERNAL_REFERENCE_ID_MAX_LENGTH + ' characters limit.');
  }

  return errors;
}
