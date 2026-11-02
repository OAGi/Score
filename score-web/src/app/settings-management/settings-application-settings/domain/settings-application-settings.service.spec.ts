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
});
