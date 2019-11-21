import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AccountList} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {switchMap} from 'rxjs/operators';

@Component({
  selector: 'srt-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.css']
})
export class AccountDetailComponent implements OnInit {
  title = 'Edit Account';
  account: AccountList;
  newPassword: string;
  confirmPassword: string;

  constructor(private service: AccountListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getAccount(params.get('id')))
    ).subscribe(resp => {
      this.account = resp;
    });

    this.newPassword = '';
    this.confirmPassword = '';
  }


  hasMinLengthError(variable: string) {
    return (variable.length < 5);
  }

  hasConfirmPasswordError() {
    return (this.newPassword !== this.confirmPassword);
  }

  isDisabled() {
    if ((this.newPassword === '') && (this.confirmPassword === '')) {
      return false;
    } else {
      return ((this.hasMinLengthError(this.newPassword) || this.hasMinLengthError(this.confirmPassword)) || this.hasConfirmPasswordError());
    }
  }

  update() {
    this.service.updatePasswordAccount(this.account, this.newPassword).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 2000,
      });
      this.router.navigateByUrl('/account');
    });
  }

}
