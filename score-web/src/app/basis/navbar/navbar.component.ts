import {HttpParams} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {UserToken} from '../../authentication/domain/auth';
import {base64Encode} from '../../common/utility';
import {MessageService} from '../../message-management/domain/message.service';
import {tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {
  SettingsApplicationSettingsService
} from '../../settings-management/settings-application-settings/domain/settings-application-settings.service';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {AboutService} from '../about/domain/about.service';
import {WebPageInfoService} from '../basis.service';

@Component({
  selector: 'score-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  private _notiCount = -1;
  public notiMatIcon = 'notifications_none';
  public brand: SafeHtml;

  constructor(private auth: AuthService,
              private aboutService: AboutService,
              private configService: SettingsApplicationSettingsService,
              private sanitizer: DomSanitizer,
              private router: Router,
              private message: MessageService,
              private stompService: RxStompService,
              public webPageInfo: WebPageInfoService,
              public translate: TranslateService) {
    translate.addLangs(['ccts', 'oagis']);
    translate.setDefaultLang('ccts');
    const browserLang = translate.getBrowserLang();
    translate.use(browserLang.match(/ccts|oagis/) ? browserLang : 'ccts');
    translate.onLangChange.subscribe((event: LangChangeEvent) => {
    });

    if (!!webPageInfo.brand) {
      this.brand = sanitizer.bypassSecurityTrustHtml(webPageInfo.brand);
    }
    if (!!webPageInfo.favicon) {
      (document.querySelector('#appIcon') as HTMLLinkElement).href = webPageInfo.favicon;
    }
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

  get hasTenantRole(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.roles !== undefined && userToken.tenant.roles.length > 0;
  }

  get isBusinessTermEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.businessTerm.enabled;
  }

  get userRole(): string {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes(this.auth.ROLE_ADMIN)) {
      return 'Admin';
    } else if (userToken.roles.includes(this.auth.ROLE_DEVELOPER)) {
      return 'Developer';
    } else {
      return 'End-User';
    }
  }

  ngOnInit() {
    this.reloadNotiCount();

    // subscribe an event
    const userToken = this.auth.getUserToken();
    if (userToken) {
      this.stompService.watch('/topic/message/' + userToken.username).subscribe((message: Message) => {
        const data = JSON.parse(message.body);
        if (!!data.messageId || !!data.messageIdList) {
          this.reloadNotiCount();
        }
      });
    }

    this.stompService.watch('/topic/webpage/info').subscribe((message: Message) => {
      this.webPageInfo.load().subscribe(_ => {});
    });
  }

  reloadNotiCount() {
    this.message.getCountOfUnreadMessages().pipe(tap(
      resp => {
        this.notiMatIcon = (resp > 0) ? 'notifications' : 'notifications_none';
      }
    )).subscribe(resp => {
      this._notiCount = resp;
    });
  }

  navigateMessageListPage() {
    this.reloadNotiCount();
    return this.router.navigateByUrl('/message');
  }

  get notiCount(): number {
    return this._notiCount;
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get username(): string {
    const userToken = this.userToken;
    return (userToken) ? userToken.username : undefined;
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : [];
  }

  get isDeveloper(): boolean {
    return this.roles.includes('developer');
  }

  showContextButton() {
    if (this.isTenantEnabled) {
      return this.auth.isAdmin();
    }
    return true;
  }

  logout() {
    const userToken = this.userToken;
    if (!!userToken && userToken.authentication === 'oauth2') {
      localStorage.removeItem(this.auth.USER_INFO_KEY);
      window.location.href = '/api/oauth2/logout';
    } else {
      this.auth.logout();
    }
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

  q(set: any): string {
    let params = new HttpParams();
    for (const param of set) {
      params = params.set(param.key, param.value);
    }
    return base64Encode(params.toString());
  }

  showTermsAndCodeListButton() {
    if (this.isTenantEnabled) {
      return !this.auth.isAdmin();
    }
    return false;
  }

  openUserGuide($event) {
    let url = this.router.serializeUrl(this.router.createUrlTree(['/docs']));
    if (!url.endsWith('/')) {
      url += '/';
    }
    window.open(url, '_blank');
  }

}
