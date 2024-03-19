import {Component, OnInit} from '@angular/core';
import {SettingsAccountService} from './domain/settings-account.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../authentication/auth.service';
import {Router} from '@angular/router';
import {AbstractControl, FormControl, ValidatorFn, Validators} from '@angular/forms';

@Component({
  selector: 'score-settings-account',
  templateUrl: './settings-account.component.html',
  styleUrls: ['./settings-account.component.css']
})
export class SettingsAccountComponent implements OnInit {

  loading: boolean;

  originalEmail: string;
  emailFormControl: FormControl;
  emailVerified: any = undefined;

  oldPasswordFormControl: FormControl;
  newPasswordFormControl: FormControl;
  confirmPasswordFormControl: FormControl;

  hideChangePassword: boolean;

  constructor(private service: SettingsAccountService,
              private accountService: AccountListService,
              private snackBar: MatSnackBar,
              private auth: AuthService,
              private router: Router) {
  }

  ngOnInit() {
    const token = this.auth.getUserToken();
    this.accountService.getAccount(token.username).subscribe(resp => {
      this.originalEmail = resp.email;
      this.emailFormControl = new FormControl(resp.email, [Validators.email]);
      if (!!resp.email) {
        this.emailVerified = resp.emailVerified;
      }
    });

    this.hideChangePassword = token.authentication !== 'basic';

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
      email_validation_link: window.location.href.replace('/settings/account', '/settings/email_validation')
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
      email_validation_link: window.location.href.replace('/settings/account', '/settings/email_validation')
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

  updatePassword() {
    this.service.updatePassword(this.oldPasswordFormControl.value, this.newPasswordFormControl.value)
      .subscribe(_ => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
  }

}
