import {DragDropModule} from '@angular/cdk/drag-drop';
import {AsccPanelComponent} from '../detail-panels/ascc-panel.component';
import {AsccpPanelComponent} from '../detail-panels/asccp-panel.component';
import {BccPanelComponent} from '../detail-panels/bcc-panel.component';
import {BccpPanelComponent} from '../detail-panels/bccp-panel.component';
import {DtPanelComponent} from '../detail-panels/dt-panel.component';
import {DtScPanelComponent} from '../detail-panels/dt-sc-panel.component';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';
import {ScoreCommonModule} from '../../common/score-common.module';
import {CcTreeViewportComponent} from '../cc-tree-viewport/cc-tree-viewport.component';
import {AngularSplitModule} from 'angular-split';
import {ExtensionDetailComponent} from './extension-detail.component';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';

const routes: Routes = [
  {
    path: 'core_component/extension/:manifestId',
    children: [
      {
        path: '**',
        component: ExtensionDetailComponent,
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
    CommonModule,
    TranslatePipe,
    ScoreCommonModule,
    CcTreeViewportComponent,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule,
    GithubIssueBoxComponent,
    DtPanelComponent,
    AsccPanelComponent,
    AsccpPanelComponent,
    BccPanelComponent,
    BccpPanelComponent,
    DtScPanelComponent
  ],
  declarations: [
    ExtensionDetailComponent
  ]
})
export class ExtensionDetailModule {
}
