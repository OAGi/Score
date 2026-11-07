import {OasDoc, OasDocHttpHeader} from './openapi-doc';
import {
  dropStrayPathParameters,
  OperationRow,
  pathParameterNames,
  reconcileOperationParameters,
  stateOfRows
} from './oas-operation-parameters';

/** Issue #1791: the request parameters that follow an operation (sinceLastDateTime, the path parameters). */
describe('operation parameters (#1791)', () => {

  const row = (verb: string, arrayIndicator: boolean, resourceName: string): OperationRow =>
    ({oasOperationId: 7, verb, arrayIndicator, resourceName});

  const doc = (httpHeaders: OasDocHttpHeader[] = []): OasDoc => {
    const oasDoc = new OasDoc();
    oasDoc.httpHeaders = httpHeaders;
    return oasDoc;
  };

  const names = (oasDoc: OasDoc) => oasDoc.httpHeaders.map(h => (h.in || 'header') + ' ' + h.name);

  describe('state of the rows', () => {
    it('takes every {name} of the path once, in order, and not the {version} of a document that has one', () => {
      expect(pathParameterNames('/a/{x}/b/{y}/{x}')).toEqual(['x', 'y']);
      expect(pathParameterNames('/{version}/orders/{id}', 'V3')).toEqual(['id']);
      expect(pathParameterNames('/{version}/orders', '')).toEqual(['version']);
      expect(pathParameterNames('/a/{ }/{b', 'V1')).toEqual([]);
    });

    it('has sinceLastDateTime for a GET with an array body only', () => {
      expect(stateOfRows([row('GET', true, '/o')]).sinceLastDateTime).toBe(true);
      expect(stateOfRows([row('get', true, '/o')]).sinceLastDateTime).toBe(true);
      expect(stateOfRows([row('GET', false, '/o')]).sinceLastDateTime).toBe(false);
      expect(stateOfRows([row('POST', true, '/o')]).sinceLastDateTime).toBe(false);
      expect(stateOfRows([row('GET', false, '/o'), row('GET', true, '/o')]).sinceLastDateTime).toBe(true);
    });
  });

  describe('following a change of the operation', () => {
    it('adds sinceLastDateTime when a GET becomes an array and takes it away when it stops being one', () => {
      const oasDoc = doc();
      const single = stateOfRows([row('GET', false, '/orders')]);
      const array = stateOfRows([row('GET', true, '/orders')]);

      reconcileOperationParameters(oasDoc, 7, single, array);
      expect(names(oasDoc)).toEqual(['query sinceLastDateTime']);
      expect(oasDoc.httpHeaders[0].oasOperationId).toBe(7);
      expect(oasDoc.httpHeaders[0].required).toBe(false);

      reconcileOperationParameters(oasDoc, 7, array, single);
      expect(names(oasDoc)).toEqual([]);
    });

    it('does not add what the operation or the document has already, nor touch another operation', () => {
      const own: OasDocHttpHeader = {name: 'sinceLastDateTime', in: 'query', direction: 'REQUEST', schema: {type: 'string'}, required: true};
      const other: OasDocHttpHeader = {...own, oasOperationId: 8};
      const oasDoc = doc([own, other]);
      reconcileOperationParameters(oasDoc, 7, stateOfRows([row('GET', false, '/o')]), stateOfRows([row('GET', true, '/o')]));
      expect(oasDoc.httpHeaders.length).toBe(2);
      reconcileOperationParameters(oasDoc, 7, stateOfRows([row('GET', true, '/o')]), stateOfRows([row('GET', false, '/o')]));
      // a header of the whole document is not the operation's to take away; the one of another operation neither
      expect(oasDoc.httpHeaders.length).toBe(2);
    });

    it('registers a path parameter for a new name, renames it with the name and removes it with the name', () => {
      const oasDoc = doc();
      const none = stateOfRows([row('DELETE', false, '/orders')]);
      const byId = stateOfRows([row('DELETE', false, '/orders/{id}')]);
      const byOrderId = stateOfRows([row('DELETE', false, '/orders/{orderId}')]);

      reconcileOperationParameters(oasDoc, 7, none, byId);
      expect(names(oasDoc)).toEqual(['path id']);
      expect(oasDoc.httpHeaders[0].required).toBe(true);

      oasDoc.httpHeaders[0].description = 'mine';
      reconcileOperationParameters(oasDoc, 7, byId, byOrderId);
      expect(names(oasDoc)).toEqual(['path orderId']);
      expect(oasDoc.httpHeaders[0].description).toBe('mine');

      reconcileOperationParameters(oasDoc, 7, byOrderId, none);
      expect(names(oasDoc)).toEqual([]);
    });

    it('renames several names in order and adds the one left over', () => {
      const oasDoc = doc();
      const before = stateOfRows([row('GET', false, '/a/{x}/b/{y}')]);
      reconcileOperationParameters(oasDoc, 7, stateOfRows([row('GET', false, '/a')]), before);
      reconcileOperationParameters(oasDoc, 7, before, stateOfRows([row('GET', false, '/a/{p}/b/{q}/c/{r}')]));
      expect(names(oasDoc)).toEqual(['path p', 'path q', 'path r']);
    });

    it('does not bring back an entry that was deleted when the operation changes into no case of it', () => {
      const oasDoc = doc();
      const state = stateOfRows([row('GET', true, '/orders/{id}')]);
      reconcileOperationParameters(oasDoc, 7, state, state);
      expect(names(oasDoc)).toEqual([]);
    });
  });
  describe('path parameters the path does not have', () => {
    const entry = (oasOperationId: number | undefined, in_: string, name: string): OasDocHttpHeader =>
      ({oasOperationId, in: in_, name, direction: 'REQUEST', schema: {type: 'string'}} as OasDocHttpHeader);

    it('drops the path parameter of an edit that was not kept, and nothing else', () => {
      const oasDoc = doc([
        entry(7, 'path', 'tru'), entry(7, 'path', 'id'), entry(7, 'query', 'tru'), entry(8, 'path', 'other'),
        entry(undefined, 'path', 'tru')]);

      expect(dropStrayPathParameters(oasDoc, [row('GET', false, '/{version}/orders/{id}')], 'V3')).toBe(true);
      expect(names(oasDoc)).toEqual(['path id', 'query tru', 'path other', 'path tru']);
    });

    it('does nothing when every path parameter is in the path', () => {
      const oasDoc = doc([entry(7, 'path', 'id')]);

      expect(dropStrayPathParameters(oasDoc, [row('GET', false, '/orders/{id}')])).toBe(false);
      expect(names(oasDoc)).toEqual(['path id']);
    });
  });
});
