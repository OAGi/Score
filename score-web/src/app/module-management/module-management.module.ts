import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModuleSetListModule} from './module-set/module-set-list.module';
import {ModuleSetReleaseListModule} from './module-set-release-list/module-set-release-list.module';
import {ModuleService} from './domain/module.service';


@NgModule({
  imports: [
    ModuleSetListModule,
    ModuleSetReleaseListModule,
    CommonModule
  ],
  providers: [
    ModuleService
  ]
})
export class ModuleManagementModule { }
