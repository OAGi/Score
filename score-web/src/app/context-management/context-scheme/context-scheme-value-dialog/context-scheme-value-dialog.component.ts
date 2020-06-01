import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ContextSchemeValue} from '../domain/context-scheme';

@Component({
  selector: 'score-context-scheme-value-dialog',
  templateUrl: './context-scheme-value-dialog.component.html',
  styleUrls: ['./context-scheme-value-dialog.component.css']
})
export class ContextSchemeValueDialogComponent implements OnInit {

  isAddAction;
  actionName;

  constructor(
    public dialogRef: MatDialogRef<ContextSchemeValueDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public contextSchemeValue: ContextSchemeValue
    ) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.isAddAction = (this.contextSchemeValue.guid === undefined);
    if (this.isAddAction) {
      this.actionName = 'Add';
    } else {
      this.actionName = 'Edit';
    }
  }

  isDisabled() {
    return false;
  }
}
