import {OasDoc, OasDocHttpHeader} from './openapi-doc';
import {
  adaptPresetSchemas,
  adoptServerHeaders,
  hasAnyPreset,
  headerKey,
  hasPreset,
  headersOf,
  lacksPreset,
  linkHeadersToRows,
  offerPreset,
  OasDocHttpHeaderRow,
  schemaForVersion,
  sortHeaders,
  TRACEPARENT_PRESET,
  withdrawPreset
} from './oas-doc-http-header-preset';

/** Issue #1791: the traceparent preset is a macro that fills the headers of the operations. Pure domain logic. */
describe('OasDoc HTTP header presets (#1791)', () => {

  function doc(httpHeaders?: OasDocHttpHeader[]): OasDoc {
    const oasDoc = new OasDoc();
    oasDoc.httpHeaders = httpHeaders as OasDocHttpHeader[];
    return oasDoc;
  }

  const other: OasDocHttpHeader = {
    name: 'x-request-id', schema: {type: 'string'},
    direction: 'REQUEST', required: false, oasOperationId: 1
  };

  /** The headers of the preset (a request parameter and a response header) for an operation, or the whole document. */
  const both = (oasOperationId?: number, name?: string): OasDocHttpHeader[] =>
    TRACEPARENT_PRESET.headers.map(header => ({...header, ...(name ? {name} : {}), oasOperationId}));

  it('is a request parameter (optional) and a response header (required), each with a pattern and an example', () => {
    expect(TRACEPARENT_PRESET.name).toBe('traceparent');
    const [request, response] = TRACEPARENT_PRESET.headers;
    expect(request.direction).toBe('REQUEST');
    expect(request.in).toBe('header');
    expect(request.required).toBe(false);
    expect(response.direction).toBe('RESPONSE');
    expect(response.required).toBe(true);

    for (const header of [request, response]) {
      expect(header.name).toBe('traceparent');
      expect(header.oasOperationId).toBeUndefined();
      expect(header.schema.type).toBe('string');
      expect(new RegExp(header.schema.pattern).test(header.schema.example)).toBe(true);
    }
  });

  it('describes and exemplifies the request and the response differently', () => {
    const [request, response] = TRACEPARENT_PRESET.headers;
    expect(request.description).not.toBe(response.description);
    expect(request.schema.example).not.toBe(response.schema.example);
  });

  describe('hasPreset', () => {
    it('is false for an operation of a document without headers', () => {
      expect(hasPreset(doc(), TRACEPARENT_PRESET, 1)).toBe(false);
      expect(hasPreset(doc([]), TRACEPARENT_PRESET, 1)).toBe(false);
    });

    it('is true for the operation that has the header, not for another one', () => {
      const oasDoc = doc(both(1));
      expect(hasPreset(oasDoc, TRACEPARENT_PRESET, 1)).toBe(true);
      expect(hasPreset(oasDoc, TRACEPARENT_PRESET, 2)).toBe(false);
    });

    it('is true for every operation when the header is one of the whole document', () => {
      const oasDoc = doc(both());
      expect(hasPreset(oasDoc, TRACEPARENT_PRESET, 1)).toBe(true);
      expect(hasPreset(oasDoc, TRACEPARENT_PRESET, 2)).toBe(true);
    });

    it('is false for an operation that has only one of the two', () => {
      const oasDoc = doc([both(1)[0]]);
      expect(hasPreset(oasDoc, TRACEPARENT_PRESET, 1)).toBe(false);
    });

    it('is not had because a query parameter of the same name is', () => {
      const query: OasDocHttpHeader[] = [{...both(1)[0], in: 'query'}, both(1)[1]];
      expect(hasPreset(doc(query), TRACEPARENT_PRESET, 1)).toBe(false);
    });

    it('matches the name without regard to case', () => {
      const oasDoc = doc(both(1, 'TraceParent'));
      expect(hasPreset(oasDoc, TRACEPARENT_PRESET, 1)).toBe(true);
    });
  });

  describe('offerPreset', () => {
    it('gives each operation a header of its own', () => {
      const oasDoc = doc([]);
      const offer = offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2, 3]);

      expect(offer).toEqual({added: [1, 2, 3], skipped: [], requestParameters: 3, responseHeaders: 3});
      expect(oasDoc.httpHeaders.map(h => [h.name, h.oasOperationId, h.direction])).toEqual([
        ['traceparent', 1, 'REQUEST'], ['traceparent', 1, 'RESPONSE'],
        ['traceparent', 2, 'REQUEST'], ['traceparent', 2, 'RESPONSE'],
        ['traceparent', 3, 'REQUEST'], ['traceparent', 3, 'RESPONSE']]);
    });

    it('works on a document whose headers are not set', () => {
      const oasDoc = doc(undefined);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1]);
      expect(oasDoc.httpHeaders.length).toBe(2);
    });

    it('treats an operation once, however many rows it has (a request and a response)', () => {
      const oasDoc = doc([]);
      const offer = offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 1, 2, 2]);

      expect(offer.added).toEqual([1, 2]);
      expect(oasDoc.httpHeaders.length).toBe(4);
    });

    it('leaves the operations that have the header, and adds only to the others', () => {
      const oasDoc = doc(both(2));
      const offer = offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2, 3]);

      expect(offer).toEqual({added: [1, 3], skipped: [2], requestParameters: 2, responseHeaders: 2});
      expect(oasDoc.httpHeaders.map(h => h.oasOperationId)).toEqual([1, 1, 2, 2, 3, 3]);
    });

    it('adds only the direction that an operation lacks', () => {
      const oasDoc = doc([both(1)[1]]);
      const offer = offerPreset(oasDoc, TRACEPARENT_PRESET, [1]);

      expect(offer).toEqual({added: [1], skipped: [], requestParameters: 1, responseHeaders: 0});
      expect(oasDoc.httpHeaders.map(h => h.direction)).toEqual(['REQUEST', 'RESPONSE']);
    });

    it('adds nothing when the whole document has the header', () => {
      const oasDoc = doc(both());
      const offer = offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2]);

      expect(offer).toEqual({added: [], skipped: [1, 2], requestParameters: 0, responseHeaders: 0});
      expect(oasDoc.httpHeaders.length).toBe(2);
    });

    it('is done again for the operations that are added later, and only for them', () => {
      const oasDoc = doc([]);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2]);
      const again = offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2, 3]);

      expect(again).toEqual({added: [3], skipped: [1, 2], requestParameters: 1, responseHeaders: 1});
      expect(oasDoc.httpHeaders.length).toBe(6);
    });

    it('keeps the other headers of the document', () => {
      const oasDoc = doc([other]);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1]);

      expect(oasDoc.httpHeaders).toContain(other);
      expect(oasDoc.httpHeaders.length).toBe(3);
    });

    it('gives each operation a copy, not the schema object of the preset', () => {
      const oasDoc = doc([]);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2]);
      oasDoc.httpHeaders[0].schema.pattern = 'changed';

      expect(oasDoc.httpHeaders[2].schema.pattern).not.toBe('changed');
      expect(TRACEPARENT_PRESET.headers[0].schema.pattern).not.toBe('changed');
    });
  });

  describe('hasAnyPreset and lacksPreset', () => {
    it('hasAnyPreset: no header, a header of an operation, a header of the whole document', () => {
      expect(hasAnyPreset(doc(), TRACEPARENT_PRESET)).toBe(false);
      expect(hasAnyPreset(doc([other]), TRACEPARENT_PRESET)).toBe(false);
      expect(hasAnyPreset(doc(both(4)), TRACEPARENT_PRESET)).toBe(true);
      expect(hasAnyPreset(doc(both()), TRACEPARENT_PRESET)).toBe(true);
    });

    it('lacksPreset: some operation without it, none without it, no operations at all', () => {
      const oasDoc = doc(both(1));
      expect(lacksPreset(oasDoc, TRACEPARENT_PRESET, [1, 2])).toBe(true);
      expect(lacksPreset(oasDoc, TRACEPARENT_PRESET, [1])).toBe(false);
      expect(lacksPreset(oasDoc, TRACEPARENT_PRESET, [])).toBe(false);
      expect(lacksPreset(doc(undefined), TRACEPARENT_PRESET, [1])).toBe(true);
    });
  });

  describe('withdrawPreset', () => {
    it('takes the header away from every operation and from the whole document, and counts them', () => {
      const oasDoc = doc([...both(1), ...both(2), ...both()]);

      expect(withdrawPreset(oasDoc, TRACEPARENT_PRESET)).toEqual({removed: 6, requestParameters: 3, responseHeaders: 3});
      expect(oasDoc.httpHeaders).toEqual([]);
    });

    it('leaves a query parameter of the same name', () => {
      const query: OasDocHttpHeader = {...other, name: 'traceparent', in: 'query'};
      const oasDoc = doc([query, ...both(1)]);

      expect(withdrawPreset(oasDoc, TRACEPARENT_PRESET)).toEqual({removed: 2, requestParameters: 1, responseHeaders: 1});
      expect(oasDoc.httpHeaders).toEqual([query]);
    });

    it('leaves the other headers, and removes nothing from a document without the header', () => {
      const oasDoc = doc([other]);

      expect(withdrawPreset(oasDoc, TRACEPARENT_PRESET)).toEqual({removed: 0, requestParameters: 0, responseHeaders: 0});
      expect(oasDoc.httpHeaders).toEqual([other]);
      expect(withdrawPreset(doc(undefined), TRACEPARENT_PRESET)).toEqual({removed: 0, requestParameters: 0, responseHeaders: 0});
    });
  });

  it('keeps the headers in one order: by operation, then by name, then by direction', () => {
    const oasDoc = doc([{...other, name: 'b', oasOperationId: 2}, {...other, name: 'A', oasOperationId: 1},
      {...other, name: 'z', oasOperationId: undefined}, {...other, name: 'a', oasOperationId: 2, direction: 'RESPONSE'},
      {...other, name: 'a', oasOperationId: 2}]);
    sortHeaders(oasDoc);

    expect(oasDoc.httpHeaders.map(h => h.name + '@' + (h.oasOperationId || 0) + ':' + h.direction))
      .toEqual(['z@0:REQUEST', 'A@1:REQUEST', 'a@2:REQUEST', 'a@2:RESPONSE', 'b@2:REQUEST']);
  });

  describe('linkHeadersToRows', () => {
    const row = (oasOperationId: number): OasDocHttpHeaderRow => ({oasOperationId, httpHeaders: []});

    it('gives a row the very objects of the document for its operation', () => {
      const oasDoc = doc([]);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1, 2]);
      const rows = [row(1), row(2)];
      linkHeadersToRows(oasDoc, rows);

      expect(rows[0].httpHeaders.length).toBe(2);
      expect(rows[0].httpHeaders[0]).toBe(oasDoc.httpHeaders[0]);
      expect(rows[1].httpHeaders[0]).toBe(oasDoc.httpHeaders[2]);
    });

    it('lets what is changed through a row be a change of the document', () => {
      const oasDoc = doc([]);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1]);
      const rows = [row(1)];
      linkHeadersToRows(oasDoc, rows);

      rows[0].httpHeaders[1].required = false;
      expect(oasDoc.httpHeaders[1].required).toBe(false);
    });

    it('gives the rows of one operation the same headers', () => {
      const oasDoc = doc([]);
      offerPreset(oasDoc, TRACEPARENT_PRESET, [1]);
      const rows = [row(1), row(1)];
      linkHeadersToRows(oasDoc, rows);

      expect(rows[0].httpHeaders).toBe(rows[1].httpHeaders);
    });

    it('gives a row the headers of the whole document too, and not those of another operation', () => {
      const wide: OasDocHttpHeader = {...other, name: 'x-wide', oasOperationId: undefined};
      const oasDoc = doc([wide, other, {...other, name: 'x-other-op', oasOperationId: 9}]);
      const rows = [row(1)];
      linkHeadersToRows(oasDoc, rows);

      expect(rows[0].httpHeaders.map(h => h.name)).toEqual(['x-wide', 'x-request-id']);
    });

    it('leaves a row without headers when the document has none', () => {
      const rows = [row(1)];
      linkHeadersToRows(doc(undefined), rows);
      expect(rows[0].httpHeaders).toEqual([]);
    });
  });

  it('picks the headers of one direction for a column of the table', () => {
    const response: OasDocHttpHeader = {...other, name: 'x-trace', direction: 'RESPONSE'};
    expect(headersOf(undefined, 'REQUEST')).toEqual([]);
    expect(headersOf([], 'RESPONSE')).toEqual([]);
    expect(headersOf([other, response], 'REQUEST')).toEqual([other]);
    expect(headersOf([other, response], 'RESPONSE')).toEqual([response]);
  });

  describe('headers the server registered by itself', () => {
    const since = (oasOperationId: number): OasDocHttpHeader => ({
      name: 'sinceLastDateTime', in: 'query', schema: {type: 'string', format: 'date-time'},
      direction: 'REQUEST', required: false, oasOperationId
    });
    const pathParam = (oasOperationId: number, name: string): OasDocHttpHeader => ({
      name, in: 'path', schema: {type: 'string'}, direction: 'REQUEST', required: true, oasOperationId
    });

    it('tells a header from another by operation, direction, location and name', () => {
      expect(headerKey(since(1))).not.toEqual(headerKey(since(2)));
      expect(headerKey({...since(1), in: 'header'})).not.toEqual(headerKey(since(1)));
      expect(headerKey({...since(1), direction: 'RESPONSE'})).not.toEqual(headerKey(since(1)));
      // a header name is case-insensitive, a query parameter name is not
      expect(headerKey({...other, in: 'header', name: 'X-Request-Id'})).toEqual(headerKey({...other, in: 'header'}));
      expect(headerKey({...since(1), name: 'SinceLastDateTime'})).not.toEqual(headerKey(since(1)));
    });

    it('replaces the headers of a page without changes by the ones of the server', () => {
      const oasDoc = doc([pathParam(1, 'id')]);
      adoptServerHeaders(oasDoc, [pathParam(1, 'orderId'), since(1)], false);
      expect(oasDoc.httpHeaders.map(h => h.name)).toEqual(['orderId', 'sinceLastDateTime']);
    });

    it('keeps what a page with changes has and adds only what the server added', () => {
      const edited = {...pathParam(1, 'id'), description: 'my edit'};
      const oasDoc = doc([edited]);
      adoptServerHeaders(oasDoc, [pathParam(1, 'id'), since(1)], true);
      expect(oasDoc.httpHeaders.length).toBe(2);
      expect(oasDoc.httpHeaders.find(h => h.name === 'id')?.description).toBe('my edit');
      expect(oasDoc.httpHeaders.some(h => h.name === 'sinceLastDateTime')).toBe(true);
    });

    it('does not add twice what the page has already', () => {
      const oasDoc = doc([since(1)]);
      adoptServerHeaders(oasDoc, [since(1)], true);
      expect(oasDoc.httpHeaders.length).toBe(1);
    });
  });
  describe('the form of the schema example by OpenAPI version', () => {
    const sample = '00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01';

    it('is the example of 3.0 when offered to a 3.0 document, and the examples of 3.1 to a 3.1 document', () => {
      const oas30 = doc();
      offerPreset(oas30, TRACEPARENT_PRESET, [1], false);
      const oas31 = doc();
      offerPreset(oas31, TRACEPARENT_PRESET, [1], true);
      for (const header of oas30.httpHeaders) {
        expect(header.schema['example']).toBeDefined();
        expect(header.schema['examples']).toBeUndefined();
      }
      for (const header of oas31.httpHeaders) {
        expect(header.schema['example']).toBeUndefined();
        expect(header.schema['examples']).toEqual([TRACEPARENT_PRESET.headers.find(h => h.direction === header.direction)!.schema.example]);
      }
      // the preset itself is not changed by what was offered
      expect(TRACEPARENT_PRESET.headers.every(h => h.schema.example && !h.schema.examples)).toBe(true);
    });

    it('is changed back and forth with the version, for the headers of the preset only', () => {
      const oasDoc = doc([other, ...both(1)]);
      const otherSchema = {type: 'string', example: 'abc'};
      oasDoc.httpHeaders[0] = {...other, schema: otherSchema};
      const before = JSON.parse(JSON.stringify(oasDoc.httpHeaders.slice(1)));

      adaptPresetSchemas(oasDoc, TRACEPARENT_PRESET, true);
      expect(oasDoc.httpHeaders.slice(1).every(h => !('example' in h.schema) && Array.isArray(h.schema['examples']))).toBe(true);
      expect(oasDoc.httpHeaders[0].schema).toEqual(otherSchema);

      adaptPresetSchemas(oasDoc, TRACEPARENT_PRESET, false);
      expect(oasDoc.httpHeaders.slice(1)).toEqual(before);
    });

    it('leaves a schema that has no example, and a header the document has no preset of', () => {
      const plain = {type: 'string'};
      expect(schemaForVersion(plain, true)).toBe(plain);
      expect(schemaForVersion(plain, false)).toBe(plain);
      expect(() => adaptPresetSchemas(doc(), TRACEPARENT_PRESET, true)).not.toThrow();
    });

    it('keeps the examples the schema has when it has both', () => {
      expect(schemaForVersion({type: 'string', example: 'a', examples: ['b']}, true)).toEqual({type: 'string', examples: ['b']});
      expect(schemaForVersion({type: 'string', example: 'a', examples: ['b']}, false)).toEqual({type: 'string', example: 'a'});
      expect(schemaForVersion({type: 'string', example: sample}, true)).toEqual({type: 'string', examples: [sample]});
    });
  });

  describe('the pattern of traceparent', () => {
    const pattern = new RegExp(TRACEPARENT_PRESET.headers[0].schema.pattern);

    it('accepts a valid traceparent', () => {
      expect(pattern.test('00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01')).toBe(true);
    });

    it('rejects what W3C Trace Context forbids', () => {
      expect(pattern.test('ff-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01')).toBe(false);
      expect(pattern.test('00-00000000000000000000000000000000-00f067aa0ba902b7-01')).toBe(false);
      expect(pattern.test('00-4bf92f3577b34da6a3ce929d0e0e4736-0000000000000000-01')).toBe(false);
      expect(pattern.test('00-4BF92F3577B34DA6A3CE929D0E0E4736-00f067aa0ba902b7-01')).toBe(false);
      expect(pattern.test('00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7')).toBe(false);
    });
  });
});
