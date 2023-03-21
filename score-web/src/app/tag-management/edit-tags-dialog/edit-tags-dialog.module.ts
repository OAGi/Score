import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {TranslateModule} from '@ngx-translate/core';
import {CommonModule} from '@angular/common';
import {ScoreCommonModule} from '../../common/score-common.module';
import {EditTagsDialogComponent} from './edit-tags-dialog.component';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    TranslateModule,
    CommonModule,
    ScoreCommonModule
  ],
  declarations: [
    EditTagsDialogComponent
  ]
})
export class EditTagsDialogModule {
}
