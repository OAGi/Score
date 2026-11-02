import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogRef} from '@angular/material/dialog';
import {Subject} from 'rxjs';
import {vi} from 'vitest';
import {PasskeyOfferDialogComponent} from './passkey-offer-dialog.component';
import {PasskeyService} from '../../authentication/passkey/passkey.service';
import {PasskeyCancelledError} from '../../authentication/passkey/passkey';

describe('PasskeyOfferDialogComponent', () => {
  let fixture: ComponentFixture<PasskeyOfferDialogComponent>;
  let component: PasskeyOfferDialogComponent;
  let dialogRef: { close: ReturnType<typeof vi.fn>; disableClose: boolean; backdropClick: any; keydownEvents: any };
  let backdrop: Subject<void>;
  let keys: Subject<{ key: string }>;
  let passkey: { register: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    backdrop = new Subject<void>();
    keys = new Subject<{ key: string }>();
    dialogRef = {close: vi.fn(), disableClose: false, backdropClick: () => backdrop, keydownEvents: () => keys};
    passkey = {register: vi.fn().mockResolvedValue(undefined)};
    TestBed.configureTestingModule({
      declarations: [PasskeyOfferDialogComponent],
      providers: [
        {provide: MatDialogRef, useValue: dialogRef},
        {provide: PasskeyService, useValue: passkey}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(PasskeyOfferDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const el = (): HTMLElement => fixture.nativeElement;

  /** Lets the asynchronous creation settle, then renders the result. */
  async function settle() {
    await fixture.whenStable();
    fixture.detectChanges();
  }
  const button = (cls: string) => el().querySelector('.' + cls) as HTMLButtonElement | null;

  it('says in one sentence what a passkey is for, and offers three ways out', () => {
    expect(el().textContent).toContain('Sign in faster with a passkey');
    expect(el().querySelector('.content p').textContent.trim())
      .toBe('Use your fingerprint, face, or screen lock instead of typing your password.');
    expect(button('passkey-offer-create').textContent).toContain('Create a passkey');
    expect(button('passkey-offer-later').textContent).toContain('Not now');
    expect(button('passkey-offer-never').textContent).toContain("Don't ask again");
  });

  it('registers the passkey without a name of its own', async () => {
    await component.create();
    expect(passkey.register).toHaveBeenCalledWith('');
  });

  it('takes over closing itself, so the browser prompt is never cut short', () => {
    expect(dialogRef.disableClose).toBe(true);
  });

  describe('creating', () => {
    it('registers the passkey and confirms', async () => {
      await component.create();
      await settle();

      expect(passkey.register).toHaveBeenCalledWith('');
      expect(component.state).toBe('created');
      expect(el().textContent).toContain('Passkey added');
      expect(button('passkey-offer-create')).toBeNull();
    });

    it('is busy while the browser prompt is open', async () => {
      let finish: () => void;
      passkey.register.mockReturnValue(new Promise<void>(resolve => finish = resolve));

      const creating = component.create();
      await settle();
      expect(component.state).toBe('creating');
      expect(button('passkey-offer-create').disabled).toBe(true);
      expect(button('passkey-offer-later').disabled).toBe(true);

      component.create();
      expect(passkey.register).toHaveBeenCalledTimes(1);

      finish();
      await creating;
      await settle();
      expect(component.state).toBe('created');
    });

    it('says nothing was added, and offers a retry, when the user dismisses the browser prompt', async () => {
      passkey.register.mockRejectedValue(new PasskeyCancelledError());

      await component.create();
      await settle();

      expect(component.state).toBe('cancelled');
      expect(el().textContent).toContain('No passkey was added');
      expect(button('passkey-offer-create').textContent).toContain('Try again');
    });

    it('reports a real failure and offers a retry', async () => {
      passkey.register.mockRejectedValue(new Error('boom'));

      await component.create();
      await settle();

      expect(component.state).toBe('failed');
      expect(el().querySelector('[role="alert"]').textContent).toContain('could not be added');
      expect(button('passkey-offer-create').textContent).toContain('Try again');
    });
  });

  describe('answers', () => {
    it('"Not now" and "Don\'t ask again"', () => {
      button('passkey-offer-later').click();
      expect(dialogRef.close).toHaveBeenLastCalledWith('later');
      button('passkey-offer-never').click();
      expect(dialogRef.close).toHaveBeenLastCalledWith('never');
    });

    it('after success: Done, or Manage passkeys', async () => {
      await component.create();
      await settle();

      button('passkey-offer-done').click();
      expect(dialogRef.close).toHaveBeenLastCalledWith('created');
      button('passkey-offer-manage').click();
      expect(dialogRef.close).toHaveBeenLastCalledWith('manage');
    });
  });

  describe('closing with Escape or a click outside', () => {
    it('counts as "Not now" before anything was created', () => {
      keys.next({key: 'Escape'});
      expect(dialogRef.close).toHaveBeenLastCalledWith('later');
      backdrop.next();
      expect(dialogRef.close).toHaveBeenLastCalledWith('later');
    });

    it('ignores other keys', () => {
      keys.next({key: 'a'});
      expect(dialogRef.close).not.toHaveBeenCalled();
    });

    it('does not close while the browser prompt is open', async () => {
      passkey.register.mockReturnValue(new Promise<void>(() => {
      }));
      component.create();

      keys.next({key: 'Escape'});
      backdrop.next();

      expect(dialogRef.close).not.toHaveBeenCalled();
    });

    it('counts as done once the passkey exists, not as "Not now"', async () => {
      await component.create();
      keys.next({key: 'Escape'});
      expect(dialogRef.close).toHaveBeenLastCalledWith('created');
    });
  });
});
