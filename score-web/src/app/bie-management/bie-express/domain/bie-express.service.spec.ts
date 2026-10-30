import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {BieExpressService} from './bie-express.service';
import {BieExpressOption} from './generate-expression';

describe('BieExpressService', () => {
  let service: BieExpressService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BieExpressService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(BieExpressService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** The generate request carries its parameters base64-encoded in `data`. */
  function sentParams(option: Partial<BieExpressOption>): URLSearchParams {
    service.generate([1], option as BieExpressOption).subscribe();
    const req = http.expectOne(r => r.url === '/api/profile_bie/generate');
    const decoded = new TextDecoder().decode(Uint8Array.from(atob(req.request.params.get('data')!), c => c.charCodeAt(0)));
    req.flush(new Blob());
    return new URLSearchParams(decoded);
  }

  it('sends an unchecked BIE Definition as false so the server default of true does not apply', () => {
    const params = sentParams({bieDefinition: false} as Partial<BieExpressOption>);

    expect(params.get('bieDefinition')).toBe('false');
  });

  it('still sends a checked option as true and omits an undefined one', () => {
    const params = sentParams({bieDefinition: true, bieCardinality: undefined} as any);

    expect(params.get('bieDefinition')).toBe('true');
    expect(params.has('bieCardinality')).toBe(false);
  });
});
