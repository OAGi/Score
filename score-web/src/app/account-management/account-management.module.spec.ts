import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {firstValueFrom, isObservable, Observable, of} from 'rxjs';
import {AccountManagementModule, routes} from './account-management.module';
import {AuthService} from '../authentication/auth.service';

describe('AccountManagementModule', () => {
  let accountManagementModule: AccountManagementModule;

  beforeEach(() => {
    accountManagementModule = new AccountManagementModule();
  });

  it('should create an instance', () => {
    expect(accountManagementModule).toBeTruthy();
  });

  it('guards every tenant route and every account route', () => {
    for (const route of routes.filter(r => r.path?.startsWith('tenant') || r.path?.startsWith('account'))) {
      expect(route.canActivate?.length, route.path).toBe(1);
    }
  });

  describe('tenant route access', () => {
    const tenantRoutes = () => routes.filter(route => route.path?.startsWith('tenant'));

    /** Whether each tenant route lets the user in, and where a refused user is sent. */
    async function canAccess(roles: string[], tenantEnabled: boolean): Promise<{allowed: boolean; to?: string}[]> {
      const parseUrl = (url: string) => ({url});
      const token = {roles, enabled: true, tenant: {enabled: tenantEnabled}};
      TestBed.configureTestingModule({
        providers: [
          {provide: AuthService, useValue: {
            isAuthenticated: () => true, getUserToken: () => token, reloadUserToken: () => of(token), logout: vi.fn()}},
          {provide: Router, useValue: {parseUrl}},
          {provide: MatSnackBar, useValue: {open: vi.fn()}},
        ],
      });
      const route = {pathFromRoot: [{url: []}]} as unknown as ActivatedRouteSnapshot;
      const state = {url: '/tenant'} as RouterStateSnapshot;
      const results = [];
      for (const r of tenantRoutes()) {
        const guard = (r.canActivate as CanActivateFn[])[0];
        let verdict: any = TestBed.runInInjectionContext(() => guard(route, state));
        if (isObservable(verdict)) {
          verdict = await firstValueFrom(verdict as Observable<any>);
        }
        results.push(verdict === true ? {allowed: true} : {allowed: false, to: verdict?.url});
      }
      return results;
    }

    it('allows an administrator when the tenant feature is enabled', async () => {
      expect((await canAccess(['admin'], true)).every(r => r.allowed)).toBe(true);
    });

    it('denies an administrator when the tenant feature is disabled', async () => {
      expect((await canAccess(['admin'], false)).some(r => r.allowed)).toBe(false);
    });

    it('denies a non-administrator, whether or not the tenant feature is enabled', async () => {
      expect((await canAccess(['end-user'], true)).some(r => r.allowed)).toBe(false);
      TestBed.resetTestingModule();
      expect((await canAccess(['end-user'], false)).some(r => r.allowed)).toBe(false);
    });

    it('sends a refused user to the home page', async () => {
      expect((await canAccess(['end-user'], true)).every(r => r.to === '/')).toBe(true);
    });
  });
});
