import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';

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
    RouterModule,
    MaterialModule,
    TranslateModule,
    StateProgressBarModule,
    NgxMatSelectSearchModule,
    CommonModule
  ],
  exports: [
    HomepageComponent,
    LoginComponent,
    JoinComponent,
    NavbarComponent,
    FooterComponent,
    NotFoundComponent
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
  ],
  providers: [
    AboutService,
    PendingActivate,
  ]
})
export class BasisModule {
}
