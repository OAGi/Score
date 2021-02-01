import {AfterViewChecked, Component, ElementRef, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OAuth2AppInfo} from '../../authentication/domain/auth';
import {Observable} from 'rxjs';

@Component({
  selector: 'score-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements AfterViewChecked {

  credentials = {username: '', password: ''};
  next = '';
  err = undefined;

  paddingTop: string = '10%';
  observer;
  @ViewChild('authForm', {static: false, read: ElementRef}) authForm: ElementRef;
  oauth2AppInfos: Observable<OAuth2AppInfo[]>;

  constructor(public auth: AuthService,
              private http: HttpClient,
              private route: ActivatedRoute,
              private router: Router) {
    this.route.queryParams.subscribe(params => {
      this.next = this.auth.nextParam(params['next']);
      if (!this.next) {
        this.next = '/';
      }
      if (this.auth.isAuthenticated()) {
        this.router.navigateByUrl(this.next);
      }
    });

    this.oauth2AppInfos = this.auth.getOAuth2AppInfos();
  }

  login() {
    this.auth.authenticate(this.credentials, _ => {
      this.router.navigateByUrl(this.next);
    }, err => {
      this.err = err;
    });

    return false;
  }

  get errorMessage(): string {
    if (!this.err) {
      return undefined;
    }

    switch (this.err.status) {
      case 401:
        return 'Invalid username or password';

      case 403:
        return 'Account is disabled';
    }

    return 'Error';
  }

  onClose() {
    this.err = undefined;
  }

  onResize(event?) {
    this.paddingTop = Math.max(window.innerHeight - this.authForm.nativeElement.offsetHeight, 30) / 2 + 'px';
  }

  ngAfterViewChecked(): void {
    setTimeout(() => {
      this.onResize();
    }, 0);
  }

  get role(): string {
    return this.auth.getUserToken().role;
  }
}
