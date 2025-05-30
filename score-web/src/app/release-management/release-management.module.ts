import {DragDropModule} from '@angular/cdk/drag-drop';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ScoreCommonModule} from '../common/score-common.module';
import {ReleaseAssignComponent} from './release-assign/release-assign.component';
import {ReleaseDetailComponent} from './release-detail/release-detail.component';
import {ReleaseListComponent} from './release-list/release-list.component';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {AuthService} from '../authentication/auth.service';
import {ReleaseService} from './domain/release.service';
import {ReleaseCreateComponent} from './release-create/release-create.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {ReleaseWhatsChangedDialogComponent} from './release-detail/release-whats-changed-dialog/release-whats-changed-dialog.component';
import {SearchBarModule} from '../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';
import {TitleWithLibrarySelector} from '../common/title-with-library-selector/title-with-library-selector';
import {ReleaseDiagramDialogComponent} from './release-diagram-dialog/release-diagram-dialog.component';

const routes: Routes = [
  {
    path: 'release',
    children: [
      {
        path: '',
        component: ReleaseListComponent,
        canActivate: [AuthService],
      }, {
        path: 'create',
        component: ReleaseCreateComponent,
        canActivate: [AuthService],
      }, {
        path: ':id',
        children: [{
          path: '',
          component: ReleaseDetailComponent,
          canActivate: [AuthService],
        }, {
          path: 'assign',
          component: ReleaseAssignComponent,
          canActivate: [AuthService],
        }]
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
        CommonModule,
        DragDropModule,
        ScoreCommonModule,
        FontAwesomeModule,
        SearchBarModule,
        ColumnSelectorModule,
        TitleWithLibrarySelector
    ],
  declarations: [
    ReleaseListComponent,
    ReleaseCreateComponent,
    ReleaseDetailComponent,
    ReleaseAssignComponent,
    ReleaseWhatsChangedDialogComponent,
    ReleaseDiagramDialogComponent
  ],
  providers: [
    ReleaseService
  ]
})
export class ReleaseManagementModule {
}
