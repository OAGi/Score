import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModuleListComponent} from './module-list/module-list.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {AuthService} from '../authentication/auth.service';
import {ModuleService} from './domain/module.service';
import {ModuleCreateComponent} from './module-create/module-create.component';
import {ModuleDetailComponent} from './module-detail/module-detail.component';
import {ModuleDependencyDialogComponent} from './module-dependency-dialog/module-dependency-dialog.component';
import {TranslateModule} from '@ngx-translate/core';

const routes: Routes = [
  {
    path: 'module',
    children: [
      {
        path: '',
        component: ModuleListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: ModuleCreateComponent,
        canActivate: [AuthService],
      },
      {
        path: ':id',
        component: ModuleDetailComponent,
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
    TranslateModule,
    CommonModule
  ],
  declarations: [
    ModuleListComponent,
    ModuleCreateComponent,
    ModuleDetailComponent,
    ModuleDependencyDialogComponent
  ],
  entryComponents: [
    ModuleDependencyDialogComponent,
  ],
  providers: [
    ModuleService
  ]
})
export class ModuleManagementModule {
}
