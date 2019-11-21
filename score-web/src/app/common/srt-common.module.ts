import {NgModule} from '@angular/core';
import {UnboundedPipe} from './utility';

@NgModule({
  declarations: [
    UnboundedPipe,
  ],
  exports: [
    UnboundedPipe
  ]
})
export class SrtCommonModule {
}
