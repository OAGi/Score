import {Component, OnInit} from '@angular/core';
import {AccountListService} from '../domain/account-list.service';
import {MatSnackBar} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountList} from '../domain/accounts';
import {GrowlService} from 'ngx-growl';

@Component({
  selector: 'srt-account-create',
  templateUrl: './account-create.component.html',
  styleUrls: ['./account-create.component.css']
})
export class AccountCreateComponent implements OnInit {
  title = 'Create Account';
  newPassword: string;
  confirmPassword: string;
  account: AccountList;
  loginNames;
  loginUsed: boolean[] = [];
  creable = true;

  constructor(private service: AccountListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private growlService: GrowlService,
              private router: Router) {
  }

  ngOnInit() {
    this.account = new AccountList();
    this.newPassword = '';
    this.confirmPassword = '';
    this.service.getAccountNames().subscribe(data => {
      this.loginNames = data;
    });

  }

  hasMinLengthError(variable: string) {
    return (variable.length < 5);
  }

  hasConfirmPasswordError() {
    return (this.newPassword !== '') && (this.confirmPassword !== '') && (this.newPassword !== this.confirmPassword);
  }

  isDisabled() {
    return ((this.newPassword === '') || (this.confirmPassword === '') ||
      (this.hasMinLengthError(this.newPassword) || this.hasMinLengthError(this.confirmPassword)) || this.hasConfirmPasswordError());
  }

  create() {
    if (this.creable) {
      this.service.create(this.account, this.newPassword).subscribe(_ => {
        this.snackBar.open('Created', '', {
          duration: 2000,
        });
        this.router.navigateByUrl('/account');
      });
    } else {
      this.growlService.addError({heading: 'Oops', message: 'You cannot create the account since this Login ID is already taken.'});
    }
  }

  changeLogin(value) {
    this.loginUsed = [];
    this.creable = true;
    for (let i = 0; i < this.loginNames.length; i++) {
      if (value === this.loginNames[i]) {
        this.growlService.addError({heading: 'Oops', message: 'This Login ID is already taken.'});
        this.loginUsed[i] = true;
      }
    }
    if (this.loginUsed.length > 0) {
      this.creable = !this.loginUsed.includes(true);
    }
  }
}
