import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {BiePackageService} from './bie-package.service';
import {BieListInBiePackageRequest} from './bie-package';

describe('BiePackageService getBieListInBiePackage', () => {
  let service: BiePackageService;
  let httpTesting: HttpTestingController;

  const request = (start?: Date, end?: Date): BieListInBiePackageRequest => {
    const r = new BieListInBiePackageRequest();
    r.biePackageId = 7;
    r.updatedDate = {start, end};
    return r;
  };

  const sentParams = (r: BieListInBiePackageRequest) => {
    service.getBieListInBiePackage(r).subscribe();
    const req = httpTesting.expectOne(x => x.url === '/api/bie-packages/7/bies');
    req.flush({list: [], page: 0, size: 0, length: 0});
    return req.request.params;
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BiePackageService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(BiePackageService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('sends the Updated range as lastUpdatedOn, which the endpoint reads', () => {
    const params = sentParams(request(new Date(1000), new Date(2000)));
    expect(params.get('lastUpdatedOn')).toBe('[1000~2000]');
    expect(params.has('updateStart')).toBe(false);
    expect(params.has('updateEnd')).toBe(false);
  });

  it('leaves the missing bound empty', () => {
    expect(sentParams(request(new Date(1000), undefined)).get('lastUpdatedOn')).toBe('[1000~]');
  });

  it('leaves the other missing bound empty', () => {
    expect(sentParams(request(undefined, new Date(2000))).get('lastUpdatedOn')).toBe('[~2000]');
  });

  it('sends nothing when no date is chosen', () => {
    expect(sentParams(request()).has('lastUpdatedOn')).toBe(false);
  });
});
