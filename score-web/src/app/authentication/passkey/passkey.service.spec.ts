import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {vi} from 'vitest';
import {PasskeyService} from './passkey.service';
import {PasskeyCancelledError, PasskeySummary} from './passkey';

const signInOptions = {challenge: 'AQI', rpId: 'localhost', allowCredentials: [], userVerification: 'required'};
const assertion = {
  id: 'abc', type: 'public-key', rawId: new Uint8Array([1, 2]).buffer, authenticatorAttachment: 'platform',
  getClientExtensionResults: () => ({}),
  response: {
    authenticatorData: new Uint8Array([3]).buffer, clientDataJSON: new Uint8Array([4]).buffer,
    signature: new Uint8Array([5]).buffer, userHandle: new Uint8Array([6]).buffer
  }
};

function passkey(overrides: Partial<PasskeySummary> = {}): PasskeySummary {
  return Object.assign(new PasskeySummary(), {
    id: 'id1', label: 'MacBook', rpId: 'localhost', usable: true, userHandle: 'handle', ...overrides
  });
}

describe('PasskeyService', () => {
  let service: PasskeyService;
  let httpTesting: HttpTestingController;
  let credentials: { get: ReturnType<typeof vi.fn>; create: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    credentials = {get: vi.fn(), create: vi.fn()};
    vi.stubGlobal('PublicKeyCredential', function () {
    });
    Object.defineProperty(navigator, 'credentials', {value: credentials, configurable: true});

    // The service logs why the browser did not complete a request; keep the tests' output clean.
    vi.spyOn(console, 'warn').mockImplementation(() => {
    });

    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(PasskeyService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('reports whether passkey sign-in is enabled from the public info endpoint', () => {
    let enabled: boolean | undefined;
    service.getInfo().subscribe(info => enabled = info.enabled);
    httpTesting.expectOne('/api/info/passkey').flush({enabled: true});
    expect(enabled).toBe(true);
  });

  it('is unsupported when the browser has no WebAuthn', () => {
    vi.unstubAllGlobals();
    delete (window as any).PublicKeyCredential;
    expect(service.supported).toBe(false);
  });

  describe('signIn', () => {
    it('fetches options, asks the browser, and posts the assertion', async () => {
      credentials.get.mockResolvedValue(assertion);

      service.signIn(false);
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);
      await vi.waitFor(() => expect(credentials.get).toHaveBeenCalled());

      const call = credentials.get.mock.calls[0][0];
      expect(call.mediation).toBeUndefined();
      expect(call.publicKey.rpId).toBe('localhost');
      expect(new Uint8Array(call.publicKey.challenge)).toEqual(new Uint8Array([1, 2]));

      await vi.waitFor(() => httpTesting.expectOne('/api/login/webauthn'));
    });

    it('posts the encoded assertion and returns the signed-in user', async () => {
      credentials.get.mockResolvedValue(assertion);

      const result = service.signIn(false);
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);
      const login = await vi.waitFor(() => httpTesting.expectOne('/api/login/webauthn'));
      expect(login.request.body.credType).toBe('public-key');
      expect(login.request.body.rawId).toBe('AQI');
      login.flush({username: 'alice', authentication: 'passkey', roles: ['developer']});

      expect((await result).authentication).toBe('passkey');
    });

    it('asks for conditional mediation when used for autofill', async () => {
      credentials.get.mockResolvedValue(assertion);

      service.signIn(true).catch(() => {
      });
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);
      await vi.waitFor(() => expect(credentials.get).toHaveBeenCalled());

      expect(credentials.get.mock.calls[0][0].mediation).toBe('conditional');
      await vi.waitFor(() => httpTesting.expectOne('/api/login/webauthn'));
    });

    it('turns a dismissed prompt into PasskeyCancelledError, not a failure', async () => {
      const dismissed = new Error('dismissed');
      dismissed.name = 'NotAllowedError';
      credentials.get.mockRejectedValue(dismissed);

      const result = service.signIn(false);
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);

      await expect(result).rejects.toBeInstanceOf(PasskeyCancelledError);
    });

    it('turns an aborted prompt into PasskeyCancelledError', async () => {
      const aborted = new Error('aborted');
      aborted.name = 'AbortError';
      credentials.get.mockRejectedValue(aborted);

      const result = service.signIn(true);
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);

      await expect(result).rejects.toBeInstanceOf(PasskeyCancelledError);
    });

    it('lets other browser errors through', async () => {
      const failure = new Error('security');
      failure.name = 'SecurityError';
      credentials.get.mockRejectedValue(failure);

      const result = service.signIn(false);
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);

      await expect(result).rejects.toBe(failure);
    });

    it('cancels the previous prompt when a new one starts', async () => {
      credentials.get.mockImplementation(() => new Promise(() => {
      }));

      service.signIn(true).catch(() => {
      });
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);
      await vi.waitFor(() => expect(credentials.get).toHaveBeenCalledTimes(1));
      const first: AbortSignal = credentials.get.mock.calls[0][0].signal;

      service.signIn(false).catch(() => {
      });
      httpTesting.expectOne('/api/webauthn/authenticate/options').flush(signInOptions);
      await vi.waitFor(() => expect(credentials.get).toHaveBeenCalledTimes(2));

      expect(first.aborted).toBe(true);
    });
  });

  describe('register', () => {
    const creationOptions = {
      challenge: 'AQI', rp: {id: 'localhost', name: 'x'}, user: {id: 'BQY', name: 'alice', displayName: 'alice'},
      excludeCredentials: []
    };
    const attestation = {
      id: 'abc', type: 'public-key', rawId: new Uint8Array([1]).buffer, getClientExtensionResults: () => ({}),
      response: {attestationObject: new Uint8Array([7]).buffer, clientDataJSON: new Uint8Array([8]).buffer}
    };

    it('creates a credential and posts it with the label', async () => {
      credentials.create.mockResolvedValue(attestation);

      const result = service.register('MacBook');
      httpTesting.expectOne('/api/webauthn/register/options').flush(creationOptions);
      const post = await vi.waitFor(() => httpTesting.expectOne('/api/webauthn/register'));
      expect(post.request.body.publicKey.label).toBe('MacBook');
      post.flush({success: true});

      await expect(result).resolves.toBeUndefined();
    });

    it('turns a dismissed prompt into PasskeyCancelledError', async () => {
      const dismissed = new Error('dismissed');
      dismissed.name = 'NotAllowedError';
      credentials.create.mockRejectedValue(dismissed);

      const result = service.register('MacBook');
      httpTesting.expectOne('/api/webauthn/register/options').flush(creationOptions);

      await expect(result).rejects.toBeInstanceOf(PasskeyCancelledError);
    });
  });

  describe('management', () => {
    it('lists, renames and removes passkeys', () => {
      service.list().subscribe();
      httpTesting.expectOne('/api/passkeys').flush([]);

      service.rename('a/b', 'New').subscribe();
      const rename = httpTesting.expectOne('/api/passkeys/a%2Fb');
      expect(rename.request.method).toBe('PATCH');
      expect(rename.request.body).toEqual({label: 'New'});
      rename.flush(null);

      service.remove('id1').subscribe();
      const remove = httpTesting.expectOne('/api/passkeys/id1');
      expect(remove.request.method).toBe('DELETE');
      remove.flush(null);
    });
  });

  describe('Signal API hints', () => {
    it('tells the browser which passkeys are still accepted, skipping unusable ones', async () => {
      const signalAll = vi.fn().mockResolvedValue(undefined);
      (PublicKeyCredential as any).signalAllAcceptedCredentials = signalAll;

      await service.signalAcceptedCredentials([passkey({id: 'a'}), passkey({id: 'b', usable: false})]);

      expect(signalAll).toHaveBeenCalledWith({rpId: 'localhost', userId: 'handle', allAcceptedCredentialIds: ['a']});
    });

    it('does nothing when there is nothing usable to report', async () => {
      const signalAll = vi.fn();
      (PublicKeyCredential as any).signalAllAcceptedCredentials = signalAll;

      await service.signalAcceptedCredentials([passkey({usable: false})]);

      expect(signalAll).not.toHaveBeenCalled();
    });

    it('tells the browser about a removed passkey', async () => {
      const signalUnknown = vi.fn().mockResolvedValue(undefined);
      (PublicKeyCredential as any).signalUnknownCredential = signalUnknown;

      await service.signalUnknownCredential(passkey({id: 'gone'}));

      expect(signalUnknown).toHaveBeenCalledWith({rpId: 'localhost', credentialId: 'gone'});
    });

    it('ignores browsers without the Signal API and swallows its failures', async () => {
      await expect(service.signalUnknownCredential(passkey())).resolves.toBeUndefined();

      (PublicKeyCredential as any).signalAllAcceptedCredentials = vi.fn().mockRejectedValue(new Error('no'));
      await expect(service.signalAcceptedCredentials([passkey()])).resolves.toBeUndefined();
    });
  });

});
