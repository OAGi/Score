import { Component, OnInit, inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ContextSchemeValue} from '../domain/context-scheme';

@Component({
  standalone: false,
  selector: 'score-context-scheme-value-dialog',
  templateUrl: './context-scheme-value-dialog.component.html',
  styleUrls: ['./context-scheme-value-dialog.component.css']
})
export class ContextSchemeValueDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ContextSchemeValueDialogComponent>>(MatDialogRef);
  contextSchemeValue = inject<ContextSchemeValue>(MAT_DIALOG_DATA);


  isAddAction;
  actionName;

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
    return !this.contextSchemeValue.value;
  }
}
