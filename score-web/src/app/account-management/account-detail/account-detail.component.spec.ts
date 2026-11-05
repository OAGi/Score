import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Title} from '@angular/platform-browser';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {BehaviorSubject, NEVER, of} from 'rxjs';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AccountListService} from '../domain/account-list.service';
import {AccountDetailComponent} from './account-detail.component';

describe('AccountDetailComponent', () => {
  function setup(getAccount: any, signedInLoginId = 'admin') {
    TestBed.configureTestingModule({
      declarations: [AccountDetailComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {provide: AccountListService, useValue: {getAccount: () => getAccount}},
        {provide: AuthService, useValue: {isAdmin: () => true, getUserToken: () => ({username: signedInLoginId})}},
        {provide: ConfirmDialogService, useValue: {}},
        {provide: MatSnackBar, useValue: {open: () => undefined}},
        {provide: Router, useValue: {navigateByUrl: () => undefined}},
        {provide: Title, useValue: {setTitle: () => undefined}},
        {provide: ActivatedRoute, useValue: {paramMap: new BehaviorSubject(convertToParamMap({id: '3'}))}},
      ],
    });
    return TestBed.createComponent(AccountDetailComponent);
  }

  it('should be defined', () => {
    expect(AccountDetailComponent).toBeTruthy();
  });

  it('renders without error before the account is loaded', () => {
    const fixture = setup(NEVER);
    expect(() => fixture.detectChanges()).not.toThrow();
    expect(fixture.nativeElement.textContent).not.toContain('Remove this account');
  });

  it('shows the remove button for a disabled account without data', () => {
    const fixture = setup(of({userId: 3, loginId: 'u', enabled: false, hasData: false}));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Remove this account');
  });

  it('offers the new password fields for another account', () => {
    const fixture = setup(of({userId: 3, loginId: 'u', enabled: true, hasData: true}));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-id="user.newPassword"]')).not.toBeNull();
  });

  it('hides the new password fields on the signed-in user\'s own account', () => {
    const fixture = setup(of({userId: 3, loginId: 'admin', enabled: true, hasData: true}));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-id="user.newPassword"]')).toBeNull();
  });
});
