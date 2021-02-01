import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AsccpDetailComponent} from './asccp-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {TranslateModule} from '@ngx-translate/core';
import {ContextMenuModule} from 'ngx-contextmenu';
import {ScoreCommonModule} from '../../common/score-common.module';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {AngularSplitModule} from 'angular-split';
import {SearchOptionsDialogComponent} from '../search-options-dialog/search-options-dialog.component';
import {SearchOptionsService} from '../search-options-dialog/domain/search-options-service';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';

const routes: Routes = [
  {
    path: 'core_component/asccp',
    children: [
      {
        path: ':manifestId',
        component: AsccpDetailComponent,
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
    MatInputModule,
    TranslateModule,
    CommonModule,
    ContextMenuModule,
    ScoreCommonModule,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule
  ],
  declarations: [
    AsccpDetailComponent,
  ]
})
export class AsccpDetailModule {
}
