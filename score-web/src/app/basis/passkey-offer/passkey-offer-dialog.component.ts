import {ChangeDetectionStrategy, ChangeDetectorRef, Component, inject} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyCancelledError} from '../../authentication/passkey/passkey';

/** How the user answered the offer. */
export type PasskeyOfferAnswer = 'created' | 'manage' | 'later' | 'never';

/**
 * Offers to create a passkey right after a password sign-in: sign in faster with a fingerprint, face, or screen lock.
 * <p>
 * The passkey is created from this dialog (the browser asks the user to confirm), then the user is told it worked.
 * Closing the dialog any other way counts as "not now".
 */
@Component({
  standalone: false,
  selector: 'score-passkey-offer-dialog',
  templateUrl: './passkey-offer-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./passkey-offer-dialog.component.css']
})
export class PasskeyOfferDialogComponent {
  private dialogRef = inject<MatDialogRef<PasskeyOfferDialogComponent, PasskeyOfferAnswer>>(MatDialogRef);
  private passkeyService = inject(PasskeyService);
  private changeDetectorRef = inject(ChangeDetectorRef);

  state: 'idle' | 'creating' | 'created' | 'cancelled' | 'failed' = 'idle';

  constructor() {
    // The dialog decides for itself when it may close: not while the browser's prompt is open.
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => this.dismiss());
    this.dialogRef.keydownEvents().subscribe(event => {
      if (event.key === 'Escape') {
        this.dismiss();
      }
    });
  }

  async create() {
    if (this.state === 'creating') {
      return;
    }
    this.setState('creating');
    try {
      // No name is given: the list shows where the passkey is kept, and it can be renamed afterwards.
      await this.passkeyService.register('');
      this.setState('created');
    } catch (e) {
      this.setState((e instanceof PasskeyCancelledError) ? 'cancelled' : 'failed');
    }
  }

  /** The state changes after the browser's prompt is answered, outside any event of this view, so say so. */
  private setState(state: PasskeyOfferDialogComponent['state']) {
    this.state = state;
    this.changeDetectorRef.markForCheck();
  }

  later() {
    this.dialogRef.close('later');
  }

  never() {
    this.dialogRef.close('never');
  }

  done() {
    this.dialogRef.close('created');
  }

  manage() {
    this.dialogRef.close('manage');
  }

  /** Backdrop click or Escape. */
  private dismiss() {
    if (this.state === 'creating') {
      return;
    }
    if (this.state === 'created') {
      this.done();
    } else {
      this.later();
    }
  }

}
