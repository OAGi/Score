import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {HotkeyModule} from 'angular2-hotkeys';
import {ContextMenuModule} from 'ngx-contextmenu';
import {BieExpressModule} from './bie-express/bie-express.module';
import {TransferOwnershipDialogModule} from '../common/transfer-ownership-dialog/transfer-ownership-dialog.module';

import {BieListComponent} from './bie-list/bie-list.component';
import {BieCreateBizCtxComponent} from './bie-create/bie-create-biz-ctx.component';
import {BieCreateAsccpComponent} from './bie-create/bie-create-asccp.component';
import {BieCopyBizCtxComponent} from './bie-copy/bie-copy-biz-ctx.component';
import {BieCopyProfileBieComponent} from './bie-copy/bie-copy-profile-bie.component';

import {AuthService} from '../authentication/auth.service';
import {BieListService} from './bie-list/domain/bie-list.service';
import {BieCreateService} from './bie-create/domain/bie-create.service';
import {BieCopyService} from './bie-copy/domain/bie-copy.service';
import {ReleaseService} from '../release-management/domain/release.service';

import {TranslateModule} from '@ngx-translate/core';
import {GrowlModule} from 'ngx-growl';
import {MatDialogModule} from '@angular/material';
import {BieEditModule} from './bie-edit/bie-edit.module';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';

const routes: Routes = [
  {
    path: 'profile_bie',
    children: [
      {
        path: '',
        component: BieListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        children: [
          {
            path: '',
            component: BieCreateBizCtxComponent,
            canActivate: [AuthService]
          },
          {
            path: 'asccp',
            component: BieCreateAsccpComponent,
            canActivate: [AuthService]
          }
        ]
      },
      {
        path: 'copy',
        children: [
          {
            path: '',
            component: BieCopyBizCtxComponent,
            canActivate: [AuthService]
          },
          {
            path: 'bie',
            component: BieCopyProfileBieComponent,
            canActivate: [AuthService]
          }
        ]
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
    BieExpressModule,
    BieEditModule,
    TransferOwnershipDialogModule,
    GrowlModule.forRoot({maxMessages: 10, displayTimeMs: 5000}),
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    TranslateModule,
    CommonModule,
  ],
  declarations: [
    BieCreateBizCtxComponent,
    BieCreateAsccpComponent,
    BieCopyBizCtxComponent,
    BieCopyProfileBieComponent,
    BieListComponent,
  ],
  providers: [
    ReleaseService,
    BieCreateService,
    BieCopyService,
    BieListService,
  ]
})
export class BieManagementModule {
}
