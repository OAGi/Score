import { Injectable, OnInit, inject } from '@angular/core';
import {
  HttpClient,
  HttpContextToken,
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpHeaders,
  HttpInterceptor,
  HttpParams,
  HttpRequest
} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {catchError, map} from 'rxjs/operators';
import {Observable, of, throwError} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {
  BIEProperties,
  BrowseStandardModeProperties,
  BusinessTermProperties,
  FunctionsRequiringEmailTransmissionProperties,
  OAuth2AppInfo,
  TenantProperties,
  UserToken
} from './domain/auth';
import {MultiActionsSnackBarComponent} from '../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {Clipboard} from '@angular/cdk/clipboard';

@Injectable()
export class AuthService implements OnInit, CanActivate {
  private http = inject(HttpClient);
  private router = inject(Router);
  private logoutInProgress = false;


  RESTRICTED_NEXT_PARAMS = ['login', 'pending', 'reject'];
  USER_INFO_KEY = 'X-Score-UserInfo';
  ROLE_DEVELOPER = 'developer';
  ROLE_END_USER = 'end-user';
  ROLE_ADMIN = 'admin';

  ngOnInit() {
  }

  isServiceUnavailableFailure(error: any, url?: string): boolean {
    if (!(error instanceof HttpErrorResponse)) {
      return false;
    }

    if ([0, 503, 504].includes(error.status)) {
      return true;
    }

    if (error.status === 500 && !!url) {
      return url.indexOf('/api/' + environment.statePath) !== -1 ||
        url.indexOf('/api/' + environment.logoutPath) !== -1;
    }

    return false;
  }

  serviceUnavailableQueryParams(error: HttpErrorResponse): { reason: string; status?: number } {
    return {
      reason: error.status === 503 ? 'service' : 'gateway',
      status: error.status > 0 ? error.status : undefined
    };
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.http.get<UserToken>('/api/' + environment.statePath).pipe(map(res => {
      if (!!res) {
        this.storeUserInfo(res);
        const roles = res.roles;
        if (roles.includes('pending')) {
          return this.router.parseUrl('/pending');
        } else if (roles.includes('reject')) {
          this.logout(getResolvedUrl(route));
          return false;
        }
        if (!res.enabled) {
          return this.router.parseUrl('/disabled');
        }
        return true;
      } else {
        this.logout(getResolvedUrl(route));
        return false;
      }
    }), catchError(err => {
      if (this.isServiceUnavailableFailure(err, '/api/' + environment.statePath)) {
        return of(this.router.createUrlTree(['/service-unavailable'], {
          queryParams: this.serviceUnavailableQueryParams(err)
        }));
      }
      this.logout(getResolvedUrl(route));
      return of(false);
    }));
  }

  reloadUserToken(): Observable<UserToken> {
    return this.http.get<UserToken>('/api/' + environment.statePath).pipe(map(res => {
      if (!!res) {
        this.storeUserInfo(res);
      }
      return res;
    }));
  }

  authenticate(credentials, callback?: (value: any) => void, errCallback?: (error: any) => void) {
    const params = new HttpParams()
      .set('username', credentials.username)
      .set('password', credentials.password);
    const headers = new HttpHeaders({'Content-Type': 'application/x-www-form-urlencoded'});
    this.http.post<UserToken>('/api/' + environment.loginPath, params, {
      headers
    }).subscribe(res => {
      this.storeUserInfo(res);
      return callback && callback(res);
    }, err => {
      return errCallback && errCallback(err);
    });
  }

  storeUserInfo(res: UserToken) {
    localStorage.setItem(this.USER_INFO_KEY, btoa(JSON.stringify(res)));
  }

  getUserToken(): UserToken {
    let value;
    try {
      value = JSON.parse(atob(localStorage.getItem(this.USER_INFO_KEY)));

      if (!value.tenant) {
        value.tenant = new TenantProperties();
        value.tenant.enabled = false;
        value.tenant.roles = [];
        this.storeUserInfo(value);
      }
      if (!value.businessTerm) {
        value.businessTerm = new BusinessTermProperties();
        value.businessTerm.enabled = false;
        this.storeUserInfo(value);
      }
      if (!value.bie) {
        value.bie = new BIEProperties();
        value.bie.inverseMode = false;
        this.storeUserInfo(value);
      }
      if (!value.functionsRequiringEmailTransmission) {
        value.functionsRequiringEmailTransmission = new FunctionsRequiringEmailTransmissionProperties();
        value.functionsRequiringEmailTransmission.enabled = false;
        this.storeUserInfo(value);
      }
      if (!value.browseStandardMode) {
        value.browseStandardMode = new BrowseStandardModeProperties();
        value.browseStandardMode.enabled = false;
        this.storeUserInfo(value);
      }
    } catch (ignore) {
      value = new UserToken();
      this.storeUserInfo(value);
    }
    return value;
  }

  isAuthenticated() {
    const userToken = this.getUserToken();
    if (!userToken.enabled) {
      return false;
    }
    return userToken.roles.includes(this.ROLE_DEVELOPER) || userToken.roles.includes(this.ROLE_END_USER);
  }

  isLogoutInProgress(): boolean {
    return this.logoutInProgress;
  }

  isAdmin() {
    const userToken = this.getUserToken();
    if (!userToken.enabled) {
      return false;
    }
    return userToken.roles.includes(this.ROLE_ADMIN);
  }

  isDeveloper() {
    const userToken = this.getUserToken();
    if (!userToken.enabled) {
      return false;
    }
    return userToken.roles.includes(this.ROLE_DEVELOPER);
  }

  isEndUser() {
    const userToken = this.getUserToken();
    if (!userToken.enabled) {
      return false;
    }
    return userToken.roles.includes(this.ROLE_END_USER);
  }

  isTenantEnabled() {
    const userToken = this.getUserToken();
    return userToken?.tenant?.enabled === true;
  }

  isBrowseStandardModeEnabled() {
    const userToken = this.getUserToken();
    return userToken?.browseStandardMode?.enabled === true;
  }

  isBrowseStandardsMenuEnabled() {
    // Browse Standards mode is controlled by application configuration for end-user accounts.
    return this.isBrowseStandardModeEnabled() &&
      this.isEndUser() &&
      !this.isDeveloper() &&
      !this.isAdmin();
  }

  logout(url?) {
    this.logoutInProgress = true;
    localStorage.removeItem(this.USER_INFO_KEY);

    this.http.get('/api/' + environment.logoutPath)
      .subscribe(resp => {
        this.redirectToLogin(url);
      }, err => {
        if (this.isServiceUnavailableFailure(err, '/api/' + environment.logoutPath)) {
          this.logoutInProgress = false;
          this.router.navigate(['/service-unavailable'], {
            queryParams: this.serviceUnavailableQueryParams(err)
          });
          return;
        }
        this.redirectToLogin(url);
      });
  }

  logoutPath(): string {
    const userToken = this.getUserToken();
    if (!!userToken && userToken.authentication === 'oauth2') {
      return '/api/oauth2/logout';
    }
    return '/logout';
  }

  redirectToLogin(url?) {
    const commands = ['/' + environment.loginPath];
    const next = this.nextParam(url);
    if (!!next) {
      return this.router.navigate(commands, {
        queryParams: {
          next
        }
      }).finally(() => {
        this.logoutInProgress = false;
      });
    } else {
      return this.router.navigate(commands).finally(() => {
        this.logoutInProgress = false;
      });
    }
  }

  nextParam(next?: string): string | undefined {
    if (!next || next.length === 0 || next === '/') {
      return undefined;
    }
    for (const param of this.RESTRICTED_NEXT_PARAMS) {
      if (next.indexOf(param) !== -1) {
        return undefined;
      }
    }
    return next;
  }

  getOAuth2AppInfos(): Observable<OAuth2AppInfo[]> {
    return this.http.get<OAuth2AppInfo[]>('/api/info/oauth2-providers');
  }
}

@Injectable()
export class XhrInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const xhr = req.clone({
      withCredentials: true,
      headers: req.headers.set('X-Requested-With', 'XMLHttpRequest')
    });
    return next.handle(xhr);
  }
}

export const SUPPRESS_ERROR_ALERT = new HttpContextToken<boolean>(() => false);

@Injectable()
export class ErrorAlertInterceptor implements HttpInterceptor {
  private auth = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private clipboard = inject(Clipboard);


  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error, caught) => {
        if (req.context.get(SUPPRESS_ERROR_ALERT)) {
          return throwError(error);
        }

        if (error instanceof HttpErrorResponse || error.name === 'HttpErrorResponse') {
          if (this.auth.isServiceUnavailableFailure(error, req.url)) {
            this.router.navigate(['/service-unavailable'], {
              queryParams: this.auth.serviceUnavailableQueryParams(error)
            });
            return throwError(error);
          }

          switch (error.status) {
            case 400:
            case 403:
            case 500:
              const errorMessageId = error.headers.get('x-error-message-id');
              const errorMessage = error.headers.get('x-error-message');
              if (!!errorMessageId) {
                this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
                  data: {
                    titleIcon: 'error',
                    title: 'Error',
                    message: ((!!errorMessage) ? errorMessage : error.message),
                    action: 'View detail in Notifications',
                    onAction: (data, snackBarRef) => {
                      this.router.navigate(['/message/' + errorMessageId]);
                      snackBarRef.dismissWithAction();
                    }
                  }
                });
              } else {
                this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
                  data: {
                    titleIcon: 'error',
                    title: 'Error',
                    message: ((!!errorMessage) ? errorMessage : error.message),
                    action: 'Copy to clipboard',
                    onAction: (data, snackBarRef) => {
                      this.clipboard.copy(data.message);
                    }
                  }
                });
              }

              break;

            case 401:
              if (req.url.indexOf(environment.loginPath) !== -1 ||
                  req.url.indexOf(environment.statePath) !== -1) {
                // ignore
              } else if (req.url.indexOf(environment.logoutPath) === -1) {
                this.snackBar.open('Authentication Failure', '', {
                  duration: 3000,
                });

                this.auth.logout(window.location.pathname);
              }

              break;

            case 404:
              // handle per case
              break;

            default:
              this.snackBar.open('Server Internal Error: ' + error.message, '', {
                duration: 3000,
              });

              break;
          }
        }

        return throwError(error);
      }));
  }
}

function getResolvedUrl(route: ActivatedRouteSnapshot): string {
  return route.pathFromRoot
    .map(v => v.url.map(segment => segment.toString()).join('/'))
    .join('/');
}

@Injectable()
export class CanActivateDeveloper implements CanActivate {
  private authService = inject(AuthService);


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const userToken = this.authService.getUserToken();
    if (userToken.roles.includes(this.authService.ROLE_DEVELOPER)) {
      return true;
    }

    this.authService.logout(getResolvedUrl(route));
    return false;
  }

}

@Injectable()
export class CanActivateAdmin implements CanActivate {
  private authService = inject(AuthService);


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const userToken = this.authService.getUserToken();
    if (userToken.roles.includes(this.authService.ROLE_ADMIN)) {
      return true;
    }

    this.authService.logout(getResolvedUrl(route));
    return false;
  }

}

@Injectable()
export class CanActivateUser implements CanActivate {
  private authService = inject(AuthService);


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const userToken = this.authService.getUserToken();
    if (this.authService.isAuthenticated()) {
      return true;
    }

    this.authService.logout(getResolvedUrl(route));
    return false;
  }

}

@Injectable()
export class CanActivateTenantInstance implements CanActivate {
  private authService = inject(AuthService);


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const userToken = this.authService.getUserToken();
    return userToken.tenant.enabled;
  }

}
