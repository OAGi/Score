import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FlexLayoutModule} from '@angular/flex-layout';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MarkdownModule, MarkedOptions} from 'ngx-markdown';

import {InjectableRxStompConfig, RxStompService, rxStompServiceFactory} from '@stomp/ng2-stompjs';
import {AgencyIdListModule} from './agency-id-list-management/agency-id-list.module';
import {scoreRxStompConfig} from './common/score-rx-stomp-config';

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
import {MessageManagementModule} from './message-management/message-management.module';

import {RouterModule} from '@angular/router';
import {SCORE_WEBAPP_ROUTES} from './basis/routes';
import {MatIconRegistry} from '@angular/material/icon';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

const httpInterceptorsProviders = [
  {provide: HTTP_INTERCEPTORS, useClass: XhrInterceptor, multi: true},
  {provide: HTTP_INTERCEPTORS, useClass: ErrorAlertInterceptor, multi: true},
];

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FlexLayoutModule,
    RouterModule.forRoot(SCORE_WEBAPP_ROUTES),
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
        provide: MarkedOptions,
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
    BieManagementModule,
    ContextManagementModule,
    CcManagementModule,
    CodeListModule,
    AgencyIdListModule,
    LogManagementModule,
    NamespaceManagementModule,
    ReleaseManagementModule,
    ModuleManagementModule,
    MessageManagementModule,
    FontAwesomeModule
  ],
  declarations: [
    ScoreWebComponent
  ],
  providers: [
    MatIconRegistry,
    AuthService,
    httpInterceptorsProviders,
    {
      provide: InjectableRxStompConfig,
      useValue: scoreRxStompConfig
    },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    },
  ],
  bootstrap: [
    ScoreWebComponent
  ],
  exports: [TranslateModule]
})
export class ScoreWebModule {
}
