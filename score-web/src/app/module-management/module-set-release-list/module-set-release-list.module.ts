import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModuleSetReleaseListComponent} from './module-set-release-list.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {ContextMenuModule} from 'ngx-contextmenu';


const routes: Routes = [
  {
    path: 'module_management/module_set_release',
    children: [
      {
        path: '',
        component: ModuleSetReleaseListComponent,
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
    CommonModule,
    ContextMenuModule
  ],
  declarations: [ModuleSetReleaseListComponent],
})
export class ModuleSetReleaseListModule { }
