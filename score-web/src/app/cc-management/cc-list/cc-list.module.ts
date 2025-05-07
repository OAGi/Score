import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {TranslateModule} from '@ngx-translate/core';
import {CcListComponent} from './cc-list.component';
import {CcListService} from './domain/cc-list.service';
import {CcNodeService} from '../domain/core-component-node.service';
import {CreateBccpDialogComponent} from './create-bccp-dialog/create-bccp-dialog.component';
import {CreateAsccpDialogComponent} from './create-asccp-dialog/create-asccp-dialog.component';
import {CreateBodDialogComponent} from './create-bod-dialog/create-bod-dialog.component';
import {CreateVerbDialogComponent} from './create-verb-dialog/create-verb-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {TagService} from '../../tag-management/domain/tag.service';
import {SearchBarModule} from '../../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';
import {TitleWithLibrarySelector} from '../../common/title-with-library-selector/title-with-library-selector';

const routes: Routes = [
  {
    path: 'core_component',
    component: CcListComponent,
    canActivate: [AuthService],
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule,
    TranslateModule,
    ScoreCommonModule,
    SearchBarModule,
    ColumnSelectorModule,
    FontAwesomeModule,
    TitleWithLibrarySelector
  ],
  declarations: [
    CcListComponent,
    CreateAsccpDialogComponent,
    CreateBccpDialogComponent,
    CreateBodDialogComponent,
    CreateVerbDialogComponent,
  ],
  providers: [
    CcListService,
    CcNodeService,
    TagService
  ]
})
export class CcListModule {
}
