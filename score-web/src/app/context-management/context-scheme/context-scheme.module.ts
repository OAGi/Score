import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ScoreCommonModule} from '../../common/score-common.module';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {CodelistListDialogComponent} from './codelist-list-dialog/codelist-list-dialog.component';
import {ContextSchemeValueDialogComponent} from './context-scheme-value-dialog/context-scheme-value-dialog.component';
import {ContextSchemeService} from './domain/context-scheme.service';
import {ContextSchemeListComponent} from './context-scheme-list/context-scheme-list.component';
import {ContextSchemeCreateComponent} from './context-scheme-create/context-scheme-create.component';
import {ContextSchemeDetailComponent} from './context-scheme-detail/context-scheme-detail.component';
import {ConfirmDialogModule} from '../../common/confirm-dialog/confirm-dialog.module';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {SearchBarModule} from '../../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';

const routes: Routes = [
  {
    path: 'context_management',
    children: [
      {
        path: 'context_scheme',
        component: ContextSchemeListComponent,
        canActivate: [AuthService]
      },
      {
        path: 'context_scheme/create',
        component: ContextSchemeCreateComponent,
        canActivate: [AuthService]
      },
      {
        path: 'context_scheme/:id',
        component: ContextSchemeDetailComponent,
        canActivate: [AuthService]
      },
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
    CommonModule,
    ScoreCommonModule,
    FontAwesomeModule,
    SearchBarModule,
    ColumnSelectorModule
  ],
  declarations: [
    ContextSchemeListComponent,
    ContextSchemeCreateComponent,
    ContextSchemeDetailComponent,
    ContextSchemeValueDialogComponent,
    CodelistListDialogComponent
  ],
  providers: [
    ContextSchemeService
  ]
})
export class ContextSchemeModule {
}
