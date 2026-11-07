import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {OasDocHttpHeader} from '../domain/openapi-doc';
import {newHttpHeader} from '../domain/oas-doc-http-header-form';
import {OasDocHttpHeaderDialogComponent} from './oas-doc-http-header-dialog.component';

/** Issue #1791: the dialog is one form for a request parameter and a response header; what differs is what the location allows. */
describe('OasDocHttpHeaderDialogComponent (#1791)', () => {

  function open(header: OasDocHttpHeader, isNew = true, others: OasDocHttpHeader[] = [], extra: any = {}) {
    TestBed.resetTestingModule(); // a test may open more than one dialog
    const close = vi.fn();
    const data = {header, isNew, scopeLabel: '', others, ...extra};
    TestBed.configureTestingModule({
      providers: [{provide: MatDialogRef, useValue: {close}}, {provide: MAT_DIALOG_DATA, useValue: data}]
    });
    const dialog = TestBed.runInInjectionContext(() => new OasDocHttpHeaderDialogComponent(data));
    return {dialog, close};
  }

  const saved = (close: any): OasDocHttpHeader => close.mock.calls[0][0].header;

  describe('a request parameter', () => {
    it('is in the query at first, serialized with a schema in the default style', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      expect(dialog.location).toBe('query');
      expect(dialog.serialization).toBe('schema');
      expect(dialog.styleChoice).toBe('form'); // the default of the query
      expect(dialog.header.style).toBeUndefined(); // which is not kept
      expect(dialog.explodeChoice).toBe('true'); // the default of form
      expect(dialog.defaultExplodeOfStyle).toBe(true);
      expect(dialog.warnings).toEqual([]);
    });

    it('starts a new cookie with content, which OpenAPI recommends for it, and warns once it is a schema', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.location = 'cookie';

      expect(dialog.serialization).toBe('content');
      expect(dialog.header.contentMediaType).toBe('text/plain');
      expect(dialog.warnings).toEqual([]);
      dialog.setSerialization('schema');
      expect(dialog.warnings[0]).toContain('cookie');
      expect('contentMediaType' in dialog.header).toBe(false);
    });

    it('warns of a header only when its value is not URL-safe, as the example is written', () => {
      const {dialog} = open({...newHttpHeader('REQUEST', 1), in: 'header'});
      expect(dialog.warnings).toEqual([]);
      dialog.exampleText = '"a; b=c"';
      expect(dialog.warnings).toHaveLength(1);
      dialog.exampleText = '"abc"';
      expect(dialog.warnings).toEqual([]);
      dialog.exampleText = '{not json';
      expect(dialog.warnings).toEqual([]); // that is for the example to say
    });

    it('leaves an existing cookie as it is serialized', () => {
      const cookie: OasDocHttpHeader = {...newHttpHeader('REQUEST', 1), name: 'session', in: 'header'};
      const {dialog} = open(cookie, false);
      dialog.location = 'cookie';
      expect(dialog.serialization).toBe('schema');
    });

    it('drops the style the new location does not have', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.styleChoice = 'deepObject';
      dialog.header.allowReserved = true;
      dialog.location = 'path';

      expect(dialog.styleChoice).toBe('matrix'); // the default of the path
      expect(dialog.header.style).toBeUndefined();
      expect(dialog.header.allowReserved).toBeUndefined();
      expect(dialog.styles).toEqual(['matrix', 'label', 'simple']);
    });

    it('has allow reserved as a list of false (the default) and true, and keeps only true', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      expect(dialog.allowReservedChoice).toBe('false');
      dialog.allowReservedChoice = 'true';
      expect(dialog.header.allowReserved).toBe(true);
      expect(dialog.allowReservedChoice).toBe('true');
      dialog.allowReservedChoice = 'false';
      expect('allowReserved' in dialog.header).toBe(false);
    });

    it('has the allow reserved of a query parameter only', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      expect(dialog.isQueryParameter).toBe(true);
      dialog.location = 'header';
      expect(dialog.isQueryParameter).toBe(false);
    });

    it('saves a path parameter as required, whatever it was', () => {
      const {dialog, close} = open({...newHttpHeader('REQUEST', 1), name: 'id'});
      dialog.location = 'path';
      dialog.header.required = false;
      dialog.save();

      expect(saved(close).in).toBe('path');
      expect(saved(close).required).toBe(true);
    });

    it('saves the style, explode and allowReserved that were chosen', () => {
      const {dialog, close} = open({...newHttpHeader('REQUEST', 1), name: 'filter'});
      dialog.schemaText = '{"type":"object"}';
      dialog.styleChoice = 'deepObject';
      dialog.explodeChoice = 'true';
      dialog.header.allowReserved = true;
      dialog.save();

      expect(saved(close)).toMatchObject({in: 'query', style: 'deepObject', explode: true, allowReserved: true});
      expect('contentMediaType' in saved(close)).toBe(false);
    });

    it('keeps a style that is not the default, and not the one that is', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.styleChoice = 'pipeDelimited';
      expect(dialog.header.style).toBe('pipeDelimited');
      dialog.styleChoice = 'form';
      expect('style' in dialog.header).toBe(false);
    });

    it('does not touch the explode when the style that is picked is the one it has', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.explodeChoice = 'false';
      dialog.styleChoice = 'form'; // it is form already
      expect(dialog.header.explode).toBe(false);
    });

    it('shows the explode of the style as the default, and goes to the default of another style', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      expect(dialog.explodeChoice).toBe('true');
      expect(dialog.defaultExplodeOfStyle).toBe(true);

      dialog.styleChoice = 'pipeDelimited';
      expect(dialog.explodeChoice).toBe('false');
      expect(dialog.defaultExplodeOfStyle).toBe(false);

      dialog.styleChoice = 'form'; // the default of the location
      expect(dialog.explodeChoice).toBe('true');
    });

    it('goes back to the default of the style when the style changes, whatever explode was picked', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.explodeChoice = 'false'; // not the default of form
      expect(dialog.header.explode).toBe(false);

      dialog.styleChoice = 'spaceDelimited';
      expect('explode' in dialog.header).toBe(false);
      expect(dialog.explodeChoice).toBe('false');
    });

    it('goes back to the default of the style when the location changes', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.explodeChoice = 'false';
      dialog.location = 'path'; // matrix: false
      expect('explode' in dialog.header).toBe(false);
      expect(dialog.explodeChoice).toBe('false');
      dialog.location = 'cookie'; // form: true
      expect(dialog.explodeChoice).toBe('true');
    });

    it('does not keep what is the default of the style as an explode of its own', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.explodeChoice = 'true'; // the default of form
      expect('explode' in dialog.header).toBe(false);
      dialog.explodeChoice = 'false';
      expect(dialog.header.explode).toBe(false);
      dialog.explodeChoice = 'true';
      expect('explode' in dialog.header).toBe(false);
    });

    it('needs explode for deepObject, which it sets, and says so when it is taken away', () => {
      const {dialog} = open(newHttpHeader('REQUEST', 1));
      dialog.schemaText = '{"type":"object"}';
      dialog.styleChoice = 'deepObject';
      expect(dialog.header.explode).toBe(true);
      expect(dialog.explodeChoice).toBe('true');
      expect(dialog.defaultExplodeOfStyle).toBe(false);
      expect(dialog.errors.serialization).toBeUndefined();

      dialog.explodeChoice = 'false';
      expect(dialog.errors.serialization).toBeTruthy();
      expect(dialog.isValid).toBe(false);
    });

    it('saves explode false apart from the default, which is no explode at all', () => {
      const {dialog, close} = open({...newHttpHeader('REQUEST', 1), name: 'ids'});
      dialog.explodeChoice = 'false';
      dialog.save();
      expect(saved(close).explode).toBe(false);

      const other = open({...newHttpHeader('REQUEST', 1), name: 'ids'});
      other.dialog.save();
      expect('explode' in saved(other.close)).toBe(false);
      expect('style' in saved(other.close)).toBe(false);
    });

    it('saves content without a style, explode or allowReserved', () => {
      const {dialog, close} = open({...newHttpHeader('REQUEST', 1), name: 'filter'});
      dialog.styleChoice = 'pipeDelimited';
      dialog.explodeChoice = 'true';
      dialog.header.allowReserved = true;
      dialog.setSerialization('content');
      dialog.header.contentMediaType = ' application/json ';
      dialog.save();

      const result = saved(close);
      expect(result.contentMediaType).toBe('application/json');
      expect('style' in result).toBe(false);
      expect('explode' in result).toBe(false);
      expect('allowReserved' in result).toBe(false);
    });

    it('remembers the media type when it goes back to a schema and then to content again', () => {
      const {dialog} = open({...newHttpHeader('REQUEST', 1), name: 'x'});
      dialog.setSerialization('content');
      dialog.header.contentMediaType = 'application/json';
      dialog.setSerialization('schema');
      dialog.setSerialization('content');
      expect(dialog.header.contentMediaType).toBe('application/json');
    });

    it('does not save an invalid media type', () => {
      const {dialog, close} = open({...newHttpHeader('REQUEST', 1), name: 'x'});
      dialog.setSerialization('content');
      dialog.header.contentMediaType = 'text';
      expect(dialog.isValid).toBe(false);
      dialog.save();
      expect(close).not.toHaveBeenCalled();
    });
  });

  describe('a response header', () => {
    it('is the same form with the location fixed to a header', () => {
      const {dialog} = open(newHttpHeader('RESPONSE', 1));
      expect(dialog.isRequest).toBe(false);
      expect(dialog.location).toBe('header');
      dialog.location = 'cookie'; // the select is disabled; nothing changes anyway
      expect(dialog.location).toBe('header');
      expect(dialog.styles).toEqual(['simple']);
      expect(dialog.isQueryParameter).toBe(false);
    });

    it('is saved without a location, and with content when it is given', () => {
      const {dialog, close} = open({...newHttpHeader('RESPONSE', 1), name: 'x-total'});
      dialog.setSerialization('content');
      dialog.save();

      const result = saved(close);
      expect('in' in result).toBe(false);
      expect(result.direction).toBe('RESPONSE');
      expect(result.contentMediaType).toBe('text/plain');
    });

    it('does not take a style of a query', () => {
      const {dialog} = open({...newHttpHeader('RESPONSE', 1), name: 'x-total'});
      dialog.header.style = 'form';
      expect(dialog.isValid).toBe(false);
    });
  });

  describe('what the dialog knows of the operation and of the document', () => {
    it('refuses a path parameter that is not in the path of the operation, and takes one that is', () => {
      const {dialog} = open({...newHttpHeader('REQUEST', 1), name: 'other', in: 'path'}, true, [], {resourcePath: '/things/{id}'});
      expect(dialog.isValid).toBe(false);
      expect(dialog.errors.name).toContain('has no {other}');
      dialog.header.name = 'id';
      expect(dialog.isValid).toBe(true);
    });

    it('refuses what OpenAPI 3.0 has not, and takes it in 3.1', () => {
      const header = {...newHttpHeader('REQUEST', 1), name: 'x-trace', in: 'header' as const};
      const v30 = open(header, true, [], {openApiVersion: '3.0'}).dialog;
      v30.schemaText = '{"type":["string","null"]}';
      expect(v30.errors.schema).toContain('3.0');
      const v31 = open(header, true, [], {openApiVersion: '3.1'}).dialog;
      v31.schemaText = '{"type":["string","null"]}';
      expect(v31.isValid).toBe(true);
    });

    it('refuses a style that is not for the type of the schema', () => {
      const {dialog} = open({...newHttpHeader('REQUEST', 1), name: 'ids'});
      dialog.styleChoice = 'pipeDelimited';
      expect(dialog.errors.serialization).toContain('array or object');
      dialog.schemaText = '{"type":"array","items":{"type":"string"}}';
      expect(dialog.isValid).toBe(true);
    });

    it('warns of a Cookie header and of an example that is not of the type', () => {
      const {dialog} = open({...newHttpHeader('REQUEST', 1), name: 'Cookie', in: 'header'});
      dialog.schemaText = '{"type":"integer"}';
      dialog.exampleText = '"abc"';
      expect(dialog.warnings.some(w => w.includes('cookie location'))).toBe(true);
      expect(dialog.warnings.some(w => w.includes('example is a string'))).toBe(true);
      expect(dialog.isValid).toBe(true); // a warning does not stop the user
    });
  });
});
