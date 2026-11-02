import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslatePipe} from '@ngx-translate/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MaterialModule} from '../../material.module';
import {ConfirmDialogModule} from '../../common/confirm-dialog/confirm-dialog.module';
import {ScoreCommonModule} from '../../common/score-common.module';
import {CcPlantumlDiagramComponent} from '../cc-plantuml-diagram/cc-plantuml-diagram.component';
import {PlantUmlService} from '../../common/plantuml-diagram/plantuml.service';


@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    ConfirmDialogModule,
    TranslatePipe,
    CommonModule,
    ScoreCommonModule,
    MatDialogModule
  ],
  declarations: [
    CcPlantumlDiagramComponent
  ],
  providers: [
    PlantUmlService
  ]
})
export class CcPlantumlDiagramModule {
}
