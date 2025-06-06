import {NgModule} from '@angular/core';
import {ActivatedRouteSnapshot, BaseRouteReuseStrategy, RouteReuseStrategy, RouterModule} from '@angular/router';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatIconRegistry} from '@angular/material/icon';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MarkdownModule, MARKED_OPTIONS} from 'ngx-markdown';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {RxStompService, rxStompServiceFactory} from './common/score-rx-stomp';

import {AgencyIdListModule} from './agency-id-list-management/agency-id-list.module';
import {AuthService, ErrorAlertInterceptor, XhrInterceptor} from './authentication/auth.service';
import {LogManagementModule} from './log-management/log-management.module';

import {ScoreWebComponent} from './score-web.component';

import {BasisModule} from './basis/basis.module';
import {AccountManagementModule} from './account-management/account-management.module';
import {ContextManagementModule} from './context-management/context-management.module';
import {CodeListModule} from './code-list-management/code-list.module';
import {BieManagementModule} from './bie-management/bie-management.module';
import {CcManagementModule} from './cc-management/cc-management.module';
import {NamespaceManagementModule} from './namespace-management/namespace-management.module';
import {ReleaseManagementModule} from './release-management/release-management.module';
import {ModuleManagementModule} from './module-management/module-management.module';
import {LibraryManagementModule} from './library-management/library-management.module';
import {MessageManagementModule} from './message-management/message-management.module';
import {BusinessTermManagementModule} from './business-term-management/business-term-management.module';
import {SettingsManagementModule} from './settings-management/settings-management.module';

import {SCORE_WEBAPP_ROUTES} from './basis/routes';
import {WebPageInfoService} from './basis/basis.service';
import {MailService} from './common/score-mail.service';

const httpInterceptorsProviders = [
  {provide: HTTP_INTERCEPTORS, useClass: XhrInterceptor, multi: true},
  {provide: HTTP_INTERCEPTORS, useClass: ErrorAlertInterceptor, multi: true},
];

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

class ShouldReuseRouteFalseRouteReuseStrategy extends BaseRouteReuseStrategy {
  shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
    return false;
  }
}

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FlexLayoutModule,
    RouterModule.forRoot(SCORE_WEBAPP_ROUTES, { onSameUrlNavigation: 'reload' }),
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    FormsModule,
    ReactiveFormsModule,
    MarkdownModule.forRoot({
      loader: HttpClient,
      markedOptions: {
        provide: MARKED_OPTIONS,
        useValue: {
          gfm: true,
          breaks: false,
          pedantic: false,
          smartLists: true,
          smartypants: false,
        }
      }
    }),
    BasisModule,
    AccountManagementModule,
    SettingsManagementModule,
    BieManagementModule,
    ContextManagementModule,
    CcManagementModule,
    CodeListModule,
    AgencyIdListModule,
    LogManagementModule,
    NamespaceManagementModule,
    ReleaseManagementModule,
    ModuleManagementModule,
    LibraryManagementModule,
    MessageManagementModule,
    BusinessTermManagementModule,
    FontAwesomeModule
  ],
  declarations: [
    ScoreWebComponent
  ],
  providers: [
    MatIconRegistry,
    {
      provide: RouteReuseStrategy,
      useClass: ShouldReuseRouteFalseRouteReuseStrategy
    },
    AuthService,
    WebPageInfoService,
    MailService,
    httpInterceptorsProviders,
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory
    }
  ],
  bootstrap: [
    ScoreWebComponent
  ],
  exports: [TranslateModule]
})
export class ScoreWebModule {
}
