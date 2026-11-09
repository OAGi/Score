import {scopeNameError, securitySchemeNameError} from './oas-security-names';

/** Issue #1891: the key of components.securitySchemes is limited to ^[a-zA-Z0-9\.\-_]+$ by the Components Object. */
describe('Security Scheme and scope names (#1891)', () => {
  it('accepts letters, digits, dot, dash and underscore, in either case', () => {
    for (const name of ['Auth', 'auth', 'Api-Key_2.v1', ' Auth ']) {
      expect(securitySchemeNameError(name)).toBe('');
    }
  });

  it('leaves a blank name to the required check', () => {
    expect(securitySchemeNameError('')).toBe('');
    expect(securitySchemeNameError(undefined)).toBe('');
    expect(securitySchemeNameError('   ')).toBe('');
  });

  it('rejects an emoji, a non-ASCII letter, a space and a separator', () => {
    for (const name of ['Auth\u{1F600}', '인증', 'Café', 'Auth Key', 'Auth/Key', 'Auth:Key']) {
      expect(securitySchemeNameError(name)).not.toBe('');
    }
  });

  describe('scope', () => {
    it('accepts the printable ASCII characters of an OAuth 2.0 scope token', () => {
      for (const name of ['read', 'Read', 'read:pets', 'https://example.com/auth/read', 'openid', 'a.b-c_d~e']) {
        expect(scopeNameError(name)).toBe('');
      }
    });

    it('leaves a blank name to the required check', () => {
      expect(scopeNameError('')).toBe('');
      expect(scopeNameError(undefined)).toBe('');
    });

    it('rejects an emoji, a non-ASCII letter, a space, a double quote and a backslash', () => {
      for (const name of ['read\u{1F600}', '읽기', 'read pets', 'read"pets', 'read\\pets']) {
        expect(scopeNameError(name)).not.toBe('');
      }
    });
  });
});
