import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {TranslateModule} from '@ngx-translate/core';
import {DisabledActivate, DisabledComponent} from './disabled/disabled.component';

import {HomepageComponent} from './homepage/homepage.component';
import {LoginComponent} from './login/login.component';
import {JoinComponent} from './join/join.component';
import {NavbarComponent} from './navbar/navbar.component';
import {FooterComponent} from './footer/footer.component';
import {NotFoundComponent} from './not-found/not-found.component';
import {PendingActivate, PendingComponent} from './pending/pending.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {MaterialModule} from '../material.module';
import {AboutComponent} from './about/about.component';
import {AuthService} from '../authentication/auth.service';
import {AboutService} from './about/domain/about.service';
import {StateProgressBarModule} from '../common/state-progress-bar/state-progress-bar.module';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {ScoreCommonModule} from '../common/score-common.module';
import {MessageManagementModule} from '../message-management/message-management.module';

const routes: Routes = [
  {
    path: 'about',
    children: [
      {
        path: '',
        component: AboutComponent,
        canActivate: [AuthService],
      },
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    RouterModule,
    MaterialModule,
    TranslateModule,
    StateProgressBarModule,
    NgxMatSelectSearchModule,
    CommonModule,
    ScoreCommonModule,
    MessageManagementModule
  ],
  exports: [
    HomepageComponent,
    LoginComponent,
    JoinComponent,
    NavbarComponent,
    FooterComponent,
    NotFoundComponent,
    AboutComponent,
    PendingComponent,
    DisabledComponent,
  ],
  declarations: [
    HomepageComponent,
    LoginComponent,
    JoinComponent,
    NavbarComponent,
    FooterComponent,
    NotFoundComponent,
    AboutComponent,
    PendingComponent,
    DisabledComponent,
  ],
  providers: [
    AboutService,
    PendingActivate,
    DisabledActivate
  ]
})
export class BasisModule {
}
