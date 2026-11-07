import {
  hasErrors,
  OasDocHttpHeaderDraft,
  schemaTypes,
  schemaVersionError,
  validateHeaderDraft,
  valueWarnings
} from './oas-doc-http-header-form';
import {OasHttpHeaderDirection, OasParameterLocation} from './openapi-doc';

/**
 * Issue #1791: every combination of what the dialog of a request parameter or of a response header lets the user give, in
 * OpenAPI 3.0.4 and 3.1.2. The expected answer is written here from the Parameter Object, the Header Object and the style
 * table of the specification (the backend has the same table, checked against the published JSON Schema).
 */
describe('OasDoc HTTP header dialog: every combination (#1791)', () => {

  const VERSIONS = ['3.0', '3.1'];
  const IN: (OasParameterLocation | undefined)[] = [undefined, 'query', 'header', 'path', 'cookie', 'body' as any];
  const STYLES = [undefined, 'matrix', 'label', 'simple', 'form', 'spaceDelimited', 'pipeDelimited', 'deepObject', 'bogus'];
  const EXPLODE = [undefined, true, false];
  const MEDIA_TYPES: (string | undefined)[] = [undefined, 'text/plain', 'application/json', 'not a media type', '  '];
  const SCHEMAS = [
    '{"type":"string"}',
    '{"type":"array","items":{"type":"string"}}',
    '{"type":"object","properties":{"a":{"type":"string"}}}',
    '{"type":"integer","minimum":1}',
    '{"type":"string","nullable":true}',
    '{"type":["string","null"]}',
    '{"$ref":"#/components/schemas/Thing"}'
  ];

  const STYLES_OF: Record<string, string[]> = {
    query: ['form', 'spaceDelimited', 'pipeDelimited', 'deepObject'],
    path: ['matrix', 'label', 'simple'],
    header: ['simple'],
    cookie: ['form']
  };

  const nameFor = (direction: OasHttpHeaderDirection, in_: string | undefined) => {
    if (direction === 'RESPONSE' || !in_ || in_ === 'header') {
      return 'x-trace';
    }
    return in_ === 'path' ? 'id' : (in_ === 'cookie' ? 'session' : 'filter');
  };

  /** The reason the specification refuses the combination, or undefined when it allows it. */
  const refusal = (version: string, d: OasDocHttpHeaderDraft): string | undefined => {
    const request = d.direction === 'REQUEST';
    const in_ = request ? (d.in ?? 'header') : 'header';
    if (!['query', 'header', 'path', 'cookie'].includes(in_)) {
      return 'location';
    }
    const media = d.contentMediaType;
    if (media !== undefined) {
      if (!/^[A-Za-z0-9][A-Za-z0-9!#$&^_.+-]*\/[A-Za-z0-9*][A-Za-z0-9!#$&^_.+*-]*$/.test(media.trim())) {
        return 'media type';
      }
      // the draft of the dialog never has a style with content: it is dropped when content is chosen
    } else {
      if (d.style !== undefined) {
        if (!STYLES_OF[in_].includes(d.style)) {
          return 'style of the location';
        }
        const wanted = d.style === 'deepObject' ? ['object'] : (['spaceDelimited', 'pipeDelimited'].includes(d.style) ? ['array', 'object'] : []);
        const types = schemaTypes(JSON.parse(d.schemaText));
        if (wanted.length && types.length && !types.some(t => wanted.includes(t))) {
          return 'style of the type';
        }
        if (d.style === 'deepObject' && d.explode !== true) {
          return 'deepObject without explode';
        }
      }
      if (d.allowReserved && !(request && in_ === 'query')) {
        return 'allowReserved outside the query';
      }
    }
    if (version === '3.0' && Array.isArray(JSON.parse(d.schemaText)['type'])) {
      return 'type list';
    }
    return undefined;
  };

  it('allows exactly what the specification allows', () => {
    const mismatches: string[] = [];
    let combinations = 0;
    let accepted = 0;
    for (const version of VERSIONS) {
      for (const direction of ['REQUEST', 'RESPONSE'] as OasHttpHeaderDirection[]) {
        for (const in_ of direction === 'REQUEST' ? IN : [undefined]) {
          for (const style of STYLES) {
            for (const explode of EXPLODE) {
              for (const allowReserved of [false, true]) {
                for (const contentMediaType of MEDIA_TYPES) {
                  for (const schemaText of SCHEMAS) {
                    const draft: OasDocHttpHeaderDraft = {
                      name: nameFor(direction, in_), direction, in: in_, style, explode, allowReserved, contentMediaType,
                      schemaText, exampleText: ''
                    };
                    // the dialog drops what content excludes when content is chosen: a draft with content has none of them
                    if (contentMediaType !== undefined) {
                      if (style !== undefined || explode !== undefined || allowReserved) {
                        continue;
                      }
                    }
                    const expected = refusal(version, draft);
                    const errors = validateHeaderDraft(draft, [], 7, {openApiVersion: version, resourcePath: '/things/{id}'});
                    const actual = hasErrors(errors);
                    combinations++;
                    if (!actual) {
                      accepted++;
                    }
                    if ((expected === undefined) === actual && mismatches.length < 25) {
                      mismatches.push(`${version} ${direction} in=${in_} style=${style} explode=${explode} allowReserved=${allowReserved}`
                        + ` content=${contentMediaType} schema=${schemaText}: expected ${expected ?? 'allowed'}, errors ${JSON.stringify(errors)}`);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    expect(mismatches).toEqual([]);
    expect(combinations).toBeGreaterThan(5000);
    expect(accepted).toBeGreaterThan(500);
  });

  it('checks the name per location', () => {
    const cases: [OasHttpHeaderDirection, string | undefined, string, boolean][] = [
      ['REQUEST', 'header', 'x-trace', true], ['REQUEST', 'header', 'TraceParent', true], ['REQUEST', 'header', 'Accept', false],
      ['REQUEST', 'header', 'accept', false], ['REQUEST', 'header', 'Content-Type', false], ['REQUEST', 'header', 'Authorization', false],
      ['REQUEST', 'header', 'Cookie', true], ['REQUEST', 'header', 'bad name', false], ['REQUEST', 'header', 'bad:name', false],
      ['REQUEST', 'header', '', false], ['REQUEST', 'header', ' ', false], ['REQUEST', 'header', 'é', false],
      ['REQUEST', 'query', 'Accept', true], ['REQUEST', 'query', 'bad name', true], ['REQUEST', 'query', 'é', true],
      ['REQUEST', 'query', '', false], ['REQUEST', 'cookie', 'Content-Type', true], ['REQUEST', 'cookie', 'a b', true],
      ['REQUEST', 'path', 'id', true], ['REQUEST', 'path', '{id}', false], ['REQUEST', 'path', 'a/b', false],
      ['REQUEST', 'path', 'a b', false], ['REQUEST', 'path', 'a{b}', false], ['REQUEST', 'path', 'user.id', true],
      ['RESPONSE', undefined, 'x-trace', true], ['RESPONSE', undefined, 'Content-Type', false], ['RESPONSE', undefined, 'content-type', false],
      ['RESPONSE', undefined, 'Accept', true], ['RESPONSE', undefined, 'Authorization', true], ['RESPONSE', undefined, 'bad name', false],
      ['RESPONSE', undefined, 'Set-Cookie', true]
    ];
    const wrong: string[] = [];
    for (const [direction, in_, name, allowed] of cases) {
      const errors = validateHeaderDraft({name, direction, in: in_ as any, schemaText: '{"type":"string"}', exampleText: ''}, [], undefined);
      if (hasErrors(errors) === allowed) {
        wrong.push(`${direction} ${in_} '${name}' expected ${allowed ? 'allowed' : 'refused'}`);
      }
    }
    expect(wrong).toEqual([]);
  });

  it('knows the path parameter has to be in the path of its operation', () => {
    const path = (name: string, resourcePath: string | undefined, operation?: number) =>
      validateHeaderDraft({name, direction: 'REQUEST', in: 'path', schemaText: '{"type":"string"}', exampleText: ''}, [], operation, {resourcePath});
    expect(hasErrors(path('id', '/things/{id}', 7))).toBe(false);
    expect(path('other', '/things/{id}', 7).name).toContain('has no {other}');
    expect(path('id', '/things', 7).name).toContain('/things');
    expect(path('id', '', 7).name).toContain('(none)');
    // for the whole document the operations that have it get it
    expect(hasErrors(path('other', '/things/{id}', undefined))).toBe(false);
    // not known: the server tells
    expect(hasErrors(path('other', undefined, 7))).toBe(false);
  });

  it('knows the schema of OpenAPI 3.0 from the one of 3.1', () => {
    const schemas: [string, boolean, boolean][] = [
      ['{"type":"string"}', true, true], ['{"type":["string","null"]}', false, true], ['{"type":"null"}', false, true],
      ['{"type":"string","nullable":true}', true, true], ['{"$ref":"#/components/schemas/X"}', true, true],
      ['{"$ref":"#/components/schemas/X","description":"d"}', false, true],
      ['{"type":"object","properties":{"a":{"$ref":"#/components/schemas/X","description":"d"}}}', false, true],
      ['{"$ref":"#/components/schemas/X","x-note":"n"}', true, true]
    ];
    for (const [text, ok30, ok31] of schemas) {
      expect(schemaVersionError(JSON.parse(text), '3.0') === undefined).withContext(text + ' 3.0').toBe(ok30);
      expect(schemaVersionError(JSON.parse(text), '3.0.4') === undefined).withContext(text + ' 3.0.4').toBe(ok30);
      expect(schemaVersionError(JSON.parse(text), '3.1') === undefined).withContext(text + ' 3.1').toBe(ok31);
      expect(schemaVersionError(JSON.parse(text), undefined)).toBeUndefined();
    }
  });

  it('warns of what is allowed but probably not meant', () => {
    const w = (name: string, in_: OasParameterLocation | undefined, direction: OasHttpHeaderDirection, schema: any, example: any) =>
      valueWarnings({name, direction, in: in_}, schema, example, 'schema');
    expect(w('Cookie', 'header', 'REQUEST', {type: 'string'}, undefined)[0]).toContain('cookie location');
    expect(w('cookie', undefined, 'REQUEST', {type: 'string'}, undefined).length).toBe(1);
    expect(w('Cookie', 'cookie', 'REQUEST', {type: 'string'}, undefined)).toEqual([]);
    expect(w('Cookie', undefined, 'RESPONSE', {type: 'string'}, undefined)).toEqual([]);
    expect(w('x', 'query', 'REQUEST', {type: 'integer'}, 'abc')[0]).toContain('example is a string');
    expect(w('x', 'query', 'REQUEST', {type: 'integer'}, 1.5)[0]).toContain('example is a number');
    expect(w('x', 'query', 'REQUEST', {type: 'integer'}, 3)).toEqual([]);
    expect(w('x', 'query', 'REQUEST', {type: 'number'}, 1.5)).toEqual([]);
    expect(w('x', 'query', 'REQUEST', {type: ['string', 'null']}, 'a')).toEqual([]);
    expect(w('x', 'query', 'REQUEST', {type: 'array'}, ['a'])).toEqual([]);
    expect(w('x', 'query', 'REQUEST', {type: 'object'}, ['a'])[0]).toContain('example is a array');
    expect(w('x', 'query', 'REQUEST', {$ref: '#/a'}, 'a')).toEqual([]);
    // with content the example is the one of the media type
    expect(valueWarnings({name: 'x', direction: 'REQUEST', in: 'query'}, {type: 'object'}, 'a', 'content')).toEqual([]);
  });
});
