import { Injectable, inject } from '@angular/core';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material/dialog';
import {ConfirmDialogConfig} from './confirm-dialog.domain';
import {ConfirmDialogComponent} from './confirm-dialog.component';

@Injectable()
export class ConfirmDialogService {
  private dialog = inject(MatDialog);


  newConfig(): MatDialogConfig {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    return dialogConfig;
  }

  open(dialogConfig: MatDialogConfig): MatDialogRef<ConfirmDialogComponent, any> {
    return this.dialog.open(ConfirmDialogComponent, dialogConfig);
  }
}
