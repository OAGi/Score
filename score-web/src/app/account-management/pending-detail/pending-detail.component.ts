import {HttpParams} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {ConfirmDialogComponent} from '../../common/confirm-dialog/confirm-dialog.component';
import {ConfirmDialogConfig} from '../../common/confirm-dialog/confirm-dialog.domain';
import {base64Encode} from '../../common/utility';
import {AccountListDialogComponent} from '../account-list-dialog/account-list-dialog.component';
import {PendingAccount} from '../domain/pending-list';
import {switchMap} from 'rxjs/operators';
import {PendingListService} from '../domain/pending-list.service';

@Component({
  selector: 'score-account-detail',
  templateUrl: './pending-detail.component.html',
  styleUrls: ['./pending-detail.component.css']
})
export class PendingDetailComponent implements OnInit {

  title = 'Review Pending Account';
  pending: PendingAccount;

  constructor(private service: PendingListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private router: Router) {
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getPending(Number(params.get('id'))))
    ).subscribe(resp => {
      this.pending = resp;
    });
  }

  linkToAccount() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = this.pending;
    const dialogRef = this.dialog.open(AccountListDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(msg => {
      if (msg) {
        this.service.getPending(this.pending.appOauth2UserId).subscribe(resp => {
          this.pending = resp;
        });
        this.snackBar.open(msg, '', {
          duration: 3000,
        });
      }
    });
  }

  createNewAccount() {
    let params = new HttpParams();
    params = params.set('appOauth2UserId', String(this.pending.appOauth2UserId));
    if (this.pending.name) {
      params = params.set('name', this.pending.name);
    }
    if (this.pending.nickname) {
      params = params.set('nickname', this.pending.nickname);
    }
    if (this.pending.preferredUsername) {
      params = params.set('preferredUsername', this.pending.preferredUsername);
    }
    if (this.pending.email) {
      params = params.set('email', this.pending.email);
    }
    if (this.pending.sub) {
      params = params.set('sub', this.pending.sub);
    }

    this.router.navigateByUrl('account/create?q=' + base64Encode(params.toString()));
  }

  reject() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Confirm reject';
    dialogConfig.data.content = [
      'Are you sure you want to reject this user?'
    ];

    dialogConfig.data.action = 'Reject anyway';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.pending.rejected = true;

          this.service.update(this.pending).subscribe(resp => {
            this.snackBar.open('Rejected', '', {
              duration: 3000,
            });
            this.pending = resp;
            this.router.navigateByUrl('/account/pending');
          });
        }
      });
  }

  get status(): string {
    if (!this.pending) {
      return '';
    }

    if (this.pending.rejected) {
      return 'Rejected';
    }
    if (this.pending.appUserId) {
      return 'Linked';
    }

    return 'Pending';
  }

}
