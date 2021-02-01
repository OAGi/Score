import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {RouterModule, Routes} from '@angular/router';

import {TranslateModule} from '@ngx-translate/core';
import {AngularSplitModule} from 'angular-split';
import {ContextMenuModule} from 'ngx-contextmenu';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {AuthService} from '../../authentication/auth.service';
import {ScoreCommonModule} from '../../common/score-common.module';
import {MaterialModule} from '../../material.module';
import {ReleaseService} from '../../release-management/domain/release.service';
import {BieListService} from '../bie-list/domain/bie-list.service';

import {BieEditComponent} from './bie-edit.component';
import {BieEditService} from './domain/bie-edit.service';
import {ReuseBieDialogComponent} from './reuse-bie-dialog/reuse-bie-dialog.component';

const routes: Routes = [
  {
    path: 'profile_bie/edit',
    children: [
      {
        path: ':id',
        component: BieEditComponent,
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
    MatDialogModule,
    NgxMatSelectSearchModule,
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    TranslateModule,
    CommonModule,
    ScoreCommonModule,
    AngularSplitModule,
  ],
  declarations: [
    BieEditComponent,
    ReuseBieDialogComponent
  ],
  entryComponents: [
    ReuseBieDialogComponent
  ],
  providers: [
    ReleaseService,
    BieListService,
    BieEditService,
  ]
})
export class BieEditModule {
}
