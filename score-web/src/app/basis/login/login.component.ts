import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'score-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  credentials = {username: '', password: ''};
  next = '';
  err = undefined;

  constructor(private auth: AuthService,
              private http: HttpClient,
              private route: ActivatedRoute,
              private router: Router) {

  }

  ngOnInit() {
    this.route.queryParams
      .subscribe(params => {
        this.next = params['next'] || '/';
        if (this.next.indexOf(environment.loginPath) !== -1) {
          this.next = '/';
        }

        if (this.auth.isAuthenticated()) {
          this.router.navigateByUrl(this.next);
        }
      });
  }

  login() {
    this.auth.authenticate(this.credentials, _ => {
      this.router.navigateByUrl(this.next);
    }, err => {
      this.err = err;
    });
    return false;
  }

  onClose() {
    this.err = undefined;
  }
}
