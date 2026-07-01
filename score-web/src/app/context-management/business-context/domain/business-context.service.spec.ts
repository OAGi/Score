import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {BusinessContextService} from './business-context.service';
import {BusinessContext} from './business-context';

/**
 * Locks the REST contract the BIE editor relies on for immediate assign/unassign (Business Contexts are
 * persisted the moment a chip is added/removed, not on BIE save) and the single-vs-bulk discard split.
 * These are the client half of the #1744-class hardening: a regression here silently breaks assignment.
 */
describe('BusinessContextService REST contract', () => {
  let service: BusinessContextService;
  let httpTesting: HttpTestingController;

  const ctx = (id: number): BusinessContext =>
    ({businessContextId: id, name: 'ctx-' + id, guid: 'g-' + id} as BusinessContext);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        BusinessContextService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(BusinessContextService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('POSTs an assignment to /{bizCtxId}/assignments/{topLevelAsbiepId}', () => {
    service.assign(1001, ctx(7)).subscribe();
    const req = httpTesting.expectOne('/api/business-contexts/7/assignments/1001');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('DELETEs the assignment at the same path when unassigning', () => {
    service.unassign(1001, ctx(7)).subscribe();
    const req = httpTesting.expectOne('/api/business-contexts/7/assignments/1001');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('deletes a single business context by path', () => {
    service.delete(7).subscribe();
    const req = httpTesting.expectOne('/api/business-contexts/7');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('deletes multiple business contexts via a body id list', () => {
    service.delete(7, 8).subscribe();
    const req = httpTesting.expectOne('/api/business-contexts');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.body).toEqual({businessContextIdList: [7, 8]});
    req.flush({});
  });
});
