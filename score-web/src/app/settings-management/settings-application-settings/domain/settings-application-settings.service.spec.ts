import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {SettingsApplicationSettingsService} from './settings-application-settings.service';

describe('SettingsApplicationSettingsService', () => {
  let service: SettingsApplicationSettingsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SettingsApplicationSettingsService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(SettingsApplicationSettingsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('verifies the email address with PUT /api/accounts/email-validation', () => {
    service.validateEmail('token').subscribe();

    const req = http.expectOne('/api/accounts/email-validation');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({q: 'token'});
    req.flush(null);
  });

  it('previews a filename expression with the samples the user edited', () => {
    service.previewFilenameExpression('bie-schema', '{BIE ID}', '-{Incremental}', {'BIE ID': '7'}, ['A', 'B']).subscribe();

    const req = http.expectOne('/api/application/filename-expression/bie-schema/preview');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      expression: '{BIE ID}',
      duplicateHandlerExpression: '-{Incremental}',
      sampleValues: {'BIE ID': '7'},
      sampleBusinessContextNames: ['A', 'B']
    });
    req.flush({});
  });

  it('previews without samples so that the backend answers with its defaults', () => {
    service.previewFilenameExpression('bie-package-schema', '{BIE ID}', '').subscribe();

    const req = http.expectOne('/api/application/filename-expression/bie-package-schema/preview');
    expect(req.request.body.sampleValues).toBeUndefined();
    expect(req.request.body.sampleBusinessContextNames).toBeUndefined();
    req.flush({});
  });

  it('validates a filename expression pair', () => {
    service.validateFilenameExpression('bie-schema', '{BIE ID}', '~{BIE ID}').subscribe();

    const req = http.expectOne('/api/application/filename-expression/bie-schema/validate');
    expect(req.request.body).toEqual({expression: '{BIE ID}', duplicateHandlerExpression: '~{BIE ID}'});
    req.flush(null);
  });

  it('asks the backend for the suggestions at the caret', () => {
    let result: any;
    service.suggestFilenameExpression('bie-schema', '{BIE Pro', 8, false).subscribe(r => result = r);

    const req = http.expectOne('/api/application/filename-expression/bie-schema/suggest');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({text: '{BIE Pro', caret: 8, duplicateHandler: false});
    req.flush({from: 1, to: 8, items: [{label: 'BIE Property Term', insert: 'BIE Property Term', detail: 'd', caretBack: 0}]});
    expect(result.items[0].label).toBe('BIE Property Term');
  });

  it('marks the duplicate handler expression in the suggestion request', () => {
    service.suggestFilenameExpression('bie-package-schema', '-{', 2, true).subscribe();

    const req = http.expectOne('/api/application/filename-expression/bie-package-schema/suggest');
    expect(req.request.body.duplicateHandler).toBe(true);
    req.flush({from: 0, to: 0, items: []});
  });
});
