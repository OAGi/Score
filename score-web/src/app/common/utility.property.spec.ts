import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {decodeUnicodeBase64, encodeUnicodeBase64, loadProperty, saveProperty} from './utility';
import {UserToken} from '../authentication/domain/auth';

describe('saveProperty and loadProperty', () => {
  const userToken = {username: 'tester'} as UserToken;
  const itemKey = 'X-Score-Test[tester]';
  const store = new Map<string, string>();

  beforeEach(() => {
    store.clear();
    vi.stubGlobal('localStorage', {
      getItem: (k: string) => store.has(k) ? store.get(k) : null,
      setItem: (k: string, v: string) => store.set(k, v),
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it.each(['.', '→', '›', 'é', '한'])('round-trips the value %s', value => {
    saveProperty(userToken, 'Test', value);
    expect(loadProperty(userToken, 'Test', 'default')).toBe(value);
  });

  it('stores an ASCII value the same way as btoa', () => {
    saveProperty(userToken, 'Test', '.');
    expect(store.get(itemKey)).toBe(btoa(JSON.stringify({value: '.'})));
  });

  it('loads a Latin-1 value stored by the plain btoa', () => {
    store.set(itemKey, btoa(JSON.stringify({value: 'é'})));
    expect(loadProperty(userToken, 'Test', 'default')).toBe('é');
  });

  it('decodes what it encodes', () => {
    expect(decodeUnicodeBase64(encodeUnicodeBase64('a→b'))).toBe('a→b');
  });
});
