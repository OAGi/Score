import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {MaterialModule} from '../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {AccDetailModule} from './acc-detail/acc-detail.module';
import {AsccpDetailModule} from './asccp-detail/asccp-detail.module';
import {BccpDetailModule} from './bccp-detail/bccp-detail.module';
import {ExtensionDetailModule} from './extension-detail/extension-detail.module';
import {TransferOwnershipDialogModule} from '../common/transfer-ownership-dialog/transfer-ownership-dialog.module';

import {CcListComponent} from './cc-list/cc-list.component';
import {CcListService} from './cc-list/domain/cc-list.service';
import {CcNodeService} from './domain/core-component-node.service';
import {AccCreateComponent} from './acc-create/acc-create.component';
import {AsccpCreateComponent} from './asccp-create/asccp-create.component';
import {BccpCreateComponent} from './bccp-create/bccp-create.component';

import {TranslateModule} from '@ngx-translate/core';
import {ContextMenuModule} from 'ngx-contextmenu';
import {AppendAsccDialogComponent} from './acc-create/append-ascc-dialog/append-ascc-dialog.component';
import {AsccpCreateEditComponent} from './asccp-create/asccp-create-edit.component';

const routes: Routes = [
  {
    path: 'core_component',
    children: [
      {
        path: '',
        component: CcListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'asccp/:id',
        component: AsccpCreateEditComponent,
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
    MaterialModule,
    HotkeyModule,
    AccDetailModule,
    AsccpDetailModule,
    BccpDetailModule,
    ExtensionDetailModule,
    TransferOwnershipDialogModule,
    TranslateModule,
    CommonModule,
    ContextMenuModule,
  ],
  declarations: [
    CcListComponent,
    AccCreateComponent,
    AsccpCreateComponent,
    BccpCreateComponent,
    AppendAsccDialogComponent,
    AsccpCreateEditComponent,
  ],
  entryComponents: [
    AppendAsccDialogComponent
  ],
  providers: [
    CcListService,
    CcNodeService
  ]
})
export class CcManagementModule {
}
