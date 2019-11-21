import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HotkeyModule} from 'angular2-hotkeys';
import {ContextMenuModule} from 'ngx-contextmenu';

import {TranslateModule} from '@ngx-translate/core';
import {GrowlModule} from 'ngx-growl';
import {MatDialogModule} from '@angular/material';
import {MaterialModule} from '../../material.module';
import {ArraySortPipe} from '../sort';

import {BieEditComponent, BieEditPublishDialogDetailComponent} from './bie-edit.component';
import {ReleaseService} from '../../release-management/domain/release.service';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BieEditService} from './domain/bie-edit.service';
import {AuthService} from '../../authentication/auth.service';
import {SrtCommonModule} from '../../common/srt-common.module';


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
    HotkeyModule,
    MatDialogModule,
    GrowlModule.forRoot({maxMessages: 10, displayTimeMs: 5000}),
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    TranslateModule,
    CommonModule,
    SrtCommonModule
  ],
  declarations: [
    BieEditComponent,
    BieEditPublishDialogDetailComponent,
    ArraySortPipe,
  ],
  entryComponents: [
    BieEditPublishDialogDetailComponent,
  ],
  providers: [
    ReleaseService,
    BieListService,
    BieEditService,
  ]
})
export class BieEditModule {
}
