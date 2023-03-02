import {DragDropModule} from '@angular/cdk/drag-drop';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AccDetailComponent} from './acc-detail.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {TranslateModule} from '@ngx-translate/core';
import {AppendAssociationDialogComponent} from './append-association-dialog/append-association-dialog.component';
import {BasedAccDialogComponent} from './based-acc-dialog/based-acc-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {AngularSplitModule} from 'angular-split';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';
import {FindUsagesDialogModule} from '../find-usages-dialog/find-usages-dialog.module';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {TagService} from '../../tag-management/domain/tag.service';

const routes: Routes = [
  {
    path: 'core_component/acc/:manifestId',
    children: [
      {
        path: '**',
        component: AccDetailComponent,
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
    CommonModule,
    TranslateModule,
    ScoreCommonModule,
    SearchOptionsDialogModule,
    FindUsagesDialogModule,
    DragDropModule,
    AngularSplitModule,
    FontAwesomeModule
  ],
  declarations: [
    AccDetailComponent,
    AppendAssociationDialogComponent,
    BasedAccDialogComponent
  ],
  entryComponents: [
    AppendAssociationDialogComponent,
    BasedAccDialogComponent
  ],
  providers: [
    TagService
  ]
})
export class AccDetailModule {
}
