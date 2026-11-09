/**
 * The names of a Security Scheme and of a scope (Issue #1891).
 *
 * The name of a Security Scheme is the key of the components.securitySchemes map, which the Components Object of the
 * OpenAPI Specification (3.0.4 and 3.1.2) limits to letters, digits, '.', '-' and '_'.
 *
 * The OpenAPI Specification does not limit a scope name, but the scopes of an oauth2 or openIdConnect scheme are those of
 * OAuth 2.0, whose scope token is made of printable ASCII characters without a space, '"' and '\' (RFC 6749 section 3.3).
 */
export const SECURITY_SCHEME_NAME_PATTERN = /^[a-zA-Z0-9.\-_]+$/;
export const SCOPE_NAME_PATTERN = /^[\x21\x23-\x5B\x5D-\x7E]+$/;

/** What is wrong with the name of a Security Scheme, or '' when it is fine (a blank name is left to the "required" check). */
export function securitySchemeNameError(name: string | undefined): string {
  const value = (name || '').trim();
  if (!value || SECURITY_SCHEME_NAME_PATTERN.test(value)) {
    return '';
  }
  return 'A name has only letters, digits, ".", "-" and "_".';
}

/** What is wrong with the name of a scope, or '' when it is fine (a blank name is left to the "required" check). */
export function scopeNameError(name: string | undefined): string {
  const value = (name || '').trim();
  if (!value || SCOPE_NAME_PATTERN.test(value)) {
    return '';
  }
  return 'A scope has only ASCII letters, digits and symbols, without a space, a double quote or a backslash.';
}
