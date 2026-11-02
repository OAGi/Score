import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {AccountManagementModule, routes} from './account-management.module';
import {AuthService, CanActivateAdmin, CanActivateTenantInstance} from '../authentication/auth.service';

describe('AccountManagementModule', () => {
  let accountManagementModule: AccountManagementModule;

  beforeEach(() => {
    accountManagementModule = new AccountManagementModule();
  });

  it('should create an instance', () => {
    expect(accountManagementModule).toBeTruthy();
  });

  it('guards every tenant route with both the administrator and tenant-instance guards', () => {
    const tenantRoutes = routes.filter(route => route.path?.startsWith('tenant'));
    expect(tenantRoutes.length).toBeGreaterThan(0);
    for (const route of tenantRoutes) {
      expect(route.canActivate, route.path).toEqual([CanActivateAdmin, CanActivateTenantInstance]);
    }
  });

  describe('tenant route access', () => {
    const tenantRoutes = () => routes.filter(route => route.path?.startsWith('tenant'));

    function canAccess(roles: string[], tenantEnabled: boolean): boolean[] {
      const authService = {
        ROLE_ADMIN: 'admin',
        getUserToken: () => ({roles, tenant: {enabled: tenantEnabled}}),
        logout: vi.fn(),
      };
      TestBed.configureTestingModule({
        providers: [
          CanActivateAdmin,
          CanActivateTenantInstance,
          {provide: AuthService, useValue: authService},
        ],
      });
      const route = {pathFromRoot: [{url: []}]} as unknown as ActivatedRouteSnapshot;
      const state = {url: '/tenant'} as RouterStateSnapshot;
      return tenantRoutes().map(r => (r.canActivate as any[]).every(
        guard => TestBed.inject(guard as any).canActivate(route, state) === true));
    }

    it('allows an administrator when the tenant feature is enabled', () => {
      expect(canAccess(['admin'], true).every(allowed => allowed)).toBe(true);
    });

    it('denies an administrator when the tenant feature is disabled', () => {
      expect(canAccess(['admin'], false).some(allowed => allowed)).toBe(false);
    });

    it('denies a non-administrator when the tenant feature is enabled', () => {
      expect(canAccess(['end-user'], true).some(allowed => allowed)).toBe(false);
    });

    it('denies a non-administrator when the tenant feature is disabled', () => {
      expect(canAccess(['end-user'], false).some(allowed => allowed)).toBe(false);
    });
  });
});
