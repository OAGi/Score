import {Component, OnInit} from '@angular/core';
import {SettingsPasswordService} from './domain/settings-password.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../authentication/auth.service';
import {Router} from '@angular/router';
import {AbstractControl, FormControl, ValidatorFn, Validators} from '@angular/forms';

@Component({
  selector: 'score-settings-password',
  templateUrl: './settings-password.component.html',
  styleUrls: ['./settings-password.component.css']
})
export class SettingsPasswordComponent implements OnInit {

  title = 'Change password';

  oldPasswordFormControl: FormControl;
  newPasswordFormControl: FormControl;
  confirmPasswordFormControl: FormControl;

  hideChangePassword: boolean;

  constructor(private service: SettingsPasswordService,
              private accountService: AccountListService,
              private snackBar: MatSnackBar,
              private auth: AuthService,
              private router: Router) {
  }

  ngOnInit() {
    const token = this.auth.getUserToken();
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

  isDisabled() {
    return this.oldPasswordFormControl.invalid ||
      this.newPasswordFormControl.invalid ||
      this.confirmPasswordFormControl.invalid;
  }

  update() {
    this.service.updatePassword(this.oldPasswordFormControl.value, this.newPasswordFormControl.value)
      .subscribe(_ => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/');
      });
  }

}
