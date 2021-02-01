import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NamespaceListComponent} from './namespace-list/namespace-list.component';
import {NamespaceCreateComponent} from './namespace-create/namespace-create.component';
import {NamespaceDetailComponent} from './namespace-detail/namespace-detail.component';
import {NamespaceService} from './domain/namespace.service';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {AuthService} from '../authentication/auth.service';
import {ContextMenuModule} from 'ngx-contextmenu';

const routes: Routes = [
  {
    path: 'namespace',
    children: [
      {
        path: '',
        component: NamespaceListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: NamespaceCreateComponent,
        canActivate: [AuthService],
      },
      {
        path: ':id',
        component: NamespaceDetailComponent,
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
    NamespaceListComponent,
    NamespaceCreateComponent,
    NamespaceDetailComponent
  ],
  providers: [
    NamespaceService
  ]
})
export class NamespaceManagementModule {
}
