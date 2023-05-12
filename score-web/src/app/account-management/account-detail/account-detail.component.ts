import {Component, HostListener, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {of} from 'rxjs';
import {AccountList} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {finalize, switchMap} from 'rxjs/operators';

@Component({
  selector: 'score-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.css']
})
export class AccountDetailComponent implements OnInit {
  title = 'Edit Account';
  accountId;
  account: AccountList;
  newPassword: string;
  confirmPassword: string;
  loading: boolean;

  constructor(private service: AccountListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => of(Number(params.get('id'))))
    ).subscribe(accountId => {
      this.loading = true;
      this.service.getAccount(accountId).pipe(finalize(() => {
        this.loading = false;
      })).subscribe(resp => {
        this.account = resp;
      });
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

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  get updateDisabled(): boolean {
    return this.isDisabled();
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.service.updatePasswordAccount(this.account, this.newPassword).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/account');
    });
  }

  setEnable(val: boolean) {
    this.service.setEnable(this.account, val).subscribe(_ => {
      this.snackBar.open((val) ? 'Enabled' : 'Disabled', '', {
        duration: 3000,
      });

      this.account.enabled = val;
    });
  }


}
