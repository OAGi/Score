import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {ContextMenuModule} from 'ngx-contextmenu';
import {TranslateModule} from '@ngx-translate/core';
import {CcListComponent} from './cc-list.component';
import {CcListService} from './domain/cc-list.service';
import {CcNodeService} from '../domain/core-component-node.service';
import {CreateBccpDialogComponent} from './create-bccp-dialog/create-bccp-dialog.component';
import {CreateAsccpDialogComponent} from './create-asccp-dialog/create-asccp-dialog.component';
import {CreateBodDialogComponent} from './create-bod-dialog/create-bod-dialog.component';
import {CreateVerbDialogComponent} from './create-verb-dialog/create-verb-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';

const routes: Routes = [
  {
    path: 'core_component',
    component: CcListComponent,
    canActivate: [AuthService],
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    CommonModule,
    TranslateModule,
    ScoreCommonModule
  ],
  declarations: [
    CcListComponent,
    CreateAsccpDialogComponent,
    CreateBccpDialogComponent,
    CreateBodDialogComponent,
    CreateVerbDialogComponent,
  ],
  entryComponents: [
    CreateAsccpDialogComponent,
    CreateBccpDialogComponent,
    CreateBodDialogComponent,
    CreateVerbDialogComponent,
  ],
  providers: [
    CcListService,
    CcNodeService
  ]
})
export class CcListModule {
}
