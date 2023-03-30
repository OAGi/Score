import {Component, OnInit} from '@angular/core';
import {AuthService} from './authentication/auth.service';
import {Router} from '@angular/router';
import WebFont from 'webfontloader';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';

const EXPORT_NOTES_ICON = `<svg xmlns="http://www.w3.org/2000/svg" height="48" viewBox="0 96 960 960" width="48"><path d="m661 920 117-117v99h30V752H658v30h99L640 899l21 21Zm-481 16q-24.75 0-42.375-17.625T120 876V276q0-24.75 17.625-42.375T180 216h600q24.75 0 42.375 17.625T840 276v329q-14-8-29.5-13t-30.5-8V276H180v600h309q4 16 9.023 31.172Q503.045 922.345 510 936H180Zm0-107v47-600 308-4 249Zm100-53h211q4-16 9-31t13-29H280v60Zm0-170h344q14-7 27-11.5t29-8.5v-40H280v60Zm0-170h400v-60H280v60Zm452.5 579q-77.5 0-132.5-55.5T545 828q0-78.435 54.99-133.718Q654.98 639 733 639q77 0 132.5 55.282Q921 749.565 921 828q0 76-55.5 131.5t-133 55.5Z"/></svg>`

@Component({
  selector: 'score-web',
  templateUrl: './score-web.component.html',
  styleUrls: ['./score-web.component.css']
})
export class ScoreWebComponent implements OnInit {

  constructor(private auth: AuthService,
              private router: Router,
              private matIconRegistry: MatIconRegistry,
              private sanitizer: DomSanitizer) {

    matIconRegistry.addSvgIconLiteral('export_notes', sanitizer.bypassSecurityTrustHtml(EXPORT_NOTES_ICON));
  }

  ngOnInit(): void {
    WebFont.load({
      google: {
        families: ['Roboto']
      }
    });
  }

  isAuthenticated() {
    return this.auth.isAuthenticated();
  }

}
