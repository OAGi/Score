import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BdtDetailComponent} from './bdt-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {TranslateModule} from '@ngx-translate/core';
import {ScoreCommonModule} from '../../common/score-common.module';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {AngularSplitModule} from 'angular-split';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';
import {CreateDtscDialogComponent} from './create-dtsc-dialog/create-dtsc-dialog.component';

const routes: Routes = [
  {
    path: 'core_component/dt/:manifestId',
    children: [
      {
        path: '**',
        component: BdtDetailComponent,
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
    ScoreCommonModule,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule
  ],
  declarations: [
    BdtDetailComponent,
    CreateDtscDialogComponent
  ]
})
export class BdtDetailModule {
}
