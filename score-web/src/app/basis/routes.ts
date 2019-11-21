import {Routes} from '@angular/router';

import {HomepageComponent} from './homepage/homepage.component';
import {LoginComponent} from './login/login.component';
import {JoinComponent} from './join/join.component';
import {NotFoundComponent} from './not-found/not-found.component';
import {AuthService} from '../authentication/auth.service';
import {environment} from '../../environments/environment';

export const SRT_WEBAPP_ROUTES: Routes = [
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
    path: 'join',
    component: JoinComponent
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];
