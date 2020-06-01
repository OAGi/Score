import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StateProgressBarComponent} from './state-progress-bar.component';
import {RouterModule} from '@angular/router';


@NgModule({
  declarations: [StateProgressBarComponent],
  exports: [
    StateProgressBarComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ]
})
export class StateProgressBarModule {
}
