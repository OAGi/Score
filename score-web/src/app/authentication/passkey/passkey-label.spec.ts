import {
  buildPasskeyLabel,
  describeFromUserAgent,
  describeUserAgent
} from './passkey-label';

const CHROME_MAC = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) ' +
  'Chrome/149.0.0.0 Safari/537.36';
const EDGE_WINDOWS = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) ' +
  'Chrome/149.0.0.0 Safari/537.36 Edg/149.0.3000.12';
const FIREFOX_LINUX = 'Mozilla/5.0 (X11; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0';
const SAFARI_IOS = 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 ' +
  '(KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1';

describe('describeFromUserAgent', () => {
  it('recognizes the browser, its version, and the system', () => {
    expect(describeFromUserAgent(CHROME_MAC)).toEqual({browser: 'Chrome', version: '149.0.0.0', os: 'macOS'});
    expect(describeFromUserAgent(EDGE_WINDOWS)).toEqual({browser: 'Edge', version: '149.0.3000.12', os: 'Windows'});
    expect(describeFromUserAgent(FIREFOX_LINUX)).toEqual({browser: 'Firefox', version: '130.0', os: 'Linux'});
    expect(describeFromUserAgent(SAFARI_IOS)).toEqual({browser: 'Safari', version: '18.0', os: 'iOS'});
  });

  it('knows Opera and Vivaldi, which also say Chrome', () => {
    expect(describeFromUserAgent(CHROME_MAC + ' OPR/120.0.0.0').browser).toBe('Opera');
    expect(describeFromUserAgent(CHROME_MAC + ' Vivaldi/7.1.3570.39').browser).toBe('Vivaldi');
  });

  it('copes with an empty or unknown user agent', () => {
    expect(describeFromUserAgent('')).toEqual({browser: undefined, version: undefined, os: undefined});
    expect(describeFromUserAgent(undefined as any).browser).toBeUndefined();
  });
});

describe('buildPasskeyLabel', () => {
  it('names the browser, its version, the system, and the suffix', () => {
    expect(buildPasskeyLabel({browser: 'Chrome', version: '149.0.7840.123', os: 'macOS'}, '3f9a2c'))
      .toBe('Chrome 149.0.7840.123 on macOS · 3f9a2c');
  });

  it('leaves out what is not known', () => {
    expect(buildPasskeyLabel({browser: 'Chrome', os: 'macOS'}, 'ab12cd')).toBe('Chrome on macOS · ab12cd');
    expect(buildPasskeyLabel({os: 'Linux'}, 'ab12cd')).toBe('Linux · ab12cd');
    expect(buildPasskeyLabel({browser: 'Firefox', version: '130.0'}, 'ab12cd')).toBe('Firefox 130.0 · ab12cd');
    expect(buildPasskeyLabel({}, 'ab12cd')).toBe('Passkey · ab12cd');
    expect(buildPasskeyLabel({browser: 'Chrome', os: 'macOS'}, '')).toBe('Chrome on macOS');
  });

  it('stays well within the 100 characters a passkey name may have', () => {
    const longest = buildPasskeyLabel({browser: 'Microsoft Edge Dev', version: '149.0.7840.123456', os: 'Chrome OS'}, 'abcdef');
    expect(longest.length).toBeLessThan(100);
  });
});

describe('describeUserAgent', () => {
  it('names the browser and the system', () => {
    expect(describeUserAgent(CHROME_MAC)).toBe('Chrome 149.0.0.0 on macOS');
  });

  it('is empty when there is nothing to say', () => {
    expect(describeUserAgent(undefined)).toBe('');
    expect(describeUserAgent('')).toBe('');
    expect(describeUserAgent('curl/8.0')).toBe('');
  });
});
