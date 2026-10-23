import {HttpParams} from '@angular/common/http';
import { Component, OnInit, OnDestroy, inject, ChangeDetectionStrategy, ElementRef, ChangeDetectorRef, NgZone, HostListener } from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {UserToken} from '../../authentication/domain/auth';
import {base64Encode, loadLibrary, saveLibrary} from '../../common/utility';
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
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  standalone: false,
  selector: 'score-navbar',
  templateUrl: './navbar.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  private auth = inject(AuthService);
  private aboutService = inject(AboutService);
  private libraryService = inject(LibraryService);
  private configService = inject(SettingsApplicationSettingsService);
  private sanitizer = inject(DomSanitizer);
  private router = inject(Router);
  private message = inject(MessageService);
  private stompService = inject(RxStompService);
  private elementRef = inject(ElementRef);
  private changeDetectorRef = inject(ChangeDetectorRef);
  private ngZone = inject(NgZone);
  webPageInfo = inject(WebPageInfoService);
  translate = inject(TranslateService);

  private resizeObserver?: ResizeObserver;
  public navbarWidth = 1400;
  public isDrawerOpen = false;

  private _notiCount = -1;
  public notiMatIcon = 'notifications_none';
  public brand: SafeHtml;

  constructor() {
    const sanitizer = this.sanitizer;
    const webPageInfo = this.webPageInfo;
    const translate = this.translate;

    translate.addLangs(['ccts', 'oagis']);
    translate.setFallbackLang('ccts');
    const browserLang = translate.getBrowserLang();
    const savedLang = localStorage.getItem('score.lang');
    translate.use((savedLang && savedLang.match(/ccts|oagis/)) ? savedLang
        : (browserLang && browserLang.match(/ccts|oagis/) ? browserLang : 'ccts'));
    translate.onLangChange.subscribe((event: LangChangeEvent) => {
      localStorage.setItem('score.lang', event.lang);
    });

    this.refreshBranding();
  }

  get isTenantEnabled(): boolean {
    return this.auth.isTenantEnabled();
  }

  get hasTenantRole(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.roles !== undefined && userToken.tenant.roles.length > 0;
  }

  get isBusinessTermEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.businessTerm.enabled;
  }

  get isBrowseStandardsMenuEnabled(): boolean {
    return this.auth.isBrowseStandardsMenuEnabled();
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

  get isCompact(): boolean {
    return this.navbarWidth < 1100;
  }

  get showFullUserRole(): boolean {
    return this.navbarWidth >= 750;
  }

  get showNistLogo(): boolean {
    return this.navbarWidth >= 1100;
  }

  toggleDrawer(): void {
    this.isDrawerOpen = !this.isDrawerOpen;
  }

  openDrawer(): void {
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
  }

  @HostListener('window:keydown.escape')
  onEscape(): void {
    if (this.isDrawerOpen) {
      this.closeDrawer();
    }
  }

  ngOnInit() {
    this.ensureDefaultLibrarySelection();
    this.webPageInfo.load().subscribe(_ => {
      this.refreshBranding();
    });
    this.reloadNotiCount();

    if (typeof ResizeObserver !== 'undefined' && this.elementRef?.nativeElement) {
      this.ngZone.runOutsideAngular(() => {
        this.resizeObserver = new ResizeObserver(entries => {
          for (const entry of entries) {
            const width = entry.contentRect.width;
            if (width > 0 && Math.abs(this.navbarWidth - width) >= 5) {
              this.ngZone.run(() => {
                this.navbarWidth = width;
                if (!this.isCompact && this.isDrawerOpen) {
                  this.isDrawerOpen = false;
                }
                this.changeDetectorRef.markForCheck();
              });
            }
          }
        });
        this.resizeObserver.observe(this.elementRef.nativeElement);
      });
    }

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
      this.webPageInfo.load().subscribe(_ => {
        this.refreshBranding();
      });
    });
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
  }

  refreshBranding() {
    const webPageInfo = this.webPageInfo;
    this.brand = webPageInfo.brand
      ? this.sanitizer.bypassSecurityTrustHtml(webPageInfo.brand)
      : undefined;
    if (webPageInfo.favicon) {
      (document.querySelector('#appIcon') as HTMLLinkElement).href = webPageInfo.favicon;
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

  get hasSelectedLibrary(): boolean {
    return !!loadLibrary(this.auth.getUserToken());
  }

  ensureDefaultLibrarySelection() {
    const userToken = this.auth.getUserToken();
    if (!userToken || loadLibrary(userToken)) {
      return;
    }

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      const defaultLibrary = libraries.find(library => library.isDefault) || libraries[0];
      if (defaultLibrary?.libraryId) {
        saveLibrary(userToken, defaultLibrary.libraryId);
      }
    });
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
    return translate.currentLang() === 'ccts';
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
