import {Component, OnInit} from '@angular/core';
import {AuthService} from './authentication/auth.service';
import {Router} from '@angular/router';
import WebFont from 'webfontloader';
import {MatIconRegistry} from '@angular/material/icon';

@Component({
  selector: 'score-web',
  templateUrl: './score-web.component.html',
  styleUrls: ['./score-web.component.css']
})
export class ScoreWebComponent implements OnInit {

  constructor(public auth: AuthService,
              private router: Router,
              private matIconRegistry: MatIconRegistry) {
  }

  ngOnInit(): void {
    WebFont.load({
      google: {
        families: ['Roboto']
      }
    });

    this.matIconRegistry.registerFontClassAlias('fontawesome', 'fa');
  }

  isAuthenticated() {
    return this.auth.isAuthenticated();
  }

  get role(): string {
    const token = this.auth.getUserToken();
    return token ? token.role : '';
  }

}
