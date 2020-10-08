import {HttpClient, HttpRequest} from '@angular/common/http';
import {Component, Injectable, OnInit} from '@angular/core';
import {map} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {AuthService} from '../../authentication/auth.service';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {UserToken} from '../../authentication/domain/auth';

@Injectable()
export class DisabledActivate implements CanActivate {
  constructor(private auth: AuthService,
              private router: Router,
              private http: HttpClient) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.http.get<UserToken>('/api/' + environment.statePath).pipe(map(res => {
      if (!!res) {
        this.auth.storeUserInfo(res);
        const enabled = res.enabled;
        if (!enabled) {
          return true;
        } else {
          return this.router.parseUrl('/');
        }
      } else {
        this.auth.logout();
        return false;
      }
    }));
  }
}

@Component({
  selector: 'score-disabled',
  templateUrl: './disabled.component.html',
  styleUrls: ['./disabled.component.css']
})
export class DisabledComponent implements OnInit {

  constructor(public auth: AuthService) { }

  ngOnInit(): void {
  }

}
