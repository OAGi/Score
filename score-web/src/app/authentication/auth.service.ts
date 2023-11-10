import {Injectable, OnInit} from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpHeaders,
  HttpInterceptor,
  HttpParams,
  HttpRequest
} from '@angular/common/http';
import {environment} from '../../environments/environment';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import {catchError, map} from 'rxjs/operators';
import {Observable, of, throwError} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OAuth2AppInfo, UserToken} from './domain/auth';
import {MultiActionsSnackBarComponent} from '../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {Clipboard} from '@angular/cdk/clipboard';

@Injectable()
export class AuthService  implements OnInit {

  RESTRICTED_NEXT_PARAMS = ['login', 'pending', 'reject'];
  USER_INFO_KEY = 'X-Score-UserInfo';
  ROLE_DEVELOPER = 'developer';
  ROLE_END_USER = 'end-user';
  ROLE_ADMIN = 'admin';

  constructor(private http: HttpClient,
              private router: Router) {
  }

  ngOnInit() {
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

  isAdmin() {
    const userToken = this.getUserToken();
    if (!userToken.enabled) {
      return false;
    }
    return userToken.roles.includes(this.ROLE_ADMIN);
  }

  logout(url?) {
    localStorage.removeItem(this.USER_INFO_KEY);

    this.http.get('/api/' + environment.logoutPath)
      .subscribe(resp => {
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
      });
    } else {
      return this.router.navigate(commands);
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
    return this.http.get<OAuth2AppInfo[]>('/api/info/oauth2_providers');
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

@Injectable()
export class ErrorAlertInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService,
              private router: Router,
              private snackBar: MatSnackBar,
              private clipboard: Clipboard) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error, caught) => {
        if (error instanceof HttpErrorResponse || error.name === 'HttpErrorResponse') {
          switch (error.status) {
            case 0:
            case 504:
              this.snackBar.open('Gateway Connection Failure', '', {
                duration: 3000,
              });

              break;

            case 503:
              this.snackBar.open('Gateway Service Unavailable', '', {
                duration: 3000,
              });

              break;

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
              if (req.url.indexOf(environment.statePath) !== -1) {
                // ignore
              } else if (req.url.indexOf(environment.logoutPath) === -1) {
                this.snackBar.open('Authentication Failure', '', {
                  duration: 3000,
                });

                this.auth.logout(window.location.pathname);
              }

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
export class CanActivateDeveloper  {

  constructor(private authService: AuthService) {
  }

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
export class CanActivateAdmin  {

  constructor(private authService: AuthService) {
  }

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
export class CanActivateUser  {

  constructor(private authService: AuthService) {
  }

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
export class CanActivateTenantInstance  {

  constructor(private authService: AuthService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const userToken = this.authService.getUserToken();
    return userToken.tenant.enabled;
  }

}
