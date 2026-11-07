import {OasDocHttpHeader} from './openapi-doc';
import {
  adaptToLocation,
  defaultExplode,
  defaultStyle,
  exampleToText,
  hasErrors,
  locationOf,
  newHttpHeader,
  OasDocHttpHeaderDraft,
  parseExampleText,
  parseSchemaText,
  serializationWarnings,
  schemaToText,
  serializationOf,
  STYLES_BY_LOCATION,
  validateHeaderDraft
} from './oas-doc-http-header-form';

/** Issue #1791: what the dialog of an HTTP header lets one enter. Pure domain logic. */
describe('OasDoc HTTP header form (#1791)', () => {

  const draft = (over: Partial<OasDocHttpHeaderDraft> = {}): OasDocHttpHeaderDraft => ({
    name: 'traceparent', schemaText: '{"type":"string"}', exampleText: '', direction: 'REQUEST', ...over
  });

  const OBJECT = '{"type":"object"}';

  const existing = (name: string, over: Partial<OasDocHttpHeader> = {}): OasDocHttpHeader => ({
    name, schema: {type: 'string'}, direction: 'REQUEST', required: false, ...over
  });

  describe('newHttpHeader', () => {
    it('is a string of the direction it is added in, for the operation', () => {
      const header = newHttpHeader('RESPONSE', 7);
      expect(header.oasOperationId).toBe(7);
      expect(header.name).toBe('');
      expect(header.schema).toEqual({type: 'string'});
      expect(header.direction).toBe('RESPONSE');
      expect(header.required).toBe(false);
      expect(newHttpHeader('REQUEST', 7).direction).toBe('REQUEST');
    });

    it('is in the query for a request parameter, and has no location as a response header', () => {
      expect(newHttpHeader('REQUEST').in).toBe('query');
      expect('in' in newHttpHeader('RESPONSE')).toBe(false);
    });

    it('is for the whole document without an operation', () => {
      expect(newHttpHeader('REQUEST').oasOperationId).toBeUndefined();
    });
  });

  describe('texts', () => {
    it('shows a schema as indented JSON, and a missing one as a string', () => {
      expect(schemaToText({type: 'string', pattern: '^a$'})).toBe('{\n  "type": "string",\n  "pattern": "^a$"\n}');
      expect(JSON.parse(schemaToText(undefined))).toEqual({type: 'string'});
    });

    it('shows an example as JSON, and none as an empty text', () => {
      expect(exampleToText('abc')).toBe('"abc"');
      expect(exampleToText(25)).toBe('25');
      expect(exampleToText({a: [1]})).toBe('{"a":[1]}');
      expect(exampleToText(undefined)).toBe('');
      expect(exampleToText(null)).toBe('');
    });
  });

  describe('parseSchemaText', () => {
    it('accepts a Schema Object and a Reference Object', () => {
      expect(parseSchemaText('{"type":"string","pattern":"^a$"}').schema).toEqual({type: 'string', pattern: '^a$'});
      expect(parseSchemaText('{"$ref":"#/components/schemas/X"}').schema).toEqual({$ref: '#/components/schemas/X'});
      expect(parseSchemaText('{}').error).toBeUndefined();
    });

    it('rejects what is blank, is not JSON, or is JSON that is not an object', () => {
      for (const text of ['', '  ', '{"type":', 'string', '[1]', '"string"', '25', 'true', 'null']) {
        expect(parseSchemaText(text).error, text).toBeTruthy();
      }
    });

    it('rejects a $ref that is not a non-blank string', () => {
      for (const text of ['{"$ref":1}', '{"$ref":null}', '{"$ref":""}', '{"$ref":" "}', '{"$ref":{}}']) {
        expect(parseSchemaText(text).error, text).toBeTruthy();
      }
    });
  });

  describe('parseExampleText', () => {
    it('is none for a blank text', () => {
      expect(parseExampleText('')).toEqual({});
      expect(parseExampleText('  ')).toEqual({});
    });

    it('is any JSON value, keeping its type', () => {
      expect(parseExampleText('"abc"').example).toBe('abc');
      expect(parseExampleText('25').example).toBe(25);
      expect(parseExampleText('false').example).toBe(false);
      expect(parseExampleText('{"a":[1]}').example).toEqual({a: [1]});
    });

    it('rejects what is not JSON, and null (an empty example is none)', () => {
      for (const text of ['bearer {access_token}', "'a'", '{', 'null']) {
        expect(parseExampleText(text).error, text).toBeTruthy();
      }
    });
  });

  describe('validateHeaderDraft', () => {
    it('has no errors for a header that is fine', () => {
      expect(hasErrors(validateHeaderDraft(draft(), []))).toBe(false);
      expect(hasErrors(validateHeaderDraft(draft({exampleText: '"abc"'}), [], 3))).toBe(false);
    });

    it('wants a name made of the characters of a header name', () => {
      expect(validateHeaderDraft(draft({name: ''}), []).name).toBeTruthy();
      expect(validateHeaderDraft(draft({name: '  '}), []).name).toBeTruthy();
      expect(validateHeaderDraft(draft({name: 'trace parent'}), []).name).toBeTruthy();
      expect(validateHeaderDraft(draft({name: 'X-Request-Id'}), []).name).toBeUndefined();
    });

    it('wants a request parameter or a response header', () => {
      expect(validateHeaderDraft(draft({direction: undefined}), []).direction).toBeTruthy();
      expect(validateHeaderDraft(draft({direction: 'REQUEST'}), []).direction).toBeUndefined();
      expect(validateHeaderDraft(draft({direction: 'RESPONSE'}), []).direction).toBeUndefined();
    });

    it('wants a schema and an example that are valid', () => {
      expect(validateHeaderDraft(draft({schemaText: '[1]'}), []).schema).toBeTruthy();
      expect(validateHeaderDraft(draft({exampleText: 'abc'}), []).example).toBeTruthy();
    });

    it('tells a header of the same name for the same operation and direction, without regard to case', () => {
      const others = [existing('TraceParent', {oasOperationId: 3})];
      expect(validateHeaderDraft(draft(), others, 3).duplicate).toBeTruthy();
    });

    it('lets the same name through for another direction, another operation, or the whole document', () => {
      const requestOnly = [existing('traceparent', {oasOperationId: 3, direction: 'REQUEST'})];
      expect(validateHeaderDraft(draft({direction: 'RESPONSE'}), requestOnly, 3).duplicate).toBeUndefined();
      expect(validateHeaderDraft(draft(), [existing('traceparent', {oasOperationId: 4})], 3).duplicate).toBeUndefined();
      // one of the whole document is overridden by one of the operation
      expect(validateHeaderDraft(draft(), [existing('traceparent')], 3).duplicate).toBeUndefined();
    });

    it('tells a header of the whole document that is there already', () => {
      expect(validateHeaderDraft(draft(), [existing('traceparent')]).duplicate).toBeTruthy();
    });
  });

  describe('the location of a request parameter', () => {
    it('is a header unless the parameter says otherwise, and always a header for a response header', () => {
      expect(locationOf({direction: 'REQUEST'})).toBe('header');
      expect(locationOf({direction: 'REQUEST', in: 'cookie'})).toBe('cookie');
      expect(locationOf({direction: 'RESPONSE', in: 'query'})).toBe('header');
    });

    it('lets the name of a query parameter or a cookie be anything, but not the name of a header', () => {
      expect(validateHeaderDraft(draft({name: 'filter[name]', in: 'query'}), []).name).toBeUndefined();
      expect(validateHeaderDraft(draft({name: 'session id', in: 'cookie'}), []).name).toBeUndefined();
      expect(validateHeaderDraft(draft({name: 'session id', in: 'header'}), []).name).toBeTruthy();
    });

    it('wants the name of a path parameter to be a name of the path', () => {
      expect(validateHeaderDraft(draft({name: 'id', in: 'path'}), []).name).toBeUndefined();
      for (const name of ['{id}', 'a/b', 'a b']) {
        expect(validateHeaderDraft(draft({name, in: 'path'}), []).name).toBeTruthy();
      }
    });

    it('refuses what OpenAPI ignores: Accept, Content-Type, Authorization as a request header, Content-Type as a response header', () => {
      for (const name of ['Accept', 'content-type', 'AUTHORIZATION']) {
        expect(validateHeaderDraft(draft({name, in: 'header'}), []).name).toBeTruthy();
        expect(validateHeaderDraft(draft({name, in: 'query'}), []).name).toBeUndefined();
      }
      expect(validateHeaderDraft(draft({name: 'Content-Type', direction: 'RESPONSE'}), []).name).toBeTruthy();
      expect(validateHeaderDraft(draft({name: 'Accept', direction: 'RESPONSE'}), []).name).toBeUndefined();
    });

    it('tells a clash only within one location, and only a header name ignores case', () => {
      const others = [existing('id', {oasOperationId: 3, in: 'query'})];
      expect(validateHeaderDraft(draft({name: 'id', in: 'query'}), others, 3).duplicate).toBeTruthy();
      expect(validateHeaderDraft(draft({name: 'id', in: 'cookie'}), others, 3).duplicate).toBeUndefined();
      expect(validateHeaderDraft(draft({name: 'id', in: 'header'}), others, 3).duplicate).toBeUndefined();
      expect(validateHeaderDraft(draft({name: 'ID', in: 'query'}), others, 3).duplicate).toBeUndefined();
      const headers = [existing('X-Id', {oasOperationId: 3, in: 'header'})];
      expect(validateHeaderDraft(draft({name: 'x-id', in: 'header'}), headers, 3).duplicate).toBeTruthy();
      // a parameter without a location is in a header
      expect(validateHeaderDraft(draft({name: 'x-id', in: undefined}), headers, 3).duplicate).toBeTruthy();
    });

    it('wants a location that exists', () => {
      expect(validateHeaderDraft(draft({in: 'body' as any}), []).direction).toBeTruthy();
    });
  });

  describe('the serialization of the value', () => {
    it('is by schema, or by content when there is a media type', () => {
      expect(serializationOf({})).toBe('schema');
      expect(serializationOf({contentMediaType: 'text/plain'})).toBe('content');
    });

    it('has the styles of the location, and the first of them is its default', () => {
      expect(STYLES_BY_LOCATION.query).toEqual(['form', 'spaceDelimited', 'pipeDelimited', 'deepObject']);
      expect(STYLES_BY_LOCATION.path).toEqual(['matrix', 'label', 'simple']);
      expect(STYLES_BY_LOCATION.header).toEqual(['simple']);
      expect(STYLES_BY_LOCATION.cookie).toEqual(['form']);
      expect(defaultStyle('query')).toBe('form');
      expect(defaultStyle('path')).toBe('matrix');
      expect(defaultStyle('header')).toBe('simple');
      expect(defaultStyle('cookie')).toBe('form');
    });

    it('warns of a cookie serialized with a schema, whatever its value', () => {
      expect(serializationWarnings('cookie', 'schema', {type: 'string'}, undefined)[0]).toContain('cookie');
      expect(serializationWarnings('cookie', 'content', {type: 'string'}, 'abc')).toEqual([]);
    });

    it('has nothing to say of a header whose values are URL-safe, which the schema is the plain way for', () => {
      expect(serializationWarnings('header', 'schema', {type: 'string'}, undefined)).toEqual([]);
      expect(serializationWarnings('header', 'schema', {type: 'string', example: '00-4bf92f3577b34da6-01'}, undefined)).toEqual([]);
      expect(serializationWarnings('header', 'schema', {type: 'string'}, 'a.b_c~d-e%20f')).toEqual([]);
    });

    it('warns of a header whose value has parameters or characters that are not URL-safe', () => {
      expect(serializationWarnings('header', 'schema', {type: 'string'}, 'text/html; charset=utf-8')[0]).toContain('parameters');
      expect(serializationWarnings('header', 'schema', {type: 'string'}, 'a b')[0]).toContain('not URL-safe');
      expect(serializationWarnings('header', 'schema', {type: 'string'}, 'café')[0]).toContain('not URL-safe');
    });

    it('looks for such a value in the example, in the example of the schema, in an enum, a const and a default', () => {
      expect(serializationWarnings('header', 'schema', {example: 'a;b'}, undefined)).toHaveLength(1);
      expect(serializationWarnings('header', 'schema', {enum: ['ok', 'not ok']}, undefined)).toHaveLength(1);
      expect(serializationWarnings('header', 'schema', {const: 'a b'}, undefined)).toHaveLength(1);
      expect(serializationWarnings('header', 'schema', {default: 'a b'}, undefined)).toHaveLength(1);
      // a value that is not a string has no characters to tell
      expect(serializationWarnings('header', 'schema', {enum: [1, 2]}, 5)).toEqual([]);
    });

    it('warns of nothing for a header serialized with content, nor for a query parameter or a path parameter', () => {
      expect(serializationWarnings('header', 'content', {}, 'a; b')).toEqual([]);
      expect(serializationWarnings('query', 'schema', {}, 'a; b')).toEqual([]);
      expect(serializationWarnings('path', 'schema', {}, 'a; b')).toEqual([]);
    });

    it('drops the style and allowReserved that a new location does not have', () => {
      const header = existing('x', {in: 'cookie', style: 'deepObject', explode: true, allowReserved: true});
      adaptToLocation(header);
      expect('style' in header).toBe(false);
      expect('explode' in header).toBe(false);
      expect('allowReserved' in header).toBe(false);

      const keeps = existing('x', {in: 'query', style: 'pipeDelimited', allowReserved: true});
      adaptToLocation(keeps);
      expect(keeps.style).toBe('pipeDelimited');
      expect(keeps.allowReserved).toBe(true);
    });

    it('wants a media type, a valid one, when there is content', () => {
      expect(validateHeaderDraft(draft({in: 'cookie', contentMediaType: 'text/plain'}), []).serialization).toBeUndefined();
      expect(validateHeaderDraft(draft({in: 'cookie', contentMediaType: ' '}), []).serialization).toBeTruthy();
      expect(validateHeaderDraft(draft({in: 'cookie', contentMediaType: 'text'}), []).serialization).toBeTruthy();
      expect(validateHeaderDraft(draft({in: 'cookie', contentMediaType: 'text/plain; charset=utf-8'}), []).serialization).toBeTruthy();
    });

    it('has the explode that OpenAPI gives a style: true for form, false for the others', () => {
      expect(defaultExplode('form')).toBe(true);
      for (const style of ['matrix', 'label', 'simple', 'spaceDelimited', 'pipeDelimited', 'deepObject']) {
        expect(defaultExplode(style)).toBe(false);
      }
    });

    it('wants a style of the location, and no deepObject without explode', () => {
      expect(validateHeaderDraft(draft({in: 'query', style: 'form'}), []).serialization).toBeUndefined();
      expect(validateHeaderDraft(draft({in: 'cookie', style: 'simple'}), []).serialization).toBeTruthy();
      expect(validateHeaderDraft(draft({in: 'query', style: 'deepObject', explode: false, schemaText: OBJECT}), []).serialization).toBeTruthy();
      // the default of deepObject is no explode, which OpenAPI does not define
      expect(validateHeaderDraft(draft({in: 'query', style: 'deepObject', schemaText: OBJECT}), []).serialization).toBeTruthy();
      expect(validateHeaderDraft(draft({in: 'query', style: 'deepObject', explode: true, schemaText: OBJECT}), []).serialization).toBeUndefined();
      expect(validateHeaderDraft(draft({direction: 'RESPONSE', style: 'form'}), []).serialization).toBeTruthy();
      expect(validateHeaderDraft(draft({direction: 'RESPONSE', style: 'simple'}), []).serialization).toBeUndefined();
    });

    it('wants a style that is for the type of the schema', () => {
      const style = (style: string, schemaText: string) => validateHeaderDraft(draft({in: 'query', style, explode: style === 'deepObject' ? true : undefined, schemaText}), []).serialization;
      expect(style('deepObject', '{"type":"string"}')).toContain('type object');
      expect(style('deepObject', '{"type":"array"}')).toBeTruthy();
      expect(style('deepObject', OBJECT)).toBeUndefined();
      expect(style('spaceDelimited', '{"type":"string"}')).toContain('array or object');
      expect(style('spaceDelimited', '{"type":"array"}')).toBeUndefined();
      expect(style('pipeDelimited', OBJECT)).toBeUndefined();
      expect(style('pipeDelimited', '{"type":"integer"}')).toBeTruthy();
      expect(style('form', '{"type":"string"}')).toBeUndefined();
      // a schema that does not say its type, or a type list that has it, cannot be judged
      expect(style('deepObject', '{"$ref":"#/components/schemas/X"}')).toBeUndefined();
      expect(style('deepObject', '{"type":["object","null"]}')).toBeUndefined();
    });

    it('lets only a query parameter allow reserved characters', () => {
      expect(validateHeaderDraft(draft({in: 'query', allowReserved: true}), []).serialization).toBeUndefined();
      expect(validateHeaderDraft(draft({in: 'cookie', allowReserved: true}), []).serialization).toBeTruthy();
      expect(validateHeaderDraft(draft({direction: 'RESPONSE', allowReserved: true}), []).serialization).toBeTruthy();
    });
  });
});
