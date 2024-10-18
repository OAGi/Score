import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatBadgeModule} from '@angular/material/badge';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {MatDialogModule} from '@angular/material/dialog';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {AngularSplitModule} from 'angular-split';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {ModelBrowserComponent} from './model-browser.component';
import {AuthService} from '../../authentication/auth.service';
import {MaterialModule} from '../../material.module';
import {ConfirmDialogModule} from '../../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../../common/score-common.module';
import {ModelBrowserService} from './domain/model-browser.service';

const routes: Routes = [
  {
    path: 'core_component/browser/:type/:manifestId',
    children: [
      {
        path: '**',
        component: ModelBrowserComponent,
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
    ConfirmDialogModule,
    TranslateModule,
    CommonModule,
    ScoreCommonModule,
    MatBadgeModule,
    MatDialogModule,
    NgxMatSelectSearchModule,
    AngularSplitModule,
    FontAwesomeModule
  ],
  declarations: [
    ModelBrowserComponent
  ],
  providers: [
    ModelBrowserService
  ]
})
export class ModelBrowserModule {
}
