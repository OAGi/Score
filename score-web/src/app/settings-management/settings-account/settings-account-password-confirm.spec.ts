import {of} from 'rxjs';
import {SettingsAccountComponent} from './settings-account.component';

/** The confirmation must keep matching the new password, whichever of the two is edited last. */
describe('SettingsAccountComponent change password', () => {
  const init = () => {
    const self: Record<string, any> = {
      userToken: {username: 'u', authentication: 'basic'},
      section: 'password',
      accountService: {getAccount: () => of({email: 'a@b.c', emailVerified: true})},
      router: {navigateByUrl: vi.fn()},
      hideChangePassword: false,
      showPasskeys: false,
      equals: SettingsAccountComponent.prototype.equals,
    };
    SettingsAccountComponent.prototype.ngOnInit.call(self);
    const form = {
      set: (oldPw: string, newPw: string, confirm: string) => {
        self['oldPasswordFormControl'].setValue(oldPw);
        self['confirmPasswordFormControl'].setValue(confirm);
        self['newPasswordFormControl'].setValue(newPw);
      },
      disabled: () => SettingsAccountComponent.prototype.isPasswordFormDisabled.call(self),
    };
    return {self, form};
  };

  it('enables Update when the new password and the confirmation match', () => {
    const {form} = init();
    form.set('old12', 'abcde1', 'abcde1');
    expect(form.disabled()).toBe(false);
  });

  it('disables Update when the new password is edited after the confirmation', () => {
    const {self, form} = init();
    form.set('old12', 'abcde1', 'abcde1');
    self['newPasswordFormControl'].setValue('zzzzz9');
    expect(self['confirmPasswordFormControl'].hasError('equals')).toBe(true);
    expect(form.disabled()).toBe(true);
  });

  it('does not send the request when the form is invalid', () => {
    const updatePassword = vi.fn(() => of(null));
    const snackBar = {open: vi.fn()};
    SettingsAccountComponent.prototype.updatePassword.call(
      {service: {updatePassword}, snackBar, oldPasswordFormControl: {}, newPasswordFormControl: {}}, {valid: false} as never);
    expect(snackBar.open).toHaveBeenCalledWith('Password form is invalid.', '', {duration: 3000});
    expect(updatePassword).not.toHaveBeenCalled();
  });
});
