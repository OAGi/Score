import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ModuleService} from './domain/module.service';
import {ModuleSetReleaseModule} from './module-set-release/module-set-release.module';
import {ModuleSetListModule} from './module-set/module-set-list.module';

@NgModule({
  imports: [
    ModuleSetListModule,
    ModuleSetReleaseModule,
    CommonModule
  ],
  providers: [
    ModuleService
  ]
})
export class ModuleManagementModule {
}
