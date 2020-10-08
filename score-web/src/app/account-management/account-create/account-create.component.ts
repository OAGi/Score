import {Component, OnInit} from '@angular/core';
import {AccountListService} from '../domain/account-list.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountList} from '../domain/accounts';
import {PendingAccount} from '../domain/pending-list';

@Component({
  selector: 'score-account-create',
  templateUrl: './account-create.component.html',
  styleUrls: ['./account-create.component.css']
})
export class AccountCreateComponent implements OnInit {
  title = 'Create Account';
  newPassword: string;
  confirmPassword: string;
  account: AccountList;
  pending: PendingAccount;
  enable = false;

  constructor(private service: AccountListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.pending = new PendingAccount(this.route.snapshot.queryParamMap);
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
    if (!this.enable) {
      return true;
    }
    if (this.isLinkOauth2()) {
      return false;
    } else {
      return ((this.newPassword === '') || (this.confirmPassword === '') ||
        (this.hasMinLengthError(this.newPassword) || this.hasMinLengthError(this.confirmPassword)) || this.hasConfirmPasswordError());
    }
  }

  create() {
    if (this.enable) {
      this.service.create(this.account, this.newPassword, this.pending).subscribe(_ => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/account');
      });
    } else {
      this.snackBar.open('You cannot create the account since this Login ID is already taken.', '', {
        duration: 3000,
      });
    }
  }

  changeLogin(loginId: string) {
    this.enable = false;

    if (!loginId) {
      return;
    }

    this.service.getAccountNames().subscribe(resp => {
      if (resp.indexOf(loginId) > -1) {
        this.snackBar.open('This Login ID is already taken.', '', {
          duration: 3000,
        });
      } else {
        this.enable = true;
      }
    });
  }

  isLinkOauth2(): boolean {
    return this.pending && !!this.pending.appOauth2UserId;
  }
}
