import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatBadgeModule} from '@angular/material/badge';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {ContextMenuModule} from 'ngx-contextmenu';
import {BieExpressModule} from './bie-express/bie-express.module';
import {TransferOwnershipDialogModule} from '../common/transfer-ownership-dialog/transfer-ownership-dialog.module';
import {BieListDialogComponent} from './bie-list-dialog/bie-list-dialog.component';

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
import {BieEditModule} from './bie-edit/bie-edit.module';
import {ConfirmDialogModule} from '../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {BieReportComponent} from './bie-report/bie-report.component';
import {BieReportService} from './bie-report/domain/bie-report.service';
import {BieUpliftComponent} from './bie-uplift/bie-uplift.component';
import {BieUpliftService} from './bie-uplift/domain/bie-uplift.service';
import {BieUpliftProfileBieComponent} from './bie-uplift/bie-uplift-profile-bie.component';
import {ReportDialogComponent} from './bie-uplift/report-dialog/report-dialog.component';

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
      },
      {
        path: 'uplift',
        children: [
          {
            path: '',
            component: BieUpliftProfileBieComponent,
            canActivate: [AuthService]
          },
          {
            path: ':topLevelAsbiepId',
            component: BieUpliftComponent,
            canActivate: [AuthService]
          }
        ]
      },
      {
        path: 'report',
        children: [
          {
            path: '',
            component: BieReportComponent,
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
    ConfirmDialogModule,
    BieExpressModule,
    BieEditModule,
    TransferOwnershipDialogModule,
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    TranslateModule,
    CommonModule,
    ScoreCommonModule,
    MatBadgeModule,
  ],
  declarations: [
    BieCreateBizCtxComponent,
    BieCreateAsccpComponent,
    BieCopyBizCtxComponent,
    BieCopyProfileBieComponent,
    BieUpliftProfileBieComponent,
    BieListComponent,
    BieListDialogComponent,
    BieUpliftComponent,
    ReportDialogComponent,
    BieReportComponent
  ],
  providers: [
    ReleaseService,
    BieCreateService,
    BieCopyService,
    BieListService,
    BieUpliftService,
    BieReportService
  ]
})
export class BieManagementModule {
}
