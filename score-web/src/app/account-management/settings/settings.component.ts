import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {AccountListService} from '../domain/account-list.service';
import {SettingsService} from './domain/settings.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';

@Component({
  selector: 'score-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {

  title = 'Change password';

  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
  hideChangePassword: boolean;

  constructor(private service: SettingsService,
              private accountService: AccountListService,
              private snackBar: MatSnackBar,
              private auth: AuthService,
              private router: Router) {
  }

  ngOnInit() {
    const token = this.auth.getUserToken();
    this.hideChangePassword = token.authentication !== 'basic';
    this.oldPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  hasMinLengthError(variable: string) {
    return (variable.length < 5);
  }

  hasConfirmPasswordError() {
    return (this.newPassword !== '') && (this.confirmPassword !== '') && (this.newPassword !== this.confirmPassword);
  }

  isDisabled() {
    return (this.oldPassword === '') || (this.newPassword === '') || (this.confirmPassword === '') ||
      (this.hasMinLengthError(this.oldPassword) || this.hasMinLengthError(this.newPassword) || this.hasMinLengthError(this.confirmPassword)) ||
      this.hasConfirmPasswordError();
  }

  update() {
    this.service.updatePassword(this.oldPassword, this.newPassword).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/');
    });
  }

}
