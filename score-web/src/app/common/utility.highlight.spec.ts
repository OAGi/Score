import {describe, expect, it} from 'vitest';
import {Injector} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';
import {HighlightSearch} from './utility';

describe('HighlightSearch', () => {
  const sanitizer = {bypassSecurityTrustHtml: (html: string) => html};
  const pipe = Injector.create({
    providers: [
      {provide: DomSanitizer, useValue: sanitizer},
      {provide: HighlightSearch, useClass: HighlightSearch, deps: []},
    ],
  }).get(HighlightSearch);

  it('escapes the value when there is no keyword', () => {
    expect(pipe.transform('a <&> "b"', '')).toBe('a &lt;&amp;&gt; &quot;b&quot;');
  });

  it('marks the keyword', () => {
    expect(pipe.transform('Hello World', 'wor')).toBe('Hello <mark>Wor</mark>ld');
  });

  it('does not mark the letters of an escaped entity', () => {
    expect(pipe.transform('edited <&> "q" meaning', 'q')).toBe(
      'edited &lt;&amp;&gt; &quot;<mark>q</mark>&quot; meaning');
  });

  it('marks a keyword that is an HTML special character', () => {
    expect(pipe.transform('a & b', '&')).toBe('a <mark>&amp;</mark> b');
  });

  it('treats the keyword as a literal', () => {
    expect(pipe.transform('a.b', '.')).toBe('a<mark>.</mark>b');
  });
});
