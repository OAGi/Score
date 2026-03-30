import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AsccpDetailComponent} from './asccp-detail.component';
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
import {CcPlantumlDiagramModule} from '../cc-plantuml-diagram/cc-plantuml-diagram.module';
import {CcPlantumlDiagramComponent} from '../cc-plantuml-diagram/cc-plantuml-diagram.component';

const routes: Routes = [
  {
    path: 'core_component/asccp/:manifestId',
    children: [
      {
        path: 'plantuml',
        component: CcPlantumlDiagramComponent,
        canActivate: [AuthService],
        title: 'Core Component Diagram'
      },
      {
        path: '**',
        component: AsccpDetailComponent,
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
    TranslateModule,
    CommonModule,
    ScoreCommonModule,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule,
    CcPlantumlDiagramModule
  ],
  declarations: [
    AsccpDetailComponent
  ]
})
export class AsccpDetailModule {
}
