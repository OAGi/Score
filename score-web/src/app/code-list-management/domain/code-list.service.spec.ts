import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {CodeListService} from './code-list.service';

describe('CodeListService.checkUniqueness', () => {
  let service: CodeListService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({providers: [CodeListService, provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(CodeListService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const check = (agencyIdListValue: any) => {
    service.checkUniqueness({
      release: {releaseId: 1}, listId: 'L', versionId: '1', agencyIdListValue
    } as any).subscribe();
    const req = http.expectOne(r => r.url === '/api/code-lists/check-uniqueness');
    req.flush(false);
    return req.request.params;
  };

  it('omits the agency parameter when the code list has no Agency ID List Value', () => {
    expect(check(undefined).has('agencyIdListValueManifestId')).toBe(false);
    expect(check({agencyIdListValueManifestId: undefined}).has('agencyIdListValueManifestId')).toBe(false);
  });

  it('sends the Agency ID List Value manifest id when there is one', () => {
    expect(check({agencyIdListValueManifestId: 9}).get('agencyIdListValueManifestId')).toBe('9');
  });
});

describe('CodeListService.deleteComment', () => {
  let service: CodeListService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({providers: [CodeListService, provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(CodeListService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('edits a comment with PUT /api/comments/{commentId}', () => {
    service.editComment(7, 'text').subscribe();
    const req = http.expectOne('/api/comments/7');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.text).toBe('text');
    req.flush(null);
  });

  it('sends DELETE /api/comments/{commentId} without a body', () => {
    service.deleteComment({commentId: 7} as any).subscribe();
    const req = http.expectOne('/api/comments/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
