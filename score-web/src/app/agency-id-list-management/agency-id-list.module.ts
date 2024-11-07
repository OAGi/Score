import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AgencyIdListListComponent} from './agency-id-list-list/agency-id-list-list.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../authentication/auth.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../material.module';
import {AgencyIdListService} from './domain/agency-id-list.service';
import {AgencyIdListValueDialogComponent} from './agency-id-list-value-dialog/agency-id-list-value-dialog.component';
import {AgencyIdListDetailComponent} from './agency-id-list-detail/agency-id-list-detail.component';
import {ConfirmDialogModule} from '../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {SearchBarModule} from '../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';

const routes: Routes = [
  {
    path: 'agency_id_list',
    children: [
      {
        path: '',
        component: AgencyIdListListComponent,
        canActivate: [AuthService],
      },
      {
        path: ':manifestId',
        component: AgencyIdListDetailComponent,
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
    CommonModule,
    ScoreCommonModule,
    SearchBarModule,
    ColumnSelectorModule
  ],
  declarations: [
    AgencyIdListListComponent,
    AgencyIdListDetailComponent,
    AgencyIdListValueDialogComponent,
  ],
  providers: [
    AgencyIdListService
  ]
})
export class AgencyIdListModule {
}
