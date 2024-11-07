import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {CommonModule} from '@angular/common';
import {ColumnSelectorComponent} from './column-selector.component';
import {CdkDropList, DragDropModule} from '@angular/cdk/drag-drop';
import {OverlayModule} from '@angular/cdk/overlay';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule,
    OverlayModule,
    DragDropModule,
    CdkDropList
  ],
  declarations: [
    ColumnSelectorComponent
  ],
  exports: [
    ColumnSelectorComponent
  ]
})
export class ColumnSelectorModule {
}
