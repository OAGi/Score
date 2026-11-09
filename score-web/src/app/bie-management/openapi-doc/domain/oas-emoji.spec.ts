import {hashCode} from '../../../common/utility';
import {OasDocHttpHeader} from './openapi-doc';
import {OasDocHttpHeaderDraft, validateHeaderDraft} from './oas-doc-http-header-form';
import {
  buildOperationId,
  extractBieName,
  oasResourcePathError,
  operationName,
  resourcePathLeaf
} from './oas-operation-validation';

/**
 * Issue #1892: the text of an OpenAPI Document may hold emoji, which are two UTF-16 units each. What the editor
 * builds, validates and compares from such text must not reject them where the specification allows them, cut one in two,
 * or take two different ones for the same.
 */
describe('OpenAPI Document text with emoji (#1892)', () => {

  const SMILE = '\u{1F600}';
  const GRIN = '\u{1F601}';
  const FAMILY = '\u{1F468}‍\u{1F469}‍\u{1F467}';

  describe('resource path', () => {
    it('accepts emoji in a segment and in a path parameter', () => {
      expect(oasResourcePathError(`/pets${SMILE}/{id${SMILE}}`)).toBe('');
      expect(oasResourcePathError(`/pets/{${FAMILY}}`)).toBe('');
    });

    it('still reports an empty or unbalanced path parameter next to an emoji', () => {
      expect(oasResourcePathError(`/pets${SMILE}/{}`)).toContain('must not be empty');
      expect(oasResourcePathError(`/pets${SMILE}/{id`)).toContain('unbalanced');
    });

    it('keeps the emoji of a property term in the leaf', () => {
      expect(resourcePathLeaf(`Pet ${SMILE}`, true)).toBe(`pet-${SMILE}-list`);
    });
  });

  describe('operation ID', () => {
    it('is built from a name with emoji, and the name is read back whole', () => {
      const operationId = buildOperationId('GET', `Pet ${SMILE}`, false);
      expect(operationId).toBe(`queryPet${SMILE}`);
      expect(extractBieName(operationId, false)).toBe(`Pet${SMILE}`);
      expect(extractBieName(buildOperationId('GET', `Pet ${SMILE}`, true), true)).toBe(`Pet${SMILE}`);
    });

    it('is not cut in two by the capitalization of an emoji at the start of the name', () => {
      expect(operationName(`${SMILE}pet`)).toBe(`${SMILE}pet`);
    });
  });

  describe('request parameter name', () => {
    const draft = (over: Partial<OasDocHttpHeaderDraft>): OasDocHttpHeaderDraft => ({
      name: 'id', schemaText: '{"type":"string"}', exampleText: '', direction: 'REQUEST', ...over
    });
    const existing = (name: string, over: Partial<OasDocHttpHeader> = {}): OasDocHttpHeader => ({
      name, schema: {type: 'string'}, direction: 'REQUEST', required: false, ...over
    });

    it('may hold emoji in a query parameter, a cookie and a path parameter', () => {
      expect(validateHeaderDraft(draft({name: `id${SMILE}`, in: 'query'}), []).name).toBeUndefined();
      expect(validateHeaderDraft(draft({name: `id${SMILE}`, in: 'cookie'}), []).name).toBeUndefined();
      expect(validateHeaderDraft(draft({name: `id${SMILE}`, in: 'path'}), [], 1, {resourcePath: `/pets/{id${SMILE}}`}).name)
        .toBeUndefined();
    });

    it('may not in an HTTP header name, which is ASCII only', () => {
      expect(validateHeaderDraft(draft({name: `X-Id${SMILE}`, in: 'header'}), []).name).toBeDefined();
    });

    it('is a duplicate of the same emoji only, not of another one', () => {
      const others = [existing(`id${SMILE}`, {in: 'query'})];
      expect(validateHeaderDraft(draft({name: `id${GRIN}`, in: 'query'}), others).duplicate).toBeUndefined();
      expect(validateHeaderDraft(draft({name: `id${SMILE}`, in: 'query'}), others).duplicate).toBeDefined();
    });
  });

  describe('change detection', () => {
    it('tells two documents that differ only in an emoji apart', () => {
      const first = hashCode({title: `Title ${SMILE}`});
      expect(hashCode({title: `Title ${SMILE}`})).toBe(first);
      expect(hashCode({title: `Title ${GRIN}`})).not.toBe(first);
      expect(hashCode({title: 'Title'})).not.toBe(first);
    });
  });
});
