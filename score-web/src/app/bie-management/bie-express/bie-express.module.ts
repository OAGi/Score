import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MaterialModule} from '../../material.module';
import {TranslateModule} from '@ngx-translate/core';
import {AuthService} from '../../authentication/auth.service';
import {BieExpressComponent} from './bie-express.component';
import {BieExpressService} from './domain/bie-express.service';
import {MetaHeaderDialogComponent} from './meta-header-dialog/meta-header-dialog.component';
import {PaginationResponseDialogComponent} from './pagination-response-dialog/pagination-response-dialog.component';
import {ScoreCommonModule} from '../../common/score-common.module';
import {SearchBarModule} from '../../common/search-bar/search-bar.module';
import {ColumnSelectorModule} from '../../common/column-selector/column-selector.module';

const routes: Routes = [
  {
    path: 'profile_bie/express',
    component: BieExpressComponent,
    canActivate: [AuthService],
  }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes),
        FormsModule,
        ReactiveFormsModule,
        MaterialModule,
        TranslateModule,
        CommonModule,
        ScoreCommonModule,
        SearchBarModule,
        ColumnSelectorModule,
    ],
  declarations: [
    BieExpressComponent,
    MetaHeaderDialogComponent,
    PaginationResponseDialogComponent,
  ],
  providers: [
    BieExpressService,
    {provide: MatDialogRef, useValue: {}},
    {provide: MAT_DIALOG_DATA, useValue: []},
  ],
  exports: [
    RouterModule,
  ]
})
export class BieExpressModule {
}
