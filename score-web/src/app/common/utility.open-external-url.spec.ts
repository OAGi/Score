import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {openExternalUrl} from './utility';

describe('openExternalUrl', () => {
  let openSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    openSpy = vi.fn();
    vi.stubGlobal('open', openSpy);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('opens an http or https URL in a new tab without opener access', () => {
    expect(openExternalUrl('https://example.com/a?b=c')).toBe(true);
    expect(openSpy).toHaveBeenCalledWith('https://example.com/a?b=c', '_blank', 'noopener,noreferrer');
  });

  it.each([
    'javascript:alert(1)',
    ' JavaScript:alert(1)',
    'data:text/html,<script>alert(1)</script>',
    'urn:example:term:1',
    '/relative/path',
    'not a url',
    '',
    null,
    undefined,
  ])('does not open %s', (value) => {
    expect(openExternalUrl(value as any)).toBe(false);
    expect(openSpy).not.toHaveBeenCalled();
  });
});
