import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BccpDetailComponent} from './bccp-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';
import {ScoreCommonModule} from '../../common/score-common.module';
import {CcTreeViewportComponent} from '../cc-tree-viewport/cc-tree-viewport.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {AngularSplitModule} from 'angular-split';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';

const routes: Routes = [
  {
    path: 'core_component/bccp/:manifestId',
    children: [
      {
        path: '**',
        component: BccpDetailComponent,
        canActivate: [AuthService],
        title: 'Core Component'
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
    TranslatePipe,
    CommonModule,
    ScoreCommonModule,
    CcTreeViewportComponent,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule,
    GithubIssueBoxComponent
  ],
  declarations: [
    BccpDetailComponent
  ]
})
export class BccpDetailModule {
}
