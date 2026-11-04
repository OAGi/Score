import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ModuleService} from './module.service';
import {ModuleSetReleaseListRequest} from './module';

describe('ModuleService.getModuleSetReleaseList', () => {
  let service: ModuleService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({providers: [ModuleService, provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(ModuleService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const paramsOf = (isDefault: boolean) => {
    const request = new ModuleSetReleaseListRequest();
    request.library.libraryId = 1;
    request.page = {pageIndex: 0, pageSize: 10, sortActive: '', sortDirection: ''} as any;
    request.isDefault = isDefault;
    service.getModuleSetReleaseList(request).subscribe();
    const req = http.expectOne(r => r.url === '/api/module-set-releases');
    req.flush({list: []});
    return req.request.params;
  };

  it('sends the default filter under the name that the endpoint reads', () => {
    expect(paramsOf(true).get('default')).toBe('true');
  });

  it('omits the default filter when it is not requested', () => {
    expect(paramsOf(false).has('default')).toBe(false);
  });
});
