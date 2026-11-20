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
import {canActivateUser, ONLY_DEVELOPERS} from '../authentication/can-activate-user';
import {ReleaseService} from './domain/release.service';
import {ReleaseCreateComponent} from './release-create/release-create.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {ReleaseWhatsChangedDialogComponent} from './release-detail/release-whats-changed-dialog/release-whats-changed-dialog.component';
import {SearchBarModule} from '../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';
import {TitleWithLibrarySelector} from '../common/title-with-library-selector/title-with-library-selector';
import {ReleaseDiagramDialogComponent} from './release-diagram-dialog/release-diagram-dialog.component';
import {ReleasePackageLoadDialogComponent} from './release-package-load-dialog/release-package-load-dialog.component';
import {ReleasePackageLoadService} from './domain/release-package-load.service';
import {SCORE_TABLE} from '../common/score-table';
import {RecordCompactRowComponent} from '../common/record-compact-row';
import {ResponsiveButtonComponent} from '../common/responsive-button';

/** A user who may not create or assign a release goes to the release list. */
const developer = canActivateUser(ONLY_DEVELOPERS, '/release');

const routes: Routes = [
  {
    path: 'release',
    children: [
      {
        path: '',
        component: ReleaseListComponent,
        canActivate: [AuthService],
        title: 'Release'
      }, {
        path: 'create',
        component: ReleaseCreateComponent,
        canActivate: [AuthService, developer],
        title: 'Create Release'
      }, {
        path: ':id',
        title: 'Release',
        children: [{
          path: '',
          component: ReleaseDetailComponent,
          canActivate: [AuthService],
          title: 'Release'
        }, {
          path: 'assign',
          component: ReleaseAssignComponent,
          canActivate: [AuthService, developer],
          title: 'Assign Release'
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
        TitleWithLibrarySelector,
        RecordCompactRowComponent,
        ResponsiveButtonComponent,
        ...SCORE_TABLE
    ],
  declarations: [
    ReleaseListComponent,
    ReleaseCreateComponent,
    ReleaseDetailComponent,
    ReleaseAssignComponent,
    ReleaseWhatsChangedDialogComponent,
    ReleaseDiagramDialogComponent,
    ReleasePackageLoadDialogComponent
  ],
  providers: [
    ReleaseService,
    ReleasePackageLoadService
  ]
})
export class ReleaseManagementModule {
}
