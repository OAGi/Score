import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModuleSetReleaseModule} from './module-set-release/module-set-release.module';
import {ModuleSetListModule} from './module-set/module-set-list.module';
import {ModuleService} from './domain/module.service';


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
export class ModuleManagementModule { }
