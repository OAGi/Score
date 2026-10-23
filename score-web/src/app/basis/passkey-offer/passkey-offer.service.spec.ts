import {TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';
import {PASSKEY_OFFER_COOLDOWN_DAYS, PasskeyOfferService} from './passkey-offer.service';
import {PasskeyService} from '../../authentication/passkey/passkey.service';

const DAY_MS = 24 * 60 * 60 * 1000;

describe('PasskeyOfferService', () => {
  let service: PasskeyOfferService;
  let passkey: Record<string, any>;
  let dialog: { open: ReturnType<typeof vi.fn> };
  let router: { navigateByUrl: ReturnType<typeof vi.fn> };

  function setup(options: {
    supported?: boolean; platform?: boolean; enabled?: boolean; passkeys?: any[]; answer?: string;
  } = {}) {
    localStorage.clear();
    passkey = {
      supported: options.supported ?? true,
      isPlatformAuthenticatorAvailable: vi.fn().mockResolvedValue(options.platform ?? true),
      getInfo: vi.fn().mockReturnValue(of({enabled: options.enabled ?? true})),
      list: vi.fn().mockReturnValue(of(options.passkeys ?? []))
    };
    dialog = {open: vi.fn().mockReturnValue({afterClosed: () => of(options.answer)})};
    router = {navigateByUrl: vi.fn()};
    TestBed.configureTestingModule({
      providers: [
        {provide: PasskeyService, useValue: passkey},
        {provide: MatDialog, useValue: dialog},
        {provide: Router, useValue: router}
      ]
    });
    service = TestBed.inject(PasskeyOfferService);
  }

  const stored = (username = 'alice') => localStorage.getItem('X-Score-PasskeyOffer:' + username);

  describe('who is offered a passkey', () => {
    it('a user with none, on a capable device, when passkeys are on', async () => {
      setup();
      expect(await service.isEligible('alice')).toBe(true);
    });

    it('nobody when passkey sign-in is off', async () => {
      setup({enabled: false});
      expect(await service.isEligible('alice')).toBe(false);
      expect(passkey.list).not.toHaveBeenCalled();
    });

    it('nobody on a browser without passkey support', async () => {
      setup({supported: false});
      expect(await service.isEligible('alice')).toBe(false);
      expect(passkey.getInfo).not.toHaveBeenCalled();
    });

    it('nobody on a device with no screen lock or biometrics, since passkeys here need user verification', async () => {
      setup({platform: false});
      expect(await service.isEligible('alice')).toBe(false);
    });

    it('nobody who already has a passkey', async () => {
      setup({passkeys: [{id: 'a'}]});
      expect(await service.isEligible('alice')).toBe(false);
    });

    it('nobody when the server cannot be asked', async () => {
      setup();
      passkey.list.mockReturnValue(throwError(() => ({status: 500})));
      expect(await service.isEligible('alice')).toBe(false);
    });
  });

  describe('what happens after the answer', () => {
    it('shows the dialog to an eligible user', async () => {
      setup({answer: 'created'});
      await service.offerAfterPasswordSignIn('alice');
      expect(dialog.open).toHaveBeenCalledTimes(1);
    });

    it('shows nothing to an ineligible user', async () => {
      setup({passkeys: [{id: 'a'}]});
      await service.offerAfterPasswordSignIn('alice');
      expect(dialog.open).not.toHaveBeenCalled();
    });

    it('"Not now" keeps the offer away for the cooldown, then it returns', async () => {
      setup({answer: 'later'});
      const before = Date.now();
      await service.offerAfterPasswordSignIn('alice');

      const until = JSON.parse(stored()).until;
      expect(until).toBeGreaterThanOrEqual(before + PASSKEY_OFFER_COOLDOWN_DAYS * DAY_MS);
      expect(until).toBeLessThanOrEqual(Date.now() + PASSKEY_OFFER_COOLDOWN_DAYS * DAY_MS);
      expect(PASSKEY_OFFER_COOLDOWN_DAYS).toBe(30);

      dialog.open.mockClear();
      await service.offerAfterPasswordSignIn('alice');
      expect(dialog.open).not.toHaveBeenCalled();

      localStorage.setItem('X-Score-PasskeyOffer:alice', JSON.stringify({until: Date.now() - 1}));
      await service.offerAfterPasswordSignIn('alice');
      expect(dialog.open).toHaveBeenCalledTimes(1);
    });

    it('"Don\'t ask again" is remembered for good', async () => {
      setup({answer: 'never'});
      await service.offerAfterPasswordSignIn('alice');
      expect(JSON.parse(stored())).toEqual({never: true});

      dialog.open.mockClear();
      await service.offerAfterPasswordSignIn('alice');
      expect(dialog.open).not.toHaveBeenCalled();
    });

    it('closing the dialog without answering counts as "Not now"', async () => {
      setup({answer: undefined});
      await service.offerAfterPasswordSignIn('alice');
      expect(JSON.parse(stored()).until).toBeGreaterThan(Date.now());
    });

    it('remembers nothing after a passkey was created, since the account now has one', async () => {
      setup({answer: 'created'});
      await service.offerAfterPasswordSignIn('alice');
      expect(stored()).toBeNull();
    });

    it('takes the user to the Passkeys box when they ask to manage passkeys', async () => {
      setup({answer: 'manage'});
      await service.offerAfterPasswordSignIn('alice');
      expect(router.navigateByUrl).toHaveBeenCalledWith('/settings/account/passkeys');
      expect(stored()).toBeNull();
    });

    it('remembers the answer per account', async () => {
      setup({answer: 'never'});
      await service.offerAfterPasswordSignIn('alice');

      dialog.open.mockClear();
      await service.offerAfterPasswordSignIn('bob');
      expect(dialog.open).toHaveBeenCalledTimes(1);
    });

    it('ignores a damaged memory instead of failing', async () => {
      setup({answer: 'created'});
      localStorage.setItem('X-Score-PasskeyOffer:alice', '{not json');
      await service.offerAfterPasswordSignIn('alice');
      expect(dialog.open).toHaveBeenCalledTimes(1);
    });

    it('never throws, whatever goes wrong', async () => {
      setup();
      dialog.open.mockImplementation(() => {
        throw new Error('boom');
      });
      await expect(service.offerAfterPasswordSignIn('alice')).resolves.toBeUndefined();
    });
  });
});
