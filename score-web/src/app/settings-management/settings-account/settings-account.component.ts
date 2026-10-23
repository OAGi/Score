import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {SettingsAccountService} from './domain/settings-account.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../authentication/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {AbstractControl, FormControl, NgForm, ValidatorFn, Validators} from '@angular/forms';
import {UserToken} from '../../authentication/domain/auth';

@Component({
  standalone: false,
  selector: 'score-settings-account',
  templateUrl: './settings-account.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./settings-account.component.css']
})
export class SettingsAccountComponent implements OnInit {
  private service = inject(SettingsAccountService);
  private accountService = inject(AccountListService);
  private snackBar = inject(MatSnackBar);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);


  loading: boolean;

  /** Which of the account pages is shown: 'personal-info', 'password' or 'passkeys' (see the routes). */
  section: 'personal-info' | 'password' | 'passkeys' = this.route.snapshot.data['section'] || 'personal-info';

  originalEmail: string;
  emailFormControl: FormControl = new FormControl();
  emailVerified: any = undefined;

  oldPasswordFormControl: FormControl = new FormControl();
  newPasswordFormControl: FormControl = new FormControl();
  confirmPasswordFormControl: FormControl = new FormControl();

  hideChangePassword: boolean;

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  /** The entry of the settings menu that belongs to the page. */
  get menuItem(): string {
    return {'personal-info': 'PersonalInfo', 'password': 'Password', 'passkeys': 'Passkeys'}[this.section];
  }

  /** Passkeys belong to local accounts and exist only while passkey sign-in is configured. */
  get showPasskeys(): boolean {
    return this.auth.canUsePasskeys();
  }

  ngOnInit() {
    const token = this.userToken;
    this.accountService.getAccount(token.username).subscribe(resp => {
      this.originalEmail = resp.email;
      this.emailFormControl = new FormControl(resp.email, [Validators.email]);
      if (!!resp.email) {
        this.emailVerified = resp.emailVerified;
      }
    });

    // A local account signed in with a passkey still has a password to change.
    this.hideChangePassword = token.authentication !== 'basic' && token.authentication !== 'passkey';

    // A page that does not exist for this account (no password, passkeys off) falls back to the first one.
    if ((this.section === 'password' && this.hideChangePassword) || (this.section === 'passkeys' && !this.showPasskeys)) {
      this.router.navigateByUrl('/settings/account/personal-info');
    }

    this.oldPasswordFormControl = new FormControl('', [
      Validators.required, Validators.minLength(5), Validators.maxLength(100)
    ]);
    this.newPasswordFormControl = new FormControl('', [
      Validators.required, Validators.minLength(5), Validators.maxLength(100)
    ]);
    this.confirmPasswordFormControl = new FormControl('', [
      Validators.required, Validators.minLength(5), Validators.maxLength(100),
      this.equals(this.newPasswordFormControl)
    ]);
  }

  isPersonInfoFormDisabled() {
    return !this.emailFormControl.value || this.emailFormControl.invalid || (this.originalEmail === this.emailFormControl.value);
  }

  updatePersonalInfo() {
    this.loading = true;

    this.service.updatePersonalInfo({
      email: this.emailFormControl.value
    }, {
      email_validation_link: window.location.href.replace(/\/settings\/account.*$/, '/settings/email_validation')
    }).subscribe(_ => {
      if (this.emailFormControl.valid) {
        this.emailVerified = false;
      }
      this.originalEmail = this.emailFormControl.value;
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });

      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  resendEmailValidationRequest() {
    this.loading = true;

    this.service.resendEmailValidationRequest({
      email: this.emailFormControl.value
    }, {
      email_validation_link: window.location.href.replace(/\/settings\/account.*$/, '/settings/email_validation')
    }).subscribe(_ => {
      this.snackBar.open('Resent', '', {
        duration: 3000,
      });

      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  equals(anotherControl: AbstractControl): ValidatorFn {
    return (control: AbstractControl) => {
      if (control.value !== anotherControl.value) {
        return {equals: true};
      }
      return null;
    };
  }

  hasMinLengthError(variable: string): boolean {
    return (variable.length < 5);
  }

  isPasswordFormDisabled() {
    return this.oldPasswordFormControl.invalid ||
      this.newPasswordFormControl.invalid ||
      this.confirmPasswordFormControl.invalid;
  }

  updatePassword(form: NgForm) {
    if (!form.valid) {
      this.snackBar.open('Password form is invalid.', '', {
        duration: 3000,
      });
    }
    this.service.updatePassword(this.oldPasswordFormControl.value, this.newPasswordFormControl.value)
      .subscribe(_ => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
  }

}
