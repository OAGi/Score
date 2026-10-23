import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ConfirmDialogConfig} from './confirm-dialog.domain';

@Component({
  standalone: false,
  selector: 'score-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ConfirmDialogComponent>>(MatDialogRef);
  config = inject<ConfirmDialogConfig>(MAT_DIALOG_DATA);


  ngOnInit(): void {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
