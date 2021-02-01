import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {ContextMenuModule} from 'ngx-contextmenu';
import {ModuleSetListComponent} from './module-set-list/module-set-list.component';
import {ModuleSetEditComponent} from './module-set-edit/module-set-edit.component';
import {ModuleSetCreateComponent} from './module-set-create/module-set-create.component';

const routes: Routes = [
  {
    path: 'module_management/module_set',
    children: [
      {
        path: '',
        component: ModuleSetListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: ModuleSetCreateComponent,
        canActivate: [AuthService],
      },
      {
        path: ':moduleSetId',
        component: ModuleSetEditComponent,
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
  declarations: [
    ModuleSetListComponent,
    ModuleSetCreateComponent,
    ModuleSetEditComponent
  ],
})
export class ModuleSetListModule { }
