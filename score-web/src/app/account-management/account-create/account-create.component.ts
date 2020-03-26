import {Component, OnInit} from '@angular/core';
import {AccountListService} from '../domain/account-list.service';
import {MatSnackBar} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountList} from '../domain/accounts';
import {GrowlService} from 'ngx-growl';
import {HttpErrorResponse} from '@angular/common/http';
import {catchError} from 'rxjs/operators';
import {EMPTY, throwError} from 'rxjs';

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
  creable = false;

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
  }

  hasMinLengthError(variable: string) {
    return (variable.length < 5);
  }

  hasConfirmPasswordError() {
    return (this.newPassword !== '') && (this.confirmPassword !== '') && (this.newPassword !== this.confirmPassword);
  }

  isDisabled() {
    return !this.creable || ((this.newPassword === '') || (this.confirmPassword === '') ||
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
    this.creable = false;

    if (!value) {
      return;
    }

    this.service.getAccountNames().subscribe(resp => {
      if (resp.indexOf(value) > -1) {
        this.creable = false;
        this.growlService.addError({heading: 'Oops', message: 'This Login ID is already taken.'});
      } else {
        this.creable = true;
      }
    });
  }
}
