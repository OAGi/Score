import {of} from 'rxjs';
import {SettingsAccountComponent} from './settings-account.component';

describe('SettingsAccountComponent', () => {
  it('should be defined', () => {
    expect(SettingsAccountComponent).toBeTruthy();
  });
});

/** The validation link must be built for the saved address, not for an unsaved edit of the field. */
describe('SettingsAccountComponent resend validation request', () => {
  it('sends the saved e-mail even when the field holds an unsaved edit', () => {
    const resend = vi.fn(() => of(null));
    const self = {
      service: {resendEmailValidationRequest: resend},
      snackBar: {open: vi.fn()},
      originalEmail: 'a@example.com',
      emailFormControl: {value: 'b@example.com'},
    };
    SettingsAccountComponent.prototype.resendEmailValidationRequest.call(self);
    expect(resend).toHaveBeenCalledWith({email: 'a@example.com'}, expect.anything());
  });
});
