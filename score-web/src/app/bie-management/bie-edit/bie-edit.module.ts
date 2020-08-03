import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ContextMenuModule} from 'ngx-contextmenu';

import {TranslateModule} from '@ngx-translate/core';
import {GrowlModule} from 'ngx-growl';
import {MatDialogModule} from '@angular/material';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {MaterialModule} from '../../material.module';
import {ArraySortPipe} from '../sort';

import {BieEditComponent, BieEditPublishDialogDetailComponent} from './bie-edit.component';
import {ReleaseService} from '../../release-management/domain/release.service';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BieEditService} from './domain/bie-edit.service';
import {AuthService} from '../../authentication/auth.service';
import {SrtCommonModule} from '../../common/srt-common.module';
import {ConfirmDialogComponent} from './confirm-dialog/confirm-dialog.component';
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
    GrowlModule.forRoot({maxMessages: 10, displayTimeMs: 5000}),
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    TranslateModule,
    CommonModule,
    SrtCommonModule,
  ],
  declarations: [
    BieEditComponent,
    BieEditPublishDialogDetailComponent,
    ArraySortPipe,
    ConfirmDialogComponent,
    ReuseBieDialogComponent,
  ],
  entryComponents: [
    BieEditPublishDialogDetailComponent,
    ConfirmDialogComponent,
    ReuseBieDialogComponent,
  ],
  providers: [
    ReleaseService,
    BieListService,
    BieEditService,
  ]
})
export class BieEditModule {
}
