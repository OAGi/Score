import {Component, HostListener, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {of} from 'rxjs';
import {AccountDetails} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.css']
})
export class AccountDetailComponent implements OnInit {
  title = 'Edit Account';
  accountId;
  account: AccountDetails;
  newPassword: string;
  confirmPassword: string;
  loading: boolean;

  constructor(private service: AccountListService,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
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

  get isOAuth2User(): boolean {
    return this.account && (!!this.account.oAuth2UserId && this.account.oAuth2UserId > 0);
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

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  get canRemove(): boolean {
    return !this.account.hasData;
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

    this.service.update(this.account.userId,
        this.account.username, this.account.organization,
        this.account.admin,
        this.newPassword).subscribe(_ => {

      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/account');
    });
  }

  setEnable(val: boolean) {
    this.service.setEnable(this.account.userId, val).subscribe(_ => {
      this.snackBar.open((val) ? 'Enabled' : 'Disabled', '', {
        duration: 3000,
      });

      this.account.enabled = val;
    });
  }

  disassociateSSO() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Disassociate SSO?';
    dialogConfig.data.content = ['Are you sure you want to disassociate SSO from this user?'];
    dialogConfig.data.action = 'Disassociate';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.service.delink(this.account.userId).subscribe(_ => {
          this.snackBar.open('Disassociated', '', {
            duration: 3000,
          });
          this.router.navigateByUrl('/account');
        });
      });
  }

  removeAccount() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove account?';
    dialogConfig.data.content = ['The removed account cannot be recovered.', 'Are you sure you want to remove this account?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.service.remove(this.account.userId).subscribe(_ => {
          this.snackBar.open('Removed', '', {
            duration: 3000,
          });
          this.router.navigateByUrl('/account');
        });
      });
  }
}
