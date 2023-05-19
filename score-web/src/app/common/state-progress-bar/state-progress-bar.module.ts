import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StateProgressBarComponent} from './state-progress-bar.component';
import {RouterModule} from '@angular/router';
import {MatTooltipModule} from "@angular/material/tooltip";


@NgModule({
  declarations: [StateProgressBarComponent],
  exports: [
    StateProgressBarComponent
  ],
    imports: [
        CommonModule,
        RouterModule,
        MatTooltipModule
    ]
})
export class StateProgressBarModule {
}
