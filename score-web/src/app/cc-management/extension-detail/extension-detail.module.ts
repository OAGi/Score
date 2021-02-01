import {DragDropModule} from '@angular/cdk/drag-drop';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {AuthService} from '../../authentication/auth.service';
import {MatInputModule} from '@angular/material/input';
import {ContextMenuModule} from 'ngx-contextmenu';
import {TranslateModule} from '@ngx-translate/core';
import {AppendAssociationDialogComponent} from '../acc-detail/append-association-dialog/append-association-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {AngularSplitModule} from 'angular-split';
import {ExtensionDetailComponent} from './extension-detail.component';
import {SearchOptionsDialogModule} from '../search-options-dialog/search-options-dialog.module';

const routes: Routes = [
  {
    path: 'core_component/extension',
    children: [
      {
        path: ':manifestId',
        component: ExtensionDetailComponent,
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
    ContextMenuModule.forRoot({
      useBootstrap4: true,
    }),
    CommonModule,
    TranslateModule,
    ScoreCommonModule,
    SearchOptionsDialogModule,
    DragDropModule,
    AngularSplitModule
  ],
  declarations: [
    ExtensionDetailComponent,
  ],
  entryComponents: [
    AppendAssociationDialogComponent,
  ]
})
export class ExtensionDetailModule {
}
