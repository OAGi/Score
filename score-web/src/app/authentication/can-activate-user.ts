import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {catchError, map} from 'rxjs/operators';
import {of} from 'rxjs';
import {AuthService, getResolvedUrl} from './auth.service';
import {UserToken} from './domain/auth';
import {ROLE} from './domain/role';

/**
 * What a page asks of the signed-in user: why the page is not available to the user, as the end of "<page> is …",
 * or nothing when the user may open it.
 */
export type AccessRule = (token: UserToken) => string | undefined;

export const hasRole = (role: string, unavailable: string): AccessRule =>
  token => token.roles.includes(role) ? undefined : unavailable;

export const lacksRole = (role: string, unavailable: string): AccessRule =>
  token => token.roles.includes(role) ? unavailable : undefined;

/** The first reason among the rules. */
export const allOf = (...rules: AccessRule[]): AccessRule =>
  token => rules.map(rule => rule(token)).find(reason => reason !== undefined);

export const ONLY_ADMINISTRATORS = hasRole(ROLE.ADMIN, 'available only to administrators');
export const ONLY_DEVELOPERS = hasRole(ROLE.DEVELOPER, 'available only to developers');
export const MULTI_TENANT_ON: AccessRule =
  token => token.tenant?.enabled ? undefined : 'not available because multi-tenant mode is not enabled';
/** The menu hides Business Term management from developers, and the server refuses them. */
export const BUSINESS_TERM_USERS = allOf(
  token => token.businessTerm?.enabled ? undefined : 'not available because Business Term management is not enabled',
  lacksRole(ROLE.DEVELOPER, 'not available to developer-role users'));
/** The menu hides OpenAPI Documents from developers. */
export const NOT_DEVELOPERS_OPENAPI =
  lacksRole(ROLE.DEVELOPER, 'not available to developer-role users');

/**
 * The title that the route of the page is configured with (the nearest one, as an empty-path child of a titled route
 * has none of its own). `ActivatedRouteSnapshot.title` is not set yet when a guard runs.
 */
function pageTitle(route: ActivatedRouteSnapshot): string | undefined {
  return [...route.pathFromRoot].reverse()
    .map(r => r.routeConfig?.title)
    .find((title): title is string => typeof title === 'string');
}

/**
 * The guard of a page for signed-in users, optionally with something more asked of them (the rule).
 *
 * - Not signed in: goes to the sign-in page, which brings the user back to the page afterwards.
 * - Signed in but not allowed: stays signed in, is told in a snack bar which page is not available and why (the
 *   page is named by the title of its route), and goes to `redirectTo`, which is a page of the same area that the
 *   user may open.
 *
 * The stored token is only a copy of what the session says (the sign-in response lacks some of its fields, and a
 * setting may have changed since), so a refusal is checked once more against a token read from the session.
 */
export function canActivateUser(rule: AccessRule = () => undefined, redirectTo = '/'): CanActivateFn {
  return route => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const snackBar = inject(MatSnackBar);

    const verdict = () => auth.isAuthenticated()
      ? {signedIn: true, reason: rule(auth.getUserToken())}
      : {signedIn: false, reason: undefined};

    const stored = verdict();
    if (stored.signedIn && !stored.reason) {
      return true;
    }

    return auth.reloadUserToken().pipe(
      map(() => {
        const fresh = verdict();
        if (!fresh.signedIn) {
          auth.logout(getResolvedUrl(route));
          return false;
        }
        if (!fresh.reason) {
          return true;
        }
        snackBar.open(`${pageTitle(route) ? `'${pageTitle(route)}'` : 'This page'} is ${fresh.reason}.`, '', {duration: 3000});
        return router.parseUrl(redirectTo);
      }),
      catchError(() => {
        auth.logout(getResolvedUrl(route));
        return of(false);
      }));
  };
}
