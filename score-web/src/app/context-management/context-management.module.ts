import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ContextCategoryModule} from './context-category/context-category.module';
import {ContextSchemeModule} from './context-scheme/context-scheme.module';
import {BusinessContextModule} from './business-context/business-context.module';

@NgModule({
  imports: [
    ContextCategoryModule,
    ContextSchemeModule,
    BusinessContextModule,
    CommonModule
  ]
})
export class ContextManagementModule {
}
