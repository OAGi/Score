import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {provideHttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {of} from 'rxjs';
import {vi} from 'vitest';
import {LoginComponent} from './login.component';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../basis.service';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyCancelledError} from '../../authentication/passkey/passkey';
import {PasskeyOfferService} from '../passkey-offer/passkey-offer.service';

const oidcApp = {
  loginUrl: 'oauth2/authorization/acme', providerName: 'acme', displayProviderName: 'Acme',
  backgroundColor: '#123456', fontColor: '#ffffff'
};

describe('LoginComponent', () => {
  it('should be defined', () => {
    expect(LoginComponent).toBeTruthy();
  });
});

describe('LoginComponent passkey sign-in', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let router: { navigateByUrl: ReturnType<typeof vi.fn> };
  let auth: Record<string, any>;
  let passkey: Record<string, any>;
  let snackBar: { open: ReturnType<typeof vi.fn> };
  let offer: { offerAfterPasswordSignIn: ReturnType<typeof vi.fn> };

  function setup(options: {
    supported?: boolean; enabled?: boolean; oidc?: any[]; conditional?: boolean; hasPasskey?: boolean;
  } = {}) {
    router = {navigateByUrl: vi.fn().mockResolvedValue(true)};
    snackBar = {open: vi.fn()};
    offer = {offerAfterPasswordSignIn: vi.fn()};
    auth = {
      isAuthenticated: vi.fn().mockReturnValue(false),
      nextParam: vi.fn().mockReturnValue('/'),
      getOAuth2AppInfos: vi.fn().mockReturnValue(of(options.oidc ?? [])),
      getUserToken: vi.fn().mockReturnValue({roles: ['']}),
      storeUserInfo: vi.fn(),
      authenticate: vi.fn()
    };
    passkey = {
      supported: options.supported ?? true,
      getInfo: vi.fn().mockReturnValue(of({enabled: options.enabled ?? true})),
      isConditionalMediationAvailable: vi.fn().mockResolvedValue(options.conditional ?? false),
      hasPasskey: vi.fn().mockReturnValue(of(options.hasPasskey ?? false)),
      signIn: vi.fn().mockReturnValue(new Promise(() => {
      })),
      cancelPending: vi.fn()
    };

    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [FormsModule],
      providers: [
        provideHttpClient(),
        {provide: AuthService, useValue: auth},
        {provide: WebPageInfoService, useValue: {load: () => of(null)}},
        {provide: PasskeyService, useValue: passkey},
        {provide: PasskeyOfferService, useValue: offer},
        {provide: Router, useValue: router},
        {provide: ActivatedRoute, useValue: {queryParams: of({})}},
        {provide: MatSnackBar, useValue: snackBar}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  }

  const el = (): HTMLElement => fixture.nativeElement;
  const separators = () => el().querySelectorAll('.or-container').length;
  const passkeyButton = () => el().querySelector('.passkey-login-button') as HTMLButtonElement | null;
  const oidcLinks = () => el().querySelectorAll('.oauth2-login-link');
  const usernameInput = () => el().querySelector('#login_field') as HTMLInputElement;

  describe('layout', () => {
    it('looks exactly like before when passkeys are off and there is no single sign-on', () => {
      setup({enabled: false});
      expect(separators()).toBe(0);
      expect(passkeyButton()).toBeNull();
      expect(usernameInput().getAttribute('autocomplete')).toBe('username');
    });

    it('shows one separator and the passkey button when only passkeys are on', () => {
      setup({enabled: true});
      expect(separators()).toBe(1);
      expect(passkeyButton()).not.toBeNull();
      expect(oidcLinks().length).toBe(0);
    });

    it('shows one separator and the provider buttons when only single sign-on is on', () => {
      setup({enabled: false, oidc: [oidcApp]});
      expect(separators()).toBe(1);
      expect(passkeyButton()).toBeNull();
      expect(oidcLinks().length).toBe(1);
    });

    it('shows ONE separator, with the passkey button first, when both are on', () => {
      setup({enabled: true, oidc: [oidcApp]});
      expect(separators()).toBe(1);
      const block = el().querySelector('.oauth2-form-body') as HTMLElement;
      expect(block.firstElementChild.querySelector('.passkey-login-button')).not.toBeNull();
      expect(oidcLinks().length).toBe(1);
    });

    it('hides the passkey button when the browser has no WebAuthn, even if enabled', () => {
      setup({enabled: true, supported: false, oidc: [oidcApp]});
      expect(passkeyButton()).toBeNull();
      expect(separators()).toBe(1);
      expect(passkey.getInfo).not.toHaveBeenCalled();
    });

    it('lets the browser offer passkeys in the username field when enabled', () => {
      setup({enabled: true});
      expect(usernameInput().getAttribute('autocomplete')).toBe('username webauthn');
    });
  });

  describe('autofill', () => {
    it('starts a conditional request when the browser supports it', async () => {
      setup({enabled: true, conditional: true});
      await fixture.whenStable();
      expect(passkey.signIn).toHaveBeenCalledWith(true);
    });

    it('does not start one when the browser does not support it', async () => {
      setup({enabled: true, conditional: false});
      await fixture.whenStable();
      expect(passkey.signIn).not.toHaveBeenCalled();
    });

    it('does not start one when passkeys are off', async () => {
      setup({enabled: false, conditional: true});
      await fixture.whenStable();
      expect(passkey.signIn).not.toHaveBeenCalled();
    });

    it('signs in when the user picks a passkey from the autofill list', async () => {
      setup({enabled: true, conditional: true});
      passkey.signIn.mockResolvedValue({username: 'alice', authentication: 'passkey', roles: ['developer']});
      // the pending conditional request was already created with the never-resolving promise; start again
      await (fixture.componentInstance as any).startPasskeyAutofill();
      expect(auth.storeUserInfo).toHaveBeenCalled();
      expect(router.navigateByUrl).toHaveBeenCalledWith('/');
    });
  });

  describe('passkey button', () => {
    it('signs in, stores the user, and goes to the next page', async () => {
      setup({enabled: true});
      passkey.signIn.mockResolvedValue({username: 'alice', authentication: 'passkey', roles: ['developer']});

      passkeyButton().click();
      await fixture.whenStable();

      expect(passkey.signIn).toHaveBeenCalledWith(false);
      expect(auth.storeUserInfo).toHaveBeenCalledWith(expect.objectContaining({username: 'alice'}));
      expect(router.navigateByUrl).toHaveBeenCalledWith('/');
      expect(snackBar.open).toHaveBeenCalledWith('Signed in as \'alice\' (developer)', '', {duration: 3000});
    });

    it('stays quiet when the user dismisses the browser prompt', async () => {
      setup({enabled: true});
      passkey.signIn.mockRejectedValue(new PasskeyCancelledError());

      passkeyButton().click();
      await fixture.whenStable();
      fixture.detectChanges();

      expect(el().querySelector('.alert').hasAttribute('hidden')).toBe(true);
      expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('shows an error and keeps the password form when the passkey is rejected', async () => {
      setup({enabled: true});
      passkey.signIn.mockRejectedValue({status: 401});

      passkeyButton().click();
      await fixture.whenStable();
      fixture.detectChanges();

      const alert = el().querySelector('.alert') as HTMLElement;
      expect(alert.hasAttribute('hidden')).toBe(false);
      expect(alert.textContent).toContain('This passkey could not be verified');
      expect(router.navigateByUrl).not.toHaveBeenCalled();
    });
  });

  describe('username step', () => {
    const passwordInput = () => el().querySelector('#password') as HTMLInputElement | null;
    const continueButton = () => el().querySelector('.passkey-continue-button') as HTMLButtonElement | null;
    const tick = async () => {
      await fixture.whenStable();
      fixture.detectChanges();
    };

    async function typeUsername(name: string) {
      fixture.componentInstance.onUsernameChange(name);
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();
    }

    it('asks for the username only, with a Continue button', () => {
      setup({enabled: true});
      expect(passwordInput()).toBeNull();
      expect(continueButton()).not.toBeNull();
      expect(passkeyButton()).not.toBeNull();
    });

    it('is the plain two-field form when passkeys are off', () => {
      setup({enabled: false});
      expect(passwordInput()).not.toBeNull();
      expect(continueButton()).toBeNull();
    });

    it('goes straight to the passkey prompt when the account has a passkey', async () => {
      setup({enabled: true, hasPasskey: true});
      passkey.signIn.mockResolvedValue({username: 'alice', authentication: 'passkey', roles: ['end-user']});
      await typeUsername('alice');

      fixture.componentInstance.onSubmit();
      await tick();

      expect(passkey.hasPasskey).toHaveBeenCalledWith('alice');
      expect(passkey.signIn).toHaveBeenCalledWith(false);
      expect(router.navigateByUrl).toHaveBeenCalledWith('/');
      expect(offer.offerAfterPasswordSignIn).not.toHaveBeenCalled();
    });

    it('shows the password field when the account has no passkey', async () => {
      setup({enabled: true, hasPasskey: false});
      await typeUsername('bob');

      fixture.componentInstance.onSubmit();
      await tick();

      expect(passkey.signIn).not.toHaveBeenCalledWith(false);
      expect(passwordInput()).not.toBeNull();
    });

    it('does nothing without a username', async () => {
      setup({enabled: true, hasPasskey: true});
      fixture.componentInstance.onSubmit();
      expect(passkey.hasPasskey).not.toHaveBeenCalled();
      expect(continueButton().disabled).toBe(true);
    });

    it('offers "Try another way" when the passkey prompt is dismissed, and that opens the password', async () => {
      setup({enabled: true, hasPasskey: true});
      passkey.signIn.mockRejectedValue(new PasskeyCancelledError());
      await typeUsername('alice');
      fixture.componentInstance.onSubmit();
      await tick();

      expect(passwordInput()).toBeNull();
      expect(el().querySelector('.alert').hasAttribute('hidden')).toBe(true);
      const another = el().querySelector('.passkey-another-way-button') as HTMLButtonElement;
      expect(another).not.toBeNull();
      expect(el().querySelector('.passkey-retry-button')).not.toBeNull();

      another.click();
      await tick();
      expect(passwordInput()).not.toBeNull();
    });

    it('tries the passkey again from the retry button', async () => {
      setup({enabled: true, hasPasskey: true});
      passkey.signIn.mockRejectedValue(new PasskeyCancelledError());
      await typeUsername('alice');
      fixture.componentInstance.onSubmit();
      await tick();
      passkey.signIn.mockClear();

      (el().querySelector('.passkey-retry-button') as HTMLButtonElement).click();
      await tick();

      expect(passkey.signIn).toHaveBeenCalledWith(false);
    });

    it('starts over when the username is changed', async () => {
      setup({enabled: true, hasPasskey: false});
      await typeUsername('bob');
      fixture.componentInstance.onSubmit();
      await tick();
      expect(passwordInput()).not.toBeNull();

      fixture.componentInstance.onUsernameChange('carol');
      await tick();
      expect(passwordInput()).toBeNull();
      expect(continueButton()).not.toBeNull();
    });

    it('still signs in with the password after the other way was chosen', async () => {
      setup({enabled: true, hasPasskey: false});
      await typeUsername('bob');
      fixture.componentInstance.onSubmit();
      await tick();
      auth.authenticate.mockImplementation((_c, ok) => ok({username: 'bob', roles: ['end-user']}));

      fixture.componentInstance.onSubmit();
      await tick();

      expect(auth.authenticate).toHaveBeenCalled();
      expect(offer.offerAfterPasswordSignIn).toHaveBeenCalledWith('bob');
    });
  });

  describe('offering a passkey after a password sign-in', () => {
    function signInWithPassword() {
      auth.authenticate.mockImplementation((_credentials, ok) => ok({username: 'alice', roles: ['end-user']}));
      fixture.componentInstance.login();
    }

    it('asks for the offer once the user has landed on the next page', async () => {
      setup({enabled: true});
      signInWithPassword();
      expect(offer.offerAfterPasswordSignIn).not.toHaveBeenCalled();

      await fixture.whenStable();

      expect(router.navigateByUrl).toHaveBeenCalledWith('/');
      expect(offer.offerAfterPasswordSignIn).toHaveBeenCalledWith('alice');
    });

    it('leaves the decision to the offer service, so it is asked even when passkeys are off', async () => {
      setup({enabled: false});
      signInWithPassword();
      await fixture.whenStable();
      expect(offer.offerAfterPasswordSignIn).toHaveBeenCalledWith('alice');
    });

    it('does not offer after signing in with a passkey', async () => {
      setup({enabled: true});
      passkey.signIn.mockResolvedValue({username: 'alice', authentication: 'passkey', roles: ['developer']});

      passkeyButton().click();
      await fixture.whenStable();

      expect(router.navigateByUrl).toHaveBeenCalled();
      expect(offer.offerAfterPasswordSignIn).not.toHaveBeenCalled();
    });

    it('does not offer when sign-in fails', async () => {
      setup({enabled: true});
      auth.authenticate.mockImplementation((_credentials, _ok, fail) => fail({status: 401}));
      fixture.componentInstance.login();
      await fixture.whenStable();
      expect(offer.offerAfterPasswordSignIn).not.toHaveBeenCalled();
    });
  });

  it('cancels a pending browser prompt when the page goes away', () => {
    setup({enabled: true});
    fixture.destroy();
    expect(passkey.cancelPending).toHaveBeenCalled();
  });
});
