import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {firstValueFrom, Observable, of, throwError} from 'rxjs';
import {vi} from 'vitest';
import {AuthService} from './auth.service';
import {
  allOf, BUSINESS_TERM_USERS, canActivateUser, hasRole, lacksRole, MULTI_TENANT_ON, NOT_DEVELOPERS_OPENAPI,
  ONLY_ADMINISTRATORS, ONLY_DEVELOPERS
} from './can-activate-user';
import {UserToken} from './domain/auth';

describe('access rules', () => {
  const token = (roles: string[], extra: any = {}) => ({roles, ...extra}) as UserToken;

  it('hasRole and lacksRole give their message when the role is missing or present', () => {
    expect(hasRole('admin', 'm')(token(['admin']))).toBeUndefined();
    expect(hasRole('admin', 'm')(token(['developer']))).toBe('m');
    expect(lacksRole('developer', 'm')(token(['end-user']))).toBeUndefined();
    expect(lacksRole('developer', 'm')(token(['developer']))).toBe('m');
  });

  it('allOf gives the first reason', () => {
    expect(allOf(() => undefined, () => 'b', () => 'c')(token([]))).toBe('b');
    expect(allOf(() => undefined)(token([]))).toBeUndefined();
  });

  it('knows who may use what', () => {
    expect(ONLY_ADMINISTRATORS(token(['developer']))).toBe('available only to administrators');
    expect(ONLY_DEVELOPERS(token(['end-user']))).toBe('available only to developers');
    expect(MULTI_TENANT_ON(token([], {tenant: {enabled: false}}))).toBe('not available because multi-tenant mode is not enabled');
    expect(MULTI_TENANT_ON(token([], {tenant: {enabled: true}}))).toBeUndefined();
    expect(NOT_DEVELOPERS_OPENAPI(token(['admin', 'developer']))).toBe('not available to developer-role users');
    expect(NOT_DEVELOPERS_OPENAPI(token(['end-user']))).toBeUndefined();
    expect(BUSINESS_TERM_USERS(token(['end-user'], {businessTerm: {enabled: false}}))).toBe('not available because Business Term management is not enabled');
    expect(BUSINESS_TERM_USERS(token(['developer'], {businessTerm: {enabled: true}})))
      .toBe('not available to developer-role users');
    expect(BUSINESS_TERM_USERS(token(['end-user'], {businessTerm: {enabled: true}}))).toBeUndefined();
  });
});

describe('canActivateUser', () => {
  let stored: UserToken;
  let session: UserToken;
  let signedInStored: boolean;
  let reload: any;
  let logout: any;
  let snackBar: { open: any };
  const parseUrl = (url: string) => ({url});
  const route = {
    pathFromRoot: [
      {url: [], routeConfig: {path: 'release', title: 'Release'}},
      {url: [{toString: () => 'release'}, {toString: () => 'create'}], routeConfig: {path: 'create', title: 'Create Release'}}
    ]
  } as any as ActivatedRouteSnapshot;

  /** A token as stored, and as the session says (what a reload stores). */
  function setup(storedToken: Partial<UserToken> & { signedIn?: boolean }, sessionToken: Partial<UserToken> = storedToken) {
    stored = {roles: [], ...storedToken} as UserToken;
    session = {roles: [], ...sessionToken} as UserToken;
    signedInStored = storedToken.signedIn ?? true;
    reload = vi.fn(() => {
      stored = session;
      signedInStored = true;
      return of(session);
    });
    logout = vi.fn();
    snackBar = {open: vi.fn()};
    TestBed.configureTestingModule({
      providers: [
        {provide: AuthService, useValue: {getUserToken: () => stored, isAuthenticated: () => signedInStored, reloadUserToken: reload, logout}},
        {provide: Router, useValue: {parseUrl}},
        {provide: MatSnackBar, useValue: snackBar}
      ]
    });
  }

  function run(guard: CanActivateFn): any {
    return TestBed.runInInjectionContext(() => guard(route, {} as RouterStateSnapshot));
  }

  const result = (guard: CanActivateFn): Promise<any> => firstValueFrom(run(guard) as Observable<any>);

  it('lets a signed-in user in at once, without asking the server', () => {
    setup({roles: ['end-user']});
    expect(run(canActivateUser())).toBe(true);
    expect(reload).not.toHaveBeenCalled();
  });

  it('lets in a user who is allowed by the rule, without asking the server', () => {
    setup({roles: ['developer']});
    expect(run(canActivateUser(ONLY_DEVELOPERS, '/release'))).toBe(true);
    expect(reload).not.toHaveBeenCalled();
  });

  it('refreshes the token of a fresh sign-in, which lacks `enabled`, and lets the user in', async () => {
    setup({roles: ['end-user'], signedIn: false}, {roles: ['end-user']});
    expect(await result(canActivateUser())).toBe(true);
    expect(reload).toHaveBeenCalled();
    expect(logout).not.toHaveBeenCalled();
  });

  it('checks a refusal against the session, so a setting changed since is not held against the user', async () => {
    setup({roles: ['end-user'], businessTerm: {enabled: false} as any}, {roles: ['end-user'], businessTerm: {enabled: true} as any});
    expect(await result(canActivateUser(BUSINESS_TERM_USERS))).toBe(true);
    expect(snackBar.open).not.toHaveBeenCalled();
  });

  it('sends a signed-in user who is not allowed to the given page, with the reason, instead of the sign-in page', async () => {
    setup({roles: ['end-user']});
    expect(await result(canActivateUser(ONLY_DEVELOPERS, '/release'))).toEqual(parseUrl('/release'));
    expect(snackBar.open).toHaveBeenCalledWith(
      '\'Create Release\' is available only to developers.', '', {duration: 3000});
    expect(logout).not.toHaveBeenCalled();
  });

  it('names the page by the nearest title when the route has none of its own', async () => {
    setup({roles: ['end-user']});
    const child = {pathFromRoot: [
      {url: [], routeConfig: {path: 'release/:id', title: 'Release'}},
      {url: [], routeConfig: {path: ''}}
    ]} as any as ActivatedRouteSnapshot;
    await firstValueFrom(TestBed.runInInjectionContext(() =>
      canActivateUser(ONLY_ADMINISTRATORS)(child, {} as RouterStateSnapshot)) as Observable<any>);
    expect(snackBar.open).toHaveBeenCalledWith('\'Release\' is available only to administrators.', '', {duration: 3000});
  });

  it('names the page "This page" when its route has no title', async () => {
    setup({roles: ['end-user']});
    const untitled = {pathFromRoot: [{url: [], routeConfig: {path: 'x'}}]} as any as ActivatedRouteSnapshot;
    await firstValueFrom(TestBed.runInInjectionContext(() =>
      canActivateUser(ONLY_ADMINISTRATORS)(untitled, {} as RouterStateSnapshot)) as Observable<any>);
    expect(snackBar.open).toHaveBeenCalledWith('This page is available only to administrators.', '', {duration: 3000});
  });

  it('goes to the home page when no page is given', async () => {
    setup({roles: ['developer']});
    expect(await result(canActivateUser(ONLY_ADMINISTRATORS))).toEqual(parseUrl('/'));
  });

  it('signs out, to come back to the page after signing in, when the user is not signed in', async () => {
    setup({roles: [], signedIn: false}, {roles: []});
    reload.mockImplementation(() => of({}));
    signedInStored = false;
    expect(await result(canActivateUser(ONLY_DEVELOPERS))).toBe(false);
    expect(logout).toHaveBeenCalledWith('/release/create');
    expect(snackBar.open).not.toHaveBeenCalled();
  });

  it('signs out when the session cannot be read', async () => {
    setup({roles: [], signedIn: false});
    reload.mockReturnValue(throwError(() => new Error('401')));
    expect(await result(canActivateUser())).toBe(false);
    expect(logout).toHaveBeenCalledWith('/release/create');
  });
});
