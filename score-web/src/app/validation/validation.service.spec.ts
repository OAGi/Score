import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {ValidationService} from './validation.service';

describe('ValidationService', () => {
  let service: ValidationService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(ValidationService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('validates an object of a kind on request', () => {
    service.validate('ACC', 12).subscribe();

    const request = httpTesting.expectOne(r => r.url === '/api/validations/acc/12');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('trigger')).toBe('ON_DEMAND');
    request.flush({});
  });

  it('asks what a move to a base ACC would break, for an ASCC and for a BCC', () => {
    service.refactorCheck('ASCC', 3, 4).subscribe();
    service.refactorCheck('BCC', 5, 6).subscribe();

    const ascc = httpTesting.expectOne(r => r.url === '/api/core-components/ascc/refactor/check');
    expect(ascc.request.params.get('targetManifestId')).toBe('3');
    expect(ascc.request.params.get('destinationManifestId')).toBe('4');
    ascc.flush({});
    const bcc = httpTesting.expectOne(r => r.url === '/api/core-components/bcc/refactor/check');
    expect(bcc.request.params.get('targetManifestId')).toBe('5');
    expect(bcc.request.params.get('destinationManifestId')).toBe('6');
    bcc.flush({});
  });

  it('asks what appending an ASCCP would break by the id of the ASCCP', () => {
    service.appendCheck(7, 'ASCCP', 8).subscribe();

    const request = httpTesting.expectOne(r => r.url === '/api/core-components/acc/7/append/check');
    expect(request.request.params.get('asccpManifestId')).toBe('8');
    expect(request.request.params.has('bccpManifestId')).toBe(false);
    request.flush({});
  });

  it('asks what appending a BCCP would break by the id of the BCCP, whatever the case of the kind', () => {
    service.appendCheck(7, 'bccp', 9).subscribe();

    const request = httpTesting.expectOne(r => r.url === '/api/core-components/acc/7/append/check');
    expect(request.request.params.get('bccpManifestId')).toBe('9');
    expect(request.request.params.has('asccpManifestId')).toBe(false);
    request.flush({});
  });

  it('asks what a base ACC would break', () => {
    service.baseCheck(7, 10).subscribe();

    const request = httpTesting.expectOne(r => r.url === '/api/core-components/acc/7/base/check');
    expect(request.request.params.get('basedAccManifestId')).toBe('10');
    request.flush({});
  });
});
