import {Injectable, inject} from '@angular/core';
import {HttpClient, HttpContext} from '@angular/common/http';
import {catchError, firstValueFrom, map, Observable, of} from 'rxjs';
import {PasskeyCancelledError, PasskeyInfo, PasskeySummary} from './passkey';
import {
  decodeCreationOptions,
  decodeRequestOptions,
  encodeAssertion,
  encodeAttestation
} from './webauthn-codec';
import {SUPPRESS_ERROR_ALERT} from '../auth.service';
import {UserToken} from '../domain/auth';

/**
 * Passkey (WebAuthn) sign-in and management.
 * <p>
 * Passkey sign-in is switched on by an administrator (Settings &gt; Application settings) by saving the relying
 * party settings; {@link getInfo} reports whether it is on. Only one browser ceremony can be pending at a time, so
 * starting a new one cancels the previous one.
 */
@Injectable({providedIn: 'root'})
export class PasskeyService {
  private http = inject(HttpClient);

  private pending?: AbortController;

  /** Whether this browser can do WebAuthn at all. */
  get supported(): boolean {
    return typeof window !== 'undefined' && !!window.PublicKeyCredential
      && !!navigator.credentials;
  }

  /** Whether the browser can offer passkeys in the username field's autofill. */
  async isConditionalMediationAvailable(): Promise<boolean> {
    try {
      return this.supported
        && !!PublicKeyCredential.isConditionalMediationAvailable
        && await PublicKeyCredential.isConditionalMediationAvailable();
    } catch (ignore) {
      return false;
    }
  }

  /**
   * Whether this device has a built-in authenticator with a screen lock or biometrics (Touch ID, Windows Hello, an
   * Android or iOS lock screen). Passkeys here require user verification, so this is what makes creating one easy.
   */
  async isPlatformAuthenticatorAvailable(): Promise<boolean> {
    try {
      return this.supported
        && !!PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable
        && await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
    } catch (ignore) {
      return false;
    }
  }

  /** Whether passkey sign-in is enabled on this instance. Public: used by the login page. */
  getInfo(): Observable<PasskeyInfo> {
    return this.http.get<PasskeyInfo>('/api/info/passkey', {context: this.suppressErrorAlert()});
  }

  /**
   * Whether this account has a passkey that can be used here. Public: used by the login page's username step. A
   * failed request counts as "no", so the user falls back to the password.
   */
  hasPasskey(username: string): Observable<boolean> {
    return this.http.post<{ passkey: boolean }>('/api/info/passkey/lookup', {username},
      {context: this.suppressErrorAlert()}).pipe(
      map(response => !!response?.passkey),
      catchError(() => of(false)));
  }

  /** Cancels the pending browser prompt, if any. */
  cancelPending(): void {
    if (this.pending) {
      this.pending.abort();
      this.pending = undefined;
    }
  }

  /**
   * Signs in with a passkey. With {@code conditional} the browser offers passkeys in the username field's
   * autofill and the promise stays pending until the user picks one.
   *
   * @returns the same body the password login returns (username, authentication, roles)
   * @throws PasskeyCancelledError when the user dismissed the prompt or a newer request replaced this one
   */
  async signIn(conditional: boolean): Promise<UserToken> {
    const options = await firstValueFrom(this.http.post<any>('/api/webauthn/authenticate/options', null,
      {context: this.suppressErrorAlert()}));

    this.cancelPending();
    const controller = new AbortController();
    this.pending = controller;

    let credential: Credential | null;
    try {
      credential = await navigator.credentials.get({
        publicKey: decodeRequestOptions(options),
        signal: controller.signal,
        ...(conditional ? {mediation: 'conditional' as CredentialMediationRequirement} : {})
      });
    } catch (e) {
      throw this.asCancelledIfDismissed(e);
    } finally {
      if (this.pending === controller) {
        this.pending = undefined;
      }
    }
    if (!credential) {
      throw new PasskeyCancelledError();
    }

    return firstValueFrom(this.http.post<UserToken>('/api/login/webauthn',
      encodeAssertion(credential as PublicKeyCredential), {context: this.suppressErrorAlert()}));
  }

  /** Registers a new passkey for the signed-in account. */
  async register(label: string): Promise<void> {
    const options = await firstValueFrom(this.http.post<any>('/api/webauthn/register/options', null,
      {context: this.suppressErrorAlert()}));

    this.cancelPending();
    const controller = new AbortController();
    this.pending = controller;

    let credential: Credential | null;
    try {
      credential = await navigator.credentials.create({
        publicKey: decodeCreationOptions(options),
        signal: controller.signal
      });
    } catch (e) {
      throw this.asCancelledIfDismissed(e);
    } finally {
      if (this.pending === controller) {
        this.pending = undefined;
      }
    }
    if (!credential) {
      throw new PasskeyCancelledError();
    }

    await firstValueFrom(this.http.post('/api/webauthn/register',
      encodeAttestation(credential as PublicKeyCredential, label), {context: this.suppressErrorAlert()}));
  }

  list(suppressErrorAlert = false): Observable<PasskeySummary[]> {
    return this.http.get<PasskeySummary[]>('/api/passkeys',
      suppressErrorAlert ? {context: this.suppressErrorAlert()} : {});
  }

  rename(id: string, label: string): Observable<any> {
    return this.http.patch('/api/passkeys/' + encodeURIComponent(id), {label});
  }

  remove(id: string): Observable<any> {
    return this.http.delete('/api/passkeys/' + encodeURIComponent(id));
  }

  /**
   * Tells the browser or password manager which passkeys the server still accepts, so it can drop the others
   * (WebAuthn Signal API). A hint only: support varies, so failures are ignored.
   */
  async signalAcceptedCredentials(passkeys: PasskeySummary[]): Promise<void> {
    const usable = passkeys.filter(e => e.usable);
    if (usable.length === 0) {
      return;
    }
    try {
      const api: any = PublicKeyCredential;
      if (typeof api.signalAllAcceptedCredentials === 'function') {
        await api.signalAllAcceptedCredentials({
          rpId: usable[0].rpId,
          userId: usable[0].userHandle,
          allAcceptedCredentialIds: usable.map(e => e.id)
        });
      }
    } catch (ignore) {
      // A hint only.
    }
  }

  /** Tells the browser that a passkey no longer exists on the server (WebAuthn Signal API). */
  async signalUnknownCredential(passkey: PasskeySummary): Promise<void> {
    try {
      const api: any = PublicKeyCredential;
      if (typeof api.signalUnknownCredential === 'function') {
        await api.signalUnknownCredential({rpId: passkey.rpId, credentialId: passkey.id});
      }
    } catch (ignore) {
      // A hint only.
    }
  }

  private asCancelledIfDismissed(error: any): Error {
    // The browser reports "dismissed", "refused by the authenticator or password manager", and "timed out" all as
    // NotAllowedError, so keep the reason where a developer can find it.
    console.warn('The browser did not complete the passkey request:', error?.name, error?.message);
    if (error && (error.name === 'NotAllowedError' || error.name === 'AbortError')) {
      return new PasskeyCancelledError();
    }
    return error;
  }

  private suppressErrorAlert(): HttpContext {
    return new HttpContext().set(SUPPRESS_ERROR_ALERT, true);
  }

}
