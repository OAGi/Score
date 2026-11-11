import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {MatDialog} from '@angular/material/dialog';
import {AgencyIdListService} from './agency-id-list.service';

describe('AgencyIdListService comments', () => {
  let service: AgencyIdListService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AgencyIdListService, provideHttpClient(), provideHttpClientTesting(),
        {provide: MatDialog, useValue: {}}]
    });
    service = TestBed.inject(AgencyIdListService);
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

  it('deletes a comment with DELETE /api/comments/{commentId} without a body', () => {
    service.deleteComment({commentId: 7} as any).subscribe();
    const req = http.expectOne('/api/comments/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
