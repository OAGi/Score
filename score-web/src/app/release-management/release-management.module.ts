import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReleaseListComponent} from './release-list/release-list.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {AuthService} from '../authentication/auth.service';
import {ReleaseService} from './domain/release.service';

const routes: Routes = [
  {
    path: 'release',
    children: [
      {
        path: '',
        component: ReleaseListComponent,
        canActivate: [AuthService],
      }
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    HotkeyModule,
    CommonModule
  ],
  declarations: [
    ReleaseListComponent
  ],
  providers: [
    ReleaseService
  ]
})
export class ReleaseManagementModule {
}
