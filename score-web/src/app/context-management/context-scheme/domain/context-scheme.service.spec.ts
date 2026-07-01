import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ContextSchemeService} from './context-scheme.service';

describe('ContextSchemeService uniqueness checks', () => {
  let service: ContextSchemeService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ContextSchemeService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ContextSchemeService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('uses the create endpoint when no context scheme id is supplied', () => {
    service.checkUniqueness('scheme-id', 'agency-id', '1.0')
      .subscribe(resp => expect(resp).toBe(false));

    const req = httpTesting.expectOne(request =>
      request.method === 'GET' &&
      request.url === '/api/context-schemes/check-uniqueness' &&
      request.params.get('schemeId') === 'scheme-id' &&
      request.params.get('schemeAgencyId') === 'agency-id' &&
      request.params.get('schemeVersionId') === '1.0');
    req.flush(false);
  });

  it('uses the edit endpoint when a context scheme id is supplied', () => {
    service.checkUniqueness('scheme-id', 'agency-id', '1.0', 42)
      .subscribe(resp => expect(resp).toBe(false));

    const req = httpTesting.expectOne(request =>
      request.method === 'GET' &&
      request.url === '/api/context-schemes/42/check-uniqueness' &&
      request.params.get('schemeId') === 'scheme-id' &&
      request.params.get('schemeAgencyId') === 'agency-id' &&
      request.params.get('schemeVersionId') === '1.0');
    req.flush(false);
  });

  it('uses the edit endpoint for scheme-name warnings when a context scheme id is supplied', () => {
    service.checkNameUniqueness('scheme name', 'scheme-id', 'agency-id', '1.0', 42)
      .subscribe(resp => expect(resp).toBe(false));

    const req = httpTesting.expectOne(request =>
      request.method === 'GET' &&
      request.url === '/api/context-schemes/42/check-name-uniqueness' &&
      request.params.get('schemeName') === 'scheme name' &&
      request.params.get('schemeId') === 'scheme-id' &&
      request.params.get('schemeAgencyId') === 'agency-id' &&
      request.params.get('schemeVersionId') === '1.0');
    req.flush(false);
  });

  it('deletes a single context scheme by path', () => {
    service.delete(7).subscribe();
    const req = httpTesting.expectOne('/api/context-schemes/7');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('deletes multiple context schemes via a body id list', () => {
    service.delete(7, 8).subscribe();
    const req = httpTesting.expectOne('/api/context-schemes');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.body).toEqual({contextSchemeIdList: [7, 8]});
    req.flush({});
  });
});
