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
import {catchError} from 'rxjs/operators';
import {Observable, throwError} from 'rxjs';
import {MatSnackBar} from '@angular/material';
import {UserToken} from './domain/auth';

@Injectable()
export class AuthService implements OnInit, CanActivate {

  USER_INFO_KEY = 'X-SRT-UserInfo';

  constructor(private http: HttpClient,
              private router: Router) {
  }

  ngOnInit() {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.isAuthenticated()) {
      return true;
    }

    this.logout(getResolvedUrl(route));
    return false;
  }

  authenticate(credentials, callback?: (value: any) => void, errCallback?: (error: any) => void) {
    const params = new HttpParams()
      .set('username', credentials.username)
      .set('password', credentials.password);
    const headers = new HttpHeaders({'Content-Type': 'application/x-www-form-urlencoded'});
    this.http.post<UserToken>('/api/' + environment.loginPath, params, {
      headers: headers
    }).subscribe(res => {
      localStorage.setItem(this.USER_INFO_KEY, btoa(JSON.stringify(res)));
      return callback && callback(res);
    }, err => {
      return errCallback && errCallback(err);
    });
  }

  getUserToken() {
    if (this.isAuthenticated()) {
      try {
        return JSON.parse(atob(localStorage.getItem(this.USER_INFO_KEY)));
      } catch (e) {
        this.logout();
      }
    }

    this.logout();
  }

  isAuthenticated() {
    const value = localStorage.getItem(this.USER_INFO_KEY);
    return (value);
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
    if (url !== undefined && 'login' !== url && url.length > 0) {
      this.router.navigate(commands, {
        queryParams: {
          next: url
        }
      });
    } else {
      this.router.navigate(commands);
    }
  }
}

@Injectable()
export class XhrInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const xhr = req.clone({
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
                duration: 2000,
              });

              break;

            case 400:
              this.snackBar.open(error.headers.get('x-error-message'), '', {
                duration: 2000,
              });

              break;

            case 401:
            case 403:
              if (req.url.indexOf(environment.logoutPath) === -1) {
                this.snackBar.open('Authentication Failure', '', {
                  duration: 2000,
                });

                this.auth.logout(window.location.pathname);
              }

              break;

            default:
              this.snackBar.open('Server Internal Error: ' + error.message, '', {
                duration: 2000,
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
    if (userToken.role === 'developer') {
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
    if (userToken.role === 'end-user' || userToken.role === 'developer') {
      return true;
    }

    this.authService.logout(getResolvedUrl(route));
    return false;
  }

}
