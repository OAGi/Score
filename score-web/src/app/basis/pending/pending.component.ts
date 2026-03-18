import {HttpClient} from '@angular/common/http';
import { Component, Injectable, OnInit, inject } from '@angular/core';
import {map} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {AuthService} from '../../authentication/auth.service';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {UserToken} from '../../authentication/domain/auth';

@Injectable()
export class PendingActivate implements CanActivate {
  private auth = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient);


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.http.get<UserToken>('/api/' + environment.statePath).pipe(map(res => {
      if (!!res) {
        this.auth.storeUserInfo(res);
        const roles = res.roles;
        if (roles.includes('pending')) {
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
  standalone: false,
  selector: 'score-pending',
  templateUrl: './pending.component.html',
  styleUrls: ['./pending.component.css']
})
export class PendingComponent implements OnInit {
  auth = inject(AuthService);


  ngOnInit(): void {
  }

}
