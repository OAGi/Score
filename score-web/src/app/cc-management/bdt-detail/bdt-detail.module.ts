import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BdtDetailComponent} from './bdt-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';
import {ScoreCommonModule} from '../../common/score-common.module';
import {CcTreeViewportComponent} from '../cc-tree-viewport/cc-tree-viewport.component';
import {ValidationPanelComponent} from '../../common/validation-panel/validation-panel.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {AngularSplitModule} from 'angular-split';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';
import {BdtPanelComponent} from '../detail-panels/bdt-panel.component';
import {BdtScPanelComponent} from '../detail-panels/bdt-sc-panel.component';
import {ValueDomainComponent} from '../detail-panels/value-domain.component';
import {GithubIssueBoxComponent} from '../github-issue-box/github-issue-box.component';

const routes: Routes = [
  {
    path: 'data_type/:manifestId',
    children: [
      {
        path: '**',
        component: BdtDetailComponent,
        canActivate: [AuthService],
        title: 'Data Type'
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
    ValidationPanelComponent,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule,
    GithubIssueBoxComponent,
    BdtPanelComponent,
    BdtScPanelComponent,
    ValueDomainComponent
  ],
  declarations: [
    BdtDetailComponent
  ]
})
export class BdtDetailModule {
}
