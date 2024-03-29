import {Routes} from '@angular/router';
import {DisabledActivate, DisabledComponent} from './disabled/disabled.component';

import {HomepageComponent} from './homepage/homepage.component';
import {LoginComponent} from './login/login.component';
import {LogoutComponent} from './logout/logout.component';
import {JoinComponent} from './join/join.component';
import {NotFoundComponent} from './not-found/not-found.component';
import {AuthService} from '../authentication/auth.service';
import {environment} from '../../environments/environment';
import {PendingActivate, PendingComponent} from './pending/pending.component';

export const SCORE_WEBAPP_ROUTES: Routes = [
  {
    path: '',
    component: HomepageComponent,
    pathMatch: 'full',
    canActivate: [AuthService]
  },
  {
    path: environment.loginPath,
    component: LoginComponent
  },
  {
    path: environment.logoutPath,
    component: LogoutComponent
  },
  {
    path: 'pending',
    component: PendingComponent,
    canActivate: [PendingActivate]
  },
  {
    path: 'disabled',
    component: DisabledComponent,
    canActivate: [DisabledActivate]
  },
  {
    path: 'join',
    component: JoinComponent
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];
