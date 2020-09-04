import {Component, Inject, OnInit} from '@angular/core';
import {ModuleDependency, SimpleModule} from '../domain/module';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {hashCode} from '../../common/utility';

@Component({
  selector: 'score-module-dependency-dialog',
  templateUrl: './module-dependency-dialog.component.html',
  styleUrls: ['./module-dependency-dialog.component.css']
})
export class ModuleDependencyDialogComponent implements OnInit {

  moduleDependency: ModuleDependency;
  modules: SimpleModule[];

  isAddAction;
  actionName;
  hashCode;

  constructor(
    public dialogRef: MatDialogRef<ModuleDependencyDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data) {

    this.moduleDependency = data.moduleDependency;
    this.modules = data.modules;

    this.hashCode = hashCode(this.moduleDependency);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.isAddAction = (this.moduleDependency.guid === undefined);
    if (this.isAddAction) {
      this.actionName = 'Add';
    } else {
      this.actionName = 'Edit';
    }
  }

  isDisabled() {
    return (this.moduleDependency.dependencyType === undefined || this.moduleDependency.dependencyType === '') ||
      (this.moduleDependency.relatedModuleId === undefined || this.moduleDependency.relatedModuleId <= 0);
  }

  isChanged() {
    return this.hashCode !== hashCode(this.moduleDependency);
  }

}
