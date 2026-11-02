import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {BieListService} from './bie-list.service';
import {BieListRequest} from './bie-list';

describe('BieListService inherited-base filter', () => {
  let service: BieListService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        BieListService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(BieListService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('omits a missing inherited-base id instead of sending a zero filter', () => {
    const request = new BieListRequest();
    request.library.libraryId = 3;
    request.basedTopLevelAsbiepIds = [undefined, 0] as unknown as number[];

    service.getBieListWithRequest(request).subscribe();

    const httpRequest = httpTesting.expectOne(request => request.url === '/api/bies');
    expect(httpRequest.request.params.has('basedTopLevelAsbiepIds')).toBe(false);
    httpRequest.flush({length: 0, list: []});
  });

  it('retains a valid inherited-base id when invalid values are mixed in', () => {
    const request = new BieListRequest();
    request.library.libraryId = 3;
    request.basedTopLevelAsbiepIds = [undefined, 27] as unknown as number[];

    service.getBieListWithRequest(request).subscribe();

    const httpRequest = httpTesting.expectOne(request => request.url === '/api/bies');
    expect(httpRequest.request.params.get('basedTopLevelAsbiepIds')).toBe('27');
    httpRequest.flush({length: 0, list: []});
  });
});
