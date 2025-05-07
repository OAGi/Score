import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {TranslateModule} from '@ngx-translate/core';
import {DtListComponent} from './dt-list.component';
import {CreateBdtDialogComponent} from './create-bdt-dialog/create-bdt-dialog.component';
import {CcNodeService} from '../domain/core-component-node.service';
import {CcListService} from '../cc-list/domain/cc-list.service';
import {ScoreCommonModule} from '../../common/score-common.module';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {TagService} from '../../tag-management/domain/tag.service';
import {SearchBarModule} from '../../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';
import {TitleWithLibrarySelector} from '../../common/title-with-library-selector/title-with-library-selector';

const routes: Routes = [
  {
    path: 'data_type',
    component: DtListComponent,
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
    DtListComponent,
    CreateBdtDialogComponent,
  ],
  providers: [
    CcListService,
    CcNodeService,
    TagService
  ]
})
export class DtListModule {
}
