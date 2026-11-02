import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {OpenAPIService} from './openapi.service';
import {AssignBieForOasDoc} from './openapi-doc';

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
