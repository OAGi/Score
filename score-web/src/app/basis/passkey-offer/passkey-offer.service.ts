import {Injectable, inject} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {firstValueFrom} from 'rxjs';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyOfferAnswer, PasskeyOfferDialogComponent} from './passkey-offer-dialog.component';

/** How long "Not now" keeps the offer away. */
export const PASSKEY_OFFER_COOLDOWN_DAYS = 30;

const STORAGE_KEY_PREFIX = 'X-Score-PasskeyOffer:';
const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * Offers a passkey to a user who has just signed in with a password and has none yet.
 * <p>
 * The offer is shown at most once per sign-in, and only when it can work: passkey sign-in is on, the browser supports
 * it, this device has a screen lock or biometrics, and the account has no passkey. "Not now" keeps it away for
 * {@link PASSKEY_OFFER_COOLDOWN_DAYS} days and "Don't ask again" for good. Both are remembered per account in this
 * browser. Anyone can always add a passkey from Settings.
 */
@Injectable({providedIn: 'root'})
export class PasskeyOfferService {
  private passkeyService = inject(PasskeyService);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  /** Offers a passkey if the user is eligible. Never throws: the offer must not get in the way of signing in. */
  async offerAfterPasswordSignIn(username: string): Promise<void> {
    try {
      if (!(await this.isEligible(username))) {
        return;
      }

      const answer = await firstValueFrom(this.dialog.open<PasskeyOfferDialogComponent, void, PasskeyOfferAnswer>(
        PasskeyOfferDialogComponent, {width: '480px', autoFocus: 'first-tabbable', panelClass: 'passkey-offer'}
      ).afterClosed());

      this.remember(username, answer ?? 'later');
      if (answer === 'manage') {
        this.router.navigateByUrl('/settings/account/passkeys');
      }
    } catch (ignore) {
      // A failed offer is not worth interrupting the user for.
    }
  }

  /** Whether to offer a passkey to this user now. */
  async isEligible(username: string): Promise<boolean> {
    if (!this.passkeyService.supported || this.isSnoozed(username)) {
      return false;
    }
    if (!(await this.passkeyService.isPlatformAuthenticatorAvailable())) {
      return false;
    }

    try {
      const info = await firstValueFrom(this.passkeyService.getInfo());
      if (!info?.enabled) {
        return false;
      }
      const passkeys = await firstValueFrom(this.passkeyService.list(true));
      return passkeys.length === 0;
    } catch (ignore) {
      return false;
    }
  }

  private isSnoozed(username: string, now = Date.now()): boolean {
    try {
      const stored = localStorage.getItem(STORAGE_KEY_PREFIX + username);
      if (!stored) {
        return false;
      }
      const value = JSON.parse(stored);
      return value.never === true || (typeof value.until === 'number' && value.until > now);
    } catch (ignore) {
      return false;
    }
  }

  private remember(username: string, answer: PasskeyOfferAnswer, now = Date.now()) {
    let value: object | undefined;
    if (answer === 'never') {
      value = {never: true};
    } else if (answer === 'later') {
      value = {until: now + PASSKEY_OFFER_COOLDOWN_DAYS * DAY_MS};
    }
    // 'created' and 'manage' need no memory: an account with a passkey is not offered one again.
    if (!value) {
      return;
    }
    try {
      localStorage.setItem(STORAGE_KEY_PREFIX + username, JSON.stringify(value));
    } catch (ignore) {
    }
  }

}
