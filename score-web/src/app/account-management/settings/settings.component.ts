import {Component, OnInit} from '@angular/core';
import {SettingsService} from './domain/settings.service';
import {MatSnackBar} from '@angular/material';
import {Router} from '@angular/router';

@Component({
  selector: 'srt-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {

  title = 'Change password';

  oldPassword: string;
  newPassword: string;
  confirmPassword: string;

  constructor(private service: SettingsService,
              private snackBar: MatSnackBar,
              private router: Router) {
  }

  ngOnInit() {
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
        duration: 2000,
      });
      this.router.navigateByUrl('/');
    });
  }

}
