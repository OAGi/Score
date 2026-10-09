import { Component, inject, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OAuth2AppInfo, UserToken} from '../../authentication/domain/auth';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyCancelledError} from '../../authentication/passkey/passkey';
import {PasskeyOfferService} from '../passkey-offer/passkey-offer.service';
import {Observable} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {WebPageInfo} from '../about/domain/about';
import {WebPageInfoService} from '../basis.service';
import {projectVersion} from '../../../environments/version';

@Component({
  standalone: false,
  selector: 'score-login',
  templateUrl: './login.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnDestroy {
  auth = inject(AuthService);
  private webPageInfoService = inject(WebPageInfoService);
  private snackBar = inject(MatSnackBar);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private passkey = inject(PasskeyService);
  private passkeyOffer = inject(PasskeyOfferService);
  private changeDetectorRef = inject(ChangeDetectorRef);


  version = projectVersion;

  credentials = {username: '', password: ''};
  next = '';
  err = undefined;

  /** Passkey sign-in is on only when an administrator saved the relying party settings, and the browser can do it. */
  passkeyEnabled = false;
  passkeyError = '';

  /**
   * With passkeys on, signing in starts with the username only: 'username' asks for it, 'passkey' waits for the
   * browser's passkey prompt, 'choose' follows a dismissed or failed prompt (try it again, or another way), and
   * 'password' asks for the password. Without passkeys the page is the plain username-and-password form.
   */
  step: 'username' | 'passkey' | 'choose' | 'password' = 'username';
  checking = false;

  oauth2AppInfos: Observable<OAuth2AppInfo[]>;
  webPageInfo: Observable<WebPageInfo>;

  constructor() {
    this.route.queryParams.subscribe(params => {
      this.next = this.auth.nextParam(params.next);
      if (!this.next) {
        this.next = '/';
      }
      if (this.auth.isAuthenticated()) {
        this.router.navigateByUrl(this.next);
      }
    });

    this.oauth2AppInfos = this.auth.getOAuth2AppInfos();
    this.webPageInfo = this.webPageInfoService.load();

    if (this.passkey.supported) {
      this.passkey.getInfo().subscribe(info => {
        this.passkeyEnabled = !!info?.enabled;
        this.changeDetectorRef.markForCheck();
        if (this.passkeyEnabled) {
          this.startPasskeyAutofill();
        }
      }, () => {
        this.passkeyEnabled = false;
      });
    }
  }

  ngOnDestroy() {
    this.passkey.cancelPending();
  }

  /** Whether the passkey button and the autofill hint are shown. */
  get showPasskey(): boolean {
    return this.passkeyEnabled && this.passkey.supported;
  }

  /** A different username starts the username step over. */
  onUsernameChange(username: string) {
    this.credentials.username = username;
    if (this.showPasskey && this.step !== 'username' && this.step !== 'passkey') {
      this.step = 'username';
    }
    this.changeDetectorRef.markForCheck();
  }

  /** Whether the password field and the Sign in button are shown. */
  get showPasswordFields(): boolean {
    return !this.showPasskey || this.step === 'password';
  }

  onSubmit() {
    if (this.showPasswordFields) {
      return this.login();
    }
    this.continueWithUsername();
    return false;
  }

  /** The username step: a user with a passkey is taken straight to the passkey prompt, anyone else to the password. */
  continueWithUsername() {
    const username = (this.credentials.username || '').trim();
    if (!username || this.checking) {
      return;
    }
    this.passkeyError = '';
    this.checking = true;
    this.passkey.hasPasskey(username).subscribe(hasPasskey => {
      this.checking = false;
      if (hasPasskey) {
        this.signInWithPasskeyStep();
      } else {
        this.useAnotherWay();
      }
      this.changeDetectorRef.markForCheck();
    });
  }

  async signInWithPasskeyStep() {
    this.step = 'passkey';
    this.passkeyError = '';
    this.changeDetectorRef.markForCheck();
    try {
      const resp = await this.passkey.signIn(false);
      this.onSignedIn(resp, 'passkey');
    } catch (e) {
      this.step = 'choose';
      this.onPasskeyFailure(e);
      this.changeDetectorRef.markForCheck();
    }
  }

  /** "Try another way": the password. */
  useAnotherWay() {
    this.passkey.cancelPending();
    this.passkeyError = '';
    this.step = 'password';
    this.changeDetectorRef.markForCheck();
    setTimeout(() => document.getElementById('password')?.focus());
  }

  /**
   * Starts a silent request so that the browser lists the user's passkeys in the username field's autofill. It
   * stays pending until the user picks one, or is cancelled by the passkey button.
   */
  private async startPasskeyAutofill() {
    if (!(await this.passkey.isConditionalMediationAvailable())) {
      return;
    }
    try {
      const resp = await this.passkey.signIn(true);
      this.onSignedIn(resp, 'passkey');
    } catch (e) {
      this.onPasskeyFailure(e);
    }
  }

  async loginWithPasskey() {
    this.passkeyError = '';
    try {
      const resp = await this.passkey.signIn(false);
      this.onSignedIn(resp, 'passkey');
    } catch (e) {
      this.onPasskeyFailure(e);
    }
  }

  private onPasskeyFailure(e: any) {
    if (e instanceof PasskeyCancelledError) {
      return;
    }
    this.passkeyError = (e?.status === 401 || e?.status === 403)
      ? 'This passkey could not be verified. Try again or sign in with your password.'
      : 'Passkey sign-in failed. Try again or sign in with your password.';
    this.changeDetectorRef.markForCheck();
  }

  private onSignedIn(resp: UserToken, method: 'password' | 'passkey') {
    this.passkey.cancelPending();
    this.auth.storeUserInfo(resp);
    const roles = resp.roles || [];
    const role = roles.includes('developer') ? 'developer' : 'end-user';
    this.snackBar.open('Signed in as \'' + resp.username + '\' (' + role + ')', '', {
      duration: 3000,
    });
    this.router.navigateByUrl(this.next).then(() => {
      // After a password sign-in, offer a passkey to a user who has none (see PasskeyOfferService).
      if (method === 'password') {
        this.passkeyOffer.offerAfterPasswordSignIn(resp.username);
      }
    });
  }

  login() {
    this.passkeyError = '';
    this.auth.authenticate(this.credentials, resp => {
      this.onSignedIn(resp, 'password');
    }, err => {
      this.err = err;
    });

    return false;
  }

  get errorMessage(): string {
    if (this.passkeyError) {
      return this.passkeyError;
    }
    if (!this.err) {
      return undefined;
    }

    switch (this.err.status) {
      case 401:
        return 'Invalid username or password';

      case 403:
        return 'Account is disabled';
    }

    return 'Error';
  }

  onClose() {
    this.err = undefined;
    this.passkeyError = '';
  }

  get roles(): string[] {
    return this.auth.getUserToken().roles;
  }
}
