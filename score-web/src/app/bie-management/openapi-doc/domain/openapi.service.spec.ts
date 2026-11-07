import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {OpenAPIService} from './openapi.service';
import {AssignBieForOasDoc, OasDoc} from './openapi-doc';
import {offerPreset, TRACEPARENT_PRESET} from './oas-doc-http-header-preset';

describe('OpenAPIService.assignBieForOasDoc', () => {
  let service: OpenAPIService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OpenAPIService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OpenAPIService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('sends the tag name so the new operation is tagged', () => {
    const payload = new AssignBieForOasDoc();
    payload.oasDocId = 5;
    payload.topLevelAsbiepId = 11;
    payload.propertyTerm = 'Purchase Order';
    payload.verb = 'GET';
    payload.messageBody = 'Response';
    payload.operationId = 'getPurchaseOrder';
    payload.tagName = 'Purchase Order';

    service.assignBieForOasDoc(payload).subscribe();

    const req = httpTesting.expectOne('/api/oas_doc/5/bie_list');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.tagName).toBe('Purchase Order');
    req.flush({});
  });
});

/** Issue #1791: the HTTP headers of a document are a part of what the web app sends to create and to update it. */
describe('OpenAPIService HTTP headers of a document (#1791)', () => {
  let service: OpenAPIService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OpenAPIService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OpenAPIService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  function doc(httpHeaders?: any[]): OasDoc {
    const oasDoc = new OasDoc();
    oasDoc.oasDocId = 5;
    oasDoc.title = 't';
    oasDoc.version = '1';
    oasDoc.openAPIVersion = '3.1';
    oasDoc.httpHeaders = httpHeaders as any;
    return oasDoc;
  }

  it('sends the headers with an update, with the operation each one is for', () => {
    const oasDoc = doc([]);
    offerPreset(oasDoc, TRACEPARENT_PRESET, [7, 8]);

    service.updateOasDoc(oasDoc).subscribe();

    const req = httpTesting.expectOne('/api/oas_doc/5');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.httpHeaders.length).toBe(4);
    expect(req.request.body.httpHeaders.map((h: any) => [h.name, h.oasOperationId, h.direction])).toEqual([
      ['traceparent', 7, 'REQUEST'], ['traceparent', 7, 'RESPONSE'], ['traceparent', 8, 'REQUEST'], ['traceparent', 8, 'RESPONSE']]);
    expect(req.request.body.httpHeaders[0].schema.type).toBe('string');
    req.flush({});
  });

  it('sends an empty list to remove them, and nothing to leave them as they are', () => {
    service.updateOasDoc(doc([])).subscribe();
    const removing = httpTesting.expectOne('/api/oas_doc/5');
    expect(removing.request.body.httpHeaders).toEqual([]);
    removing.flush({});

    service.updateOasDoc(doc(undefined)).subscribe();
    const keeping = httpTesting.expectOne('/api/oas_doc/5');
    expect('httpHeaders' in JSON.parse(JSON.stringify(keeping.request.body))).toBe(false);
    keeping.flush({});
  });

  it('sends the headers with a create as well', () => {
    const oasDoc = doc([]);
    offerPreset(oasDoc, TRACEPARENT_PRESET, [1]);
    oasDoc.oasDocId = undefined as any;

    service.createOasDoc(oasDoc).subscribe();

    const req = httpTesting.expectOne('/api/oas_doc');
    expect(req.request.body.httpHeaders.length).toBe(2);
    req.flush({});
  });
});
