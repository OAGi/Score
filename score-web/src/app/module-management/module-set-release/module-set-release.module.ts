import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ContextMenuModule} from 'ngx-contextmenu';
import {AuthService} from '../../authentication/auth.service';
import {ScoreCommonModule} from '../../common/score-common.module';
import {MaterialModule} from '../../material.module';
import {ModuleSetReleaseCreateComponent} from './module-set-release-create/module-set-release-create.component';
import {ModuleSetReleaseDetailComponent} from './module-set-release-detail/module-set-release-detail.component';
import {ModuleSetReleaseListComponent} from './module-set-release-list/module-set-release-list.component';


const routes: Routes = [
  {
    path: 'module_management/module_set_release',
    children: [
      {
        path: '',
        component: ModuleSetReleaseListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: ModuleSetReleaseCreateComponent,
        canActivate: [AuthService],
      },
      {
        path: ':moduleSetReleaseId',
        component: ModuleSetReleaseDetailComponent,
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
    ContextMenuModule,
    ScoreCommonModule
  ],
  declarations: [
    ModuleSetReleaseListComponent,
    ModuleSetReleaseCreateComponent,
    ModuleSetReleaseDetailComponent
  ],
})
export class ModuleSetReleaseModule { }
