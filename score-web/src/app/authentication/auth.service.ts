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
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {catchError, map} from 'rxjs/operators';
import {Observable, of, throwError} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OAuth2AppInfo, UserToken} from './domain/auth';

@Injectable()
export class AuthService implements OnInit, CanActivate {

  RESTRICTED_NEXT_PARAMS = ['login', 'pending', 'reject'];
  USER_INFO_KEY = 'X-SRT-UserInfo';
  ROLE_DEVELOPER = 'developer';
  ROLE_END_USER = 'end-user';

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
        const role = res.role;
        if (role === 'pending') {
          return this.router.parseUrl('/pending');
        } else if (role === 'reject') {
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

  authenticate(credentials, callback?: (value: any) => void, errCallback?: (error: any) => void) {
    const params = new HttpParams()
      .set('username', credentials.username)
      .set('password', credentials.password);
    const headers = new HttpHeaders({'Content-Type': 'application/x-www-form-urlencoded'});
    this.http.post<UserToken>('/api/' + environment.loginPath, params, {
      headers: headers
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
    let value;
    try {
      value = JSON.parse(atob(localStorage.getItem(this.USER_INFO_KEY)));
    } catch (ignore) {
      value = new UserToken();
      this.storeUserInfo(value);
    }
    return userToken.enabled && (userToken.role === this.ROLE_DEVELOPER || userToken.role === this.ROLE_END_USER);
  }

  logout(url?) {
    localStorage.removeItem(this.USER_INFO_KEY);

    this.http.get('/api/logout').subscribe(_ => {
    });

    if (url === undefined || url === null) {
      this.http.get('/api/' + environment.logoutPath).subscribe(_ => {
        this.redirectToLogin(url);
      }, err => {
        this.redirectToLogin(url);
      });
    } else {
      this.redirectToLogin(url);
    }
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
      this.router.navigate(commands);
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
              private snackBar: MatSnackBar) {
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
              this.snackBar.open(error.headers.get('x-error-message'), '', {
                duration: 3000,
              });

              break;

            case 401:
            case 403:
              if (req.url.indexOf(environment.logoutPath) === -1) {
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
export class CanActivateDeveloper implements CanActivate {

  constructor(private authService: AuthService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const userToken = this.authService.getUserToken();
    if (userToken.role === this.authService.ROLE_DEVELOPER) {
      return true;
    }

    this.authService.logout(getResolvedUrl(route));
    return false;
  }

}

@Injectable()
export class CanActivateUser implements CanActivate {

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
