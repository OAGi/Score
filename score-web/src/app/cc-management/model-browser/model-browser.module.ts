import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatBadgeModule} from '@angular/material/badge';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {MatDialogModule} from '@angular/material/dialog';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {AngularSplitModule} from 'angular-split';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {ModelBrowserComponent} from './model-browser.component';
import {AuthService} from '../../authentication/auth.service';
import {MaterialModule} from '../../material.module';
import {ConfirmDialogModule} from '../../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../../common/score-common.module';
import {CcPlantumlDiagramComponent} from '../cc-plantuml-diagram/cc-plantuml-diagram.component';
import {MarkdownModule} from 'ngx-markdown';
import {CcPlantumlDiagramModule} from '../cc-plantuml-diagram/cc-plantuml-diagram.module';

const routes: Routes = [
  {
    path: 'core_component/browser/:type/:manifestId',
    children: [
      {
        path: 'plantuml',
        component: CcPlantumlDiagramComponent,
        canActivate: [AuthService],
        title: 'Model Browser Diagram'
      },
      {
        path: '**',
        component: ModelBrowserComponent,
        canActivate: [AuthService],
        title: 'Model Browser'
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
    ConfirmDialogModule,
    TranslateModule,
    CommonModule,
    ScoreCommonModule,
    MatBadgeModule,
    MatDialogModule,
    NgxMatSelectSearchModule,
    AngularSplitModule,
    DragDropModule,
    MarkdownModule,
    FontAwesomeModule,
    CcPlantumlDiagramModule
  ],
  declarations: [
    ModelBrowserComponent
  ]
})
export class ModelBrowserModule {
}
