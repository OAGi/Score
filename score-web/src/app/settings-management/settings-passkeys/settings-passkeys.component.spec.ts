import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';
import {SettingsPasskeysComponent} from './settings-passkeys.component';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyCancelledError, PasskeySummary} from '../../authentication/passkey/passkey';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

function passkey(overrides: Partial<PasskeySummary> = {}): PasskeySummary {
  return Object.assign(new PasskeySummary(), {
    id: 'id1', label: 'MacBook', creationTimestamp: '2026-10-08T09:00:00', lastUpdateTimestamp: '2026-10-08T09:00:00',
    backupEligible: true, backedUp: false, transports: ['internal'], rpId: 'localhost', usable: true,
    userHandle: 'handle', ...overrides
  });
}

describe('SettingsPasskeysComponent', () => {
  let fixture: ComponentFixture<SettingsPasskeysComponent>;
  let component: SettingsPasskeysComponent;
  let service: Record<string, any>;
  let snackBar: { open: ReturnType<typeof vi.fn> };
  let dialogResult: boolean;

  function setup(passkeys: PasskeySummary[], supported = true) {
    dialogResult = true;
    snackBar = {open: vi.fn()};
    service = {
      supported,
      list: vi.fn().mockReturnValue(of(passkeys)),
      register: vi.fn().mockResolvedValue(undefined),
      rename: vi.fn().mockReturnValue(of(null)),
      remove: vi.fn().mockReturnValue(of(null)),
      signalAcceptedCredentials: vi.fn(),
      signalUnknownCredential: vi.fn()
    };
    TestBed.configureTestingModule({
      declarations: [SettingsPasskeysComponent],
      imports: [FormsModule],
      providers: [
        {provide: PasskeyService, useValue: service},
        {provide: MatSnackBar, useValue: snackBar},
        {
          provide: ConfirmDialogService, useValue: {
            newConfig: () => ({data: {}}),
            open: () => ({afterClosed: () => of(dialogResult)})
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(SettingsPasskeysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  const el = (): HTMLElement => fixture.nativeElement;

  describe('provider', () => {
    it('heads the row with the provider when it is known', () => {
      setup([passkey({label: 'Brave 154 on macOS', provider: 'Bitwarden'})]);
      const label = el().querySelector('.passkey-label') as HTMLElement;
      expect(label.textContent).toContain('Bitwarden');
      expect(label.querySelector('.passkey-name').textContent).toBe('Brave 154 on macOS');
    });

    it('heads the row with the given name when the provider is unknown', () => {
      setup([passkey({label: 'Brave 154 on macOS', provider: 'Unknown'})]);
      expect(el().querySelector('.passkey-label').textContent.trim()).toBe('Brave 154 on macOS');
      expect(el().querySelector('.passkey-name')).toBeNull();
    });

    it('heads the row with the provider alone when the passkey has no name', () => {
      setup([passkey({label: '', provider: 'Apple Passwords'})]);
      expect(el().querySelector('.passkey-label').textContent.trim()).toBe('Apple Passwords');
      expect(el().querySelector('.passkey-name')).toBeNull();
    });

    it('copes with a passkey whose name is null, also when renaming', () => {
      setup([passkey({label: null, provider: 'Apple Passwords'})]);
      expect(el().querySelector('.passkey-label').textContent.trim()).toBe('Apple Passwords');
      component.startRename(component.passkeys[0]);
      expect(component.editingLabel).toBe('');
    });

    it('heads the row with "Unknown" when there is neither a name nor a provider', () => {
      setup([passkey({label: ''})]);
      expect(el().querySelector('.passkey-label').textContent.trim()).toBe('Unknown');
    });

    it('heads the row with the given name when there is no provider', () => {
      setup([passkey({label: 'Brave 154 on macOS'})]);
      expect(el().querySelector('.passkey-label').textContent.trim()).toBe('Brave 154 on macOS');
      expect(el().querySelector('.passkey-provider-icon')).toBeNull();
    });

    it('shows the provider icon instead of the fingerprint when there is one', () => {
      setup([passkey({provider: 'Bitwarden', providerIcon: 'data:image/svg+xml;base64,PHN2Zz48L3N2Zz4='})]);
      const icon = el().querySelector('.passkey-provider-icon') as HTMLImageElement;
      expect(icon.getAttribute('src')).toBe('data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=');
      expect(icon.getAttribute('alt')).toBe('Bitwarden');
    });

    it('no longer says that a passkey is synced', () => {
      setup([passkey({backedUp: true})]);
      expect(el().textContent).not.toContain('Synced across your devices');
    });

    it('keeps the fingerprint for a provider without an icon', () => {
      setup([passkey({provider: 'Some Vault'})]);
      expect(el().querySelector('.passkey-provider-icon')).toBeNull();
      expect(el().querySelector('.passkey-label').textContent).toContain('Some Vault');
    });
  });

  it('says when it was created', () => {
    setup([passkey({creationTimestamp: '2026-10-08T09:00:00'})]);
    expect(el().querySelector('.passkey-created').textContent).toContain('Created: Oct 8, 2026');
  });

  describe('last used', () => {
    it('says when a passkey has not been used', () => {
      setup([passkey()]);
      expect(el().querySelector('.passkey-last-used').textContent).toContain('Not used yet');
    });

    it('shows when and from which browser and system it was last used', () => {
      setup([passkey({
        lastUsedTimestamp: '2026-10-08T17:40:00',
        lastUsedUserAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) ' +
          'Chrome/154.0.0.0 Safari/537.36'
      })]);
      const text = el().querySelector('.passkey-last-used').textContent;
      expect(text).toContain('Last used:');
      expect(text).toContain('Chrome 154.0.0.0 on macOS');
    });

    it('leaves the browser out when it was not recorded', () => {
      setup([passkey({lastUsedTimestamp: '2026-10-08T17:40:00'})]);
      const text = el().querySelector('.passkey-last-used').textContent;
      expect(text).toContain('Last used:');
      expect(text).not.toContain('·');
    });
  });

  it('lists the passkeys and tells the browser which ones the server accepts', () => {
    const passkeys = [passkey(), passkey({id: 'id2', label: 'YubiKey'})];
    setup(passkeys);

    expect(el().querySelectorAll('.passkey-row').length).toBe(2);
    expect(el().textContent).toContain('YubiKey');
    expect(service.signalAcceptedCredentials).toHaveBeenCalledWith(passkeys);
  });

  it('invites the user to add a first passkey', () => {
    setup([]);
    expect(el().textContent).toContain('You have not added a passkey yet.');
  });

  it('marks a passkey registered for another domain as unusable here', () => {
    setup([passkey({usable: false, rpId: 'old.example.org'})]);
    expect(el().querySelector('.passkey-chip-warn')).not.toBeNull();
  });


  it('says so when the browser has no passkey support', () => {
    setup([], false);
    expect(el().querySelector('.btn-add-passkey')).toBeNull();
    expect(el().textContent).toContain('does not support passkeys');
  });

  describe('add', () => {
    it('registers without a name and reloads the list', async () => {
      setup([]);

      await component.add();

      expect(service.register).toHaveBeenCalledWith('');
      expect(service.list).toHaveBeenCalledTimes(2);
      expect(snackBar.open).toHaveBeenCalledWith('Passkey added', '', {duration: 3000});
    });

    it('has no name field next to the button', () => {
      setup([]);
      expect(el().querySelector('.passkey-add input')).toBeNull();
    });

    it('stays quiet when the user dismisses the browser prompt', async () => {
      setup([]);
      service.register.mockRejectedValue(new PasskeyCancelledError());

      await component.add();

      expect(snackBar.open).not.toHaveBeenCalled();
      expect(component.adding).toBe(false);
    });

    it('reports a real failure', async () => {
      setup([]);
      service.register.mockRejectedValue(new Error('boom'));

      await component.add();

      expect(snackBar.open).toHaveBeenCalledWith('The passkey could not be added. Try again.', '', {duration: 5000});
      expect(component.adding).toBe(false);
    });
  });

  describe('rename', () => {
    it('saves a new name', () => {
      setup([passkey()]);
      component.startRename(component.passkeys[0]);
      component.editingLabel = ' Home Mac ';

      component.saveRename(component.passkeys[0]);

      expect(service.rename).toHaveBeenCalledWith('id1', 'Home Mac');
      expect(component.editingId).toBeUndefined();
    });

    it('does not call the server when the name did not change', () => {
      setup([passkey()]);
      component.startRename(component.passkeys[0]);

      component.saveRename(component.passkeys[0]);

      expect(service.rename).not.toHaveBeenCalled();
      expect(component.editingId).toBeUndefined();
    });

    it('refuses an empty name', () => {
      setup([passkey()]);
      component.startRename(component.passkeys[0]);
      component.editingLabel = '  ';

      component.saveRename(component.passkeys[0]);

      expect(service.rename).not.toHaveBeenCalled();
      expect(component.editingId).toBe('id1');
    });
  });

  describe('remove', () => {
    it('removes after confirmation, tells the browser, and reloads', () => {
      setup([passkey()]);

      component.remove(component.passkeys[0]);

      expect(service.remove).toHaveBeenCalledWith('id1');
      expect(service.signalUnknownCredential).toHaveBeenCalledWith(expect.objectContaining({id: 'id1'}));
      expect(service.list).toHaveBeenCalledTimes(2);
    });

    it('keeps the passkey when the user does not confirm', () => {
      setup([passkey()]);
      dialogResult = false;

      component.remove(component.passkeys[0]);

      expect(service.remove).not.toHaveBeenCalled();
    });

    it('keeps the list as is when the server refuses (for example the only sign-in method)', () => {
      setup([passkey()]);
      service.remove.mockReturnValue(throwError(() => ({status: 400})));

      component.remove(component.passkeys[0]);

      expect(service.signalUnknownCredential).not.toHaveBeenCalled();
      expect(service.list).toHaveBeenCalledTimes(1);
    });
  });
});
