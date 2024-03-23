import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatMultiSortModule} from 'ngx-mat-multi-sort';
import {AuthService} from '../../authentication/auth.service';
import {MaterialModule} from '../../material.module';
import {ScoreCommonModule} from '../../common/score-common.module';
import {BiePackageListComponent} from './bie-package-list/bie-package-list.component';
import {BiePackageService} from './domain/bie-package.service';
import {BiePackageDetailComponent} from './bie-package-detail/bie-package-detail.component';
import {BiePackageAddBieDialogComponent} from './bie-package-add-bie-dialog/bie-package-add-bie-dialog.component';


const routes: Routes = [
  {
    path: 'bie_package',
    children: [
      {
        path: '',
        component: BiePackageListComponent,
        canActivate: [AuthService]
      },
      {
        path: ':id',
        children: [
          {
            path: '**',
            component: BiePackageDetailComponent,
            canActivate: [AuthService]
          }
        ]
      }
    ]
  }
];

@NgModule({
  declarations: [
    BiePackageListComponent,
    BiePackageDetailComponent,
    BiePackageAddBieDialogComponent
  ],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    MaterialModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
    ScoreCommonModule,
    MatMultiSortModule
  ],
  exports: [
    BiePackageListComponent,
    BiePackageDetailComponent,
    BiePackageAddBieDialogComponent,
    RouterModule,
  ],
  providers: [
    BiePackageService
  ]
})
export class BiePackageModule {
}
