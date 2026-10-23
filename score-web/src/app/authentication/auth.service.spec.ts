import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {Router} from '@angular/router';
import {AuthService} from './auth.service';

describe('AuthService.canUsePasskeys', () => {
  let service: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting(), {provide: Router, useValue: {}}]
    });
    service = TestBed.inject(AuthService);
  });

  function signedIn(authentication: string, passkeyEnabled: boolean | undefined) {
    service.storeUserInfo({
      username: 'alice', roles: ['end-user'], authentication, enabled: true,
      ...(passkeyEnabled === undefined ? {} : {passkey: {enabled: passkeyEnabled}})
    } as any);
  }

  it('is true for a password or passkey account when passkey sign-in is on', () => {
    signedIn('basic', true);
    expect(service.canUsePasskeys()).toBe(true);
    signedIn('passkey', true);
    expect(service.canUsePasskeys()).toBe(true);
  });

  it('is false when passkey sign-in is off', () => {
    signedIn('basic', false);
    expect(service.canUsePasskeys()).toBe(false);
  });

  it('is false for a single sign-on account, which uses its provider\'s passkeys', () => {
    signedIn('oauth2', true);
    expect(service.canUsePasskeys()).toBe(false);
  });

  it('is false for an older stored session that predates the setting', () => {
    signedIn('basic', undefined);
    expect(service.canUsePasskeys()).toBe(false);
  });

  it('is false when nobody is signed in', () => {
    expect(service.canUsePasskeys()).toBe(false);
  });
});
