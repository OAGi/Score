import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MultiActionsSnackBarComponent} from './multi-actions-snack-bar.component';
import {MaterialModule} from '../../material.module';


@NgModule({
  declarations: [MultiActionsSnackBarComponent],
  exports: [
    MultiActionsSnackBarComponent
  ],
  imports: [
    CommonModule,
    MaterialModule
  ]
})
export class MultiActionsSnackBarModule {
}
