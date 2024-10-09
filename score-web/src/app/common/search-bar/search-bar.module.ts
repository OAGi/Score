import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../material.module';
import {CommonModule} from '@angular/common';
import {SearchBarComponent} from './search-bar.component';

@NgModule({
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CommonModule
  ],
  declarations: [
    SearchBarComponent
  ],
  exports: [
    SearchBarComponent
  ]
})
export class SearchBarModule {
}
