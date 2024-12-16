import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LibraryService} from './domain/library.service';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {SearchBarModule} from '../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';
import {LibraryListComponent} from './library-list/library-list.component';
import {LibraryCreateComponent} from './library-create/library-create.component';
import {LibraryDetailComponent} from './library-detail/library-detail.component';

const routes: Routes = [
  {
    path: 'library',
    children: [
      {
        path: '',
        component: LibraryListComponent,
        canActivate: [AuthService],
      },
      {
        path: 'create',
        component: LibraryCreateComponent,
        canActivate: [AuthService],
      },
      {
        path: ':id',
        component: LibraryDetailComponent,
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
    ColumnSelectorModule
  ],
  declarations: [
    LibraryListComponent,
    LibraryCreateComponent,
    LibraryDetailComponent
  ],
  providers: [
    LibraryService
  ]
})
export class LibraryManagementModule {
}
