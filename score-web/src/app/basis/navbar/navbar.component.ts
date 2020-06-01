import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {UserToken} from '../../authentication/domain/auth';

@Component({
  selector: 'score-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  constructor(private auth: AuthService,
              public translate: TranslateService) {
    translate.addLangs(['ccts', 'oagis']);
    translate.setDefaultLang('ccts');
    const browserLang = translate.getBrowserLang();
    translate.use(browserLang.match(/ccts|oagis/) ? browserLang : 'ccts');
    translate.onLangChange.subscribe((event: LangChangeEvent) => {
    });
  }

  ngOnInit() {
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  logout() {
    this.auth.logout();
  }

  languageCurrentOagis(translate: TranslateService) {
    translate.use('oagis');
  }

  languageCurrentCcts(translate: TranslateService) {
    translate.use('ccts');
  }

  getActiveCcts(translate: TranslateService): boolean {
    return translate.currentLang === 'ccts';
  }
}
