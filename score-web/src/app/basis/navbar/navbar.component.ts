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

@Component({
  selector: 'score-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  private _notiCount: number = -1;
  public notiMatIcon: string = 'notifications_none';

  constructor(private auth: AuthService,
              private router: Router,
              private message: MessageService,
              private stompService: RxStompService,
              public translate: TranslateService) {
    translate.addLangs(['ccts', 'oagis']);
    translate.setDefaultLang('ccts');
    const browserLang = translate.getBrowserLang();
    translate.use(browserLang.match(/ccts|oagis/) ? browserLang : 'ccts');
    translate.onLangChange.subscribe((event: LangChangeEvent) => {
    });
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

  get backgroundColor(): string {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes(this.auth.ROLE_ADMIN)) {
      return '#FFE4E1'; // Light red
    } else if (userToken.roles.includes(this.auth.ROLE_DEVELOPER)) {
      return '#FAFAD2'; // Light yellow
    } else {
      return '';
    }
  }

  ngOnInit() {
    this.reloadNotiCount();

    // subscribe an event
    const userToken = this.auth.getUserToken();
    if (userToken) {
      this.stompService.watch('/topic/message/' + userToken.username).subscribe((message: Message) => {
        const data = JSON.parse(message.body);
        if (!!data.messageId) {
          this.reloadNotiCount();
        }
      });
    }
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
      params = params.set(param['key'], param['value']);
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
