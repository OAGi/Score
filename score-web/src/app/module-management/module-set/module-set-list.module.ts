import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {ScoreCommonModule} from '../../common/score-common.module';
import {MaterialModule} from '../../material.module';
import {ModuleSetCreateComponent} from './module-set-create/module-set-create.component';
import {ModuleAddDialogComponent} from './module-set-edit/module-add-dialog/module-add-dialog.component';
import {ModuleEditDialogComponent} from './module-set-edit/module-edit-dialog/module-edit-dialog.component';
import {ModuleSetEditComponent} from './module-set-edit/module-set-edit.component';
import {ModuleSetListComponent} from './module-set-list/module-set-list.component';
import {SearchBarModule} from '../../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';
import {TitleWithLibrarySelector} from '../../common/title-with-library-selector/title-with-library-selector';

const routes: Routes = [
  {
    path: 'module_management/module_set',
    children: [
      {
        path: '',
        component: ModuleSetListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: ModuleSetCreateComponent,
        canActivate: [AuthService],
      },
      {
        path: ':moduleSetId',
        component: ModuleSetEditComponent,
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
    CommonModule,
    ScoreCommonModule,
    SearchBarModule,
    ColumnSelectorModule,
    TitleWithLibrarySelector
  ],
  declarations: [
    ModuleSetListComponent,
    ModuleSetCreateComponent,
    ModuleSetEditComponent,
    ModuleEditDialogComponent,
    ModuleAddDialogComponent
  ],
})
export class ModuleSetListModule {
}
