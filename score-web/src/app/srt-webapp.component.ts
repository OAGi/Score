import {Component, OnInit} from '@angular/core';
import {AuthService} from './authentication/auth.service';
import {Router} from '@angular/router';
import WebFont from 'webfontloader';
import {MatIconRegistry} from '@angular/material';

@Component({
  selector: 'srt-webapp',
  templateUrl: './srt-webapp.component.html',
  styleUrls: ['./srt-webapp.component.css']
})
export class SrtWebappComponent implements OnInit {

  constructor(private auth: AuthService,
              private router: Router,
              private matIconRegistry: MatIconRegistry) {
  }

  ngOnInit(): void {
    WebFont.load({
      google: {
        families: ['Noto Sans', 'Roboto', 'Montserrat']
      }
    });

    this.matIconRegistry.registerFontClassAlias('fontawesome', 'fa');
  }

  isAuthenticated() {
    return this.auth.isAuthenticated();
  }

}
