import {ChangeDetectionStrategy, Component, OnInit, inject} from '@angular/core';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {MatSnackBar} from '@angular/material/snack-bar';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyCancelledError, PasskeySummary} from '../../authentication/passkey/passkey';
import {describeUserAgent} from '../../authentication/passkey/passkey-label';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

/**
 * The signed-in user's own passkeys: list, add, rename, remove.
 * <p>
 * Removing a passkey here only removes it from the server. The passkey on the user's device is not touched, but it
 * can no longer be used to sign in.
 */
@Component({
  standalone: false,
  selector: 'score-settings-passkeys',
  templateUrl: './settings-passkeys.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./settings-passkeys.component.css']
})
export class SettingsPasskeysComponent implements OnInit {
  private passkeyService = inject(PasskeyService);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private sanitizer = inject(DomSanitizer);

  passkeys: PasskeySummary[] = [];
  /** Provider icons by passkey ID, trusted once because the server only sends inline SVGs from its own list. */
  private providerIcons = new Map<string, SafeUrl>();
  loading = false;
  adding = false;

  editingId: string | undefined;
  editingLabel = '';

  get supported(): boolean {
    return this.passkeyService.supported;
  }

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.loading = true;
    this.passkeyService.list().subscribe(passkeys => {
      this.passkeys = passkeys;
      this.providerIcons = new Map(passkeys.filter(p => p.providerIcon)
        .map(p => [p.id, this.sanitizer.bypassSecurityTrustUrl(p.providerIcon)] as [string, SafeUrl]));
      this.loading = false;
      this.passkeyService.signalAcceptedCredentials(passkeys);
    }, () => {
      this.loading = false;
    });
  }

  async add() {
    if (this.adding) {
      return;
    }

    this.adding = true;
    try {
      // No name is given: the list shows where the passkey is kept, and it can be renamed afterwards.
      await this.passkeyService.register('');
      this.snackBar.open('Passkey added', '', {duration: 3000});
        this.reload();
    } catch (e) {
      if (!(e instanceof PasskeyCancelledError)) {
        this.snackBar.open('The passkey could not be added. Try again.', '', {duration: 5000});
      }
    } finally {
      this.adding = false;
    }
  }

  /**
   * What a row is headed with: where the passkey is kept (e.g. "Bitwarden") when that is known, else the name it was
   * given, else "Unknown". When the heading is the provider, the given name (if any) follows it.
   */
  title(passkey: PasskeySummary): string {
    return this.hasProviderTitle(passkey) ? passkey.provider : (passkey.label || 'Unknown');
  }

  /** Whether the heading is the provider, so the given name follows it. */
  hasProviderTitle(passkey: PasskeySummary): boolean {
    return !!passkey.provider && passkey.provider !== 'Unknown';
  }

  /** The icon of the passkey manager holding the passkey, or undefined to show the generic fingerprint. */
  providerIcon(passkey: PasskeySummary): SafeUrl | undefined {
    return this.providerIcons.get(passkey.id);
  }

  /** The browser and system a passkey was last used from, or empty. */
  lastUsedFrom(passkey: PasskeySummary): string {
    return describeUserAgent(passkey.lastUsedUserAgent);
  }

  startRename(passkey: PasskeySummary) {
    this.editingId = passkey.id;
    this.editingLabel = passkey.label ?? '';
  }

  cancelRename() {
    this.editingId = undefined;
  }

  saveRename(passkey: PasskeySummary) {
    const label = this.editingLabel.trim();
    if (!label) {
      return;
    }
    if (label === passkey.label) {
      this.cancelRename();
      return;
    }
    this.passkeyService.rename(passkey.id, label).subscribe({
      next: () => {
        this.cancelRename();
        this.snackBar.open('Updated', '', {duration: 3000});
        this.reload();
      },
      // The error interceptor already told the user why; keep the edit open so they can fix the name.
      error: () => {
      }
    });
  }

  remove(passkey: PasskeySummary) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove passkey?';
    dialogConfig.data.content = ['Are you sure you want to remove \'' + passkey.label + '\'?',
      'It can no longer be used to sign in. The passkey stays on your device until you delete it there.'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
      if (result) {
        this.passkeyService.remove(passkey.id).subscribe({
          next: () => {
            this.snackBar.open('Passkey removed', '', {duration: 3000});
            this.passkeyService.signalUnknownCredential(passkey);
            this.reload();
          },
          // For example "this is your only sign-in method": the error interceptor already told the user.
          error: () => {
          }
        });
      }
    });
  }

}
