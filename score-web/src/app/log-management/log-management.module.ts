import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {AuthService} from '../authentication/auth.service';
import {MaterialModule} from '../material.module';
import {ScoreCommonModule} from '../common/score-common.module';
import {LogService} from './domain/log.service';
import {LogCompareDialogComponent} from './log-compare-dialog/log-compare-dialog.component';
import {LogListComponent} from './log-list/log-list.component';
import {ColumnSelectorModule} from '../common/column-selector/column-selector.module';

const routes: Routes = [
  {
    path: 'log',
    children: [
      {
        path: 'core-component',
        children: [
          {
            path: ':reference',
            component: LogListComponent,
            canActivate: [AuthService],
          }
        ]
      }
    ]
  }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes),
        ReactiveFormsModule,
        MaterialModule,
        CommonModule,
        ScoreCommonModule,
        ColumnSelectorModule,
    ],
  declarations: [
    LogListComponent,
    LogCompareDialogComponent
  ],
  providers: [
    LogService,
  ]
})
export class LogManagementModule {
}
